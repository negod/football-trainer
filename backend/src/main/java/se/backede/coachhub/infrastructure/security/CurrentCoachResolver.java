package se.backede.coachhub.infrastructure.security;

import se.backede.coachhub.domain.model.CoachId;

/**
 * Resolves the coach making the current request. Every controller that
 * needs an ownership boundary depends on this instead of trusting a
 * client-supplied id, per docs/ai-instructions.md.
 */
public interface CurrentCoachResolver {

    CoachId resolve();
}
