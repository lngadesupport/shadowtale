from pathlib import Path
import json
import zipfile

from src.shadowtale_audit import audit_assets

def add(archive: zipfile.ZipFile, name: str, data: str | bytes) -> None:
    archive.writestr(name, data)

def test_audit_builds_real_asset_inventory(tmp_path: Path):
    source = tmp_path / "Assets.zip"
    with zipfile.ZipFile(source, "w") as archive:
        add(archive, "Server/Environments/Overworld.json", json.dumps({"WaterTint": "#ffffff", "WeatherForecasts": {}}))
        add(archive, "Server/Weathers/Rain.json", json.dumps({"FogDistance": [-8, 256], "SunColors": [], "Clouds": []}))
        add(archive, "Server/Particles/Rain.particlesystem", "{}")
        add(archive, "Server/Item/Block/Fluids/Water.json", json.dumps({"FluidFXId": "Water"}))
        add(archive, "Server/Item/Block/FluidFX/Water.json", json.dumps({"Particle": {"SystemId": "Rain"}}))
        add(archive, "Common/Sky/Day.png", b"test")
    output = audit_assets(source, tmp_path / "real-assets")
    report = json.loads(output.read_text(encoding="utf-8"))
    assert report["target_hytale_version"] == "0.6.8"
    assert report["assets"]["counts_by_family"]["Weather"] == 1
    assert report["candidate_overrides"]["Weather"][0]["id"] == "Rain"
    assert report["common_assets"]["common_relevant_counts"]["Common/Sky/"] == 1
    assert len(report["source"]["sha256"]) == 64

def test_audit_records_malformed_json(tmp_path: Path):
    source = tmp_path / "Assets.zip"
    with zipfile.ZipFile(source, "w") as archive:
        add(archive, "Server/Weathers/Broken.json", '{"FogDistance":')
    output = audit_assets(source, tmp_path / "real-assets")
    report = json.loads(output.read_text(encoding="utf-8"))
    assert report["assets"]["json_parse_failures"][0]["path"] == "Server/Weathers/Broken.json"
