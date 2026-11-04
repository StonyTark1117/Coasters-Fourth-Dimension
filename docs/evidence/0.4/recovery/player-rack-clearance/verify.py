#!/usr/bin/env python3
"""Verify the FD402 sampled vanilla-player carrier-rack checkpoint."""

import hashlib
import json
from pathlib import Path
import subprocess

here = Path(__file__).resolve().parent
repo = here.parents[4]
inputs = here.parent / "current-tick-rider-clearance"


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


for line in (here / "SHA256SUMS").read_text().splitlines():
    expected, name = line.split("  ", 1)
    assert sha(here / name) == expected, name

auditor_hash = sha(here / "audit-recovery-mesh-trace.py.txt")
expected = {
    "single": ({"ARROW": 1, "LEGACY": 1, "FREE_SPIN": 1, "REVISED": 1, "PAX": 1}, 5700),
    "train": ({"ARROW": 2, "PAX": 4}, 6500),
}
for stem, (families, minimum_samples) in expected.items():
    report = json.loads((here / f"{stem}-results.json").read_text())
    assert report["verifierSha256"] == auditor_hash
    assert report["framesSha256"] == sha(inputs / f"{stem}-frames.json")
    raw = subprocess.check_output(["zstd", "-dc", str(inputs / f"{stem}-trace.jsonl.zst")])
    assert report["traceSha256"] == hashlib.sha256(raw).hexdigest()
    assert {route["family"]: route["cars"] for route in report["routes"]} == families
    for name, expected_hash in report["sourceHashes"].items():
        assert sha(repo / name) == expected_hash, name
    for route in report["routes"]:
        assert route["samples"] >= minimum_samples, route["family"]
        assert route["passes"] and not route["uncertifiedPairs"], route["family"]
        assert route["playerMeshEnvelopeCount"] == route["riderEnvelopeCount"]
        assert route["playerRackEnvelopeCount"] == 6 * route["riderRackEnvelopeCount"]
        assert route["playerMeshMinimumSeparatingBound"] >= .02
        assert route["playerRackMinimumSeparatingBound"] >= .02
        assert route["riderRackMinimumSeparatingBound"] >= .02
        assert route["minimumSeparatingBound"] >= .02
        substitutions = route["prospectiveLegacyAxleSubstitutions"]
        assert substitutions == (4 if stem == "single" and route["family"] == "LEGACY" else 0)
    for name, expected_hash in report["resourceHashes"].items():
        path = repo / "src/main/resources" / name
        if path.exists():
            assert sha(path) == expected_hash, name

print("FD402 sampled vanilla-player carrier-rack clearance: verified")
