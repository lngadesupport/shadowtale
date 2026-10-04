package com.dynamictale.hytale;

import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.player.ClientMovement;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.adapter.PacketWatcher;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Bridges client movement-state packets to the per-player camera session. */
public final class CameraInputWatcher implements PacketWatcher {
    private final Map<UUID, CameraRuntimeSession> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, AltInputState> altStates = new ConcurrentHashMap<>();

    @Override
    public void accept(PacketHandler packetHandler, Packet packet) {
        if (!(packet instanceof ClientMovement movement)
                || movement.movementStates == null
                || packetHandler.getAuth() == null
                || packetHandler.getAuth().getUuid() == null) {
            return;
        }

        UUID playerId = packetHandler.getAuth().getUuid();
        CameraRuntimeSession session = sessions.computeIfAbsent(
                playerId, ignored -> new CameraRuntimeSession());
        session.updateBodyRotation(movement.bodyOrientation);
        session.ensureApplied(packetHandler);

        MovementStates states = movement.movementStates;
        AltInputState altState = altStates.computeIfAbsent(
                playerId, ignored -> new AltInputState());
        if (altState.updateWalking(states.walking)) {
            session.setFreeLook(packetHandler, altState.isFreeLook());
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
}
