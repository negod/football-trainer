package se.backede.coachhub.application.dto;

import java.time.LocalDate;

import se.backede.coachhub.domain.model.SessionStatus;

/**
 * Both fields are optional: a null {@code status} leaves status unchanged,
 * a null {@code date} leaves the date unchanged. Backs a single
 * PATCH-for-skip/restore/reschedule endpoint (added in #45) rather than
 * three separate ones.
 */
public record UpdateSessionRequest(SessionStatus status, LocalDate date) {
}
