#!/usr/bin/env python3
"""Audit default block-model volumes at recorded native recovery poses.

This is sampled clearance evidence, not continuous swept clearance. Original
track tubes are reconstructed from the exact renderer sampling and source-pinned
dimensions. Body transforms and block states come from the native test, not ideal
track frames. External resources are read from the separately installed pinned
JAR and never copied. The native lower bearing model and full-spin wheel
envelopes use the same recorded track-aligned anchor as the chassis. Wheels are
checked against non-contact original track components and neighboring cars; the
two running rails are excluded because they are the intended wheel contact.
The sampled live native-seat passengers contribute their mounted oriented
envelopes. This does not verify player meshes, a client renderer or arbitrary
player cosmetics. Arrow's unmodeled mechanical linkage, decorations and resource
overrides remain outside this audit. A zero separating bound is UNCERTIFIED, not
a proven mesh collision.
"""
import argparse
import hashlib
import gzip
from functools import lru_cache
import itertools
import json
import math
from pathlib import Path
import re
import zipfile

import numpy as np

ROOT = Path(__file__).resolve().parents[1]
BASE_PIN = "f80b4c3cdf9fa6188804967147761b0373e150a6eadbdc53626e5ac9700b2049"
MINECRAFT_PIN = "499f6897d1837516680f3114072d8106e11c9adcd933fe5cf051b551089b0c99"
BOTTOM_MODEL = "simulatedcoasters:block/coaster_cart_bottom"
WHEEL_MODEL = "simulatedcoasters:block/coaster_cart_wheel_axis"
SOURCE_PINS = {
    "src/main/java/dev/fourthdimension/client/OriginalTrackRenderer.java": "5089ed597ef5651d5b221eb437e088469f2f28d21028c7bc6be6c9266eca0f0f",
    "src/main/java/dev/fourthdimension/track/TrackDesign.java": "e3a88c3ca661d7b232ee0b9d3c853a6e66c1d0d1147ac4266b001616b1fe1ff1",
    "src/main/java/dev/fourthdimension/vehicle/BankedRecoveryExamples.java": "0d6597a484cf83cca90671ab6351c81cca42a4e9b96075b4843b136d81229439",
}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


@lru_cache(maxsize=None)
def constant(name):
    source = (ROOT / "src/main/java/dev/fourthdimension/track/TrackDesign.java").read_text()
    match = re.search(r"\b" + name + r"\s*=\s*([\d.\-]+)\s*[,;]", source)
    if match is None:
        raise ValueError("Unrecognized renderer dimension: " + name)
    return float(match[1])


def tube(a, b, ar, au, br, bu, rx, ry):
    angles = np.arange(8) * math.pi / 4
    first = a + np.cos(angles)[:, None] * rx * ar + np.sin(angles)[:, None] * ry * au
    last = b + np.cos(angles)[:, None] * rx * br + np.sin(angles)[:, None] * ry * bu
    return [tri for i in range(8) for tri in
            ((first[i], first[(i + 1) % 8], last[(i + 1) % 8]),
             (first[i], last[(i + 1) % 8], last[i]))]


def track_mesh(route):
    triangles, labels = [], []
    def put(label, *args):
        result = tube(*args)
        triangles.extend(result)
        labels.extend([label] * len(result))
    for section_index, section in enumerate(route["sections"]):
        previous = None
        frames = section["frames"]
        count = max(8, math.ceil(section["length"] * 4))
        if len(frames) != count + 1:
            raise ValueError("Native frames differ from renderer panel count")
        for i, frame in enumerate(frames):
            c, r, u, f = (np.array(frame[key]) for key in ("center", "right", "up", "forward"))
            control = constant("CONTROL_BASE_HEIGHT") + frame["profileAngle"] * .5 / (8 * math.pi)
            points = [c + side * constant("HALF_GAUGE") * r + constant("MAIN_RAIL_HEIGHT") * u
                      for side in (-1, 1)]
            points += [c + side * constant("CONTROL_HALF_GAUGE") * r + control * u
                       for side in (-1, 1)]
            points += [c + constant("SPINE_HEIGHT") * u]
            if previous is not None:
                old_points, old_r, old_u = previous
                for rail in range(5):
                    if rail in (2, 3) and route["family"] != "ARROW":
                        continue
                    rx = constant("RAIL_HALF_WIDTH") if rail < 2 else .22 if rail == 4 else .09
                    ry = constant("RAIL_HALF_HEIGHT") if rail < 2 else rx
                    put(f"section{section_index}:rail{rail}", old_points[rail], points[rail],
                        old_r, old_u, r, u, rx, ry)
            if i % 8 == 0 or i == count:
                radius = constant("RUNNING_CROSSBAR_RADIUS")
                underbrace = c + constant("RUNNING_UNDERBRACE_HEIGHT") * u
                for side in (-1, 1):
                    outer_bottom = underbrace + side * constant("RUNNING_OUTBOARD_HALF_GAUGE") * r
                    outer_top = c + side * constant("RUNNING_OUTBOARD_HALF_GAUGE") * r + constant("MAIN_RAIL_HEIGHT") * u
                    put(f"section{section_index}:underbrace{side}", underbrace, outer_bottom, f, u, f, u, radius, radius)
                    put(f"section{section_index}:outboardSupport{side}", outer_bottom, outer_top, f, r, f, r, radius, radius)
                    if route["family"] == "ARROW":
                        radius = constant("CONTROL_SUPPORT_RADIUS")
                        put(f"section{section_index}:controlMount{side}", points[2 if side < 0 else 3],
                            points[4], f, r, f, r, radius, radius)
            previous = points, r, u
    return np.array(triangles), labels


class Resources:
    def __init__(self, base, minecraft):
        self.base = base
        self.minecraft = minecraft
        self.hashes = {}
    def read(self, name):
        path = ROOT / "src/main/resources" / name
        data = path.read_bytes() if path.exists() else (self.minecraft if name.startswith("assets/minecraft/") else self.base).read(name)
        self.hashes[name] = hashlib.sha256(data).hexdigest()
        return json.loads(data)
    def elements(self, model):
        namespace, resource = model.split(":") if ":" in model else ("minecraft", model)
        data = self.read(f"assets/{namespace}/models/{resource}.json")
        if "elements" in data:
            return data["elements"]
        return self.elements(data["parent"])
    def boxes(self, block):
        namespace, name = block["block"].split(":")
        state = self.read(f"assets/{namespace}/blockstates/{name}.json")
        if "variants" not in state:
            raise ValueError("Unsupported multipart model: " + block["block"])
        matches = []
        for key, entry in state["variants"].items():
            if all(block["properties"].get(k) == v for k, v in
                   (part.split("=") for part in key.split(",") if part)):
                matches.append(entry)
        if len(matches) != 1 or not isinstance(matches[0], dict):
            raise ValueError("Ambiguous or weighted block model")
        entry = matches[0]
        if entry.get("x", 0) or entry.get("y", 0) % 90:
            raise ValueError("Unsupported blockstate rotation")
        angle = math.radians(-entry.get("y", 0))
        rotation = np.array([[math.cos(angle), 0, math.sin(angle)], [0, 1, 0],
                             [-math.sin(angle), 0, math.cos(angle)]])
        result = []
        for index, element in enumerate(self.elements(entry["model"])):
            if element.get("rotation", {}).get("angle", 0):
                raise ValueError("Unsupported model element rotation")
            low, high = (np.array(element[key]) / 16 - .5 for key in ("from", "to"))
            points = np.array(list(itertools.product(*zip(low, high)))) @ rotation.T + block["offset"]
            result.append((points, f'{block["block"]}@{block["offset"]}:element{index}'))
        return result

    def model_boxes(self, model, label):
        result = []
        for index, element in enumerate(self.elements(model)):
            if element.get("rotation", {}).get("angle", 0):
                raise ValueError("Unsupported native lower-bearing element rotation")
            low, high = (np.array(element[key]) / 16 - .5 for key in ("from", "to"))
            result.append(
                (np.array(list(itertools.product(*zip(low, high)))), f"{label}:element{index}"))
        return result

    def wheel_envelopes(self):
        elements = self.elements(WHEEL_MODEL)
        if len(elements) != 10:
            raise ValueError("Unexpected native wheel model")
        result = []
        for index, element in enumerate(elements):
            rotation = element.get("rotation", {})
            if rotation.get("angle", 0) != 0 and (
                rotation.get("axis") != "x" or rotation.get("rescale", False)
                or rotation.get("origin", [0, 0, 0])[1:] != [8, 8]
            ):
                raise ValueError("Wheel cuboid rotation does not preserve the spin axis")
            low, high = (np.array(element[key]) / 16 - .5 for key in ("from", "to"))
            radius = max(math.hypot(y, z) for y in (low[1], high[1]) for z in (low[2], high[2]))
            for row, height in (("main", .6), ("upstop", .05)):
                for axle, longitudinal in (("front", -.7), ("back", .7)):
                    envelope_low = np.array((low[0], height - radius, longitudinal - radius))
                    envelope_high = np.array((high[0], height + radius, longitudinal + radius))
                    result.append(
                        (np.array(list(itertools.product(*zip(envelope_low, envelope_high)))),
                         f"nativeWheel:{row}:{axle}:element{index}"))
        return result


def separation(box, axes, triangles):
    """OBB/triangle SAT lower distance bounds, including contained triangles."""
    edges = np.roll(triangles, -1, axis=1) - triangles
    directions = [np.broadcast_to(axis, (len(triangles), 3)) for axis in axes]
    directions += [np.cross(edges[:, 0], edges[:, 1])]
    directions += [np.cross(axis, edges[:, edge]) for axis in axes for edge in range(3)]
    best = np.zeros(len(triangles))
    for direction in directions:
        length = np.linalg.norm(direction, axis=1)
        direction = direction / np.maximum(length, 1e-15)[:, None]
        a = direction @ box.T
        b = np.einsum("nik,nk->ni", triangles, direction)
        best = np.maximum(best, np.maximum(a.min(axis=1) - b.max(axis=1), b.min(axis=1) - a.max(axis=1)))
    return best


def boxes_separation(first, axes, second, other_axes):
    directions = list(axes) + list(other_axes)
    directions += [np.cross(a, b) for a in axes for b in other_axes]
    best = 0.0
    for direction in directions:
        length = np.linalg.norm(direction)
        if length < 1e-12:
            continue
        direction = direction / length
        a, b = first @ direction, second @ direction
        best = max(best, float(a.min() - b.max()), float(b.min() - a.max()))
    return best


def audit(route, entries, resources, margin):
    triangles, labels = track_mesh(route)
    lows, highs = triangles.min(axis=1), triangles.max(axis=1)
    grid = {}
    for index, (low, high) in enumerate(zip(lows, highs)):
        for cell in itertools.product(*(range(a, b + 1) for a, b in zip(np.floor(low / 2).astype(int), np.floor(high / 2).astype(int)))):
            grid.setdefault(cell, []).append(index)
    centers = [np.array([frame["center"] for frame in section["frames"]]) for section in route["sections"]]
    native_bottom_boxes = resources.model_boxes(BOTTOM_MODEL, "nativeLowerBearing")
    native_wheel_boxes = resources.wheel_envelopes()
    specs, samples, completed, visited = {}, 0, False, {}
    previous_tick, original_body_ids = None, None
    closest, failures = None, []
    minimum = math.inf
    wheel_minimum, wheel_closest, wheel_checks = math.inf, None, 0
    rider_minimum, rider_closest, rider_checks = math.inf, None, 0
    rider_rack_minimum, rider_rack_closest, rider_rack_checks = math.inf, None, 0
    neighbor_minimum, neighbor_closest = math.inf, None
    neighbor_pairs = 0
    for entry in entries:
        if entry["type"] == "body":
            specs[entry["id"]] = {**entry, "boxes": [box for block in entry["blocks"] for box in resources.boxes(block)]}
        elif entry.get("variant") == route["variant"] and entry["type"] == "complete":
            if entry["cars"] != route["carCount"]:
                raise ValueError("Incorrect completed car count")
            completed = True
        elif entry.get("variant") == route["variant"] and entry["type"] == "sample":
            samples += 1
            if previous_tick is not None and entry["tick"] - previous_tick != 20:
                raise ValueError("Missing or out-of-order native pose samples")
            previous_tick = entry["tick"]
            ids = [pose["id"] for pose in entry["poses"]]
            if len(ids) != len(set(ids)) or (original_body_ids is not None and set(ids) != original_body_ids):
                raise ValueError("Duplicated or replaced original mesh bodies")
            original_body_ids = set(ids)
            cars = [specs[p["id"]] for p in entry["poses"] if specs[p["id"]]["role"] == "chassis"]
            carriers = [specs[p["id"]] for p in entry["poses"] if specs[p["id"]]["role"] == "carrier"]
            if len(cars) != route["carCount"] or len(carriers) != len(cars) * (4 if route["family"] in ("FREE_SPIN", "REVISED") else 2):
                raise ValueError("Incomplete original bodies in mesh sample")
            sample_boxes = []
            pose_by_id = {pose["id"]: pose for pose in entry["poses"]}
            rider_cars, rider_ids = set(), set()
            for rider in entry.get("riders", []):
                if rider["id"] in rider_ids:
                    raise ValueError("Duplicated mounted rider in recovery trace")
                rider_ids.add(rider["id"])
                if rider["body"] not in pose_by_id or rider["body"] not in specs:
                    raise ValueError("Mounted rider is not attached to a sampled carrier")
                body = specs[rider["body"]]
                if body["role"] != "carrier":
                    raise ValueError("Mounted rider is not attached to a carrier")
                axes = np.array(pose_by_id[rider["body"]]["axes"])
                origin = np.array(pose_by_id[rider["body"]]["origin"])
                eye = np.array(rider["eye"])
                width, height, eye_height = (float(rider[key]) for key in ("width", "height", "eyeHeight"))
                if (not np.isfinite(eye).all() or not all(math.isfinite(value) for value in (width, height, eye_height))
                        or not .5 <= width <= 1.1 or not .5 <= height <= 2.1 or not .3 <= eye_height <= 1.8):
                    raise ValueError("Mounted rider has unexpected dimensions")
                # Sable renders a passenger around its live eye point with the containing carrier's
                # orientation. The native passenger dimensions define this mounted body envelope.
                local_low, local_high = np.array((-width / 2, -eye_height, -width / 2)), np.array((width / 2, height - eye_height, width / 2))
                world = np.array(list(itertools.product(*zip(local_low, local_high)))) @ axes + eye
                low, high = world.min(axis=0), world.max(axis=0)
                sample_boxes.append((world, axes, low, high, body["car"], "nativeRider"))
                rider_cars.add(body["car"])
                padding = margin * 4
                cells = itertools.product(*(range(a, b + 1) for a, b in zip(np.floor((low - padding) / 2).astype(int), np.floor((high + padding) / 2).astype(int))))
                indices = np.array(sorted({index for cell in cells for index in grid.get(cell, [])}), dtype=int)
                rider_checks += 1
                if not len(indices):
                    rider_minimum = min(rider_minimum, padding)
                    continue
                gaps = np.linalg.norm(np.maximum(0, np.maximum(low - highs[indices], lows[indices] - high)), axis=1)
                candidates = np.flatnonzero(gaps < padding)
                if candidates.size:
                    gaps[candidates] = separation(world, axes, triangles[indices[candidates]])
                index = int(indices[int(np.argmin(gaps))])
                gap = min(padding, float(gaps.min()))
                if gap < rider_minimum:
                    rider_minimum = gap
                    rider_closest = {"tick": entry["tick"], "body": rider["body"], "modelElement": "nativeRider", "trackComponent": labels[index]}
                if gap < margin and len(failures) < 20:
                    failures.append({"gap": gap, "tick": entry["tick"], "body": rider["body"], "modelElement": "nativeRider", "trackComponent": labels[index]})
                # A rider is mounted to a seat, so its seat volume is intentional contact.
                # Every other block-model volume in that exact carrier is mechanical rack
                # geometry and must retain the same clearance as the route/neighbor audit.
                for rack_points, rack_label in body["boxes"]:
                    if rack_label.startswith("fourthdimension:") and "_seat@" in rack_label:
                        continue
                    rack_world = rack_points @ axes + origin
                    rack_low, rack_high = rack_world.min(axis=0), rack_world.max(axis=0)
                    rack_gap = float(np.linalg.norm(np.maximum(0, np.maximum(low - rack_high, rack_low - high))))
                    if rack_gap < margin * 4:
                        rack_gap = boxes_separation(world, axes, rack_world, axes)
                    rider_rack_checks += 1
                    if rack_gap < rider_rack_minimum:
                        rider_rack_minimum = rack_gap
                        rider_rack_closest = {
                            "tick": entry["tick"], "body": rider["body"],
                            "modelElement": "nativeRider", "rackComponent": rack_label}
                    if rack_gap < margin and len(failures) < 20:
                        failures.append({
                            "gap": rack_gap, "tick": entry["tick"], "body": rider["body"],
                            "modelElement": "nativeRider", "rackComponent": rack_label})
            if len(rider_cars) != route["carCount"]:
                raise ValueError("Missing a mounted rider for an original car")
            for pose in entry["poses"]:
                body = specs[pose["id"]]
                axes, origin = np.array(pose["axes"]), np.array(pose["origin"])
                if not np.isfinite(axes).all() or not np.isfinite(origin).all() or not np.allclose(axes @ axes.T, np.eye(3), atol=1e-5):
                    raise ValueError("Invalid native pose")
                if body["role"] == "chassis":
                    distances = [np.linalg.norm(section - origin, axis=1).min() for section in centers]
                    if min(distances) > 2:
                        raise ValueError("Original chassis left recorded route")
                    nearest = int(np.argmin(distances))
                    visited.setdefault(pose["id"], set()).add(nearest)
                rendered_boxes = list(body["boxes"])
                if body["role"] in ("chassis", "ordinary"):
                    # Coasters draws this model at pathAnchorBottom, qPath and
                    # translates -0.5 before model coordinates. The recorded
                    # chassis origin/axes are bounded against that native frame
                    # by the GameTest, so these centered model points use it.
                    rendered_boxes.extend(native_bottom_boxes)
                    # Coasters rotates the wheel model around local X. A box
                    # enclosing its full spin is conservative at each sampled
                    # qPath-equivalent frame. Running rails are intentionally
                    # filtered below because both wheel rows contact them.
                    rendered_boxes.extend(native_wheel_boxes)
                for points, label in rendered_boxes:
                    world = points @ axes + origin
                    low, high = world.min(axis=0), world.max(axis=0)
                    sample_boxes.append((world, axes, low, high, body["car"], label))
                    padding = margin * 4
                    cells = itertools.product(*(range(a, b + 1) for a, b in zip(np.floor((low - padding) / 2).astype(int), np.floor((high + padding) / 2).astype(int))))
                    indices = np.array(sorted({index for cell in cells for index in grid.get(cell, [])}), dtype=int)
                    wheel = label.startswith("nativeWheel:")
                    if wheel:
                        indices = np.array(
                            [index for index in indices if not labels[index].endswith((":rail0", ":rail1"))],
                            dtype=int)
                        wheel_checks += 1
                    if not len(indices):
                        if wheel:
                            wheel_minimum = min(wheel_minimum, padding)
                        else:
                            minimum = min(minimum, padding)
                        continue
                    gaps = np.linalg.norm(np.maximum(0, np.maximum(low - highs[indices], lows[indices] - high)), axis=1)
                    candidates = np.flatnonzero(gaps < padding)
                    if candidates.size:
                        gaps[candidates] = separation(world, axes, triangles[indices[candidates]])
                    best = int(np.argmin(gaps))
                    index = int(indices[best])
                    gap = min(padding, float(gaps[best]))
                    if gap < minimum:
                        minimum = gap
                        closest = {"tick": entry["tick"], "body": pose["id"], "role": body["role"],
                                   "modelElement": label, "trackComponent": labels[index]}
                    if wheel and gap < wheel_minimum:
                        wheel_minimum = gap
                        wheel_closest = {"tick": entry["tick"], "body": pose["id"], "role": body["role"],
                                         "modelElement": label, "trackComponent": labels[index]}
                    if gap < margin and len(failures) < 20:
                        failures.append({"gap": gap, "tick": entry["tick"], "body": pose["id"],
                                         "modelElement": label, "trackComponent": labels[index]})
            # Contacts within one car include intentional strut/hinge joints.
            # Different original cars and the ordinary recovery body cannot
            # share those joints, so every pair of recorded model volumes is checked.
            for first, second in itertools.combinations(sample_boxes, 2):
                if first[4] == second[4]:
                    continue
                neighbor_pairs += 1
                gap = float(np.linalg.norm(np.maximum(0, np.maximum(first[2] - second[3], second[2] - first[3]))))
                if gap < margin * 4:
                    gap = boxes_separation(first[0], first[1], second[0], second[1])
                if gap < neighbor_minimum:
                    neighbor_minimum = gap
                    neighbor_closest = {"tick": entry["tick"], "first": first[5], "second": second[5]}
                if gap < margin and len(failures) < 20:
                    failures.append({"gap": gap, "tick": entry["tick"], "neighborPair": neighbor_closest})
    if not completed or samples < 150 or len(visited) != route["carCount"] or any(len(v) != 5 for v in visited.values()):
        raise ValueError("Mesh trace lacks full original-car route coverage and normal completion")
    if wheel_checks == 0 or not math.isfinite(wheel_minimum):
        raise ValueError("Native wheel envelopes were not audited")
    if rider_checks == 0 or not math.isfinite(rider_minimum):
        raise ValueError("Mounted rider envelopes were not audited")
    if rider_rack_checks == 0 or not math.isfinite(rider_rack_minimum):
        raise ValueError("Mounted rider envelopes were not audited against carrier rack geometry")
    return {"family": route["family"], "cars": route["carCount"], "samples": samples,
            "trackTriangles": len(triangles), "minimumSeparatingBound": minimum,
            "closest": closest, "requiredGap": margin, "passes": not failures,
            "wheelEnvelopeCount": wheel_checks,
            "wheelMinimumSeparatingBound": wheel_minimum,
            "wheelClosest": wheel_closest,
            "riderEnvelopeCount": rider_checks,
            "riderMinimumSeparatingBound": rider_minimum,
            "riderClosest": rider_closest,
            "riderRackEnvelopeCount": rider_rack_checks,
            "riderRackMinimumSeparatingBound": rider_rack_minimum,
            "riderRackClosest": rider_rack_closest,
            "neighborMinimumSeparatingBound": neighbor_minimum,
            "neighborClosest": neighbor_closest, "neighborPairs": neighbor_pairs,
            "uncertifiedPairs": failures, "allCarsVisitedSections": 5}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("base_jar", type=Path)
    parser.add_argument("minecraft_jar", type=Path)
    parser.add_argument("frames", type=Path)
    parser.add_argument("trace", type=Path)
    parser.add_argument("--margin", type=float, default=.02)
    args = parser.parse_args()
    if not math.isfinite(args.margin) or not 0 < args.margin <= .05:
        parser.error("Margin must be finite and in (0, .05]")
    if digest(args.base_jar) != BASE_PIN:
        parser.error("Requires inspected native Coasters 0.1.5 artifact")
    if digest(args.minecraft_jar) != MINECRAFT_PIN:
        parser.error("Requires inspected Minecraft 1.21.1 client resources")
    for name, pin in SOURCE_PINS.items():
        if digest(ROOT / name) != pin:
            parser.error("Reinspect geometry reconstruction after source change: " + name)
    # A contained triangle must be uncertified; a separated one needs a real gap.
    box = np.array(list(itertools.product((-1, 1), repeat=3)))
    control = np.array([[(0, 0, 0), (.5, 0, 0), (0, .5, 0)],
                        [(2, 0, 0), (2, .5, 0), (2, 0, .5)]])
    assert np.allclose(separation(box, np.eye(3), control), [0, 1])
    angle = math.pi / 6
    rotation = np.array([[math.cos(angle), -math.sin(angle), 0], [math.sin(angle), math.cos(angle), 0], [0, 0, 1]])
    assert np.allclose(separation(box @ rotation.T, np.eye(3) @ rotation.T, control @ rotation.T), [0, 1])
    assert boxes_separation(box, np.eye(3), box, np.eye(3)) == 0
    assert boxes_separation(box, np.eye(3), box + [3, 0, 0], np.eye(3)) == 1
    def read_text(path):
        return gzip.decompress(path.read_bytes()).decode() if path.suffix == ".gz" else path.read_text()
    routes = json.loads(read_text(args.frames))
    text = read_text(args.trace)
    entries = [json.loads(line) for line in text.splitlines()]
    with zipfile.ZipFile(args.base_jar) as base, zipfile.ZipFile(args.minecraft_jar) as minecraft:
        resources = Resources(base, minecraft)
        results = [audit(route, entries, resources, args.margin) for route in routes]
    output = {"baseSha256": BASE_PIN, "minecraftSha256": digest(args.minecraft_jar), "framesSha256": digest(args.frames), "traceSha256": digest(args.trace),
              "verifierSha256": digest(Path(__file__)),
              "sourceHashes": {name: digest(ROOT / name) for name in
                               ("src/main/java/dev/fourthdimension/client/OriginalTrackRenderer.java",
                                "src/main/java/dev/fourthdimension/track/TrackDesign.java",
                                "src/main/java/dev/fourthdimension/gametest/RecoveryMeshTrace.java",
                                "src/main/java/dev/fourthdimension/vehicle/BankedRecoveryExamples.java") },
              "resourceHashes": resources.hashes, "sampleIntervalTicks": 20,
              "scope": "sampled original default block-model, native lower-bearing, full-spin wheel-envelope and mounted native-rider rendered-orientation volumes against original track tubes; running rails are excluded only for wheels as intended contact; no continuous sweep, player meshes, client renderer/cosmetics, linkage, decorations or resource overrides",
              "method": "2-block spatial bins; omitted pairs certify at least 4x margin; world AABB separation refined by OBB/triangle SAT lower distance bounds",
              "routes": results}
    print(json.dumps(output, indent=2))
    if not all(result["passes"] for result in results):
        raise SystemExit("Sampled model clearance contains uncertified pairs")


if __name__ == "__main__":
    main()
