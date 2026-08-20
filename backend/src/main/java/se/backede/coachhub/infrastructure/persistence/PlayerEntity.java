package se.backede.coachhub.infrastructure.persistence;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "player")
public class PlayerEntity {

    @Id
    private String id;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "birth_year", nullable = false)
    private int birthYear;

    @Column(length = 50)
    private String position;

    protected PlayerEntity() {
        // JPA
    }

    public PlayerEntity(String id, UUID teamId, String name, int birthYear, String position) {
        this.id = id;
        this.teamId = teamId;
        this.name = name;
        this.birthYear = birthYear;
        this.position = position;
    }

    public String getId() {
        return id;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public String getName() {
        return name;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public String getPosition() {
        return position;
    }
}
