package se.backede.coachhub.domain.repository;

import java.util.List;
import java.util.Optional;

import se.backede.coachhub.domain.model.PeriodId;
import se.backede.coachhub.domain.model.Session;
import se.backede.coachhub.domain.model.SessionId;

public interface SessionRepositoryPort {

    Session save(Session session);

    Optional<Session> findById(SessionId id);

    List<Session> findAllByPeriod(PeriodId periodId);
}
