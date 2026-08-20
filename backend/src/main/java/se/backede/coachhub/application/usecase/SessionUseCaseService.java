package se.backede.coachhub.application.usecase;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import se.backede.coachhub.application.dto.CreateAdhocSessionRequest;
import se.backede.coachhub.application.dto.GenerateSessionsRequest;
import se.backede.coachhub.application.dto.SessionResponse;
import se.backede.coachhub.application.dto.UpdateSessionRequest;
import se.backede.coachhub.application.mapper.SessionMapper;
import se.backede.coachhub.domain.model.CoachId;
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
import se.backede.coachhub.domain.service.SessionGenerationService;
import se.backede.coachhub.shared.exception.AccessDeniedException;
import se.backede.coachhub.shared.exception.DomainValidationException;
import se.backede.coachhub.shared.exception.ResourceNotFoundException;

@Service
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

    /**
     * Skips/restores (via {@code status}) and/or reschedules (via
     * {@code date}) a session; either field may be omitted to leave that
     * aspect unchanged.
     */
    public SessionResponse update(CoachId requester, TeamId teamId, PeriodId periodId, SessionId sessionId, UpdateSessionRequest request) {
        requireOwnedPeriod(requester, teamId, periodId);
        Session session = requireOnPeriod(periodId, sessionId);

        if (request.status() != null) {
            session = request.status() == SessionStatus.SKIPPED ? session.skip() : session.restore();
        }
        if (request.date() != null && !request.date().equals(session.date())) {
            requireDateAvailable(periodId, request.date());
            session = session.reschedule(request.date());
        }

        return SessionMapper.toResponse(sessionRepository.save(session));
    }

    /** Adds a one-off session not tied to the period's recurring weekdays. */
    public SessionResponse addAdhoc(CoachId requester, TeamId teamId, PeriodId periodId, CreateAdhocSessionRequest request) {
        requireOwnedPeriod(requester, teamId, periodId);
        requireDateAvailable(periodId, request.date());
        Session session = Session.createAdhoc(periodId, request.date());
        return SessionMapper.toResponse(sessionRepository.save(session));
    }

    private void requireDateAvailable(PeriodId periodId, LocalDate date) {
        boolean taken = sessionRepository.findAllByPeriod(periodId).stream()
                .anyMatch(session -> session.date().equals(date));
        if (taken) {
            throw new DomainValidationException("A session already exists on " + date);
        }
    }

    private Session requireOnPeriod(PeriodId periodId, SessionId sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId.value()));
        if (!session.belongsToPeriod(periodId)) {
            throw new ResourceNotFoundException("Session not found: " + sessionId.value());
        }
        return session;
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
