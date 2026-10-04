package com.dynamictale.hytale;

import com.dynamictale.camera.CameraState;
import com.hypixel.hytale.protocol.AttachedToType;
import com.hypixel.hytale.protocol.ApplyLookType;
import com.hypixel.hytale.protocol.ApplyMovementType;
import com.hypixel.hytale.protocol.CanMoveType;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.MouseInputTargetType;
import com.hypixel.hytale.protocol.MouseInputType;
import com.hypixel.hytale.protocol.MovementForceRotationType;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.PositionDistanceOffsetType;
import com.hypixel.hytale.protocol.PositionType;
import com.hypixel.hytale.protocol.RotationType;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.io.PacketHandler;

/** Per-player camera bridge. Hytale remains an external transport boundary. */
public final class CameraRuntimeSession {
    private static final double DISTANCE = 4.0;
    private static final double RIGHT_SHOULDER = 0.90;
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
        settings.speedModifier = 1.0f;
        settings.allowPitchControls = true;
        settings.displayCursor = false;
        settings.displayReticle = true;
        settings.mouseInputTargetType = MouseInputTargetType.Block;
        settings.sendMouseMotion = true;
        settings.skipCharacterPhysics = false;
        settings.hideHeldItem = false;
        settings.movementForceRotationType = next.freeLook()
                ? MovementForceRotationType.Custom
                : MovementForceRotationType.AttachedToHead;
        settings.movementForceRotation = next.freeLook() ? bodyYawOnly() : null;
        settings.attachedToType = AttachedToType.LocalPlayer;
        settings.attachedToEntityId = 0;
        settings.eyeOffset = true;
        settings.followAttachedEntity = true;
        settings.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffsetRaycast;
        settings.positionOffset = new Position(profile.shoulderX(), 0.0, 0.0);
        settings.positionType = PositionType.AttachedToPlusOffset;
        settings.position = null;
        settings.rotationType = RotationType.AttachedToPlusOffset;
        settings.rotationOffset = new Direction(0.0f, 0.0f, 0.0f);
        settings.rotation = null;
        settings.canMoveType = CanMoveType.AttachedToLocalPlayer;
        settings.applyMovementType = ApplyMovementType.CharacterController;
        settings.movementMultiplier = null;
        settings.applyLookType = ApplyLookType.LocalPlayerLookOrientation;
        settings.lookMultiplier = null;
        settings.mouseInputType = MouseInputType.LookAtTargetBlock;
        settings.planeNormal = null;
        settings.baseFov = null;
        settings.depthOfField = null;

        packetHandler.writeNoCache(
                new SetServerCamera(ClientCameraView.Custom, false, settings));
    }

    private Direction bodyYawOnly() {
        if (bodyRotation == null) return new Direction(0.0f, 0.0f, 0.0f);
        return new Direction(bodyRotation.yaw, 0.0f, 0.0f);
    }
}
