package se.backede.coachhub.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPlayerRepository extends JpaRepository<PlayerEntity, String> {

    List<PlayerEntity> findAllByTeamId(UUID teamId);
}
