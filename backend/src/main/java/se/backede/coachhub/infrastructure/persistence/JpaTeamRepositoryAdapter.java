package se.backede.coachhub.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import se.backede.coachhub.domain.model.CoachId;
import se.backede.coachhub.domain.model.Team;
import se.backede.coachhub.domain.model.TeamId;
import se.backede.coachhub.domain.repository.TeamRepositoryPort;

@Component
public class JpaTeamRepositoryAdapter implements TeamRepositoryPort {

    private final SpringDataTeamRepository springDataTeamRepository;

    public JpaTeamRepositoryAdapter(SpringDataTeamRepository springDataTeamRepository) {
        this.springDataTeamRepository = springDataTeamRepository;
    }

    @Override
    public Team save(Team team) {
        TeamEntity saved = springDataTeamRepository.save(toEntity(team));
        return toDomain(saved);
    }

    @Override
    public Optional<Team> findById(TeamId id) {
        return springDataTeamRepository.findById(id.value()).map(JpaTeamRepositoryAdapter::toDomain);
    }

    @Override
    public List<Team> findAllByOwner(CoachId ownerId) {
        return springDataTeamRepository.findAllByOwnerId(ownerId.value()).stream()
                .map(JpaTeamRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public void deleteById(TeamId id) {
        springDataTeamRepository.deleteById(id.value());
    }

    private static TeamEntity toEntity(Team team) {
        return new TeamEntity(
                team.id().value(),
                team.ownerId().value(),
                team.name(),
                team.birthYear(),
                team.genderCategory()
        );
    }

    private static Team toDomain(TeamEntity entity) {
        return new Team(
                new TeamId(entity.getId()),
                new CoachId(entity.getOwnerId()),
                entity.getName(),
                entity.getBirthYear(),
                entity.getGenderCategory()
        );
    }
}
