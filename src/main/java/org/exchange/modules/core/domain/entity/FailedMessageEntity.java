package org.exchange.modules.core.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "failed_messages", indexes = {
    @Index(name = "idx_source_queue", columnList = "source_queue"),
    @Index(name = "idx_failed_at", columnList = "failed_at"),
    @Index(name = "idx_processed", columnList = "processed")
})
public class FailedMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_queue", nullable = false, length = 255)
    private String sourceQueue;

    @Column(name = "message_body", nullable = false, columnDefinition = "TEXT")
    private String messageBody;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "failed_at", nullable = false)
    private Instant failedAt;

    @Column(name = "processed", nullable = false)
    private boolean processed = false;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    protected FailedMessageEntity() {
        // JPA requires default constructor
    }

    public FailedMessageEntity(String sourceQueue, String messageBody, String errorMessage, String stackTrace) {
        this.sourceQueue = sourceQueue;
        this.messageBody = messageBody;
        this.errorMessage = errorMessage;
        this.stackTrace = stackTrace;
        this.failedAt = Instant.now();
        this.processed = false;
        this.retryCount = 0;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public String getSourceQueue() {
        return sourceQueue;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }
}
