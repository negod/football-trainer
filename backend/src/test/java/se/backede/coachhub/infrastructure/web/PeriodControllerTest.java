package se.backede.coachhub.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

import se.backede.coachhub.application.dto.PeriodResponse;
import se.backede.coachhub.application.usecase.PeriodUseCaseService;
import se.backede.coachhub.domain.model.CoachId;
import se.backede.coachhub.domain.model.MatchFormat;
import se.backede.coachhub.domain.model.PeriodId;
import se.backede.coachhub.domain.model.TeamId;
import se.backede.coachhub.infrastructure.security.CurrentCoachResolver;
import se.backede.coachhub.shared.exception.AccessDeniedException;
import se.backede.coachhub.shared.exception.DomainValidationException;
import se.backede.coachhub.shared.exception.ResourceNotFoundException;

@WebMvcTest(PeriodController.class)
@AutoConfigureMockMvc(addFilters = false)
class PeriodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PeriodUseCaseService periodUseCaseService;

    @MockitoBean
    private CurrentCoachResolver currentCoachResolver;

    private final CoachId coach = new CoachId(UUID.randomUUID());
    private final UUID teamId = UUID.randomUUID();
    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate END = LocalDate.of(2026, 6, 1);

    @BeforeEach
    void setUp() {
        when(currentCoachResolver.resolve()).thenReturn(coach);
    }

    @Test
    void createsAPeriod() throws Exception {
        PeriodResponse response = new PeriodResponse(UUID.randomUUID(), teamId, "Spring term", START, END, MatchFormat.SEVEN_V_SEVEN);
        when(periodUseCaseService.create(eq(coach), eq(new TeamId(teamId)), any())).thenReturn(response);

        mockMvc.perform(post("/api/teams/{teamId}/periods", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Spring term\",\"startDate\":\"2026-01-01\",\"endDate\":\"2026-06-01\",\"format\":\"SEVEN_V_SEVEN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Spring term"))
                .andExpect(jsonPath("$.format").value("SEVEN_V_SEVEN"));
    }

    @Test
    void returns400WhenCreationFailsDomainValidation() throws Exception {
        when(periodUseCaseService.create(eq(coach), eq(new TeamId(teamId)), any()))
                .thenThrow(new DomainValidationException("Period end date must be after the start date"));

        mockMvc.perform(post("/api/teams/{teamId}/periods", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Term\",\"startDate\":\"2026-06-01\",\"endDate\":\"2026-01-01\",\"format\":\"SEVEN_V_SEVEN\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns404WhenTheTeamDoesNotExist() throws Exception {
        when(periodUseCaseService.list(coach, new TeamId(teamId)))
                .thenThrow(new ResourceNotFoundException("Team not found: " + teamId));

        mockMvc.perform(get("/api/teams/{teamId}/periods", teamId))
                .andExpect(status().isNotFound());
    }

    @Test
    void returns403WhenTheTeamIsOwnedBySomeoneElse() throws Exception {
        when(periodUseCaseService.list(coach, new TeamId(teamId)))
                .thenThrow(new AccessDeniedException("Team is not owned by the requesting coach"));

        mockMvc.perform(get("/api/teams/{teamId}/periods", teamId))
                .andExpect(status().isForbidden());
    }

    @Test
    void listsPeriodsOnTheTeam() throws Exception {
        when(periodUseCaseService.list(coach, new TeamId(teamId))).thenReturn(
                List.of(new PeriodResponse(UUID.randomUUID(), teamId, "Spring term", START, END, MatchFormat.SEVEN_V_SEVEN)));

        mockMvc.perform(get("/api/teams/{teamId}/periods", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Spring term"));
    }

    @Test
    void getsAPeriod() throws Exception {
        UUID id = UUID.randomUUID();
        PeriodResponse response = new PeriodResponse(id, teamId, "Spring term", START, END, MatchFormat.SEVEN_V_SEVEN);
        when(periodUseCaseService.get(coach, new TeamId(teamId), new PeriodId(id))).thenReturn(response);

        mockMvc.perform(get("/api/teams/{teamId}/periods/{id}", teamId, id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Spring term"));
    }

    @Test
    void returns404WhenThePeriodDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(periodUseCaseService.get(coach, new TeamId(teamId), new PeriodId(id)))
                .thenThrow(new ResourceNotFoundException("Period not found: " + id));

        mockMvc.perform(get("/api/teams/{teamId}/periods/{id}", teamId, id))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatesAPeriod() throws Exception {
        UUID id = UUID.randomUUID();
        PeriodResponse response = new PeriodResponse(id, teamId, "New name", START, END, MatchFormat.NINE_V_NINE);
        when(periodUseCaseService.update(eq(coach), eq(new TeamId(teamId)), eq(new PeriodId(id)), any()))
                .thenReturn(response);

        mockMvc.perform(patch("/api/teams/{teamId}/periods/{id}", teamId, id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New name\",\"startDate\":\"2026-01-01\",\"endDate\":\"2026-06-01\",\"format\":\"NINE_V_NINE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New name"));
    }

    @Test
    void returnsTheSuggestedFormat() throws Exception {
        when(periodUseCaseService.suggestFormat(coach, new TeamId(teamId))).thenReturn(MatchFormat.SEVEN_V_SEVEN);

        mockMvc.perform(get("/api/teams/{teamId}/periods/suggested-format", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.format").value("SEVEN_V_SEVEN"));
    }
}
