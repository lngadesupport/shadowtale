import json
from pathlib import Path

from src.shadowtale_validator import format_report, validate_pack


def write_json(path: Path, data: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data), encoding="utf-8")


def make_manifest(root: Path, **overrides: object) -> None:
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
    data.update(overrides)
    write_json(root / "manifest.json", data)


def issue_codes(report):
    return {issue.code for issue in report.issues}


def test_missing_manifest_is_fail(tmp_path: Path):
    report = validate_pack(tmp_path)
    assert report.has_failures
    assert "MANIFEST_MISSING" in issue_codes(report)


def test_manifest_requires_documented_fields(tmp_path: Path):
    make_manifest(tmp_path)
    data = json.loads((tmp_path / "manifest.json").read_text(encoding="utf-8"))
    del data["Authors"]
    write_json(tmp_path / "manifest.json", data)

    report = validate_pack(tmp_path)
    assert report.has_failures
    assert "MANIFEST_FIELD_MISSING" in issue_codes(report)


def test_manifest_accepts_documented_field_types(tmp_path: Path):
    make_manifest(tmp_path)
    report = validate_pack(tmp_path)
    assert not report.has_failures
    assert "MANIFEST_VALID" in issue_codes(report)


def test_invalid_json_is_fail(tmp_path: Path):
    make_manifest(tmp_path)
    asset = tmp_path / "Server" / "Weathers" / "ShadowTale_Test.json"
    asset.parent.mkdir(parents=True)
    asset.write_text('{"Parent": ', encoding="utf-8")

    report = validate_pack(tmp_path)
    assert report.has_failures
    assert "JSON_INVALID" in issue_codes(report)


def test_supported_asset_extensions_are_discovered(tmp_path: Path):
    make_manifest(tmp_path)
    write_json(tmp_path / "Server" / "Environments" / "ShadowTale_Test.json", {})
    write_json(tmp_path / "Server" / "Weathers" / "ShadowTale_Test.json", {})
    write_json(tmp_path / "Server" / "Item" / "Block" / "Fluids" / "ShadowTale_Test.json", {})
    write_json(tmp_path / "Server" / "Item" / "Block" / "Particles" / "ShadowTale_Test.json", {})
    write_json(tmp_path / "Server" / "Item" / "ConnectedBlockRuleSets" / "ShadowTale_Test.json", {})
    particle = tmp_path / "Server" / "Particles" / "ShadowTale_Test.particlesystem"
    particle.parent.mkdir(parents=True, exist_ok=True)
    particle.write_text("{}", encoding="utf-8")

    report = validate_pack(tmp_path)
    assert not report.has_failures
    assert "ASSETS_VALID" in issue_codes(report)


def test_unsupported_asset_path_is_fail(tmp_path: Path):
    make_manifest(tmp_path)
    write_json(tmp_path / "Server" / "MadeUp" / "ShadowTale_Test.json", {})

    report = validate_pack(tmp_path)
    assert report.has_failures
    assert "UNSUPPORTED_ASSET_PATH" in issue_codes(report)


def test_missing_parent_reference_is_external_warning(tmp_path: Path):
    make_manifest(tmp_path)
    write_json(
        tmp_path / "Server" / "Weathers" / "ShadowTale_Test.json",
        {"Parent": "ShadowTale_Missing"},
    )

    report = validate_pack(tmp_path)
    assert not report.has_failures
    assert "PARENT_EXTERNAL_OR_BASE" in issue_codes(report)


def test_parent_cycle_is_fail(tmp_path: Path):
    make_manifest(tmp_path)
    write_json(
        tmp_path / "Server" / "Weathers" / "ShadowTale_A.json",
        {"Parent": "ShadowTale_B"},
    )
    write_json(
        tmp_path / "Server" / "Weathers" / "ShadowTale_B.json",
        {"Parent": "ShadowTale_A"},
    )

    report = validate_pack(tmp_path)
    assert report.has_failures
    assert "PARENT_CYCLE" in issue_codes(report)


def test_existing_parent_reference_is_pass(tmp_path: Path):
    make_manifest(tmp_path)
    write_json(tmp_path / "Server" / "Weathers" / "ShadowTale_Base.json", {})
    write_json(
        tmp_path / "Server" / "Weathers" / "ShadowTale_Child.json",
        {"Parent": "ShadowTale_Base"},
    )

    report = validate_pack(tmp_path)
    assert not report.has_failures
    assert "PARENT_RESOLVED" in issue_codes(report)


def test_report_format_is_structured(tmp_path: Path):
    report = validate_pack(tmp_path)
    output = format_report(report)
    assert "FAIL" in output
    assert "MANIFEST_MISSING" in output


def test_empty_asset_pack_warns_until_visual_assets_exist(tmp_path: Path):
    make_manifest(tmp_path)
    report = validate_pack(tmp_path)
    assert not report.has_failures
    assert "ASSETS_EMPTY" in issue_codes(report)
    assert report.counts["WARN"] == 1
