#!/usr/bin/env python3
"""Verify the bounded Free Spin damper-demo connected-client record."""

import hashlib
import json
from pathlib import Path
import re
import struct
import subprocess
import uuid

ROOT = Path(__file__).resolve().parent
CHASSIS = "e98213db-3c7a-4883-9ce5-d89541b91db8"
CARRIERS = (
    "70e98fbe-8754-4884-a8f3-f832edeaec4c",
    "eb6fd2f2-ebe7-4c83-b414-f39e4dfe7695",
    "760ea120-ac47-43fe-b7c7-9ab930049db4",
    "b251fea3-605a-4ce5-b758-19564ee232fe",
)


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
    "[604.4999927619461d, -55.56262584134196d, 329.4993379131383d]",
    "[628.5000077540684d, -55.56258942619908d, 344.0385859085037d]",
    "[613.3484260997769d, -55.56262980296939d, 316.4999929078357d]",
):
    assert position in run, position


def carrier_sample(source, time, carrier):
    pattern = (rf"\[{time}\].*sns_free_spin {carrier} angle=([^\s]+) omega=([^\s]+) "
               rf"capture=(true|false) occupants=(\d+) damperImpulses=(\d+) "
               rf"damperEnergy=([^\s]+) damperSpeedReduction=([^\s]+)")
    match = re.search(pattern, source)
    assert match, (time, carrier)
    angle, omega, captured, occupants, impulses, energy, reduction = match.groups()
    return (float(angle), float(omega), captured == "true", int(occupants),
            int(impulses), float(energy), float(reduction))


for index, carrier in enumerate(CARRIERS):
    before = carrier_sample(run, "02:48:28", carrier)
    after = carrier_sample(run, "02:48:52", carrier)
    unloaded = carrier_sample(run, "02:49:38", carrier)
    cold = carrier_sample(reopen, "02:53:18", carrier)
    expected_occupants = int(index == 1)
    assert not before[2] and before[3] == expected_occupants
    assert before[4:] == (0, 0.0, 0.0)
    assert not after[2] and after[3] == expected_occupants
    assert after[4] >= 120 and after[5] < 0 and after[6] > 0
    assert unloaded[2] and unloaded[3] == 0
    assert cold[2] and cold[3] == 0 and cold[4:] == (0, 0.0, 0.0)

assert run.count("Received a sub-level movement packet for a non-existent sub-level") == 3
assert "Received a sub-level movement packet for a non-existent sub-level" not in reopen
assert "Found no elements matching RootVehicle" in run and "Found no elements matching RootVehicle" in reopen
assert "Invalid rider seat assignment" not in run + reopen
assert "Matching track support lost" not in run + reopen
assert "Saving worlds" in run and "Saving worlds" in reopen
assert "All dimensions are saved" in run and "All dimensions are saved" in reopen
assert "BUILD SUCCESSFUL" in run
assert "exit value 143" in reopen and "BUILD FAILED" in reopen

data = json.loads((ROOT / "foundation-final.json").read_text())["data"]
assert data["schema"] == 6
rails = [r for r in data["rails"] if r["family"] == "sns_free_spin"]
assert len(rails) == 11  # three older sections plus eight demo sections
demo_rails = [r for r in rails if r["from"] >= 164926745481159]
assert len(demo_rails) == 8
assert all(r["valid"] == 1 and r["provider"] == "fourthdimension:original" for r in demo_rails)
assert all(sum(s["to"] == r["from"] for s in demo_rails) == 1 for r in demo_rails)
assert all(sum(s["from"] == r["to"] for s in demo_rails) == 1 for r in demo_rails)
spans = [span for rail in demo_rails for span in rail["magnets"]]
assert [(s["mode"], s["start"], s["end"], s["targets"]) for s in spans] == [
    ("INDUCE", 3.0, 9.0, 1),
    ("INDUCE", 12.0, 18.0, 8),
    ("DAMP", 24.0, 32.0, 15),
]
car = next(v for v in data["vehicles"] if identifier(v["chassis"]) == CHASSIS)
assert car["family"] == "sns_free_spin" and car["fault"] == ""
assert car["boarding_ready"] == 1 and car["stopping"] == 1 and car["restraints"] == 0
assert tuple(identifier(c["body"]) for c in car["carriers"]) == CARRIERS
assert all(c["capture"] == 1 and c["occupants"] == [] and c["rider_seats"] == []
           for c in car["carriers"])
assert any(r["from"] == car["boundFrom"] and r["to"] == car["boundTo"] for r in demo_rails)

world_data = subprocess.check_output([
    "tar", "-I", "zstd", "-xOf", str(ROOT / "world.tar.zst"),
    "FD403FreeSpinDemoDamper2/data/fourthdimension_foundation.dat",
])
assert world_data == (ROOT / "foundation-final.dat").read_bytes()
for path in ROOT.glob("*.png"):
    raw = path.read_bytes()
    assert raw[:8] == b"\x89PNG\r\n\x1a\n"
    assert struct.unpack(">II", raw[16:24]) == (854, 480), path.name
print("Free Spin damper-demo record: verified")
