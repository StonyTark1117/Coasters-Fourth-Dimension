#!/usr/bin/env python3
"""Check the bounded Arrow client record without launching Minecraft."""

import hashlib
import json
import math
from pathlib import Path
import re
import struct
import subprocess

ROOT = Path(__file__).resolve().parent
IDS = (
    "f4c76d78-95bf-4d8a-a981-6b3437319c3c",
    "63711d4a-c906-495c-ba5c-9fe7edabf871",
)


def log(name):
    return subprocess.check_output(["zstd", "-dc", str(ROOT / (name + ".log.zst"))], text=True)


def vehicle(data):
    candidates = [v for v in data["vehicles"] if v["family"] == "arrow_controlled"
                  and all(abs(c["angle"] - math.tau) < 0.01 for c in v["carriers"])]
    assert len(candidates) == 1, "expected one new full-turn Arrow car"
    return candidates[0]


def rail(data):
    candidates = [r for r in data["rails"] if r["family"] == "arrow_controlled"
                  and len(r["profile"]) == 3 and r["profile"][-1]["distance"] == 50]
    assert len(candidates) == 1, "expected one new 50-block Arrow rail"
    return candidates[0]


for line in (ROOT / "SHA256SUMS").read_text().splitlines():
    expected, name = line.split("  ", 1)
    assert hashlib.sha256((ROOT / name).read_bytes()).hexdigest() == expected, name

run = log("arrow-powered-retry2")
reopen = log("arrow-powered-retry2-reopen")
for s in ("25.391801760221234d", "33.63263015412359d", "54.8386", "55.5092748",
          "230, -60, 20 has the following block data: 16.0f", "id: \"create:seat\"",
          "Found no elements matching RootVehicle", "Saving worlds", "BUILD SUCCESSFUL"):
    assert s in run, f"run: {s}"
for ident in IDS:
    assert re.search(rf"arrow_controlled {ident} angle=6\.\d+ .*capture=false occupants=[01]", run)
    assert re.search(rf"arrow_controlled {ident} angle=6\.\d+ .*capture=true occupants=0", run)
    assert re.search(rf"arrow_controlled {ident} angle=6\.\d+ .*capture=true occupants=0", reopen)
for s in ("id: \"create:seat\"", "Found no elements matching RootVehicle",
          "Saving worlds", "BUILD SUCCESSFUL"):
    assert s in reopen, f"reopen: {s}"
assert "OutOfMemoryError" not in run + reopen

before = json.loads((ROOT / "foundation-pre.json").read_text())["data"]
after = json.loads((ROOT / "foundation-final.json").read_text())["data"]
bad = json.loads((ROOT / "short-route-foundation.json").read_text())["data"]
assert any(v["family"] == "arrow_controlled" and "End of compatible track" in v["fault"]
           for v in bad["vehicles"])
for data in (before, after):
    r, v = rail(data), vehicle(data)
    assert r["valid"] == 1 and r["provider"] == "fourthdimension:original"
    assert r["original_curve"]["Positions"][-1]["Pos"] == [0, 0, 50]
    assert [p["distance"] for p in r["profile"]] == [0, 15, 50]
    assert [p["angle"] for p in r["profile"]] == [0, math.tau, math.tau]
    assert v["fault"] == "" and v["boarding_ready"] == 1
    assert v["stopping"] == 1 and v["restraints"] == 0
    assert len(v["carriers"]) == 2
    assert all(c["capture"] == 1 and c["rider_seats"] == []
               and c["occupants"] == [] for c in v["carriers"])
assert rail(before)["id"] == rail(after)["id"]
assert vehicle(before)["chassis"] == vehicle(after)["chassis"]
assert [c["body"] for c in vehicle(before)["carriers"]] == [
    c["body"] for c in vehicle(after)["carriers"]]

for path in ROOT.glob("*.png"):
    image = path.read_bytes()
    assert image[:8] == b"\x89PNG\r\n\x1a\n"
    assert struct.unpack(">II", image[16:24]) == (854, 480), path.name
assert (ROOT / "world.tar.zst").stat().st_size > 1_000_000
print("Arrow bounded powered-pass record: verified")
