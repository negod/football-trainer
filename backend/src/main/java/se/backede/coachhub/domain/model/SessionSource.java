package se.backede.coachhub.domain.model;

/**
 * Whether a {@link Session} came from {@link SessionGenerationService}-style
 * recurrence generation, or was added directly as a one-off exception.
 */
public enum SessionSource {
    GENERATED,
    ADHOC
}
