package com.dynamictale.camera;

import java.util.Objects;

public final class SessionLifecycle {
    private final TransitionTicketGate gate;
    private boolean active;

    public SessionLifecycle(TransitionTicketGate gate) {
        this.gate = Objects.requireNonNull(gate, "gate");
    }

    public synchronized boolean start() {
        if (active) return false;
        active = true;
        return true;
    }

    public synchronized boolean stop() {
        if (!active) return false;
        active = false;
        gate.invalidate();
        return true;
    }

    public synchronized void worldChanged() {
        active = false;
        gate.invalidate();
    }

    public synchronized boolean isActive() { return active; }
}
