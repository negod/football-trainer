package se.backede.coachhub.domain.model;

/**
 * Only {@code SCHEDULED} exists yet. {@code SKIPPED} (and the separate
 * {@code source} concept: generated vs. ad-hoc) are added by feature #10
 * (issue #44) — kept out until that feature actually needs them.
 */
public enum SessionStatus {
    SCHEDULED
}
