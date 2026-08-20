package se.backede.coachhub.application.dto;

import java.time.LocalDate;

import se.backede.coachhub.domain.model.MatchFormat;

public record CreatePeriodRequest(String name, LocalDate startDate, LocalDate endDate, MatchFormat format) {
}
