package se.backede.coachhub.application.mapper;

import se.backede.coachhub.application.dto.PlayerResponse;
import se.backede.coachhub.domain.model.Player;

public final class PlayerMapper {

    private PlayerMapper() {
    }

    public static PlayerResponse toResponse(Player player) {
        return new PlayerResponse(
                player.id().value(),
                player.teamId().value().toString(),
                player.name(),
                player.birthYear(),
                player.position()
        );
    }
}
