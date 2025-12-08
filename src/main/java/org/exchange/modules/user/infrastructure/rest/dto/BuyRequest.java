package org.exchange.modules.user.infrastructure.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


import java.math.BigDecimal;

public record BuyRequest(
        @NotBlank( message = "Symbol cannot be empty")
        String symbol,
        @NotNull( message = "Amount cannot be empty")
        @DecimalMin( value = "0.00001", message = "Amount must be greater than 0.00001")
        BigDecimal amount
) {}
