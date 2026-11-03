"""Verify bounded fresh pass and retained historical failure; no Minecraft launch."""
from pathlib import Path
import hashlib
import json
import math
import re

root = Path(__file__).resolve().parent
records = [json.loads(s) for s in (root / "observations.jsonl").read_text().splitlines()]
by_case = {r["case"]: r for r in records}
assert len(records) == 13
old_id = [543374797, -1922610306, -1252424642, 897751176]
new_id = [-1474168113, -778088683, -1679358650, -462846203]
def car(case, identity=new_id):
    return next(v for v in by_case[case]["savedVehicles"] if v["chassis"] == identity)
def bodies(v):
    return [c["body"] for c in v["carriers"]]
def riders(v):
    return [(c["occupants"], c["rider_seats"]) for c in v["carriers"]]
def bound_rail(case):
    v = car(case)
    return next(r for r in by_case[case]["savedRails"] if r["from"] == v["boundFrom"] and r["to"] == v["boundTo"])
old = car("historical-loaded", old_id)
for case in [r["case"] for r in records if r["case"].startswith("historical-")]:
    v = car(case, old_id)
    assert bodies(v) == bodies(old) and riders(v) == riders(old)
    assert v["fault"] == "End of compatible track; capture and inspect the route"
    assert v["alignment_required"] and v["restraints"] and v["stopping"]
    assert "RootVehicle" in by_case[case]["players"]["Dev"]
assert sum(len(c["rider_seats"]) for c in car("fresh-occupied-ready")["carriers"]) == 0
ready = car("fresh-occupied-ready-confirmed")
assert ready["fault"] == "" and not ready["alignment_required"]
assert sum(len(c["rider_seats"]) for c in ready["carriers"]) == 1
occupied_cases = [r["case"] for r in records if r["case"].startswith("fresh-") and r["case"] not in ("fresh-occupied-ready", "fresh-normal-unloaded")]
for case in occupied_cases:
    v = car(case)
    assert bodies(v) == bodies(ready) and riders(v) == riders(ready)
    assert v["restraints"] and v["stopping"] and all(c["capture"] for c in v["carriers"])
    assert "RootVehicle" in by_case[case]["players"]["Dev"]
    assert "fourthdimension:ride_fault" in by_case[case]["repairDisplayLink"]
for case in ["fresh-support-lost", "fresh-restored-inert"]:
    assert car(case)["fault"] == "Matching track support lost" and car(case)["alignment_required"]
assert "Matching track support lost" in by_case["fresh-support-lost"]["repairSign"]
assert not bound_rail("fresh-restored-inert")["valid"]
start, end = "fresh-repaired-delay-start", "fresh-repaired-delay-end"
for case in [start, end]:
    assert car(case)["fault"] == "" and car(case)["alignment_required"]
    assert bound_rail(case)["valid"]
    assert "Matching track support lost" not in by_case[case]["repairSign"]
time_value = lambda case: int(re.search(r"\d+", by_case[case]["gameTime"]).group())
assert time_value(end) - time_value(start) == 240
original = bound_rail("fresh-first-approach-removal")
for case in ["fresh-restored-inert", start, end, "fresh-after-realign", "fresh-normal-unloaded"]:
    r = bound_rail(case)
    assert r["geometry_hash"] == original["geometry_hash"] and r["profile"] == original["profile"]
    assert r["family"] == original["family"] and r["variant"] == original["variant"]
aligned = car("fresh-after-realign")
assert not aligned["alignment_required"] and aligned["fault"] == "" and aligned["boarding_ready"]
final = car("fresh-normal-unloaded")
assert bodies(final) == bodies(ready) and not final["restraints"] and not final["alignment_required"]
assert final["fault"] == "" and final["boarding_ready"]
assert all(not c["occupants"] and not c["rider_seats"] for c in final["carriers"])
assert "RootVehicle" not in by_case["fresh-normal-unloaded"]["players"]["Dev"]
for name, above in [("historical-motion.jsonl", True), ("fresh-repaired-motion.jsonl", False)]:
    samples = [json.loads(s) for s in (root / name).read_text().splitlines()]
    assert len(samples) == 5
    for sample in samples:
        values = [float(x) for x in re.search(r"velocity=\((.*?)\)", sample["motion"]).group(1).split()]
        speed = math.sqrt(sum(v * v for v in values))
        assert (speed > .1) if above else (speed < .1)
input_world = json.loads((root / "input-world.json").read_text())
assert input_world["sourceFoundationSha256"] == input_world["replayFoundationSha256"] == "7b30ddbf3abf72f1002a3935992d6331d1b89ee2beed15f7ccc100d9098ca821"
shutdown = json.loads((root / "shutdown-state.json").read_text())
for name, summary in shutdown.items():
    assert hashlib.sha256((root / f"{name}-stopped-foundation.dat").read_bytes()).hexdigest() == summary["foundationSha256"]
assert shutdown["historical"]["seatAssignments"] == 1 and shutdown["historical"]["alignmentRequired"]
assert shutdown["fresh"]["seatAssignments"] == shutdown["fresh"]["occupants"] == 0
manifest = json.loads((root / "manifest.json").read_text())
assert manifest["sourceRevision"] == "effa1c63a668bf1856c567fc702105c01e9accd4"
assert manifest["jarSha256"] == "9014f8f27724536e088e3a3e546c20930dae45c0fa0b7e363fdb8e62c772de88"
for name, expected in manifest["files"].items():
    assert hashlib.sha256((root / name).read_bytes()).hexdigest() == expected, name
print(f"Verified 13 observations, 10 motion samples, 240-tick delay and {len(manifest['files'])} hashes: fresh bounded pass; historical failure remains.")
