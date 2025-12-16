package org.exchange.modules.engine.domain.model;

import java.math.BigDecimal;

/**
 * Represents a tradable instrument (asset/symbol) on the exchange.
 * Contains metadata about trading rules and constraints.
 */
public class Instrument {
    
    private final Long id;
    private final String symbol;
    private final String name;
    private final int precision;        // Decimal places (e.g., 8 for BTC)
    private final BigDecimal minAmount; // Minimum order amount
    private final BigDecimal tickSize;  // Minimum price increment
    private final InstrumentStatus status;
    
    public Instrument(
            Long id,
            String symbol,
            String name,
            int precision,
            BigDecimal minAmount,
            BigDecimal tickSize,
            InstrumentStatus status
    ) {
        if (symbol == null || symbol.isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (precision < 0 || precision > 18) {
            throw new IllegalArgumentException("Precision must be between 0 and 18");
        }
        if (minAmount == null || minAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Min amount must be positive");
        }
        if (tickSize == null || tickSize.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Tick size must be positive");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        this.id = id;
        this.symbol = symbol.toUpperCase();
        this.name = name;
        this.precision = precision;
        this.minAmount = minAmount;
        this.tickSize = tickSize;
        this.status = status;
    }
    
    public boolean isActive() {
        return status == InstrumentStatus.ACTIVE;
    }
    
    public boolean canTrade() {
        return status == InstrumentStatus.ACTIVE;
    }
    
    // Getters
    
    public Long getId() {
        return id;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public String getName() {
        return name;
    }
    
    public int getPrecision() {
        return precision;
    }
    
    public BigDecimal getMinAmount() {
        return minAmount;
    }
    
    public BigDecimal getTickSize() {
        return tickSize;
    }
    
    public InstrumentStatus getStatus() {
        return status;
    }
}
