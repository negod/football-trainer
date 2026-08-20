package se.backede.coachhub.domain.model;

import java.time.Year;
import java.util.Objects;

import se.backede.coachhub.shared.exception.DomainValidationException;

/**
 * A coach's team: its stable identity (name, birth year, gender category).
 * Match format deliberately isn't stored here since it changes season to
 * season as the cohort ages — that belongs on {@code Period} instead.
 */
public record Team(TeamId id, CoachId ownerId, String name, int birthYear, GenderCategory genderCategory) {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_TEAM_AGE_YEARS = 100;

    public Team {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(ownerId, "ownerId must not be null");
        Objects.requireNonNull(genderCategory, "genderCategory must not be null");
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("Team name must not be blank");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new DomainValidationException("Team name must be at most " + MAX_NAME_LENGTH + " characters");
        }
        int currentYear = Year.now().getValue();
        if (birthYear > currentYear || birthYear < currentYear - MAX_TEAM_AGE_YEARS) {
            throw new DomainValidationException("Team birth year must be a plausible year, not in the future");
        }
    }

    public static Team create(CoachId ownerId, String name, int birthYear, GenderCategory genderCategory) {
        return new Team(TeamId.newId(), ownerId, name, birthYear, genderCategory);
    }

    public Team withDetails(String newName, int newBirthYear, GenderCategory newGenderCategory) {
        return new Team(id, ownerId, newName, newBirthYear, newGenderCategory);
    }

    public boolean isOwnedBy(CoachId coachId) {
        return ownerId.equals(coachId);
    }

    /** The Swedish shorthand for the team, e.g. "P19" for boys born in 2019. */
    public String shorthand() {
        String prefix = switch (genderCategory) {
            case BOYS -> "P";
            case GIRLS -> "F";
            case MIXED -> "P/F";
        };
        int twoDigitYear = birthYear % 100;
        return prefix + String.format("%02d", twoDigitYear);
    }
}
