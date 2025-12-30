package org.exchange.modules.engine.domain.entity;

import jakarta.persistence.*;
import org.exchange.modules.engine.domain.model.Asset;

import java.time.Instant;

@Entity
@Table(name = "assets", indexes = {
        @Index(name = "idx_asset_symbol", columnList = "symbol", unique = true),
})
public class AssetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    @Column(name = "symbol", nullable = false, unique = true, length = 20)
    private String symbol;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AssetEntity() {
        // JPA requires default constructor
    }
    public AssetEntity(
            String name,
            String symbol
    ) {
        this.name = name;
        this.symbol = symbol;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Asset toDomain() {
        return new Asset(
                id,
                name,
                symbol,
                createdAt
        );
    }
}
