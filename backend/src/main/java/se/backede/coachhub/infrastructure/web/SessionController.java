package se.backede.coachhub.infrastructure.web;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import se.backede.coachhub.application.dto.GenerateSessionsRequest;
import se.backede.coachhub.application.dto.SessionResponse;
import se.backede.coachhub.application.usecase.SessionUseCaseService;
import se.backede.coachhub.domain.model.PeriodId;
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
}
