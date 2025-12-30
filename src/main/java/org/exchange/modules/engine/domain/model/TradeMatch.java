package org.exchange.modules.engine.domain.model;

import java.math.BigDecimal;

public record TradeMatch(
        Long makerUserId,
        Long takerUserId,
        Long instrumentId,
        BigDecimal price,
        BigDecimal quantity,
        Side takerSide
) {
}
