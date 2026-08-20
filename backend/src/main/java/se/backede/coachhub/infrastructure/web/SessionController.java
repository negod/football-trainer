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

import se.backede.coachhub.application.dto.CreateAdhocSessionRequest;
import se.backede.coachhub.application.dto.GenerateSessionsRequest;
import se.backede.coachhub.application.dto.SessionResponse;
import se.backede.coachhub.application.dto.UpdateSessionRequest;
import se.backede.coachhub.application.usecase.SessionUseCaseService;
import se.backede.coachhub.domain.model.PeriodId;
import se.backede.coachhub.domain.model.SessionId;
import se.backede.coachhub.domain.model.TeamId;
import se.backede.coachhub.infrastructure.security.CurrentCoachResolver;

@RestController
@RequestMapping("/api/teams/{teamId}/periods/{periodId}")
public class SessionController {

    private final SessionUseCaseService sessionUseCaseService;
    private final CurrentCoachResolver currentCoachResolver;

    public SessionController(SessionUseCaseService sessionUseCaseService, CurrentCoachResolver currentCoachResolver) {
        this.sessionUseCaseService = sessionUseCaseService;
        this.currentCoachResolver = currentCoachResolver;
    }

    @PostMapping("/generate-sessions")
    List<SessionResponse> generate(@PathVariable UUID teamId, @PathVariable UUID periodId, @RequestBody GenerateSessionsRequest request) {
        return sessionUseCaseService.generate(currentCoachResolver.resolve(), new TeamId(teamId), new PeriodId(periodId), request);
    }

    @GetMapping("/sessions")
    List<SessionResponse> list(@PathVariable UUID teamId, @PathVariable UUID periodId) {
        return sessionUseCaseService.list(currentCoachResolver.resolve(), new TeamId(teamId), new PeriodId(periodId));
    }

    @PostMapping("/sessions")
    ResponseEntity<SessionResponse> addAdhoc(@PathVariable UUID teamId, @PathVariable UUID periodId, @RequestBody CreateAdhocSessionRequest request) {
        SessionResponse response = sessionUseCaseService.addAdhoc(currentCoachResolver.resolve(), new TeamId(teamId), new PeriodId(periodId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/sessions/{id}")
    SessionResponse update(@PathVariable UUID teamId, @PathVariable UUID periodId, @PathVariable UUID id, @RequestBody UpdateSessionRequest request) {
        return sessionUseCaseService.update(currentCoachResolver.resolve(), new TeamId(teamId), new PeriodId(periodId), new SessionId(id), request);
    }
}
