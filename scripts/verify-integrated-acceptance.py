#!/usr/bin/env python3
"""Verify read-only JSON evidence from the five-family integrated-world check.

Usage: verify-integrated-acceptance.py schema3-before.json schema4-saved.json
       schema4-reopened.json
The JSON inputs come from inspect-foundation-save.py after clean Save and Quit.
This checks the observed fixtures; it does not replace running their rides.
"""
import json
import math
import sys


def read(path):
    with open(path, encoding="utf-8") as source:
        return json.load(source)["data"]


def index(records, key):
    result = {tuple(record[key]): record for record in records}
    assert len(result) == len(records), f"Duplicate {key}"
    return result


def compare_rails(before, after):
    for identity, rail in before.items():
        assert identity in after, f"Lost rail {identity}"
        for field in (
            "family", "variant", "provider", "from", "to", "profile", "magnets",
            "geometry_hash", "valid", "revision",
        ):
            assert rail[field] == after[identity][field], f"Changed rail {field}: {identity}"


def compare_vehicles(before, after, reopening):
    for identity, vehicle in before.items():
        assert identity in after, f"Lost chassis {identity}"
        restored = after[identity]
        for field in ("family", "variant", "damping", "ballast"):
            assert vehicle[field] == restored[field], f"Changed vehicle {field}: {identity}"
        carriers = index(vehicle["carriers"], "body")
        reopened = index(restored["carriers"], "body")
        assert carriers.keys() == reopened.keys(), f"Changed carrier identities: {identity}"
        for body, carrier in carriers.items():
            other = reopened[body]
            for field, default in (
                ("seats", []), ("side", 0), ("longitudinal", 0),
                ("ballast", 0), ("pivot_height", 2), ("child_pivot_offset", 0),
            ):
                assert carrier.get(field, default) == other.get(field, default), (
                    f"Changed carrier {field}: {body}"
                )
            if reopening:
                assert abs(carrier["angle"] - other["angle"]) < .01, (
                    f"Lost unwrapped boarding angle: {body}"
                )


def assert_ready(data):
    for vehicle in data["vehicles"]:
        assert not vehicle["fault"], f"Vehicle fault: {vehicle['fault']}"
        assert vehicle["boarding_ready"] and not vehicle["restraints"]
        for carrier in vehicle["carriers"]:
            assert carrier["capture"] and not carrier["occupants"] and not carrier["rider_seats"]
            assert abs(math.remainder(carrier["angle"], 2 * math.pi)) < .01


old, saved, reopened = (read(path) for path in sys.argv[1:])
assert old["schema"] == 3 and saved["schema"] == reopened["schema"] == 4
old_rails, saved_rails, reopened_rails = (index(data["rails"], "id") for data in (old, saved, reopened))
old_cars, saved_cars, reopened_cars = (index(data["vehicles"], "chassis") for data in (old, saved, reopened))
assert len(old_cars) == 3 and len(old_rails) == 24
assert len(saved_cars) == len(reopened_cars) == 8
assert len(saved_rails) == len(reopened_rails) == 35
assert saved_cars.keys() == reopened_cars.keys() and saved_rails.keys() == reopened_rails.keys()
compare_rails(old_rails, saved_rails)
compare_rails(saved_rails, reopened_rails)
compare_vehicles(old_cars, saved_cars, reopening=False)
compare_vehicles(saved_cars, reopened_cars, reopening=True)
for data in (saved, reopened):
    assert_ready(data)
    assert {vehicle["family"] for vehicle in data["vehicles"]} == {
        "arrow_controlled", "zacspin_legacy", "sns_free_spin", "zacspin_revised", "pax_shuttle",
    }
    assert sum(len(vehicle["carriers"]) for vehicle in data["vehicles"]) == 20
    for vehicle in data["vehicles"]:
        expected = -.5 if vehicle["family"] == "zacspin_revised" else 0
        assert all(carrier["child_pivot_offset"] == expected for carrier in vehicle["carriers"])
print("PASS schema 3 -> 4: all 3 old vehicles and 24 old rails preserved")
print("PASS same-process reopen: all 8 chassis, 20 carriers and 35 rails preserved")
print("PASS profiles, magnetic spans, functional family/provider IDs, seat topology and hinge offsets")
print("PASS all five families captured, ready, unrestrained, empty and without faults")
print("PASS unwrapped boarding angles retained within 0.01 rad; runtime drift is allowed")
print("Scope: these fixture save assertions accompany GUI ride evidence, not full FD903 coverage")
