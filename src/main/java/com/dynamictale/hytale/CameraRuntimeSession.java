package com.dynamictale.hytale;

import com.dynamictale.camera.CameraState;
import com.hypixel.hytale.server.core.io.PacketHandler;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Per-player camera bridge. Protocol objects are reflected to tolerate minor API field-type changes. */
public final class CameraRuntimeSession {
    private static final double DISTANCE = 4.0;
    private static final double RIGHT_SHOULDER = 0.90;
    private static final double POSITION_LERP = 0.20;
    private static final double ROTATION_LERP = 0.25;

    private CameraState state = CameraState.thirdPerson(
            DISTANCE, RIGHT_SHOULDER, POSITION_LERP, ROTATION_LERP);
    private float bodyYaw;
    private boolean applied;

    public void updateBodyYaw(float yaw) {
        if (Float.isFinite(yaw)) bodyYaw = yaw;
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
        try {
            Class<?> settingsClass = Class.forName("com.hypixel.hytale.protocol.ServerCameraSettings");
            Object settings = settingsClass.getConstructor().newInstance();

            set(settings, "isFirstPerson", false);
            set(settings, "distance", (float) next.distance());
            set(settings, "positionLerpSpeed", (float) next.positionLerpSpeed());
            set(settings, "rotationLerpSpeed", (float) next.rotationLerpSpeed());
            set(settings, "speedModifier", 1.0f);
            set(settings, "allowPitchControls", true);
            set(settings, "displayCursor", false);
            set(settings, "displayReticle", true);
            setEnum(settings, "mouseInputTargetType", "com.hypixel.hytale.protocol.MouseInputTargetType", "Block");
            set(settings, "sendMouseMotion", true);
            set(settings, "skipCharacterPhysics", false);
            set(settings, "hideHeldItem", false);
            setEnum(settings, "movementForceRotationType", "com.hypixel.hytale.protocol.MovementForceRotationType",
                    next.freeLook() ? "Custom" : "AttachedToHead");
            set(settings, "movementForceRotation", next.freeLook()
                    ? newDirection(bodyYaw, 0.0f, 0.0f)
                    : null);
            setEnum(settings, "attachedToType", "com.hypixel.hytale.protocol.AttachedToType", "LocalPlayer");
            set(settings, "attachedToEntityId", 0);
            set(settings, "eyeOffset", true);
            set(settings, "followAttachedEntity", true);
            setEnum(settings, "positionDistanceOffsetType",
                    "com.hypixel.hytale.protocol.PositionDistanceOffsetType", "DistanceOffsetRaycast");
            set(settings, "positionOffset", newPosition(next.shoulderX(), 0.0, 0.0));
            setEnum(settings, "positionType", "com.hypixel.hytale.protocol.PositionType", "AttachedToPlusOffset");
            set(settings, "position", null);
            setEnum(settings, "rotationType", "com.hypixel.hytale.protocol.RotationType", "AttachedToPlusOffset");
            set(settings, "rotationOffset", newDirection(0.0f, 0.0f, 0.0f));
            set(settings, "rotation", null);
            setEnum(settings, "canMoveType", "com.hypixel.hytale.protocol.CanMoveType", "AttachedToLocalPlayer");
            setEnum(settings, "applyMovementType", "com.hypixel.hytale.protocol.ApplyMovementType", "CharacterController");
            set(settings, "movementMultiplier", null);
            setEnum(settings, "applyLookType", "com.hypixel.hytale.protocol.ApplyLookType", "LocalPlayerLookOrientation");
            set(settings, "lookMultiplier", null);
            setEnum(settings, "mouseInputType", "com.hypixel.hytale.protocol.MouseInputType", "LookAtTargetBlock");
            set(settings, "planeNormal", null);
            set(settings, "baseFov", null);
            set(settings, "depthOfField", null);

            Class<?> viewClass = Class.forName("com.hypixel.hytale.protocol.ClientCameraView");
            Object customView = enumValue(viewClass, "Custom");
            Class<?> packetClass = Class.forName("com.hypixel.hytale.protocol.packets.camera.SetServerCamera");
            Constructor<?> packetConstructor = packetClass.getConstructor(viewClass, boolean.class, settingsClass);
            Object packet = packetConstructor.newInstance(customView, false, settings);

            Method writeNoCache = packetHandler.getClass().getMethod(
                    "writeNoCache", Class.forName("com.hypixel.hytale.protocol.ToClientPacket"));
            writeNoCache.invoke(packetHandler, packet);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("DymanicTale could not build the Hytale camera packet", e);
        }
    }

    private static Object newPosition(double x, double y, double z) throws ReflectiveOperationException {
        Class<?> type = Class.forName("com.hypixel.hytale.protocol.Position");
        return type.getConstructor(double.class, double.class, double.class).newInstance(x, y, z);
    }

    private static Object newDirection(float yaw, float pitch, float roll) throws ReflectiveOperationException {
        Class<?> type = Class.forName("com.hypixel.hytale.protocol.Direction");
        return type.getConstructor(float.class, float.class, float.class).newInstance(yaw, pitch, roll);
    }

    private static void set(Object target, String fieldName, Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getField(fieldName);
        field.set(target, value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object enumValue(Class<?> enumClass, String constant) {
        Class<? extends Enum> typed = enumClass.asSubclass(Enum.class);
        return Enum.valueOf(typed, constant);
    }

    private static void setEnum(Object target, String fieldName, String enumName, String constant)
            throws ReflectiveOperationException {
        set(target, fieldName, enumValue(Class.forName(enumName), constant));
    }
}
