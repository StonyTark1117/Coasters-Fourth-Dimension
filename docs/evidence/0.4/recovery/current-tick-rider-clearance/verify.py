#!/usr/bin/env python3
"""Verify the current one-tick native clearance record and archived inputs."""

import hashlib
import json
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parent


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


for line in (root / "SHA256SUMS").read_text().splitlines():
    expected, name = line.split("  ", 1)
    assert sha(root / name) == expected, name

log = subprocess.check_output(["zstd", "-dc", str(root / "gametest.log.zst")], text=True)
assert "All 50 required tests passed :)" in log
assert "BUILD SUCCESSFUL in 5m 23s" in log
assert "FD_RECOVERY_BANK family=ARROW" in log
assert "FD_RECOVERY_BANK_TRAIN family=PAX" in log

for stem, families, minimum in (
    ("single", {"ARROW", "LEGACY", "FREE_SPIN", "REVISED", "PAX"}, 5700),
    ("train", {"ARROW", "PAX"}, 6500),
):
    result = json.loads((root / f"{stem}-results.json").read_text())
    assert result["sampleIntervalTicks"] == 1
    assert result["framesSha256"] == sha(root / f"{stem}-frames.json")
    raw = subprocess.check_output(["zstd", "-dc", str(root / f"{stem}-trace.jsonl.zst")])
    assert result["traceSha256"] == hashlib.sha256(raw).hexdigest()
    assert {r["family"] for r in result["routes"]} == families
    for route in result["routes"]:
        assert route["samples"] >= minimum, route["family"]
        assert route["passes"] and not route["uncertifiedPairs"], route["family"]
        assert route["riderRackEnvelopeCount"] == 2 * route["riderEnvelopeCount"]
        assert route["riderRackMinimumSeparatingBound"] >= .02
        assert route["riderMinimumSeparatingBound"] >= .02
        assert route["minimumSeparatingBound"] >= .02
        assert route["allCarsVisitedSections"] == 5
print("Current FD402 one-tick rider/rack clearance: verified")
