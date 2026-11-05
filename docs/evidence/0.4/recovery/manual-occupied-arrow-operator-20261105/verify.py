#!/usr/bin/env python3
import gzip
import hashlib
from pathlib import Path
import tarfile


ROOT = Path(__file__).resolve().parent


def text(name: str) -> str:
    return (ROOT / name).read_text(encoding="utf-8")


for line in text("SHA256SUMS").splitlines():
    expected, name = line.split("  ", 1)
    actual = hashlib.sha256((ROOT / name).read_bytes()).hexdigest()
    assert actual == expected, (name, expected, actual)

chassis = "[I;1072001237,-2039920335,-1325126715,-1785077027]"
bearing = "[I;-1934957001,-1290777781,-1202197365,-230454829]"
player = "[I;940439953,-167562164,-1601161573,-1389718966]"

foundation_input = text("foundation-input.snbt")
input_vehicle = foundation_input[foundation_input.rfind("{alignment_required"):]
assert chassis in input_vehicle
assert f"recovery_cart:{bearing}" in input_vehicle
assert "restraints:1b" in input_vehicle
assert f"occupants:[{player}]" in input_vehicle
assert f"player:{player}" in input_vehicle
assert "RootVehicle" in text("player-input.snbt")

foundation_final = text("foundation-final.snbt")
final_vehicle = foundation_final[foundation_final.rfind("{alignment_required"):]
assert chassis in final_vehicle
assert "recovery_cart" not in final_vehicle
assert "restraints:0b" in final_vehicle
assert final_vehicle.count("occupants:[]") == 2
assert final_vehicle.count("rider_seats:[]") == 2

player_final = text("player-final.snbt")
assert "RootVehicle" not in player_final
assert 'base:5.0d,id:"minecraft:player.block_interaction_range"' in player_final

with gzip.open(ROOT / "selected-continuation-client.log.gz", "rt", encoding="utf-8") as handle:
    continuation_log = handle.read()
for marker in (
    "FD_BOARDING_RESTORE mounted player=Dev",
    "Base value for attribute Block Interaction Range for entity Dev set to 5.0",
    "Found no elements matching RootVehicle",
    "Stopping!",
):
    assert marker in continuation_log, marker

equivalence = text("production-class-equivalence.txt")
assert equivalence.count(".class") == 7
assert "811090454924d86c234b4d72efa19c568bbea108b55b84fb3b82e697586288aa" in equivalence
assert "656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7" in equivalence

for archive_name in ("input-world-before-cleanup.tar.gz", "final-world-after-cleanup.tar.gz"):
    with tarfile.open(ROOT / archive_name, "r:gz") as archive:
        names = set(archive.getnames())
    assert "FD402OperatorFinal/data/fourthdimension_foundation.dat" in names
    assert "FD402OperatorFinal/playerdata/380df991-f603-344c-a090-369bad2a924a.dat" in names

for index in range(1, 17):
    assert any(ROOT.glob(f"{index:02d}-*.png")), index

print("FD402C occupied Arrow operator evidence: pass")
