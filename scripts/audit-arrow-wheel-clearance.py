#!/usr/bin/env python3
"""Audit default installed decoration against native wheels on a level straight.

Pinned provider meshes remain in memory. Each wheel-model cuboid is enclosed in
its full-spin cylinder. Projecting that cylinder and every decoration triangle
onto XY proves clearance over continuous longitudinal travel and wheel phase.
This conservative check excludes banking, chassis, carriers, riders, neighboring
cars and resource overrides; those require their separate FD401 checks.
"""
import argparse
import hashlib
import importlib.util
import json
import math
from pathlib import Path
import re
import zipfile

spec = importlib.util.spec_from_file_location("geometry", Path(__file__).with_name("audit-arrow-style-geometry.py"))
geometry = importlib.util.module_from_spec(spec)
spec.loader.exec_module(geometry)
BASE_PIN = "f80b4c3cdf9fa6188804967147761b0373e150a6eadbdc53626e5ac9700b2049"
WHEEL_RESOURCE = "assets/simulatedcoasters/models/block/coaster_cart_wheel_axis.json"


def wheel_envelopes(source):
    elements = json.loads(source)["elements"]
    if len(elements) != 10:
        raise ValueError("Unexpected pinned wheel model")
    result = []
    for index, element in enumerate(elements):
        rotation = element.get("rotation", {})
        if rotation.get("angle", 0) != 0 and (
            rotation.get("axis") != "x" or rotation.get("rescale", False)
            or rotation.get("origin", [0, 0, 0])[1:] != [8, 8]
        ):
            raise ValueError("Wheel cuboid rotation does not preserve the audited spin axis")
        low, high = element["from"], element["to"]
        radius = max(math.hypot(y / 16 - .5, z / 16 - .5)
                     for y in (low[1], high[1]) for z in (low[2], high[2]))
        for row, center_y in (("main", .6), ("upstop", .05)):
            x0, x1 = low[0] / 16 - .5, high[0] / 16 - .5
            y0, y1 = center_y - radius, center_y + radius
            result.append({"element": index, "row": row, "radius": radius,
                           "polygon": [(x0, y0), (x1, y0), (x1, y1), (x0, y1)]})
    return result


def measure(triangles, envelopes, margin):
    best, closest = math.inf, None
    for envelope in envelopes:
        for index, triangle in enumerate(triangles):
            gap = geometry.projected_gap(envelope["polygon"], [(p[0], p[1]) for p in triangle])
            if gap < best:
                best = gap
                closest = {"element": envelope["element"], "row": envelope["row"], "triangle": index}
    if not math.isfinite(best):
        raise ValueError("Wheel-clearance component produced no nondegenerate projected triangles")
    return {"minimumConservativeGap": best, "requiredGap": margin, "closestPair": closest,
            "triangles": len(triangles), "passes": best >= margin}


def native_supports(placement):
    source = Path(__file__).resolve().parents[1] / "src/main/java/dev/fourthdimension/track/TrackDesign.java"
    content = source.read_text()
    def constant(name):
        match = re.search(r"public static final double " + name + r" = ([\d.\-]+);", content)
        if match is None:
            raise ValueError("Unrecognized production track support: " + name)
        return float(match[1])
    gauge, height = constant("HALF_GAUGE"), constant("MAIN_RAIL_HEIGHT")
    outboard = constant("RUNNING_OUTBOARD_HALF_GAUGE")
    underbrace = constant("RUNNING_UNDERBRACE_HEIGHT")
    spine = constant("SPINE_HEIGHT") + geometry.placement_config(placement)[1]
    crossbar = constant("RUNNING_CROSSBAR_RADIUS")
    # XY hulls enclose the renderer's octagonal tube cross-sections. Projecting
    # away Z covers every longitudinal phase of the front and rear wheel axes.
    def support(a, b, radius, vertical):
        # A vertical support needs lateral offsets; a horizontal beam needs
        # vertical offsets. Reversing these creates a collinear hull and would
        # silently omit the support from the wheel-envelope audit.
        offsets = [(-radius, 0), (radius, 0)] if vertical else [(0, -radius), (0, radius)]
        points = [(x + dx, y + dy) for x, y in (a, b) for dx, dy in offsets]
        polygon = geometry.hull(points)
        return [[(polygon[0][0], polygon[0][1], 0),
                 (polygon[i][0], polygon[i][1], 0),
                 (polygon[i + 1][0], polygon[i + 1][1], 0)] for i in range(1, len(polygon) - 1)]
    result = []
    for side in (-1, 1):
        result.append((f"outboard support {side}", support((side * outboard, underbrace), (side * outboard, height), crossbar, True)))
        result.append((f"underbrace {side}", support((0, underbrace), (side * outboard, underbrace), crossbar, False)))
    if placement != "legacy":
        result.append(("external spine crossbrace", support((-outboard, underbrace), (outboard, underbrace), crossbar, False)))
        result.append(("appearance end bracket", support((0, constant("SPINE_HEIGHT")), (0, spine), constant("RUNNING_MOUNT_RADIUS"), True)))
    for side in (-1, 1):
        # Both endpoint heights bound all supported continuous control strokes.
        points = [(side * 1.35 + dx, y) for dx in (-constant("CONTROL_SUPPORT_RADIUS"), constant("CONTROL_SUPPORT_RADIUS")) for y in (-1.35, -.6)]
        points.extend([(dx, spine) for dx in (-constant("CONTROL_SUPPORT_RADIUS"), constant("CONTROL_SUPPORT_RADIUS"))])
        polygon = geometry.hull(points)
        triangles = [[(*polygon[0], 0), (*polygon[i], 0), (*polygon[i + 1], 0)] for i in range(1, len(polygon) - 1)]
        result.append((f"control mount {side}", triangles))
    return result, hashlib.sha256(source.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("base_jar", type=Path)
    parser.add_argument("styles_jar", type=Path)
    parser.add_argument("--placement", choices=("legacy", "wheel-clearance-v2"), default="wheel-clearance-v2")
    parser.add_argument("--margin", type=float, default=.01)
    args = parser.parse_args()
    if not 0 < args.margin <= .05:
        parser.error("Margin must be finite and within (0, .05]")
    base_digest = hashlib.sha256(args.base_jar.read_bytes()).hexdigest()
    styles_digest = hashlib.sha256(args.styles_jar.read_bytes()).hexdigest()
    if base_digest != BASE_PIN or styles_digest != geometry.stroke.PIN:
        parser.error("Requires the pinned base 0.1.5 and Track Styles 1.2.0 artifacts")
    # Synthetic intersection, containment, degenerate face and separated cases.
    square = [(0, 0), (1, 0), (1, 1), (0, 1)]
    assert geometry.projected_gap(square, [(.5, .5)] * 3) == 0
    assert geometry.projected_gap(square, [(2, 0), (2, 1), (3, 0)]) == 1
    assert geometry.projected_gap(square, [(-1, -1), (3, -1), (0, 3)]) == 0
    results, legacy_results = [], []
    with zipfile.ZipFile(args.base_jar) as base, zipfile.ZipFile(args.styles_jar) as styles:
        envelopes = wheel_envelopes(base.read(WHEEL_RESOURCE).decode())
        for style in ("arrow_4d", "arrow_4d_dbl_spine"):
            for component in ("center_beam", "tie"):
                resource = f"assets/coasterssimulatedextratypes/models/block/track/{style}/{style}_{component}.obj"
                triangles = geometry.model(styles.read(resource).decode(), component, args.placement)
                results.append({"resource": resource, **measure(triangles, envelopes, args.margin)})
                legacy = geometry.model(styles.read(resource).decode(), component, "legacy")
                legacy_results.append({"resource": resource, **measure(legacy, envelopes, args.margin)})
    if not all(not r["passes"] and r["minimumConservativeGap"] == 0 for r in legacy_results if "_tie.obj" in r["resource"]):
        raise ValueError("Audit no longer detects the pinned legacy tie-envelope conflict")
    supports, track_source_digest = native_supports(args.placement)
    support_results = [{"component": name, **measure(triangles, envelopes, args.margin)} for name, triangles in supports]
    output = {"baseSha256": base_digest, "providerSha256": styles_digest,
              "placement": args.placement, "placementSourceSha256": geometry.placement_config(args.placement)[2],
              "nativeWheelResource": WHEEL_RESOURCE, "wheelEnvelopes": len(envelopes),
              "placementDerivation": "native path anchor +.3125Y, renderer +.1875Y and axis +.1Y; lower row -.55Y; centered model, no world mesh scale",
              "method": "XY triangle distance to full-spin cuboid cylinder projection, extruded over continuous longitudinal travel",
              "scope": "level straight, default pinned wheel and decoration meshes; excludes banking, chassis, carriers, riders, linked vehicles and resource overrides",
              "trackDesignSourceSha256": track_source_digest,
              "nativeSupports": support_results, "legacyReferenceModels": legacy_results, "models": results}
    print(json.dumps(output, indent=2))
    if not all(r["passes"] for r in results + support_results):
        raise SystemExit("Wheel swept envelope has an uncertified or insufficient decoration gap")


if __name__ == "__main__":
    main()
