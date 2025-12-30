package org.exchange.modules.engine.infrastructure.db.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.exchange.modules.engine.domain.entity.AssetEntity;
import org.exchange.modules.engine.domain.entity.InstrumentEntity;
import org.exchange.modules.engine.domain.model.Asset;
import org.exchange.modules.engine.domain.model.Instrument;
import org.exchange.modules.engine.domain.repository.AssetRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JpaAssetRepository implements AssetRepository {

    @PersistenceContext
    private EntityManager em;
    @Override
    @Transactional(readOnly = true)
    public List<Asset> findAll() {
        TypedQuery<AssetEntity> query = em.createQuery(
                "SELECT i FROM AssetEntity i ORDER BY i.symbol",
                AssetEntity.class
        );

        return query.getResultStream()
                .map(AssetEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Asset> findById(Long id) {
        AssetEntity assetEntity = em.find(AssetEntity.class, id);
        return Optional.ofNullable(assetEntity).map(AssetEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Asset> findBySymbol(String symbol) {
        AssetEntity assetEntity = em.find(AssetEntity.class, symbol);
        return Optional.ofNullable(assetEntity).map(AssetEntity::toDomain);
    }
}
