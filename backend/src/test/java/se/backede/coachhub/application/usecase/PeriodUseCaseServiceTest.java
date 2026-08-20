package se.backede.coachhub.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.backede.coachhub.application.dto.CreatePeriodRequest;
import se.backede.coachhub.application.dto.CreateTeamRequest;
import se.backede.coachhub.application.dto.PeriodResponse;
import se.backede.coachhub.application.dto.TeamResponse;
import se.backede.coachhub.application.dto.UpdatePeriodRequest;
import se.backede.coachhub.domain.model.CoachId;
import se.backede.coachhub.domain.model.GenderCategory;
import se.backede.coachhub.domain.model.MatchFormat;
import se.backede.coachhub.domain.model.Period;
import se.backede.coachhub.domain.model.PeriodId;
import se.backede.coachhub.domain.model.Team;
import se.backede.coachhub.domain.model.TeamId;
import se.backede.coachhub.domain.repository.PeriodRepositoryPort;
import se.backede.coachhub.domain.repository.TeamRepositoryPort;
import se.backede.coachhub.shared.exception.AccessDeniedException;
import se.backede.coachhub.shared.exception.ResourceNotFoundException;

class PeriodUseCaseServiceTest {

    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate END = LocalDate.of(2026, 6, 1);

    private final InMemoryPeriodRepository periodRepository = new InMemoryPeriodRepository();
    private final InMemoryTeamRepository teamRepository = new InMemoryTeamRepository();
    private final TeamUseCaseService teamUseCase = new TeamUseCaseService(teamRepository);
    private final PeriodUseCaseService useCase = new PeriodUseCaseService(periodRepository, teamRepository);
    private final CoachId owner = new CoachId(UUID.randomUUID());
    private TeamId ownedTeamId;

    @BeforeEach
    void setUp() {
        periodRepository.clear();
        teamRepository.clear();
        TeamResponse team = teamUseCase.create(owner, new CreateTeamRequest("Team", 2015, GenderCategory.BOYS));
        ownedTeamId = new TeamId(team.id());
    }

    @Test
    void createsAPeriodOnAnOwnedTeam() {
        PeriodResponse response = useCase.create(owner, ownedTeamId,
                new CreatePeriodRequest("Spring term", START, END, MatchFormat.SEVEN_V_SEVEN));

        assertThat(response.name()).isEqualTo("Spring term");
        assertThat(response.teamId()).isEqualTo(ownedTeamId.value());
        assertThat(periodRepository.findAllByTeam(ownedTeamId)).hasSize(1);
    }

    @Test
    void deniesCreatingAPeriodOnATeamOwnedBySomeoneElse() {
        CoachId otherCoach = new CoachId(UUID.randomUUID());

        assertThatThrownBy(() -> useCase.create(otherCoach, ownedTeamId,
                new CreatePeriodRequest("Spring term", START, END, MatchFormat.SEVEN_V_SEVEN)))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(periodRepository.findAllByTeam(ownedTeamId)).isEmpty();
    }

    @Test
    void throwsNotFoundWhenCreatingOnAnUnknownTeam() {
        assertThatThrownBy(() -> useCase.create(owner, TeamId.newId(),
                new CreatePeriodRequest("Spring term", START, END, MatchFormat.SEVEN_V_SEVEN)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listsOnlyPeriodsOnTheGivenTeam() {
        useCase.create(owner, ownedTeamId, new CreatePeriodRequest("Mine", START, END, MatchFormat.SEVEN_V_SEVEN));
        TeamResponse otherTeam = teamUseCase.create(owner, new CreateTeamRequest("Other", 2014, GenderCategory.GIRLS));
        useCase.create(owner, new TeamId(otherTeam.id()), new CreatePeriodRequest("Not mine", START, END, MatchFormat.SEVEN_V_SEVEN));

        List<PeriodResponse> periods = useCase.list(owner, ownedTeamId);

        assertThat(periods).hasSize(1);
        assertThat(periods.get(0).name()).isEqualTo("Mine");
    }

    @Test
    void deniesListingPeriodsOnATeamOwnedBySomeoneElse() {
        CoachId otherCoach = new CoachId(UUID.randomUUID());

        assertThatThrownBy(() -> useCase.list(otherCoach, ownedTeamId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getsAnOwnedPeriod() {
        PeriodResponse created = useCase.create(owner, ownedTeamId,
                new CreatePeriodRequest("Spring term", START, END, MatchFormat.SEVEN_V_SEVEN));

        PeriodResponse fetched = useCase.get(owner, ownedTeamId, new PeriodId(created.id()));

        assertThat(fetched).isEqualTo(created);
    }

    @Test
    void throwsNotFoundForAnUnknownPeriod() {
        assertThatThrownBy(() -> useCase.get(owner, ownedTeamId, PeriodId.newId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void throwsNotFoundWhenPeriodBelongsToADifferentTeam() {
        PeriodResponse created = useCase.create(owner, ownedTeamId,
                new CreatePeriodRequest("Spring term", START, END, MatchFormat.SEVEN_V_SEVEN));
        TeamResponse otherTeam = teamUseCase.create(owner, new CreateTeamRequest("Other", 2014, GenderCategory.GIRLS));

        assertThatThrownBy(() -> useCase.get(owner, new TeamId(otherTeam.id()), new PeriodId(created.id())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updatesAnOwnedPeriod() {
        PeriodResponse created = useCase.create(owner, ownedTeamId,
                new CreatePeriodRequest("Old", START, END, MatchFormat.FIVE_V_FIVE));

        PeriodResponse updated = useCase.update(owner, ownedTeamId, new PeriodId(created.id()),
                new UpdatePeriodRequest("New", START.plusDays(1), END.plusDays(1), MatchFormat.SEVEN_V_SEVEN));

        assertThat(updated.name()).isEqualTo("New");
        assertThat(updated.startDate()).isEqualTo(START.plusDays(1));
        assertThat(updated.endDate()).isEqualTo(END.plusDays(1));
        assertThat(updated.format()).isEqualTo(MatchFormat.SEVEN_V_SEVEN);
    }

    @Test
    void deniesUpdatingAPeriodOnATeamOwnedBySomeoneElse() {
        PeriodResponse created = useCase.create(owner, ownedTeamId,
                new CreatePeriodRequest("Spring term", START, END, MatchFormat.SEVEN_V_SEVEN));
        CoachId otherCoach = new CoachId(UUID.randomUUID());

        assertThatThrownBy(() -> useCase.update(otherCoach, ownedTeamId, new PeriodId(created.id()),
                new UpdatePeriodRequest("New", START, END, MatchFormat.SEVEN_V_SEVEN)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void suggestsAFormatFromTheTeamsBirthYear() {
        MatchFormat suggested = useCase.suggestFormat(owner, ownedTeamId);

        assertThat(suggested).isEqualTo(MatchFormat.suggestedFor(2015));
    }

    @Test
    void deniesSuggestingAFormatForATeamOwnedBySomeoneElse() {
        CoachId otherCoach = new CoachId(UUID.randomUUID());

        assertThatThrownBy(() -> useCase.suggestFormat(otherCoach, ownedTeamId))
                .isInstanceOf(AccessDeniedException.class);
    }

    private static final class InMemoryPeriodRepository implements PeriodRepositoryPort {

        private final Map<UUID, Period> periods = new ConcurrentHashMap<>();

        @Override
        public Period save(Period period) {
            periods.put(period.id().value(), period);
            return period;
        }

        @Override
        public Optional<Period> findById(PeriodId id) {
            return Optional.ofNullable(periods.get(id.value()));
        }

        @Override
        public List<Period> findAllByTeam(TeamId teamId) {
            return periods.values().stream()
                    .filter(period -> period.belongsToTeam(teamId))
                    .collect(Collectors.toList());
        }

        void clear() {
            periods.clear();
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
