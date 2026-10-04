# DymanicTale

Camera overhaul for Hytale, currently being rebuilt from the validated beta.5 behavior and hardened core.

## Current repository state

The repository was initialized on 2026-10-04 and did not contain the original beta.5 source. The current branch therefore contains the beta.6.1 core-session hardening layer, not a fabricated replacement JAR.

### Core currently included

- SessionController
- CameraController
- TransitionTicketGate
- TransitionWatchdog
- CameraTransition
- immutable CameraState
- camera invariants
- input-generation routing
- idempotent session lifecycle
- confirmed right-shoulder baseline
- stale-callback rejection
- watchdog rollback

The reconstructed core was previously validated with Java 21 and 95 regression checks.

## Hytale boundary

The Hytale adapter is intentionally separate. Official Hytale API documentation exposes ServerCameraSettings and SetServerCamera; the adapter will translate the validated core state to that API only after the exact server dependency is available.

True First Person local head-only visibility remains an integration item to validate in the actual client; no unsupported API is assumed.

## Next implementation gate

To produce the real installable DymanicTale JAR, this repository needs the working beta.5 source/project or the exact HytaleServer.jar dependency used by that build. The goal is to integrate the hardened core without losing the behavior already validated in beta.5.
