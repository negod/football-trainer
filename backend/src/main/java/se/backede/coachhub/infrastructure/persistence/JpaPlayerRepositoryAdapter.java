package se.backede.coachhub.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import se.backede.coachhub.domain.model.Player;
import se.backede.coachhub.domain.model.PlayerId;
import se.backede.coachhub.domain.model.TeamId;
import se.backede.coachhub.domain.repository.PlayerRepositoryPort;

@Component
public class JpaPlayerRepositoryAdapter implements PlayerRepositoryPort {

    private final SpringDataPlayerRepository springDataPlayerRepository;

    public JpaPlayerRepositoryAdapter(SpringDataPlayerRepository springDataPlayerRepository) {
        this.springDataPlayerRepository = springDataPlayerRepository;
    }

    @Override
    public Player save(Player player) {
        PlayerEntity saved = springDataPlayerRepository.save(toEntity(player));
        return toDomain(saved);
    }

    @Override
    public Optional<Player> findById(PlayerId id) {
        return springDataPlayerRepository.findById(id.value()).map(JpaPlayerRepositoryAdapter::toDomain);
    }

    @Override
    public List<Player> findAllByTeam(TeamId teamId) {
        return springDataPlayerRepository.findAllByTeamId(teamId.value()).stream()
                .map(JpaPlayerRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public void deleteById(PlayerId id) {
        springDataPlayerRepository.deleteById(id.value());
    }

    private static PlayerEntity toEntity(Player player) {
        return new PlayerEntity(
                player.id().value(),
                player.teamId().value(),
                player.name(),
                player.birthYear(),
                player.position()
        );
    }

    private static Player toDomain(PlayerEntity entity) {
        return new Player(
                new PlayerId(entity.getId()),
                new TeamId(entity.getTeamId()),
                entity.getName(),
                entity.getBirthYear(),
                entity.getPosition()
        );
    }
}
