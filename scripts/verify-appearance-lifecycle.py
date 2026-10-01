#!/usr/bin/env python3
"""Verify the recorded real schema-4 upgrade and optional appearance lifecycle."""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
evidence = root / "docs/evidence/0.4"
baseline = json.loads(
    (root / "docs/evidence/0.3/integrated-schema4-reopened.json").read_text()
)["data"]


def indexed(records, key):
    result = {tuple(record[key]): record for record in records}
    assert len(result) == len(records), "Duplicate persisted identity"
    return result


old_rails = indexed(baseline["rails"], "id")
old_vehicles = indexed(baseline["vehicles"], "chassis")
previous = None
for name in ("appearance-saved", "appearance-reloaded", "provider-removed", "provider-restored"):
    saved = json.loads((evidence / (name + ".json")).read_text())["data"]
    assert saved["schema"] == 5
    rails = indexed(saved["rails"], "id")
    vehicles = indexed(saved["vehicles"], "chassis")
    assert rails.keys() == old_rails.keys() and vehicles.keys() == old_vehicles.keys()
    assert len(rails) == 35 and len(vehicles) == 8
    assert sum(len(v["carriers"]) for v in vehicles.values()) == 20
    selected = []
    for identity, rail in rails.items():
        old = old_rails[identity]
        assert {k: v for k, v in rail.items() if k not in ("appearance", "revision")} == {
            k: v for k, v in old.items() if k != "revision"
        }, "Appearance altered functional rail data"
        if rail["appearance"]["id"] != "fourthdimension:original":
            selected.append(rail)
        else:
            assert rail["appearance"]["tint"] == -1
            assert rail["revision"] == old["revision"]
    assert len(selected) == 1
    assert selected[0]["appearance"] == {
        "id": "coasterssimulatedextratypes:arrow_4d", "tint": 0x4A90E2
    }
    assert selected[0]["revision"] == old_rails[tuple(selected[0]["id"])]["revision"] + 1
    for identity, vehicle in vehicles.items():
        old = old_vehicles[identity]
        assert {k: v for k, v in vehicle.items() if k != "carriers"} == {
            k: v for k, v in old.items() if k != "carriers"
        }, "Vehicle binding or boarding state changed"
        carriers = indexed(vehicle["carriers"], "body")
        old_carriers = indexed(old["carriers"], "body")
        assert carriers.keys() == old_carriers.keys()
        for carrier_id, carrier in carriers.items():
            # This live captured fixture still simulates tiny angular corrections;
            # it is not a frozen-state or moving-ride acceptance test.
            assert {k: v for k, v in carrier.items() if k not in ("angle", "omega")} == {
                k: v for k, v in old_carriers[carrier_id].items() if k not in ("angle", "omega")
            }, "Carrier definition, capture or occupancy changed"
    if previous is not None:
        assert rails == previous, "Resource/provider lifecycle altered rail records"
    previous = rails
    print(f"{name}: schema=5 rails=35 vehicles=8 carriers=20 profiles/bindings/intent preserved")

print("PASS: real schema-4 upgrade, resource reload, provider removal/save and reinstallation/save")
