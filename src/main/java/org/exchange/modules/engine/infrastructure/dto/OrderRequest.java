package org.exchange.modules.engine.infrastructure.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.exchange.modules.engine.domain.model.Side;
import org.exchange.modules.engine.infrastructure.validator.ValidOrder;

import java.math.BigDecimal;

@ValidOrder
public record OrderRequest(
        @NotBlank( message = "client order id cannot be empty")
        String clientOrderId,
        @NotNull( message = "User cannot be empty")
        Long userId,
        @NotNull( message = "Side cannot be empty")
        Side side,
        @NotBlank( message = "Symbol cannot be empty")
        String symbol,
        @NotNull( message = "Amount cannot be empty")
        BigDecimal amount,
        @NotNull( message = "price cannot be empty")
        BigDecimal price
) {
}
