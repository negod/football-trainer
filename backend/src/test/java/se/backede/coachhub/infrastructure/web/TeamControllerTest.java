package se.backede.coachhub.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import se.backede.coachhub.application.dto.TeamResponse;
import se.backede.coachhub.application.usecase.TeamUseCaseService;
import se.backede.coachhub.domain.model.CoachId;
import se.backede.coachhub.domain.model.GenderCategory;
import se.backede.coachhub.domain.model.TeamId;
import se.backede.coachhub.infrastructure.security.CurrentCoachResolver;
import se.backede.coachhub.shared.exception.AccessDeniedException;
import se.backede.coachhub.shared.exception.DomainValidationException;
import se.backede.coachhub.shared.exception.ResourceNotFoundException;

@WebMvcTest(TeamController.class)
@AutoConfigureMockMvc(addFilters = false)
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamUseCaseService teamUseCaseService;

    @MockitoBean
    private CurrentCoachResolver currentCoachResolver;

    private final CoachId coach = new CoachId(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        when(currentCoachResolver.resolve()).thenReturn(coach);
    }

    @Test
    void createsATeam() throws Exception {
        TeamResponse response = new TeamResponse(UUID.randomUUID(), "Team", 2019, GenderCategory.BOYS, "P19");
        when(teamUseCaseService.create(eq(coach), any())).thenReturn(response);

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Team\",\"birthYear\":2019,\"genderCategory\":\"BOYS\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Team"))
                .andExpect(jsonPath("$.shorthand").value("P19"));
    }

    @Test
    void returns400WhenCreationFailsDomainValidation() throws Exception {
        when(teamUseCaseService.create(eq(coach), any()))
                .thenThrow(new DomainValidationException("Team name must not be blank"));

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"birthYear\":2019,\"genderCategory\":\"BOYS\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns404WhenTeamDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(teamUseCaseService.get(eq(coach), eq(new TeamId(id))))
                .thenThrow(new ResourceNotFoundException("Team not found: " + id));

        mockMvc.perform(get("/api/teams/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void returns403WhenTeamIsOwnedBySomeoneElse() throws Exception {
        UUID id = UUID.randomUUID();
        when(teamUseCaseService.get(eq(coach), eq(new TeamId(id))))
                .thenThrow(new AccessDeniedException("Team is not owned by the requesting coach"));

        mockMvc.perform(get("/api/teams/{id}", id))
                .andExpect(status().isForbidden());
    }

    @Test
    void listsTeamsOwnedByTheCurrentCoach() throws Exception {
        when(teamUseCaseService.list(coach)).thenReturn(
                List.of(new TeamResponse(UUID.randomUUID(), "Team", 2019, GenderCategory.BOYS, "P19")));

        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Team"));
    }

    @Test
    void updatesATeam() throws Exception {
        UUID id = UUID.randomUUID();
        TeamResponse response = new TeamResponse(id, "New", 2019, GenderCategory.MIXED, "P/F19");
        when(teamUseCaseService.update(eq(coach), eq(new TeamId(id)), any())).thenReturn(response);

        mockMvc.perform(patch("/api/teams/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New\",\"birthYear\":2019,\"genderCategory\":\"MIXED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"));
    }
}
