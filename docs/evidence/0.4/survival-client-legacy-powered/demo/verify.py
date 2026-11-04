#!/usr/bin/env python3
"""Verify the Legacy powered-demo connected-client record without Minecraft."""

import hashlib
import json
from pathlib import Path
import re
import struct
import subprocess
import uuid

ROOT = Path(__file__).resolve().parent
CARRIERS = (
    "5e00f025-b6b8-44c5-a113-d0455715d59b",
    "cba2a2d7-a44b-45a1-bc6e-ebbfd7da8f31",
)
CHASSIS = "b859c201-4f92-4aca-a4d7-963cd57ce27d"


def identifier(ints):
    return str(uuid.UUID(bytes=struct.pack(">4i", *ints)))


def log(name):
    return subprocess.check_output(["zstd", "-dc", str(ROOT / name)], text=True)


for line in (ROOT / "SHA256SUMS").read_text().splitlines():
    expected, name = line.split("  ", 1)
    assert hashlib.sha256((ROOT / name).read_bytes()).hexdigest() == expected, name

run = log("client.log.zst")
reopen = log("reopen.log.zst")
assert "Example circuit built" in run
for position in (
    "[497.50000731242176d, -55.56257919800738d, 331.4147944369386d]",
    "[529.8752976400152d, -51.17518304344385d, 366.0115291242555d]",
    "[522.8033315014488d, -55.51545161526306d, 309.4999569327521d]",
    "[497.4999752685352d, -55.55274547297d, 344.28785474784706d]",
):
    assert position in run, position
assert re.search(rf"zacspin_legacy {CARRIERS[0]} angle=0.56471.*capture=false occupants=0", run)
assert re.search(rf"zacspin_legacy {CARRIERS[1]} angle=0.64383.*capture=false occupants=1", run)
assert re.search(rf"zacspin_legacy {CARRIERS[0]} angle=-0.10456.*omega=0.48951.*capture=false", run)
assert "Found no elements matching RootVehicle" in run + reopen
for ident in CARRIERS:
    assert re.search(rf"zacspin_legacy {ident} .*capture=true occupants=0", run), ident
    assert re.search(rf"zacspin_legacy {ident} .*capture=true occupants=0", reopen), ident
for sample in ("Saving worlds", "All dimensions are saved", "BUILD SUCCESSFUL"):
    assert sample in run and sample in reopen, sample
assert "Invalid rider seat assignment" not in run + reopen

data = json.loads((ROOT / "foundation-final.json").read_text())["data"]
assert data["schema"] == 6
rails = [r for r in data["rails"] if r["family"] == "zacspin_legacy"]
assert len(rails) == 10  # two older survival sections plus eight demo sections
assert all(r["valid"] == 1 and r["provider"] == "fourthdimension:original" for r in rails)
demo_rails = [r for r in rails if r["from"] >= 137438954786759]
assert len(demo_rails) == 8
assert all(sum(s["to"] == r["from"] for s in demo_rails) == 1 for r in demo_rails)
assert all(sum(s["from"] == r["to"] for s in demo_rails) == 1 for r in demo_rails)
car = next(v for v in data["vehicles"] if identifier(v["chassis"]) == CHASSIS)
assert car["family"] == "zacspin_legacy" and car["fault"] == ""
assert car["boarding_ready"] == 1 and car["stopping"] == 1 and car["restraints"] == 0
assert tuple(identifier(c["body"]) for c in car["carriers"]) == CARRIERS
assert all(c["capture"] == 1 and c["occupants"] == [] and c["rider_seats"] == []
           for c in car["carriers"])
assert any(r["from"] == car["boundFrom"] and r["to"] == car["boundTo"] for r in demo_rails)

world_data = subprocess.check_output([
    "tar", "-I", "zstd", "-xOf", str(ROOT / "world.tar.zst"),
    "FD403LegacyDemoPowered/data/fourthdimension_foundation.dat",
])
assert world_data == (ROOT / "foundation-final.dat").read_bytes()
for path in ROOT.glob("*.png"):
    raw = path.read_bytes()
    assert raw[:8] == b"\x89PNG\r\n\x1a\n"
    assert struct.unpack(">II", raw[16:24]) == (854, 480), path.name
print("Legacy powered-demo free-rotation record: verified")
