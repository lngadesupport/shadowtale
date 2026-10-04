import json
from pathlib import Path

from src.shadowtale_validator import load_base_asset_index, validate_pack

def write_json(path: Path, data: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data), encoding="utf-8")

def make_manifest(root: Path) -> None:
    data = {
        "Group": "ShadowTale",
        "Name": "ShadowTale",
        "Version": "0.1.0",
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
    write_json(root / "manifest.json", data)

def test_parent_resolves_from_real_asset_index(tmp_path: Path):
    make_manifest(tmp_path)
    write_json(tmp_path / "Server" / "Weathers" / "ShadowTale_Rain.json", {"Parent": "Rain"})
    index = {
        "target_hytale_version": "0.6.8",
        "records": [{"family": "Weather", "id": "Rain", "path": "Server/Weathers/Rain.json"}],
    }
    index_path = tmp_path / "assets-index.json"
    index_path.write_text(json.dumps(index), encoding="utf-8")

    report = validate_pack(tmp_path, load_base_asset_index(index_path, "0.6.8"))

    assert not report.has_failures
    assert any(issue.code == "PARENT_BASE_RESOLVED" for issue in report.issues)

def test_parent_rejects_mismatched_base_index(tmp_path: Path):
    make_manifest(tmp_path)
    write_json(tmp_path / "Server" / "Weathers" / "ShadowTale_Rain.json", {"Parent": "Rain"})
    index_path = tmp_path / "assets-index.json"
    index_path.write_text(json.dumps({
        "target_hytale_version": "0.6.7",
        "records": [{"family": "Weather", "id": "Rain", "path": "Server/Weathers/Rain.json"}],
    }), encoding="utf-8")

    import pytest
    with pytest.raises(ValueError, match="expected '0.6.8'"):
        load_base_asset_index(index_path, "0.6.8")
