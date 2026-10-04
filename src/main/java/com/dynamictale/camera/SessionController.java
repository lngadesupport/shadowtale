package com.dynamictale.camera;

import java.util.Objects;
import java.util.function.Function;

/**
 * Reconstructed session boundary for the beta.6 core.
 * It validates all external inputs before creating mutable camera state,
 * and owns the coupling between input generation, camera transitions,
 * lifecycle invalidation, and the transition watchdog.
 */
public final class SessionController<P, S> {
    public static final long NO_TICKET = -1L;

    private final P player;
    private final S session;
    private final CameraController camera;
    private final SessionLifecycle lifecycle;
    private final InputRouter inputRouter;
    private final TransitionWatchdog watchdog;
    private final long watchdogTimeoutNanos;

    private long pendingTicket = NO_TICKET;
    private long pendingInputVersion = Long.MIN_VALUE;

    public SessionController(
            P player,
            Function<? super P, ? extends S> sessionResolver,
            CameraTransport transport,
            CameraState initialState,
            long watchdogTimeoutNanos) {
        this.player = Objects.requireNonNull(player, "player");
        Objects.requireNonNull(sessionResolver, "sessionResolver");
        Objects.requireNonNull(transport, "transport");
        Objects.requireNonNull(initialState, "initialState");
        if (watchdogTimeoutNanos <= 0) {
            throw new IllegalArgumentException("watchdogTimeoutNanos must be positive");
        }

        this.session = Objects.requireNonNull(
                sessionResolver.apply(this.player), "sessionResolver returned null");
        this.camera = new CameraController(transport, initialState);
        TransitionTicketGate gate = new TransitionTicketGate();
        this.lifecycle = new SessionLifecycle(gate);
        this.inputRouter = new InputRouter();
        this.watchdog = new TransitionWatchdog();
        this.watchdogTimeoutNanos = watchdogTimeoutNanos;
    }

    public synchronized boolean start() {
        return lifecycle.start();
    }

    public synchronized boolean stop() {
        boolean changed = lifecycle.stop();
        if (changed) clearPendingAndInvalidateCamera();
        return changed;
    }

    public synchronized boolean worldChanged() {
        if (!lifecycle.isActive()) return false;
        lifecycle.worldChanged();
        clearPendingAndInvalidateCamera();
        return true;
    }

    public synchronized long request(
            InputAction action,
            CameraState candidate,
            boolean gameplayInputAllowed,
            long nowNanos) {
        if (!lifecycle.isActive()) return NO_TICKET;
        validateCandidate(candidate);
        if (!inputRouter.submit(action, gameplayInputAllowed)) return NO_TICKET;

        long inputVersion = inputRouter.inputVersion();
        long ticket = camera.request(candidate, inputVersion);
        pendingTicket = ticket;
        pendingInputVersion = inputVersion;

        if (camera.hasPendingTransition()) {
            watchdog.arm(ticket, inputVersion, safeDeadline(nowNanos, watchdogTimeoutNanos));
        } else {
            watchdog.cancel(ticket);
            pendingTicket = NO_TICKET;
            pendingInputVersion = Long.MIN_VALUE;
        }
        return ticket;
    }

    public synchronized boolean onApplyResult(long ticket, boolean applied) {
        if (ticket != pendingTicket || pendingTicket == NO_TICKET) return false;
        long inputVersion = pendingInputVersion;
        camera.onApplyResult(ticket, inputVersion, applied);
        watchdog.cancel(ticket);
        pendingTicket = NO_TICKET;
        pendingInputVersion = Long.MIN_VALUE;
        return true;
    }

    public synchronized boolean watchdogShouldRecover(long ticket, long nowNanos) {
        if (ticket != pendingTicket || pendingTicket == NO_TICKET) return false;
        if (!watchdog.shouldRecover(ticket, pendingInputVersion, nowNanos)) return false;
        boolean recovered = camera.recoverIfCurrent(ticket, pendingInputVersion);
        pendingTicket = NO_TICKET;
        pendingInputVersion = Long.MIN_VALUE;
        return recovered;
    }

    public synchronized P player() { return player; }
    public synchronized S session() { return session; }
    public synchronized CameraController camera() { return camera; }
    public synchronized boolean isActive() { return lifecycle.isActive(); }
    public synchronized boolean hasPendingTransition() { return pendingTicket != NO_TICKET; }
    public synchronized long inputVersion() { return inputRouter.inputVersion(); }

    private static void validateCandidate(CameraState candidate) {
        var violations = CameraInvariant.validate(candidate);
        if (!violations.isEmpty()) throw new IllegalArgumentException(violations.toString());
    }

    private static long safeDeadline(long nowNanos, long timeoutNanos) {
        if (nowNanos > Long.MAX_VALUE - timeoutNanos) return Long.MAX_VALUE;
        return nowNanos + timeoutNanos;
    }

    private void clearPendingAndInvalidateCamera() {
        if (pendingTicket != NO_TICKET) watchdog.cancel(pendingTicket);
        pendingTicket = NO_TICKET;
        pendingInputVersion = Long.MIN_VALUE;
        camera.invalidate();
    }
}
