#!/usr/bin/env python3
"""Check the packaged interactive font reload checkpoint."""

import hashlib
from pathlib import Path
import re
import struct
import subprocess

root = Path(__file__).resolve().parent
for line in (root / "SHA256SUMS").read_text().splitlines():
    expected, name = line.split("  ", 1)
    assert hashlib.sha256((root / name).read_bytes()).hexdigest() == expected, name

log = subprocess.check_output(["zstd", "-dc", str(root / "client.log.zst")], text=True)
for generation in (1, 2, 3, 4):
    assert f"FD_FONT_APPLY_START generation={generation}" in log
    assert f"FD_FONT_APPLY_END generation={generation}" in log
assert len(re.findall(r"FD_FONT_APPLY_START generation=", log)) == 4
assert len(re.findall(r"FD_FONT_APPLY_END generation=", log)) == 4
for generation in (2, 3):
    assert f"FD_FONT_STALE_SET_REDIRECT generation={generation} font=minecraft:default" in log
for absent in ("FD_FONT_IMAGE_UNALLOCATED", "FD_FONT_CLOSED_SET_LOOKUP",
               "Image is not allocated", "OutOfMemoryError", "BUILD FAILED"):
    assert absent not in log, absent
assert "Saving worlds" in log and "BUILD SUCCESSFUL in 2m 30s" in log

for path in root.glob("*.png"):
    data = path.read_bytes()
    assert data[:8] == b"\x89PNG\r\n\x1a\n"
    assert struct.unpack(">II", data[16:24]) == (854, 480), path.name
assert (root / "world.tar.zst").stat().st_size > 1_000_000
print("FD401 interactive font checkpoint: verified")
