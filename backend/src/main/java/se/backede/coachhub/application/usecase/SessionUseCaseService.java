package se.backede.coachhub.application.usecase;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import se.backede.coachhub.application.dto.GenerateSessionsRequest;
import se.backede.coachhub.application.dto.SessionResponse;
import se.backede.coachhub.application.mapper.SessionMapper;
import se.backede.coachhub.domain.model.CoachId;
import se.backede.coachhub.domain.model.Period;
import se.backede.coachhub.domain.model.PeriodId;
import se.backede.coachhub.domain.model.Session;
import se.backede.coachhub.domain.model.Team;
import se.backede.coachhub.domain.model.TeamId;
import se.backede.coachhub.domain.repository.PeriodRepositoryPort;
import se.backede.coachhub.domain.repository.SessionRepositoryPort;
import se.backede.coachhub.domain.repository.TeamRepositoryPort;
import se.backede.coachhub.domain.service.SessionGenerationService;
import se.backede.coachhub.shared.exception.AccessDeniedException;
import se.backede.coachhub.shared.exception.ResourceNotFoundException;

/**
 * Not yet a Spring bean ({@code @Service}): {@link SessionRepositoryPort} has
 * no implementation until #42 adds the JPA adapter, and the full-context
 * persistence integration tests would fail to autowire an unimplemented
 * port. #42 adds {@code @Service} back alongside the adapter — same
 * approach as #35/#36 for {@code Player} and #38/#39 for {@code Period}.
 */
public class SessionUseCaseService {

    private final SessionRepositoryPort sessionRepository;
    private final PeriodRepositoryPort periodRepository;
    private final TeamRepositoryPort teamRepository;

    public SessionUseCaseService(SessionRepositoryPort sessionRepository, PeriodRepositoryPort periodRepository,
            TeamRepositoryPort teamRepository) {
        this.sessionRepository = sessionRepository;
        this.periodRepository = periodRepository;
        this.teamRepository = teamRepository;
    }

    /**
     * Generates one {@link Session} for every date in the period's range
     * that falls on a selected weekday and doesn't already have one.
     * Idempotent: calling this again with the same weekdays creates no
     * duplicates, since already-covered dates are skipped.
     */
    public List<SessionResponse> generate(CoachId requester, TeamId teamId, PeriodId periodId, GenerateSessionsRequest request) {
        Period period = requireOwnedPeriod(requester, teamId, periodId);
        List<Session> existing = sessionRepository.findAllByPeriod(periodId);
        Set<LocalDate> existingDates = existing.stream().map(Session::date).collect(Collectors.toSet());

        SessionGenerationService.generateDates(period.startDate(), period.endDate(), request.weekdays()).stream()
                .filter(date -> !existingDates.contains(date))
                .map(date -> Session.create(periodId, date))
                .forEach(sessionRepository::save);

        return list(requester, teamId, periodId);
    }

    public List<SessionResponse> list(CoachId requester, TeamId teamId, PeriodId periodId) {
        requireOwnedPeriod(requester, teamId, periodId);
        return sessionRepository.findAllByPeriod(periodId).stream()
                .map(SessionMapper::toResponse)
                .sorted((a, b) -> a.date().compareTo(b.date()))
                .toList();
    }

    private Period requireOwnedPeriod(CoachId requester, TeamId teamId, PeriodId periodId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId.value()));
        if (!team.isOwnedBy(requester)) {
            throw new AccessDeniedException("Team is not owned by the requesting coach");
        }
        Period period = periodRepository.findById(periodId)
                .orElseThrow(() -> new ResourceNotFoundException("Period not found: " + periodId.value()));
        if (!period.belongsToTeam(teamId)) {
            throw new ResourceNotFoundException("Period not found: " + periodId.value());
        }
        return period;
    }
}
