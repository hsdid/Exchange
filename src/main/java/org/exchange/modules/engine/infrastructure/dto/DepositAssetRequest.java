package org.exchange.modules.engine.infrastructure.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositAssetRequest(
        @NotBlank(message = "Asset cannot be blank")
        String asset,
        @NotNull(message = "Amount cannot be null")
        BigDecimal amount,
        @NotNull(message = "User ID cannot be null")
        Long userId
) {
}
