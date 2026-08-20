package se.backede.coachhub.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import se.backede.coachhub.application.dto.SessionResponse;
import se.backede.coachhub.application.usecase.SessionUseCaseService;
import se.backede.coachhub.domain.model.CoachId;
import se.backede.coachhub.domain.model.PeriodId;
import se.backede.coachhub.domain.model.SessionStatus;
import se.backede.coachhub.domain.model.TeamId;
import se.backede.coachhub.infrastructure.security.CurrentCoachResolver;
import se.backede.coachhub.shared.exception.AccessDeniedException;
import se.backede.coachhub.shared.exception.DomainValidationException;
import se.backede.coachhub.shared.exception.ResourceNotFoundException;

@WebMvcTest(SessionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessionUseCaseService sessionUseCaseService;

    @MockitoBean
    private CurrentCoachResolver currentCoachResolver;

    private final CoachId coach = new CoachId(UUID.randomUUID());
    private final UUID teamId = UUID.randomUUID();
    private final UUID periodId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(currentCoachResolver.resolve()).thenReturn(coach);
    }

    @Test
    void generatesSessions() throws Exception {
        SessionResponse response = new SessionResponse(UUID.randomUUID(), periodId, LocalDate.of(2026, 1, 6), SessionStatus.SCHEDULED);
        when(sessionUseCaseService.generate(eq(coach), eq(new TeamId(teamId)), eq(new PeriodId(periodId)), any()))
                .thenReturn(List.of(response));

        mockMvc.perform(post("/api/teams/{teamId}/periods/{periodId}/generate-sessions", teamId, periodId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weekdays\":[\"TUESDAY\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-01-06"))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));
    }

    @Test
    void returns400WhenGenerationFailsDomainValidation() throws Exception {
        when(sessionUseCaseService.generate(eq(coach), eq(new TeamId(teamId)), eq(new PeriodId(periodId)), any()))
                .thenThrow(new DomainValidationException("At least one weekday must be selected"));

        mockMvc.perform(post("/api/teams/{teamId}/periods/{periodId}/generate-sessions", teamId, periodId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weekdays\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns404WhenThePeriodDoesNotExist() throws Exception {
        when(sessionUseCaseService.generate(eq(coach), eq(new TeamId(teamId)), eq(new PeriodId(periodId)), any()))
                .thenThrow(new ResourceNotFoundException("Period not found: " + periodId));

        mockMvc.perform(post("/api/teams/{teamId}/periods/{periodId}/generate-sessions", teamId, periodId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weekdays\":[\"TUESDAY\"]}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returns403WhenTheTeamIsOwnedBySomeoneElse() throws Exception {
        when(sessionUseCaseService.generate(eq(coach), eq(new TeamId(teamId)), eq(new PeriodId(periodId)), any()))
                .thenThrow(new AccessDeniedException("Team is not owned by the requesting coach"));

        mockMvc.perform(post("/api/teams/{teamId}/periods/{periodId}/generate-sessions", teamId, periodId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weekdays\":[\"TUESDAY\"]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listsSessions() throws Exception {
        SessionResponse response = new SessionResponse(UUID.randomUUID(), periodId, LocalDate.of(2026, 1, 6), SessionStatus.SCHEDULED);
        when(sessionUseCaseService.list(coach, new TeamId(teamId), new PeriodId(periodId))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/teams/{teamId}/periods/{periodId}/sessions", teamId, periodId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-01-06"));
    }

    @Test
    void returns404WhenListingForAnUnknownPeriod() throws Exception {
        when(sessionUseCaseService.list(coach, new TeamId(teamId), new PeriodId(periodId)))
                .thenThrow(new ResourceNotFoundException("Period not found: " + periodId));

        mockMvc.perform(get("/api/teams/{teamId}/periods/{periodId}/sessions", teamId, periodId))
                .andExpect(status().isNotFound());
    }
}
