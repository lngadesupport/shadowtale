import json
import zipfile
from pathlib import Path

import pytest

from src.shadowtale_package import build_release, release_name


def make_manifest(root: Path, version: str = "0.1.0") -> None:
    data = {
        "Group": "ShadowTale",
        "Name": "ShadowTale",
        "Version": version,
        "Description": "Cinematic graphics overhaul for Hytale Release 0.6.8.",
        "Authors": [{"Name": "ShadowTale Team", "Email": "", "Url": ""}],
        "Website": "",
        "Dependencies": {},
        "OptionalDependencies": {},
        "LoadBefore": {},
        "DisabledByDefault": False,
        "IncludesAssetPack": False,
        "SubPlugins": [],
    }
    root.mkdir(parents=True, exist_ok=True)
    (root / "manifest.json").write_text(json.dumps(data), encoding="utf-8")


def test_release_name_includes_shadowtale_version_and_hytale_version():
    assert release_name("0.1.0", "0.6.8") == "ShadowTale-0.1.0-Hytale-0.6.8.zip"


def test_release_zip_contains_only_pack_files(tmp_path: Path):
    pack = tmp_path / "pack"
    make_manifest(pack)
    asset = pack / "Server" / "Weathers" / "ShadowTale_Test.json"
    asset.parent.mkdir(parents=True)
    asset.write_text("{}", encoding="utf-8")
    (pack / ".gitkeep").write_text("", encoding="utf-8")
    (pack / "Server" / "Weathers" / ".hidden").write_text("", encoding="utf-8")

    output = build_release(pack, tmp_path / "dist")

    with zipfile.ZipFile(output) as archive:
        assert archive.namelist() == ["Server/Weathers/ShadowTale_Test.json", "manifest.json"]


def test_packaging_refuses_validation_failures(tmp_path: Path):
    pack = tmp_path / "pack"
    make_manifest(pack)
    bad_asset = pack / "Server" / "MadeUp" / "ShadowTale_Test.json"
    bad_asset.parent.mkdir(parents=True)
    bad_asset.write_text("{}", encoding="utf-8")

    with pytest.raises(ValueError, match="validation failed"):
        build_release(pack, tmp_path / "dist")


def test_package_cli_runs_from_repository_root(tmp_path: Path):
    import subprocess
    import sys

    repo = Path(__file__).resolve().parents[1]
    result = subprocess.run(
        [
            sys.executable,
            str(repo / "scripts" / "package_shadowtale.py"),
            "--pack-root",
            str(repo / "pack"),
            "--output-dir",
            str(tmp_path / "dist"),
            "--hytale-version",
            "0.6.8",
        ],
        cwd=repo,
        capture_output=True,
        text=True,
    )
    assert result.returncode == 0, result.stderr
    assert (tmp_path / "dist" / "ShadowTale-0.1.0-Hytale-0.6.8.zip").is_file()
