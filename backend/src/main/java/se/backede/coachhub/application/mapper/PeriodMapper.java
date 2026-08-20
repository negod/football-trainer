package se.backede.coachhub.application.mapper;

import se.backede.coachhub.application.dto.PeriodResponse;
import se.backede.coachhub.domain.model.Period;

public final class PeriodMapper {

    private PeriodMapper() {
    }

    public static PeriodResponse toResponse(Period period) {
        return new PeriodResponse(
                period.id().value(),
                period.teamId().value(),
                period.name(),
                period.startDate(),
                period.endDate(),
                period.format()
        );
    }
}
