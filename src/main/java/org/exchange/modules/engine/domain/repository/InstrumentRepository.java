package org.exchange.modules.engine.domain.repository;

import org.exchange.modules.engine.domain.model.Instrument;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Instrument operations.
 */
public interface InstrumentRepository {
    
    /**
     * Find instrument by ID.
     */
    Optional<Instrument> findById(Long id);
    
    /**
     * Find instrument by symbol (e.g., "BTC", "ETH").
     */
    Optional<Instrument> findBySymbol(String symbol);
    
    /**
     * Get all instruments.
     */
    List<Instrument> findAll();
    
    /**
     * Get all active instruments (tradable).
     */
    List<Instrument> findAllActive();
    
    /**
     * Save or update instrument.
     */
    Instrument save(Instrument instrument);
}
