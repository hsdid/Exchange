package org.exchange.modules.engine.domain.model;

import java.time.Instant;

public class Asset {
    private final Long id;
    private final String name;
    private final String symbol;
    private final Instant createdAt;

    public Asset(
            Long id,
            String name,
            String symbol,
            Instant createdAt
    ) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public Long getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
