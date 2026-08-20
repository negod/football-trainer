package se.backede.coachhub.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import se.backede.coachhub.domain.model.Period;
import se.backede.coachhub.domain.model.PeriodId;
import se.backede.coachhub.domain.model.TeamId;
import se.backede.coachhub.domain.repository.PeriodRepositoryPort;

@Component
public class JpaPeriodRepositoryAdapter implements PeriodRepositoryPort {

    private final SpringDataPeriodRepository springDataPeriodRepository;

    public JpaPeriodRepositoryAdapter(SpringDataPeriodRepository springDataPeriodRepository) {
        this.springDataPeriodRepository = springDataPeriodRepository;
    }

    @Override
    public Period save(Period period) {
        PeriodEntity saved = springDataPeriodRepository.save(toEntity(period));
        return toDomain(saved);
    }

    @Override
    public Optional<Period> findById(PeriodId id) {
        return springDataPeriodRepository.findById(id.value()).map(JpaPeriodRepositoryAdapter::toDomain);
    }

    @Override
    public List<Period> findAllByTeam(TeamId teamId) {
        return springDataPeriodRepository.findAllByTeamId(teamId.value()).stream()
                .map(JpaPeriodRepositoryAdapter::toDomain)
                .toList();
    }

    private static PeriodEntity toEntity(Period period) {
        return new PeriodEntity(
                period.id().value(),
                period.teamId().value(),
                period.name(),
                period.startDate(),
                period.endDate(),
                period.format()
        );
    }

    private static Period toDomain(PeriodEntity entity) {
        return new Period(
                new PeriodId(entity.getId()),
                new TeamId(entity.getTeamId()),
                entity.getName(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getFormat()
        );
    }
}
