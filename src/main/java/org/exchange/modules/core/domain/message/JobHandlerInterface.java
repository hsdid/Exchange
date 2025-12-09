package org.exchange.modules.core.domain.message;

public interface JobHandlerInterface<T extends JobInterface> {
    void handle(T job);
}
