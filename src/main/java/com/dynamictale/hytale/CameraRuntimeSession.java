package com.dynamictale.hytale;

import com.dynamictale.camera.CameraState;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.MouseInputType;
import com.hypixel.hytale.protocol.MovementForceRotationType;
import com.hypixel.hytale.protocol.PositionDistanceOffsetType;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.io.PacketHandler;

/** Per-player camera bridge. Hytale remains an external transport boundary. */
public final class CameraRuntimeSession {
    private static final double DISTANCE = 3.4;
    private static final double RIGHT_SHOULDER = 0.72;
    private static final double POSITION_LERP = 0.20;
    private static final double ROTATION_LERP = 0.25;

    private CameraState state = CameraState.thirdPerson(
            DISTANCE, RIGHT_SHOULDER, POSITION_LERP, ROTATION_LERP);
    private Direction bodyRotation;
    private boolean applied;

    public void updateBodyRotation(Direction rotation) {
        if (rotation != null) bodyRotation = rotation;
    }

    public void ensureApplied(PacketHandler packetHandler) {
        if (!applied) {
            send(packetHandler, state);
            applied = true;
        }
    }

    public void setFreeLook(PacketHandler packetHandler, boolean enabled) {
        if (state.freeLook() == enabled) return;
        state = state.withFreeLook(enabled);
        send(packetHandler, state);
    }

    public CameraState state() {
        return state;
    }

    private void send(PacketHandler packetHandler, CameraState next) {
        CameraSettingsProfile profile = CameraSettingsProfile.from(next);
        ServerCameraSettings settings = new ServerCameraSettings();
        settings.isFirstPerson = profile.firstPerson();
        settings.distance = profile.distance();
        settings.positionLerpSpeed = profile.positionLerpSpeed();
        settings.rotationLerpSpeed = profile.rotationLerpSpeed();
        settings.eyeOffset = true;
        settings.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffsetRaycast;
        settings.positionOffset = new Vector3f(profile.shoulderX(), 0.0f, 0.0f);
        settings.mouseInputType = MouseInputType.LookAtTarget;
        settings.allowPitchControls = true;
        settings.displayCursor = false;

        if (next.freeLook()) {
            settings.movementForceRotationType = MovementForceRotationType.Custom;
            settings.movementForceRotation = bodyYawOnly();
        } else {
            settings.movementForceRotationType = MovementForceRotationType.AttachedToHead;
        }

        packetHandler.writeNoCache(
                new SetServerCamera(ClientCameraView.Custom, false, settings));
    }

    private Direction bodyYawOnly() {
        if (bodyRotation == null) return new Direction(0.0f, 0.0f, 0.0f);
        return new Direction(bodyRotation.yaw, 0.0f, 0.0f);
    }
}
