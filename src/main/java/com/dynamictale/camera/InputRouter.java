package com.dynamictale.camera;

public final class InputRouter {
    private long inputVersion;

    public synchronized boolean submit(InputAction action, boolean gameplayInputAllowed) {
        if (action == null || !gameplayInputAllowed) return false;
        inputVersion++;
        return true;
    }

    public synchronized long inputVersion() { return inputVersion; }
}
