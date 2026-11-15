#!/usr/bin/env python3
"""Generate original Ponder diagrams and English text without external assets.

The tiny NBT writer uses only Python's standard library. Colored strips are
explicit teaching diagrams, not assembled native paths or physics fixtures.
"""
import gzip
import io
import json
import struct
from pathlib import Path

REPO = Path(__file__).resolve().parents[1]
RESOURCES = REPO / "src/main/resources"
LESSONS = json.loads((REPO / "scripts/ponder-lessons.json").read_text())


def utf(value):
    encoded = value.encode("utf-8")
    return struct.pack(">H", len(encoded)) + encoded


def nbt(value):
    if isinstance(value, str):
        return 8, utf(value)
    if isinstance(value, int):
        return 3, struct.pack(">i", value)
    if isinstance(value, list):
        encoded = [nbt(v) for v in value]
        kind = encoded[0][0] if encoded else 10
        if any(k != kind for k, _ in encoded):
            raise ValueError("NBT lists must be homogeneous")
        return 9, bytes([kind]) + struct.pack(">i", len(encoded)) + b"".join(v for _, v in encoded)
    if isinstance(value, dict):
        fields = []
        for name, child in value.items():
            kind, payload = nbt(child)
            fields.append(bytes([kind]) + utf(name) + payload)
        return 10, b"".join(fields) + b"\0"
    raise TypeError(value)


def schematic(name, blocks, size=(13, 9, 13)):
    palette = []
    placed = []
    for pos, state in sorted(blocks.items()):
        if state not in palette:
            palette.append(state)
        placed.append({"pos": list(pos), "state": palette.index(state)})
    _, payload = nbt({"DataVersion": 3955, "size": list(size), "palette": palette,
                      "blocks": placed, "entities": []})
    target = RESOURCES / f"assets/fourthdimension/ponder/{name}.nbt"
    target.parent.mkdir(parents=True, exist_ok=True)
    output = io.BytesIO()
    # GzipFile keeps a fixed OS byte across Python versions; gzip.compress has
    # changed its header and default compression level between 3.12 and 3.14.
    with gzip.GzipFile(fileobj=output, mode="wb", filename="", mtime=0, compresslevel=9) as stream:
        stream.write(b"\x0a\0\0" + payload)
    target.write_bytes(output.getvalue())


def state(name, **properties):
    result = {"Name": name if ":" in name else f"minecraft:{name}"}
    if properties:
        result["Properties"] = properties
    return result


def base():
    return {(x, 0, z): state("smooth_stone") for x in range(13) for z in range(13)}


FAMILIES = [("arrow", "red"), ("legacy", "yellow"), ("free_spin", "blue"),
            ("revised", "purple"), ("pax", "green")]


def car(family, color):
    blocks = base()
    for z in range(1, 12):
        blocks[(6, 1, z)] = state(f"{color}_concrete")
        for x in (5, 7):
            blocks[(x, 1, z)] = state("iron_bars", north="true", south="true", east="false", west="false", waterlogged="false")
    for z in (1, 11):
        blocks[(6, 2, z)] = state("simulatedcoasters:coaster_anchorpoint")
    paired = family in ("free_spin", "revised")
    rows = (3, 9) if paired else (6,)
    for z in range(min(rows), max(rows) + 1):
        blocks[(6, 3, z)] = state("iron_block")
    for z in rows:
        for x in range(4, 9):
            blocks[(x, 4, z)] = state("fourthdimension:wing_strut")
        for side, xs in ((-1, (3, 2)), (1, (9, 10))):
            for x in xs:
                blocks[(x, 4, z)] = state("fourthdimension:" + ("revised_axle" if family == "revised" else "free_spin_axle" if family in ("free_spin", "pax") else "carrier_axle"))
                seat_rows = (z - 1, z + 1) if family == "legacy" else (z,)
                for seat_z in seat_rows:
                    facing = "south" if (z > 6 if paired else seat_z > 6) else "north"
                    blocks[(x, 3, seat_z)] = state(f"fourthdimension:{family}_seat", facing=facing, restrained="false")
    return blocks


def generate():
    # Shared language is integrated by its owner. Export an exact scoped patch;
    # do not erase unrelated translations during deterministic regeneration.
    language_path = REPO / "docs/evidence/fd917/art/ponder-language-patch.json"
    language = {}
    declarations = []
    for name, lesson in LESSONS.items():
        language[f"fourthdimension.ponder.{name}.header"] = lesson["title"]
        for index, line in enumerate(lesson["text"], 1):
            language[f"fourthdimension.ponder.{name}.text_{index}"] = line
        lines = ",\n          ".join(json.dumps(line) for line in lesson["text"])
        declarations.append(f'    Map.entry("{name}", new Lesson({json.dumps(lesson["title"])}, List.of(\n          {lines})))')
    language_path.write_text(json.dumps(language, indent=2) + "\n")
    java = REPO / "src/main/java/dev/fourthdimension/client/ponder/PonderLessons.java"
    java.parent.mkdir(parents=True, exist_ok=True)
    java.write_text('''package dev.fourthdimension.client.ponder;

import java.util.List;
import java.util.Map;

/** Generated by scripts/generate-ponder.py; edit scripts/ponder-lessons.json. */
final class PonderLessons {
  private PonderLessons() {}
  record Lesson(String title, List<String> text) {}
  static final Map<String, Lesson> ALL = Map.ofEntries(
''' + ",\n".join(declarations) + ');\n}\n')
    matching = base()
    for i, (family, color) in enumerate(FAMILIES):
        x = 2 + i * 2
        for z in range(2, 11):
            matching[(x, 1, z)] = state(f"{color}_concrete")
        matching[(x, 2, 6)] = state(f"fourthdimension:{family}_seat", facing="north", restrained="false")
    schematic("matching", matching)
    construction = base()
    for z in (1, 11):
        construction[(6, 2, z)] = state("simulatedcoasters:coaster_anchorpoint")
    for z in range(2, 11):
        construction[(6, 2, z)] = state("red_concrete")
    schematic("construction", construction)
    for family, color in FAMILIES:
        schematic(family, car(family, color))
    magnets = car("free_spin", "blue")
    for z, color in ((3, "orange"), (9, "cyan")):
        for x in (4, 8):
            magnets[(x, 2, z)] = state(f"{color}_concrete")
    schematic("magnets", magnets)
    boarding = car("arrow", "red")
    boarding[(6, 1, 0)] = state("fourthdimension:boarding_station", facing="north", ready="false", locked="false")
    boarding[(6, 1, 1)] = state("lever", face="floor", facing="north", powered="false")
    schematic("boarding", boarding)
    # Original compact catch/lift diagram. It deliberately does not instantiate
    # real rail constraints in Ponder's temporary world.
    pax = base()
    for z in range(1, 12):
        y = 1 + abs(z - 6) // 2
        pax[(6, y, z)] = state("green_concrete")
    pax[(4, 1, 6)] = state("fourthdimension:shuttle_winch", axis="x")
    # Route-local carriage above the lead chassis; the remote kinetic unit is
    # deliberately separate. These blocks illustrate roles, not cable routing.
    pax[(6, 6, 6)] = state("fourthdimension:shuttle_carriage")
    pax[(6, 5, 6)] = state("iron_bars", north="false", south="false", east="false", west="false", waterlogged="false")
    for x in (2, 3, 9, 10):
        pax[(x, 3, 6)] = state("fourthdimension:pax_seat", facing="north", restrained="false")
        pax[(x, 4, 6)] = state("fourthdimension:free_spin_axle")
    for x in range(4, 9):
        pax[(x, 4, 6)] = state("fourthdimension:wing_strut")
    schematic("pax", pax)


if __name__ == "__main__":
    generate()
