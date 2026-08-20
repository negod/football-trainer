package se.backede.coachhub.domain.repository;

import java.util.List;

import se.backede.coachhub.domain.model.PeriodId;
import se.backede.coachhub.domain.model.Session;

public interface SessionRepositoryPort {

    Session save(Session session);

    List<Session> findAllByPeriod(PeriodId periodId);
}
