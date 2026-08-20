package se.backede.coachhub.application.usecase;

import java.util.List;

import se.backede.coachhub.application.dto.CreatePeriodRequest;
import se.backede.coachhub.application.dto.PeriodResponse;
import se.backede.coachhub.application.dto.UpdatePeriodRequest;
import se.backede.coachhub.application.mapper.PeriodMapper;
import se.backede.coachhub.domain.model.CoachId;
import se.backede.coachhub.domain.model.MatchFormat;
import se.backede.coachhub.domain.model.Period;
import se.backede.coachhub.domain.model.PeriodId;
import se.backede.coachhub.domain.model.Team;
import se.backede.coachhub.domain.model.TeamId;
import se.backede.coachhub.domain.repository.PeriodRepositoryPort;
import se.backede.coachhub.domain.repository.TeamRepositoryPort;
import se.backede.coachhub.shared.exception.AccessDeniedException;
import se.backede.coachhub.shared.exception.ResourceNotFoundException;

/**
 * Not yet a Spring bean ({@code @Service}): {@link PeriodRepositoryPort} has
 * no implementation until #39 adds the JPA adapter, and the full-context
 * persistence integration tests would fail to autowire an unimplemented
 * port. #39 adds {@code @Service} back alongside the adapter — same
 * approach as #35/#36 for {@code Player}.
 */
public class PeriodUseCaseService {

    private final PeriodRepositoryPort periodRepository;
    private final TeamRepositoryPort teamRepository;

    public PeriodUseCaseService(PeriodRepositoryPort periodRepository, TeamRepositoryPort teamRepository) {
        this.periodRepository = periodRepository;
        this.teamRepository = teamRepository;
    }

    public PeriodResponse create(CoachId requester, TeamId teamId, CreatePeriodRequest request) {
        requireOwnedTeam(requester, teamId);
        Period period = Period.create(teamId, request.name(), request.startDate(), request.endDate(), request.format());
        return PeriodMapper.toResponse(periodRepository.save(period));
    }

    public List<PeriodResponse> list(CoachId requester, TeamId teamId) {
        requireOwnedTeam(requester, teamId);
        return periodRepository.findAllByTeam(teamId).stream()
                .map(PeriodMapper::toResponse)
                .toList();
    }

    public PeriodResponse get(CoachId requester, TeamId teamId, PeriodId id) {
        requireOwnedTeam(requester, teamId);
        return PeriodMapper.toResponse(requireOnTeam(teamId, id));
    }

    public PeriodResponse update(CoachId requester, TeamId teamId, PeriodId id, UpdatePeriodRequest request) {
        requireOwnedTeam(requester, teamId);
        Period existing = requireOnTeam(teamId, id);
        Period updated = existing.withDetails(request.name(), request.startDate(), request.endDate(), request.format());
        return PeriodMapper.toResponse(periodRepository.save(updated));
    }

    public MatchFormat suggestFormat(CoachId requester, TeamId teamId) {
        Team team = requireOwnedTeam(requester, teamId);
        return MatchFormat.suggestedFor(team.birthYear());
    }

    private Team requireOwnedTeam(CoachId requester, TeamId teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId.value()));
        if (!team.isOwnedBy(requester)) {
            throw new AccessDeniedException("Team is not owned by the requesting coach");
        }
        return team;
    }

    private Period requireOnTeam(TeamId teamId, PeriodId id) {
        Period period = periodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Period not found: " + id.value()));
        if (!period.belongsToTeam(teamId)) {
            throw new ResourceNotFoundException("Period not found: " + id.value());
        }
        return period;
    }
}
