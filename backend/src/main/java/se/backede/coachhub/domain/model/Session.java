package se.backede.coachhub.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A single dated practice occasion generated for a {@link Period}. Feature
 * #10 (issue #44) extends this with a {@code source} (generated vs.
 * ad-hoc) and adds the {@code SKIPPED} status.
 */
public record Session(SessionId id, PeriodId periodId, LocalDate date, SessionStatus status) {

    public Session {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(periodId, "periodId must not be null");
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }

    public static Session create(PeriodId periodId, LocalDate date) {
        return new Session(SessionId.newId(), periodId, date, SessionStatus.SCHEDULED);
    }

    public boolean belongsToPeriod(PeriodId candidatePeriodId) {
        return periodId.equals(candidatePeriodId);
    }
}
