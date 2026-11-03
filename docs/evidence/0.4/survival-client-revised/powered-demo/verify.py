#!/usr/bin/env python3
"""Verify the bounded Revised powered-demo client record without Minecraft."""

import hashlib
import json
from pathlib import Path
import re
import struct
import subprocess
import uuid

ROOT = Path(__file__).resolve().parent
CARRIERS = (
    "162d8a64-d4ae-4603-8187-79d0508b07d7",
    "81b2b9c0-f874-4ce0-836b-c318c7e58b64",
    "57becd27-294a-4b3c-a320-aa35c89fd10b",
    "c090e360-f8ee-4848-9bec-2c3e9ec48a39",
)
CHASSIS = "a649ce0b-aca6-4297-b7de-da70f0bfd4de"


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
    "[404.50002278552165d, -55.56261778039146d, 329.49968907482645d]",
    "[405.33670902545464d, -49.750958865757845d, 360.38791249141724d]",
    "[428.4999770267186d, -55.5626762406869d, 325.83804741523727d]",
    "[404.50002307621554d, -55.562684066068336d, 342.12264162889414d]",
):
    assert position in run, position
assert re.search(rf"zacspin_revised {CARRIERS[1]} angle=0.03319.*capture=false occupants=1", run)
assert "Found no elements matching RootVehicle" in run
assert "Found no elements matching RootVehicle" in reopen
for ident in CARRIERS:
    assert re.search(rf"zacspin_revised {ident} .*capture=true occupants=0", run), ident
    assert re.search(rf"zacspin_revised {ident} .*capture=true occupants=0", reopen), ident
for sample in ("Saving worlds", "All dimensions are saved", "BUILD SUCCESSFUL"):
    assert sample in run and sample in reopen, sample
for bad in ("Invalid rider seat assignment", "End of compatible track; capture and inspect the route"):
    assert bad not in run + reopen, bad

data = json.loads((ROOT / "foundation-final.json").read_text())["data"]
assert data["schema"] == 6
rails = [r for r in data["rails"] if r["family"] == "zacspin_revised"]
assert len(rails) == 9  # one existing survival section, eight demo sections
assert all(r["valid"] == 1 and r["provider"] == "fourthdimension:original" for r in rails)
car = next(v for v in data["vehicles"] if identifier(v["chassis"]) == CHASSIS)
assert car["family"] == "zacspin_revised" and car["fault"] == ""
assert car["boarding_ready"] == 1 and car["stopping"] == 1 and car["restraints"] == 0
assert tuple(identifier(c["body"]) for c in car["carriers"]) == CARRIERS
assert all(c["pivot_height"] == 1.5 and c["child_pivot_offset"] == -0.5 for c in car["carriers"])
assert all(c["capture"] == 1 and c["occupants"] == [] and c["rider_seats"] == []
           for c in car["carriers"])
demo_rails = [r for r in rails if r["from"] != 71468256833493]
assert len(demo_rails) == 8
assert all(sum(s["to"] == r["from"] for s in demo_rails) == 1 for r in demo_rails)
assert all(sum(s["from"] == r["to"] for s in demo_rails) == 1 for r in demo_rails)
assert any(r["from"] == car["boundFrom"] and r["to"] == car["boundTo"] for r in demo_rails)

world_data = subprocess.check_output([
    "tar", "-I", "zstd", "-xOf", str(ROOT / "world.tar.zst"),
    "FD403RevisedDemoPowered/data/fourthdimension_foundation.dat",
])
assert world_data == (ROOT / "foundation-final.dat").read_bytes()
for path in ROOT.glob("*.png"):
    raw = path.read_bytes()
    assert raw[:8] == b"\x89PNG\r\n\x1a\n"
    assert struct.unpack(">II", raw[16:24]) == (854, 480), path.name
print("Revised powered-demo connected-client record: verified")
