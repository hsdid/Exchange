package org.exchange.modules.engine.domain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.exchange.modules.engine.domain.journal.ExchangeEventJournal;
import org.exchange.modules.engine.domain.journal.JournalModelEvent;
import org.exchange.modules.engine.domain.model.Deposit;
import org.exchange.modules.engine.domain.model.Instrument;
import org.exchange.modules.engine.domain.model.Order;
import org.exchange.modules.engine.domain.model.Side;
import org.exchange.modules.engine.infrastructure.cache.InstrumentCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.exchange.modules.engine.infrastructure.dto.OrderBookView;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.*;

@Service
public final class MatchingEngine {
    private final BlockingQueue<JournalModelEvent> queue = new LinkedBlockingQueue<>();
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Matching-Engine-Worker");
        t.setDaemon(true);
        return t;
    });

    private final OrderBook orderBook = new OrderBook();
    private static final Logger log = LoggerFactory.getLogger(MatchingEngine.class);

    private final ExchangeEventJournal journal;
    private final DeduplicationChecker deduplicator;
    private final BalanceManager balanceManager;
    private final InstrumentCache instrumentCache;

    public MatchingEngine(
            ExchangeEventJournal journal,
            DeduplicationChecker deduplicator,
            BalanceManager balanceManager,
            InstrumentCache instrumentCache
    ) {
        this.journal = journal;
        this.deduplicator = deduplicator;
        this.balanceManager = balanceManager;
        this.instrumentCache = instrumentCache;
    }

    private void startWorker() {
        worker.submit(() -> {
            log.info("Matching Engine Worker started.");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    JournalModelEvent modelEvent = queue.take();

                    if (modelEvent instanceof Order order) {
                        if (deduplicator.isDuplicate(order.getClientOrderId())) {
                            log.info("Ignored duplicate order: {}", order.getClientOrderId());
                            continue;
                        }

                        journal.append(order);

                        deduplicator.markAsProcessed(order.getClientOrderId());
                        orderBook.process(order, balanceManager);
                    } else if (modelEvent instanceof Deposit deposit) {
                        journal.append(deposit);
                        balanceManager.deposit(
                                deposit.getUserId(),
                                deposit.getAssetId(),
                                deposit.getAmount()
                        );
                    }

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
        log.info("Matching Engine init");
        replayJournal();
        startWorker();
    }

    public void process(Order order) {
        //check if order is already processed
        if (deduplicator.isDuplicate(order.getClientOrderId())) {
            log.info("Ignored duplicate order: {}", order.getClientOrderId());
            return;
        }


        log.info("Received order: {}", order);
        lockFunds(order);
        queue.offer(order);
    }

    public void process(Deposit deposit) {
        log.info("Received deposit: {}", deposit);
        queue.offer(deposit);
    }

    private void lockFunds(Order order) {
        //validate and lock funds
        Instrument instrument = instrumentCache.getById(order.getInstrumentId());
        if (null == instrument) {
            log.warn("Order rejected: Instrument not found for id {}", order.getInstrumentId());
            return;
        }

        Long assetToLockId = (order.getSide() == Side.BUY) ? instrument.getQuoteAssetId() : instrument.getBaseAssetId();

        BigDecimal fundsToLock = (order.getSide() == Side.BUY ? order.getAmount().multiply(order.getPrice()) : order.getAmount());

        boolean locked = balanceManager.tryLockFunds(order.getUserId(), assetToLockId, fundsToLock);
        if (!locked) {
            log.warn("Order rejected: Insufficient funds for user {}", order.getUserId());
        }
    }

    private void replayJournal() throws IOException
    {
        journal.replay(journalObject -> {
            if (journalObject instanceof Order order) {
                //process order without adding to jurnal
                lockFunds(order);
                deduplicator.markAsProcessed(order.getClientOrderId());
                orderBook.process(order, balanceManager);
            } else if (journalObject instanceof Deposit deposit) {
                //process deposit without adding to jurnal
                balanceManager.deposit(
                        deposit.getUserId(),
                        deposit.getAssetId(),
                        deposit.getAmount()
                );
            }
        });
        journal.init();
    }


    @PreDestroy
    public void stop() {
        worker.shutdownNow();
    }

    //TODO: remove only for testing
    public void getOrderBookSnapshot(String symbol) {
        OrderBookView snapshot = orderBook.getSnapshot(symbol, 10);
        System.out.println("Order book snapshot: " + snapshot);
    }
}
