package org.exchange.modules.core.infrastructure.message.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.exchange.modules.core.domain.entity.FailedMessageEntity;
import org.exchange.modules.core.infrastructure.repository.FailedMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;

@Service
public class SqsDeadLetterConsumer {
    private static final Logger log = LoggerFactory.getLogger(SqsDeadLetterConsumer.class);
    private final FailedMessageRepository failedMessageRepository;
    private final ObjectMapper objectMapper;

    public SqsDeadLetterConsumer(FailedMessageRepository failedMessageRepository, ObjectMapper objectMapper) {
        this.failedMessageRepository = failedMessageRepository;
        this.objectMapper = objectMapper;
    }

    @SqsListener(value = "${app.sqs.queue-dead-letter-name}")
    @Transactional
    public void listen(Message<String> message) {
        try {
            String messageBody = message.getPayload();
            String sourceQueue = extractSourceQueue(message);

            log.warn("Processing dead letter message from queue: {}", sourceQueue);

            String errorMessage = extractHeader(message, "ErrorMessage", "Unknown error");
            String exceptionType = extractHeader(message, "ExceptionType", "Unknown");

            FailedMessageEntity entity = new FailedMessageEntity(
                    sourceQueue,
                    messageBody,
                    errorMessage,
                    "Exception type: " + exceptionType
            );

            failedMessageRepository.save(entity);
            log.info("Saved failed message to database with ID: {}", entity.getId());

        } catch (Exception e) {
            log.error("Failed to save dead letter message to database", e);
            throw new RuntimeException("Failed to process dead letter message", e);
        }
    }

    private String extractSourceQueue(Message<String> message) {
        Object queueName = message.getHeaders().get("QueueName");
        if (queueName != null) {
            return queueName.toString();
        }
        return "unknown";
    }

    private String extractHeader(Message<String> message, String headerName, String defaultValue) {
        Object header = message.getHeaders().get(headerName);
        if (header != null) {
            return header.toString();
        }
        return defaultValue;
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
