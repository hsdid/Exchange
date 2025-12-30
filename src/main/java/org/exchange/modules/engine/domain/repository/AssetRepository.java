package org.exchange.modules.engine.domain.repository;

import org.exchange.modules.engine.domain.model.Asset;

import java.util.List;
import java.util.Optional;

public interface AssetRepository {
    List<Asset> findAll();
    Optional<Asset> findById(Long id);
    Optional<Asset> findBySymbol(String symbol);
}
