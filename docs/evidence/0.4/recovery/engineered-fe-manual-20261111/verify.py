#!/usr/bin/env python3
import hashlib
import pathlib
import subprocess

ROOT = pathlib.Path(__file__).resolve().parent


def require(condition, message):
    if not condition:
        raise SystemExit(message)


manifest = {}
for line in (ROOT / "sha256.txt").read_text().splitlines():
    digest, name = line.split("  ", 1)
    manifest[name] = digest
for name, expected in manifest.items():
    actual = hashlib.sha256((ROOT / name).read_bytes()).hexdigest()
    require(actual == expected, f"hash mismatch: {name}")

log = subprocess.check_output(["zstd", "-dc", ROOT / "latest.log.zst"], text=True)
launch = (ROOT / "direct-client-launch.sh").read_text()
require("Coasters: Engineered 1.1.1" in log, "Engineered 1.1.1 marker missing")
require("Coasters: Track Styles" not in log, "run was not Engineered-only")
require("java version 21.0.13" in log, "Java 21 marker missing")
require("/usr/lib/jvm/java-21-openjdk/bin/java" in launch, "Java 21 launch missing")
require("-Xmx1536M" in launch, "1536 MiB heap missing")
for marker in (
    "position=( 5.000E-1  1.321E+2  4.964E+1)",
    "position=( 5.000E-1  1.321E+2  4.904E+1)",
    "velocity=(-9.128E-8 -6.132E-4 -4.816E-1)",
    "position=( 5.000E-1  1.321E+2  4.716E+1)",
    "velocity=(-3.860E-8 -6.120E-4 -4.886E-1)",
    "following block data: 100000",
    "following block data: 87762",
    "following block data: 86376",
    "Recovery bearing selected. Open the retained end car and choose Attach recovery.",
    "FD402_STOW_RAN",
):
    require(marker in log, f"log marker missing: {marker}")
require(len(list((ROOT / "screenshots").glob("*.png"))) == 27, "screenshot count changed")
print("FD402F1 evidence verified")
