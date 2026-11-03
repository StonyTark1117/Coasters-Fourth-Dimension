"""Validate recorded failed replay; does not launch Minecraft."""
from pathlib import Path
import hashlib
import json
import math
import re

root = Path(__file__).resolve().parent
records = [json.loads(s) for s in (root / "observations.jsonl").read_text().splitlines()]
chassis = [543374797, -1922610306, -1252424642, 897751176]
assert len(records) == 5
cars = [next(v for v in r["savedVehicles"] if v["chassis"] == chassis) for r in records]
def identities(v):
    return [(c["body"], c["occupants"], c["rider_seats"]) for c in v["carriers"]]
for r, v in zip(records, cars):
    assert identities(v) == identities(cars[0])
    assert sum(len(c["rider_seats"]) for c in v["carriers"]) == 1
    assert v["fault"] == "End of compatible track; capture and inspect the route"
    assert v["alignment_required"] and v["restraints"] and v["stopping"]
    assert not v["boarding_ready"]
    assert "RootVehicle" in r["players"]["Dev"]
    assert v["fault"] in r["repairSign"]
    assert "fourthdimension:ride_fault" in r["repairDisplayLink"]
motion = [json.loads(s) for s in (root / "historical-motion.jsonl").read_text().splitlines()]
assert len(motion) == 10
speeds = []
for sample in motion:
    values = [float(x) for x in re.search(r"velocity=\((.*?)\)", sample["motion"]).group(1).split()]
    speed = math.sqrt(sum(x * x for x in values))
    assert speed > .1 and "capture=true" in sample["motion"]
    speeds.append(speed)
input_world = json.loads((root / "input-world.json").read_text())
assert input_world["originalFoundationSha256"] == input_world["copiedFoundationSha256"] == "7b30ddbf3abf72f1002a3935992d6331d1b89ee2beed15f7ccc100d9098ca821"
manifest = json.loads((root / "manifest.json").read_text())
assert manifest["sourceRevision"] == "09690c1a45d09a088438fa40e1ba3af35c9731d4"
assert manifest["jarSha256"] == "bc98cd6cc7b8e2ff6d8d647c8e6914f2b618aac1258fbeef8da070a82ee777e4"
for name, expected in manifest["files"].items():
    assert hashlib.sha256((root / name).read_bytes()).hexdigest() == expected, name
print(f"Verified {len(records)} observations, {len(motion)} motion samples and {len(manifest['files'])} hashes; recovery remains unproven.")
