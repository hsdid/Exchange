package org.exchange.modules.engine.infrastructure.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.exchange.modules.core.domain.message.JobBusInterface;
import org.exchange.modules.engine.application.job.SendOrderJob;
import org.exchange.modules.engine.infrastructure.dto.OrderCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final JobBusInterface jobBus;
    private final String queueUrl;

    public OrderController(
            JobBusInterface jobBus,
            @Value("${app.sqs.queue-url}") String queueUrl
    ) {
        this.jobBus = jobBus;
        this.queueUrl = queueUrl;
    }

    @PostMapping
    public ResponseEntity<?> submitOrder(@RequestBody OrderCommand command) throws JsonProcessingException
    {
        System.out.println(command);

        SendOrderJob job = new SendOrderJob(
                command.clientOrderId(),
                command.userId(),
                command.side(),
                command.symbol(),
                command.amount(),
                command.price()
        );

        jobBus.send(queueUrl, job, "orders-group");

        return ResponseEntity.ok().build();
    }
}
