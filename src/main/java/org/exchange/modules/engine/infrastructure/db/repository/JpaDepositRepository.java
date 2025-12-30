package org.exchange.modules.engine.infrastructure.db.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.exchange.modules.engine.domain.entity.DepositEntity;
import org.exchange.modules.engine.domain.model.Deposit;
import org.exchange.modules.engine.domain.repository.DepositRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class JpaDepositRepository implements DepositRepository {
    private static final int BATCH_FLUSH_SIZE = 50;

    @PersistenceContext
    private EntityManager em;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Deposit deposit) {
        DepositEntity depositEntity = DepositEntity.fromDeposit(deposit);
        em.persist(depositEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatch(List<Deposit> deposits) {
        if (deposits.isEmpty()) {
            return;
        }
        int count = 0;
        try {
            for (Deposit deposit : deposits) {
                DepositEntity depositEntity = DepositEntity.fromDeposit(deposit);
                em.persist(depositEntity);
                count++;

                if (deposits.indexOf(deposit) % BATCH_FLUSH_SIZE == 0) {
                    em.flush();
                    em.clear();
                }
            }

            em.flush();
            em.clear();
        } catch (Exception e) {
            throw e;
        }
    }
}
