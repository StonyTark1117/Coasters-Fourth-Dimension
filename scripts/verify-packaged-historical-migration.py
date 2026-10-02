#!/usr/bin/env python3
"""Verify a schema-3 FoundationData file after a packaged-server schema-6 replay."""
import gzip
import struct
import sys
from pathlib import Path


def read_tag(data, offset, kind):
    if kind == 1:
        return data[offset], offset + 1
    if kind == 2:
        return struct.unpack_from(">h", data, offset)[0], offset + 2
    if kind == 3:
        return struct.unpack_from(">i", data, offset)[0], offset + 4
    if kind == 4:
        return struct.unpack_from(">q", data, offset)[0], offset + 8
    if kind == 5:
        return struct.unpack_from(">f", data, offset)[0], offset + 4
    if kind == 6:
        return struct.unpack_from(">d", data, offset)[0], offset + 8
    if kind == 7:
        count = struct.unpack_from(">i", data, offset)[0]
        offset += 4
        return data[offset : offset + count], offset + count
    if kind == 8:
        count = struct.unpack_from(">H", data, offset)[0]
        offset += 2
        return data[offset : offset + count].decode("utf-8"), offset + count
    if kind == 9:
        child = data[offset]
        count = struct.unpack_from(">i", data, offset + 1)[0]
        offset += 5
        values = []
        for _ in range(count):
            value, offset = read_tag(data, offset, child)
            values.append(value)
        return values, offset
    if kind == 10:
        values = {}
        while data[offset] != 0:
            child = data[offset]
            name, offset = read_tag(data, offset + 1, 8)
            value, offset = read_tag(data, offset, child)
            values[name] = value
        return values, offset + 1
    if kind == 11:
        count = struct.unpack_from(">i", data, offset)[0]
        offset += 4
        return list(struct.unpack_from(f">{count}i", data, offset)), offset + count * 4
    if kind == 12:
        count = struct.unpack_from(">i", data, offset)[0]
        offset += 4
        return list(struct.unpack_from(f">{count}q", data, offset)), offset + count * 8
    raise ValueError(f"Unsupported NBT tag {kind}")


def read_root(path):
    raw = gzip.decompress(path.read_bytes())
    if not raw or raw[0] != 10:
        raise ValueError(f"{path} is not a gzip-compressed NBT compound")
    _, offset = read_tag(raw, 1, 8)
    root, offset = read_tag(raw, offset, 10)
    if offset != len(raw):
        raise ValueError(f"{path} has trailing NBT bytes")
    return root["data"]


def identities(records, field):
    return {tuple(record[field]) for record in records}


def profiles(records):
    return {
        tuple(record["id"]): [
            (key["distance"], key["angle"], key.get("slope", 0.0)) for key in record["profile"]
        ]
        for record in records
    }


def verify(source_path, upgraded_path):
    source = read_root(source_path)
    upgraded = read_root(upgraded_path)
    if source.get("schema") != 3:
        raise ValueError("The source file is not the expected schema-3 record")
    if upgraded.get("schema") != 6:
        raise ValueError("The packaged server did not write schema 6")
    if len(source["rails"]) != 24 or len(source["vehicles"]) != 3:
        raise ValueError("The source record no longer has 24 rails and 3 vehicles")
    if identities(source["rails"], "id") != identities(upgraded["rails"], "id"):
        raise ValueError("The packaged migration changed a rail ID")
    if profiles(source["rails"]) != profiles(upgraded["rails"]):
        raise ValueError("The packaged migration changed a historic rotation profile")
    if identities(source["vehicles"], "chassis") != identities(upgraded["vehicles"], "chassis"):
        raise ValueError("The packaged migration changed a chassis ID")
    source_families = {vehicle["family"] for vehicle in source["vehicles"]}
    families = {vehicle["family"] for vehicle in upgraded["vehicles"]}
    if families != source_families:
        raise ValueError(f"The packaged migration changed functional families: {sorted(families)}")
    if any(
        "recovery_cart" in vehicle or "recovery_cart_removed" in vehicle
        for vehicle in upgraded["vehicles"]
    ):
        raise ValueError("The packaged migration invented recovery state")
    print(
        "FD903_PACKAGED_MIGRATION "
        f"sourceSchema={source['schema']} upgradedSchema={upgraded['schema']} "
        f"rails={len(upgraded['rails'])} vehicles={len(upgraded['vehicles'])} "
        f"families={','.join(sorted(families))} identitiesPreserved=true recoveryInvented=false"
        " profilesPreserved=true"
    )


if __name__ == "__main__":
    if len(sys.argv) != 3:
        raise SystemExit(f"Usage: {Path(sys.argv[0]).name} SOURCE.dat UPGRADED.dat")
    verify(Path(sys.argv[1]), Path(sys.argv[2]))
