package org.exchange.modules.engine.infrastructure.db.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.exchange.modules.engine.domain.entity.InstrumentEntity;
import org.exchange.modules.engine.domain.model.Instrument;
import org.exchange.modules.engine.domain.model.InstrumentStatus;
import org.exchange.modules.engine.domain.repository.InstrumentRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of InstrumentRepository.
 */
@Repository
public class JpaInstrumentRepository implements InstrumentRepository {
    
    @PersistenceContext
    private EntityManager em;
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Instrument> findById(Long id) {
        InstrumentEntity entity = em.find(InstrumentEntity.class, id);
        return Optional.ofNullable(entity).map(InstrumentEntity::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Instrument> findBySymbol(String symbol) {
        TypedQuery<InstrumentEntity> query = em.createQuery(
            "SELECT i FROM InstrumentEntity i WHERE i.symbol = :symbol",
            InstrumentEntity.class
        );
        query.setParameter("symbol", symbol.toUpperCase());
        
        return query.getResultStream()
            .findFirst()
            .map(InstrumentEntity::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Instrument> findAll() {
        TypedQuery<InstrumentEntity> query = em.createQuery(
            "SELECT i FROM InstrumentEntity i ORDER BY i.symbol",
            InstrumentEntity.class
        );
        
        return query.getResultStream()
            .map(InstrumentEntity::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Instrument> findAllActive() {
        TypedQuery<InstrumentEntity> query = em.createQuery(
            "SELECT i FROM InstrumentEntity i WHERE i.status = :status ORDER BY i.symbol",
            InstrumentEntity.class
        );
        query.setParameter("status", InstrumentStatus.ACTIVE);
        
        return query.getResultStream()
            .map(InstrumentEntity::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Instrument save(Instrument instrument) {
        InstrumentEntity entity;
        
        if (instrument.getId() == null) {
            // New instrument
            entity = new InstrumentEntity(
                instrument.getSymbol(),
                instrument.getName(),
                instrument.getBaseAssetId(),
                instrument.getQuoteAssetId(),
                instrument.getPrecision(),
                instrument.getMinAmount(),
                instrument.getTickSize(),
                instrument.getStatus()
            );
            em.persist(entity);
        } else {
            // Update existing
            entity = em.find(InstrumentEntity.class, instrument.getId());
            if (entity == null) {
                throw new IllegalArgumentException("Instrument not found: " + instrument.getId());
            }
            entity.setName(instrument.getName());
            entity.setPrecision(instrument.getPrecision());
            entity.setMinAmount(instrument.getMinAmount());
            entity.setTickSize(instrument.getTickSize());
            entity.setStatus(instrument.getStatus());
            em.merge(entity);
        }
        
        em.flush();
        return entity.toDomain();
    }
}
