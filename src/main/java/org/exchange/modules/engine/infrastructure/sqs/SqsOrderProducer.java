package org.exchange.modules.engine.infrastructure.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.exchange.modules.engine.infrastructure.dto.OrderCommand;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.sqs.SqsClient;
import org.springframework.stereotype.Service;


@Service
public class SqsOrderProducer {
    private final SqsClient sqsClient;
    private final String queueUrl;
    private final ObjectMapper mapper;

    public SqsOrderProducer(
            SqsClient sqsClient,
            @Value("${app.sqs.queue-url}") String queueUrl,
            ObjectMapper mapper
    ) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
        this.mapper = mapper;
    }

    public void send(OrderCommand command) {
        try {
            System.out.println("Sending order: " + command);
            String message = mapper.writeValueAsString(command);
            sqsClient.sendMessage(
                    builder -> builder
                            .queueUrl(queueUrl)
                            .messageGroupId("orders-group")
                            .messageBody(message)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
