package org.exchange.modules.engine.domain.entity;

import jakarta.persistence.*;
import org.exchange.modules.engine.domain.model.Deposit;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "deposits")
public class DepositEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "asset_id", nullable = false)
    private Long assetId;
    @Column(name = "amount", nullable = false, precision = 20, scale = 8)
    private BigDecimal amount;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public DepositEntity() {
    }

    public DepositEntity(Deposit deposit) {
        this.userId = deposit.getUserId();
        this.assetId = deposit.getAssetId();
        this.amount = deposit.getAmount();
        this.createdAt = Instant.now();
    }

    public static DepositEntity fromDeposit(Deposit deposit) {
        return new DepositEntity(deposit);
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
