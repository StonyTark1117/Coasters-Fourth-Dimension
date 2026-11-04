#!/usr/bin/env python3
"""Verify the FD402 occupied recovery strength-rating checkpoint."""

import hashlib
import json
from pathlib import Path
import subprocess


here = Path(__file__).resolve().parent
repo = here.parents[4]


def sha(path):
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


for line in (here / "SHA256SUMS").read_text().splitlines():
    expected, name = line.split("  ", 1)
    assert sha(here / name) == expected, name

metadata = json.loads((here / "metadata.json").read_text())
hashes = metadata["hashes"]
assert sha(here / "gametest.log.zst") == hashes["compressedLog"]
raw = subprocess.check_output(["zstd", "-dc", str(here / "gametest.log.zst")])
assert hashlib.sha256(raw).hexdigest() == hashes["uncompressedLog"]
text = raw.decode(errors="replace")
assert "Enabled Gametest Namespaces: [fourthdimension_fd402]" in text
assert "fourthdimension_fd402_strength:0' (1 tests)" in text
assert "2 GAME TESTS COMPLETE" in text
assert "All 2 required tests passed" in text

for name, expected in hashes.items():
    if not name.startswith("src/"):
        continue
    assert sha(repo / name) == expected, name
assert sha(here / "RecoveryOperations.java.txt") == \
       hashes["src/main/java/dev/fourthdimension/vehicle/RecoveryOperations.java"]
assert sha(here / "RecoveryStrengthGameTests.java.txt") == \
       hashes["src/main/java/dev/fourthdimension/gametest/RecoveryStrengthGameTests.java"]

assert metadata["result"] == "pass"
assert metadata["requiredTests"] == 2 and metadata["strengthTests"] == 1
metrics = metadata["metrics"]
requirements = metadata["requirements"]
assert metrics["ratedCars"] == requirements["maximumRecoveryCars"] == 4
assert metrics["occupiedRiders"] == 16
assert metrics["oversizedCars"] == 5 and metrics["oversizedRejected"]
assert metrics["travelBlocks"] > requirements["minimumTravelBlocks"]
assert metrics["maximumSpacingErrorBlocks"] < requirements["maximumSpacingErrorBlocks"]
assert metrics["maximumRetainedAngleRadians"] < requirements["maximumRetainedAngleRadians"]
assert metrics["maximumSpeedBlocksPerSecond"] < requirements["maximumSpeedBlocksPerSecond"]
assert metrics["normalBrake"] and metrics["originalLinksAfterDetach"]

source = (repo / "src/main/java/dev/fourthdimension/vehicle/RecoveryOperations.java").read_text()
test = (repo / "src/main/java/dev/fourthdimension/gametest/RecoveryStrengthGameTests.java").read_text()
assert "MAX_RECOVERY_CARS = 4" in source
assert "cars.size() > MAX_RECOVERY_CARS" in source
assert "rated for at most " in source
assert "travel > 3" in test
assert "maxSpacing[0] < .15" in test
assert "maxAngle[0] < .04" in test
assert "oversized.cars().stream().allMatch(car -> car.recoveryCart == null)" in test

print("FD402 occupied recovery strength rating: verified")
