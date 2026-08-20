package se.backede.coachhub.application.dto;

public record PlayerResponse(
        String id,
        String teamId,
        String name,
        int birthYear,
        String position
) {
}
