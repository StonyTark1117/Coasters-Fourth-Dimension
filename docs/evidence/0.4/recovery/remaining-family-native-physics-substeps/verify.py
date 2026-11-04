#!/usr/bin/env python3
"""Verify the FD402 Legacy, Free Spin, and Revised physics-substep checkpoint."""

import gzip
import hashlib
import json
import math
from pathlib import Path

here = Path(__file__).resolve().parent
repo = here.parents[4]


def sha(path):
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def uncompressed_sha(path):
    digest = hashlib.sha256()
    with gzip.open(path, "rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


mesh = json.loads((here / "mesh-report.json").read_text())
tick_trace = here / "recovery-bank-family-substep-mesh-trace.jsonl.gz"
frames = here / "recovery-bank-family-substep-native-frames.json"

assert sha(repo / "src/main/java/dev/fourthdimension/gametest/RecoveryFamilySubstepGameTests.java") == (
    "1787d6fd570d409b1a7e1abc453ee13bb7b15748a4c9048d19fa89e11c7e762e"
)
assert sha(repo / "src/main/java/dev/fourthdimension/gametest/RecoverySubstepTrace.java") == (
    "4ecafb76570698431823479dcde77b35f5accd409f6c1058e4643a1f7b0c0370"
)
assert sha(here / "audit-recovery-mesh-trace.py.txt") == mesh["verifierSha256"]
assert sha(here / "audit-recovery-mesh-trace.py.txt") == sha(
    repo / "scripts/audit-recovery-mesh-trace.py"
)
assert sha(here / "audit-recovery-native-substeps.py.txt") == sha(
    repo / "scripts/audit-recovery-native-substeps.py"
)
for source, digest in mesh["sourceHashes"].items():
    assert sha(repo / source) == digest, source

assert sha(frames) == mesh["framesSha256"]
assert uncompressed_sha(tick_trace) == mesh["traceSha256"]

expected = {
    "LEGACY": {
        "slug": "legacy",
        "tickFamily": "ZACSPIN",
        "variant": "zacspin_legacy_v1",
        "samples": 5842,
        "traceSteps": 11964,
        "auditedSteps": 11683,
        "bodyCount": 4,
        "bodyBound": 0.031227598868396614,
        "playerBound": 0.06723691791527375,
        "neighborBound": 1.7006470766927846,
    },
    "FREE_SPIN": {
        "slug": "free_spin",
        "tickFamily": "FREE_SPIN",
        "variant": "sns_magnetic_v1",
        "samples": 5872,
        "traceSteps": 12024,
        "auditedSteps": 11743,
        "bodyCount": 6,
        "bodyBound": 0.0308460128797127,
        "playerBound": 0.06691885545005614,
        "neighborBound": 4.995513109051926,
    },
    "REVISED": {
        "slug": "revised",
        "tickFamily": "ZACSPIN_REVISED",
        "variant": "zacspin_revised_v1",
        "samples": 5869,
        "traceSteps": 12018,
        "auditedSteps": 11737,
        "bodyCount": 6,
        "bodyBound": 0.030846770402981227,
        "playerBound": 0.06685393972309826,
        "neighborBound": 6.8763318536001465,
    },
}

routes = {route["family"]: route for route in mesh["routes"]}
assert set(routes) == set(expected)

for family, values in expected.items():
    route = routes[family]
    report_path = here / f"{values['slug']}-substep-report.json"
    report = json.loads(report_path.read_text())
    substeps = here / f"recovery-bank-family-{values['slug']}-substeps.jsonl.gz"

    assert route["passes"] and not route["uncertifiedPairs"]
    assert route["cars"] == 1 and route["samples"] == values["samples"]
    assert route["occupiedSeatsPerSample"] == 8
    assert route["allCarsVisitedSections"] == 5

    assert report["passes"]
    assert report["family"] == family
    assert report["tickFamily"] == values["tickFamily"]
    assert report["variant"] == values["variant"]
    assert report["cars"] == 1
    assert report["traceSteps"] == values["traceSteps"]
    assert report["auditedSteps"] == values["auditedSteps"]
    assert report["auditedSteps"] == 2 * report["matchedTickSamples"] - 1
    assert report["matchedTickSamples"] == route["samples"]
    assert report["substepsPerTick"] == 2
    assert report["requiredGap"] == 0.02
    assert report["inputHashes"]["base_jar"] == mesh["baseSha256"]
    assert report["inputHashes"]["minecraft_jar"] == mesh["minecraftSha256"]
    assert report["inputHashes"]["mesh_report"] == sha(here / "mesh-report.json")
    assert report["inputHashes"]["tick_trace"] == sha(tick_trace)
    assert report["inputHashes"]["substep_trace"] == sha(substeps)
    assert all(
        bound >= report["requiredGap"]
        for bound in report["substepSeparatingLowerBounds"].values()
    )
    assert all(
        bound >= report["requiredGap"]
        for bound in report["carrierLocalInvariantBounds"].values()
    )
    assert report["neighborSeparatingLowerBound"] >= report["requiredGap"]
    assert math.isclose(report["substepSeparatingLowerBounds"]["body"], values["bodyBound"])
    assert math.isclose(report["substepSeparatingLowerBounds"]["player"], values["playerBound"])
    assert math.isclose(report["neighborSeparatingLowerBound"], values["neighborBound"])

    with gzip.open(substeps, "rt") as stream:
        header = json.loads(next(stream))
        last = header
        for line in stream:
            last = json.loads(line)
    assert header["type"] == "trace"
    assert header["family"] == family and header["variant"] == values["variant"]
    assert header["cars"] == 1 and len(header["bodyIds"]) == values["bodyCount"]
    assert last == {"type": "complete", "steps": values["traceSteps"]}

with gzip.open(here / "server-console.log.gz", "rt") as stream:
    log = stream.read()
family_lines = [
    line for line in log.splitlines() if line.startswith("FD_RECOVERY_FAMILY_SUBSTEP family=")
]
assert len(family_lines) == 3
for family in expected:
    matches = [line for line in family_lines if f"family={family} sections=5" in line]
    assert len(matches) == 1
    assert "riders=8" in matches[0]
    assert "profilesUnchanged=true normalBrakePowerCutDetachUnload=true" in matches[0]
assert "All 4 required tests passed" in log
assert "BUILD SUCCESSFUL in 2m 33s" in log

print("FD402 remaining-family native physics-substep clearance: verified")
