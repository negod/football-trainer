package se.backede.coachhub.application.usecase;

import java.util.List;

import se.backede.coachhub.application.dto.CreatePlayerRequest;
import se.backede.coachhub.application.dto.PlayerResponse;
import se.backede.coachhub.application.dto.UpdatePlayerRequest;
import se.backede.coachhub.application.mapper.PlayerMapper;
import se.backede.coachhub.domain.model.CoachId;
import se.backede.coachhub.domain.model.Player;
import se.backede.coachhub.domain.model.PlayerId;
import se.backede.coachhub.domain.model.Team;
import se.backede.coachhub.domain.model.TeamId;
import se.backede.coachhub.domain.repository.PlayerRepositoryPort;
import se.backede.coachhub.domain.repository.TeamRepositoryPort;
import se.backede.coachhub.shared.exception.AccessDeniedException;
import se.backede.coachhub.shared.exception.ResourceNotFoundException;

/**
 * Not yet a Spring bean ({@code @Service}): {@link PlayerRepositoryPort} has
 * no implementation until #36 adds the JPA adapter, and the full-context
 * {@code TeamPersistenceIntegrationTest} would fail to autowire an
 * unimplemented port. #36 adds {@code @Service} back alongside the adapter.
 */
public class PlayerUseCaseService {

    private final PlayerRepositoryPort playerRepository;
    private final TeamRepositoryPort teamRepository;

    public PlayerUseCaseService(PlayerRepositoryPort playerRepository, TeamRepositoryPort teamRepository) {
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
    }

    public PlayerResponse create(CoachId requester, TeamId teamId, CreatePlayerRequest request) {
        requireOwnedTeam(requester, teamId);
        Player player = Player.create(teamId, request.name(), request.birthYear(), request.position());
        return PlayerMapper.toResponse(playerRepository.save(player));
    }

    public List<PlayerResponse> list(CoachId requester, TeamId teamId) {
        requireOwnedTeam(requester, teamId);
        return playerRepository.findAllByTeam(teamId).stream()
                .map(PlayerMapper::toResponse)
                .toList();
    }

    public PlayerResponse get(CoachId requester, TeamId teamId, PlayerId id) {
        requireOwnedTeam(requester, teamId);
        return PlayerMapper.toResponse(requireOnTeam(teamId, id));
    }

    public PlayerResponse update(CoachId requester, TeamId teamId, PlayerId id, UpdatePlayerRequest request) {
        requireOwnedTeam(requester, teamId);
        Player existing = requireOnTeam(teamId, id);
        Player updated = existing.withDetails(request.name(), request.birthYear(), request.position());
        return PlayerMapper.toResponse(playerRepository.save(updated));
    }

    public void delete(CoachId requester, TeamId teamId, PlayerId id) {
        requireOwnedTeam(requester, teamId);
        requireOnTeam(teamId, id);
        playerRepository.deleteById(id);
    }

    private Team requireOwnedTeam(CoachId requester, TeamId teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId.value()));
        if (!team.isOwnedBy(requester)) {
            throw new AccessDeniedException("Team is not owned by the requesting coach");
        }
        return team;
    }

    private Player requireOnTeam(TeamId teamId, PlayerId id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found: " + id.value()));
        if (!player.belongsToTeam(teamId)) {
            throw new ResourceNotFoundException("Player not found: " + id.value());
        }
        return player;
    }
}
