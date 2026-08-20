package se.backede.coachhub.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A single dated practice occasion generated for a {@link Period}. #45
 * extends this with a {@code source} (generated vs. ad-hoc) once the
 * schema change it requires is in scope.
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

    /** Excludes this session from the active schedule without deleting it. */
    public Session skip() {
        return new Session(id, periodId, date, SessionStatus.SKIPPED);
    }

    /** Reverses {@link #skip()}. */
    public Session restore() {
        return new Session(id, periodId, date, SessionStatus.SCHEDULED);
    }

    /** Moves this occurrence to a new date without affecting other sessions. */
    public Session reschedule(LocalDate newDate) {
        Objects.requireNonNull(newDate, "date must not be null");
        return new Session(id, periodId, newDate, status);
    }

    public boolean belongsToPeriod(PeriodId candidatePeriodId) {
        return periodId.equals(candidatePeriodId);
    }
}
