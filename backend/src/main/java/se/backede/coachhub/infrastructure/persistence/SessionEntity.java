package se.backede.coachhub.infrastructure.persistence;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import se.backede.coachhub.domain.model.SessionStatus;

@Entity
@Table(name = "session")
public class SessionEntity {

    @Id
    private UUID id;

    @Column(name = "period_id", nullable = false)
    private UUID periodId;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;

    protected SessionEntity() {
        // JPA
    }

    public SessionEntity(UUID id, UUID periodId, LocalDate date, SessionStatus status) {
        this.id = id;
        this.periodId = periodId;
        this.date = date;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPeriodId() {
        return periodId;
    }

    public LocalDate getDate() {
        return date;
    }

    public SessionStatus getStatus() {
        return status;
    }
}
