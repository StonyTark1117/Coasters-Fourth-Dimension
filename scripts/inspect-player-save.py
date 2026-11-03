#!/usr/bin/env python3
"""Summarize a stopped Java Edition level.dat for connected-player evidence."""

import gzip
import hashlib
import io
import json
import struct
import sys
from pathlib import Path


def read_nbt(path):
    compressed = Path(path).read_bytes()
    with gzip.GzipFile(fileobj=io.BytesIO(compressed)) as source:
        raw = source.read(16 * 1024 * 1024 + 1)
    if len(raw) > 16 * 1024 * 1024:
        raise ValueError("Save exceeds inspection limit")
    stream = io.BytesIO(raw)

    def number(fmt):
        size = struct.calcsize(">" + fmt)
        data = stream.read(size)
        if len(data) != size:
            raise ValueError("Truncated NBT number")
        return struct.unpack(">" + fmt, data)[0]

    def string():
        size = number("H")
        data = stream.read(size)
        if len(data) != size:
            raise ValueError("Truncated NBT string")
        return data.decode("utf-8")

    def value(kind):
        formats = {1: "b", 2: "h", 3: "i", 4: "q", 5: "f", 6: "d"}
        if kind in formats:
            return number(formats[kind])
        if kind == 8:
            return string()
        if kind == 10:
            result = {}
            while (child := number("B")) != 0:
                name = string()
                result[name] = value(child)
            return result
        if kind == 9:
            child, count = number("B"), number("i")
            if not 0 <= count <= 100000:
                raise ValueError("Invalid NBT list length")
            return [value(child) for _ in range(count)]
        if kind in (7, 11, 12):
            count = number("i")
            if not 0 <= count <= 100000:
                raise ValueError("Invalid NBT array length")
            return [number({7: "b", 11: "i", 12: "q"}[kind]) for _ in range(count)]
        raise ValueError(f"Unexpected NBT type {kind}")

    root_kind = number("B")
    string()
    root = value(root_kind)
    if stream.read(1):
        raise ValueError("Unexpected trailing NBT data")
    return root, hashlib.sha256(compressed).hexdigest()


def summarize(root, digest):
    data = root["Data"]
    player = data["Player"]
    inventory = [
        {
            "slot": int(item["Slot"]),
            "id": str(item["id"]),
            "count": int(item.get("count", item.get("Count", 0))),
        }
        for item in player["Inventory"]
    ]
    inventory.sort(key=lambda item: item["slot"])
    recipes = [str(recipe) for recipe in player["recipeBook"]["recipes"]]
    return {
        "sha256_level_dat": digest,
        "level_name": str(data["LevelName"]),
        "game_type": int(data["GameType"]),
        "allow_commands": bool(data["allowCommands"]),
        "difficulty": int(data["Difficulty"]),
        "seed": int(data["WorldGenSettings"]["seed"]),
        "player_position": [float(value) for value in player["Pos"]],
        "inventory": inventory,
        "known_recipe_count": len(recipes),
        "fourthdimension_recipes": sorted(
            recipe for recipe in recipes if recipe.startswith("fourthdimension:")
        ),
    }


if __name__ == "__main__":
    if len(sys.argv) != 2:
        raise SystemExit("Usage: inspect-player-save.py /path/to/stopped/world/level.dat")
    root, digest = read_nbt(sys.argv[1])
    json.dump(summarize(root, digest), sys.stdout, indent=2)
    print()
