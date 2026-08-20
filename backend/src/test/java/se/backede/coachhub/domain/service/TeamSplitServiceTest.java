package se.backede.coachhub.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.backede.coachhub.domain.model.PlayerId;
import se.backede.coachhub.domain.model.TeamAssignment;
import se.backede.coachhub.shared.exception.DomainValidationException;

class TeamSplitServiceTest {

    private final TeamSplitService service = new TeamSplitService();

    private static PlayerId player(String id) {
        return new PlayerId(id);
    }

    @Test
    void splitsEvenlyWhenThereIsNoHistory() {
        List<PlayerId> present = List.of(player("a"), player("b"), player("c"), player("d"));
        PairingHistory history = PairingHistory.from(List.of());

        TeamAssignment result = service.split(present, 2, history);

        assertThat(result.teams()).hasSize(2);
        assertThat(result.teams()).allSatisfy(team -> assertThat(team).hasSize(2));
        assertThat(result.teams().stream().flatMap(List::stream).distinct().toList()).hasSize(4);
    }

    @Test
    void distributesAnOddNumberOfPlayersWithSizeDifferenceOfAtMostOne() {
        List<PlayerId> present = List.of(player("a"), player("b"), player("c"), player("d"), player("e"));
        PairingHistory history = PairingHistory.from(List.of());

        TeamAssignment result = service.split(present, 2, history);

        List<Integer> sizes = result.teams().stream().map(List::size).toList();
        assertThat(sizes).containsExactlyInAnyOrder(3, 2);
        assertThat(result.teams().stream().flatMap(List::stream).distinct().toList()).hasSize(5);
    }

    @Test
    void separatesPlayersWhoHaveAlreadyPlayedTogetherWhenAnAlternativeExists() {
        PlayerId a = player("a");
        PlayerId b = player("b");
        PlayerId c = player("c");
        PlayerId d = player("d");

        TeamAssignment previousSession = new TeamAssignment(List.of(List.of(a, b), List.of(c, d)));
        TeamAssignment anotherPreviousSession = new TeamAssignment(List.of(List.of(a, b), List.of(c, d)));
        PairingHistory history = PairingHistory.from(List.of(previousSession, anotherPreviousSession));

        TeamAssignment result = service.split(List.of(a, b, c, d), 2, history);

        boolean aAndBTogether = result.teams().stream().anyMatch(team -> team.contains(a) && team.contains(b));
        assertThat(aAndBTogether).isFalse();
    }

    @Test
    void everyPlayerIsAssignedToExactlyOneTeam() {
        List<PlayerId> present = List.of(player("a"), player("b"), player("c"), player("d"), player("e"), player("f"));
        PairingHistory history = PairingHistory.from(List.of());

        TeamAssignment result = service.split(present, 3, history);

        List<PlayerId> allAssigned = result.teams().stream().flatMap(List::stream).toList();
        assertThat(allAssigned).hasSize(present.size());
        assertThat(allAssigned).containsExactlyInAnyOrderElementsOf(present);
    }

    @Test
    void rejectsFewerThanTwoPresentPlayers() {
        PairingHistory history = PairingHistory.from(List.of());

        assertThatThrownBy(() -> service.split(List.of(player("a")), 2, history))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void rejectsDuplicatePresentPlayers() {
        PairingHistory history = PairingHistory.from(List.of());
        PlayerId a = player("a");

        assertThatThrownBy(() -> service.split(List.of(a, a, player("b")), 2, history))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void rejectsTeamCountBelowTwo() {
        PairingHistory history = PairingHistory.from(List.of());
        List<PlayerId> present = List.of(player("a"), player("b"));

        assertThatThrownBy(() -> service.split(present, 1, history))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void rejectsTeamCountAboveNumberOfPresentPlayers() {
        PairingHistory history = PairingHistory.from(List.of());
        List<PlayerId> present = List.of(player("a"), player("b"));

        assertThatThrownBy(() -> service.split(present, 3, history))
                .isInstanceOf(DomainValidationException.class);
    }
}
