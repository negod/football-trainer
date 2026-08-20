package se.backede.coachhub.domain.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.backede.coachhub.domain.model.PlayerId;
import se.backede.coachhub.domain.model.TeamAssignment;

/**
 * How many times each pair of players has previously shared a team, derived
 * from a season's worth of saved {@link TeamAssignment}s.
 */
public final class PairingHistory {

    private final Map<PairKey, Integer> counts;

    private PairingHistory(Map<PairKey, Integer> counts) {
        this.counts = counts;
    }

    public static PairingHistory from(List<TeamAssignment> pastAssignments) {
        Map<PairKey, Integer> counts = new HashMap<>();
        for (TeamAssignment assignment : pastAssignments) {
            for (List<PlayerId> team : assignment.teams()) {
                for (int i = 0; i < team.size(); i++) {
                    for (int j = i + 1; j < team.size(); j++) {
                        PairKey key = PairKey.of(team.get(i), team.get(j));
                        counts.merge(key, 1, Integer::sum);
                    }
                }
            }
        }
        return new PairingHistory(counts);
    }

    public int pairCount(PlayerId a, PlayerId b) {
        return counts.getOrDefault(PairKey.of(a, b), 0);
    }

    private record PairKey(PlayerId first, PlayerId second) {

        static PairKey of(PlayerId a, PlayerId b) {
            return a.value().compareTo(b.value()) <= 0 ? new PairKey(a, b) : new PairKey(b, a);
        }
    }
}
