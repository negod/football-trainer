package se.backede.coachhub.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPeriodRepository extends JpaRepository<PeriodEntity, UUID> {

    List<PeriodEntity> findAllByTeamId(UUID teamId);
}
