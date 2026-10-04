package com.dynamictale.camera;

@FunctionalInterface
public interface CameraTransport {
    /** Returns true when the candidate was accepted for asynchronous application. */
    boolean apply(CameraState candidate);
}
