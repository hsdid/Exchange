package org.exchange.modules.core.infrastructure.message;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.exchange.modules.core.domain.message.JobBusInterface;
import org.exchange.modules.core.domain.message.JobInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class JobBus implements JobBusInterface {

    private static final Logger log = LoggerFactory.getLogger(JobBus.class);
    private final SqsTemplate sqsTemplate;

    public JobBus(SqsTemplate sqsTemplate) {
        this.sqsTemplate = sqsTemplate;
    }

    @Override
    public void send(String queueUrl, JobInterface job) {
        send(queueUrl, job, null);
    }

    @Override
    public void send(String queueUrl, JobInterface job, String messageGroupId) {
        log.info("Sending message to SQS queue: {}", job);
        try {
            var messageBuilder = MessageBuilder.withPayload(job);

            if (messageGroupId != null && !messageGroupId.isBlank()) {
                messageBuilder.setHeader("message-group-id", messageGroupId);
            }

            sqsTemplate.send(queueUrl, messageBuilder.build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message to SQS queue: " + queueUrl, e);
        }
    }
}
