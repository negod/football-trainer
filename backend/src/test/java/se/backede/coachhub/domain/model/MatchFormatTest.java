package se.backede.coachhub.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Year;

import org.junit.jupiter.api.Test;

class MatchFormatTest {

    private static int birthYearForAge(int age) {
        return Year.now().getValue() - age;
    }

    @Test
    void suggestsThreeVThreeForSixAndSevenYearOlds() {
        assertThat(MatchFormat.suggestedFor(birthYearForAge(6))).isEqualTo(MatchFormat.THREE_V_THREE);
        assertThat(MatchFormat.suggestedFor(birthYearForAge(7))).isEqualTo(MatchFormat.THREE_V_THREE);
    }

    @Test
    void suggestsFiveVFiveForEightAndNineYearOlds() {
        assertThat(MatchFormat.suggestedFor(birthYearForAge(8))).isEqualTo(MatchFormat.FIVE_V_FIVE);
        assertThat(MatchFormat.suggestedFor(birthYearForAge(9))).isEqualTo(MatchFormat.FIVE_V_FIVE);
    }

    @Test
    void suggestsSevenVSevenForTenToTwelveYearOlds() {
        assertThat(MatchFormat.suggestedFor(birthYearForAge(10))).isEqualTo(MatchFormat.SEVEN_V_SEVEN);
        assertThat(MatchFormat.suggestedFor(birthYearForAge(12))).isEqualTo(MatchFormat.SEVEN_V_SEVEN);
    }

    @Test
    void suggestsNineVNineForThirteenToFifteenYearOlds() {
        assertThat(MatchFormat.suggestedFor(birthYearForAge(13))).isEqualTo(MatchFormat.NINE_V_NINE);
        assertThat(MatchFormat.suggestedFor(birthYearForAge(15))).isEqualTo(MatchFormat.NINE_V_NINE);
    }

    @Test
    void suggestsElevenVElevenForOverFifteenYearOlds() {
        assertThat(MatchFormat.suggestedFor(birthYearForAge(16))).isEqualTo(MatchFormat.ELEVEN_V_ELEVEN);
        assertThat(MatchFormat.suggestedFor(birthYearForAge(19))).isEqualTo(MatchFormat.ELEVEN_V_ELEVEN);
    }
}
