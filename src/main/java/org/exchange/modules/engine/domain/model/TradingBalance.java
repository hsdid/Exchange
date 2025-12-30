package org.exchange.modules.engine.domain.model;

import java.math.BigDecimal;

public class TradingBalance {
    private BigDecimal available;
    private BigDecimal locked;

    public TradingBalance() {
        this.available = BigDecimal.ZERO;
        this.locked = BigDecimal.ZERO;
    }

    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.available = this.available.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (this.available.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Not enough available balance");
        }
        this.available = this.available.subtract(amount);
    }

    public void lock(BigDecimal amount) {
        if (this.available.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Not enough available balance");
        }
        this.locked = this.locked.add(amount);
        this.available = this.available.subtract(amount);
    }

    public void consumeLocked(BigDecimal amount) {
        if (this.locked.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Not enough locked balance");
        }
        this.locked = this.locked.subtract(amount);
    }

    public void unlock(BigDecimal amount) {
        if (this.locked.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Not enough locked balance");
        }
        this.locked = this.locked.subtract(amount);
        this.available = this.available.add(amount);
    }

    public BigDecimal getLocked() {
        return locked;
    }

    public BigDecimal getAvailable() {
        return available;
    }
}
