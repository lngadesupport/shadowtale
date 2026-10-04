package com.dynamictale.camera;

import java.util.List;
import java.util.Objects;

public final class CameraController {
    private final CameraTransport transport;
    private final TransitionTicketGate gate = new TransitionTicketGate();
    private CameraState confirmed;
    private CameraState pending;
    private long pendingInputVersion = Long.MIN_VALUE;
    private long lastTicket;

    public CameraController(CameraTransport transport, CameraState initialState) {
        this.transport = Objects.requireNonNull(transport, "transport");
        List<String> violations = CameraInvariant.validate(initialState);
        if (!violations.isEmpty()) throw new IllegalArgumentException(violations.toString());
        this.confirmed = initialState;
    }

    public synchronized long request(CameraState candidate, long inputVersion) {
        Objects.requireNonNull(candidate, "candidate");
        List<String> violations = CameraInvariant.validate(candidate);
        if (!violations.isEmpty()) throw new IllegalArgumentException(violations.toString());
        if (pending == null && candidate.equals(confirmed)) return lastTicket;

        long ticket = gate.beginTransition(inputVersion);
        pending = candidate;
        pendingInputVersion = inputVersion;
        lastTicket = ticket;
        if (!transport.apply(candidate)) {
            pending = null;
            pendingInputVersion = Long.MIN_VALUE;
            gate.invalidate();
        }
        return ticket;
    }

    public synchronized void onApplyResult(long ticket, long inputVersion, boolean applied) {
        if (!gate.isCurrent(ticket, inputVersion)) return;
        if (pending == null || pendingInputVersion != inputVersion) return;
        if (applied) {
            confirmed = pending;
        }
        pending = null;
        pendingInputVersion = Long.MIN_VALUE;
        gate.invalidate();
    }

    public synchronized CameraState confirmedState() { return confirmed; }
    public synchronized boolean hasPendingTransition() { return pending != null; }
    public synchronized long lastTicket() { return lastTicket; }

    public synchronized boolean recoverIfCurrent(long ticket, long inputVersion) {
        if (!gate.isCurrent(ticket, inputVersion)) return false;
        pending = null;
        pendingInputVersion = Long.MIN_VALUE;
        gate.invalidate();
        return true;
    }

    public synchronized void invalidate() {
        pending = null;
        pendingInputVersion = Long.MIN_VALUE;
        gate.invalidate();
    }
}
