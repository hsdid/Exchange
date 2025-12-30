package org.exchange.modules.engine.application.jobHandler;

import org.exchange.modules.core.domain.message.JobHandlerInterface;
import org.exchange.modules.engine.application.job.DepositJob;
import org.exchange.modules.engine.domain.MatchingEngine;
import org.exchange.modules.engine.domain.model.Deposit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DepositJobHandler implements JobHandlerInterface<DepositJob> {
    private static final Logger log = LoggerFactory.getLogger(DepositJobHandler.class);
    private final MatchingEngine engine;

    public DepositJobHandler(MatchingEngine engine) {
        this.engine = engine;
    }
    @Override
    public void handle(DepositJob job) {
        log.info("Handle deposit job");

        Deposit deposit = new Deposit(
                job.userId(),
                job.assetId(),
                job.amount()
        );

        engine.process(deposit);
    }
}
