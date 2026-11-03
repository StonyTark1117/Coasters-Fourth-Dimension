#!/usr/bin/env python3
"""Check the bounded Legacy powered client record without launching Minecraft."""

import hashlib
import json
from pathlib import Path
import re
import struct
import subprocess

ROOT = Path(__file__).resolve().parent
IDS = (
    "6bae19a6-1412-45a2-9573-8ccd01433ad0",
    "df127c8b-65f5-4b15-a987-35556cc3733a",
)


def log(name):
    return subprocess.check_output(["zstd", "-dc", str(ROOT / (name + ".log.zst"))], text=True)


def rail(data):
    matches = [r for r in data["rails"] if r["family"] == "zacspin_legacy"
               and r["original_curve"]["Positions"][-1]["Pos"] == [0, 0, 50]]
    assert len(matches) == 1
    return matches[0]


def car(data):
    matches = [v for v in data["vehicles"] if v["family"] == "zacspin_legacy"
               and v["fault"] == ""]
    assert len(matches) == 1
    return matches[0]


for line in (ROOT / "SHA256SUMS").read_text().splitlines():
    expected, name = line.split("  ", 1)
    assert hashlib.sha256((ROOT / name).read_bytes()).hexdigest() == expected, name

run, reopen = log("legacy-powered-client"), log("legacy-powered-reopen")
for sample in ("25.180105686716d", "44.54058148762567d", "61.05387564359598d",
               "260, -60, 20 has the following block data: 50.0d",
               "260, -60, 20 has the following block data: 16.0f",
               "Found no elements matching RootVehicle", "Saving worlds", "BUILD SUCCESSFUL"):
    assert sample in run, sample
for sample in ("260, -60, 20 has the following block data: 50.0d",
               "260, -60, 20 has the following block data: 16.0f",
               "Found no elements matching RootVehicle", "Saving worlds", "BUILD SUCCESSFUL"):
    assert sample in reopen, sample
for ident in IDS:
    assert re.search(rf"zacspin_legacy {ident} .*capture=true occupants=0", run)
    assert re.search(rf"zacspin_legacy {ident} .*capture=true occupants=0", reopen)
assert re.search(rf"zacspin_legacy {IDS[0]} .*capture=true occupants=1", run)
assert "OutOfMemoryError" not in run + reopen

before = json.loads((ROOT / "foundation-pre.json").read_text())["data"]
after = json.loads((ROOT / "foundation-final.json").read_text())["data"]
for data in (before, after):
    r, v = rail(data), car(data)
    assert r["valid"] == 1 and r["provider"] == "fourthdimension:original"
    assert r["variant"] == "zacspin_legacy_v1"
    assert v["boarding_ready"] == 1 and v["stopping"] == 1 and v["restraints"] == 0
    assert len(v["carriers"]) == 2
    assert all(c["capture"] == 1 and c["rider_seats"] == [] and c["occupants"] == []
               for c in v["carriers"])
assert rail(before)["id"] == rail(after)["id"]
assert car(before)["chassis"] == car(after)["chassis"]
assert [c["body"] for c in car(before)["carriers"]] == [
    c["body"] for c in car(after)["carriers"]]

for path in ROOT.glob("*.png"):
    raw = path.read_bytes()
    assert raw[:8] == b"\x89PNG\r\n\x1a\n"
    assert struct.unpack(">II", raw[16:24]) == (854, 480), path.name
assert (ROOT / "world.tar.zst").stat().st_size > 1_000_000
print("Legacy bounded powered-pass record: verified")
