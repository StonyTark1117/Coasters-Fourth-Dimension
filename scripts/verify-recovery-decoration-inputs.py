#!/usr/bin/env python3
"""Check a same-route native export's decoration sampling and transforms, not clearance.

This is a structural/numerical check of supplied data. It cannot establish that a
JVM produced the input, that a model rendered, or that a ride passed acceptance.
"""
import argparse
import hashlib
import json
import math
from pathlib import Path
import re
import struct

ROOT = Path(__file__).resolve().parents[1]
SOURCES = (
    "src/main/java/dev/fourthdimension/gametest/RecoveryRouteFrames.java",
    "src/main/java/dev/fourthdimension/client/integration/TrackStylesRenderer.java",
    "src/main/java/dev/fourthdimension/physics/ArrowDecorationPlacement.java",
    "src/main/java/dev/fourthdimension/physics/ModelFrame.java",
    "src/main/java/dev/fourthdimension/contract/Family.java",
    "src/main/java/dev/fourthdimension/track/TrackDesign.java",
    "src/main/java/dev/fourthdimension/physics/RotationProfile.java",
    "src/main/java/dev/fourthdimension/track/TrackGeometry.java",
)


# Stable persisted Family IDs and TrackDesign names in the pinned five-family scope.
DESIGNS = {
    "ARROW": ("arrow_four_rail_v1", "arrow_controlled"),
    "LEGACY": ("zacspin_legacy_v1", "zacspin_legacy"),
    "FREE_SPIN": ("sns_magnetic_v1", "sns_free_spin"),
    "REVISED": ("zacspin_revised_v1", "zacspin_revised"),
    "PAX": ("pax_shuttle_v1", "pax_shuttle"),
}


def require(condition, message):
    if not condition:
        raise ValueError(message)


def number(value):
    return type(value) in (float, int) and math.isfinite(value)


def vector(value, size):
    return isinstance(value, list) and len(value) == size and all(number(x) for x in value)


def f32(value):
    return struct.unpack("f", struct.pack("f", value))[0]


def close(a, b, tolerance=1e-6):
    return number(a) and abs(a - b) <= tolerance


def unit(v):
    norm = math.sqrt(sum(x*x for x in v))
    require(norm > 1e-12, "degenerate frame")
    return [x/norm for x in v]


def cross(a, b):
    return [a[1]*b[2]-a[2]*b[1], a[2]*b[0]-a[0]*b[2], a[0]*b[1]-a[1]*b[0]]


def constants():
    source = (ROOT / SOURCES[2]).read_text()
    def read(name):
        match = re.search(r"\b" + name + r"\s*=\s*([-.0-9]+)f", source)
        require(match is not None, f"cannot inspect placement constant {name}")
        return f32(float(match[1]))
    return read("TIE_SCALE_X"), read("VERTICAL_OFFSET")


def angle_at(keys, distance):
    if distance < keys[0]["distance"]:
        return keys[0]["angle"]
    for a, b in zip(keys, keys[1:]):
        if distance <= b["distance"]:
            h = b["distance"] - a["distance"]
            t = (distance - a["distance"]) / h
            return ((2*t**3-3*t*t+1)*a["angle"] + (-2*t**3+3*t*t)*b["angle"]
                    + (t**3-2*t*t+t)*h*a["slope"] + (t**3-t*t)*h*b["slope"])
    return keys[-1]["angle"]


def check_piece(piece, distance, scale, keys, render_keys, vertical, spine):
    require(close(piece["distance"], distance, 1e-9), "decoration distance/grid mismatch")
    require(number(piece["parameter"]) and 0 <= piece["parameter"] <= 1, "invalid native parameter")
    for name in ("center", "right", "up", "forward", "scale"):
        require(vector(piece[name], 3), f"invalid {name}")
    for name in ("right", "up", "forward"):
        require(close(sum(x*x for x in piece[name]), 1, 1e-5), f"non-unit {name}")
    require(all(close(a, b) for a, b in zip(piece["scale"], scale)), "wrong piece scale")
    require(close(piece["profileAngle"], angle_at(keys, distance)), "profile angle mismatch")
    require(close(piece["renderProfileAngle"], angle_at(render_keys, distance)), "renderer profile angle mismatch")
    matrix = piece["modelTransform"]
    require(vector(matrix, 16), "invalid model transform")
    forward = unit(piece["forward"])
    right = unit(cross(piece["up"], forward))
    up = unit(cross(forward, right))
    # ModelFrame deliberately derives model-right from up x forward; native
    # basis.right has its own convention and must not substitute for it.
    columns = [[f32(v)*s for v in axis] for axis, s in zip((right, up, forward), scale)]
    origin = [f32(c) + f32(u)*vertical for c, u in zip(piece["center"], up)]
    if not spine:
        origin = [c - .5*r - .25*f for c, r, f in zip(origin, columns[0], columns[2])]
    expected = [value for col in columns for value in (*col, 0)] + origin + [1]
    require(all(close(a, b, 2e-4) for a, b in zip(matrix, expected)), "model origin/basis/scale mismatch")


def verify(routes):
    require(isinstance(routes, list) and routes, "empty routes")
    tie_scale, vertical = constants()
    counts = {"routes": len(routes), "sections": 0, "spines": 0, "ties": 0}
    ids = set()
    for route in routes:
        require(type(route["carCount"]) is int and route["carCount"] > 0, "invalid car count")
        require(route["family"] in DESIGNS, "unknown route design")
        variant, family = DESIGNS[route["family"]]
        require(route["variant"] == variant, "route design/variant mismatch")
        require(isinstance(route["chassis"], list) and len(route["chassis"]) == route["carCount"]
                and len(set(route["chassis"])) == route["carCount"]
                and all(isinstance(car, str) and car for car in route["chassis"]), "missing chassis identity")
        require(isinstance(route["dimension"], str) and route["dimension"]
                and type(route["gameTime"]) is int, "missing observation identity")
        require(route["sections"], "empty sections")
        for section in route["sections"]:
            rail = section["rail"]
            require(isinstance(rail, str) and rail and rail not in ids, "missing/duplicate rail")
            ids.add(rail)
            length = section["length"]
            require(number(length) and length > 0, "invalid section length")
            require(section["family"] == family and section["variant"] == variant, "rail identity mismatch")
            require(re.fullmatch(r"-?[0-9]+", section["geometryHash"]) is not None, "missing geometry identity")
            require(type(section["revision"]) is int and type(section["valid"]) is bool, "invalid rail state")
            require(isinstance(section["appearance"]["id"], str) and section["appearance"]["id"], "missing appearance")
            require(type(section["appearance"]["tint"]) is int, "missing appearance tint")
            keys = section["profile"]
            require(isinstance(keys, list) and len(keys) >= 2, "missing profile")
            require(all(all(number(k[n]) for n in ("distance", "angle", "slope")) for k in keys), "invalid profile")
            require(all(a["distance"] < b["distance"] for a, b in zip(keys, keys[1:])), "unordered profile")
            require(type(section["canonical"]) is bool, "missing native-edge direction")
            for name in ("railFrom", "railTo", "edgeFrom", "edgeTo"):
                require(vector(section[name], 3) and all(type(x) is int for x in section[name]), "invalid endpoint")
            expected_endpoints = ([section["railFrom"], section["railTo"]] if section["canonical"]
                                  else [section["railTo"], section["railFrom"]])
            require([section["edgeFrom"], section["edgeTo"]] == expected_endpoints, "native-edge direction mismatch")
            render_keys = section["renderProfile"]
            expected_keys = keys if section["canonical"] else [
                dict(distance=keys[-1]["distance"]-key["distance"], angle=key["angle"], slope=-key["slope"])
                for key in reversed(keys)]
            require(len(render_keys) == len(expected_keys) and all(
                all(close(got[name], expected[name], 1e-9) for name in ("distance", "angle", "slope"))
                for got, expected in zip(render_keys, expected_keys)), "renderer profile direction mismatch")
            original_samples = max(8, math.ceil(length*4))
            require(len(section["frames"]) == original_samples + 1, "original frame grid changed")
            for index, frame in enumerate(section["frames"]):
                distance = length*index/original_samples
                require(close(frame["distance"], distance, 1e-9), "original frame distance/grid mismatch")
                for name in ("center", "right", "up", "forward"):
                    require(vector(frame[name], 3), f"invalid original frame {name}")
                require(close(frame["profileAngle"], angle_at(keys, distance)), "original profile angle mismatch")
                require(close(frame["renderProfileAngle"], angle_at(render_keys, distance)), "original renderer profile angle mismatch")
            data = section["decorationInputs"]
            require(data["schema"] == 1 and data["matrixLayout"] == "column-major-model-to-world", "unknown decoration schema")
            require(data["scope"] == "potential-renderer-inputs-only; no model availability or clearance claim", "missing evidence scope")
            segments = max(1, math.ceil(length*2))
            step = length/segments
            distances = [(i+.5)*step for i in range(segments)]
            ties = []
            d = 0
            while d <= length + 1e-6:
                ties.append(min(d, length))
                d += 2
            for name, required, scale, spine in (
                ("spines", distances, [1, 1, f32(step)], True),
                ("ties", ties, [tie_scale, 1, 1], False),
            ):
                require(len(data[name]) == len(required), f"missing/extra {name}")
                for piece, distance in zip(data[name], required):
                    check_piece(piece, distance, scale, keys, render_keys, vertical, spine)
                counts[name] += len(required)
            counts["sections"] += 1
    return counts


def verify_trace(routes, path):
    """Pair identifiers only; no swept-volume or gameplay assertion."""
    expected = {route["variant"]: set(route["chassis"]) for route in routes}
    require(len(expected) == len(routes), "ambiguous duplicate route variant")
    samples = {key: 0 for key in expected}
    completed = set()
    bodies = set()
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for raw in stream:
            digest.update(raw)
            row = json.loads(raw)
            if row["type"] == "body" and row["role"] == "chassis":
                require(row["id"] == row["car"], "chassis body/car mismatch")
                bodies.add(row["id"])
            elif row["type"] == "sample" and row["variant"] in expected:
                variant = row["variant"]
                require(expected[variant] <= {pose["id"] for pose in row["poses"]},
                        "trace has different/missing route chassis")
                samples[variant] += 1
            elif row["type"] == "complete" and row["variant"] in expected:
                require(row["cars"] == len(expected[row["variant"]]), "trace completion car count mismatch")
                completed.add(row["variant"])
    require(all(cars <= bodies for cars in expected.values()), "missing chassis body definitions")
    require(all(samples.values()) and completed == set(expected), "trace lacks samples/completion")
    return {"sha256": digest.hexdigest(), "samples_by_variant": samples,
            "scope": "Recorded chassis pairing only; completion marker is not test acceptance."}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("frames", type=Path)
    parser.add_argument("--mesh-trace", type=Path, help="Optionally require matching recorded chassis IDs")
    args = parser.parse_args()
    try:
        payload = args.frames.read_bytes()
        routes = json.loads(payload)
        counts = verify(routes)
        pairing = verify_trace(routes, args.mesh_trace) if args.mesh_trace else None
    except (OSError, ValueError, KeyError, TypeError, OverflowError) as error:
        parser.exit(1, f"FAIL: {error}\n")
    print(json.dumps({
        "result": "structural-and-transform-check-pass",
        "scope": "No clearance, model visibility, native execution or release acceptance inferred.",
        "input_sha256": hashlib.sha256(payload).hexdigest(),
        "trace_pairing": pairing,
        "inspected_local_source_sha256": {name: hashlib.sha256((ROOT/name).read_bytes()).hexdigest() for name in SOURCES},
        **counts,
    }, indent=2))


if __name__ == "__main__":
    main()
