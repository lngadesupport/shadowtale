package com.dynamictale.hytale;

import com.dynamictale.camera.CameraState;

public final class CameraInputWatcherTest {
    public static void main(String[] args) {
        AltInputState input = new AltInputState();
        require(!input.isFreeLook(), "free look must start disabled");
        require(input.updateWalking(true), "Walk press must transition");
        require(input.isFreeLook(), "Walk press must enable free look");
        require(!input.updateWalking(true), "duplicate Walk state must be idempotent");
        require(input.updateWalking(false), "Walk release must transition");
        require(!input.isFreeLook(), "Walk release must disable free look");

        CameraState state = new CameraRuntimeSession().state();
        near(4.0, state.distance(), "distance");
        near(0.90, state.shoulderX(), "right shoulder");
        require(!state.firstPerson(), "default bridge profile must be third person");

        System.out.println("PASS: CameraInputWatcherTest");
    }

    private static void near(double expected, double actual, String message) {
        if (Math.abs(expected - actual) > 1e-9) {
            throw new AssertionError(message + " expected=" + expected + " actual=" + actual);
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
