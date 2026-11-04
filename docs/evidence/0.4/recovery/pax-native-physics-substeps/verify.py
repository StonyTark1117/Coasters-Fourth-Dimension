#!/usr/bin/env python3
"""Verify the FD402 occupied PAX recorded-physics-substep checkpoint."""

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
report = json.loads((here / "pax-substep-report.json").read_text())
tick_trace = here / "recovery-bank-train-mesh-trace.jsonl.gz"
substeps = here / "recovery-bank-train-pax-substeps.jsonl.gz"
frames = here / "recovery-bank-train-native-frames.json"

assert sha(here / "audit-recovery-mesh-trace.py.txt") == mesh["verifierSha256"]
assert sha(here / "audit-recovery-native-substeps.py.txt") == sha(
    repo / "scripts/audit-recovery-native-substeps.py"
)
assert sha(here / "audit-recovery-mesh-trace.py.txt") == sha(
    repo / "scripts/audit-recovery-mesh-trace.py"
)
for source, digest in mesh["sourceHashes"].items():
    assert sha(repo / source) == digest, source

assert sha(frames) == mesh["framesSha256"]
assert uncompressed_sha(tick_trace) == mesh["traceSha256"]
assert report["inputHashes"]["base_jar"] == mesh["baseSha256"]
assert report["inputHashes"]["minecraft_jar"] == mesh["minecraftSha256"]
assert report["inputHashes"]["mesh_report"] == sha(here / "mesh-report.json")
assert report["inputHashes"]["tick_trace"] == sha(tick_trace)
assert report["inputHashes"]["substep_trace"] == sha(substeps)

routes = [route for route in mesh["routes"] if route["family"] == "PAX"]
assert len(routes) == 1
route = routes[0]
assert route["passes"] and not route["uncertifiedPairs"]
assert route["cars"] == 4 and route["samples"] == 6951

assert report["passes"]
assert report["family"] == "PAX"
assert report["variant"] == "pax_shuttle_v1"
assert report["cars"] == 4
assert report["traceSteps"] == 14222
assert report["auditedSteps"] == 13901 == 2 * report["matchedTickSamples"] - 1
assert report["matchedTickSamples"] == route["samples"]
assert report["substepsPerTick"] == 2
assert report["requiredGap"] == 0.02
assert all(value >= report["requiredGap"] for value in report["substepSeparatingLowerBounds"].values())
assert all(value >= report["requiredGap"] for value in report["carrierLocalInvariantBounds"].values())
assert report["neighborSeparatingLowerBound"] >= report["requiredGap"]
assert math.isclose(report["substepSeparatingLowerBounds"]["body"], 0.03152361026414478)
assert math.isclose(report["substepSeparatingLowerBounds"]["player"], 0.06752334466990671)
assert math.isclose(report["neighborSeparatingLowerBound"], 1.2512465467693223)

with gzip.open(substeps, "rt") as stream:
    header = json.loads(next(stream))
    last = header
    for line in stream:
        last = json.loads(line)
assert header["type"] == "trace"
assert header["family"] == "PAX" and header["variant"] == "pax_shuttle_v1"
assert header["cars"] == 4 and len(header["bodyIds"]) == 13
assert last == {"type": "complete", "steps": 14222}

with gzip.open(here / "server-console.log.gz", "rt") as stream:
    log = stream.read()
assert "FD_RECOVERY_BANK_TRAIN family=PAX cars=4 riders=16" in log
assert "originalLinksAfterDetach=true" in log
assert "All 3 required tests passed" in log
assert "BUILD SUCCESSFUL" in log

print("FD402 occupied PAX native physics-substep clearance: verified")
