package org.exchange.modules.engine.domain.repository;

import org.exchange.modules.engine.domain.model.Instrument;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Instrument operations.
 */
public interface InstrumentRepository {
    Optional<Instrument> findById(Long id);

    Optional<Instrument> findBySymbol(String symbol);

    List<Instrument> findAll();

    List<Instrument> findAllActive();

    Instrument save(Instrument instrument);
}
