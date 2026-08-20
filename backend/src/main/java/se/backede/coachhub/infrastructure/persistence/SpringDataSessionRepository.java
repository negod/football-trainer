package se.backede.coachhub.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSessionRepository extends JpaRepository<SessionEntity, UUID> {

    List<SessionEntity> findAllByPeriodId(UUID periodId);
}
