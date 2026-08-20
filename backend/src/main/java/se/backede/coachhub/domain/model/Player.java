package se.backede.coachhub.domain.model;

import java.time.Year;
import java.util.Objects;

import se.backede.coachhub.shared.exception.DomainValidationException;

/**
 * A player on a coach's team roster. Position is deliberately free text
 * (not an enum) since terminology varies by club and age group.
 */
public record Player(PlayerId id, TeamId teamId, String name, int birthYear, String position) {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_POSITION_LENGTH = 50;
    private static final int MAX_PLAYER_AGE_YEARS = 100;

    public Player {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(teamId, "teamId must not be null");
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("Player name must not be blank");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new DomainValidationException("Player name must be at most " + MAX_NAME_LENGTH + " characters");
        }
        int currentYear = Year.now().getValue();
        if (birthYear > currentYear || birthYear < currentYear - MAX_PLAYER_AGE_YEARS) {
            throw new DomainValidationException("Player birth year must be a plausible year, not in the future");
        }
        if (position != null) {
            position = position.isBlank() ? null : position.trim();
        }
        if (position != null && position.length() > MAX_POSITION_LENGTH) {
            throw new DomainValidationException("Player position must be at most " + MAX_POSITION_LENGTH + " characters");
        }
    }

    public static Player create(TeamId teamId, String name, int birthYear, String position) {
        return new Player(PlayerId.newId(), teamId, name, birthYear, position);
    }

    public Player withDetails(String newName, int newBirthYear, String newPosition) {
        return new Player(id, teamId, newName, newBirthYear, newPosition);
    }

    public boolean belongsToTeam(TeamId candidateTeamId) {
        return teamId.equals(candidateTeamId);
    }
}
