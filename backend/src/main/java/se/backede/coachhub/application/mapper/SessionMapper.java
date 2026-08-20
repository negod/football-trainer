package se.backede.coachhub.application.mapper;

import se.backede.coachhub.application.dto.SessionResponse;
import se.backede.coachhub.domain.model.Session;

public final class SessionMapper {

    private SessionMapper() {
    }

    public static SessionResponse toResponse(Session session) {
        return new SessionResponse(
                session.id().value(),
                session.periodId().value(),
                session.date(),
                session.status()
        );
    }
}
