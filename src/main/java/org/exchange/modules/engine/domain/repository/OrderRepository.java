package org.exchange.modules.engine.domain.repository;

import org.exchange.modules.engine.domain.model.Order;

import java.util.List;

/**
 * Repository interface for persisting orders to database.
 * Implementation should be optimized for batch inserts in HFT scenarios.
 */
public interface OrderRepository {
    
    /**
     * Save a single order to database.
     * For HFT, prefer using saveBatch() instead.
     */
    void save(Order order);
    
    /**
     * Batch save multiple orders in a single transaction.
     * This is the preferred method for HFT to minimize DB round-trips.
     * 
     * @param orders List of orders to persist
     * @return Number of successfully saved orders
     */
    int saveBatch(List<Order> orders);
}
