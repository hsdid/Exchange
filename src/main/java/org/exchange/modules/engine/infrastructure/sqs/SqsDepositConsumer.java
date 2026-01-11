package org.exchange.modules.engine.infrastructure.sqs;

import io.awspring.cloud.sqs.annotation.SqsListener;
import org.exchange.modules.engine.application.job.DepositJob;
import org.exchange.modules.engine.application.jobHandler.DepositJobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SqsDepositConsumer {
    private static final Logger log = LoggerFactory.getLogger(SqsDepositConsumer.class);
    private final DepositJobHandler jobHandler;

    public SqsDepositConsumer(DepositJobHandler jobHandler) {
        this.jobHandler = jobHandler;
    }

    @SqsListener(value = "${app.sqs.queue-deposit-name}")
    public void listen(DepositJob job) {
        log.info("Processing deposit job: {}", job);
        jobHandler.handle(job);
    }
}
