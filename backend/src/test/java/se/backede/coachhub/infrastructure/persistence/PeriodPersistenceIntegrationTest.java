package se.backede.coachhub.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
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
import se.backede.coachhub.domain.model.MatchFormat;
import se.backede.coachhub.domain.model.Period;
import se.backede.coachhub.domain.model.Team;
import se.backede.coachhub.domain.repository.PeriodRepositoryPort;
import se.backede.coachhub.domain.repository.TeamRepositoryPort;

/**
 * Runs the real Liquibase changelog against a Postgres container and
 * exercises the JPA adapter through it, per docs/ai-instructions.md's
 * requirement to verify schema changes against the real database engine.
 */
@SpringBootTest
@Testcontainers
class PeriodPersistenceIntegrationTest {

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
    private PeriodRepositoryPort periodRepository;

    @Autowired
    private TeamRepositoryPort teamRepository;

    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate END = LocalDate.of(2026, 6, 1);

    private Team ownedTeam() {
        Team team = Team.create(new CoachId(UUID.randomUUID()), "Team", 2015, GenderCategory.BOYS);
        return teamRepository.save(team);
    }

    @Test
    void savesAndReloadsAPeriodThroughTheLiquibaseManagedSchema() {
        Team team = ownedTeam();
        Period period = Period.create(team.id(), "Spring term", START, END, MatchFormat.SEVEN_V_SEVEN);

        periodRepository.save(period);

        List<Period> found = periodRepository.findAllByTeam(team.id());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).name()).isEqualTo("Spring term");
        assertThat(found.get(0).format()).isEqualTo(MatchFormat.SEVEN_V_SEVEN);
    }

    @Test
    void findAllByTeamDoesNotReturnAnotherTeamsPeriods() {
        Team team = ownedTeam();
        Team otherTeam = ownedTeam();
        periodRepository.save(Period.create(team.id(), "Mine", START, END, MatchFormat.SEVEN_V_SEVEN));
        periodRepository.save(Period.create(otherTeam.id(), "Not mine", START, END, MatchFormat.FIVE_V_FIVE));

        List<Period> found = periodRepository.findAllByTeam(team.id());

        assertThat(found).hasSize(1);
        assertThat(found.get(0).name()).isEqualTo("Mine");
    }

    @Test
    void updatingAPeriodPersistsTheNewValues() {
        Team team = ownedTeam();
        Period period = Period.create(team.id(), "Old", START, END, MatchFormat.FIVE_V_FIVE);
        periodRepository.save(period);

        Period updated = period.withDetails("New", START.plusDays(1), END.plusDays(1), MatchFormat.SEVEN_V_SEVEN);
        periodRepository.save(updated);

        Period reloaded = periodRepository.findById(period.id()).orElseThrow();
        assertThat(reloaded.name()).isEqualTo("New");
        assertThat(reloaded.startDate()).isEqualTo(START.plusDays(1));
        assertThat(reloaded.endDate()).isEqualTo(END.plusDays(1));
        assertThat(reloaded.format()).isEqualTo(MatchFormat.SEVEN_V_SEVEN);
    }
}
