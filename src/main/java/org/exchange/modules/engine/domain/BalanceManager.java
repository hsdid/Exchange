package org.exchange.modules.engine.domain;

import org.exchange.modules.engine.domain.model.Instrument;
import org.exchange.modules.engine.domain.model.Side;
import org.exchange.modules.engine.domain.model.TradeMatch;
import org.exchange.modules.engine.domain.model.TradingBalance;
import org.exchange.modules.engine.infrastructure.cache.InstrumentCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


@Component
public class BalanceManager {
    // userId -> assetId -> balance
    private final Map<Long, Map<Long, TradingBalance>> balances = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(BalanceManager.class);

    public TradingBalance getBalance(Long userId, Long assetId) {
        return balances
                .computeIfAbsent(userId, k -> new HashMap<>())
                .computeIfAbsent(assetId, k -> new TradingBalance());
    }

    public void deposit(Long userId, Long assetId, BigDecimal amount) {
        getBalance(userId, assetId).deposit(amount);
    }

    public boolean tryLockFunds(Long userId, Long assetId, BigDecimal amount) {
        TradingBalance balance = getBalance(userId, assetId);
        if (balance.getAvailable().compareTo(amount) < 0) {
            return false;
        }
        balance.lock(amount);
        return true;
    }

    public void processTrade(TradeMatch trade, Instrument instrument) {
        Long baseAssetId = instrument.getBaseAssetId();
        Long quoteAssetId = instrument.getQuoteAssetId();

        BigDecimal baseAmount = trade.quantity();
        BigDecimal quoteAmount = trade.quantity().multiply(trade.price());

        if (trade.takerSide() == Side.BUY) {
            // Taker (Buyer) Settlement
            // Taker buy base, sell quote
            TradingBalance takerQuote = getBalance(trade.takerUserId(), quoteAssetId);
            TradingBalance takerBase = getBalance(trade.takerUserId(), baseAssetId);
            takerQuote.consumeLocked(quoteAmount);
            takerBase.deposit(baseAmount);

            // Maker settlement
            // Maker sell base, buy quote
            TradingBalance makerQuote = getBalance(trade.makerUserId(), quoteAssetId);
            TradingBalance makerBase = getBalance(trade.makerUserId(), baseAssetId);
            makerQuote.deposit(quoteAmount);
            makerBase.consumeLocked(baseAmount);
        } else {
            // Taker (Seller) Settlement
            // Taker sell base, buy quote
            TradingBalance takerQuote = getBalance(trade.takerUserId(), quoteAssetId);
            TradingBalance takerBase = getBalance(trade.takerUserId(), baseAssetId);
            takerQuote.deposit(quoteAmount);
            takerBase.consumeLocked(baseAmount);

            // Maker settlement
            // Maker buy base, sell quote
            TradingBalance makerQuote = getBalance(trade.makerUserId(), quoteAssetId);
            TradingBalance makerBase = getBalance(trade.makerUserId(), baseAssetId);
            makerQuote.consumeLocked(quoteAmount);
            makerBase.deposit(baseAmount);
        }
    }

    //TODO: remove only for testing
    public void getBalances() {
        this.balances.forEach((userId, assetBalances) -> {
            assetBalances.forEach((assetId, balance) -> {
                System.out.println("User: " + userId + " Asset: " + assetId + " Balance:  available:" + balance.getAvailable() + " locked:" + balance.getLocked());
            });
        });
    }
}
