package com.dynamictale.camera;

import java.util.Objects;

public final class CameraState {
    private final CameraMode mode;
    private final boolean firstPerson;
    private final double distance;
    private final double shoulderX;
    private final double positionLerpSpeed;
    private final double rotationLerpSpeed;
    private final boolean freeLook;
    private final double rightShoulderBaselineX;

    public CameraState(CameraMode mode, boolean firstPerson, double distance, double shoulderX,
                       double positionLerpSpeed, double rotationLerpSpeed, boolean freeLook,
                       double rightShoulderBaselineX) {
        this.mode = Objects.requireNonNull(mode, "mode");
        this.firstPerson = firstPerson;
        this.distance = distance;
        this.shoulderX = shoulderX;
        this.positionLerpSpeed = positionLerpSpeed;
        this.rotationLerpSpeed = rotationLerpSpeed;
        this.freeLook = freeLook;
        this.rightShoulderBaselineX = rightShoulderBaselineX;
    }

    public static CameraState firstPersonState() {
        return new CameraState(CameraMode.FIRST_PERSON, true, 0.0, 0.72, 0.72, 0.95, false, 0.72);
    }

    public static CameraState thirdPerson(double distance, double shoulderX,
                                          double positionLerpSpeed, double rotationLerpSpeed) {
        return new CameraState(CameraMode.THIRD_PERSON, false, distance, shoulderX,
                positionLerpSpeed, rotationLerpSpeed, false, 0.72);
    }

    public CameraMode mode() { return mode; }
    public boolean firstPerson() { return firstPerson; }
    public double distance() { return distance; }
    public double shoulderX() { return shoulderX; }
    public double positionLerpSpeed() { return positionLerpSpeed; }
    public double rotationLerpSpeed() { return rotationLerpSpeed; }
    public boolean freeLook() { return freeLook; }
    public double rightShoulderBaselineX() { return rightShoulderBaselineX; }

    public CameraState withMode(CameraMode nextMode, boolean nextFirstPerson) {
        return new CameraState(nextMode, nextFirstPerson, distance, shoulderX, positionLerpSpeed,
                rotationLerpSpeed, freeLook, rightShoulderBaselineX);
    }

    public CameraState withShoulderX(double value) {
        return new CameraState(mode, firstPerson, distance, value, positionLerpSpeed,
                rotationLerpSpeed, freeLook, rightShoulderBaselineX);
    }

    public CameraState withFreeLook(boolean value) {
        return new CameraState(mode, firstPerson, distance, shoulderX, positionLerpSpeed,
                rotationLerpSpeed, value, rightShoulderBaselineX);
    }

    public CameraState normalizedRightShoulder() {
        return new CameraState(CameraMode.THIRD_PERSON, false, distance, rightShoulderBaselineX,
                positionLerpSpeed, rotationLerpSpeed, false, rightShoulderBaselineX);
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof CameraState other)) return false;
        return mode == other.mode && firstPerson == other.firstPerson
                && Double.compare(distance, other.distance) == 0
                && Double.compare(shoulderX, other.shoulderX) == 0
                && Double.compare(positionLerpSpeed, other.positionLerpSpeed) == 0
                && Double.compare(rotationLerpSpeed, other.rotationLerpSpeed) == 0
                && freeLook == other.freeLook
                && Double.compare(rightShoulderBaselineX, other.rightShoulderBaselineX) == 0;
    }

    @Override public int hashCode() {
        return Objects.hash(mode, firstPerson, distance, shoulderX, positionLerpSpeed,
                rotationLerpSpeed, freeLook, rightShoulderBaselineX);
    }

    @Override public String toString() {
        return "CameraState{" + mode + ", firstPerson=" + firstPerson + ", distance=" + distance
                + ", shoulderX=" + shoulderX + ", freeLook=" + freeLook + '}';
    }
}
