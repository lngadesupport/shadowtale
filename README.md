# DymanicTale

Standalone Hytale camera plugin. The graphics mod is intentionally a separate project and is not a dependency of this JAR.

## beta.7 integration

This branch adds the real Hytale plugin boundary around the validated camera core:

- JavaPlugin entry point: com.dynamictale.hytale.DymanicTalePlugin
- Server camera transport through SetServerCamera / ServerCameraSettings
- Inbound packet watcher for client movement-state packets
- Default Hytale Walk state (normally Left Alt) mapped to DymanicTale free-look
- Right-shoulder third-person profile with smooth camera interpolation and raycast distance offset
- Free-look locomotion kept aligned to body yaw
- Hytale Server dependency kept compileOnly; it is not bundled into the final JAR

## Input boundary

Hytale server plugins do not receive raw keyboard events. DymanicTale therefore uses the server-visible walking movement state for Alt/free-look instead of inventing an unsupported raw-key API.

The V camera-switch key is not exposed as a raw inbound server packet in the documented packet set. The custom camera packet is sent unlocked so the native client camera switch remains available; exact V behavior still requires a live Hytale client/server smoke test on the target build.

## Repository status

The original beta.5 source/JAR is still unavailable in the current workspace, so this integration is based on the validated beta.6.1 core and the documented Hytale server API. No proprietary HytaleServer.jar is committed to the repository.

## Build

Requires JDK 25 and access to the Hytale release Maven repository.

    gradle build

The intended output is DymanicTale-0.3.0-beta7.jar. Install the resulting JAR in the Hytale server Mods directory.

## Verification

Local verification uses a minimal compile-only API stub and a real production-only package step:

- Java compilation with -Xlint:all -Werror: PASS
- beta.6 core regression suite: 95/95
- Alt input bridge test: PASS
- camera profile test: PASS
- packet watcher integration test: PASS
- final JAR: no Hytale stub classes
- final JAR: no test classes
- plugin entry point and manifest: present

This is source/package verification only. It does not claim a live Hytale runtime test.
