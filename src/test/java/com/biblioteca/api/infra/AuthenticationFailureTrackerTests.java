package com.biblioteca.api.infra;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationFailureTrackerTests {

    @Test
    void aumentaBloqueioParaQuinzeMinutosUmaHoraEDepoisUmDia() {
        MutableClock clock = new MutableClock(Instant.parse("2026-11-08T12:00:00Z"));
        AuthenticationFailureTracker tracker = new AuthenticationFailureTracker(clock);

        assertThat(registerFive(tracker, "client")).isEqualTo(Duration.ofMinutes(15));
        clock.advance(Duration.ofMinutes(15));
        assertThat(tracker.remainingLockout("client")).isNull();
        assertThat(registerFive(tracker, "client")).isEqualTo(Duration.ofHours(1));
        clock.advance(Duration.ofHours(1));
        assertThat(registerFive(tracker, "client")).isEqualTo(Duration.ofHours(24));
    }

    @Test
    void janelaExpiraEFalhasDeOutroClienteNaoCompartilhamBloqueio() {
        MutableClock clock = new MutableClock(Instant.parse("2026-11-08T12:00:00Z"));
        AuthenticationFailureTracker tracker = new AuthenticationFailureTracker(clock);
        for (int i = 0; i < AuthenticationFailureTracker.MAX_FAILURES - 1; i++) {
            assertThat(tracker.registerFailure("client-a")).isNull();
        }
        clock.advance(Duration.ofMinutes(11));
        assertThat(tracker.registerFailure("client-a")).isNull();
        assertThat(registerFive(tracker, "client-b")).isEqualTo(Duration.ofMinutes(15));
        assertThat(tracker.remainingLockout("client-a")).isNull();
    }

    @Test
    void autenticacaoBemSucedidaLimpaEscalonamentoAnterior() {
        MutableClock clock = new MutableClock(Instant.parse("2026-11-08T12:00:00Z"));
        AuthenticationFailureTracker tracker = new AuthenticationFailureTracker(clock);
        assertThat(registerFive(tracker, "client")).isEqualTo(Duration.ofMinutes(15));
        clock.advance(Duration.ofMinutes(15));
        tracker.registerSuccess("client");
        assertThat(registerFive(tracker, "client")).isEqualTo(Duration.ofMinutes(15));
    }

    private Duration registerFive(AuthenticationFailureTracker tracker, String client) {
        Duration last = null;
        for (int i = 0; i < AuthenticationFailureTracker.MAX_FAILURES; i++) {
            last = tracker.registerFailure(client);
        }
        return last;
    }

    private static final class MutableClock extends Clock {
        private final AtomicReference<Instant> instant;

        private MutableClock(Instant initial) {
            instant = new AtomicReference<>(initial);
        }

        private void advance(Duration duration) {
            instant.updateAndGet(value -> value.plus(duration));
        }

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant.get(); }
    }
}
