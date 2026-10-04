package com.dynamictale.hytale;

import com.dynamictale.camera.CameraState;

public final class CameraProfileTest {
    public static void main(String[] args) {
        CameraSettingsProfile tp = CameraSettingsProfile.from(
                CameraState.thirdPerson(3.4, 0.72, 0.20, 0.25));
        require(!tp.firstPerson(), "third person must stay third person");
        near(3.4, tp.distance(), "distance");
        near(0.72, tp.shoulderX(), "right shoulder");
        near(0.20, tp.positionLerpSpeed(), "position lerp");
        near(0.25, tp.rotationLerpSpeed(), "rotation lerp");

        CameraSettingsProfile fp = CameraSettingsProfile.from(CameraState.firstPersonState());
        require(fp.firstPerson(), "first person profile must be first person");
        System.out.println("PASS: CameraProfileTest");
    }

    private static void near(double expected, double actual, String message) {
        if (Math.abs(expected - actual) > 1e-6) {
            throw new AssertionError(message + " expected=" + expected + " actual=" + actual);
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
