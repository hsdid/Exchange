package org.exchange.modules.user.infrastructure.rest.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest (
        @NotBlank( message = "Symbol cannot be empty")
        String fullName,
        @NotBlank( message = "Symbol cannot be empty")
        String email,
        @NotBlank( message = "Symbol cannot be empty")
        String password
) {}
