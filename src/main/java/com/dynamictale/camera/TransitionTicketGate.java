package com.dynamictale.camera;

public final class TransitionTicketGate {
    private long sequence;
    private long currentTicket;
    private long currentInputVersion;
    private boolean valid;

    public synchronized long beginTransition(long inputVersion) {
        currentTicket = ++sequence;
        currentInputVersion = inputVersion;
        valid = true;
        return currentTicket;
    }

    public synchronized boolean isCurrent(long ticket, long inputVersion) {
        return valid && ticket == currentTicket && inputVersion == currentInputVersion;
    }

    public synchronized void invalidate() {
        valid = false;
    }
}
