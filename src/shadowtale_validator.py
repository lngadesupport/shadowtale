from __future__ import annotations

import json
from dataclasses import dataclass
from pathlib import Path
from typing import Any


MANIFEST_FIELDS: dict[str, type] = {
    "Group": str,
    "Name": str,
    "Version": str,
    "Description": str,
    "Authors": list,
    "Website": str,
    "Dependencies": dict,
    "OptionalDependencies": dict,
    "LoadBefore": dict,
    "DisabledByDefault": bool,
    "IncludesAssetPack": bool,
    "SubPlugins": list,
}

SUPPORTED_ASSET_FAMILIES: tuple[tuple[str, str, str], ...] = (
    ("Server/Environments", ".json", "Environment"),
    ("Server/Weathers", ".json", "Weather"),
    ("Server/Particles", ".particlesystem", "ParticleSystem"),
    ("Server/Item/Block/Fluids", ".json", "Fluid"),
    ("Server/Item/Block/FluidFX", ".json", "FluidFX"),
    ("Server/Item/Block/Particles", ".json", "BlockParticleSet"),
    ("Server/Item/Block/Blocks", ".json", "BlockType"),
    ("Server/Item/ConnectedBlockRuleSets", ".json", "ConnectedBlockRuleSet"),
)


@dataclass(frozen=True)
class ValidationIssue:
    level: str
    code: str
    message: str
    path: str | None = None


@dataclass
class ValidationReport:
    issues: list[ValidationIssue]

    @property
    def has_failures(self) -> bool:
        return any(issue.level == "FAIL" for issue in self.issues)

    @property
    def counts(self) -> dict[str, int]:
        return {
            level: sum(issue.level == level for issue in self.issues)
            for level in ("PASS", "WARN", "FAIL")
        }


def _issue(issues: list[ValidationIssue], level: str, code: str, message: str, path: Path | None = None) -> None:
    issues.append(
        ValidationIssue(
            level=level,
            code=code,
            message=message,
            path=path.as_posix() if path else None,
        )
    )


def _read_json(path: Path) -> Any:
    return json.loads(path.read_text(encoding="utf-8"))


def _manifest_checks(pack_root: Path, issues: list[ValidationIssue]) -> None:
    manifest = pack_root / "manifest.json"
    if not manifest.is_file():
        _issue(issues, "FAIL", "MANIFEST_MISSING", "manifest.json is missing", manifest)
        return

    try:
        data = _read_json(manifest)
    except (OSError, UnicodeDecodeError, json.JSONDecodeError) as exc:
        _issue(issues, "FAIL", "MANIFEST_INVALID_JSON", f"manifest.json is not valid UTF-8 JSON: {exc}", manifest)
        return

    if not isinstance(data, dict):
        _issue(issues, "FAIL", "MANIFEST_NOT_OBJECT", "manifest.json root must be a JSON object", manifest)
        return

    for field, expected_type in MANIFEST_FIELDS.items():
        if field not in data:
            _issue(issues, "FAIL", "MANIFEST_FIELD_MISSING", f"documented manifest field is missing: {field}", manifest)
            continue
        if type(data[field]) is not expected_type:
            _issue(
                issues,
                "FAIL",
                "MANIFEST_FIELD_TYPE",
                f"manifest field {field} must be {expected_type.__name__}",
                manifest,
            )

    unknown = sorted(set(data) - set(MANIFEST_FIELDS))
    for field in unknown:
        _issue(
            issues,
            "WARN",
            "MANIFEST_UNKNOWN_FIELD",
            f"manifest field is not part of the documented 0.6.8 example: {field}",
            manifest,
        )

    if not any(issue.code.startswith("MANIFEST_") and issue.level == "FAIL" for issue in issues):
        _issue(issues, "PASS", "MANIFEST_VALID", "manifest.json satisfies the documented manifest contract", manifest)


def _match_asset(path: Path) -> tuple[str, str] | None:
    normalized = path.as_posix()
    for directory, extension, family in SUPPORTED_ASSET_FAMILIES:
        prefix = f"{directory}/"
        if normalized.startswith(prefix) and path.suffix.lower() == extension:
            relative_name = normalized[len(prefix) :]
            if "/" not in relative_name and relative_name:
                return family, path.stem
    return None


def _asset_scan(pack_root: Path, issues: list[ValidationIssue]) -> dict[tuple[str, str], Path]:
    asset_index: dict[tuple[str, str], Path] = {}
    server_root = pack_root / "Server"
    if not server_root.exists():
        _issue(issues, "WARN", "ASSETS_EMPTY", "no Server assets are present yet; asset validation is ready for future modules")
        return asset_index

    supported_files = 0
    for path in sorted(server_root.rglob("*")):
        if not path.is_file() or path.name.startswith("."):
            continue
        match = _match_asset(path.relative_to(pack_root))
        if match is None:
            _issue(
                issues,
                "FAIL",
                "UNSUPPORTED_ASSET_PATH",
                "asset path is not in the documented ShadowTale 0.6.8 foundation scope",
                path.relative_to(pack_root),
            )
            continue

        family, asset_id = match
        key = (family, asset_id)
        if key in asset_index:
            _issue(issues, "FAIL", "DUPLICATE_ASSET_ID", f"duplicate {family} asset id: {asset_id}", path.relative_to(pack_root))
            continue
        asset_index[key] = path
        supported_files += 1

        try:
            value = _read_json(path)
        except (OSError, UnicodeDecodeError, json.JSONDecodeError) as exc:
            _issue(issues, "FAIL", "JSON_INVALID", f"asset is not valid UTF-8 JSON: {exc}", path.relative_to(pack_root))
            continue
        if not isinstance(value, dict):
            _issue(issues, "FAIL", "ASSET_NOT_OBJECT", "asset root must be a JSON object", path.relative_to(pack_root))

    if supported_files == 0 and not any(issue.level == "FAIL" for issue in issues):
        _issue(issues, "WARN", "ASSETS_EMPTY", "no supported Server assets are present yet; visual implementation is still pending")
    elif not any(issue.level == "FAIL" and issue.code in {"UNSUPPORTED_ASSET_PATH", "DUPLICATE_ASSET_ID", "JSON_INVALID", "ASSET_NOT_OBJECT"} for issue in issues):
        _issue(issues, "PASS", "ASSETS_VALID", f"validated {supported_files} supported asset file(s)")

    return asset_index


def load_base_asset_index(index_path: Path, expected_hytale_version: str = "0.6.8") -> set[tuple[str, str]]:
    data = json.loads(Path(index_path).read_text(encoding="utf-8"))
    target_version = data.get("target_hytale_version")
    if target_version != expected_hytale_version:
        raise ValueError(
            f"base asset index targets Hytale {target_version!r}, expected {expected_hytale_version!r}"
        )
    records = data.get("records")
    if not isinstance(records, list):
        raise ValueError("base asset index is missing a records array")
    resolved: set[tuple[str, str]] = set()
    for record in records:
        if not isinstance(record, dict):
            continue
        family = record.get("family")
        asset_id = record.get("id")
        if isinstance(family, str) and isinstance(asset_id, str) and asset_id:
            resolved.add((family, asset_id))
    return resolved


def _parent_checks(
    pack_root: Path,
    asset_index: dict[tuple[str, str], Path],
    issues: list[ValidationIssue],
    base_asset_index: set[tuple[str, str]] | None = None,
) -> None:
    parent_edges: dict[tuple[str, str], tuple[str, str]] = {}

    for (family, asset_id), path in sorted(asset_index.items()):
        try:
            data = _read_json(path)
        except (OSError, UnicodeDecodeError, json.JSONDecodeError):
            continue
        if not isinstance(data, dict) or "Parent" not in data:
            continue
        parent = data["Parent"]
        if not isinstance(parent, str) or not parent:
            _issue(issues, "FAIL", "PARENT_INVALID", "Parent must be a non-empty string when present", path.relative_to(pack_root))
            continue

        parent_key = (family, parent)
        if parent_key in asset_index:
            parent_edges[(family, asset_id)] = parent_key
            _issue(issues, "PASS", "PARENT_RESOLVED", f"resolved local Parent reference: {parent}", path.relative_to(pack_root))
        elif base_asset_index is not None and parent_key in base_asset_index:
            _issue(issues, "PASS", "PARENT_BASE_RESOLVED", f"resolved base-game Parent reference: {parent}", path.relative_to(pack_root))
        else:
            _issue(
                issues,
                "WARN",
                "PARENT_EXTERNAL_OR_BASE",
                f"Parent {parent!r} is not shipped by ShadowTale; it may refer to a base-game or external asset",
                path.relative_to(pack_root),
            )

    visiting: set[tuple[str, str]] = set()
    visited: set[tuple[str, str]] = set()

    def visit(node: tuple[str, str], chain: list[tuple[str, str]]) -> None:
        if node in visiting:
            cycle = " -> ".join(f"{family}:{asset_id}" for family, asset_id in chain + [node])
            _issue(issues, "FAIL", "PARENT_CYCLE", f"Parent inheritance cycle detected: {cycle}")
            return
        if node in visited:
            return
        visiting.add(node)
        parent = parent_edges.get(node)
        if parent is not None:
            visit(parent, chain + [node])
        visiting.remove(node)
        visited.add(node)

    for node in sorted(parent_edges):
        visit(node, [])


def validate_pack(pack_root: Path, base_asset_index: set[tuple[str, str]] | None = None) -> ValidationReport:
    pack_root = Path(pack_root)
    issues: list[ValidationIssue] = []
    if not pack_root.exists() or not pack_root.is_dir():
        _issue(issues, "FAIL", "PACK_ROOT_INVALID", "pack root does not exist or is not a directory")
        return ValidationReport(issues)

    _manifest_checks(pack_root, issues)
    asset_index = _asset_scan(pack_root, issues)
    _parent_checks(pack_root, asset_index, issues, base_asset_index)
    return ValidationReport(issues)


def format_report(report: ValidationReport) -> str:
    lines = [
        "ShadowTale validation report",
        f"PASS={report.counts['PASS']} WARN={report.counts['WARN']} FAIL={report.counts['FAIL']}",
    ]
    for issue in report.issues:
        location = f" [{issue.path}]" if issue.path else ""
        lines.append(f"{issue.level} {issue.code}{location}: {issue.message}")
    return "\n".join(lines)
