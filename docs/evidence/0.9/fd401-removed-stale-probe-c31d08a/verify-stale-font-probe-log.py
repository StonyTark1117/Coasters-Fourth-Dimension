#!/usr/bin/env python3
"""Check the retained removed-custom-font client log's event order and identities."""

import argparse
import json
import re
from pathlib import Path


def event(lines, pattern):
    matches = [(index, match) for index, line in enumerate(lines)
               if (match := re.search(pattern, line))]
    if len(matches) != 1:
        raise ValueError(f"Expected exactly one {pattern!r}; found {len(matches)}")
    return matches[0]


def verify(log):
    lines = Path(log).read_text().splitlines()
    started, _ = event(lines, r"FD_FONT_APPLY_END generation=1 newFontSets=8\b")
    reload_start, _ = event(lines, r"FD_FONT_APPLY_START generation=2 oldFontSets=8\b")
    closed, close = event(
        lines, r"FD_FONT_SET_CLOSE generation=2 font=fd_font_probe:transient identity=(\d+)")
    applied, _ = event(lines, r"FD_FONT_APPLY_END generation=2 newFontSets=7\b")
    redirected, redirect = event(
        lines, r"FD_FONT_STALE_SET_REDIRECT generation=2 font=fd_font_probe:transient "
        r"closedIdentity=(\d+) liveIdentity=(\d+) missingFallback=true\b")
    probed, probe = event(
        lines, r"FD_FONT_REMOVED_STALE_PROBE passed generation=2 font=fd_font_probe:transient "
        r"closedIdentity=(\d+) missingIdentity=(\d+) codepoint=U\+0041\b")
    stopped, _ = event(lines, r"\[net\.minecraft\.client\.Minecraft/\]: Stopping!$")
    if not started < reload_start < closed < applied < redirected < probed < stopped:
        raise ValueError("Font load, close, redirect, probe and shutdown occurred out of order")
    if close[1] != redirect[1] or close[1] != probe[1] or redirect[2] != probe[2]:
        raise ValueError("Closed and live missing-font identities did not match")
    for marker in ("FD_FONT_IMAGE_UNALLOCATED", "FD_FONT_CLOSED_SET_LOOKUP", "Image is not allocated"):
        if any(marker in line for line in lines):
            raise ValueError(f"Failure marker present: {marker}")
    return {"passed": True, "font": "fd_font_probe:transient", "generation": 2,
            "closedIdentity": close[1], "missingIdentity": redirect[2],
            "eventLineNumbers": {"initialApply": started + 1, "reloadStart": reload_start + 1,
                                 "closed": closed + 1, "newApply": applied + 1,
                                 "redirect": redirected + 1, "probe": probed + 1,
                                 "normalStop": stopped + 1}}


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("log", type=Path)
    args = parser.parse_args()
    try:
        print(json.dumps(verify(args.log), indent=2))
    except ValueError as error:
        parser.error(str(error))
