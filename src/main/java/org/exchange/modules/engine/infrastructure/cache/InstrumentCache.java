package org.exchange.modules.engine.infrastructure.cache;

import jakarta.annotation.PostConstruct;
import org.exchange.modules.engine.domain.model.Instrument;
import org.exchange.modules.engine.domain.repository.InstrumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache for Instrument lookups.
 * Critical for HFT - avoids DB queries on every order.
 * 
 * Usage:
 *   Long instrumentId = cache.getIdBySymbol("BTC");
 *   Instrument instrument = cache.getBySymbol("BTC");
 */
@Component
public class InstrumentCache {
    
    private final Logger log = LoggerFactory.getLogger(InstrumentCache.class);
    
    private final InstrumentRepository repository;
    private final Map<String, Instrument> bySymbol = new ConcurrentHashMap<>();
    private final Map<Long, Instrument> byId = new ConcurrentHashMap<>();
    
    public InstrumentCache(InstrumentRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        List<Instrument> instruments = repository.findAll();

        bySymbol.clear();
        byId.clear();

        for (Instrument instrument : instruments) {
            bySymbol.put(instrument.getSymbol(), instrument);
            byId.put(instrument.getId(), instrument);
        }

        log.info("InstrumentCache loaded {} instruments", instruments.size());
    }

    public Long getIdBySymbol(String symbol) {
        Instrument instrument = bySymbol.get(symbol.toUpperCase());
        return instrument != null ? instrument.getId() : null;
    }

    public Instrument getBySymbol(String symbol) {
        return bySymbol.get(symbol.toUpperCase());
    }

    public Instrument getById(Long id) {
        return byId.get(id);
    }

    public String getSymbolById(Long id) {
        Instrument instrument = byId.get(id);
        return instrument != null ? instrument.getSymbol() : null;
    }

    public boolean isActive(String symbol) {
        Instrument instrument = bySymbol.get(symbol.toUpperCase());
        return instrument != null && instrument.isActive();
    }

    public List<Instrument> getAll() {
        return List.copyOf(bySymbol.values());
    }

    public List<Instrument> getAllActive() {
        return bySymbol.values().stream()
            .filter(Instrument::isActive)
            .toList();
    }
}
