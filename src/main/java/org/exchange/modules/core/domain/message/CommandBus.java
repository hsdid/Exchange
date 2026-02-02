package org.exchange.modules.core.domain.message;

public interface CommandBus {
    <C extends Command, R> R dispatch(C command);
}
