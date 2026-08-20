package se.backede.coachhub.domain.model;

import java.util.Objects;
import java.util.UUID;

public record TeamId(UUID value) {

    public TeamId {
        Objects.requireNonNull(value, "team id must not be null");
    }

    public static TeamId newId() {
        return new TeamId(UUID.randomUUID());
    }
}
