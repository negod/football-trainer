package se.backede.coachhub.infrastructure.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import se.backede.coachhub.application.dto.CreateTeamRequest;
import se.backede.coachhub.application.dto.TeamResponse;
import se.backede.coachhub.application.dto.UpdateTeamRequest;
import se.backede.coachhub.application.usecase.TeamUseCaseService;
import se.backede.coachhub.domain.model.TeamId;
import se.backede.coachhub.infrastructure.security.CurrentCoachResolver;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamUseCaseService teamUseCaseService;
    private final CurrentCoachResolver currentCoachResolver;

    public TeamController(TeamUseCaseService teamUseCaseService, CurrentCoachResolver currentCoachResolver) {
        this.teamUseCaseService = teamUseCaseService;
        this.currentCoachResolver = currentCoachResolver;
    }

    @PostMapping
    ResponseEntity<TeamResponse> create(@RequestBody CreateTeamRequest request) {
        TeamResponse response = teamUseCaseService.create(currentCoachResolver.resolve(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    List<TeamResponse> list() {
        return teamUseCaseService.list(currentCoachResolver.resolve());
    }

    @GetMapping("/{id}")
    TeamResponse get(@PathVariable UUID id) {
        return teamUseCaseService.get(currentCoachResolver.resolve(), new TeamId(id));
    }

    @PatchMapping("/{id}")
    TeamResponse update(@PathVariable UUID id, @RequestBody UpdateTeamRequest request) {
        return teamUseCaseService.update(currentCoachResolver.resolve(), new TeamId(id), request);
    }
}
