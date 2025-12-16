package org.exchange.modules.engine.infrastructure.sync;

import org.exchange.modules.engine.domain.journal.OrderJournal;
import org.exchange.modules.engine.domain.model.Order;
import org.exchange.modules.engine.domain.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Asynchronous syncer that reads from OrderJournal and persists to database.
 * Runs in a separate thread to avoid blocking the MatchingEngine.
 * 
 * Key features:
 * - Non-blocking: MatchingEngine never waits for DB
 * - Batch processing: Accumulates orders before DB write
 * - Offset tracking: Remembers position in journal file
 * - Fault tolerant: Retries on failure
 */
@Component
public class JournalDatabaseSyncer {
    
    private static final Logger log = LoggerFactory.getLogger(JournalDatabaseSyncer.class);
    
    private final OrderJournal journal;
    private final OrderRepository repository;
    private final ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    private final Path offsetPath;
    private final int batchSize;
    private final long pollIntervalMs;
    
    private long currentOffset = 0;
    
    public JournalDatabaseSyncer(
            OrderJournal journal,
            OrderRepository repository,
            @Value("${app.sync.offset-path:data/sync-offset.txt}") String offsetPathStr,
            @Value("${app.sync.batch-size:1000}") int batchSize,
            @Value("${app.sync.poll-interval-ms:100}") long pollIntervalMs
    ) {
        this.journal = journal;
        this.repository = repository;
        this.offsetPath = Paths.get(offsetPathStr);
        this.batchSize = batchSize;
        this.pollIntervalMs = pollIntervalMs;
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "journal-db-syncer");
            t.setDaemon(false); // Nie daemon - chcemy dokończyć sync przed shutdown
            return t;
        });
    }
    
    @PostConstruct
    public void start() throws IOException {
        loadOffset();
        running.set(true);
        executor.submit(this::syncLoop);
        log.info("JournalDatabaseSyncer started, offset: {}, batchSize: {}", currentOffset, batchSize);
    }
    
    @PreDestroy
    public void stop() {
        log.info("Stopping JournalDatabaseSyncer...");
        running.set(false);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                log.warn("Syncer didn't terminate gracefully, forcing shutdown");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("JournalDatabaseSyncer stopped");
    }
    
    private void syncLoop() {
        while (running.get()) {
            try {
                //log.info("Syncing orders from offset: {}", currentOffset);
                List<Order> batch = new ArrayList<>(batchSize);
                
                // Czytaj z journal od ostatniego offsetu
                long newOffset = journal.readFrom(currentOffset, order -> {
                    log.info("Reading order from journal: {}", order);
                    batch.add(order);
                });
                
                // Jeśli są nowe ordery, zapisz do DB
                if (!batch.isEmpty()) {
                    int saved = repository.saveBatch(batch);
                    log.debug("Synced {} orders to DB, offset: {} -> {}", saved, currentOffset, newOffset);
                    
                    // Zaktualizuj offset tylko po udanym zapisie
                    currentOffset = newOffset;
                    saveOffset();
                }
                
                // Poczekaj przed kolejną iteracją
                Thread.sleep(pollIntervalMs);
                
            } catch (InterruptedException e) {
                log.info("Syncer interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error in sync loop, will retry", e);
                try {
                    Thread.sleep(pollIntervalMs * 10); // Dłuższe czekanie po błędzie
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    private void loadOffset() throws IOException {
        if (Files.exists(offsetPath)) {
            String content = Files.readString(offsetPath).trim();
            currentOffset = Long.parseLong(content);
            log.info("Loaded offset: {}", currentOffset);
        } else {
            log.info("No offset file found, starting from 0");
            currentOffset = 0;
        }
    }
    
    private void saveOffset() {
        try {
            Files.createDirectories(offsetPath.getParent());
            Files.writeString(offsetPath, String.valueOf(currentOffset));
        } catch (IOException e) {
            log.error("Failed to save offset {}", currentOffset, e);
        }
    }
    
    /**
     * Returns current sync status for monitoring.
     */
    public SyncStatus getStatus() throws IOException {
        return new SyncStatus(
                currentOffset,
                journal.size(),
                running.get()
        );
    }
    
    public record SyncStatus(
            long currentOffset,
            long journalSize,
            boolean running
    ) {
        public long lag() {
            return journalSize - currentOffset;
        }
    }
}
