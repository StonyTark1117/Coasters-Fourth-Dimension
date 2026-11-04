#!/usr/bin/env python3
"""Certify recorded recovery physics substeps against native geometry.

The one-tick mesh report and Rapier half-step trace are independently preserved
inputs.  This verifier proves that every recorded full physics step is identical
to its one-tick pose, bounds each intervening half-step by rigid-body motion from
the nearest sampled pose, and deducts that motion from the sampled separating
bounds.  Rider-to-rack and player-to-rack distances are carrier-local, so rigid
carrier motion leaves them invariant.

This certifies every paired post-physics substep inside the sampled recovery
interval, not unanchored startup/teardown steps or the unexposed continuous
trajectory inside one discrete Rapier substep.
"""

import argparse
import gzip
import hashlib
import importlib.util
import itertools
import json
import math
from pathlib import Path
import zipfile

import numpy as np

ROOT = Path(__file__).resolve().parents[1]


def load_mesh_auditor():
    path = ROOT / "scripts" / "audit-recovery-mesh-trace.py"
    spec = importlib.util.spec_from_file_location("recovery_mesh_auditor", path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def require(condition, message):
    if not condition:
        raise ValueError(message)


def sha256(path):
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def corners(low, high):
    return np.array(list(itertools.product(*zip(low, high))), dtype=float)


def pose(entry):
    return np.array(entry["origin"]), np.array(entry["axes"])


def radius(points):
    return float(np.linalg.norm(points, axis=1).max())


def motion(first, second, local_radius):
    origin_a, axes_a = first
    origin_b, axes_b = second
    angle = math.acos(float(np.clip((np.trace(axes_a @ axes_b.T) - 1) / 2, -1, 1)))
    require(angle < .01, "Unexpected half-step rotation")
    return float(np.linalg.norm(origin_a - origin_b)) + 2 * local_radius * math.sin(angle / 2)


def same_pose(first, second):
    return np.array_equal(first[0], second[0]) and np.array_equal(first[1], second[1])


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("base_jar", type=Path)
    parser.add_argument("minecraft_jar", type=Path)
    parser.add_argument("mesh_report", type=Path)
    parser.add_argument("tick_trace", type=Path)
    parser.add_argument("substep_trace", type=Path)
    args = parser.parse_args()
    require(__debug__, "Run without Python -O")

    input_hashes = {name: sha256(path) for name, path in vars(args).items()}
    report = json.loads(args.mesh_report.read_text())
    with gzip.open(args.substep_trace, "rt") as stream:
        trace_header = json.loads(next(stream))
    require(trace_header["type"] == "trace", "Missing substep trace header")
    family = trace_header["family"]
    variant = trace_header["variant"]
    cars = int(trace_header["cars"])
    require(input_hashes["base_jar"] == report["baseSha256"], "Base JAR differs from mesh audit")
    require(input_hashes["minecraft_jar"] == report["minecraftSha256"],
            "Minecraft JAR differs from mesh audit")
    routes = report["routes"]
    matches = [route for route in routes if route["family"] == family and route["cars"] == cars]
    require(len(matches) == 1, f"Expected one coupled {family} route with {cars} cars")
    sampled = matches[0]
    require(sampled["passes"] and not sampled["uncertifiedPairs"], "Sampled clearance did not pass")
    required = float(sampled["requiredGap"])

    with gzip.open(args.tick_trace, "rt") as stream:
        tick_entries = [json.loads(line) for line in stream]
    body_specs = {entry["id"]: entry for entry in tick_entries if entry["type"] == "body"}
    samples = {
        entry["tick"]: {item["id"]: pose(item) for item in entry["poses"]}
        for entry in tick_entries
        if entry["type"] == "sample"
        and entry.get("variant") == variant
    }
    route_entries = [
        entry for entry in tick_entries
        if entry["type"] == "sample"
        and entry.get("variant") == variant
    ]
    tick_families = {entry.get("family") for entry in route_entries}
    require(len(tick_families) == 1, "Variant maps to multiple telemetry families")
    tick_family = tick_families.pop()
    require(len(samples) == sampled["samples"] >= 5000, "Sample/report count differs")
    ids = set(samples[min(samples)])
    require(all(set(sample) == ids for sample in samples.values()), "Tick body set changed")

    mesh = load_mesh_auditor()
    require(sha256(ROOT / "scripts" / "audit-recovery-mesh-trace.py") == report["verifierSha256"],
            "Mesh auditor differs from sampled report")
    with zipfile.ZipFile(args.base_jar) as base, zipfile.ZipFile(args.minecraft_jar) as minecraft:
        resources = mesh.Resources(base, minecraft)
        wheel_radius = max(radius(points) for points, _ in resources.wheel_envelopes())
        body_radius = {}
        for uid in ids:
            body = body_specs[uid]
            boxes = [points for block in body["blocks"] for points, _ in resources.boxes(block)]
            if body["role"] in ("chassis", "ordinary"):
                boxes += [points for points, _ in resources.model_boxes(
                    mesh.BOTTOM_MODEL, "nativeLowerBearing")]
            body_radius[uid] = max(map(radius, boxes))
    for name, digest in resources.hashes.items():
        require(report["resourceHashes"].get(name) == digest,
                "Rendered resource differs from mesh audit: " + name)

    rider_radius = {uid: 0.0 for uid in ids}
    player_radius = {uid: 0.0 for uid in ids}
    player_low = np.array(sampled["playerMeshLocalBoundsFromEye"]["minimum"])
    player_high = np.array(sampled["playerMeshLocalBoundsFromEye"]["maximum"])
    eye_delta = float(sampled["playerMeshEyeOffsetFromFixtureRider"])
    for entry in route_entries:
        current = samples[entry["tick"]]
        for rider in entry["riders"]:
            uid = rider["body"]
            require(uid in current and body_specs[uid]["role"] == "carrier", "Wrong rider carrier")
            origin, axes = current[uid]
            local_eye = (np.array(rider["eye"]) - origin) @ axes.T
            width, height, eye_height = (float(rider[key]) for key in ("width", "height", "eyeHeight"))
            local_rider = corners(
                (-width / 2, -eye_height, -width / 2),
                (width / 2, height - eye_height, width / 2),
            ) + local_eye
            local_player = corners(player_low, player_high) + local_eye + np.array((0, eye_delta, 0))
            rider_radius[uid] = max(rider_radius[uid], radius(local_rider))
            player_radius[uid] = max(player_radius[uid], radius(local_player))

    first_tick = min(samples)
    offset = None
    full_matches = set()
    maximum_motion = {"body": 0.0, "wheel": 0.0, "rider": 0.0, "player": 0.0}
    worst = {}
    previous_step, previous_time, steps, audited_steps, complete = 0, None, 0, 0, False
    with gzip.open(args.substep_trace, "rt") as stream:
        header = json.loads(next(stream))
        require(header == trace_header and set(header["bodyIds"]) == ids,
                "Substep trace header/body set differs")
        for line in stream:
            entry = json.loads(line)
            if entry["type"] == "complete":
                require(entry["steps"] == steps, "Completion count differs")
                complete = True
                break
            require(entry["type"] == "substep" and entry["step"] == previous_step + 1,
                    "Missing or out-of-order substep")
            require(entry["timeStep"] == .025 and entry["partialPhysicsTick"] in (.5, 1.0),
                    "Unexpected substep timing")
            require(previous_time is None or entry["gameTime"] in (previous_time, previous_time + 1),
                    "Substep game-time gap")
            previous_step, previous_time = entry["step"], entry["gameTime"]
            steps += 1
            current = {item["id"]: pose(item) for item in entry["poses"]}
            require(set(current) == ids, "Missing substep body")
            if offset is None and entry["partialPhysicsTick"] == 1.0:
                if all(same_pose(current[uid], samples[first_tick][uid]) for uid in ids):
                    offset = entry["gameTime"] - first_tick
            if offset is None:
                continue
            tick = entry["gameTime"] - offset
            if tick < first_tick or tick > max(samples):
                continue
            audited_steps += 1
            if entry["partialPhysicsTick"] == 1.0:
                require(tick in samples and all(same_pose(current[uid], samples[tick][uid]) for uid in ids),
                        "Full substep differs from tick sample")
                full_matches.add(tick)
            anchors = [samples[anchor] for anchor in (tick - 1, tick) if anchor in samples]
            require(anchors, "Unanchored substep")
            for uid in ids:
                def nearest(local_radius):
                    return min(motion(anchor[uid], current[uid], local_radius) for anchor in anchors)
                values = {"body": nearest(body_radius[uid])}
                if body_specs[uid]["role"] in ("chassis", "ordinary"):
                    values["wheel"] = nearest(wheel_radius)
                if rider_radius[uid]:
                    values["rider"] = nearest(rider_radius[uid])
                    values["player"] = nearest(player_radius[uid])
                for kind, value in values.items():
                    if value > maximum_motion[kind]:
                        maximum_motion[kind] = value
                        worst[kind] = {"tick": tick, "step": steps, "body": uid}

    require(complete and steps > 2 * len(samples), "Incomplete substep trace")
    require(audited_steps == 2 * len(samples) - 1,
            f"Sampled interval has unexpected substep count: {audited_steps}")
    require(full_matches == set(samples), "Not every tick matched a full physics step")
    sampled_gaps = {
        "body": float(sampled["minimumSeparatingBound"]),
        "wheel": float(sampled["wheelMinimumSeparatingBound"]),
        "rider": float(sampled["riderMinimumSeparatingBound"]),
        "player": float(sampled["playerMeshMinimumSeparatingBound"]),
    }
    lower_bounds = {kind: sampled_gaps[kind] - maximum_motion[kind] for kind in sampled_gaps}
    require(all(value >= required for value in lower_bounds.values()), "Substep track clearance failed")
    neighbor_lower = float(sampled["neighborMinimumSeparatingBound"]) - 2 * max(maximum_motion.values())
    require(neighbor_lower >= required, "Substep neighbor clearance failed")
    rack_bounds = {
        "riderRack": float(sampled["riderRackMinimumSeparatingBound"]),
        "playerRack": float(sampled["playerRackMinimumSeparatingBound"]),
    }
    require(all(value >= required for value in rack_bounds.values()), "Carrier-local rack clearance failed")
    require({name: sha256(path) for name, path in vars(args).items()} == input_hashes,
            "Inputs changed during audit")

    print(json.dumps({
        "passes": True,
        "scope": f"Every paired Rapier post-physics half/full step inside the sampled occupied coupled-{family} recovery interval; unanchored startup/teardown steps and the unexposed continuous trajectory inside a discrete substep are excluded.",
        "family": family,
        "tickFamily": tick_family,
        "variant": variant,
        "cars": cars,
        "inputHashes": input_hashes,
        "traceSteps": steps,
        "auditedSteps": audited_steps,
        "substepsPerTick": 2,
        "gameTimeOffset": offset,
        "matchedTickSamples": len(full_matches),
        "maximumMotionFromNearestTick": maximum_motion,
        "worstMotion": worst,
        "sampledSeparatingBounds": sampled_gaps,
        "substepSeparatingLowerBounds": lower_bounds,
        "carrierLocalInvariantBounds": rack_bounds,
        "neighborSeparatingLowerBound": neighbor_lower,
        "requiredGap": required,
    }, indent=2))


if __name__ == "__main__":
    main()
