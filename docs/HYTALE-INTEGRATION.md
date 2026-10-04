# Hytale integration boundary

The pure core deliberately does not import Hytale classes.

The current official API documents ServerCameraSettings with:

- isFirstPerson
- distance
- positionLerpSpeed
- rotationLerpSpeed
- positionOffset
- rotationOffset
- eyeOffset

and documents SetServerCamera as carrying ServerCameraSettings.

## Adapter responsibilities

1. Validate the Hytale player/session boundary before constructing the camera controller.
2. Read/construct Hytale camera settings and translate them to/from CameraState.
3. Route accepted V/Alt gameplay input through the session controller so invalid or blocked input does not consume a generation.
4. Call CameraController.request(...) only with a validated candidate.
5. Complete onApplyResult(...) only for the current transition.
6. Let the TransitionWatchdog recover only the transition that owns its ticket + input generation.
7. Use CameraTransition when the adapter needs deterministic, frame-rate-independent return interpolation.
8. Invalidate the controller on player/world/session teardown.

True First Person local head-only visibility remains an external validation point. No unsupported local-head hiding API is assumed here.
