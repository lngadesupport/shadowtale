package com.dynamictale.hytale;

import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.adapter.PacketWatcher;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Bridges Hytale client movement-state packets to per-player camera state. */
public final class CameraInputWatcher implements PacketWatcher {
    private static final String CLIENT_MOVEMENT = "com.hypixel.hytale.protocol.packets.player.ClientMovement";
    private static final String CLIENT_DISCONNECT = "com.hypixel.hytale.protocol.packets.connection.ClientDisconnect";

    private final Map<UUID, CameraRuntimeSession> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, AltInputState> altStates = new ConcurrentHashMap<>();

    @Override
    public void accept(PacketHandler packetHandler, Packet packet) {
        if (packetHandler == null || packet == null) return;
        UUID playerId = playerId(packetHandler);
        if (playerId == null) return;

        String packetName = packet.getClass().getName();
        if (CLIENT_DISCONNECT.equals(packetName)) {
            remove(playerId);
            return;
        }
        if (!CLIENT_MOVEMENT.equals(packetName)) return;

        try {
            Object movementStates = publicField(packet, "movementStates");
            if (movementStates == null) return;
            Object bodyOrientation = publicField(packet, "bodyOrientation");
            float yaw = bodyOrientation == null
                    ? 0.0f
                    : ((Number) publicField(bodyOrientation, "yaw")).floatValue();
            boolean walking = (Boolean) publicField(movementStates, "walking");

            CameraRuntimeSession session = sessions.computeIfAbsent(
                    playerId, ignored -> new CameraRuntimeSession());
            session.updateBodyYaw(yaw);
            session.ensureApplied(packetHandler);

            AltInputState altState = altStates.computeIfAbsent(
                    playerId, ignored -> new AltInputState());
            if (altState.updateWalking(walking)) {
                session.setFreeLook(packetHandler, altState.isFreeLook());
            }
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new IllegalStateException("DymanicTale could not read Hytale client movement state", e);
        }
    }

    public void remove(UUID playerId) {
        if (playerId == null) return;
        sessions.remove(playerId);
        altStates.remove(playerId);
    }

    public int sessionCount() {
        return sessions.size();
    }

    private static UUID playerId(PacketHandler packetHandler) {
        try {
            Object auth = packetHandler.getClass().getMethod("getAuth").invoke(packetHandler);
            if (auth == null) return null;
            Object uuid = auth.getClass().getMethod("getUuid").invoke(auth);
            return uuid instanceof UUID value ? value : null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static Object publicField(Object target, String name) throws ReflectiveOperationException {
        return target.getClass().getField(name).get(target);
    }
}