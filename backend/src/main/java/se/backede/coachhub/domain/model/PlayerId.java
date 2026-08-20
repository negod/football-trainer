package se.backede.coachhub.domain.model;

import java.util.UUID;

import se.backede.coachhub.shared.exception.DomainValidationException;

public record PlayerId(String value) {

    public PlayerId {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("Player id must not be blank");
        }
    }

    public static PlayerId newId() {
        return new PlayerId(UUID.randomUUID().toString());
    }
}
