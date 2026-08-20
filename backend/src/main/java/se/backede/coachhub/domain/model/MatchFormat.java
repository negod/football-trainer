package se.backede.coachhub.domain.model;

import java.time.Year;

/**
 * The match-format tier a {@link Period} is played in. Shared with
 * {@code Drill.level} (epic #5, issue #16) so drill content can be filtered
 * by the same tiers a team's periods are scheduled under.
 */
public enum MatchFormat {
    THREE_V_THREE,
    FIVE_V_FIVE,
    SEVEN_V_SEVEN,
    NINE_V_NINE,
    ELEVEN_V_ELEVEN;

    /**
     * A default suggested from a team's birth year using SvFF's national
     * age-to-format guideline (3v3: 6-7, 5v5: 8-9, 7v7: 10-12, 9v9: 13-15,
     * 11v11: 15-19). Always overridable by the coach — district associations
     * vary and the national table itself is revised over time.
     */
    public static MatchFormat suggestedFor(int birthYear) {
        int age = Year.now().getValue() - birthYear;
        if (age <= 7) {
            return THREE_V_THREE;
        }
        if (age <= 9) {
            return FIVE_V_FIVE;
        }
        if (age <= 12) {
            return SEVEN_V_SEVEN;
        }
        if (age <= 15) {
            return NINE_V_NINE;
        }
        return ELEVEN_V_ELEVEN;
    }
}
