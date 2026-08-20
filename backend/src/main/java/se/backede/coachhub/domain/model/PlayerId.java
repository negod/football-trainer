package se.backede.coachhub.domain.model;

import se.backede.coachhub.shared.exception.DomainValidationException;

public record PlayerId(String value) {

    public PlayerId {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("Player id must not be blank");
        }
    }
}
