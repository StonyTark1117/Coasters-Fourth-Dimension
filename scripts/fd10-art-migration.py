#!/usr/bin/env python3
"""Read-only identity comparison for the retained historical world and 1.0 copies."""
import argparse
import hashlib
import importlib.util
import json
from pathlib import Path

spec = importlib.util.spec_from_file_location(
    "historical_nbt", Path(__file__).with_name("verify-packaged-historical-migration.py"))
nbt = importlib.util.module_from_spec(spec)
spec.loader.exec_module(nbt)


def keyed(records, key):
    result = {tuple(r[key]): r for r in records}
    assert len(result) == len(records), f"Duplicate {key}"
    return result


def identity_view(data):
    rails = keyed(data["rails"], "id")
    vehicles = keyed(data["vehicles"], "chassis")
    return {
        "rails": {str(k): {name: r.get(name) for name in
                  ("family", "variant", "from", "to", "profile", "magnets")}
                  for k, r in rails.items()},
        "vehicles": {str(k): {
            "family": v["family"], "variant": v.get("variant"),
            "carriers": {str(body): {name: c.get(name) for name in
                         ("side", "axis", "seats", "longitudinal", "child_pivot_offset")}
                         for body, c in keyed(v["carriers"], "body").items()}}
                     for k, v in vehicles.items()},
    }


def rider_view(data):
    # Logical seat/carrier identity is persistent. Native passenger entity UUIDs
    # are allowed to be recreated by Create on reconnect.
    return {str(tuple(v["chassis"])): {
        "restraints": v.get("restraints"),
        "carriers": {str(tuple(c["body"])): c.get("rider_seats", [])
                     for c in v["carriers"]}}
            for v in data["vehicles"]}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path)
    parser.add_argument("candidate", type=Path)
    parser.add_argument("--schema", type=int, required=True)
    parser.add_argument("--riders", action="store_true")
    args = parser.parse_args()
    source, candidate = map(nbt.read_root, (args.source, args.candidate))
    assert candidate["schema"] == args.schema, "Unexpected output schema"
    a, b = identity_view(source), identity_view(candidate)
    # Older records legitimately omit optional fields; compare every historical
    # value that was actually present, including full magnetic/profile intent.
    def compare(old, new, path=""):
        if isinstance(old, dict):
            assert old.keys() == new.keys(), f"Identity/field mismatch at {path}"
            for key in old: compare(old[key], new[key], path + "/" + key)
        elif old is not None:
            assert old == new, f"Saved contract changed at {path}: {old!r} != {new!r}"
    compare(a, b)
    if args.riders:
        assert rider_view(source) == rider_view(candidate), "Logical rider/restraint state changed"
    if not any("recovery_cart" in v for v in source["vehicles"]):
        assert not any("recovery_cart" in v or "recovery_cart_removed" in v
                       for v in candidate["vehicles"]), "Invented recovery state"
    print(json.dumps({
        "source_schema": source["schema"], "candidate_schema": candidate["schema"],
        "source_sha256": hashlib.sha256(args.source.read_bytes()).hexdigest(),
        "candidate_sha256": hashlib.sha256(args.candidate.read_bytes()).hexdigest(),
        "rails": len(candidate["rails"]), "vehicles": len(candidate["vehicles"]),
        "carriers": sum(len(v["carriers"]) for v in candidate["vehicles"]),
        "identities_profiles_and_topology_preserved": True,
        "logical_riders_and_restraints_checked": args.riders,
    }, indent=2))


if __name__ == "__main__":
    main()
