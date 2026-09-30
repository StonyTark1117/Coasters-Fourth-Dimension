#!/usr/bin/env python3
"""Generate original schematic Ponder structures. No third-party structures or assets."""
import gzip
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources/assets/fourthdimension/ponder"


def string(value):
    raw = value.encode("utf-8")
    return struct.pack(">H", len(raw)) + raw


def payload(kind, value):
    if kind == 3:
        return struct.pack(">i", value)
    if kind == 8:
        return string(value)
    if kind == 9:
        child, items = value
        return bytes([child]) + struct.pack(">i", len(items)) + b"".join(payload(child, x) for x in items)
    if kind == 10:
        return b"".join(bytes([k]) + string(n) + payload(k, v) for n, (k, v) in value.items()) + b"\0"
    raise ValueError(kind)


for family, seat, color, paired in [
    ("arrow", "arrow", "red", False),
    ("legacy", "legacy", "yellow", False),
    ("free_spin", "free_spin", "blue", True),
    ("revised", "revised", "purple", True),
    ("pax", "pax", "green", False),
]:
    palette, blocks = [], {}

    def block(x, y, z, name, **properties):
        state = {"Name": (8, name)}
        if properties:
            state["Properties"] = (10, {k: (8, v) for k, v in properties.items()})
        if state not in palette:
            palette.append(state)
        blocks[x, y, z] = palette.index(state)

    for x in range(13):
        for z in range(13):
            block(x, 0, z, "minecraft:smooth_stone")
    for z in range(1, 12):
        block(6, 1, z, "minecraft:" + color + "_concrete")
    block(1, 1, 1, "fourthdimension:shuttle_winch" if family == "pax" else "fourthdimension:boarding_station",
          **({"axis": "x"} if family == "pax" else {"facing": "north", "ready": "false", "locked": "false"}))
    if family == "pax":
        block(6, 3, 2, "fourthdimension:shuttle_carriage")
    if family == "free_spin":
        block(5, 2, 2, "minecraft:orange_concrete")
        block(7, 2, 10, "minecraft:cyan_concrete")
    for x in (3, 9):
        for z in ((3, 9) if paired else (6,)):
            block(x, 4, z, "fourthdimension:carrier_axle")
            for dx in (-1, 1):
                for dz in ((-1, 1) if family == "legacy" else (0,)):
                    block(x + dx, 3, z + dz, "fourthdimension:" + seat + "_seat",
                          facing="north", restrained="false", waterlogged="false")
    tag = {
        "DataVersion": (3, 3955),
        "size": (9, (3, [13, 7, 13])),
        "palette": (9, (10, palette)),
        "blocks": (9, (10, [{"pos": (9, (3, list(pos))), "state": (3, state)} for pos, state in blocks.items()])),
        "entities": (9, (10, [])),
    }
    ROOT.mkdir(parents=True, exist_ok=True)
    (ROOT / ("family_" + family + ".nbt")).write_bytes(
        gzip.compress(b"\x0a\0\0" + payload(10, tag), mtime=0)
    )
