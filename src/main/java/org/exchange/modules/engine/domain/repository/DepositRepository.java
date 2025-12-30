package org.exchange.modules.engine.domain.repository;

import org.exchange.modules.engine.domain.model.Deposit;

import java.util.List;

public interface DepositRepository {
    void save(Deposit deposit);
    void saveBatch(List<Deposit> deposits);
}
