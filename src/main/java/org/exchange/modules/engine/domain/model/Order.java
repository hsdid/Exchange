package org.exchange.modules.engine.domain.model;

import org.exchange.modules.engine.domain.journal.JournalModelEvent;

import java.math.BigDecimal;

final public class Order implements JournalModelEvent {
    private final String clientOrderId;
    private final Long userId;
    private final Side side;
    private final Long instrumentId;  // Changed from String symbol
    private BigDecimal amount;
    private final BigDecimal price;

    public Order(
            String clientOrderId,
            Long userId,
            Side side,
            Long instrumentId,
            BigDecimal amount,
            BigDecimal price
    ) {
        if (clientOrderId == null || clientOrderId.isEmpty()) {
            throw new IllegalArgumentException("Client order ID cannot be null or empty");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (side == null) {
            throw new IllegalArgumentException("Side cannot be null");
        }
        if (instrumentId == null) {
            throw new IllegalArgumentException("Instrument ID cannot be null");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount cannot be null or less than or equal to zero");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price cannot be null or less than or equal to zero");
        }

        this.clientOrderId = clientOrderId;
        this.userId = userId;
        this.side = side;
        this.instrumentId = instrumentId;
        this.amount = amount;
        this.price = price;
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void changeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount cannot be null or less than or equal to zero");
        }
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Side getSide() {
        return side;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }
}
