package se.backede.coachhub.domain.repository;

import java.util.List;
import java.util.Optional;

import se.backede.coachhub.domain.model.Player;
import se.backede.coachhub.domain.model.PlayerId;
import se.backede.coachhub.domain.model.TeamId;

public interface PlayerRepositoryPort {

    Player save(Player player);

    Optional<Player> findById(PlayerId id);

    List<Player> findAllByTeam(TeamId teamId);

    void deleteById(PlayerId id);
}
