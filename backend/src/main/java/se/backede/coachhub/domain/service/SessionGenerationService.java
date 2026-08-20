package se.backede.coachhub.domain.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import se.backede.coachhub.shared.exception.DomainValidationException;

/**
 * The pure recurrence algorithm behind feature #9: which dates within a
 * period's range fall on one of the selected weekdays. Deterministic and
 * side-effect free — idempotency (not re-creating a {@code Session} for a
 * date that already has one) is the use case layer's job, not this one's.
 */
public final class SessionGenerationService {

    private SessionGenerationService() {
    }

    public static List<LocalDate> generateDates(LocalDate startDate, LocalDate endDate, Set<DayOfWeek> weekdays) {
        if (weekdays == null || weekdays.isEmpty()) {
            throw new DomainValidationException("At least one weekday must be selected");
        }
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate cursor = startDate; !cursor.isAfter(endDate); cursor = cursor.plusDays(1)) {
            if (weekdays.contains(cursor.getDayOfWeek())) {
                dates.add(cursor);
            }
        }
        return dates;
    }
}
