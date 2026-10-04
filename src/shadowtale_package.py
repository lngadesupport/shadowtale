from __future__ import annotations

import json
import zipfile
from pathlib import Path

from .shadowtale_validator import load_base_asset_index, validate_pack


def release_name(version: str, hytale_version: str) -> str:
    return f"ShadowTale-{version}-Hytale-{hytale_version}.zip"


def _pack_files(pack_root: Path) -> list[Path]:
    files: list[Path] = []
    for path in pack_root.rglob("*"):
        if not path.is_file():
            continue
        relative = path.relative_to(pack_root)
        if any(part.startswith(".") for part in relative.parts):
            continue
        if path.name == ".gitkeep":
            continue
        files.append(relative)
    return sorted(files, key=lambda value: value.as_posix())


def build_release(
    pack_root: Path,
    output_dir: Path,
    hytale_version: str = "0.6.8",
    base_index: Path | None = None,
) -> Path:
    pack_root = Path(pack_root)
    output_dir = Path(output_dir)
    base_asset_index = load_base_asset_index(base_index, hytale_version) if base_index is not None else None
    report = validate_pack(pack_root, base_asset_index)
    if report.has_failures:
        raise ValueError(f"validation failed\n{format_report(report)}")

    manifest = json.loads((pack_root / "manifest.json").read_text(encoding="utf-8"))
    version = manifest["Version"]
    output_dir.mkdir(parents=True, exist_ok=True)
    output_path = output_dir / release_name(version, hytale_version)

    with zipfile.ZipFile(
        output_path,
        "w",
        compression=zipfile.ZIP_DEFLATED,
        compresslevel=9,
    ) as archive:
        for relative in _pack_files(pack_root):
            info = zipfile.ZipInfo(relative.as_posix(), date_time=(1980, 1, 1, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            info.external_attr = 0o644 << 16
            archive.writestr(info, (pack_root / relative).read_bytes())

    return output_path
