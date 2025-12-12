package org.exchange.modules.engine.domain.journal;

import org.exchange.modules.engine.domain.BinaryOrderSerializer;
import org.exchange.modules.engine.domain.model.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.function.Consumer;

@Component
public class OrderJournal implements AutoCloseable {

    private final Path journalPath;
    private final ByteBuffer writeBuffer = ByteBuffer.allocate(4096);
    private FileChannel channel;

    public OrderJournal(@Value("${app.engine.journal-path:data/journal.log}") String pathStr) {
        this.journalPath = Paths.get(pathStr);
    }

    public void append(Order order) throws IOException {
        writeBuffer.clear();
        BinaryOrderSerializer.serialize(order, writeBuffer);
        writeBuffer.flip();
        while (writeBuffer.hasRemaining()) {
            channel.write(writeBuffer);
        }
    }

    // Metoda replay przyjmuje Consumera - co zrobić z odczytanym orderem
    public void replay(Consumer<Order> orderProcessor) throws IOException {
        if (!Files.exists(journalPath)) return;

        try (FileChannel readChannel = FileChannel.open(journalPath, StandardOpenOption.READ)) {
            ByteBuffer headerBuf = ByteBuffer.allocate(4);
            while (true) {
                headerBuf.clear();
                if (readChannel.read(headerBuf) < 4) break;
                headerBuf.flip();

                int len = headerBuf.getInt();
                ByteBuffer payload = ByteBuffer.allocate(len);
                while (payload.hasRemaining()) readChannel.read(payload);
                payload.flip();

                Order order = BinaryOrderSerializer.deserialize(payload);
                orderProcessor.accept(order);
            }
        }
    }

    /**
     * Reads orders from journal starting at given byte offset.
     * Used by async DB syncer to read only new entries.
     *
     * @param fromOffset Byte position to start reading from
     * @param orderProcessor Consumer to process each order
     * @return New offset position after reading (for next iteration)
     */
    public long readFrom(long fromOffset, Consumer<Order> orderProcessor) throws IOException {
        if (!Files.exists(journalPath)) return fromOffset;

        try (FileChannel readChannel = FileChannel.open(journalPath, StandardOpenOption.READ)) {
            readChannel.position(fromOffset);

            ByteBuffer headerBuf = ByteBuffer.allocate(4);
            while (true) {
                long positionBeforeRead = readChannel.position();
                headerBuf.clear();
                int bytesRead = readChannel.read(headerBuf);

                // Koniec pliku lub niepełny header
                if (bytesRead < 4) {
                    return positionBeforeRead; // Zwróć pozycję przed niepełnym rekordem
                }

                headerBuf.flip();
                int len = headerBuf.getInt();

                // Sprawdź czy cały payload jest dostępny
                if (readChannel.size() - readChannel.position() < len) {
                    return positionBeforeRead; // Niepełny payload, poczekaj na kolejną iterację
                }

                ByteBuffer payload = ByteBuffer.allocate(len);
                while (payload.hasRemaining()) readChannel.read(payload);
                payload.flip();

                Order order = BinaryOrderSerializer.deserialize(payload);
                orderProcessor.accept(order);
            }
        }
    }

    /**
     * Returns current size of journal file in bytes.
     * Useful for monitoring and offset tracking.
     */
    public long size() throws IOException {
        if (!Files.exists(journalPath)) return 0;
        return Files.size(journalPath);
    }

    public void init() throws IOException {
        Files.createDirectories(journalPath.getParent());
        this.channel = FileChannel.open(journalPath,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
    }

    @Override
    public void close() throws IOException {
        if (channel != null) channel.close();
    }
}
