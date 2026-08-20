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
import se.backede.coachhub.domain.model.Session;
import se.backede.coachhub.domain.model.SessionSource;
import se.backede.coachhub.domain.model.SessionStatus;
import se.backede.coachhub.domain.model.Team;
import se.backede.coachhub.domain.repository.PeriodRepositoryPort;
import se.backede.coachhub.domain.repository.SessionRepositoryPort;
import se.backede.coachhub.domain.repository.TeamRepositoryPort;

/**
 * Runs the real Liquibase changelog against a Postgres container and
 * exercises the JPA adapter through it, per docs/ai-instructions.md's
 * requirement to verify schema changes against the real database engine.
 */
@SpringBootTest
@Testcontainers
class SessionPersistenceIntegrationTest {

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
    private SessionRepositoryPort sessionRepository;

    @Autowired
    private PeriodRepositoryPort periodRepository;

    @Autowired
    private TeamRepositoryPort teamRepository;

    private Period ownedPeriod() {
        Team team = teamRepository.save(Team.create(new CoachId(UUID.randomUUID()), "Team", 2015, GenderCategory.BOYS));
        return periodRepository.save(Period.create(team.id(), "Term", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1), MatchFormat.SEVEN_V_SEVEN));
    }

    @Test
    void savesAndReloadsASessionThroughTheLiquibaseManagedSchema() {
        Period period = ownedPeriod();
        Session session = Session.create(period.id(), LocalDate.of(2026, 1, 6));

        sessionRepository.save(session);

        List<Session> found = sessionRepository.findAllByPeriod(period.id());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).date()).isEqualTo(LocalDate.of(2026, 1, 6));
        assertThat(found.get(0).status()).isEqualTo(SessionStatus.SCHEDULED);
        assertThat(found.get(0).source()).isEqualTo(SessionSource.GENERATED);
    }

    @Test
    void savesAndReloadsAnAdhocSessionAndItsSkippedStatus() {
        Period period = ownedPeriod();
        Session adhoc = Session.createAdhoc(period.id(), LocalDate.of(2026, 1, 9));

        sessionRepository.save(adhoc.skip());

        Session found = sessionRepository.findAllByPeriod(period.id()).get(0);
        assertThat(found.source()).isEqualTo(SessionSource.ADHOC);
        assertThat(found.status()).isEqualTo(SessionStatus.SKIPPED);
    }

    @Test
    void findAllByPeriodDoesNotReturnAnotherPeriodsSessions() {
        Period period = ownedPeriod();
        Period otherPeriod = ownedPeriod();
        sessionRepository.save(Session.create(period.id(), LocalDate.of(2026, 1, 6)));
        sessionRepository.save(Session.create(otherPeriod.id(), LocalDate.of(2026, 1, 7)));

        List<Session> found = sessionRepository.findAllByPeriod(period.id());

        assertThat(found).hasSize(1);
        assertThat(found.get(0).date()).isEqualTo(LocalDate.of(2026, 1, 6));
    }
}
