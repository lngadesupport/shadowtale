package com.dynamictale.camera;

/**
 * Deterministic scalar camera blend using an ease-in/ease-out smoothstep.
 * Time is expressed as elapsed nanoseconds, so the result is independent of frame rate.
 */
public final class CameraTransition {
    private final double start;
    private final double target;
    private final long durationNanos;

    private CameraTransition(double start, double target, long durationNanos) {
        this.start = start;
        this.target = target;
        this.durationNanos = durationNanos;
    }

    public static CameraTransition start(double start, double target, long durationNanos) {
        if (!Double.isFinite(start) || !Double.isFinite(target)) {
            throw new IllegalArgumentException("transition values must be finite");
        }
        if (durationNanos < 0) {
            throw new IllegalArgumentException("durationNanos must be non-negative");
        }
        if (durationNanos == 0 && Double.compare(start, target) != 0) {
            throw new IllegalArgumentException("non-identical endpoints require a positive duration");
        }
        return new CameraTransition(start, target, durationNanos);
    }

    public double valueAt(long elapsedNanos) {
        if (durationNanos == 0 || elapsedNanos >= durationNanos) return target;
        if (elapsedNanos <= 0) return start;

        double t = (double) elapsedNanos / (double) durationNanos;
        double eased = t * t * (3.0 - 2.0 * t);
        return start + (target - start) * eased;
    }

    public boolean isComplete(long elapsedNanos) {
        return durationNanos == 0 || elapsedNanos >= durationNanos;
    }

    public double start() { return start; }
    public double target() { return target; }
    public long durationNanos() { return durationNanos; }
}
