package se.backede.coachhub.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import se.backede.coachhub.shared.exception.DomainValidationException;

class SessionGenerationServiceTest {

    @Test
    void generatesOnlyDatesOnTheSelectedWeekdaysAcrossALeapYearMonthBoundary() {
        // Feb 1 2024 is a Thursday; Feb 29 2024 is the leap day (a Thursday, not selected).
        LocalDate start = LocalDate.of(2024, 2, 1);
        LocalDate end = LocalDate.of(2024, 3, 1);

        List<LocalDate> dates = SessionGenerationService.generateDates(start, end, Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));

        assertThat(dates).containsExactly(
                LocalDate.of(2024, 2, 5),
                LocalDate.of(2024, 2, 7),
                LocalDate.of(2024, 2, 12),
                LocalDate.of(2024, 2, 14),
                LocalDate.of(2024, 2, 19),
                LocalDate.of(2024, 2, 21),
                LocalDate.of(2024, 2, 26),
                LocalDate.of(2024, 2, 28)
        );
    }

    @Test
    void includesBothEndpointsWhenTheyFallOnASelectedWeekday() {
        LocalDate start = LocalDate.of(2026, 1, 6); // a Tuesday
        LocalDate end = LocalDate.of(2026, 1, 13); // the following Tuesday

        List<LocalDate> dates = SessionGenerationService.generateDates(start, end, Set.of(DayOfWeek.TUESDAY));

        assertThat(dates).containsExactly(start, end);
    }

    @Test
    void producesNoDatesWhenNoWeekdayInTheRangeMatches() {
        LocalDate start = LocalDate.of(2026, 1, 6);
        LocalDate end = LocalDate.of(2026, 1, 6);

        List<LocalDate> dates = SessionGenerationService.generateDates(start, end, Set.of(DayOfWeek.SUNDAY));

        assertThat(dates).isEmpty();
    }

    @Test
    void isDeterministicForTheSameInputs() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 6, 1);
        Set<DayOfWeek> weekdays = EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY);

        List<LocalDate> first = SessionGenerationService.generateDates(start, end, weekdays);
        List<LocalDate> second = SessionGenerationService.generateDates(start, end, weekdays);

        assertThat(first).isEqualTo(second);
    }

    @Test
    void rejectsAnEmptyWeekdaySelection() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 6, 1);

        assertThatThrownBy(() -> SessionGenerationService.generateDates(start, end, Set.of()))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void rejectsANullWeekdaySelection() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 6, 1);

        assertThatThrownBy(() -> SessionGenerationService.generateDates(start, end, null))
                .isInstanceOf(DomainValidationException.class);
    }
}
