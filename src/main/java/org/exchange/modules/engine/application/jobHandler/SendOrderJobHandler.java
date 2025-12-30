package org.exchange.modules.engine.application.jobHandler;

import org.exchange.modules.core.domain.message.JobHandlerInterface;
import org.exchange.modules.engine.application.job.SendOrderJob;
import org.exchange.modules.engine.domain.MatchingEngine;
import org.exchange.modules.engine.domain.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SendOrderJobHandler implements JobHandlerInterface<SendOrderJob> {
    private static final Logger log = LoggerFactory.getLogger(SendOrderJobHandler.class);
    private final MatchingEngine engine;

    public SendOrderJobHandler(MatchingEngine engine) {
        this.engine = engine;
    }

    @Override
    public void handle(SendOrderJob job) {
        log.info("Handle job: {}", job);

        Order order = new Order(
                job.clientOrderId(),
                job.userId(),
                job.side(),
                job.instrumentId(),
                job.amount(),
                job.price()
        );

        engine.process(order);
    }
}
