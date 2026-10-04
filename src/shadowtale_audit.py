from __future__ import annotations

import argparse
import hashlib
import json
import os
import zipfile
from collections import Counter, defaultdict
from pathlib import Path
from typing import Any, Iterable

AUDIT_VERSION = "0.1.0"
TARGET_HYTALE_VERSION = "0.6.8"

ASSET_RULES = (
    ("Server/Environments", ".json", "Environment"),
    ("Server/Weathers", ".json", "Weather"),
    ("Server/Particles", ".particlesystem", "ParticleSystem"),
    ("Server/Particles", ".particlespawner", "ParticleSpawner"),
    ("Server/Item/Block/Fluids", ".json", "Fluid"),
    ("Server/Item/Block/FluidFX", ".json", "FluidFX"),
    ("Server/Item/Block/Particles", ".json", "BlockParticleSet"),
    ("Server/Item/Block/Blocks", ".json", "BlockType"),
    ("Server/Item/ConnectedBlockRuleSets", ".json", "ConnectedBlockRuleSet"),
    ("Server/Models", ".json", "ModelAsset"),
)

COMMON_RELEVANT_PREFIXES = (
    "Common/Sky/",
    "Common/ScreenEffects/",
    "Common/VFX/",
    "Common/Characters/",
    "Common/Items/",
    "Common/Blocks/",
    "Common/BlockTextures/",
    "Common/Resources/",
)

RANK_FIELDS = {
    "Environment": ("WaterTint", "FluidParticles", "WeatherForecasts", "WeatherForecastSeed"),
    "Weather": (
        "Stars", "ScreenEffect", "FogDistance", "FogOptions", "Particle",
        "ScreenEffectColors", "SunlightDampingMultipliers", "SunlightColors",
        "SunColors", "MoonColors", "SunGlowColors", "MoonGlowColors",
        "SunScales", "MoonScales", "SkyTopColors", "SkyBottomColors",
        "SkySunsetColors", "FogColors", "FogHeightFalloffs", "FogDensities",
        "WaterTints", "ColorFilters", "Moons", "Clouds",
    ),
}

def _sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()

def _sha256_bytes(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()

def _normal(path: str) -> str:
    return path.replace("\\", "/").lstrip("./")

def _match_asset(path: str) -> tuple[str, str] | None:
    normalized = _normal(path)
    folded = normalized.casefold()
    for directory, extension, family in ASSET_RULES:
        prefix = directory.casefold() + "/"
        if folded.startswith(prefix) and folded.endswith(extension.casefold()):
            relative = normalized[len(directory) + 1:]
            if relative and "/" not in relative:
                return family, Path(relative).stem
    return None

def _json(raw: bytes) -> Any:
    return json.loads(raw.decode("utf-8"))

def _fields(value: Any) -> tuple[str, ...]:
    return tuple(sorted(value)) if isinstance(value, dict) else ()

def _score(family: str, fields: Iterable[str]) -> int:
    wanted = RANK_FIELDS.get(family, ())
    actual = set(fields)
    return sum(name in actual for name in wanted)

def _relevant_common(path: str) -> bool:
    return _normal(path).startswith(COMMON_RELEVANT_PREFIXES)

def _cycles(records: list[dict[str, Any]]) -> list[list[str]]:
    parents = {
        (record["family"], record["id"]): (record["family"], record["parent"])
        for record in records
        if record.get("parent")
    }
    state: dict[tuple[str, str], int] = {}
    stack: list[tuple[str, str]] = []
    found: list[list[str]] = []

    def visit(node: tuple[str, str]) -> None:
        status = state.get(node, 0)
        if status == 1:
            start = stack.index(node) if node in stack else 0
            found.append([f"{family}:{asset_id}" for family, asset_id in stack[start:] + [node]])
            return
        if status == 2:
            return
        state[node] = 1
        stack.append(node)
        parent = parents.get(node)
        if parent is not None:
            visit(parent)
        stack.pop()
        state[node] = 2

    for node in sorted(parents):
        visit(node)
    return found

def audit_assets(zip_path: Path, output_dir: Path) -> Path:
    zip_path = Path(zip_path)
    output_dir = Path(output_dir)
    if not zip_path.is_file():
        raise FileNotFoundError(zip_path)
    if not zipfile.is_zipfile(zip_path):
        raise ValueError(f"not a ZIP file: {zip_path}")

    records: list[dict[str, Any]] = []
    counts: Counter[str] = Counter()
    common_counts: Counter[str] = Counter()
    common_examples: dict[str, list[str]] = defaultdict(list)
    malformed: list[dict[str, str]] = []
    duplicate_entries: list[str] = []
    seen_paths: set[str] = set()

    with zipfile.ZipFile(zip_path, "r") as archive:
        for info in archive.infolist():
            if info.is_dir():
                continue
            path = _normal(info.filename)
            if path in seen_paths:
                duplicate_entries.append(path)
                continue
            seen_paths.add(path)

            match = _match_asset(path)
            if match:
                family, asset_id = match
                raw = archive.read(info)
                parent = None
                fields: tuple[str, ...] = ()
                json_ok: bool | None = None
                if info.filename.casefold().endswith((".json", ".particlesystem", ".particlespawner")):
                    try:
                        value = _json(raw)
                        json_ok = isinstance(value, dict)
                        if isinstance(value, dict):
                            parent_value = value.get("Parent")
                            parent = parent_value if isinstance(parent_value, str) else None
                            fields = _fields(value)
                    except (UnicodeDecodeError, json.JSONDecodeError) as exc:
                        json_ok = False
                        malformed.append({"path": path, "error": str(exc)})
                records.append({
                    "family": family,
                    "id": asset_id,
                    "path": path,
                    "size": info.file_size,
                    "sha256": _sha256_bytes(raw),
                    "json_ok": json_ok,
                    "parent": parent,
                    "field_count": len(fields),
                    "fields": list(fields),
                })
                counts[family] += 1
            elif _relevant_common(path):
                prefix = next(prefix for prefix in COMMON_RELEVANT_PREFIXES if path.startswith(prefix))
                common_counts[prefix] += 1
                if len(common_examples[prefix]) < 12:
                    common_examples[prefix].append(path)

    records.sort(key=lambda record: (record["family"], record["id"].casefold(), record["path"].casefold()))

    candidates: dict[str, list[dict[str, Any]]] = {}
    for family in ("Environment", "Weather", "Fluid", "FluidFX", "ParticleSystem"):
        ranked = sorted(
            (record for record in records if record["family"] == family),
            key=lambda record: (-_score(family, record["fields"]), -record["field_count"], record["id"].casefold()),
        )
        candidates[family] = [
            {
                "id": record["id"],
                "path": record["path"],
                "score": _score(family, record["fields"]),
                "field_count": record["field_count"],
                "fields": record["fields"],
                "parent": record["parent"],
            }
            for record in ranked[:12]
        ]

    report = {
        "audit_version": AUDIT_VERSION,
        "target_hytale_version": TARGET_HYTALE_VERSION,
        "source": {
            "path_name": zip_path.name,
            "size": zip_path.stat().st_size,
            "sha256": _sha256_file(zip_path),
        },
        "assets": {
            "total_supported": len(records),
            "counts_by_family": dict(sorted(counts.items())),
            "parent_cycles": _cycles(records),
            "json_parse_failures": malformed,
            "duplicate_zip_entries": sorted(set(duplicate_entries)),
        },
        "candidate_overrides": candidates,
        "common_assets": {
            "common_relevant_counts": dict(sorted(common_counts.items())),
            "common_relevant_examples": {key: value for key, value in sorted(common_examples.items())},
        },
        "records": records,
    }

    output_dir.mkdir(parents=True, exist_ok=True)
    output = output_dir / f"shadowtale-assets-hytale-{TARGET_HYTALE_VERSION}.json"
    output.write_text(json.dumps(report, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    return output

def find_assets_zip(explicit: Path | None = None) -> Path:
    if explicit is not None:
        return explicit
    roots: list[Path] = []
    for name in ("APPDATA", "LOCALAPPDATA", "PROGRAMFILES", "PROGRAMFILES(X86)"):
        value = os.environ.get(name)
        if value:
            roots.append(Path(value))
    matches: list[Path] = []
    for root in roots:
        if not root.exists():
            continue
        try:
            matches.extend(path for path in root.rglob("Assets.zip") if path.is_file())
        except OSError:
            continue
    if not matches:
        raise FileNotFoundError("Assets.zip was not found automatically. Pass --assets <path-to-Assets.zip>.")
    matches.sort(key=lambda path: (len(path.parts), str(path).casefold()))
    return matches[0]

def main() -> int:
    parser = argparse.ArgumentParser(description="Audit the real Hytale 0.6.8 Assets.zip for ShadowTale.")
    parser.add_argument("--assets", type=Path, default=None, help="Path to the Hytale Assets.zip")
    parser.add_argument("--output", type=Path, default=Path("real-assets"), help="Directory for the generated audit JSON")
    args = parser.parse_args()
    try:
        assets = find_assets_zip(args.assets)
        output = audit_assets(assets, args.output)
    except (OSError, ValueError) as exc:
        print(f"FAIL AUDIT: {exc}")
        return 1
    print(f"ASSETS: {assets}")
    print(f"AUDIT PASS: {output}")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
