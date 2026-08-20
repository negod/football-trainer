package se.backede.coachhub.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.backede.coachhub.application.dto.CreatePeriodRequest;
import se.backede.coachhub.application.dto.CreateTeamRequest;
import se.backede.coachhub.application.dto.GenerateSessionsRequest;
import se.backede.coachhub.application.dto.PeriodResponse;
import se.backede.coachhub.application.dto.SessionResponse;
import se.backede.coachhub.application.dto.TeamResponse;
import se.backede.coachhub.application.dto.UpdateSessionRequest;
import se.backede.coachhub.domain.model.CoachId;
import se.backede.coachhub.domain.model.GenderCategory;
import se.backede.coachhub.domain.model.MatchFormat;
import se.backede.coachhub.domain.model.Period;
import se.backede.coachhub.domain.model.PeriodId;
import se.backede.coachhub.domain.model.Session;
import se.backede.coachhub.domain.model.SessionId;
import se.backede.coachhub.domain.model.SessionStatus;
import se.backede.coachhub.domain.model.Team;
import se.backede.coachhub.domain.model.TeamId;
import se.backede.coachhub.domain.repository.PeriodRepositoryPort;
import se.backede.coachhub.domain.repository.SessionRepositoryPort;
import se.backede.coachhub.domain.repository.TeamRepositoryPort;
import se.backede.coachhub.shared.exception.AccessDeniedException;
import se.backede.coachhub.shared.exception.ResourceNotFoundException;

class SessionUseCaseServiceTest {

    private static final LocalDate START = LocalDate.of(2026, 1, 1); // a Thursday
    private static final LocalDate END = LocalDate.of(2026, 1, 31);

    private final InMemorySessionRepository sessionRepository = new InMemorySessionRepository();
    private final InMemoryPeriodRepository periodRepository = new InMemoryPeriodRepository();
    private final InMemoryTeamRepository teamRepository = new InMemoryTeamRepository();
    private final TeamUseCaseService teamUseCase = new TeamUseCaseService(teamRepository);
    private final PeriodUseCaseService periodUseCase = new PeriodUseCaseService(periodRepository, teamRepository);
    private final SessionUseCaseService useCase = new SessionUseCaseService(sessionRepository, periodRepository, teamRepository);
    private final CoachId owner = new CoachId(UUID.randomUUID());
    private TeamId ownedTeamId;
    private PeriodId ownedPeriodId;

    @BeforeEach
    void setUp() {
        sessionRepository.clear();
        periodRepository.clear();
        teamRepository.clear();
        TeamResponse team = teamUseCase.create(owner, new CreateTeamRequest("Team", 2015, GenderCategory.BOYS));
        ownedTeamId = new TeamId(team.id());
        PeriodResponse period = periodUseCase.create(owner, ownedTeamId,
                new CreatePeriodRequest("Winter term", START, END, MatchFormat.SEVEN_V_SEVEN));
        ownedPeriodId = new PeriodId(period.id());
    }

    @Test
    void generatesASessionForEveryMatchingWeekdayInRange() {
        List<SessionResponse> sessions = useCase.generate(owner, ownedTeamId, ownedPeriodId,
                new GenerateSessionsRequest(Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)));

        assertThat(sessions).allMatch(s -> s.date().getDayOfWeek() == DayOfWeek.TUESDAY
                || s.date().getDayOfWeek() == DayOfWeek.THURSDAY);
        assertThat(sessions).allMatch(s -> !s.date().isBefore(START) && !s.date().isAfter(END));
        assertThat(sessions).isNotEmpty();
    }

    @Test
    void runningGenerationAgainUnchangedCreatesNoDuplicates() {
        List<SessionResponse> first = useCase.generate(owner, ownedTeamId, ownedPeriodId,
                new GenerateSessionsRequest(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)));

        List<SessionResponse> second = useCase.generate(owner, ownedTeamId, ownedPeriodId,
                new GenerateSessionsRequest(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)));

        assertThat(second).hasSameSizeAs(first);
        Set<LocalDate> distinctDates = second.stream().map(SessionResponse::date).collect(Collectors.toSet());
        assertThat(distinctDates).hasSameSizeAs(second);
    }

    @Test
    void generatingAgainWithAdditionalWeekdaysOnlyAddsTheNewDates() {
        List<SessionResponse> first = useCase.generate(owner, ownedTeamId, ownedPeriodId,
                new GenerateSessionsRequest(Set.of(DayOfWeek.MONDAY)));

        List<SessionResponse> second = useCase.generate(owner, ownedTeamId, ownedPeriodId,
                new GenerateSessionsRequest(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)));

        assertThat(second.size()).isGreaterThan(first.size());
        assertThat(second).containsAll(first);
    }

    @Test
    void deniesGenerationForATeamOwnedBySomeoneElse() {
        CoachId otherCoach = new CoachId(UUID.randomUUID());

        assertThatThrownBy(() -> useCase.generate(otherCoach, ownedTeamId, ownedPeriodId,
                new GenerateSessionsRequest(Set.of(DayOfWeek.MONDAY))))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void throwsNotFoundWhenGeneratingForAnUnknownPeriod() {
        assertThatThrownBy(() -> useCase.generate(owner, ownedTeamId, PeriodId.newId(),
                new GenerateSessionsRequest(Set.of(DayOfWeek.MONDAY))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void throwsNotFoundWhenPeriodBelongsToADifferentTeam() {
        TeamResponse otherTeam = teamUseCase.create(owner, new CreateTeamRequest("Other", 2014, GenderCategory.GIRLS));

        assertThatThrownBy(() -> useCase.generate(owner, new TeamId(otherTeam.id()), ownedPeriodId,
                new GenerateSessionsRequest(Set.of(DayOfWeek.MONDAY))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listsSessionsSortedByDate() {
        useCase.generate(owner, ownedTeamId, ownedPeriodId, new GenerateSessionsRequest(Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)));

        List<SessionResponse> sessions = useCase.list(owner, ownedTeamId, ownedPeriodId);

        assertThat(sessions).isSortedAccordingTo((a, b) -> a.date().compareTo(b.date()));
    }

    @Test
    void deniesListingForATeamOwnedBySomeoneElse() {
        CoachId otherCoach = new CoachId(UUID.randomUUID());

        assertThatThrownBy(() -> useCase.list(otherCoach, ownedTeamId, ownedPeriodId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void skipsASession() {
        SessionId sessionId = generateOneSession();

        SessionResponse updated = useCase.update(owner, ownedTeamId, ownedPeriodId, sessionId,
                new UpdateSessionRequest(SessionStatus.SKIPPED, null));

        assertThat(updated.status()).isEqualTo(SessionStatus.SKIPPED);
    }

    @Test
    void restoresASkippedSession() {
        SessionId sessionId = generateOneSession();
        useCase.update(owner, ownedTeamId, ownedPeriodId, sessionId, new UpdateSessionRequest(SessionStatus.SKIPPED, null));

        SessionResponse restored = useCase.update(owner, ownedTeamId, ownedPeriodId, sessionId,
                new UpdateSessionRequest(SessionStatus.SCHEDULED, null));

        assertThat(restored.status()).isEqualTo(SessionStatus.SCHEDULED);
    }

    @Test
    void reschedulesASessionWithoutAffectingOthers() {
        useCase.generate(owner, ownedTeamId, ownedPeriodId, new GenerateSessionsRequest(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)));
        List<SessionResponse> before = useCase.list(owner, ownedTeamId, ownedPeriodId);
        SessionResponse target = before.get(0);
        LocalDate newDate = target.date().plusDays(2);

        SessionResponse updated = useCase.update(owner, ownedTeamId, ownedPeriodId, new SessionId(target.id()),
                new UpdateSessionRequest(null, newDate));

        assertThat(updated.date()).isEqualTo(newDate);
        List<SessionResponse> after = useCase.list(owner, ownedTeamId, ownedPeriodId);
        assertThat(after).hasSameSizeAs(before);
        List<SessionResponse> untouched = after.stream().filter(s -> !s.id().equals(target.id())).toList();
        List<SessionResponse> untouchedBefore = before.stream().filter(s -> !s.id().equals(target.id())).toList();
        assertThat(untouched).containsExactlyInAnyOrderElementsOf(untouchedBefore);
    }

    @Test
    void deniesUpdatingASessionForATeamOwnedBySomeoneElse() {
        SessionId sessionId = generateOneSession();
        CoachId otherCoach = new CoachId(UUID.randomUUID());

        assertThatThrownBy(() -> useCase.update(otherCoach, ownedTeamId, ownedPeriodId, sessionId,
                new UpdateSessionRequest(SessionStatus.SKIPPED, null)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void throwsNotFoundWhenUpdatingAnUnknownSession() {
        assertThatThrownBy(() -> useCase.update(owner, ownedTeamId, ownedPeriodId, SessionId.newId(),
                new UpdateSessionRequest(SessionStatus.SKIPPED, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void throwsNotFoundWhenSessionBelongsToADifferentPeriod() {
        SessionId sessionId = generateOneSession();
        PeriodResponse otherPeriod = periodUseCase.create(owner, ownedTeamId,
                new CreatePeriodRequest("Other term", START, END, MatchFormat.SEVEN_V_SEVEN));

        assertThatThrownBy(() -> useCase.update(owner, ownedTeamId, new PeriodId(otherPeriod.id()), sessionId,
                new UpdateSessionRequest(SessionStatus.SKIPPED, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private SessionId generateOneSession() {
        List<SessionResponse> sessions = useCase.generate(owner, ownedTeamId, ownedPeriodId,
                new GenerateSessionsRequest(Set.of(DayOfWeek.THURSDAY)));
        return new SessionId(sessions.get(0).id());
    }

    private static final class InMemorySessionRepository implements SessionRepositoryPort {

        private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

        @Override
        public Session save(Session session) {
            sessions.put(session.id().value(), session);
            return session;
        }

        @Override
        public Optional<Session> findById(SessionId id) {
            return Optional.ofNullable(sessions.get(id.value()));
        }

        @Override
        public List<Session> findAllByPeriod(PeriodId periodId) {
            return sessions.values().stream()
                    .filter(session -> session.belongsToPeriod(periodId))
                    .collect(Collectors.toList());
        }

        void clear() {
            sessions.clear();
        }
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
