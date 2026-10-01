#!/usr/bin/env python3
"""Report aggregate OBJ bounds from a user-supplied Track Styles JAR.

This does not extract or redistribute assets, transform them into ride coordinates,
or certify running-gear/clearance compatibility. It supplies reproducible raw-model
measurements for the optional rendering adapter.
"""
import hashlib
import json
from pathlib import Path
import sys
import zipfile

source = Path(sys.argv[1])
styles = set(sys.argv[2:] or ("arrow_4d", "arrow_4d_dbl_spine", "intamin_bb"))
result = {"sha256": hashlib.sha256(source.read_bytes()).hexdigest(), "space": "raw OBJ coordinates", "models": []}
with zipfile.ZipFile(source) as archive:
    for name in sorted(archive.namelist()):
        parts = name.split("/")
        if not name.endswith(".obj") or len(parts) < 7 or parts[4] != "track" or parts[5] not in styles:
            continue
        vertices = []
        for line in archive.read(name).decode("utf-8").splitlines():
            if line.startswith("v "):
                vertices.append(tuple(map(float, line.split()[1:4])))
        if vertices:
            result["models"].append({"resource": name, "vertices": len(vertices), "bounds": {
                axis: [min(v[i] for v in vertices), max(v[i] for v in vertices)] for i, axis in enumerate("xyz")
            }})
assert result["models"], "No candidate model resources found"
json.dump(result, sys.stdout, indent=2)
print()
