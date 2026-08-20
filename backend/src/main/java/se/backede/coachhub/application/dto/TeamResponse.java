package se.backede.coachhub.application.dto;

import java.util.UUID;

import se.backede.coachhub.domain.model.GenderCategory;

public record TeamResponse(
        UUID id,
        String name,
        int birthYear,
        GenderCategory genderCategory,
        String shorthand
) {
}
