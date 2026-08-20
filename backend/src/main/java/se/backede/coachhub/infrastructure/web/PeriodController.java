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

import se.backede.coachhub.application.dto.CreatePeriodRequest;
import se.backede.coachhub.application.dto.PeriodResponse;
import se.backede.coachhub.application.dto.SuggestedFormatResponse;
import se.backede.coachhub.application.dto.UpdatePeriodRequest;
import se.backede.coachhub.application.usecase.PeriodUseCaseService;
import se.backede.coachhub.domain.model.PeriodId;
import se.backede.coachhub.domain.model.TeamId;
import se.backede.coachhub.infrastructure.security.CurrentCoachResolver;

@RestController
@RequestMapping("/api/teams/{teamId}/periods")
public class PeriodController {

    private final PeriodUseCaseService periodUseCaseService;
    private final CurrentCoachResolver currentCoachResolver;

    public PeriodController(PeriodUseCaseService periodUseCaseService, CurrentCoachResolver currentCoachResolver) {
        this.periodUseCaseService = periodUseCaseService;
        this.currentCoachResolver = currentCoachResolver;
    }

    @PostMapping
    ResponseEntity<PeriodResponse> create(@PathVariable UUID teamId, @RequestBody CreatePeriodRequest request) {
        PeriodResponse response = periodUseCaseService.create(currentCoachResolver.resolve(), new TeamId(teamId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    List<PeriodResponse> list(@PathVariable UUID teamId) {
        return periodUseCaseService.list(currentCoachResolver.resolve(), new TeamId(teamId));
    }

    @GetMapping("/suggested-format")
    SuggestedFormatResponse suggestedFormat(@PathVariable UUID teamId) {
        return new SuggestedFormatResponse(periodUseCaseService.suggestFormat(currentCoachResolver.resolve(), new TeamId(teamId)));
    }

    @GetMapping("/{id}")
    PeriodResponse get(@PathVariable UUID teamId, @PathVariable UUID id) {
        return periodUseCaseService.get(currentCoachResolver.resolve(), new TeamId(teamId), new PeriodId(id));
    }

    @PatchMapping("/{id}")
    PeriodResponse update(@PathVariable UUID teamId, @PathVariable UUID id, @RequestBody UpdatePeriodRequest request) {
        return periodUseCaseService.update(currentCoachResolver.resolve(), new TeamId(teamId), new PeriodId(id), request);
    }
}
