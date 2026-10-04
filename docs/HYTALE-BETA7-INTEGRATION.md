# DymanicTale beta.7 Hytale integration

## Scope

This branch turns the validated camera core into an independent Hytale Java plugin. The graphics mod is intentionally outside this project and is not a dependency.

## Runtime bridge

The adapter uses Hytale's server camera packet boundary and inbound movement-state packets. The default Hytale Walk state (normally Left Alt) drives the DymanicTale free-look state. While free-look is active, locomotion remains aligned to the stored body yaw while mouse input controls the camera.

## V key

Hytale server plugins do not receive raw keyboard events. The documented inbound packet set exposes movement/action state rather than a raw V-key event. The server camera packet is therefore sent unlocked so native client camera switching remains available; exact V behavior must be validated against the target client build.

## Build dependency

The Hytale Server artifact is compileOnly and is never bundled into the final JAR. The build resolves it from the Hytale release Maven repository using com.hypixel.hytale:Server:+ and Java 25.

## Verification boundary

Local verification compiles the adapter against a minimal API stub and runs the core regression suite plus adapter tests. That proves the bridge's source-level contracts and package hygiene; it does not claim a live Hytale client/server smoke test.
