#!/usr/bin/env python3
"""Check exported native recovery route geometry independently of its towing metrics."""
import json
import math
import sys
from pathlib import Path


def dot(a, b):
    return sum(x * y for x, y in zip(a, b))


def distance(a, b):
    return math.sqrt(sum((x - y) ** 2 for x, y in zip(a, b)))


def bank(frame):
    return math.degrees(math.acos(max(-1, min(1, frame["up"][1]))))


def verify(path, train_routes=False):
    routes = json.loads(Path(path).read_text())
    expected = {"ARROW", "PAX"} if train_routes else {"ARROW", "LEGACY", "FREE_SPIN", "REVISED", "PAX"}
    assert {r["family"] for r in routes} == expected
    assert len(routes) == len(expected)
    results = []
    for route in routes:
        sections = route["sections"]
        assert len(sections) == 5
        if train_routes:
            assert route["carCount"] == (2 if route["family"] == "ARROW" else 4)
            assert abs(sections[0]["length"]-48) < 1e-6
            assert abs(sections[4]["length"]-48) < 1e-6
        assert len({s["rail"] for s in sections}) == 5
        maximum_gap = maximum_frame_gap = maximum_bank = maximum_grade = 0
        for index, section in enumerate(sections):
            frames = section["frames"]
            assert len(frames) >= math.ceil(section["length"] * 4) + 1
            assert abs(frames[0]["distance"]) < 1e-9
            assert abs(frames[-1]["distance"] - section["length"]) < 1e-9
            assert all(a["distance"] < b["distance"] for a, b in zip(frames, frames[1:]))
            for frame in frames:
                for field in ["center", "right", "up", "forward"]:
                    assert len(frame[field]) == 3 and all(math.isfinite(v) for v in frame[field])
                assert abs(frame["center"][0]) <= 1024 and abs(frame["center"][2]) <= 1024
                for field in ["right", "up", "forward"]:
                    assert abs(dot(frame[field], frame[field]) - 1) < 1e-6
                for first, second in [("right", "up"), ("up", "forward"), ("forward", "right")]:
                    assert abs(dot(frame[first], frame[second])) < 1e-6
                maximum_bank = max(maximum_bank, bank(frame))
                maximum_grade = max(maximum_grade, abs(frame["forward"][1]))
                if index == 2:
                    assert 29.999 < bank(frame) < 30.001
                if index in [0, 4]:
                    assert bank(frame) < .001
            if index:
                end = sections[index - 1]["frames"][-1]
                start = frames[0]
                gap = distance(end["center"], start["center"])
                frame_gap = max(distance(end[f], start[f]) for f in ["right", "up", "forward"])
                assert gap < 1e-6 and frame_gap < 1e-3
                maximum_gap = max(maximum_gap, gap)
                maximum_frame_gap = max(maximum_frame_gap, frame_gap)
        assert dot(sections[0]["frames"][0]["forward"], [0, 0, 1]) > .999999
        assert dot(sections[4]["frames"][-1]["forward"], [1, 0, 0]) > .999999
        angles = [f["profileAngle"] for f in sections[2]["frames"]]
        assert angles[0] == angles[-1] == 0
        if route["family"] == "ARROW":
            assert max(angles) > 6.28
        else:
            assert all(a == 0 for a in angles)
        results.append({"family": route["family"], "length": sum(s["length"] for s in sections),
                        "maxJointGap": maximum_gap, "maxFrameGap": maximum_frame_gap,
                        "maxBankDegrees": maximum_bank, "maxAbsTangentY": maximum_grade,
                        "sustainedBankLength": sections[2]["length"], "profilePeak": max(angles)})
    return {"nativeRouteGeometryPassed": True, "routes": results,
            "scope": "Native centerline/frame/profile geometry only; no mesh, rider, collision or rendered clearance claim."}


if __name__ == "__main__":
    print(json.dumps(verify(sys.argv[1], train_routes="--trains" in sys.argv[2:]), indent=2))
