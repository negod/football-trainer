package se.backede.coachhub.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A single dated practice occasion generated for a {@link Period}, or added
 * directly as a one-off exception (see {@link SessionSource}).
 */
public record Session(SessionId id, PeriodId periodId, LocalDate date, SessionStatus status, SessionSource source) {

    public Session {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(periodId, "periodId must not be null");
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(source, "source must not be null");
    }

    public static Session create(PeriodId periodId, LocalDate date) {
        return new Session(SessionId.newId(), periodId, date, SessionStatus.SCHEDULED, SessionSource.GENERATED);
    }

    /** An extra one-off session added directly, not from recurrence generation. */
    public static Session createAdhoc(PeriodId periodId, LocalDate date) {
        return new Session(SessionId.newId(), periodId, date, SessionStatus.SCHEDULED, SessionSource.ADHOC);
    }

    /** Excludes this session from the active schedule without deleting it. */
    public Session skip() {
        return new Session(id, periodId, date, SessionStatus.SKIPPED, source);
    }

    /** Reverses {@link #skip()}. */
    public Session restore() {
        return new Session(id, periodId, date, SessionStatus.SCHEDULED, source);
    }

    /** Moves this occurrence to a new date without affecting other sessions. */
    public Session reschedule(LocalDate newDate) {
        Objects.requireNonNull(newDate, "date must not be null");
        return new Session(id, periodId, newDate, status, source);
    }

    public boolean belongsToPeriod(PeriodId candidatePeriodId) {
        return periodId.equals(candidatePeriodId);
    }
}
