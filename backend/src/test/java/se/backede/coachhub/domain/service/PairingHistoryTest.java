package se.backede.coachhub.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.backede.coachhub.domain.model.PlayerId;
import se.backede.coachhub.domain.model.TeamAssignment;

class PairingHistoryTest {

    private static PlayerId player(String id) {
        return new PlayerId(id);
    }

    @Test
    void countsAreZeroWhenThereAreNoPastAssignments() {
        PairingHistory history = PairingHistory.from(List.of());

        assertThat(history.pairCount(player("a"), player("b"))).isZero();
    }

    @Test
    void countsHowManyTimesTwoPlayersSharedATeam() {
        PlayerId a = player("a");
        PlayerId b = player("b");
        PlayerId c = player("c");

        TeamAssignment first = new TeamAssignment(List.of(List.of(a, b), List.of(c)));
        TeamAssignment second = new TeamAssignment(List.of(List.of(a, b, c)));

        PairingHistory history = PairingHistory.from(List.of(first, second));

        assertThat(history.pairCount(a, b)).isEqualTo(2);
        assertThat(history.pairCount(a, c)).isEqualTo(1);
        assertThat(history.pairCount(b, c)).isEqualTo(1);
    }

    @Test
    void isSymmetric() {
        PlayerId a = player("a");
        PlayerId b = player("b");
        TeamAssignment assignment = new TeamAssignment(List.of(List.of(a, b)));

        PairingHistory history = PairingHistory.from(List.of(assignment));

        assertThat(history.pairCount(a, b)).isEqualTo(history.pairCount(b, a));
    }

    @Test
    void doesNotCountPlayersOnDifferentTeamsAsAPair() {
        PlayerId a = player("a");
        PlayerId b = player("b");
        TeamAssignment assignment = new TeamAssignment(List.of(List.of(a), List.of(b)));

        PairingHistory history = PairingHistory.from(List.of(assignment));

        assertThat(history.pairCount(a, b)).isZero();
    }
}
