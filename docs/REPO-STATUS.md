# Repository status

## Confirmed

- Repository: lngadesupport/shadowtale
- Default branch: main
- Initial commit: 1e7de89c818cf6e555cd47f6033f177c1b82f647
- Initial repository contained only README.md.
- Working branch: beta6-core-session
- Core source added under src/main/java/com/dynamictale/camera.

## Why the original beta.5 was not copied

The validated beta.5 package was referenced in previous project records, but its bytes/source are not currently available through the repository or attached project files. Reconstructing a different implementation and calling it beta.5 would be incorrect.

## Integration target

The official Hytale API currently documents:

- com.hypixel.hytale.protocol.ServerCameraSettings
- com.hypixel.hytale.protocol.packets.camera.SetServerCamera
- com.hypixel.hytale.server.core.plugin.JavaPlugin

The adapter should keep the state machine independent of these classes and use them only at the transport/plugin boundary.

## Current blocker

A real Hytale build cannot be verified from this repository alone until the exact server API dependency used by the target build is supplied/available. Do not commit a proprietary HytaleServer.jar to the repository.

## Planned sequence

1. Recover or recreate the real Hytale plugin entry point.
2. Attach the exact Hytale server dependency locally.
3. Implement the camera transport adapter.
4. Restore V/Alt input handling and world/session lifecycle.
5. Run compile + regression tests.
6. Build the installable JAR.
7. Validate the camera visually in Hytale.
