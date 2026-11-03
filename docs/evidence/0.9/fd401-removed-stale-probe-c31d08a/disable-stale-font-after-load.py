#!/usr/bin/env python3
"""Disable a private test-pack font after the client has loaded it once."""

import argparse
import datetime
import hashlib
import json
import time
from pathlib import Path


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("log", type=Path)
    parser.add_argument("font", type=Path)
    parser.add_argument("--timeout", type=int, default=120)
    args = parser.parse_args()
    if args.font.name != "transient.json" or args.font.parent.name != "font":
        parser.error("Refusing to rename anything except a transient.json font file")
    if not args.font.is_file():
        parser.error("The test font file is absent before launch")
    start = time.time_ns()
    deadline = time.monotonic() + args.timeout
    marker = "FD_FONT_APPLY_END generation=1 newFontSets=8"
    while time.monotonic() < deadline:
        if args.log.is_file() and args.log.stat().st_mtime_ns > start:
            lines = args.log.read_text(errors="replace").splitlines()
            found = next((line for line in lines if marker in line), None)
            if found is not None:
                before = digest(args.font)
                target = args.font.with_name(args.font.name + ".disabled")
                if target.exists():
                    parser.error("Disabled target already exists")
                args.font.rename(target)
                print(json.dumps({"loadedMarker": found, "renamedUtc": datetime.datetime.now(datetime.timezone.utc).isoformat(),
                                  "fontSha256": before, "disabledPath": str(target),
                                  "disabledSha256": digest(target)}, indent=2), flush=True)
                break
        time.sleep(.05)
    else:
        parser.error("Timed out before the first loaded custom-font generation")
