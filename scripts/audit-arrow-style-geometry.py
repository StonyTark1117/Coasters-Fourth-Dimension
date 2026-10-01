#!/usr/bin/env python3
"""Conservatively check installed Arrow decoration against a recorded native circuit.

Inputs are our exported native frames and the separately installed pinned provider
JAR. External vertices stay in memory. A convex volume covers each rendered
control-rail segment over the entire allowed stroke, including its square tube.
Projection and separating-axis gaps are lower bounds on actual surface distance.
This certifies the recorded circuit only, not arbitrary tracks or resource packs.
"""
import argparse
import hashlib
import importlib.util
import itertools
import json
import math
from pathlib import Path
import re
import struct
import zipfile

spec = importlib.util.spec_from_file_location("stroke", Path(__file__).with_name("audit-arrow-style-stroke.py"))
stroke = importlib.util.module_from_spec(spec)
spec.loader.exec_module(stroke)


def add(a, b):
    return tuple(x + y for x, y in zip(a, b))


def sub(a, b):
    return tuple(x - y for x, y in zip(a, b))


def mul(a, s):
    return tuple(x * s for x in a)


def dot(a, b):
    return sum(x * y for x, y in zip(a, b))


def cross3(a, b):
    return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])


def unit(a):
    size = math.sqrt(dot(a, a))
    return mul(a, 1 / size) if size > 1e-12 else None


def bounds(points):
    return tuple(min(p[i] for p in points) for i in range(3)), tuple(max(p[i] for p in points) for i in range(3))


def box_gap(a, b):
    return math.sqrt(sum(max(0, a[0][i] - b[1][i], b[0][i] - a[1][i]) ** 2 for i in range(3)))


def hull(points):
    points = sorted(set((p[0], p[1]) for p in points))
    halves = []
    for ordered in (points, points[::-1]):
        half = []
        for p in ordered:
            while len(half) >= 2 and stroke.cross(half[-2], half[-1], p) <= 0:
                half.pop()
            half.append(p)
        halves.append(half[:-1])
    return halves[0] + halves[1]


def projected_gap(poly, tri):
    if len(poly) < 3:
        raise ValueError("Degenerate control cross-section projection")
    if any(all(stroke.cross(a, b, p) >= -1e-12 for a, b in zip(poly, poly[1:] + poly[:1])) for p in tri):
        return 0.0
    return min(stroke.stroke_triangle(a, b, tri) for a, b in zip(poly, poly[1:] + poly[:1]))


def separating_gap(volume, triangle):
    # Any separating direction proves a lower distance bound. All pair/triple
    # directions include the convex hull's edges and face normals; redundant
    # directions are harmless. A zero result means uncertified, not safe.
    edges = [sub(b, a) for a, b in itertools.combinations(volume, 2)]
    tri_edges = [sub(triangle[(i + 1) % 3], triangle[i]) for i in range(3)]
    axes = [(1, 0, 0), (0, 1, 0), (0, 0, 1), cross3(tri_edges[0], tri_edges[1])]
    axes.extend(cross3(sub(b, a), sub(c, a)) for a, b, c in itertools.combinations(volume, 3))
    axes.extend(cross3(a, b) for a in edges for b in tri_edges)
    best = 0.0
    for axis in axes:
        axis = unit(axis)
        if axis is None:
            continue
        a = [dot(axis, p) for p in volume]
        b = [dot(axis, p) for p in triangle]
        best = max(best, min(a) - max(b), min(b) - max(a))
    return best


def placement_config(placement):
    if placement == "legacy":
        return 1.0, 0.0, None
    source = Path(__file__).resolve().parents[1] / "src/main/java/dev/fourthdimension/physics/ArrowDecorationPlacement.java"
    content = source.read_text()
    def constant(name):
        match = re.search(r"public static final float " + name + r" = ([\d.\-]+)f;", content)
        if match is None:
            raise ValueError("Unrecognized production decoration placement: " + name)
        return struct.unpack("f", struct.pack("f", float(match[1])))[0]
    return constant("TIE_SCALE_X"), constant("VERTICAL_OFFSET"), hashlib.sha256(source.read_bytes()).hexdigest()


def model(source, component, placement="legacy"):
    tie_scale, vertical, _ = placement_config(placement)
    vertices, faces = [], []
    for line in source.splitlines():
        if line.startswith("v "):
            p = tuple(map(float, line.split()[1:4]))
            if component == "tie":
                p = sub(p, (.5, 0, .25))
                p = (p[0] * tie_scale, p[1], p[2])
            p = (p[0], p[1] + vertical, p[2])
            vertices.append(p)
        elif line.startswith("f "):
            face = [int(x.split("/")[0]) for x in line.split()[1:]]
            faces.append([vertices[i - 1 if i > 0 else i] for i in face])
    return [(f[0], f[i], f[i + 1]) for f in faces for i in range(1, len(f) - 1)]


def control_segments(sections, low_turn, high_turn):
    result = []
    for section_index, section in enumerate(sections):
        for segment_index, (a, b) in enumerate(zip(section["rails"], section["rails"][1:])):
            for side in (-1, 1):
                corners = []
                for frame in (a, b):
                    for x in (side * 1.35 - .09, side * 1.35 + .09):
                        for y in (-.85 + low_turn * .125 - .09, -.85 + high_turn * .125 + .09):
                            corners.append(add(frame["center"], add(mul(frame["right"], x), mul(frame["up"], y))))
                result.append((corners, bounds(corners), {"section": section_index, "segment": segment_index, "side": side}))
    return result


def local(frame, point):
    p = sub(point, frame["center"])
    right = unit(cross3(frame["up"], frame["forward"]))
    return dot(p, right), dot(p, frame["up"]), dot(p, frame["forward"])


def world(frame, point):
    right = unit(cross3(frame["up"], frame["forward"]))
    return add(frame["center"], add(mul(right, point[0]), add(mul(frame["up"], point[1]), mul(frame["forward"], point[2]))))


def measure(triangles, sections, component, controls, required):
    min_gap, pieces, near_pairs, exact_pairs = math.inf, 0, 0, 0
    closest = None
    vertices = [p for tri in triangles for p in tri]
    model_bounds = bounds(vertices)
    for section_index, section in enumerate(sections):
        frames = section["spines" if component == "center_beam" else "ties"]
        scale = section["length"] / len(frames) if component == "center_beam" else 1
        scaled = [[(p[0], p[1], p[2] * scale) for p in tri] for tri in triangles]
        for frame in frames:
            pieces += 1
            piece_bounds = bounds([world(frame, (x, y, z * scale)) for x in (model_bounds[0][0], model_bounds[1][0])
                                   for y in (model_bounds[0][1], model_bounds[1][1])
                                   for z in (model_bounds[0][2], model_bounds[1][2])])
            for corners, control_bounds, control_id in controls:
                coarse = box_gap(piece_bounds, control_bounds)
                if coarse >= min_gap:
                    continue
                # A distant box is already a valid lower bound. Only refine near pairs.
                if coarse > required * 4:
                    min_gap = min(min_gap, coarse)
                    continue
                near_pairs += 1
                volume = [local(frame, p) for p in corners]
                polygon = hull(volume)
                low_z, high_z = min(p[2] for p in volume), max(p[2] for p in volume)
                for tri in scaled:
                    planar = projected_gap(polygon, [(p[0], p[1]) for p in tri])
                    dz = max(0, low_z - max(p[2] for p in tri), min(p[2] for p in tri) - high_z)
                    gap = math.hypot(planar, dz)
                    if gap < required:
                        exact_pairs += 1
                        gap = max(gap, separating_gap(volume, tri))
                    if gap < min_gap:
                        min_gap = gap
                        closest = {"decorationSection": section_index, "decorationDistance": frame["distance"], "control": control_id}
    return {"pieces": pieces, "trianglesPerModel": len(triangles), "nearSegmentPairs": near_pairs,
            "separatingAxisRefinements": exact_pairs, "minimumConservativeGap": min_gap,
            "requiredGap": required, "closestRefinedPair": closest, "passes": min_gap >= required}


def validate_frames(sections):
    if len(sections) != 8:
        raise ValueError("Expected the complete eight-section native bank fixture")
    center_error, up_error, tangent_error = 0, 0, 0
    for i, section in enumerate(sections):
        length = section["length"]
        if not math.isfinite(length) or length <= 0:
            raise ValueError("Invalid section length")
        counts = {"rails": max(8, math.ceil(length * 4)) + 1,
                  "spines": max(1, math.ceil(length * 2)), "ties": math.floor((length + 1e-6) / 2) + 1}
        for kind, count in counts.items():
            if len(section[kind]) != count:
                raise ValueError("Incomplete native render sampling")
            for index, frame in enumerate(section[kind]):
                expected_distance = (index * length / (count - 1) if kind == "rails" else
                                     (index + .5) * length / count if kind == "spines" else min(index * 2, length))
                if not math.isfinite(frame["distance"]) or abs(frame["distance"] - expected_distance) > 1e-6:
                    raise ValueError("Frames do not match native renderer distances")
                for name in ("center", "right", "up", "forward"):
                    if len(frame[name]) != 3 or not all(math.isfinite(x) for x in frame[name]):
                        raise ValueError("Non-finite frame vector")
                if any(abs(dot(frame[k], frame[k]) - 1) > 1e-6 for k in ("right", "up", "forward")):
                    raise ValueError("Frame axes are not unit vectors")
                if any(abs(dot(frame[a], frame[b])) > 1e-6 for a, b in itertools.combinations(("right", "up", "forward"), 2)):
                    raise ValueError("Frame axes are not orthogonal")
        a, b = section["rails"][-1], sections[(i + 1) % len(sections)]["rails"][0]
        center_error = max(center_error, math.dist(a["center"], b["center"]))
        up_error = max(up_error, math.dist(a["up"], b["up"]))
        tangent_error = max(tangent_error, math.dist(a["forward"], b["forward"]))
    if center_error >= 1e-6 or up_error >= 1e-3 or tangent_error >= 1e-3:
        raise ValueError("Discontinuous native fixture; repair it before evaluating decoration")
    return {"maximumCenterError": center_error, "maximumUpError": up_error, "maximumTangentError": tangent_error}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("jar", type=Path)
    parser.add_argument("frames", type=Path)
    parser.add_argument("--margin", type=float, default=.01)
    parser.add_argument("--low-turn", type=float, default=-4)
    parser.add_argument("--high-turn", type=float, default=2)
    parser.add_argument("--placement", choices=("legacy", "wheel-clearance-v2"), default="wheel-clearance-v2")
    args = parser.parse_args()
    if not 0 < args.margin <= .05:
        parser.error("Margin must be finite and within (0, .05]")
    digest = hashlib.sha256(args.jar.read_bytes()).hexdigest()
    if digest != stroke.PIN:
        parser.error("Requires the pinned Track Styles 1.2.0 artifact")
    sections = json.loads(args.frames.read_text())
    if not -4 <= args.low_turn <= args.high_turn <= 2:
        parser.error("Turn interval must be within [-4, +2]")
    joint_errors = validate_frames(sections)
    controls = control_segments(sections, args.low_turn, args.high_turn)
    # Independently known geometry validates intersection and separated distances.
    cube = list(itertools.product((-1, 1), repeat=3))
    assert separating_gap(cube, [(2, 0, 0), (2, 1, 0), (2, 0, 1)]) == 1
    assert separating_gap(cube, [(0, 0, 0), (0, 1, 0), (0, 0, 1)]) == 0
    assert projected_gap(hull(cube), [(2, 0), (2, 1), (3, 0)]) == 1
    assert projected_gap(hull(cube), [(0, 0), (0, .5), (.5, 0)]) == 0
    results = []
    with zipfile.ZipFile(args.jar) as archive:
        for style in ("arrow_4d", "arrow_4d_dbl_spine"):
            for component in ("center_beam", "tie"):
                resource = f"assets/coasterssimulatedextratypes/models/block/track/{style}/{style}_{component}.obj"
                triangles = model(archive.read(resource).decode(), component, args.placement)
                results.append({"resource": resource, **measure(triangles, sections, component, controls, args.margin)})
    output = {"providerSha256": digest, "nativeFramesSha256": hashlib.sha256(args.frames.read_bytes()).hexdigest(),
              "placement": args.placement, "placementSourceSha256": placement_config(args.placement)[2],
              "jointErrors": joint_errors, "sections": len(sections), "controlSegments": len(controls), "turnInterval": [args.low_turn, args.high_turn],
              "method": "convex volume of rendered square-tube segment over continuous stroke; conservative projection/SAT distance bounds",
              "scope": "recorded native circuit and default provider OBJ meshes only; excludes riders, chassis, wheels, linked vehicles and resource overrides",
              "models": results}
    print(json.dumps(output, indent=2))
    if not all(r["passes"] for r in results):
        raise SystemExit("Recorded circuit has an uncertified or insufficient decoration/control-rail gap")


if __name__ == "__main__":
    main()
