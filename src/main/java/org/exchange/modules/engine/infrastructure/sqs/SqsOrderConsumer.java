package org.exchange.modules.engine.infrastructure.sqs;

import io.awspring.cloud.sqs.annotation.SqsListener;
import org.exchange.modules.engine.application.job.SendOrderJob;
import org.exchange.modules.engine.application.jobHandler.SendOrderJobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SqsOrderConsumer {
    private static final Logger log = LoggerFactory.getLogger(SqsOrderConsumer.class);
    private final SendOrderJobHandler jobHandler;

    public SqsOrderConsumer(SendOrderJobHandler jobHandler) {
        this.jobHandler = jobHandler;
    }

    @SqsListener(value = "${app.sqs.queue-name}")
    public void listen(SendOrderJob job) {
        log.info("Processing order: {}", job);
        jobHandler.handle(job);
    }
}
