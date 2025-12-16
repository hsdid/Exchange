package org.exchange.modules.engine.domain.entity;

import jakarta.persistence.*;
import org.exchange.modules.engine.domain.model.Order;
import org.exchange.modules.engine.domain.model.Side;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * JPA entity for persisting Order to database.
 * Separated from domain model to keep domain clean.
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_instrument_id", columnList = "instrument_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
public class OrderEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false, length = 4)
    private Side side;
    
    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;
    
    @Column(name = "amount", nullable = false, precision = 20, scale = 8)
    private BigDecimal amount;
    
    @Column(name = "price", nullable = false, precision = 20, scale = 8)
    private BigDecimal price;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OrderEntity() {
        // JPA requires default constructor
    }
    
    public OrderEntity(Order order) {
        this.userId = order.getUserId();
        this.side = order.getSide();
        this.instrumentId = order.getInstrumentId();
        this.amount = order.getAmount();
        this.price = order.getPrice();
        this.createdAt = Instant.now();
    }
    
    public static OrderEntity fromDomain(Order order) {
        return new OrderEntity(order);
    }
    
    // Getters
    
    public Long getId() {
        return id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public Side getSide() {
        return side;
    }
    
    public Long getInstrumentId() {
        return instrumentId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
}
