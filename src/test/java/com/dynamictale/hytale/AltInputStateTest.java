package com.dynamictale.hytale;

public final class AltInputStateTest {
    public static void main(String[] args) {
        AltInputState state = new AltInputState();
        require(!state.isFreeLook(), "initial state must be off");
        require(state.updateWalking(true), "press transition must be accepted");
        require(state.isFreeLook(), "walking must enter free look");
        require(!state.updateWalking(true), "duplicate press must be idempotent");
        require(state.updateWalking(false), "release transition must be accepted");
        require(!state.isFreeLook(), "release must leave free look");
        require(!state.updateWalking(false), "duplicate release must be idempotent");
        System.out.println("PASS: AltInputStateTest");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
