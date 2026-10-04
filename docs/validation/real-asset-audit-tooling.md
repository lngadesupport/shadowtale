# ShadowTale — Real Asset Audit

## Purpose

This tool bridges the approved ShadowTale foundation to the real Hytale 0.6.8 installation.

It reads the user's original `Assets.zip` **without modifying or extracting over it** and writes a development-only inventory:

    real-assets/
    └── shadowtale-assets-hytale-0.6.8.json

## What is collected

- supported 0.6.8 server asset families and exact IDs;
- file paths, sizes and SHA-256 hashes;
- `Parent` values;
- discovered top-level fields for JSON-like assets;
- malformed supported assets;
- duplicate ZIP entries;
- Parent-cycle inspection;
- relevant `Common/Sky`, `Common/ScreenEffects`, `Common/VFX` and material/texture-adjacent paths;
- ranked candidates for Environment, Weather, Fluid, FluidFX and ParticleSystem overrides.
- inventory of selected Common asset prefixes for follow-up inspection (these are discovery hints, not assumed codecs).

The audit is intentionally inventory-first. It does **not** invent a ShadowTale asset ID and it does not generate a release package from the base-game archive.

## Usage

From Windows:

    scripts\RealAsset-Audit-ShadowTale.cmd --assets "C:\path\to\Assets.zip"

The `--assets` argument can be omitted when the script can automatically locate an `Assets.zip` beneath the standard Windows application directories.

## Next visual step

The first actual visual layer is **Weather**, because the official Release 0.6.8 codec exposes the documented cinematic controls there: fog distance/options, sky and sunlight/moon color curves, fog curves, water tint curves, moons and clouds.

`Environment` remains the per-environment context layer, using its smaller documented surface for water tint, fluid particles and weather forecasts.

Only after the real inventory exists will ShadowTale write concrete `Parent` overrides into `pack/`.
