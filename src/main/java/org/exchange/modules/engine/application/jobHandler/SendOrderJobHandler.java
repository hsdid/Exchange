package org.exchange.modules.engine.application.jobHandler;

import org.exchange.modules.core.domain.message.JobHandlerInterface;
import org.exchange.modules.engine.application.job.SendOrderJob;
import org.exchange.modules.engine.domain.MatchingEngine;
import org.exchange.modules.engine.domain.Order;
import org.springframework.stereotype.Component;

@Component
public class SendOrderJobHandler implements JobHandlerInterface<SendOrderJob> {

    private final MatchingEngine engine;

    public SendOrderJobHandler(MatchingEngine engine) {
        this.engine = engine;
    }

    @Override
    public void handle(SendOrderJob job) {
        System.out.println("Handler processing job: " + job);

        // Logic moved from Consumer to Handler
//        Order order = new Order(
//                job.clientOrderId(),
//                job.userId(),
//                job.side(),
//                job.symbol(),
//                job.amount(),
//                job.price()
//        );
//
//        engine.process(order);
    }
}
