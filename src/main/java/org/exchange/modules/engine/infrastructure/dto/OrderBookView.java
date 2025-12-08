package org.exchange.modules.engine.infrastructure.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderBookView(
        String symbol,
        List<LevelDto> asks, // sells
        List<LevelDto> bids  // buys
) {
    public record LevelDto(BigDecimal price, BigDecimal quantity) {}
}
