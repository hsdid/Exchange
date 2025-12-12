package org.exchange.modules.engine.infrastructure.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.exchange.modules.engine.domain.entity.OrderEntity;
import org.exchange.modules.engine.domain.model.Order;
import org.exchange.modules.engine.domain.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * High-performance JPA implementation of OrderRepository.
 * Optimized for HFT with batch inserts and minimal overhead.
 */
@Repository
public class JpaOrderRepository implements OrderRepository {
    
    private static final Logger log = LoggerFactory.getLogger(JpaOrderRepository.class);
    private static final int BATCH_FLUSH_SIZE = 50;
    
    @PersistenceContext
    private EntityManager em;
    
    @Override
    @Transactional
    public void save(Order order) {
        OrderEntity entity = OrderEntity.fromDomain(order);
        em.persist(entity);
    }
    
    @Override
    @Transactional
    public int saveBatch(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return 0;
        }

        int count = 0;
        
        try {
            for (Order order : orders) {
                OrderEntity entity = OrderEntity.fromDomain(order);
                em.persist(entity);
                count++;

                if (count % BATCH_FLUSH_SIZE == 0) {
                    em.flush();
                    em.clear();
                }
            }

            em.flush();
            em.clear();

            return count;
            
        } catch (Exception e) {
            log.error("Failed to batch save orders", e);
            throw e;
        }
    }
}
