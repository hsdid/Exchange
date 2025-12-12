package org.exchange.modules.engine.domain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.exchange.modules.engine.domain.journal.OrderJournal;
import org.exchange.modules.engine.domain.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.exchange.modules.engine.infrastructure.dto.OrderBookView;

import java.io.IOException;
import java.util.concurrent.*;

@Service
public final class MatchingEngine {
    private final BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Matching-Engine-Worker");
        t.setDaemon(true);
        return t;
    });

    private final OrderBook orderBook = new OrderBook();
    private static final Logger log = LoggerFactory.getLogger(MatchingEngine.class);

    private final OrderJournal journal;
    private final DeduplicationChecker deduplicator;

    public MatchingEngine(
            OrderJournal journal,
            DeduplicationChecker deduplicator
    ) {
        this.journal = journal;
        this.deduplicator = deduplicator;
    }

    private void startWorker() {
        worker.submit(() -> {
            log.info("Matching Engine Worker started.");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Order order = queue.take();

                    if (deduplicator.isDuplicate(order.getClientOrderId())) {
                        log.info("Ignored duplicate order: {}", order.getClientOrderId());
                        continue;
                    }

                    journal.append(order);
                    deduplicator.markAsProcessed(order.getClientOrderId());
                    orderBook.process(order);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.info("Worker interrupted, shutting down.");
                } catch (Exception e) {
                    log.error("CRITICAL: Error processing order", e);
                }
            }
        });
    }

    @PostConstruct
    public void init() throws IOException {
        // 1. Replay (odtwórz stan)
        journal.replay(order -> {
            deduplicator.markAsProcessed(order.getClientOrderId());
            orderBook.process(order);
        });

        // 2. Init journala do zapisu
        journal.init();

        startWorker();
    }

    public void process(Order order) {
        log.info("Received order: {}", order);
        queue.offer(order);
    }

    @PreDestroy
    public void stop() {
        worker.shutdownNow();
    }

    public void getOrderBookSnapshot(String symbol) {
        OrderBookView snapshot = orderBook.getSnapshot(symbol, 10);
        System.out.println("Order book snapshot: " + snapshot);
    }
}
