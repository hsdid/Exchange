package org.exchange.modules.engine.domain.entity;

import jakarta.persistence.*;
import org.exchange.modules.engine.domain.model.Instrument;
import org.exchange.modules.engine.domain.model.InstrumentStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * JPA entity for persisting Instrument to database.
 */
@Entity
@Table(name = "instruments", indexes = {
    @Index(name = "idx_instrument_symbol", columnList = "symbol", unique = true),
    @Index(name = "idx_instrument_status", columnList = "status")
})
public class InstrumentEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "symbol", nullable = false, unique = true, length = 20)
    private String symbol;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "precision", nullable = false)
    private int precision;
    
    @Column(name = "min_amount", nullable = false, precision = 20, scale = 8)
    private BigDecimal minAmount;
    
    @Column(name = "tick_size", nullable = false, precision = 20, scale = 8)
    private BigDecimal tickSize;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InstrumentStatus status;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    protected InstrumentEntity() {
        // JPA requires default constructor
    }
    
    public InstrumentEntity(
            String symbol,
            String name,
            int precision,
            BigDecimal minAmount,
            BigDecimal tickSize,
            InstrumentStatus status
    ) {
        this.symbol = symbol.toUpperCase();
        this.name = name;
        this.precision = precision;
        this.minAmount = minAmount;
        this.tickSize = tickSize;
        this.status = status;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
    
    public Instrument toDomain() {
        return new Instrument(id, symbol, name, precision, minAmount, tickSize, status);
    }
    
    // Getters and setters
    
    public Long getId() {
        return id;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getPrecision() {
        return precision;
    }
    
    public void setPrecision(int precision) {
        this.precision = precision;
    }
    
    public BigDecimal getMinAmount() {
        return minAmount;
    }
    
    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }
    
    public BigDecimal getTickSize() {
        return tickSize;
    }
    
    public void setTickSize(BigDecimal tickSize) {
        this.tickSize = tickSize;
    }
    
    public InstrumentStatus getStatus() {
        return status;
    }
    
    public void setStatus(InstrumentStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
