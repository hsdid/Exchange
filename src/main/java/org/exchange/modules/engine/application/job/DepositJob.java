package org.exchange.modules.engine.application.job;

import org.exchange.modules.core.domain.message.JobInterface;

import java.math.BigDecimal;

public record DepositJob(
        Long userId,
        Long assetId,
        BigDecimal amount
) implements JobInterface { }
