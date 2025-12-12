package org.exchange.modules.engine.infrastructure.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.exchange.modules.engine.application.job.SendOrderJob;
import org.exchange.modules.engine.application.jobHandler.SendOrderJobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final SendOrderJobHandler jobHandler;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean running = true;
    private static final Logger log = LoggerFactory.getLogger(SqsOrderConsumer.class);

    public SqsOrderConsumer(
            SqsClient sqs,
            @Value("${app.sqs.queue-url}") String queueUrl,
            ObjectMapper mapper,
            SendOrderJobHandler jobHandler
    ) {
        this.sqs = sqs;
        this.queueUrl = queueUrl;
        this.mapper = mapper;
        this.jobHandler = jobHandler;
    }

    @PostConstruct
    public void start() {
        executor.submit(this::consumeMessages);
    }

    private void consumeMessages() {
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
                log.error("Error in SQS consumer loop", e);

                try { Thread.sleep(1000); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
            }
        }
    }

    private void processMessage(Message message) {
        try {
            var job = mapper.readValue(message.body(), SendOrderJob.class);
            log.info("Processing order: {}", job);
            jobHandler.handle(job);

            sqs.deleteMessage(builder -> builder
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build()
            );
        } catch (Exception e) {
            log.error("Failed to process", e);
        }
    }

    @PreDestroy
    public void stop() {
        running = false;
        executor.shutdownNow();
    }
}
