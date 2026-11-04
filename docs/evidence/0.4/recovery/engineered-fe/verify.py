#!/usr/bin/env python3
"""Verify the isolated FD402 Engineered recovery/FE checkpoint."""

import hashlib
import json
from pathlib import Path
import re
import subprocess

here = Path(__file__).resolve().parent
repo = here.parents[4]
meta = json.loads((here / "metadata.json").read_text())
revision = meta["sourceRevision"]


def sha(data):
    return hashlib.sha256(data).hexdigest()


assert subprocess.run(
    ["git", "merge-base", "--is-ancestor", revision, "HEAD"], cwd=repo
).returncode == 0
for name, expected in meta["sourceHashes"].items():
    committed = subprocess.check_output(["git", "show", f"{revision}:{name}"], cwd=repo)
    assert sha(committed) == expected, name

compressed = (here / "gametest.log.zst").read_bytes()
assert sha(compressed) == meta["compressedLogSha256"]
log = subprocess.check_output(["zstd", "-dc", str(here / "gametest.log.zst")])
assert sha(log) == meta["uncompressedLogSha256"]
text = log.decode()

assert "Coasters: Engineered 1.1.1 (coastersengineered)" in text
assert "Enabled Gametest Namespaces: [fourthdimension_fd402]" in text
assert "1 tests are now running" in text
assert "All 1 required tests passed" in text
assert "BUILD SUCCESSFUL" in text
assert "absent/uninspected" not in text

match = re.search(
    r"FD_ENGINEERED_RECOVERY_FE towingDistance=(\S+) brakedDistance=(\S+) "
    r"starvedTowDistance=(\S+) suppliedEnergy=(\d+) activeEnergy=(\d+) "
    r"speed=(\S+) maxSpeed=(\S+) riderMounted=(\S+)",
    text,
)
assert match
towing, braked, starved, supplied, active, speed, maximum, mounted = match.groups()
metrics = meta["metrics"]
assert float(towing) == metrics["towingDistance"] and float(towing) > .05
assert float(braked) == metrics["brakedDistance"] and float(braked) < .01
assert float(starved) == metrics["starvedTowDistance"] and float(starved) < .01
assert int(supplied) == metrics["suppliedEnergy"]
assert int(active) == metrics["activeEnergy"] and int(active) < int(supplied)
assert float(speed) == metrics["activeSpeed"] and float(speed) <= float(maximum)
assert float(maximum) == metrics["maximumSpeed"]
assert mounted == "true" and metrics["riderMounted"]

assert "brakedEnergy=99680 minimumBrakedEnergy=99680 noBrakeDepletion=true" in text
assert "cleanup vehiclePresent=false remainingVehicles=0" in text
assert "cleanupVerified=true" in text and metrics["cleanupVerified"]
print("FD402 isolated Engineered recovery/FE checkpoint: verified")
