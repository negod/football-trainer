package se.backede.coachhub.infrastructure.persistence;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import se.backede.coachhub.domain.model.GenderCategory;

@Entity
@Table(name = "team")
public class TeamEntity {

    @Id
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "birth_year", nullable = false)
    private int birthYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_category", nullable = false, length = 10)
    private GenderCategory genderCategory;

    protected TeamEntity() {
        // JPA
    }

    public TeamEntity(UUID id, UUID ownerId, String name, int birthYear, GenderCategory genderCategory) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.birthYear = birthYear;
        this.genderCategory = genderCategory;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public GenderCategory getGenderCategory() {
        return genderCategory;
    }
}
