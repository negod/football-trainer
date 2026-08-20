package se.backede.coachhub.domain.model;

import java.time.LocalDate;
import java.util.Objects;

import se.backede.coachhub.shared.exception.DomainValidationException;

/**
 * A coach's season/term for one team: a date range and the match format
 * played during it. Format is deliberately not inherited from {@link Team}
 * — it changes as the cohort ages and must be chosen (with a suggested
 * default, see {@link MatchFormat#suggestedFor(int)}) per period.
 */
public record Period(PeriodId id, TeamId teamId, String name, LocalDate startDate, LocalDate endDate, MatchFormat format) {

    private static final int MAX_NAME_LENGTH = 100;

    public Period {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(teamId, "teamId must not be null");
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("Period name must not be blank");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new DomainValidationException("Period name must be at most " + MAX_NAME_LENGTH + " characters");
        }
        Objects.requireNonNull(startDate, "startDate must not be null");
        Objects.requireNonNull(endDate, "endDate must not be null");
        if (!endDate.isAfter(startDate)) {
            throw new DomainValidationException("Period end date must be after the start date");
        }
        if (format == null) {
            throw new DomainValidationException("Period format must be provided");
        }
    }

    public static Period create(TeamId teamId, String name, LocalDate startDate, LocalDate endDate, MatchFormat format) {
        return new Period(PeriodId.newId(), teamId, name, startDate, endDate, format);
    }

    public Period withDetails(String newName, LocalDate newStartDate, LocalDate newEndDate, MatchFormat newFormat) {
        return new Period(id, teamId, newName, newStartDate, newEndDate, newFormat);
    }

    public boolean belongsToTeam(TeamId candidateTeamId) {
        return teamId.equals(candidateTeamId);
    }
}
