package com.dynamictale.hytale;

import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.MovementForceRotationType;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.protocol.packets.player.ClientMovement;
import com.hypixel.hytale.server.core.auth.PlayerAuthentication;
import com.hypixel.hytale.server.core.io.PacketHandler;
import java.util.UUID;

public final class CameraInputWatcherTest {
    public static void main(String[] args) {
        UUID id = UUID.randomUUID();
        PacketHandler handler = new PacketHandler(new PlayerAuthentication(id));
        CameraInputWatcher watcher = new CameraInputWatcher();

        ClientMovement idle = new ClientMovement(new MovementStates(false));
        idle.bodyOrientation = new com.hypixel.hytale.protocol.Direction(1.25f, 0f, 0f);
        watcher.accept(handler, idle);

        SetServerCamera initial = (SetServerCamera) handler.lastPacket();
        require(initial != null, "initial camera packet must be sent");
        require(initial.clientCameraView == ClientCameraView.Custom, "camera view must be custom");
        near(3.4f, initial.cameraSettings.distance, "distance");
        near(0.72f, initial.cameraSettings.positionOffset.x, "right shoulder");

        ClientMovement freeLook = new ClientMovement(new MovementStates(true));
        freeLook.bodyOrientation = idle.bodyOrientation;
        watcher.accept(handler, freeLook);
        SetServerCamera enabled = (SetServerCamera) handler.lastPacket();
        require(enabled.cameraSettings.movementForceRotationType == MovementForceRotationType.Custom,
                "free look must decouple movement rotation from camera look");
        near(1.25f, enabled.cameraSettings.movementForceRotation.yaw, "body yaw during free look");

        ClientMovement released = new ClientMovement(new MovementStates(false));
        released.bodyOrientation = idle.bodyOrientation;
        watcher.accept(handler, released);
        SetServerCamera disabled = (SetServerCamera) handler.lastPacket();
        require(disabled.cameraSettings.movementForceRotationType == MovementForceRotationType.AttachedToHead,
                "release must restore attached movement rotation");
        require(watcher.sessionCount() == 1, "one session must be retained for one player");

        System.out.println("PASS: CameraInputWatcherTest");
    }

    private static void near(float expected, float actual, String message) {
        if (Math.abs(expected - actual) > 1e-5f) {
            throw new AssertionError(message + " expected=" + expected + " actual=" + actual);
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
