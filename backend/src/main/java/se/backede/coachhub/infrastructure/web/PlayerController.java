package se.backede.coachhub.infrastructure.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import se.backede.coachhub.application.dto.CreatePlayerRequest;
import se.backede.coachhub.application.dto.PlayerResponse;
import se.backede.coachhub.application.dto.UpdatePlayerRequest;
import se.backede.coachhub.application.usecase.PlayerUseCaseService;
import se.backede.coachhub.domain.model.PlayerId;
import se.backede.coachhub.domain.model.TeamId;
import se.backede.coachhub.infrastructure.security.CurrentCoachResolver;

@RestController
@RequestMapping("/api/teams/{teamId}/players")
public class PlayerController {

    private final PlayerUseCaseService playerUseCaseService;
    private final CurrentCoachResolver currentCoachResolver;

    public PlayerController(PlayerUseCaseService playerUseCaseService, CurrentCoachResolver currentCoachResolver) {
        this.playerUseCaseService = playerUseCaseService;
        this.currentCoachResolver = currentCoachResolver;
    }

    @PostMapping
    ResponseEntity<PlayerResponse> create(@PathVariable UUID teamId, @RequestBody CreatePlayerRequest request) {
        PlayerResponse response = playerUseCaseService.create(currentCoachResolver.resolve(), new TeamId(teamId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    List<PlayerResponse> list(@PathVariable UUID teamId) {
        return playerUseCaseService.list(currentCoachResolver.resolve(), new TeamId(teamId));
    }

    @GetMapping("/{id}")
    PlayerResponse get(@PathVariable UUID teamId, @PathVariable String id) {
        return playerUseCaseService.get(currentCoachResolver.resolve(), new TeamId(teamId), new PlayerId(id));
    }

    @PatchMapping("/{id}")
    PlayerResponse update(@PathVariable UUID teamId, @PathVariable String id, @RequestBody UpdatePlayerRequest request) {
        return playerUseCaseService.update(currentCoachResolver.resolve(), new TeamId(teamId), new PlayerId(id), request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID teamId, @PathVariable String id) {
        playerUseCaseService.delete(currentCoachResolver.resolve(), new TeamId(teamId), new PlayerId(id));
        return ResponseEntity.noContent().build();
    }
}
