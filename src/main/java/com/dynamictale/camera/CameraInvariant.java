package com.dynamictale.camera;

import java.util.ArrayList;
import java.util.List;

public final class CameraInvariant {
    private CameraInvariant() {}

    public static List<String> validate(CameraState state) {
        List<String> violations = new ArrayList<>();
        if (state == null) { violations.add("state is null"); return violations; }
        if (!Double.isFinite(state.distance()) || !Double.isFinite(state.shoulderX())
                || !Double.isFinite(state.positionLerpSpeed()) || !Double.isFinite(state.rotationLerpSpeed())
                || !Double.isFinite(state.rightShoulderBaselineX())) {
            violations.add("numeric values must be finite");
        }
        if (state.distance() < 0) violations.add("distance must be non-negative");
        if (state.positionLerpSpeed() < 0 || state.rotationLerpSpeed() < 0) {
            violations.add("lerp speeds must be non-negative");
        }
        if (state.mode() == CameraMode.FIRST_PERSON && !state.firstPerson()) {
            violations.add("first-person mode requires firstPerson=true");
        }
        if (state.mode() == CameraMode.THIRD_PERSON && state.firstPerson()) {
            violations.add("third-person mode requires firstPerson=false");
        }
        if (state.mode() == CameraMode.THIRD_PERSON && !state.freeLook()
                && Math.abs(state.shoulderX() - state.rightShoulderBaselineX()) > 1e-9) {
            violations.add("normal third-person state must use the confirmed right-shoulder baseline");
        }
        return List.copyOf(violations);
    }
}
