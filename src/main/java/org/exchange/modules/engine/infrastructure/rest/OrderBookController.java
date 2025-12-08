package org.exchange.modules.engine.infrastructure.rest;

import org.exchange.modules.engine.domain.MatchingEngine;
import org.exchange.modules.engine.infrastructure.dto.OrderBookView;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/orderbook")
public class OrderBookController {

    private final MatchingEngine matchingEngine;

    public OrderBookController(MatchingEngine matchingEngine) {
        this.matchingEngine = matchingEngine;
    }

    @GetMapping("/{symbol}")
    public int getOrderBook(@PathVariable String symbol) {
        // Spring Boot potrafi obsłużyć CompletableFuture natywnie
        matchingEngine.getOrderBookSnapshot(symbol);
        return 200;
    }
}
