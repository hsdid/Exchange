package org.exchange.modules.engine.infrastructure.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.exchange.modules.engine.domain.MatchingEngine;
import org.exchange.modules.engine.domain.Order;
import org.exchange.modules.engine.infrastructure.dto.OrderCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class SqsOrderConsumer {
    private final SqsClient sqs;
    private final String queueUrl;
    private final ObjectMapper mapper;
    private final MatchingEngine engine;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean running = true;

    public SqsOrderConsumer(
            SqsClient sqs,
            @Value("${app.sqs.queue-url}") String queueUrl,
            ObjectMapper mapper,
            MatchingEngine engine
    ) {
        this.sqs = sqs;
        this.queueUrl = queueUrl;
        this.mapper = mapper;
        this.engine = engine;
    }

    @PostConstruct
    public void start() {
        executor.submit(this::consumeMessages);
    }

    private void consumeMessages() {
        System.out.println("SQS consumer is running on" );
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                var response = sqs.receiveMessage(builder -> builder
                        .queueUrl(queueUrl)
                        .maxNumberOfMessages(10)
                        .waitTimeSeconds(20)
                        .build()
                );

                if (!response.hasMessages()) {
                    continue;
                }

                for (Message message : response.messages()) {
                    processMessage(message);
                }

            } catch (Exception e) {
                System.err.println("Error in SQS consumer loop: " + e.getMessage());
                e.printStackTrace();

                try { Thread.sleep(1000); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
            }
        }
        System.out.println("SQS consumer stopped.");
    }

    private void processMessage(Message message) {
        try {
            var orderCommand = mapper.readValue(message.body(), OrderCommand.class);
            System.out.println("Processing order: " + orderCommand); // Log dla pewności
            //map Command to Order
            Order order = new Order(
                    orderCommand.clientOrderId(),
                    orderCommand.userId(),
                    orderCommand.side(),
                    orderCommand.symbol(),
                    orderCommand.amount(),
                    orderCommand.price()
            );

            engine.process(order);

            sqs.deleteMessage(builder -> builder
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build()
            );
        } catch (Exception e) {
            System.err.println("Failed to process message ID " + message.messageId() + ": " + e.getMessage());
        }
    }

    @PreDestroy
    public void stop() {
        running = false;
        executor.shutdownNow();
    }
}
