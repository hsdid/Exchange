package org.exchange.modules.engine.application.job;

import org.exchange.modules.core.domain.message.JobInterface;
import org.exchange.modules.engine.domain.Side;

import java.math.BigDecimal;

public record SendOrderJob (
        String clientOrderId,
        Long userId,
        Side side,
        String symbol,
        BigDecimal amount,
        BigDecimal price
) implements JobInterface {}
