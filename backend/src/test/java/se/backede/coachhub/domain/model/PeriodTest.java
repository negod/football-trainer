package se.backede.coachhub.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import se.backede.coachhub.shared.exception.DomainValidationException;

class PeriodTest {

    private static TeamId team() {
        return TeamId.newId();
    }

    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate END = LocalDate.of(2026, 6, 1);

    @Test
    void createsAValidPeriod() {
        Period period = Period.create(team(), "Spring term", START, END, MatchFormat.SEVEN_V_SEVEN);

        assertThat(period.name()).isEqualTo("Spring term");
        assertThat(period.startDate()).isEqualTo(START);
        assertThat(period.endDate()).isEqualTo(END);
        assertThat(period.format()).isEqualTo(MatchFormat.SEVEN_V_SEVEN);
    }

    @Test
    void rejectsABlankName() {
        assertThatThrownBy(() -> Period.create(team(), "  ", START, END, MatchFormat.SEVEN_V_SEVEN))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void rejectsANameLongerThan100Characters() {
        String tooLong = "x".repeat(101);

        assertThatThrownBy(() -> Period.create(team(), tooLong, START, END, MatchFormat.SEVEN_V_SEVEN))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void rejectsAnEndDateBeforeTheStartDate() {
        assertThatThrownBy(() -> Period.create(team(), "Term", END, START, MatchFormat.SEVEN_V_SEVEN))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void rejectsAnEndDateEqualToTheStartDate() {
        assertThatThrownBy(() -> Period.create(team(), "Term", START, START, MatchFormat.SEVEN_V_SEVEN))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void rejectsAMissingFormat() {
        assertThatThrownBy(() -> Period.create(team(), "Term", START, END, null))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void withDetailsReturnsAnUpdatedCopyPreservingIdentityAndTeam() {
        TeamId teamId = team();
        Period original = Period.create(teamId, "Old", START, END, MatchFormat.FIVE_V_FIVE);

        Period updated = original.withDetails("New", START.plusDays(1), END.plusDays(1), MatchFormat.SEVEN_V_SEVEN);

        assertThat(updated.id()).isEqualTo(original.id());
        assertThat(updated.teamId()).isEqualTo(teamId);
        assertThat(updated.name()).isEqualTo("New");
        assertThat(updated.startDate()).isEqualTo(START.plusDays(1));
        assertThat(updated.endDate()).isEqualTo(END.plusDays(1));
        assertThat(updated.format()).isEqualTo(MatchFormat.SEVEN_V_SEVEN);
    }

    @Test
    void belongsToTeamOnlyForItsOwnTeam() {
        TeamId teamId = team();
        TeamId otherTeamId = team();
        Period period = Period.create(teamId, "Term", START, END, MatchFormat.SEVEN_V_SEVEN);

        assertThat(period.belongsToTeam(teamId)).isTrue();
        assertThat(period.belongsToTeam(otherTeamId)).isFalse();
    }
}
