package se.backede.coachhub.domain.repository;

import java.util.List;
import java.util.Optional;

import se.backede.coachhub.domain.model.CoachId;
import se.backede.coachhub.domain.model.Team;
import se.backede.coachhub.domain.model.TeamId;

public interface TeamRepositoryPort {

    Team save(Team team);

    Optional<Team> findById(TeamId id);

    List<Team> findAllByOwner(CoachId ownerId);

    void deleteById(TeamId id);
}
