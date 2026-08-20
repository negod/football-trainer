package se.backede.coachhub.domain.model;

/**
 * {@code SKIPPED} (issue #44) excludes a session from the active schedule
 * without deleting it — reversible via {@link Session#restore()}. The
 * separate {@code source} concept (generated vs. ad-hoc) is added by #45
 * alongside the schema change it requires.
 */
public enum SessionStatus {
    SCHEDULED,
    SKIPPED
}
