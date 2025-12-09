package org.exchange.modules.engine.domain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy; // upewnij się co do importów
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.exchange.modules.engine.infrastructure.dto.OrderBookView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

@Service
public final class MatchingEngine {

    private final BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    //private final ExecutorService worker = Executors.newFixedThreadPool(3);
    private final OrderBook orderBook = new OrderBook(); // Stan w pamięci

    private final Set<String> processedClientOrderIds = Collections.newSetFromMap(
            new LinkedHashMap<String, Boolean>(100_000, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                    return size() > 100_000; // Limit pamięci
                }
            }
    );

    private FileChannel journalChannel;
    private final Path journalPath;
    // Reużywalny bufor do zapisu (żeby nie tworzyć obiektów w kółko)
    private final ByteBuffer writeBuffer = ByteBuffer.allocate(4096);

    public MatchingEngine(@Value("${app.engine.journal-path:data/journal.log}") String pathStr) {
        this.journalPath = Paths.get(pathStr);
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(journalPath.getParent());

        if (Files.exists(journalPath)) {
            replayJournal();
        }

        // 4. Otwórz kanał do zapisu (append)
        this.journalChannel = FileChannel.open(journalPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND
        );

        startWorker();
    }

    private void replayJournal() throws IOException {
        System.out.println("Replaying binary journal...");

        // Otwieramy kanał tylko do odczytu
        try (FileChannel readChannel = FileChannel.open(journalPath, StandardOpenOption.READ)) {
            // Bufor do nagłówka (4 bajty = długość rekordu)
            ByteBuffer headerBuf = ByteBuffer.allocate(4);

            while (true) {
                headerBuf.clear();
                // Czytamy 4 bajty (długość)
                int bytesRead = readChannel.read(headerBuf);
                if (bytesRead < 4) break; // Koniec pliku (EOF) lub ucięty plik

                headerBuf.flip();
                int payloadLength = headerBuf.getInt();

                // Przygotowujemy bufor na dane
                ByteBuffer payloadBuf = ByteBuffer.allocate(payloadLength);
                while (payloadBuf.hasRemaining()) {
                    readChannel.read(payloadBuf);
                }
                payloadBuf.flip(); // Przygotuj do odczytu

                // Deserializacja
                Order order = BinaryOrderSerializer.deserialize(payloadBuf);

                // Wrzucamy do pamięci
                processedClientOrderIds.add(order.getClientOrderId());
                orderBook.process(order);
            }
        }
        System.out.println("Replay finished.");
    }

    private void startWorker() {
        worker.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Order order = queue.take();

                    if (isDuplicate(order)) {
                        System.out.println("Ignored duplicate order: " + order.getClientOrderId());
                        continue;
                    }

                    appendJournal(order);
                    processedClientOrderIds.add(order.getClientOrderId());
                    orderBook.process(order);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    e.printStackTrace(); // todo log error
                }
            }
        });
    }

    public void process(Order order) {
        System.out.println("Received order: " + order);
        queue.offer(order);
    }

    private void appendJournal(Order order) throws IOException {
        writeBuffer.clear();

        BinaryOrderSerializer.serialize(order, writeBuffer);

        writeBuffer.flip();

        while (writeBuffer.hasRemaining()) {
            journalChannel.write(writeBuffer);
        }
        // journalChannel.force(false);
    }

    @PreDestroy
    public void stop() {
        worker.shutdownNow();
        try {
            if (journalChannel != null) journalChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isDuplicate(Order order) {
        return processedClientOrderIds.contains(order.getClientOrderId());
    }



    public void getOrderBookSnapshot(String symbol) {
        System.out.println("Getting order book snapshot for symbol: " + symbol);
        OrderBookView snapshot = orderBook.getSnapshot(symbol, 10);
        System.out.println("Order book snapshot: " + snapshot);
    }
}
