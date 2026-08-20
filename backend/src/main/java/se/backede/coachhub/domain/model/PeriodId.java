package se.backede.coachhub.domain.model;

import java.util.Objects;
import java.util.UUID;

public record PeriodId(UUID value) {

    public PeriodId {
        Objects.requireNonNull(value, "period id must not be null");
    }

    public static PeriodId newId() {
        return new PeriodId(UUID.randomUUID());
    }
}
