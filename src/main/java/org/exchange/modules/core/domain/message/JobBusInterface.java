package org.exchange.modules.core.domain.message;

public interface JobBusInterface {
    void send(String queueUrl, JobInterface job);
    void send(String queueUrl, JobInterface job, String messageGroupId);
}
