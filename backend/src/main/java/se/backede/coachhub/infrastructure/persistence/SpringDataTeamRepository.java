package se.backede.coachhub.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTeamRepository extends JpaRepository<TeamEntity, UUID> {

    List<TeamEntity> findAllByOwnerId(UUID ownerId);
}
