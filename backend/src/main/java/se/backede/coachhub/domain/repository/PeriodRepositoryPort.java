package se.backede.coachhub.domain.repository;

import java.util.List;
import java.util.Optional;

import se.backede.coachhub.domain.model.Period;
import se.backede.coachhub.domain.model.PeriodId;
import se.backede.coachhub.domain.model.TeamId;

public interface PeriodRepositoryPort {

    Period save(Period period);

    Optional<Period> findById(PeriodId id);

    List<Period> findAllByTeam(TeamId teamId);
}
