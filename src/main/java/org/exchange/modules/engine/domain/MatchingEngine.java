package org.exchange.modules.engine.domain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.exchange.modules.engine.domain.journal.ExchangeEventJournal;
import org.exchange.modules.engine.domain.journal.JournalModelEvent;
import org.exchange.modules.engine.domain.model.*;
import org.exchange.modules.engine.infrastructure.cache.InstrumentCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.exchange.modules.engine.infrastructure.dto.OrderBookView;
import java.io.IOException;
import java.math.BigDecimal;
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
                    JournalModelEvent event = queue.take();
                    processInternal(event);
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
        queue.offer(order);
    }

    public void process(Deposit deposit) {
        queue.offer(deposit);
    }

    private void replayJournal() throws IOException
    {
        journal.replay(journalObject -> {
            if (journalObject instanceof Order order) {
                //process order without adding to jurnal
                tryLockFunds(order);
                deduplicator.markAsProcessed(order.getClientOrderId());

                MatchResult result = orderBook.process(order);

                Instrument instrument = instrumentCache.getById(order.getInstrumentId());

                if (result.trades() != null) {
                    for (TradeMatch trade : result.trades()) {
                        balanceManager.processTrade(trade, instrument);
                    }
                }
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


    private void processInternal(JournalModelEvent event) throws IOException {
        if (event instanceof Order order) {
            handleOrder(order);
        } else if (event instanceof Deposit deposit) {
            handleDeposit(deposit);
        }
    }

    private void handleOrder(Order order) throws IOException {
        if (deduplicator.isDuplicate(order.getClientOrderId())) {
            log.warn("Ignored duplicate order: {}", order.getClientOrderId());
            return;
        }

        if (!tryLockFunds(order)) {
            log.info("Order rejected due to insufficient funds: {}", order.getClientOrderId());
            // Tu można wygenerować zdarzenie OrderRejected i zapisać/wysłać
            return;
        }

        MatchResult result = orderBook.process(order);
        
        Instrument instrument = instrumentCache.getById(order.getInstrumentId());
        
        if (result.trades() != null) {
            for (TradeMatch trade : result.trades()) {
                balanceManager.processTrade(trade, instrument);
            }
        }
        
        journal.append(order);
        deduplicator.markAsProcessed(order.getClientOrderId());
    }

    private void handleDeposit(Deposit deposit) throws IOException {
        journal.append(deposit);
        balanceManager.deposit(deposit.getUserId(), deposit.getAssetId(), deposit.getAmount());
    }

    private boolean tryLockFunds(Order order) {
        Instrument instrument = instrumentCache.getById(order.getInstrumentId());
        if (instrument == null) {
            log.error("Instrument not found {}", order.getInstrumentId());
            return false;
        }

        Long assetToLockId = (order.getSide() == Side.BUY) ? instrument.getQuoteAssetId() : instrument.getBaseAssetId();
        BigDecimal fundsToLock = (order.getSide() == Side.BUY ? order.getAmount().multiply(order.getPrice()) : order.getAmount());

        return balanceManager.tryLockFunds(order.getUserId(), assetToLockId, fundsToLock);
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
