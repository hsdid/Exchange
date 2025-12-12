package org.exchange.modules.core.infrastructure.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.exchange.modules.core.domain.message.JobBusInterface;
import org.exchange.modules.core.domain.message.JobInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;

@Service
public class JobBus implements JobBusInterface {

    private static final Logger log = LoggerFactory.getLogger(JobBus.class);
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    public JobBus(SqsClient sqsClient, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void send(String queueUrl, JobInterface job) {
        send(queueUrl, job, null);
    }

    @Override
    public void send(String queueUrl, JobInterface job, String messageGroupId) {
        log.info("Sending message to SQS queue: {}", job);
        try {
            String messageBody = objectMapper.writeValueAsString(job);

            sqsClient.sendMessage(builder -> {
                builder.queueUrl(queueUrl).messageBody(messageBody);
                if (messageGroupId != null && !messageGroupId.isBlank()) {
                    builder.messageGroupId(messageGroupId);
                }
                builder.build();
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload to JSON", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message to SQS queue: " + queueUrl, e);
        }
    }
}
