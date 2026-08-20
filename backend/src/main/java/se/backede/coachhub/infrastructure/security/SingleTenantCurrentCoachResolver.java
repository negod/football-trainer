package se.backede.coachhub.infrastructure.security;

import java.util.UUID;

import org.springframework.stereotype.Component;

import se.backede.coachhub.domain.model.CoachId;

/**
 * TODO(#20): delete this once Coach Accounts &amp; Authentication exists, and
 * replace it with a resolver that reads the authenticated principal from
 * {@code SecurityContextHolder}.
 *
 * <p>Until then every request is treated as the same single coach, so the
 * app is usable end to end (see epic #96's comment thread). Ownership is
 * still enforced in every use case via {@link CoachId} — swapping this
 * class out is the only change needed to move to real multi-coach auth.
 */
@Component
public class SingleTenantCurrentCoachResolver implements CurrentCoachResolver {

    private static final CoachId SINGLE_TENANT_COACH_ID =
            new CoachId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    @Override
    public CoachId resolve() {
        return SINGLE_TENANT_COACH_ID;
    }
}
