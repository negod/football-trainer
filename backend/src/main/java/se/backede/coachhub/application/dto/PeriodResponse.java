package se.backede.coachhub.application.dto;

import java.time.LocalDate;
import java.util.UUID;

import se.backede.coachhub.domain.model.MatchFormat;

public record PeriodResponse(
        UUID id,
        UUID teamId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        MatchFormat format
) {
}
