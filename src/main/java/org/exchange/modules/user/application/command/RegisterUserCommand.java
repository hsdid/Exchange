package org.exchange.modules.user.application.command;

import org.exchange.modules.core.domain.message.Command;

public record RegisterUserCommand (
        String fullName,
        String email,
        String password
) implements Command {}
