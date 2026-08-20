package se.backede.coachhub.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Confirms the existing {@code /api/teams/**} permit-list entry already
 * covers the nested player routes without a SecurityConfig change: Spring's
 * {@code **} segment matches across the extra path segments, so
 * {@code /api/teams/{id}/players/...} is already permitted. Runs with the
 * real security filter chain (unlike PlayerControllerTest, which disables it).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PlayerSecurityConfigTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.auth.token-secret", () -> "test-secret");
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void permitsAnAnonymousRequestToTheNestedPlayersRoute() throws Exception {
        UUID unknownTeamId = UUID.randomUUID();

        // Reaches PlayerUseCaseService (not blocked by security), which
        // reports 404 since the team doesn't exist.
        mockMvc.perform(get("/api/teams/{teamId}/players", unknownTeamId))
                .andExpect(status().isNotFound());
    }
}
