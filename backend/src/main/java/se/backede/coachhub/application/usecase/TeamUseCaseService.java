package se.backede.coachhub.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import se.backede.coachhub.application.dto.CreateTeamRequest;
import se.backede.coachhub.application.dto.TeamResponse;
import se.backede.coachhub.application.dto.UpdateTeamRequest;
import se.backede.coachhub.application.mapper.TeamMapper;
import se.backede.coachhub.domain.model.CoachId;
import se.backede.coachhub.domain.model.Team;
import se.backede.coachhub.domain.model.TeamId;
import se.backede.coachhub.domain.repository.TeamRepositoryPort;
import se.backede.coachhub.shared.exception.AccessDeniedException;
import se.backede.coachhub.shared.exception.ResourceNotFoundException;

@Service
public class TeamUseCaseService {

    private final TeamRepositoryPort teamRepository;

    public TeamUseCaseService(TeamRepositoryPort teamRepository) {
        this.teamRepository = teamRepository;
    }

    public TeamResponse create(CoachId requester, CreateTeamRequest request) {
        Team team = Team.create(requester, request.name(), request.birthYear(), request.genderCategory());
        return TeamMapper.toResponse(teamRepository.save(team));
    }

    public List<TeamResponse> list(CoachId requester) {
        return teamRepository.findAllByOwner(requester).stream()
                .map(TeamMapper::toResponse)
                .toList();
    }

    public TeamResponse get(CoachId requester, TeamId id) {
        return TeamMapper.toResponse(requireOwned(requester, id));
    }

    public TeamResponse update(CoachId requester, TeamId id, UpdateTeamRequest request) {
        Team existing = requireOwned(requester, id);
        Team updated = existing.withDetails(request.name(), request.birthYear(), request.genderCategory());
        return TeamMapper.toResponse(teamRepository.save(updated));
    }

    public void delete(CoachId requester, TeamId id) {
        requireOwned(requester, id);
        teamRepository.deleteById(id);
    }

    private Team requireOwned(CoachId requester, TeamId id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id.value()));
        if (!team.isOwnedBy(requester)) {
            throw new AccessDeniedException("Team is not owned by the requesting coach");
        }
        return team;
    }
}
