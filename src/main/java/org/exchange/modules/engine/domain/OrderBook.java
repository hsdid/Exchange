package org.exchange.modules.engine.domain;

import org.exchange.modules.engine.infrastructure.dto.OrderBookView;
import org.exchange.modules.engine.infrastructure.dto.OrderCommand;

import java.math.BigDecimal;
import java.util.*;

public class OrderBook {
    // price -> list of orders
    private final NavigableMap<BigDecimal, LinkedList<OrderCommand>> bids = new TreeMap<>(Comparator.reverseOrder()); //list of buy orders
    private final NavigableMap<BigDecimal, LinkedList<OrderCommand>> asks = new TreeMap<>(); //list of sell orders

    public void process(OrderCommand orderCommand) {
        if (orderCommand.side() == Side.BUY) {
            matchBuy(orderCommand);
        } else {
            matchSell(orderCommand);
        }
    }

    private void matchBuy(OrderCommand buyOrder) {
        // naive: match with lowest ask price <= buy.price
        Iterator<Map.Entry<BigDecimal, LinkedList<OrderCommand>>> asksIterator = asks.entrySet().iterator();
        BigDecimal remainingAmountToBuy = buyOrder.amount();
        while (asksIterator.hasNext() && remainingAmountToBuy.compareTo(BigDecimal.ZERO) > 0) {
            Map.Entry<BigDecimal, LinkedList<OrderCommand>> askLevel = asksIterator.next();
            BigDecimal askLevelPrice = askLevel.getKey();
            if (buyOrder.price().compareTo(askLevelPrice) < 0) break; // price not acceptable
            LinkedList<OrderCommand> offerList = askLevel.getValue();
            while (!offerList.isEmpty() && remainingAmountToBuy.compareTo(BigDecimal.ZERO) > 0) {
                OrderCommand ask = offerList.peek();
                BigDecimal tradeQtyToConsume = remainingAmountToBuy.min(ask.amount());
                // here produce Trade record, update wallets -- omitted for brevity
                // reduce quantities (in real model we'd mutate sizes)
                remainingAmountToBuy = remainingAmountToBuy.subtract(tradeQtyToConsume);

                if (tradeQtyToConsume.equals(ask.amount())) {
                    offerList.poll();
                } else {
                    //change ammount
                }
            }
            if (offerList.isEmpty()) asksIterator.remove();
        }
        if (remainingAmountToBuy.compareTo(BigDecimal.ZERO) > 0) {
            // place remaining in bids
            bids.computeIfAbsent(buyOrder.price(), p -> new LinkedList<>()).add(buyOrder);
        }
    }

    private void matchSell(OrderCommand sellOrder) {
        Iterator<Map.Entry<BigDecimal, LinkedList<OrderCommand>>> bidsIterator = bids.entrySet().iterator();
        BigDecimal remainingAmountToSell = sellOrder.amount();
        while (bidsIterator.hasNext() && remainingAmountToSell.compareTo(BigDecimal.ZERO) > 0) {
            Map.Entry<BigDecimal, LinkedList<OrderCommand>> bidsLevel = bidsIterator.next();
            BigDecimal bidPrice = bidsLevel.getKey();
            if (sellOrder.price().compareTo(bidPrice) > 0) break;
            LinkedList<OrderCommand> list = bidsLevel.getValue();
            while (!list.isEmpty() && remainingAmountToSell.compareTo(BigDecimal.ZERO) > 0) {
                OrderCommand bid = list.peek();
                BigDecimal tradeQty = remainingAmountToSell.min(bid.amount());
                // produce trade ...
                remainingAmountToSell = remainingAmountToSell.subtract(tradeQty);
                list.poll();
            }
            if (list.isEmpty()) bidsIterator.remove();
        }
        if (remainingAmountToSell.compareTo(BigDecimal.ZERO) > 0) {
            asks.computeIfAbsent(sellOrder.price(), p -> new LinkedList<>()).add(sellOrder);
        }
    }


    //--------- to delete later
    public OrderBookView getSnapshot(String symbol, int depth) {
        return new OrderBookView(
                symbol,
                collectLevels(asks, depth),
                collectLevels(bids, depth)
        );
    }

    private List<OrderBookView.LevelDto> collectLevels(
            NavigableMap<BigDecimal, LinkedList<OrderCommand>> levels,
            int depth
    ) {
        return levels.entrySet().stream()
                .limit(depth)
                .map(entry -> {
                    BigDecimal price = entry.getKey();
                    // Sumujemy ilość z wszystkich zleceń na tym poziomie
                    BigDecimal totalVolume = entry.getValue().stream()
                            .map(OrderCommand::amount) // 'remainingAmount'
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new OrderBookView.LevelDto(price, totalVolume);
                })
                .toList();
    }
}
