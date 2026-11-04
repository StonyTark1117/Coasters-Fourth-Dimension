#!/usr/bin/env python3
"""Verify the FD402 native recorded-physics-substep checkpoint."""

import hashlib
import json
import math
from pathlib import Path


here = Path(__file__).resolve().parent
repo = here.parents[4]
inputs = repo / "docs/evidence/0.9/fd401-styles-physics-substeps-2c5d9a7"


def sha(path):
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


for line in (here / "SHA256SUMS").read_text().splitlines():
    expected, name = line.split("  ", 1)
    assert sha(here / name) == expected, name

mesh = json.loads((here / "mesh-report.json").read_text())
report = json.loads((here / "report.json").read_text())
assert report["passes"]
assert report["inputHashes"]["mesh_report"] == sha(here / "mesh-report.json")
assert mesh["verifierSha256"] == sha(here / "audit-recovery-mesh-trace.py.txt")
assert sha(here / "audit-recovery-native-substeps.py.txt") == \
       "dc6085baca652c372ae6d4511d48b0f4d35c0ae3ccdae9eacde88fcc68fc22cb"

frames = inputs / "recovery-bank-train-native-frames.json"
ticks = inputs / "recovery-bank-train-mesh-trace.jsonl.gz"
substeps = inputs / "recovery-bank-train-arrow-substeps.jsonl.gz"
assert mesh["framesSha256"] == sha(frames)
assert mesh["traceSha256"] == sha(ticks) == report["inputHashes"]["tick_trace"]
assert sha(substeps) == report["inputHashes"]["substep_trace"]
assert mesh["baseSha256"] == report["inputHashes"]["base_jar"]
assert mesh["minecraftSha256"] == report["inputHashes"]["minecraft_jar"]

for name, expected in mesh["sourceHashes"].items():
    assert sha(repo / name) == expected, name
for name, expected in mesh["resourceHashes"].items():
    local = repo / "src/main/resources" / name
    if local.exists():
        assert sha(local) == expected, name

routes = {(route["family"], route["cars"]): route for route in mesh["routes"]}
assert set(routes) == {("ARROW", 2), ("PAX", 4)}
arrow = routes[("ARROW", 2)]
assert arrow["samples"] == report["matchedTickSamples"] == 6548
assert report["traceSteps"] == 13414 and report["auditedSteps"] == 13095
assert report["auditedSteps"] == 2 * report["matchedTickSamples"] - 1
assert report["substepsPerTick"] == 2
for route in routes.values():
    assert route["passes"] and not route["uncertifiedPairs"]
    for key in ("minimumSeparatingBound", "wheelMinimumSeparatingBound",
                "riderMinimumSeparatingBound", "playerMeshMinimumSeparatingBound",
                "riderRackMinimumSeparatingBound", "playerRackMinimumSeparatingBound",
                "neighborMinimumSeparatingBound"):
        assert route[key] >= report["requiredGap"], (route["family"], key)

sample_keys = {
    "body": "minimumSeparatingBound",
    "wheel": "wheelMinimumSeparatingBound",
    "rider": "riderMinimumSeparatingBound",
    "player": "playerMeshMinimumSeparatingBound",
}
for kind, mesh_key in sample_keys.items():
    assert math.isclose(report["sampledSeparatingBounds"][kind], arrow[mesh_key])
    expected = arrow[mesh_key] - report["maximumMotionFromNearestTick"][kind]
    assert math.isclose(report["substepSeparatingLowerBounds"][kind], expected)
    assert expected >= report["requiredGap"], kind

maximum_motion = max(report["maximumMotionFromNearestTick"].values())
neighbor = arrow["neighborMinimumSeparatingBound"] - 2 * maximum_motion
assert math.isclose(report["neighborSeparatingLowerBound"], neighbor)
assert neighbor >= report["requiredGap"]
assert math.isclose(report["carrierLocalInvariantBounds"]["riderRack"],
                    arrow["riderRackMinimumSeparatingBound"])
assert math.isclose(report["carrierLocalInvariantBounds"]["playerRack"],
                    arrow["playerRackMinimumSeparatingBound"])

print("FD402 native recorded physics-substep clearance: verified")
