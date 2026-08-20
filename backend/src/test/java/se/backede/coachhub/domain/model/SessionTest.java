package se.backede.coachhub.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class SessionTest {

    @Test
    void createsAScheduledSession() {
        PeriodId periodId = PeriodId.newId();
        LocalDate date = LocalDate.of(2026, 3, 3);

        Session session = Session.create(periodId, date);

        assertThat(session.periodId()).isEqualTo(periodId);
        assertThat(session.date()).isEqualTo(date);
        assertThat(session.status()).isEqualTo(SessionStatus.SCHEDULED);
    }

    @Test
    void belongsToPeriodOnlyForItsOwnPeriod() {
        PeriodId periodId = PeriodId.newId();
        PeriodId otherPeriodId = PeriodId.newId();
        Session session = Session.create(periodId, LocalDate.of(2026, 3, 3));

        assertThat(session.belongsToPeriod(periodId)).isTrue();
        assertThat(session.belongsToPeriod(otherPeriodId)).isFalse();
    }
}
