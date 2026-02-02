package org.exchange.modules.core.domain.message;

public interface CommandHandler<R, C extends Command> {
    R handle(C command);
}
