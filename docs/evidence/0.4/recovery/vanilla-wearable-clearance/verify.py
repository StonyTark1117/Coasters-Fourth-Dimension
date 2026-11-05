#!/usr/bin/env python3
"""Verify the FD402 sampled vanilla wearable-clearance checkpoint."""

import hashlib
import json
from pathlib import Path
import subprocess


HERE = Path(__file__).resolve().parent
REPO = HERE.parents[4]
TRACE_INPUTS = HERE.parent / "current-tick-rider-clearance"
BASELINES = HERE.parent / "player-rack-clearance"


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


for line in (HERE / "SHA256SUMS").read_text().splitlines():
    expected, name = line.split("  ", 1)
    assert sha(HERE / name) == expected, name

production_snapshots = {
    "MechanicalPartBlock.java": REPO / "src/main/java/dev/fourthdimension/vehicle/MechanicalPartBlock.java",
    "VehicleParts.java": REPO / "src/main/java/dev/fourthdimension/vehicle/VehicleParts.java",
    "generate-content.py.txt": REPO / "scripts/generate-content.py",
    "carrier_axle.json": REPO / "src/main/resources/assets/fourthdimension/models/block/carrier_axle.json",
    "free_spin_axle.json": REPO / "src/main/resources/assets/fourthdimension/models/block/free_spin_axle.json",
    "legacy_axle.json": REPO / "src/main/resources/assets/fourthdimension/models/block/legacy_axle.json",
    "revised_axle.json": REPO / "src/main/resources/assets/fourthdimension/models/block/revised_axle.json",
}
for snapshot, current in production_snapshots.items():
    assert sha(HERE / snapshot) == sha(current), snapshot
assert sha(HERE / "audit-recovery-player-wearables.py.txt") == sha(
    REPO / "scripts/audit-recovery-player-wearables.py")

expected_routes = {
    "single": {"ARROW": (1, 5768, 4), "LEGACY": (1, 5845, 8),
               "FREE_SPIN": (1, 5873, 8), "REVISED": (1, 5865, 8),
               "PAX": (1, 5767, 4)},
    "train": {"ARROW": (2, 6548, 8), "PAX": (4, 6977, 16)},
}
for stem, expected in expected_routes.items():
    report = json.loads((HERE / f"{stem}-results.json").read_text())
    assert report["wearableVerifierSha256"] == sha(
        HERE / "audit-recovery-player-wearables.py.txt")
    assert report["meshVerifierSha256"] == sha(
        BASELINES / "audit-recovery-mesh-trace.py.txt")
    assert report["framesSha256"] == sha(TRACE_INPUTS / f"{stem}-frames.json")
    assert report["traceArchiveSha256"] == sha(
        TRACE_INPUTS / f"{stem}-trace.jsonl.zst")
    raw = subprocess.check_output(
        ["zstd", "-dc", str(TRACE_INPUTS / f"{stem}-trace.jsonl.zst")])
    assert report["traceRawSha256"] == hashlib.sha256(raw).hexdigest()
    assert report["baselineReportSha256"] == sha(BASELINES / f"{stem}-results.json")
    assert len(report["wearableParts"]) == 11
    assert len(set(report["wearableParts"])) == 11
    assert {route["family"] for route in report["routes"]} == set(expected)
    for route in report["routes"]:
        cars, samples, seats = expected[route["family"]]
        assert (route["cars"], route["samples"], route["occupiedSeatsPerSample"]) == (
            cars, samples, seats)
        assert route["wearablePartCount"] == 11
        assert route["wearableTrackChecks"] == samples * seats * 11
        assert route["wearableRackChecks"] >= route["wearableTrackChecks"]
        assert route["rackTrackChecks"] > 0
        assert route["passes"] and not route["uncertifiedPairs"]
        assert route["wearableTrackMinimumSeparatingBound"] >= .02
        assert route["wearableRackMinimumSeparatingBound"] >= .02
        assert route["rackTrackMinimumSeparatingBound"] >= .02
        assert all(value >= .02 for value in
                   route["partTrackMinimumSeparatingBounds"].values())
        assert all(value >= .02 for value in
                   route["partRackMinimumSeparatingBounds"].values())
    for name, expected_hash in report["resourceHashes"].items():
        current = REPO / "src/main/resources" / name
        if current.exists():
            assert sha(current) == expected_hash, name

print("FD402 sampled vanilla wearable clearance: verified")
