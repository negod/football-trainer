package se.backede.coachhub.application.mapper;

import se.backede.coachhub.application.dto.TeamResponse;
import se.backede.coachhub.domain.model.Team;

public final class TeamMapper {

    private TeamMapper() {
    }

    public static TeamResponse toResponse(Team team) {
        return new TeamResponse(
                team.id().value(),
                team.name(),
                team.birthYear(),
                team.genderCategory(),
                team.shorthand()
        );
    }
}
