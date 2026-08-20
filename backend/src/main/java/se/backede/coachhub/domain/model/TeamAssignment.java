package se.backede.coachhub.domain.model;

import java.util.List;

/**
 * A full split of present players into teams for one session, either a
 * freshly generated proposal or a previously saved assignment used as
 * pairing history input.
 */
public record TeamAssignment(List<List<PlayerId>> teams) {

    public TeamAssignment {
        teams = teams.stream().map(List::copyOf).toList();
    }
}
