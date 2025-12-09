package org.exchange.modules.core.domain.message;

public interface JobBusInterface {
    void send(String queueUrl, Object payload);
    void send(String queueUrl, Object payload, String messageGroupId);
}
