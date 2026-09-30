#!/usr/bin/env python3
"""Read an addon SavedData .dat as JSON for restart evidence; never edits the save."""
import gzip
import io
import json
import struct
import sys

source = gzip.open(sys.argv[1], "rb")
raw = source.read(16 * 1024 * 1024 + 1)
source.close()
if len(raw) > 16 * 1024 * 1024:
    raise ValueError("Save exceeds inspection limit")
stream = io.BytesIO(raw)


def number(fmt):
    return struct.unpack(">" + fmt, stream.read(struct.calcsize(">" + fmt)))[0]


def string():
    return stream.read(number("H")).decode("utf-8")


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
            raise ValueError("Invalid list length")
        return [value(child) for _ in range(count)]
    if kind in (7, 11, 12):
        count = number("i")
        if not 0 <= count <= 100000:
            raise ValueError("Invalid array length")
        return [number({7: "b", 11: "i", 12: "q"}[kind]) for _ in range(count)]
    raise ValueError(f"Unexpected NBT type {kind}")


root = number("B")
string()
json.dump(value(root), sys.stdout, indent=2)
print()
