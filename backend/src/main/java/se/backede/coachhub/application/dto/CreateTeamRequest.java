package se.backede.coachhub.application.dto;

import se.backede.coachhub.domain.model.GenderCategory;

public record CreateTeamRequest(String name, int birthYear, GenderCategory genderCategory) {
}
