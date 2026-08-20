package se.backede.coachhub.domain.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import se.backede.coachhub.domain.model.PlayerId;
import se.backede.coachhub.domain.model.TeamAssignment;
import se.backede.coachhub.shared.exception.DomainValidationException;

/**
 * Splits the players present at a session into balanced teams, weighted to
 * avoid repeating pairings that {@link PairingHistory} shows have already
 * happened this season.
 *
 * <p>Uses a greedy heuristic, not an optimal solution: players are placed
 * one at a time, in a deterministic order, into whichever non-full team
 * currently minimizes the summed pairing history with that team's existing
 * members.
 */
public final class TeamSplitService {

    public TeamAssignment split(List<PlayerId> presentPlayers, int teamCount, PairingHistory history) {
        validate(presentPlayers, teamCount);

        List<PlayerId> ordered = presentPlayers.stream()
                .sorted(Comparator.comparing(PlayerId::value))
                .toList();

        int[] capacity = teamCapacities(ordered.size(), teamCount);

        List<List<PlayerId>> teams = new ArrayList<>();
        for (int i = 0; i < teamCount; i++) {
            teams.add(new ArrayList<>());
        }

        for (PlayerId player : ordered) {
            int bestTeam = pickTeam(player, teams, capacity, history);
            teams.get(bestTeam).add(player);
        }

        return new TeamAssignment(teams.stream().map(List::copyOf).toList());
    }

    private int pickTeam(PlayerId player, List<List<PlayerId>> teams, int[] capacity, PairingHistory history) {
        int bestTeam = -1;
        int bestCost = Integer.MAX_VALUE;
        for (int i = 0; i < teams.size(); i++) {
            List<PlayerId> team = teams.get(i);
            if (team.size() >= capacity[i]) {
                continue;
            }
            int cost = 0;
            for (PlayerId member : team) {
                cost += history.pairCount(player, member);
            }
            if (cost < bestCost) {
                bestCost = cost;
                bestTeam = i;
            }
        }
        return bestTeam;
    }

    private int[] teamCapacities(int totalPlayers, int teamCount) {
        int baseSize = totalPlayers / teamCount;
        int remainder = totalPlayers % teamCount;
        int[] capacity = new int[teamCount];
        for (int i = 0; i < teamCount; i++) {
            capacity[i] = baseSize + (i < remainder ? 1 : 0);
        }
        return capacity;
    }

    private void validate(List<PlayerId> presentPlayers, int teamCount) {
        if (presentPlayers == null || presentPlayers.size() < 2) {
            throw new DomainValidationException("At least two present players are required to split into teams");
        }
        if (Set.copyOf(presentPlayers).size() != presentPlayers.size()) {
            throw new DomainValidationException("Present players must not contain duplicates");
        }
        if (teamCount < 2) {
            throw new DomainValidationException("Team count must be at least 2");
        }
        if (teamCount > presentPlayers.size()) {
            throw new DomainValidationException("Team count must not exceed the number of present players");
        }
    }
}
