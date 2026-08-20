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
import se.backede.coachhub.domain.model.Team;
import se.backede.coachhub.domain.repository.TeamRepositoryPort;

/**
 * Runs the real Liquibase changelog against a Postgres container and
 * exercises the JPA adapter through it, per docs/ai-instructions.md's
 * requirement to verify schema changes against the real database engine.
 */
@SpringBootTest
@Testcontainers
class TeamPersistenceIntegrationTest {

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
    private TeamRepositoryPort teamRepository;

    @Test
    void savesAndReloadsATeamThroughTheLiquibaseManagedSchema() {
        CoachId owner = new CoachId(UUID.randomUUID());
        Team team = Team.create(owner, "IFK Testby", 2019, GenderCategory.BOYS);

        teamRepository.save(team);

        List<Team> found = teamRepository.findAllByOwner(owner);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).name()).isEqualTo("IFK Testby");
        assertThat(found.get(0).shorthand()).isEqualTo("P19");
    }

    @Test
    void findAllByOwnerDoesNotReturnAnotherCoachsTeams() {
        CoachId owner = new CoachId(UUID.randomUUID());
        CoachId otherCoach = new CoachId(UUID.randomUUID());
        teamRepository.save(Team.create(owner, "Mine", 2019, GenderCategory.BOYS));
        teamRepository.save(Team.create(otherCoach, "Not mine", 2018, GenderCategory.GIRLS));

        List<Team> found = teamRepository.findAllByOwner(owner);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).name()).isEqualTo("Mine");
    }

    @Test
    void deleteRemovesTheTeam() {
        CoachId owner = new CoachId(UUID.randomUUID());
        Team team = Team.create(owner, "Team", 2019, GenderCategory.BOYS);
        teamRepository.save(team);

        teamRepository.deleteById(team.id());

        assertThat(teamRepository.findById(team.id())).isEmpty();
    }
}
