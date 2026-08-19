package se.backede.coachhub.infrastructure.web;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(String message, List<String> details, Instant timestamp) {
}

