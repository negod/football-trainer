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

import se.backede.coachhub.application.dto.CreatePlayerRequest;
import se.backede.coachhub.application.dto.CreateTeamRequest;
import se.backede.coachhub.application.dto.PlayerResponse;
import se.backede.coachhub.application.dto.TeamResponse;
import se.backede.coachhub.application.dto.UpdatePlayerRequest;
import se.backede.coachhub.domain.model.CoachId;
import se.backede.coachhub.domain.model.GenderCategory;
import se.backede.coachhub.domain.model.Player;
import se.backede.coachhub.domain.model.PlayerId;
import se.backede.coachhub.domain.model.Team;
import se.backede.coachhub.domain.model.TeamId;
import se.backede.coachhub.domain.repository.PlayerRepositoryPort;
import se.backede.coachhub.domain.repository.TeamRepositoryPort;
import se.backede.coachhub.shared.exception.AccessDeniedException;
import se.backede.coachhub.shared.exception.ResourceNotFoundException;

class PlayerUseCaseServiceTest {

    private final InMemoryPlayerRepository playerRepository = new InMemoryPlayerRepository();
    private final InMemoryTeamRepository teamRepository = new InMemoryTeamRepository();
    private final TeamUseCaseService teamUseCase = new TeamUseCaseService(teamRepository);
    private final PlayerUseCaseService useCase = new PlayerUseCaseService(playerRepository, teamRepository);
    private final CoachId owner = new CoachId(UUID.randomUUID());
    private TeamId ownedTeamId;

    @BeforeEach
    void setUp() {
        playerRepository.clear();
        teamRepository.clear();
        TeamResponse team = teamUseCase.create(owner, new CreateTeamRequest("Team", 2015, GenderCategory.BOYS));
        ownedTeamId = new TeamId(team.id());
    }

    @Test
    void createsAPlayerOnAnOwnedTeam() {
        PlayerResponse response = useCase.create(owner, ownedTeamId,
                new CreatePlayerRequest("Alex Andersson", 2015, "Forward"));

        assertThat(response.name()).isEqualTo("Alex Andersson");
        assertThat(response.teamId()).isEqualTo(ownedTeamId.value().toString());
        assertThat(playerRepository.findAllByTeam(ownedTeamId)).hasSize(1);
    }

    @Test
    void deniesCreatingAPlayerOnATeamOwnedBySomeoneElse() {
        CoachId otherCoach = new CoachId(UUID.randomUUID());

        assertThatThrownBy(() -> useCase.create(otherCoach, ownedTeamId,
                new CreatePlayerRequest("Alex Andersson", 2015, null)))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(playerRepository.findAllByTeam(ownedTeamId)).isEmpty();
    }

    @Test
    void throwsNotFoundWhenCreatingOnAnUnknownTeam() {
        assertThatThrownBy(() -> useCase.create(owner, TeamId.newId(),
                new CreatePlayerRequest("Alex Andersson", 2015, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listsOnlyPlayersOnTheGivenTeam() {
        useCase.create(owner, ownedTeamId, new CreatePlayerRequest("Mine", 2015, null));
        TeamResponse otherTeam = teamUseCase.create(owner, new CreateTeamRequest("Other", 2014, GenderCategory.GIRLS));
        useCase.create(owner, new TeamId(otherTeam.id()), new CreatePlayerRequest("Not mine", 2014, null));

        List<PlayerResponse> players = useCase.list(owner, ownedTeamId);

        assertThat(players).hasSize(1);
        assertThat(players.get(0).name()).isEqualTo("Mine");
    }

    @Test
    void deniesListingPlayersOnATeamOwnedBySomeoneElse() {
        CoachId otherCoach = new CoachId(UUID.randomUUID());

        assertThatThrownBy(() -> useCase.list(otherCoach, ownedTeamId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getsAnOwnedPlayer() {
        PlayerResponse created = useCase.create(owner, ownedTeamId,
                new CreatePlayerRequest("Alex Andersson", 2015, "Forward"));

        PlayerResponse fetched = useCase.get(owner, ownedTeamId, new PlayerId(created.id()));

        assertThat(fetched).isEqualTo(created);
    }

    @Test
    void throwsNotFoundForAnUnknownPlayer() {
        assertThatThrownBy(() -> useCase.get(owner, ownedTeamId, PlayerId.newId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void throwsNotFoundWhenPlayerBelongsToADifferentTeam() {
        PlayerResponse created = useCase.create(owner, ownedTeamId,
                new CreatePlayerRequest("Alex Andersson", 2015, null));
        TeamResponse otherTeam = teamUseCase.create(owner, new CreateTeamRequest("Other", 2014, GenderCategory.GIRLS));

        assertThatThrownBy(() -> useCase.get(owner, new TeamId(otherTeam.id()), new PlayerId(created.id())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updatesAnOwnedPlayer() {
        PlayerResponse created = useCase.create(owner, ownedTeamId,
                new CreatePlayerRequest("Old", 2014, "Defender"));

        PlayerResponse updated = useCase.update(owner, ownedTeamId, new PlayerId(created.id()),
                new UpdatePlayerRequest("New", 2015, "Midfielder"));

        assertThat(updated.name()).isEqualTo("New");
        assertThat(updated.birthYear()).isEqualTo(2015);
        assertThat(updated.position()).isEqualTo("Midfielder");
    }

    @Test
    void deniesUpdatingAPlayerOnATeamOwnedBySomeoneElse() {
        PlayerResponse created = useCase.create(owner, ownedTeamId,
                new CreatePlayerRequest("Alex Andersson", 2015, null));
        CoachId otherCoach = new CoachId(UUID.randomUUID());

        assertThatThrownBy(() -> useCase.update(otherCoach, ownedTeamId, new PlayerId(created.id()),
                new UpdatePlayerRequest("New", 2015, null)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deletesAnOwnedPlayer() {
        PlayerResponse created = useCase.create(owner, ownedTeamId,
                new CreatePlayerRequest("Alex Andersson", 2015, null));

        useCase.delete(owner, ownedTeamId, new PlayerId(created.id()));

        assertThatThrownBy(() -> useCase.get(owner, ownedTeamId, new PlayerId(created.id())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deniesDeletingAPlayerOnATeamOwnedBySomeoneElse() {
        PlayerResponse created = useCase.create(owner, ownedTeamId,
                new CreatePlayerRequest("Alex Andersson", 2015, null));
        CoachId otherCoach = new CoachId(UUID.randomUUID());

        assertThatThrownBy(() -> useCase.delete(otherCoach, ownedTeamId, new PlayerId(created.id())))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(playerRepository.findAllByTeam(ownedTeamId)).hasSize(1);
    }

    private static final class InMemoryPlayerRepository implements PlayerRepositoryPort {

        private final Map<String, Player> players = new ConcurrentHashMap<>();

        @Override
        public Player save(Player player) {
            players.put(player.id().value(), player);
            return player;
        }

        @Override
        public Optional<Player> findById(PlayerId id) {
            return Optional.ofNullable(players.get(id.value()));
        }

        @Override
        public List<Player> findAllByTeam(TeamId teamId) {
            return players.values().stream()
                    .filter(player -> player.belongsToTeam(teamId))
                    .collect(Collectors.toList());
        }

        @Override
        public void deleteById(PlayerId id) {
            players.remove(id.value());
        }

        void clear() {
            players.clear();
        }
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
