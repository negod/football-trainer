package se.backede.coachhub.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.backede.coachhub.application.dto.CreateTeamRequest;
import se.backede.coachhub.application.dto.TeamResponse;
import se.backede.coachhub.application.dto.UpdateTeamRequest;
import se.backede.coachhub.domain.model.CoachId;
import se.backede.coachhub.domain.model.GenderCategory;
import se.backede.coachhub.domain.model.Team;
import se.backede.coachhub.domain.model.TeamId;
import se.backede.coachhub.domain.repository.TeamRepositoryPort;
import se.backede.coachhub.shared.exception.AccessDeniedException;
import se.backede.coachhub.shared.exception.ResourceNotFoundException;

class TeamUseCaseServiceTest {

    private final InMemoryTeamRepository repository = new InMemoryTeamRepository();
    private final TeamUseCaseService useCase = new TeamUseCaseService(repository);
    private final CoachId owner = new CoachId(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        repository.clear();
    }

    @Test
    void createsATeamOwnedByTheRequester() {
        TeamResponse response = useCase.create(owner, new CreateTeamRequest("Team", 2019, GenderCategory.BOYS));

        assertThat(response.name()).isEqualTo("Team");
        assertThat(response.shorthand()).isEqualTo("P19");
        assertThat(repository.findAllByOwner(owner)).hasSize(1);
    }

    @Test
    void listsOnlyTeamsOwnedByTheRequester() {
        useCase.create(owner, new CreateTeamRequest("Mine", 2019, GenderCategory.BOYS));
        CoachId otherCoach = new CoachId(UUID.randomUUID());
        useCase.create(otherCoach, new CreateTeamRequest("Not mine", 2018, GenderCategory.GIRLS));

        List<TeamResponse> teams = useCase.list(owner);

        assertThat(teams).hasSize(1);
        assertThat(teams.get(0).name()).isEqualTo("Mine");
    }

    @Test
    void getsAnOwnedTeam() {
        TeamResponse created = useCase.create(owner, new CreateTeamRequest("Team", 2019, GenderCategory.BOYS));

        TeamResponse fetched = useCase.get(owner, new TeamId(created.id()));

        assertThat(fetched).isEqualTo(created);
    }

    @Test
    void throwsNotFoundForAnUnknownTeam() {
        assertThatThrownBy(() -> useCase.get(owner, TeamId.newId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deniesAccessToATeamOwnedBySomeoneElse() {
        TeamResponse created = useCase.create(owner, new CreateTeamRequest("Team", 2019, GenderCategory.BOYS));
        CoachId otherCoach = new CoachId(UUID.randomUUID());

        assertThatThrownBy(() -> useCase.get(otherCoach, new TeamId(created.id())))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void updatesAnOwnedTeam() {
        TeamResponse created = useCase.create(owner, new CreateTeamRequest("Old", 2018, GenderCategory.GIRLS));

        TeamResponse updated = useCase.update(owner, new TeamId(created.id()),
                new UpdateTeamRequest("New", 2019, GenderCategory.MIXED));

        assertThat(updated.name()).isEqualTo("New");
        assertThat(updated.birthYear()).isEqualTo(2019);
        assertThat(updated.genderCategory()).isEqualTo(GenderCategory.MIXED);
    }

    @Test
    void deniesUpdatingATeamOwnedBySomeoneElse() {
        TeamResponse created = useCase.create(owner, new CreateTeamRequest("Team", 2019, GenderCategory.BOYS));
        CoachId otherCoach = new CoachId(UUID.randomUUID());

        assertThatThrownBy(() -> useCase.update(otherCoach, new TeamId(created.id()),
                new UpdateTeamRequest("New", 2019, GenderCategory.BOYS)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deletesAnOwnedTeam() {
        TeamResponse created = useCase.create(owner, new CreateTeamRequest("Team", 2019, GenderCategory.BOYS));

        useCase.delete(owner, new TeamId(created.id()));

        assertThatThrownBy(() -> useCase.get(owner, new TeamId(created.id())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deniesDeletingATeamOwnedBySomeoneElse() {
        TeamResponse created = useCase.create(owner, new CreateTeamRequest("Team", 2019, GenderCategory.BOYS));
        CoachId otherCoach = new CoachId(UUID.randomUUID());

        assertThatThrownBy(() -> useCase.delete(otherCoach, new TeamId(created.id())))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(repository.findAllByOwner(owner)).hasSize(1);
    }

    private static final class InMemoryTeamRepository implements TeamRepositoryPort {

        private final Map<UUID, Team> teams = new ConcurrentHashMap<>();

        @Override
        public Team save(Team team) {
            teams.put(team.id().value(), team);
            return team;
        }

        @Override
        public Optional<Team> findById(TeamId id) {
            return Optional.ofNullable(teams.get(id.value()));
        }

        @Override
        public List<Team> findAllByOwner(CoachId ownerId) {
            return teams.values().stream()
                    .filter(team -> team.isOwnedBy(ownerId))
                    .collect(Collectors.toList());
        }

        @Override
        public void deleteById(TeamId id) {
            teams.remove(id.value());
        }

        void clear() {
            teams.clear();
        }
    }
}
