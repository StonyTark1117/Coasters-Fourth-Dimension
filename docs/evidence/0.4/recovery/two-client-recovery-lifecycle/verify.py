#!/usr/bin/env python3
"""Verify the bounded two-client recovery evidence without launching Minecraft."""

import gzip
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parent


def compressed_text(name: str) -> str:
    with gzip.open(ROOT / name, "rt", encoding="utf-8") as source:
        return source.read()


server = compressed_text("server.log.gz")
for marker in (
    "admitted rider=Observer slot=1/2",
    "admitted rider=Dev slot=2/2",
    "boarded rider=Observer family=ARROW",
    "boarded rider=Dev family=ARROW",
    "attached occupied recovery",
    "riders=2",
    "started occupied recovery",
    "braked occupied recovery",
    "detached=true restraintsOpen=true ridersUnloaded=2 riders=[Observer, Dev]",
    "Stopping server",
    "All dimensions are saved",
):
    assert marker in server, marker

for name in ("dev.log.gz", "observer.log.gz"):
    client = compressed_text(name)
    for marker in (
        "FD_CARRIER_VALIDATION passed",
        "riderAssignments=2",
        "riderMounted=false",
        "localRiderRenderCalls=true",
        "recoveryLinkSeen=true",
        "recoveryMotionSeen=true",
        "recoveryCycleSeen=true",
        "motionFamilies=[ARROW]",
    ):
        assert marker in client, f"{name}: {marker}"
    assert "OutOfMemoryError" not in client, name

saved = json.loads((ROOT / "final-save.json").read_text(encoding="utf-8"))["data"]
assert saved["schema"] == 6
assert len(saved["vehicles"]) == 1
vehicle = saved["vehicles"][0]
assert vehicle["family"] == "arrow_controlled"
assert vehicle["restraints"] == 0
assert vehicle["maintenance"] == 1
assert "recovery_cart" not in vehicle
assert all(not carrier["rider_seats"] for carrier in vehicle["carriers"])
assert all(not carrier["occupants"] for carrier in vehicle["carriers"])

memory = (ROOT / "memory.txt").read_text(encoding="utf-8")
for marker in ("1.8G memory peak", "2.4G memory peak", "2.5G memory peak"):
    assert marker in memory, marker

limits = (ROOT / "limits.txt").read_text(encoding="utf-8")
assert "MemoryMax=2684354560" in limits
assert limits.count("MemoryMax=3221225472") == 2

print("PASS: two-client occupied Arrow recovery lifecycle evidence")
