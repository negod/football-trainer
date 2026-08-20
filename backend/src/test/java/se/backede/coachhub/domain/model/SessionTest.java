package se.backede.coachhub.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void skipExcludesTheSessionWithoutChangingItsDate() {
        Session session = Session.create(PeriodId.newId(), LocalDate.of(2026, 3, 3));

        Session skipped = session.skip();

        assertThat(skipped.status()).isEqualTo(SessionStatus.SKIPPED);
        assertThat(skipped.id()).isEqualTo(session.id());
        assertThat(skipped.date()).isEqualTo(session.date());
    }

    @Test
    void restoreReversesSkip() {
        Session skipped = Session.create(PeriodId.newId(), LocalDate.of(2026, 3, 3)).skip();

        Session restored = skipped.restore();

        assertThat(restored.status()).isEqualTo(SessionStatus.SCHEDULED);
    }

    @Test
    void rescheduleMovesTheDateWithoutChangingIdentityOrStatus() {
        Session skipped = Session.create(PeriodId.newId(), LocalDate.of(2026, 3, 3)).skip();
        LocalDate newDate = LocalDate.of(2026, 3, 10);

        Session rescheduled = skipped.reschedule(newDate);

        assertThat(rescheduled.date()).isEqualTo(newDate);
        assertThat(rescheduled.id()).isEqualTo(skipped.id());
        assertThat(rescheduled.status()).isEqualTo(SessionStatus.SKIPPED);
    }

    @Test
    void rescheduleRejectsANullDate() {
        Session session = Session.create(PeriodId.newId(), LocalDate.of(2026, 3, 3));

        assertThatThrownBy(() -> session.reschedule(null)).isInstanceOf(NullPointerException.class);
    }
}
