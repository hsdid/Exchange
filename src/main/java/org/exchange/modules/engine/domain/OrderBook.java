package org.exchange.modules.engine.domain;

import org.exchange.modules.engine.domain.model.MatchResult;
import org.exchange.modules.engine.domain.model.Order;
import org.exchange.modules.engine.domain.model.Side;
import org.exchange.modules.engine.domain.model.TradeMatch;
import org.exchange.modules.engine.infrastructure.dto.OrderBookView;

import java.math.BigDecimal;
import java.util.*;

public class OrderBook {
    // price -> list of orders
    private final NavigableMap<BigDecimal, LinkedList<Order>> bids = new TreeMap<>(Comparator.reverseOrder()); //list of buy orders
    private final NavigableMap<BigDecimal, LinkedList<Order>> asks = new TreeMap<>(); //list of sell orders

    public MatchResult process(Order order) {
        List<TradeMatch> trades = new ArrayList<>();
        if (order.getSide() == Side.BUY) {
            matchBuy(order, trades);
        } else {
            matchSell(order, trades);
        }

        return new MatchResult(trades, order);
    }

    private void matchBuy(Order buyOrder, List<TradeMatch> trades) {
        // naive: match with lowest ask price <= buy.price
        Iterator<Map.Entry<BigDecimal, LinkedList<Order>>> asksIterator = asks.entrySet().iterator();
        BigDecimal remainingAmountToBuy = buyOrder.getAmount();
        while (asksIterator.hasNext() && remainingAmountToBuy.compareTo(BigDecimal.ZERO) > 0) {
            Map.Entry<BigDecimal, LinkedList<Order>> askLevel = asksIterator.next();
            BigDecimal askLevelPrice = askLevel.getKey();
            if (buyOrder.getPrice().compareTo(askLevelPrice) < 0) break; // price not acceptable
            LinkedList<Order> offerList = askLevel.getValue();
            while (!offerList.isEmpty() && remainingAmountToBuy.compareTo(BigDecimal.ZERO) > 0) {
                // ask sell order
                Order ask = offerList.peek();
                // take sell order base qty what is available
                BigDecimal tradeQtyToConsume = remainingAmountToBuy.min(ask.getAmount());
                remainingAmountToBuy = remainingAmountToBuy.subtract(tradeQtyToConsume);

                // trade record
                trades.add(new TradeMatch(
                        ask.getUserId(),      // Maker
                        buyOrder.getUserId(), // Taker
                        buyOrder.getInstrumentId(),
                        ask.getPrice(),
                        tradeQtyToConsume,
                        Side.BUY              // Taker side
                ));

                if (tradeQtyToConsume.equals(ask.getAmount())) {
                    offerList.poll();
                } else {
                    ask.changeAmount(ask.getAmount().subtract(tradeQtyToConsume));
                }
            }
            if (offerList.isEmpty()) asksIterator.remove();
        }
        if (remainingAmountToBuy.compareTo(BigDecimal.ZERO) > 0) {
            // place remaining in bids
            buyOrder.changeAmount(remainingAmountToBuy);
            bids.computeIfAbsent(buyOrder.getPrice(), p -> new LinkedList<>()).add(buyOrder);
        }
    }

    private void matchSell(Order sellOrder, List<TradeMatch> trades) {
        Iterator<Map.Entry<BigDecimal, LinkedList<Order>>> bidsIterator = bids.entrySet().iterator();
        BigDecimal remainingAmountToSell = sellOrder.getAmount();
        while (bidsIterator.hasNext() && remainingAmountToSell.compareTo(BigDecimal.ZERO) > 0) {
            Map.Entry<BigDecimal, LinkedList<Order>> bidsLevel = bidsIterator.next();
            BigDecimal bidPrice = bidsLevel.getKey();
            if (sellOrder.getPrice().compareTo(bidPrice) > 0) break;
            LinkedList<Order> orderList = bidsLevel.getValue();
            while (!orderList.isEmpty() && remainingAmountToSell.compareTo(BigDecimal.ZERO) > 0) {
                // take buy order base qty what is available
                Order bid = orderList.peek();
                BigDecimal tradeQtyToConsume = remainingAmountToSell.min(bid.getAmount());
                remainingAmountToSell = remainingAmountToSell.subtract(tradeQtyToConsume);

                // produce trade ...
                trades.add(new TradeMatch(
                    bid.getUserId(),
                    sellOrder.getUserId(),
                    sellOrder.getInstrumentId(),
                    bid.getPrice(),
                    tradeQtyToConsume,
                    Side.SELL
                ));

                if (tradeQtyToConsume.equals(bid.getAmount())) {
                    orderList.poll(); // remove from book
                } else {
                    bid.changeAmount(bid.getAmount().subtract(tradeQtyToConsume)); // reduce quantity
                }

            }
            if (orderList.isEmpty()) bidsIterator.remove();
        }
        if (remainingAmountToSell.compareTo(BigDecimal.ZERO) > 0) {
            asks.computeIfAbsent(sellOrder.getPrice(), p -> new LinkedList<>()).add(sellOrder);
        }
    }


    //TODO: remove only for testing
    public OrderBookView getSnapshot(String symbol, int depth) {
        return new OrderBookView(
                symbol,
                collectLevels(asks, depth),
                collectLevels(bids, depth)
        );
    }

    private List<OrderBookView.LevelDto> collectLevels(
            NavigableMap<BigDecimal, LinkedList<Order>> levels,
            int depth
    ) {
        return levels.entrySet().stream()
                .limit(depth)
                .map(entry -> {
                    BigDecimal price = entry.getKey();
                    BigDecimal totalVolume = entry.getValue().stream()
                            .map(Order::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new OrderBookView.LevelDto(price, totalVolume);
                })
                .toList();
    }
}
