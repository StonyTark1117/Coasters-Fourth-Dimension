#!/usr/bin/env python3
"""Verify the bounded connected-client Free Spin record without Minecraft."""

import hashlib
import json
from pathlib import Path
import re
import struct
import subprocess
import uuid

ROOT = Path(__file__).resolve().parent
NEW_CHASSIS = (1377766060, -608415901, -1167706261, 994672529)
OLD_FAULTED_CHASSIS = (-1778246182, -1127530254, -2101248854, -792536721)
NEW_CARRIERS = (
    "1bfaca5d-8118-4732-a9a5-1aff332ab2a6",
    "b6ea842b-cfd5-45a4-9092-ef8e07b08b09",
    "6a706dfd-7ca1-4297-b567-da3405e38a15",
    "afaf90c0-4c5c-400e-8dd4-c80b84bb0a56",
)


def log(name):
    return subprocess.check_output(["zstd", "-dc", str(ROOT / name)], text=True)


def identifier(ints):
    return str(uuid.UUID(bytes=struct.pack(">4i", *ints)))


for line in (ROOT / "SHA256SUMS").read_text().splitlines():
    expected, name = line.split("  ", 1)
    assert hashlib.sha256((ROOT / name).read_bytes()).hexdigest() == expected, name

initial = log("free-spin-initial-negative.log.zst")
reach = log("free-spin-reach-negative.log.zst")
duplicate = log("free-spin-duplicate-negative.log.zst")
run = log("free-spin-powered-pass.log.zst")
reopen = log("free-spin-powered-reopen.log.zst")
first_test = log("rider-assignment-first-gametest.log.zst")
partial_test = log("rider-assignment-partial-gametest.log.zst")
assert "Stand within eight blocks of the selected section" in initial
assert "sns_free_spin b60ecb30-cfd7-47b9-bb88-009014872d5e angle=-6.279" in reach
assert "Invalid rider seat assignment" in duplicate
assert "revisedpivoteightridersindependentcircuitandsave failed" in first_test
assert "[+++++++++++++++++++++++++++++++++++++++           ]" in partial_test
assert "failed at" not in partial_test
for sample in (
    "42.46455039760065d", "35.88586000964121d", "35.36741663879188d",
    "30.135348146353554d", "angle=-7.0849509844529726 omega=3.032558036172587",
    "angle=-6.3012181225787165 omega=0.0027937338808776174",
    "Found no elements matching RootVehicle", "Saving worlds", "BUILD SUCCESSFUL",
):
    assert sample in run, sample
for ident in NEW_CARRIERS:
    assert re.search(rf"sns_free_spin {ident} .*capture=true occupants=0", run), ident
    assert re.search(rf"sns_free_spin {ident} .*capture=true occupants=0", reopen), ident
assert "Found no elements matching RootVehicle" in reopen
assert "Saving worlds" in reopen and "BUILD SUCCESSFUL" in reopen
assert "Invalid rider seat assignment" not in run + reopen

data = json.loads((ROOT / "foundation-final.json").read_text())["data"]
rails = [r for r in data["rails"] if r["family"] == "sns_free_spin" and len(r["magnets"]) == 2]
assert len(rails) == 1 and rails[0]["valid"] == 1
assert rails[0]["provider"] == "fourthdimension:original"
spans = {(m["mode"], m["start"], m["end"], m["targets"], m["strength"],
          m["falloff"], m["direction"]) for m in rails[0]["magnets"]}
assert spans == {("INDUCE", 28, 32, 1, 20, .2, 1), ("DAMP", 13, 17, 1, 20, .2, 1)}
cars = {tuple(v["chassis"]): v for v in data["vehicles"] if v["family"] == "sns_free_spin"}
assert cars[OLD_FAULTED_CHASSIS]["fault"] == "End of compatible track; capture and inspect the route"
car = cars[NEW_CHASSIS]
assert identifier(car["chassis"]) == "521f0aac-dbbc-4f63-ba66-376b3b497f91"
assert car["fault"] == "" and car["boarding_ready"] == 1
assert car["stopping"] == 1 and car["restraints"] == 0
assert tuple(identifier(c["body"]) for c in car["carriers"]) == NEW_CARRIERS
assert all(c["capture"] == 1 and c["occupants"] == [] and c["rider_seats"] == []
           for c in car["carriers"])

world_data = subprocess.check_output([
    "tar", "-I", "zstd", "-xOf", str(ROOT / "world.tar.zst"),
    "FD403FreeSpinPoweredRetry3/data/fourthdimension_foundation.dat",
])
assert world_data == (ROOT / "foundation-final.dat").read_bytes()
for path in ROOT.glob("*.png"):
    raw = path.read_bytes()
    assert raw[:8] == b"\x89PNG\r\n\x1a\n"
    assert struct.unpack(">II", raw[16:24]) == (854, 480), path.name
assert (ROOT / "world.tar.zst").stat().st_size > 1_000_000
print("Free Spin bounded powered magnetic record: verified")
