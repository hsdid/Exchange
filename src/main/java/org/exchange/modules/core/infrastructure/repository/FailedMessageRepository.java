package org.exchange.modules.core.infrastructure.repository;

import org.exchange.modules.core.domain.entity.FailedMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FailedMessageRepository extends JpaRepository<FailedMessageEntity, Long> {

    List<FailedMessageEntity> findByProcessedFalse();

    List<FailedMessageEntity> findBySourceQueue(String sourceQueue);
}
