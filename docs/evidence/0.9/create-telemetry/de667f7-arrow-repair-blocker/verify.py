"""Verify retained observations and hashes; this does not run Minecraft."""
from pathlib import Path
import hashlib
import json

root = Path(__file__).resolve().parent
records = [json.loads(line) for line in (root / "observations.jsonl").read_text().splitlines()]
by_case = {record["case"]: record for record in records}
chassis = [543374797, -1922610306, -1252424642, 897751176]
a, m, b = 8796093104211, 8796093177939, 8796093251667

def car(case):
    return next(v for v in by_case[case]["savedVehicles"] if v["chassis"] == chassis)

def rail(case):
    return next(r for r in by_case[case]["savedRails"] if r["from"] == m and r["to"] == b)

def identity(v):
    return [(c["body"], c["rider_seats"], c["occupants"]) for c in v["carriers"]]

ready = car("repair-occupied-closed-ready")
assert ready["fault"] == "" and ready["restraints"] == 1
assert (ready["boundFrom"], ready["boundTo"]) == (a, m)
assert sum(len(c["occupants"]) for c in ready["carriers"]) == 1
for record in records[1:]:
    v = car(record["case"])
    assert identity(v) == identity(ready), record["case"]
    assert v["restraints"] == 1 and all(c["capture"] for c in v["carriers"])
    assert "RootVehicle" in record["players"]["Dev"]
    assert "fourthdimension:ride_fault" in record["repairDisplayLink"]
rebound = car("repair-occupied-support-absent")
assert (rebound["boundFrom"], rebound["boundTo"]) == (m, b)
assert rebound["fault"] == "" and not rebound["alignment_required"]
lost = car("repair-no-support-verified")
assert lost["fault"] == "Matching track support lost" and lost["alignment_required"]
restored = rail("repair-restored-verified")
assert not restored["valid"]
assert car("repair-restored-verified")["fault"] == lost["fault"]
repaired = rail("repair-explicit-repair-before-realign")
assert repaired["valid"]
assert restored["geometry_hash"] == repaired["geometry_hash"] == rail("repair-occupied-closed-ready")["geometry_hash"]
assert restored["profile"] == repaired["profile"] == rail("repair-occupied-closed-ready")["profile"]
for case in ["repair-explicit-repair-before-realign", "repair-explicit-realign", "repair-capture-retry-blocked", "repair-final-retained-blocker"]:
    v = car(case)
    assert v["fault"] == "End of compatible track; capture and inspect the route"
    assert v["alignment_required"] and not v["boarding_ready"]
    assert v["fault"] in by_case[case]["repairSign"]
assert lost["fault"] in by_case["repair-no-support-verified"]["repairSign"]
manifest = json.loads((root / "manifest.json").read_text())
assert manifest["sourceRevision"] == "de667f7bd7deef808d6229df9e2d386e87dd598a"
assert manifest["jarSha256"] == "6231a0241bef082df3f3edff689e74ae2604465aa095c13656136f13e10de3c5"
for name, expected in manifest["files"].items():
    assert hashlib.sha256((root / name).read_bytes()).hexdigest() == expected, name
print(f"Verified {len(records)} retained observations and {len(manifest['files'])} file hashes; failed acceptance remains failed.")
