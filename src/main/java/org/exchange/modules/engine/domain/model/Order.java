package org.exchange.modules.engine.domain.model;

import java.math.BigDecimal;

public class Order {
    private final String clientOrderId;
    private final Long userId;
    private final Side side;
    private final String symbol;
    private BigDecimal amount;
    private final BigDecimal price;

    public Order(
            String clientOrderId,
            Long userId,
            Side side,
            String symbol,
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
        if (symbol == null || symbol.isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
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
        this.symbol = symbol;
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

    public String getSymbol() {
        return symbol;
    }
}
