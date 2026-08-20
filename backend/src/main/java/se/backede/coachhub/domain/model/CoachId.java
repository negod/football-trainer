package se.backede.coachhub.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * The authenticated coach a private resource is owned by. Coach accounts
 * themselves (epic #20) don't exist yet; this is the stable identity type
 * every ownership boundary in the domain references in the meantime.
 */
public record CoachId(UUID value) {

    public CoachId {
        Objects.requireNonNull(value, "coach id must not be null");
    }
}
