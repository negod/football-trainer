package se.backede.coachhub.application.dto;

import java.time.LocalDate;
import java.util.UUID;

import se.backede.coachhub.domain.model.SessionSource;
import se.backede.coachhub.domain.model.SessionStatus;

public record SessionResponse(UUID id, UUID periodId, LocalDate date, SessionStatus status, SessionSource source) {
}
