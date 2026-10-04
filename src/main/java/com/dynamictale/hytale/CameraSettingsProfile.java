package com.dynamictale.hytale;

import com.dynamictale.camera.CameraState;

/** Pure, testable projection of camera state into Hytale camera parameters. */
public record CameraSettingsProfile(
        boolean firstPerson,
        float distance,
        float shoulderX,
        float positionLerpSpeed,
        float rotationLerpSpeed) {

    public static CameraSettingsProfile from(CameraState state) {
        return new CameraSettingsProfile(
                state.firstPerson(),
                (float) state.distance(),
                (float) state.shoulderX(),
                (float) state.positionLerpSpeed(),
                (float) state.rotationLerpSpeed());
    }
}
