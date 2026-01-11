package org.exchange.modules.engine.infrastructure.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import org.exchange.modules.core.domain.message.JobBusInterface;
import org.exchange.modules.engine.application.job.SendOrderJob;
import org.exchange.modules.engine.infrastructure.cache.InstrumentCache;
import org.exchange.modules.engine.infrastructure.dto.OrderRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
final public class OrderController {
    private final JobBusInterface jobBus;
    private final String queueUrl;
    private final InstrumentCache instrumentCache;

    public OrderController(
            JobBusInterface jobBus,
            @Value("${app.sqs.queue-name}") String queueUrl,
            InstrumentCache instrumentCache
    ) {
        this.jobBus = jobBus;
        this.queueUrl = queueUrl;
        this.instrumentCache = instrumentCache;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest request) throws JsonProcessingException
    {
        Long instrumentId = instrumentCache.getIdBySymbol(request.symbol());

        if (instrumentId == null) {
            return ResponseEntity.badRequest().body("Instrument not found");
        }

        if (!instrumentCache.isActive(request.symbol())) {
            return ResponseEntity.badRequest().body("Instrument is not active");
        }

        SendOrderJob job = new SendOrderJob(
                request.clientOrderId(), // add auto generate if not sent
                request.userId(),
                request.side(),
                instrumentId,
                request.amount(),
                request.price()
        );

        jobBus.send(queueUrl, job, "orders-group");

        return ResponseEntity.ok().build();
    }
}
