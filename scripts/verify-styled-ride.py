#!/usr/bin/env python3
"""Verify saved observations from the dev.4 GUI lifecycle and styled Arrow ride.

Reads committed evidence only. Rendering observations are reviewed separately;
this script does not claim arbitrary curve/bank clearance or graphics support.
"""
import json
import math
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
evidence = root / "docs/evidence/0.4"


def saved(name):
    result = json.loads((evidence / f"styled-{name}.json").read_text())["data"]
    assert result["schema"] == 5
    return result


def indexed(records, key):
    result = {tuple(record[key]): record for record in records}
    assert len(result) == len(records), "Duplicate identity"
    return result


def excluding(record, *keys):
    return {key: value for key, value in record.items() if key not in keys}


baseline = saved("baseline")
old_rails = indexed(baseline["rails"], "id")
old_vehicles = indexed(baseline["vehicles"], "chassis")
straight_id = (-1409238043, -1011071397, -1854073850, 1741977380)
curve_id = (582110559, -1317781297, -1637917680, 2088743903)
arrow_id = next(k for k, v in old_vehicles.items() if v["family"] == "arrow_controlled")
previous_rails = None

for name in ("high-save", "restored-save", "occupied-save", "final-save"):
    snapshot = saved(name)
    rails = indexed(snapshot["rails"], "id")
    vehicles = indexed(snapshot["vehicles"], "chassis")
    assert rails.keys() == old_rails.keys() and vehicles.keys() == old_vehicles.keys()
    assert len(rails) == 35 and len(vehicles) == 8
    assert sum(len(v["carriers"]) for v in vehicles.values()) == 20
    for identity, rail in rails.items():
        old = old_rails[identity]
        assert excluding(rail, "appearance", "revision", "profile") == excluding(
            old, "appearance", "revision", "profile"
        ), "Mechanical identity or geometry changed"
        if identity == straight_id:
            expected = json.loads(json.dumps(old["profile"]))
            if name == "high-save":
                expected[1]["angle"] *= 3
            assert rail["profile"] == expected, "Full-turn profile was erased or clamped"
            assert rail["appearance"] == {
                "id": "coasterssimulatedextratypes:arrow_4d_dbl_spine", "tint": 0x4A90E2
            }
            assert rail["revision"] == old["revision"] + (2 if name == "high-save" else 3)
        elif identity == curve_id and name != "high-save":
            assert rail["profile"] == old["profile"]
            assert rail["appearance"] == {
                "id": "coasterssimulatedextratypes:arrow_4d", "tint": 0x9B59B6
            }
            assert rail["revision"] == old["revision"] + 1
        else:
            assert rail == old, "Unedited section changed"
    for identity, vehicle in vehicles.items():
        old = old_vehicles[identity]
        allowed_vehicle = ("carriers", "revision") if identity == arrow_id else ("carriers",)
        assert excluding(vehicle, *allowed_vehicle) == excluding(old, *allowed_vehicle)
        assert vehicle["fault"] == "" and vehicle["boarding_ready"]
        assert not vehicle["restraints"] and not vehicle["maintenance"]
        if identity == arrow_id:
            assert vehicle["revision"] == old["revision"] + (
                0 if name in ("high-save", "restored-save") else 6
            ), "Unexpected operator transaction"
        carriers = indexed(vehicle["carriers"], "body")
        old_carriers = indexed(old["carriers"], "body")
        assert carriers.keys() == old_carriers.keys()
        for body, carrier in carriers.items():
            old_carrier = old_carriers[body]
            assert excluding(carrier, "angle", "omega", "occupants", "rider_seats") == excluding(
                old_carrier, "angle", "omega", "occupants", "rider_seats"
            )
            assert abs(carrier["angle"] - old_carrier["angle"]) < .015
            assert math.isfinite(carrier["omega"])
            if name == "occupied-save" and body == (1149311824, 1680297634, -1516308889, -8020090):
                assert carrier["occupants"] == [[940439953, -167562164, -1601161573, -1389718966]]
                assert carrier["rider_seats"] == [{
                    "seat": 274877911039,
                    "player": [940439953, -167562164, -1601161573, -1389718966],
                }]
            else:
                assert carrier["occupants"] == [] and carrier["rider_seats"] == []
    if name in ("occupied-save", "final-save"):
        assert rails == previous_rails, "Ride or cold reopen changed the track program"
    previous_rails = rails
    print(f"{name}: schema=5; 35 rails, 8 vehicles, 20 carriers; expected intent/profile/operator state")

player = json.loads((evidence / "styled-final-player.json").read_text())
assert "RootVehicle" not in player and not player.get("NoGravity", False)
assert player["Pos"] == [-105.5, 124.0, 32.5]
events = (evidence / "styled-client-events.txt").read_text()
samples = [float(value) for value in re.findall(
    r"body=44811b50-6427-4ea2-a59e-f667ff859f86 snapshotAngle=([^ ]+) "
    r"nativeAngle=[^ ]+ occupants=\[380df991-f603-344c-a090-369bad2a924a\] riding=true", events
)]
assert samples and max(samples) > 6.2, "Missing occupied full-turn observation"
assert abs(samples[-1]) < .015, "Missing captured return observation"
assert events.count("BUILD SUCCESSFUL") == 3, "Missing clean client completions"
assert "[20:47:00]" in events and "FD_OPTIONAL Track Styles 1.2.0" in events
assert "Found no elements matching RootVehicle" in events
assert "Found no elements matching NoGravity" in events
print(f"Occupied turn: peak recorded target={max(samples):.6f} rad; final boarding/dismount preserved")
print("PASS: bounded dev.4 GUI/save/ride observations; graphics/clearance breadth remains separate")
