# DymanicTale

Standalone Hytale camera plugin. The graphics mod is intentionally a separate project and is not a dependency of this JAR.

## beta.7.1 integration

- JavaPlugin entry point: com.dynamictale.hytale.DymanicTalePlugin
- Server camera transport through SetServerCamera / ServerCameraSettings
- Right-shoulder third-person profile: +0.90 lateral offset, 4-block distance, raycast collision
- Block-aware mouse targeting: MouseInputTargetType.Block + MouseInputType.LookAtTargetBlock
- Walk movement state drives free-look; on default controls this is Left Alt
- Free-look keeps locomotion aligned to the stored body yaw
- Disconnect cleanup for per-player camera sessions
- Hytale Server remains compileOnly and is never bundled into the JAR

## Input boundary

The server API exposes movement state rather than raw keyboard events. DymanicTale therefore follows Hytale's Walk state instead of inventing a raw Alt event. If the player rebinds Walk, the bridge follows the resulting server movement state.

The V camera-switch key is not exposed as a raw inbound server packet in the documented packet set. The custom camera packet is sent unlocked, preserving the native client camera-switch path; exact V behavior still requires a live Hytale client/server smoke test.

## API compatibility strategy

The protocol portion of the camera bridge is reflected at runtime. This deliberately avoids hard-linking the adapter to generated protocol field types that can change across Hytale API revisions. The documented field names and enum values are still used exactly.

## Build

Requires JDK 25 and access to the Hytale release Maven repository.

    gradle build

The intended output is DymanicTale-0.3.0-beta7.1.jar. Install the resulting JAR in the Hytale server Mods directory.

## Verification boundary

Local verification performed for beta.7.1:

- adapter compilation with -Xlint:all -Werror: PASS
- reflection bridge simulation using the documented protocol field/enum names: PASS
- right shoulder +0.90 / 4-block distance: PASS
- block targeting: PASS
- Alt/Walk press/release transition: PASS
- native camera packet remains unlocked: PASS

The previously validated beta.6.1 core remains unchanged. No live Hytale client/server smoke test has been performed in this environment.