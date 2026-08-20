package se.backede.coachhub.infrastructure.persistence;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import se.backede.coachhub.domain.model.MatchFormat;

@Entity
@Table(name = "period")
public class PeriodEntity {

    @Id
    private UUID id;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchFormat format;

    protected PeriodEntity() {
        // JPA
    }

    public PeriodEntity(UUID id, UUID teamId, String name, LocalDate startDate, LocalDate endDate, MatchFormat format) {
        this.id = id;
        this.teamId = teamId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.format = format;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public String getName() {
        return name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public MatchFormat getFormat() {
        return format;
    }
}
