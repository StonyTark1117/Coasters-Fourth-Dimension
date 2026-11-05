#!/usr/bin/env python3
"""Audit vanilla armor, cape and elytra clearance at recorded recovery poses.

This extends the source-pinned default-player audit without changing its archived
verifier.  The inputs must already have a passing ``audit-recovery-mesh-trace``
report for the exact route frames and raw trace.  Wearable geometry is reconstructed
from the pinned Minecraft 1.21.1 client definitions:

* inner (0.5 px) and outer (1.0 px, 0.9 px legs) armor models;
* the complete clamped vanilla cape rotation range, including walk bob; and
* all non-fall-flying elytra interpolation between folded and crouched poses.

Every named wearable envelope is checked against original track triangles and every
non-seat block-model element in its live carrier.  Mutually exclusive wearables are
audited as alternatives, not combined into one synthetic mesh.  This does not cover
held items, non-armor head-slot renderers, arbitrary modded render layers, resource
overrides, or client observation.  A zero separating bound is uncertified.
"""

import argparse
import hashlib
from importlib.machinery import SourceFileLoader
import importlib.util
import io
import itertools
import json
import math
from pathlib import Path
import zipfile

import numpy as np
import zstandard


ROOT = Path(__file__).resolve().parents[1]
# Extend the exact verifier archived with the passing player/rack reports.  Later
# substep work evolved the live script, so silently importing today's revision
# would sever the baseline report's provenance even when geometry was unchanged.
MESH_PATH = (ROOT / "docs/evidence/0.4/recovery/player-rack-clearance"
             / "audit-recovery-mesh-trace.py.txt")
MESH_SPEC = importlib.util.spec_from_loader(
    "recovery_mesh", SourceFileLoader("recovery_mesh", str(MESH_PATH)))
MESH = importlib.util.module_from_spec(MESH_SPEC)
MESH_SPEC.loader.exec_module(MESH)
MESH.ROOT = ROOT


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def corners(low, high):
    return np.array(list(itertools.product(*zip(low, high))), dtype=float)


def aabb_enclosure(points):
    """Reduce a sampled pose union to its conservative carrier-local box."""
    points = np.asarray(points, dtype=float)
    return corners(points.min(axis=0), points.max(axis=0))


def rotation_x(angle):
    sine, cosine = math.sin(angle), math.cos(angle)
    return np.array(((1, 0, 0), (0, cosine, -sine), (0, sine, cosine)))


def rotation_y(angle):
    sine, cosine = math.sin(angle), math.cos(angle)
    return np.array(((cosine, 0, sine), (0, 1, 0), (-sine, 0, cosine)))


def rotation_z(angle):
    sine, cosine = math.sin(angle), math.cos(angle)
    return np.array(((cosine, -sine, 0), (sine, cosine, 0), (0, 0, 1)))


def rotation_zyx(z_angle, y_angle, x_angle):
    """Match ModelPart's Z, Y, X rotation order for a local cuboid."""
    return rotation_z(z_angle) @ rotation_y(y_angle) @ rotation_x(x_angle)


def player_model_to_eye(points):
    """Apply the inspected Sable player scale/translation, relative to the eye."""
    result = np.array(points, dtype=float)
    result[:, 0] = MESH.PLAYER_RENDER_SCALE * result[:, 0] / 16
    result[:, 1] = (MESH.PLAYER_RENDER_SCALE
                    * (MESH.PLAYER_RENDER_TRANSLATION - result[:, 1] / 16)
                    - MESH.PLAYER_EYE_HEIGHT)
    result[:, 2] = MESH.PLAYER_RENDER_SCALE * result[:, 2] / 16
    return result


def rotation_enclosure(low, high, pivot):
    """Axis-aligned cube enclosing every rotation of a cuboid about its pivot."""
    radius = float(np.linalg.norm(corners(low, high), axis=1).max())
    pivot = np.array(pivot, dtype=float)
    return corners(pivot - radius, pivot + radius)


def armor_parts():
    """Conservative visible parts for every vanilla armor slot while seated."""
    parts = []
    # Outer armor is used by head/chest/feet.  The armor-model legs reduce its
    # one-pixel deformation by 0.1 pixel.  The head retains the baseline audit's
    # all-camera-angle enclosure; arms cover the complete empty-handed seated
    # idle-bob range. Held-item poses are a distinct, still-open audit surface.
    parts.append(("armor:outerHelmetAllRotations", player_model_to_eye(
        rotation_enclosure((-5, -9, -5), (5, 1, 5), (0, 0, 0)))))
    parts.append(("armor:outerChestBody", player_model_to_eye(
        corners((-5, -1, -3), (5, 13, 3)))))
    for side in (-1, 1):
        low = (-4, -3, -3) if side < 0 else (-2, -3, -3)
        high = (2, 11, 3) if side < 0 else (4, 11, 3)
        label = "right" if side < 0 else "left"
        arm = corners(low, high)
        poses = []
        for x_delta in np.linspace(-.05, .05, 9):
            for z_amount in np.linspace(0, .1, 9):
                transform = rotation_zyx(
                    side * z_amount, 0, -math.pi / 5 + side * x_delta)
                poses.extend(arm @ transform.T + np.array((side * 5, 2, 0)))
        parts.append((f"armor:outerChest{label.title()}ArmIdleBobEnvelope",
                      aabb_enclosure(player_model_to_eye(poses))))
    outer_leg = corners((-2.9, -.9, -2.9), (2.9, 12.9, 2.9))
    inner_leg = corners((-2.4, -.4, -2.4), (2.4, 12.4, 2.4))
    for side in (-1, 1):
        pivot = np.array((side * 1.9, 12.0, 0.0))
        transform = rotation_zyx(side * .07853982, side * -math.pi / 10, -1.4137167)
        label = "right" if side < 0 else "left"
        parts.append((f"armor:outerBoot{label.title()}Leg",
                      player_model_to_eye(outer_leg @ transform.T + pivot)))
        parts.append((f"armor:innerLegging{label.title()}Leg",
                      player_model_to_eye(inner_leg @ transform.T + pivot)))
    return parts


def elytra_parts():
    """Enclose all seated/crouched interpolation states, excluding fall flight."""
    result = []
    # CubeDeformation(1): each nominal 10x20x2 wing grows by one pixel.
    for side in (-1, 1):
        local = corners((-1, -1, -1), (11, 21, 3)) if side < 0 else corners((-11, -1, -1), (1, 21, 3))
        poses = []
        # AbstractClientPlayer lerps by 0.1, so all intermediate rotations and
        # pivots between folded and crouched states are reachable after mounting.
        for amount in np.linspace(0, 1, 33):
            x_angle = (1 - amount) * math.pi / 12 + amount * 2 * math.pi / 9
            z_angle = side * ((1 - amount) * math.pi / 12 + amount * math.pi / 4)
            y_angle = side * amount * math.radians(5)
            pivot = np.array((-side * 5, amount * 3, 2.0))  # layer z translation = 2 px
            poses.extend(local @ rotation_zyx(z_angle, y_angle, x_angle).T + pivot)
        label = "right" if side < 0 else "left"
        result.append((f"elytra:{label}WingFoldedToCrouched",
                       aabb_enclosure(player_model_to_eye(poses))))
    return result


def cape_parts():
    """Enclose the full vanilla clamped cape animation for seated adults."""
    cloak = corners((-5, 0, -1), (5, 16, 0))
    poses = []
    # CapeLayer clamps vertical cloak motion to [-6, 32], then adds a walk-bob
    # term in [-32, 32].  Forward and lateral clamps are [0,150] and [-20,20].
    # Dense samples retain extrema that need not occur at a corner of angle space.
    for vertical in np.linspace(-38, 64, 33):
        for forward in np.linspace(0, 150, 33):
            for lateral in np.linspace(-20, 20, 9):
                x_angle = math.radians(6 + forward / 2 + vertical)
                z_angle = math.radians(lateral / 2)
                y_angle = math.radians(180 - lateral / 2)
                # Empty chest and armored chest use different cloak pivots.
                for pivot_y, pivot_z in ((0.0, 0.0), (-.85, -1.1)):
                    local = cloak + np.array((0, pivot_y, pivot_z))
                    # CapeLayer translates two pixels rearward before X/Z/Y.
                    transform = rotation_x(x_angle) @ rotation_z(z_angle) @ rotation_y(y_angle)
                    poses.extend(local @ transform.T + np.array((0, 0, 2.0)))
    return [("cape:allClampedRecoveryPoses",
             aabb_enclosure(player_model_to_eye(poses)))]


WEARABLE_PARTS = tuple(armor_parts() + elytra_parts() + cape_parts())


def raw_trace_lines(path, hasher):
    with path.open("rb") as source:
        if path.suffix == ".zst":
            reader = zstandard.ZstdDecompressor().stream_reader(source)
        elif path.suffix == ".gz":
            import gzip
            reader = gzip.GzipFile(fileobj=source)
        else:
            reader = source
        with reader:
            for line in io.BufferedReader(reader):
                hasher.update(line)
                yield line


def spatial_index(triangles):
    lows, highs = triangles.min(axis=1), triangles.max(axis=1)
    grid = {}
    for index, (low, high) in enumerate(zip(lows, highs)):
        ranges = (range(a, b + 1) for a, b in zip(
            np.floor(low / 2).astype(int), np.floor(high / 2).astype(int)))
        for cell in itertools.product(*ranges):
            grid.setdefault(cell, []).append(index)
    return lows, highs, grid


def check_track(points, axes, origin, route_state, margin):
    world = points @ axes + origin
    low, high = world.min(axis=0), world.max(axis=0)
    padding = margin * 4
    ranges = (range(a, b + 1) for a, b in zip(
        np.floor((low - padding) / 2).astype(int),
        np.floor((high + padding) / 2).astype(int)))
    indices = np.array(sorted({index for cell in itertools.product(*ranges)
                               for index in route_state["grid"].get(cell, [])}), dtype=int)
    if not len(indices):
        return padding, None
    lows, highs = route_state["lows"], route_state["highs"]
    gaps = np.linalg.norm(np.maximum(
        0, np.maximum(low - highs[indices], lows[indices] - high)), axis=1)
    candidates = np.flatnonzero(gaps < padding)
    if candidates.size:
        gaps[candidates] = MESH.separation(
            world, axes, route_state["triangles"][indices[candidates]])
    best = int(np.argmin(gaps))
    index = int(indices[best])
    return min(padding, float(gaps[best])), route_state["labels"][index]


def body_boxes(resources, body, family):
    blocks = body["blocks"]
    if family == "LEGACY" and body["role"] == "carrier":
        blocks = [{**block, "block": "fourthdimension:legacy_axle"}
                  if block["block"] == "fourthdimension:carrier_axle" else block
                  for block in blocks]
    return [box for block in blocks for box in resources.boxes(block)]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("base_jar", type=Path)
    parser.add_argument("minecraft_jar", type=Path)
    parser.add_argument("frames", type=Path)
    parser.add_argument("trace", type=Path)
    parser.add_argument("baseline_report", type=Path)
    parser.add_argument("--margin", type=float, default=.02)
    args = parser.parse_args()
    if not math.isfinite(args.margin) or not 0 < args.margin <= .05:
        parser.error("Margin must be finite and in (0, .05]")

    inputs = {name: getattr(args, name) for name in
              ("base_jar", "minecraft_jar", "frames", "trace", "baseline_report")}
    inputs["mesh_verifier"] = MESH_PATH
    inputs["wearable_verifier"] = Path(__file__)
    initial = {name: digest(path) for name, path in inputs.items()}
    if initial["base_jar"] != MESH.BASE_PIN or initial["minecraft_jar"] != MESH.MINECRAFT_PIN:
        parser.error("Requires the same inspected Coasters and Minecraft artifacts as the baseline audit")
    baseline = json.loads(args.baseline_report.read_text())
    if (baseline["baseSha256"] != initial["base_jar"]
            or baseline["minecraftSha256"] != initial["minecraft_jar"]
            or baseline["framesSha256"] != initial["frames"]
            or baseline["verifierSha256"] != initial["mesh_verifier"]
            or not all(route["passes"] and not route["uncertifiedPairs"]
                       for route in baseline["routes"])):
        parser.error("Baseline report does not certify these exact inputs with the current mesh verifier")

    routes = json.loads(args.frames.read_text())
    baseline_routes = {(route["family"], route["cars"]): route for route in baseline["routes"]}
    states = {}
    for route in routes:
        key = (route["family"], route["carCount"])
        if key not in baseline_routes:
            parser.error("Baseline report omits a supplied route")
        triangles, labels = MESH.track_mesh(route)
        lows, highs, grid = spatial_index(triangles)
        states[route["variant"]] = {
            "route": route, "triangles": triangles, "labels": labels,
            "lows": lows, "highs": highs, "grid": grid,
            "samples": 0, "complete": False, "riders": None, "mounts": None,
            "trackChecks": 0, "rackChecks": 0, "rackTrackChecks": 0,
            "trackMinimum": math.inf, "rackMinimum": math.inf,
            "rackTrackMinimum": math.inf,
            "trackClosest": None, "rackClosest": None, "rackTrackClosest": None,
            "failures": [],
            "partTrackMinimums": {label: math.inf for label, _ in WEARABLE_PARTS},
            "partRackMinimums": {label: math.inf for label, _ in WEARABLE_PARTS},
            "rackCache": {}, "boxes": {}}

    body_specs = {}
    raw_hasher = hashlib.sha256()
    with zipfile.ZipFile(args.base_jar) as base, zipfile.ZipFile(args.minecraft_jar) as minecraft:
        resources = MESH.Resources(base, minecraft)
        for raw_line in raw_trace_lines(args.trace, raw_hasher):
            entry = json.loads(raw_line)
            if entry["type"] == "body":
                body_specs[entry["id"]] = entry
                continue
            variant = entry.get("variant")
            if variant not in states:
                continue
            state = states[variant]
            route = state["route"]
            if entry["type"] == "complete":
                state["complete"] = entry["cars"] == route["carCount"]
                continue
            if entry["type"] != "sample":
                continue
            state["samples"] += 1
            pose_by_id = {pose["id"]: pose for pose in entry["poses"]}
            mounts = {rider["id"]: rider["body"] for rider in entry.get("riders", [])}
            if state["mounts"] is None:
                state["mounts"] = mounts
                state["riders"] = len(mounts)
            elif mounts != state["mounts"]:
                raise ValueError("Wearable audit observed a missing or remounted rider")
            # The production corrections in this checkpoint move carrier-rack
            # elements. Check their current resource geometry against track at
            # every retained pose instead of inheriting the old model hashes.
            for body_id in set(mounts.values()):
                body = body_specs[body_id]
                pose = pose_by_id[body_id]
                axes = np.array(pose["axes"])
                origin = np.array(pose["origin"])
                if body_id not in state["boxes"]:
                    state["boxes"][body_id] = body_boxes(resources, body, route["family"])
                for rack_points, rack_label in state["boxes"][body_id]:
                    if rack_label.startswith("fourthdimension:") and "_seat@" in rack_label:
                        continue
                    rack_track_gap, component = check_track(
                        rack_points, axes, origin, state, args.margin)
                    state["rackTrackChecks"] += 1
                    if rack_track_gap < state["rackTrackMinimum"]:
                        state["rackTrackMinimum"] = rack_track_gap
                        state["rackTrackClosest"] = {
                            "tick": entry["tick"], "body": body_id,
                            "rackComponent": rack_label, "trackComponent": component}
                    if rack_track_gap < args.margin and len(state["failures"]) < 50:
                        state["failures"].append({
                            "kind": "rackTrack", "gap": rack_track_gap,
                            "tick": entry["tick"], "body": body_id,
                            "rackComponent": rack_label, "trackComponent": component})
            for rider in entry.get("riders", []):
                if not (abs(float(rider["width"]) - .9) < 1e-5
                        and abs(float(rider["height"]) - .9) < 1e-5
                        and abs(float(rider["eyeHeight"]) - .765) < 1e-5):
                    raise ValueError("Wearable proxy requires the inspected occupied pig fixture")
                body_id = rider["body"]
                body = body_specs[body_id]
                if body["role"] != "carrier" or body_id not in pose_by_id:
                    raise ValueError("Wearable proxy rider is not on a sampled carrier")
                pose = pose_by_id[body_id]
                axes = np.array(pose["axes"])
                origin = np.array(pose["origin"])
                eye = np.array(rider["eye"]) + MESH.PLAYER_EYE_FROM_FIXTURE_EYE * axes[1]
                eye_local = (eye - origin) @ axes.T
                for label, points in WEARABLE_PARTS:
                    track_gap, component = check_track(points, axes, eye, state, args.margin)
                    state["trackChecks"] += 1
                    if track_gap < state["partTrackMinimums"][label]:
                        state["partTrackMinimums"][label] = track_gap
                    if track_gap < state["trackMinimum"]:
                        state["trackMinimum"] = track_gap
                        state["trackClosest"] = {"tick": entry["tick"], "body": body_id,
                                                   "wearable": label, "trackComponent": component}
                    if track_gap < args.margin and len(state["failures"]) < 50:
                        state["failures"].append({"kind": "track", "gap": track_gap,
                                                  "tick": entry["tick"], "body": body_id,
                                                  "wearable": label, "trackComponent": component})
                    local_points = points + eye_local
                    for rack_points, rack_label in state["boxes"][body_id]:
                        if rack_label.startswith("fourthdimension:") and "_seat@" in rack_label:
                            continue
                        cache_key = (body_id, tuple(np.round(eye_local, 9)), label, rack_label)
                        if cache_key not in state["rackCache"]:
                            state["rackCache"][cache_key] = MESH.boxes_separation(
                                local_points, np.eye(3), rack_points, np.eye(3))
                        rack_gap = state["rackCache"][cache_key]
                        state["rackChecks"] += 1
                        if rack_gap < state["partRackMinimums"][label]:
                            state["partRackMinimums"][label] = rack_gap
                        if rack_gap < state["rackMinimum"]:
                            state["rackMinimum"] = rack_gap
                            state["rackClosest"] = {"tick": entry["tick"], "body": body_id,
                                                    "wearable": label, "rackComponent": rack_label}
                        if rack_gap < args.margin and len(state["failures"]) < 50:
                            state["failures"].append({"kind": "rack", "gap": rack_gap,
                                                      "tick": entry["tick"], "body": body_id,
                                                      "wearable": label, "rackComponent": rack_label})

    if raw_hasher.hexdigest() != baseline["traceSha256"]:
        parser.error("Decompressed trace does not match the passing baseline report")
    results = []
    for state in states.values():
        route = state["route"]
        base = baseline_routes[(route["family"], route["carCount"])]
        expected_checks = base["samples"] * base["occupiedSeatsPerSample"] * len(WEARABLE_PARTS)
        if (not state["complete"] or state["samples"] != base["samples"]
                or state["riders"] != base["occupiedSeatsPerSample"]
                or state["trackChecks"] != expected_checks or state["rackChecks"] < expected_checks
                or state["rackTrackChecks"] == 0):
            raise ValueError("Wearable audit did not cover every baseline rider pose")
        results.append({
            "family": route["family"], "cars": route["carCount"],
            "samples": state["samples"], "occupiedSeatsPerSample": state["riders"],
            "wearablePartCount": len(WEARABLE_PARTS),
            "wearableTrackChecks": state["trackChecks"],
            "wearableRackChecks": state["rackChecks"],
            "wearableTrackMinimumSeparatingBound": state["trackMinimum"],
            "wearableTrackClosest": state["trackClosest"],
            "wearableRackMinimumSeparatingBound": state["rackMinimum"],
            "wearableRackClosest": state["rackClosest"],
            "rackTrackChecks": state["rackTrackChecks"],
            "rackTrackMinimumSeparatingBound": state["rackTrackMinimum"],
            "rackTrackClosest": state["rackTrackClosest"],
            "partTrackMinimumSeparatingBounds": state["partTrackMinimums"],
            "partRackMinimumSeparatingBounds": state["partRackMinimums"],
            "requiredGap": args.margin, "passes": not state["failures"],
            "uncertifiedPairs": state["failures"],
        })

    final = {name: digest(path) for name, path in inputs.items()}
    changed = [name for name in initial if initial[name] != final[name]]
    if changed:
        parser.error("Audit provenance changed during analysis: " + ", ".join(changed))
    output = {
        "baseSha256": initial["base_jar"], "minecraftSha256": initial["minecraft_jar"],
        "framesSha256": initial["frames"], "traceRawSha256": raw_hasher.hexdigest(),
        "traceArchiveSha256": initial["trace"], "baselineReportSha256": initial["baseline_report"],
        "meshVerifierSha256": initial["mesh_verifier"],
        "wearableVerifierSha256": initial["wearable_verifier"],
        "scope": "one-tick sampled vanilla 1.21.1 armor, cape and non-fall-flying elytra alternatives at every baseline recovery rider pose against original track and non-seat carrier rack geometry; excludes held items, non-armor head-slot renderers, arbitrary modded render layers, resource overrides, client observation and continuous trajectory",
        "modelBasis": "Minecraft 1.21.1 client LayerDefinitions, HumanoidArmorModel, CapeLayer and ElytraModel formulas reconstructed under the pinned minecraftSha256; Sable seated player scale/orientation reused from the passing baseline verifier",
        "wearableParts": [label for label, _ in WEARABLE_PARTS],
        "resourceHashes": resources.hashes,
        "provenanceCheck": "All compressed inputs, reports and verifier files matched before and after analysis; decompressed trace hash matches the passing baseline report.",
        "routes": results,
    }
    print(json.dumps(output, indent=2))
    if not all(result["passes"] for result in results):
        raise SystemExit("Sampled wearable clearance contains uncertified pairs")


if __name__ == "__main__":
    main()
