package se.backede.coachhub.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Year;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.backede.coachhub.shared.exception.DomainValidationException;

class TeamTest {

    private static CoachId coach() {
        return new CoachId(UUID.randomUUID());
    }

    @Test
    void createsAValidTeam() {
        Team team = Team.create(coach(), "IFK Testby P19", 2019, GenderCategory.BOYS);

        assertThat(team.name()).isEqualTo("IFK Testby P19");
        assertThat(team.birthYear()).isEqualTo(2019);
        assertThat(team.genderCategory()).isEqualTo(GenderCategory.BOYS);
    }

    @Test
    void rejectsABlankName() {
        assertThatThrownBy(() -> Team.create(coach(), "  ", 2019, GenderCategory.BOYS))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void rejectsANameLongerThan100Characters() {
        String tooLong = "x".repeat(101);

        assertThatThrownBy(() -> Team.create(coach(), tooLong, 2019, GenderCategory.BOYS))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void rejectsABirthYearInTheFuture() {
        int nextYear = Year.now().getValue() + 1;

        assertThatThrownBy(() -> Team.create(coach(), "Team", nextYear, GenderCategory.BOYS))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void rejectsAnImplausiblyOldBirthYear() {
        assertThatThrownBy(() -> Team.create(coach(), "Team", 1800, GenderCategory.BOYS))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void computesTheSwedishShorthand() {
        assertThat(Team.create(coach(), "Team", 2019, GenderCategory.BOYS).shorthand()).isEqualTo("P19");
        assertThat(Team.create(coach(), "Team", 2019, GenderCategory.GIRLS).shorthand()).isEqualTo("F19");
        assertThat(Team.create(coach(), "Team", 2019, GenderCategory.MIXED).shorthand()).isEqualTo("P/F19");
    }

    @Test
    void isOwnedByOnlyItsOwner() {
        CoachId owner = coach();
        CoachId other = coach();
        Team team = Team.create(owner, "Team", 2019, GenderCategory.BOYS);

        assertThat(team.isOwnedBy(owner)).isTrue();
        assertThat(team.isOwnedBy(other)).isFalse();
    }

    @Test
    void withDetailsReturnsAnUpdatedCopyPreservingIdentityAndOwner() {
        CoachId owner = coach();
        Team original = Team.create(owner, "Old name", 2018, GenderCategory.GIRLS);

        Team updated = original.withDetails("New name", 2019, GenderCategory.MIXED);

        assertThat(updated.id()).isEqualTo(original.id());
        assertThat(updated.ownerId()).isEqualTo(owner);
        assertThat(updated.name()).isEqualTo("New name");
        assertThat(updated.birthYear()).isEqualTo(2019);
        assertThat(updated.genderCategory()).isEqualTo(GenderCategory.MIXED);
    }
}
