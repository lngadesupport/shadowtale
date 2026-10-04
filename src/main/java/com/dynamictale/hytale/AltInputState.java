package com.dynamictale.hytale;

/** Maps Hytale's default Walk input state to DymanicTale free-look state. */
public final class AltInputState {
    private boolean freeLook;

    public boolean updateWalking(boolean walking) {
        if (freeLook == walking) return false;
        freeLook = walking;
        return true;
    }

    public boolean isFreeLook() {
        return freeLook;
    }
}
