package se.backede.coachhub.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import se.backede.coachhub.domain.model.PeriodId;
import se.backede.coachhub.domain.model.Session;
import se.backede.coachhub.domain.model.SessionId;
import se.backede.coachhub.domain.repository.SessionRepositoryPort;

@Component
public class JpaSessionRepositoryAdapter implements SessionRepositoryPort {

    private final SpringDataSessionRepository springDataSessionRepository;

    public JpaSessionRepositoryAdapter(SpringDataSessionRepository springDataSessionRepository) {
        this.springDataSessionRepository = springDataSessionRepository;
    }

    @Override
    public Session save(Session session) {
        SessionEntity saved = springDataSessionRepository.save(toEntity(session));
        return toDomain(saved);
    }

    @Override
    public Optional<Session> findById(SessionId id) {
        return springDataSessionRepository.findById(id.value()).map(JpaSessionRepositoryAdapter::toDomain);
    }

    @Override
    public List<Session> findAllByPeriod(PeriodId periodId) {
        return springDataSessionRepository.findAllByPeriodId(periodId.value()).stream()
                .map(JpaSessionRepositoryAdapter::toDomain)
                .toList();
    }

    private static SessionEntity toEntity(Session session) {
        return new SessionEntity(
                session.id().value(),
                session.periodId().value(),
                session.date(),
                session.status(),
                session.source()
        );
    }

    private static Session toDomain(SessionEntity entity) {
        return new Session(
                new SessionId(entity.getId()),
                new PeriodId(entity.getPeriodId()),
                entity.getDate(),
                entity.getStatus(),
                entity.getSource()
        );
    }
}
