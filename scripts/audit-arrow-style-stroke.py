#!/usr/bin/env python3
"""Measure straight control-rail/tie clearance in a separately installed pinned JAR.

Print aggregate distances only, never extracted models or vertex/face data. Project
triangles into local X/Y: their union bounds clearance to a longitudinal cylinder
over every Z of the tie. This covers the continuous declared stroke interval; it
does not certify curved/banked placement, riders or resource-pack replacements.
"""
import argparse
import hashlib
import json
import math
from pathlib import Path
import zipfile

PIN = "42535a83307f6898b33c13ca201533bb9e337418fd7c73039c867fc80bfbcb38"
HALF_GAUGE, RADIUS = 1.35, 0.09


def cross(a, b, p):
    return (b[0] - a[0]) * (p[1] - a[1]) - (b[1] - a[1]) * (p[0] - a[0])


def point_segment(p, a, b):
    dx, dy = b[0] - a[0], b[1] - a[1]
    length2 = dx * dx + dy * dy
    t = 0 if length2 == 0 else max(0, min(1, ((p[0] - a[0]) * dx + (p[1] - a[1]) * dy) / length2))
    return math.hypot(p[0] - a[0] - t * dx, p[1] - a[1] - t * dy)


def point_triangle(p, triangle):
    a, b, c = triangle
    signs = (cross(a, b, p), cross(b, c, p), cross(c, a, p))
    if abs(cross(a, b, c)) > 1e-12 and (min(signs) >= 0 or max(signs) <= 0):
        return 0.0
    return min(point_segment(p, a, b), point_segment(p, b, c), point_segment(p, c, a))


def segments_intersect(a, b, c, d):
    ab_c, ab_d, cd_a, cd_b = cross(a, b, c), cross(a, b, d), cross(c, d, a), cross(c, d, b)
    if ab_c * ab_d < 0 and cd_a * cd_b < 0:
        return True
    # Include collinear overlap and endpoint contact without accepting disjoint collinear lines.
    return any(point_segment(p, x, y) <= 1e-12 for p, x, y in ((a, c, d), (b, c, d), (c, a, b), (d, a, b)))


def stroke_triangle(a, b, triangle):
    edges = ((triangle[0], triangle[1]), (triangle[1], triangle[2]), (triangle[2], triangle[0]))
    if any(segments_intersect(a, b, c, d) for c, d in edges):
        return 0.0
    return min(point_triangle(a, triangle), point_triangle(b, triangle), *(point_segment(p, a, b) for p in triangle))


def projected_triangles(source, x_origin):
    vertices, faces = [], []
    for line in source.splitlines():
        if line.startswith("v "):
            x, y, _ = map(float, line.split()[1:4])
            vertices.append((x - x_origin, y))
        elif line.startswith("f "):
            face = [int(part.split("/")[0]) for part in line.split()[1:]]
            faces.append([vertices[index - 1 if index > 0 else index] for index in face])
    return [(face[0], face[i], face[i + 1]) for face in faces for i in range(1, len(face) - 1)]


def measure(triangles, low_turn, high_turn):
    y0, y1 = -0.85 + low_turn * 0.125, -0.85 + high_turn * 0.125
    distance = min(stroke_triangle((side * HALF_GAUGE, y0), (side * HALF_GAUGE, y1), triangle)
                   for side in (-1, 1) for triangle in triangles)
    return {"turnInterval": [low_turn, high_turn], "controlHeightInterval": [y0, y1],
            "minimumCenterDistance": distance, "minimumSurfaceClearance": distance - RADIUS}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("jar", type=Path)
    args = parser.parse_args()
    # Independent analytic fixtures for projection degeneracy, crossing and nearest distance.
    assert not segments_intersect((0, 0), (0, 1), (0, 2), (0, 3))
    assert segments_intersect((0, 0), (0, 2), (0, 1), (0, 3))
    assert segments_intersect((-1, 0), (1, 0), (0, -1), (0, 1))
    assert point_triangle((0.2, 0.2), ((0, 0), (1, 0), (0, 1))) == 0
    assert point_triangle((0, 2), ((0, 0), (0, 1), (0, 1))) == 1
    assert stroke_triangle((0, 0), (0, 3), ((1, 1), (2, 1), (1, 2))) == 1
    assert abs(stroke_triangle((0, -1), (0, 1), ((1, 2), (2, 2), (1, 3))) - math.sqrt(2)) < 1e-12
    assert stroke_triangle((0.2, -1), (0.2, 2), ((0, 0), (1, 0), (0, 1))) == 0
    digest = hashlib.sha256(args.jar.read_bytes()).hexdigest()
    if digest != PIN:
        parser.error("This audit is pinned to the inspected Track Styles 1.2.0 artifact")
    models = []
    with zipfile.ZipFile(args.jar) as archive:
        for style in ("arrow_4d", "arrow_4d_dbl_spine"):
            for component, origin in (("center_beam", 0), ("tie", 0.5)):
                resource = f"assets/coasterssimulatedextratypes/models/block/track/{style}/{style}_{component}.obj"
                triangles = projected_triangles(archive.read(resource).decode(), origin)
                safe = measure(triangles, -4, 2)
                models.append({"resource": resource, "projectedTriangles": len(triangles),
                               "acceptedStroke": safe, "knownConflict": measure(triangles, 3, 4)})
                assert safe["minimumSurfaceClearance"] > 0.05, "Declared straight-stroke margin not met"
                if component == "tie":
                    assert models[-1]["knownConflict"]["minimumSurfaceClearance"] < 0, "Expected high-stroke conflict absent"
    print(json.dumps({"sha256": digest, "space": "default OBJ local X/Y, tie origin X=0.5",
                      "controlRailHalfGauge": HALF_GAUGE, "controlRailRadius": RADIUS,
                      "method": "continuous stroke segment against projected face triangles",
                      "scope": "straight local cylinder; excludes curves, banking, riders and resource overrides",
                      "models": models}, indent=2))


if __name__ == "__main__":
    main()
