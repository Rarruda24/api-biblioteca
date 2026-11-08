package com.biblioteca.api.infra;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/** Progressive temporary lockout for repeated authentication failures from one peer IP. */
@Component
public class AuthenticationFailureTracker {
    public static final int MAX_FAILURES = 5;
    public static final Duration WINDOW = Duration.ofMinutes(10);
    private static final Duration RESET_AFTER_IDLE = Duration.ofHours(24);
    private static final Duration[] LOCKOUTS = {
            Duration.ofMinutes(15), Duration.ofHours(1), Duration.ofHours(24)
    };

    private final Clock clock;
    private final Map<String, State> attemptsByClient = new HashMap<>();

    public AuthenticationFailureTracker() {
        this(Clock.systemUTC());
    }

    AuthenticationFailureTracker(Clock clock) {
        this.clock = clock;
    }

    /** Returns the remaining lockout, or null when the client is not blocked. */
    public synchronized Duration remainingLockout(String client) {
        Instant now = clock.instant();
        State state = attemptsByClient.get(client);
        if (state == null) return null;
        if (Duration.between(state.lastFailure, now).compareTo(RESET_AFTER_IDLE) >= 0) {
            attemptsByClient.remove(client);
            return null;
        }
        if (state.blockedUntil == null) return null;
        Duration remaining = Duration.between(now, state.blockedUntil);
        if (remaining.isNegative() || remaining.isZero()) {
            state.blockedUntil = null;
            return null;
        }
        return remaining;
    }

    /** Registers a failure and returns a duration when this failure starts a lockout. */
    public synchronized Duration registerFailure(String client) {
        Duration remaining = remainingLockout(client);
        if (remaining != null) return remaining;

        Instant now = clock.instant();
        State state = attemptsByClient.computeIfAbsent(client, ignored -> new State(now));
        if (Duration.between(state.windowStarted, now).compareTo(WINDOW) >= 0) {
            state.failures = 0;
            state.windowStarted = now;
        }
        state.lastFailure = now;
        state.failures++;
        if (state.failures < MAX_FAILURES) return null;

        Duration lockout = LOCKOUTS[Math.min(state.escalationLevel, LOCKOUTS.length - 1)];
        state.escalationLevel = Math.min(state.escalationLevel + 1, LOCKOUTS.length - 1);
        state.failures = 0;
        state.windowStarted = now;
        state.blockedUntil = now.plus(lockout);
        return lockout;
    }

    /** A successful authentication clears that peer's failure history. */
    public synchronized void registerSuccess(String client) {
        attemptsByClient.remove(client);
    }

    /** Intended for isolated integration tests. */
    public synchronized void clear() {
        attemptsByClient.clear();
    }

    private static final class State {
        private Instant windowStarted;
        private Instant lastFailure;
        private Instant blockedUntil;
        private int failures;
        private int escalationLevel;

        private State(Instant now) {
            this.windowStarted = now;
            this.lastFailure = now;
        }
    }
}
