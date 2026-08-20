package se.backede.coachhub.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Year;

import org.junit.jupiter.api.Test;

import se.backede.coachhub.shared.exception.DomainValidationException;

class PlayerTest {

    private static TeamId team() {
        return TeamId.newId();
    }

    @Test
    void createsAValidPlayer() {
        Player player = Player.create(team(), "Alex Andersson", 2015, "Forward");

        assertThat(player.name()).isEqualTo("Alex Andersson");
        assertThat(player.birthYear()).isEqualTo(2015);
        assertThat(player.position()).isEqualTo("Forward");
    }

    @Test
    void createsAValidPlayerWithoutAPosition() {
        Player player = Player.create(team(), "Alex Andersson", 2015, null);

        assertThat(player.position()).isNull();
    }

    @Test
    void treatsABlankPositionAsAbsent() {
        Player player = Player.create(team(), "Alex Andersson", 2015, "   ");

        assertThat(player.position()).isNull();
    }

    @Test
    void trimsAPosition() {
        Player player = Player.create(team(), "Alex Andersson", 2015, "  Forward  ");

        assertThat(player.position()).isEqualTo("Forward");
    }

    @Test
    void rejectsABlankName() {
        assertThatThrownBy(() -> Player.create(team(), "  ", 2015, null))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void rejectsANameLongerThan100Characters() {
        String tooLong = "x".repeat(101);

        assertThatThrownBy(() -> Player.create(team(), tooLong, 2015, null))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void rejectsAPositionLongerThan50Characters() {
        String tooLong = "x".repeat(51);

        assertThatThrownBy(() -> Player.create(team(), "Alex Andersson", 2015, tooLong))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void rejectsABirthYearInTheFuture() {
        int nextYear = Year.now().getValue() + 1;

        assertThatThrownBy(() -> Player.create(team(), "Alex Andersson", nextYear, null))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void rejectsAnImplausiblyOldBirthYear() {
        assertThatThrownBy(() -> Player.create(team(), "Alex Andersson", 1800, null))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void withDetailsReturnsAnUpdatedCopyPreservingIdentityAndTeam() {
        TeamId teamId = team();
        Player original = Player.create(teamId, "Old name", 2014, "Defender");

        Player updated = original.withDetails("New name", 2015, "Midfielder");

        assertThat(updated.id()).isEqualTo(original.id());
        assertThat(updated.teamId()).isEqualTo(teamId);
        assertThat(updated.name()).isEqualTo("New name");
        assertThat(updated.birthYear()).isEqualTo(2015);
        assertThat(updated.position()).isEqualTo("Midfielder");
    }

    @Test
    void belongsToTeamOnlyForItsOwnTeam() {
        TeamId teamId = team();
        TeamId otherTeamId = team();
        Player player = Player.create(teamId, "Alex Andersson", 2015, null);

        assertThat(player.belongsToTeam(teamId)).isTrue();
        assertThat(player.belongsToTeam(otherTeamId)).isFalse();
    }
}
