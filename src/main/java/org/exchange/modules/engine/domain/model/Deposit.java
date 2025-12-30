package org.exchange.modules.engine.domain.model;

import org.exchange.modules.engine.domain.journal.JournalModelEvent;

import java.math.BigDecimal;

final public class Deposit implements JournalModelEvent {
    private final Long userId;
    private final Long assetId;
    private final BigDecimal amount;

    public Deposit(Long userId, Long assetId, BigDecimal amount) {
        this.userId = userId;
        this.assetId = assetId;
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getAssetId() {
        return assetId;
    }
}
