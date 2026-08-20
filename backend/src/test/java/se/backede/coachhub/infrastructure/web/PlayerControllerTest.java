package se.backede.coachhub.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

import se.backede.coachhub.application.dto.PlayerResponse;
import se.backede.coachhub.application.usecase.PlayerUseCaseService;
import se.backede.coachhub.domain.model.CoachId;
import se.backede.coachhub.domain.model.PlayerId;
import se.backede.coachhub.domain.model.TeamId;
import se.backede.coachhub.infrastructure.security.CurrentCoachResolver;
import se.backede.coachhub.shared.exception.AccessDeniedException;
import se.backede.coachhub.shared.exception.DomainValidationException;
import se.backede.coachhub.shared.exception.ResourceNotFoundException;

@WebMvcTest(PlayerController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayerUseCaseService playerUseCaseService;

    @MockitoBean
    private CurrentCoachResolver currentCoachResolver;

    private final CoachId coach = new CoachId(UUID.randomUUID());
    private final UUID teamId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(currentCoachResolver.resolve()).thenReturn(coach);
    }

    @Test
    void createsAPlayer() throws Exception {
        PlayerResponse response = new PlayerResponse("player-1", teamId.toString(), "Alex Andersson", 2015, "Forward");
        when(playerUseCaseService.create(eq(coach), eq(new TeamId(teamId)), any())).thenReturn(response);

        mockMvc.perform(post("/api/teams/{teamId}/players", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alex Andersson\",\"birthYear\":2015,\"position\":\"Forward\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Alex Andersson"))
                .andExpect(jsonPath("$.position").value("Forward"));
    }

    @Test
    void returns400WhenCreationFailsDomainValidation() throws Exception {
        when(playerUseCaseService.create(eq(coach), eq(new TeamId(teamId)), any()))
                .thenThrow(new DomainValidationException("Player name must not be blank"));

        mockMvc.perform(post("/api/teams/{teamId}/players", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"birthYear\":2015,\"position\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns404WhenTheTeamDoesNotExist() throws Exception {
        when(playerUseCaseService.list(coach, new TeamId(teamId)))
                .thenThrow(new ResourceNotFoundException("Team not found: " + teamId));

        mockMvc.perform(get("/api/teams/{teamId}/players", teamId))
                .andExpect(status().isNotFound());
    }

    @Test
    void returns403WhenTheTeamIsOwnedBySomeoneElse() throws Exception {
        when(playerUseCaseService.list(coach, new TeamId(teamId)))
                .thenThrow(new AccessDeniedException("Team is not owned by the requesting coach"));

        mockMvc.perform(get("/api/teams/{teamId}/players", teamId))
                .andExpect(status().isForbidden());
    }

    @Test
    void listsPlayersOnTheTeam() throws Exception {
        when(playerUseCaseService.list(coach, new TeamId(teamId))).thenReturn(
                List.of(new PlayerResponse("player-1", teamId.toString(), "Alex Andersson", 2015, "Forward")));

        mockMvc.perform(get("/api/teams/{teamId}/players", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alex Andersson"));
    }

    @Test
    void getsAPlayer() throws Exception {
        PlayerResponse response = new PlayerResponse("player-1", teamId.toString(), "Alex Andersson", 2015, "Forward");
        when(playerUseCaseService.get(coach, new TeamId(teamId), new PlayerId("player-1"))).thenReturn(response);

        mockMvc.perform(get("/api/teams/{teamId}/players/{id}", teamId, "player-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alex Andersson"));
    }

    @Test
    void returns404WhenThePlayerDoesNotExist() throws Exception {
        when(playerUseCaseService.get(coach, new TeamId(teamId), new PlayerId("missing")))
                .thenThrow(new ResourceNotFoundException("Player not found: missing"));

        mockMvc.perform(get("/api/teams/{teamId}/players/{id}", teamId, "missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatesAPlayer() throws Exception {
        PlayerResponse response = new PlayerResponse("player-1", teamId.toString(), "New name", 2015, "Midfielder");
        when(playerUseCaseService.update(eq(coach), eq(new TeamId(teamId)), eq(new PlayerId("player-1")), any()))
                .thenReturn(response);

        mockMvc.perform(patch("/api/teams/{teamId}/players/{id}", teamId, "player-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New name\",\"birthYear\":2015,\"position\":\"Midfielder\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New name"));
    }

    @Test
    void deletesAPlayer() throws Exception {
        mockMvc.perform(delete("/api/teams/{teamId}/players/{id}", teamId, "player-1"))
                .andExpect(status().isNoContent());

        verify(playerUseCaseService).delete(coach, new TeamId(teamId), new PlayerId("player-1"));
    }
}
