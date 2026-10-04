# DymanicTale beta.7.1 Hytale integration

## Scope

This branch is the standalone DymanicTale camera mod. The graphics mod is a separate project and is not a dependency.

## Camera profile

The validated camera profile uses a +0.90 right-shoulder lateral offset, 4-block distance, and Hytale's distance-offset raycast collision. The mouse target remains block-aware so normal block interaction is preserved.

## Runtime bridge

The adapter watches inbound ClientMovement packets. It reads the server-visible movementStates.walking state and the player's bodyOrientation.yaw. The default Hytale Walk key is Left Alt, so holding Walk/Alt activates free-look and releasing it restores the normal shoulder camera. The actual bridge follows the movement state, not a hard-coded keyboard event.

The protocol construction is reflection-based. This is intentional: the current official API exposes ServerCameraSettings.positionOffset as Position, while earlier generated integrations used other vector representations. Reflection keeps the final JAR from hard-linking that generated field type while still using the documented field and enum names.

## V camera switching

The server plugin API does not expose a raw V-key event. SetServerCamera is sent with isLocked=false, so native client camera switching remains unlocked. Whether V remains fully functional on the target Hytale build must be confirmed by live client/server testing.

## Build dependency

The Hytale Server artifact is compileOnly and is never bundled into the final JAR. The Gradle build targets Java 25 and resolves com.hypixel.hytale:Server:+ from the Hytale release repository.

## Verification boundary

The local beta.7.1 validation compiles the adapter with strict warnings and runs a protocol reflection simulation against stubs matching the documented API names and types. This proves the bridge logic and package hygiene; it is not a substitute for a live Hytale client/server smoke test.