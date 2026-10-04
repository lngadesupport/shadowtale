package com.dynamictale.camera;

public final class TransitionWatchdog {
    private long ticket = Long.MIN_VALUE;
    private long inputVersion = Long.MIN_VALUE;
    private long deadlineNanos = Long.MAX_VALUE;
    private boolean armed;

    public synchronized void arm(long ticket, long inputVersion, long deadlineNanos) {
        this.ticket = ticket;
        this.inputVersion = inputVersion;
        this.deadlineNanos = deadlineNanos;
        this.armed = true;
    }

    public synchronized boolean shouldRecover(long ticket, long inputVersion, long nowNanos) {
        if (!armed || ticket != this.ticket || inputVersion != this.inputVersion || nowNanos < deadlineNanos) {
            return false;
        }
        armed = false;
        return true;
    }

    public synchronized void cancel(long ticket) {
        if (armed && ticket == this.ticket) armed = false;
    }
}
