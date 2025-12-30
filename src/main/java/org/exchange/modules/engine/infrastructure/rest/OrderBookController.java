package org.exchange.modules.engine.infrastructure.rest;

import org.exchange.modules.engine.domain.MatchingEngine;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orderbook")
public class OrderBookController {

    private final MatchingEngine matchingEngine;

    public OrderBookController(MatchingEngine matchingEngine) {
        this.matchingEngine = matchingEngine;
    }

    //TODO: remove only for testing
    @GetMapping("/{symbol}")
    public int getOrderBook(@PathVariable String symbol) {
        matchingEngine.getOrderBookSnapshot(symbol);
        return 200;
    }
}
