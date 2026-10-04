from __future__ import annotations

import argparse
from pathlib import Path
import sys

REPO_ROOT = Path(__file__).resolve().parents[1]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from src.shadowtale_package import build_release
from src.shadowtale_validator import format_report, load_base_asset_index, validate_pack


def main() -> int:
    parser = argparse.ArgumentParser(description="Build a validated ShadowTale Asset Pack ZIP.")
    parser.add_argument("--pack-root", type=Path, default=Path("pack"))
    parser.add_argument("--output-dir", type=Path, default=Path("dist"))
    parser.add_argument("--hytale-version", default="0.6.8")
    parser.add_argument("--base-index", type=Path, default=None, help="Optional JSON index produced from the real Hytale Assets.zip")
    args = parser.parse_args()

    base_asset_index = load_base_asset_index(args.base_index, args.hytale_version) if args.base_index else None
    report = validate_pack(args.pack_root, base_asset_index)
    print(format_report(report))
    if report.has_failures:
        return 1

    try:
        output = build_release(args.pack_root, args.output_dir, args.hytale_version, args.base_index)
    except (OSError, ValueError, KeyError) as exc:
        print(f"FAIL PACKAGE: {exc}", file=sys.stderr)
        return 1

    print(f"PACKAGE PASS: {output.as_posix()}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
