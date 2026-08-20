package se.backede.coachhub.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import se.backede.coachhub.domain.model.CoachId;
import se.backede.coachhub.domain.model.GenderCategory;
import se.backede.coachhub.domain.model.Player;
import se.backede.coachhub.domain.model.Team;
import se.backede.coachhub.domain.repository.PlayerRepositoryPort;
import se.backede.coachhub.domain.repository.TeamRepositoryPort;

/**
 * Runs the real Liquibase changelog against a Postgres container and
 * exercises the JPA adapter through it, per docs/ai-instructions.md's
 * requirement to verify schema changes against the real database engine.
 */
@SpringBootTest
@Testcontainers
class PlayerPersistenceIntegrationTest {

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
    private PlayerRepositoryPort playerRepository;

    @Autowired
    private TeamRepositoryPort teamRepository;

    private Team ownedTeam() {
        Team team = Team.create(new CoachId(UUID.randomUUID()), "Team", 2015, GenderCategory.BOYS);
        return teamRepository.save(team);
    }

    @Test
    void savesAndReloadsAPlayerThroughTheLiquibaseManagedSchema() {
        Team team = ownedTeam();
        Player player = Player.create(team.id(), "Alex Andersson", 2015, "Forward");

        playerRepository.save(player);

        List<Player> found = playerRepository.findAllByTeam(team.id());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).name()).isEqualTo("Alex Andersson");
        assertThat(found.get(0).position()).isEqualTo("Forward");
    }

    @Test
    void savesAPlayerWithoutAPosition() {
        Team team = ownedTeam();
        Player player = Player.create(team.id(), "Alex Andersson", 2015, null);

        playerRepository.save(player);

        Player found = playerRepository.findById(player.id()).orElseThrow();
        assertThat(found.position()).isNull();
    }

    @Test
    void findAllByTeamDoesNotReturnAnotherTeamsPlayers() {
        Team team = ownedTeam();
        Team otherTeam = ownedTeam();
        playerRepository.save(Player.create(team.id(), "Mine", 2015, null));
        playerRepository.save(Player.create(otherTeam.id(), "Not mine", 2014, null));

        List<Player> found = playerRepository.findAllByTeam(team.id());

        assertThat(found).hasSize(1);
        assertThat(found.get(0).name()).isEqualTo("Mine");
    }

    @Test
    void deleteRemovesThePlayer() {
        Team team = ownedTeam();
        Player player = Player.create(team.id(), "Alex Andersson", 2015, null);
        playerRepository.save(player);

        playerRepository.deleteById(player.id());

        assertThat(playerRepository.findById(player.id())).isEmpty();
    }
}
