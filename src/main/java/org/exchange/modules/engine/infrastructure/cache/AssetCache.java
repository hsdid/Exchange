package org.exchange.modules.engine.infrastructure.cache;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.exchange.modules.engine.domain.model.Asset;
import org.exchange.modules.engine.domain.repository.AssetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AssetCache {
    private final Logger log = LoggerFactory.getLogger(AssetCache.class);
    private final AssetRepository assetRepository;
    private final Map<String, Asset> assetCache = new ConcurrentHashMap<>();

    public AssetCache(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @PostConstruct
    public void init() {
        List<Asset> assets = assetRepository.findAll();

        assetCache.clear();

        for (Asset asset : assets) {
            assetCache.put(asset.getSymbol(), asset);
        }

        log.info("AssetCache loaded {} assets", assetCache.size());
    }

    public Optional<Asset> getAssetBySymbol(String symbol) {
        return Optional.ofNullable(assetCache.get(symbol));
    }

    public Long getAssetId(String symbol) {
        return assetCache.get(symbol).getId();
    }

    @PreDestroy
    public void destroy() {
        assetCache.clear();
    }
}
