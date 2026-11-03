#!/usr/bin/env python3
"""Verify the exact-source native Free Spin damper regression record."""

import gzip
import hashlib
import json
from pathlib import Path
import re
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent
SOURCE = "b670499cf5e6aa6801a1d26910dd28a97d060541"
EXPORTS = {
    "bank-native-frames.json",
    "recovery-bank-native-frames.json",
    "recovery-bank-train-native-frames.json",
    "recovery-bank-mesh-trace.jsonl",
    "recovery-bank-train-mesh-trace.jsonl",
}


def sha256(data):
    return hashlib.sha256(data).hexdigest()


for line in (ROOT / "SHA256SUMS").read_text().splitlines():
    expected, name = line.split("  ", 1)
    assert sha256((ROOT / name).read_bytes()) == expected, name

metadata = json.loads((ROOT / "metadata.json").read_text())
manifest = json.loads((ROOT / "native-exports/manifest.json").read_text())
assert metadata["source"] == manifest["source"] == SOURCE
assert metadata["profile"] == manifest["profile"] == "base"
assert metadata["exit_code"] == manifest["reported_process_exit_code"] == 0
assert manifest["capture_complete"] and manifest["candidate_for_separate_acceptance_review"]
assert sha256((ROOT / "metadata.json").read_bytes()) == manifest["metadata_sha256"]
assert set(manifest["files"]) == EXPORTS
for name, entry in manifest["files"].items():
    assert not entry["issues"], name
    compressed = (ROOT / "native-exports" / entry["payload"]).read_bytes()
    assert sha256(compressed) == entry["gzip_sha256"], name
    raw = gzip.decompress(compressed)
    assert len(raw) == entry["bytes"] == entry["source_bytes"], name
    assert sha256(raw) == entry["sha256"], name

log = subprocess.check_output(["zstd", "-dc", str(ROOT / "gametest.log.zst")], text=True)
match = re.search(
    r"FD02_DAMPER impulses=(\d+) energy=([-\d.]+) relativeAngularSpeedReduction=([-\d.]+)",
    log,
)
assert match and int(match[1]) == 483
assert float(match[2]) < -8.5 and float(match[3]) > 4.7
for marker in (
    "All 50 required tests passed :)",
    "All dimensions are saved",
    "BUILD SUCCESSFUL in 4m 38s",
):
    assert marker in log, marker
assert "FAILED" not in log
unit_log = subprocess.check_output(["zstd", "-dc", str(ROOT / "unit-tests.log.zst")], text=True)
assert "BUILD SUCCESSFUL" in unit_log and "> Task :test" in unit_log
archive = ROOT / "unit-results.tar.zst"
members = subprocess.check_output(["tar", "-I", "zstd", "-tf", str(archive)], text=True)
files = [name for name in members.splitlines() if name.startswith("test/TEST-") and name.endswith(".xml")]
assert len(files) == 10
totals = [0, 0, 0]
for name in files:
    data = subprocess.check_output(["tar", "-I", "zstd", "-xOf", str(archive), name])
    attrs = ET.fromstring(data).attrib
    for index, key in enumerate(("tests", "failures", "errors")):
        totals[index] += int(attrs[key])
assert totals == [33, 0, 0]
print("Free Spin native damper record: verified")
