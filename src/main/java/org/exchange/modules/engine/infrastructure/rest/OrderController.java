package org.exchange.modules.engine.infrastructure.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.exchange.modules.engine.infrastructure.dto.OrderCommand;
import org.exchange.modules.engine.infrastructure.sqs.SqsOrderProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final SqsOrderProducer producer;

    public OrderController(SqsOrderProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    public ResponseEntity<?> submitOrder(@RequestBody OrderCommand command) throws JsonProcessingException
    {
        System.out.println(command);

        producer.send(command);
        return ResponseEntity.ok().build();
    }
}
