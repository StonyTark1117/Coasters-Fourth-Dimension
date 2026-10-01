#!/usr/bin/env python3
"""Verify bounded Immersion QA log observations, not general compatibility.

Usage: verify-immersion-camera.py RIDE_LOG DISMOUNT_LOG
The ride log must include the enabled and disabled Arrow inversion runs. The
second log must contain an actual mounted-to-unmounted transition in one client
session; a restart boundary cannot satisfy the dismount requirement.
"""
import json
import math
import re
import sys
from pathlib import Path


def samples(path):
    rows = []
    for line in Path(path).read_text().splitlines():
        if "FD_IMMERSION " not in line:
            continue
        row = dict(re.findall(r"(\w+)=([^ ]+)", line.split(" camera=", 1)[0]))
        assert row.get("metric") == "normalized-v1", "Use normalized orientation measurements"
        for name in ("rawError", "carrierUp", "cameraUp", "yaw", "pitch", "travel"):
            row[name] = float(row[name])
            assert math.isfinite(row[name]), ("nonfinite camera data", line)
        rows.append(row)
    assert rows, f"No camera samples in {path}"
    return rows


ride = samples(sys.argv[1])
mounted = [r for r in ride if r["riding"] == "true" and r["body"] != "none"]
assert len(mounted) >= 100, "Insufficient mounted coverage"
assert all(0 <= r["rawError"] < 1e-6 for r in mounted), "Carrier pose source mismatch"
inverted = [r for r in mounted if r["carrierUp"] < -.95]
assert len(inverted) >= 3, "Missing native inversion"
for enabled, inertia in (("true", "0.04"), ("true", "0.0"), ("false", "0.0")):
    phase = [r for r in inverted if r["enabled"] == enabled and r["inertia"] == inertia]
    assert len(phase) >= 2, f"Missing inverted camera state {enabled}, inertia={inertia}"
    assert any(r["cameraUp"] < -.5 for r in phase), "View failed to invert"
disabled = [r for r in mounted if r["enabled"] == "false"]
assert all(r["processing"] == "false" and r["travel"] == 0 for r in disabled)
assert max(r["yaw"] for r in mounted) - min(r["yaw"] for r in mounted) > 20
assert max(r["pitch"] for r in mounted) - min(r["pitch"] for r in mounted) > 4

exit_rows = samples(sys.argv[2])
transitions = []
for previous, current in zip(exit_rows, exit_rows[1:]):
    if previous["body"] != "none" and previous["riding"] == "true" and current["riding"] == "false":
        assert current["body"] == "none" and current["travel"] == 0, "Stale carrier effects after exit"
        expected_up = math.cos(math.radians(current["pitch"]))
        assert abs(current["cameraUp"] - expected_up) < .001, "View did not return to ordinary mouse pitch"
        transitions.append(current)
assert transitions, "No mounted-to-unmounted transition within the dismount session"
print(json.dumps({"mounted_samples": len(mounted), "inverted_samples": len(inverted),
                  "max_raw_error_radians": max(r["rawError"] for r in mounted),
                  "disabled_samples": len(disabled), "dismount_transitions": len(transitions),
                  "scope": "Logged first-person Arrow QA only; not multiplayer or all-family certification"}, indent=2))
