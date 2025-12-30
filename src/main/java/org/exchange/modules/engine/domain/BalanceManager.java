package org.exchange.modules.engine.domain;

import org.exchange.modules.engine.domain.model.Instrument;
import org.exchange.modules.engine.domain.model.Side;
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
    private final InstrumentCache instrumentCache;
    private static final Logger log = LoggerFactory.getLogger(BalanceManager.class);

    public BalanceManager(InstrumentCache instrumentCache) {
        this.instrumentCache = instrumentCache;
    }

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

    public void transfer(Long userId, Long instrumentId, BigDecimal baseAmount, BigDecimal quoteAmount, Side side) {
        Instrument instrument = instrumentCache.getById(instrumentId);
        if (instrument == null) {
            throw new IllegalArgumentException("Instrument not found");
        }

        Long quoteAssetId = instrument.getQuoteAssetId();
        Long baseAssetId = instrument.getBaseAssetId();

        // add base, consume quote
        if (side == Side.BUY) {
            log.info("Consume quote {} {} to {}", quoteAmount, instrumentId, userId);
            TradingBalance quoteBalance = getBalance(userId, quoteAssetId);
            quoteBalance.consumeLocked(quoteAmount);
            log.info("Add base: {} {} to {}", baseAmount, instrument.getSymbol(), userId);
            TradingBalance baseBalance = getBalance(userId, baseAssetId);
            baseBalance.deposit(baseAmount);
        } else {
            // add quote, consume base
            log.info("Consume base locked: {} {} to {}", baseAmount, instrument.getSymbol(), userId);
            TradingBalance baseBalance = getBalance(userId, baseAssetId);
            baseBalance.consumeLocked(baseAmount);
            log.info("Add quote: {} {} to {}", quoteAmount, instrument.getSymbol(), userId);
            TradingBalance quoteBalance = getBalance(userId, quoteAssetId);
            quoteBalance.deposit(quoteAmount);
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
