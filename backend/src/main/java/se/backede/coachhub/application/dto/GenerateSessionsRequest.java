package se.backede.coachhub.application.dto;

import java.time.DayOfWeek;
import java.util.Set;

public record GenerateSessionsRequest(Set<DayOfWeek> weekdays) {
}
