#!/usr/bin/env python3
"""Audit the instrumented coupled Arrow recovery physics substeps against Styles meshes."""

import argparse
import gzip
import hashlib
import importlib.util
import itertools
import json
import math
from pathlib import Path
import sys
import zipfile

import numpy as np

ROOT = Path(__file__).resolve().parents[1]


def module(name, filename):
    spec = importlib.util.spec_from_file_location(name, ROOT / 'scripts' / filename)
    result = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(result)
    return result


mesh = module('mesh', 'audit-recovery-mesh-trace.py')
styles = module('styles', 'audit-recovery-styles.py')
inputs = module('inputs', 'verify-recovery-decoration-inputs.py')
route_geometry = module('route_geometry', 'verify-recovery-bank-frames.py')

FIXTURE_PINS = {
    'src/main/java/dev/fourthdimension/gametest/BankedRecoveryTrainGameTests.java':
        '1192286e979801504acf758427aca5e0cbb9a739486f45d49124b6e1fcac795c',
    'src/main/java/dev/fourthdimension/gametest/RecoverySubstepTrace.java':
        '4ecafb76570698431823479dcde77b35f5accd409f6c1058e4643a1f7b0c0370',
}
RAPIER_PIN = 'aabd5ed08349821989cd1806122918cbec3c9dfafe4584e0ab012fa015b7749d'


def require(ok, message):
    if not ok:
        raise ValueError(message)


def sha256(path):
    h = hashlib.sha256()
    with path.open('rb') as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(chunk)
    return h.hexdigest()


def radius(points):
    return float(np.linalg.norm(points, axis=1).max())


def corners(low, high):
    return np.array(list(itertools.product(*zip(low, high))), dtype=float)


def pose(entry):
    return np.array(entry['origin']), np.array(entry['axes'])


def motion(first, second, local_radius):
    origin_a, axes_a = first
    origin_b, axes_b = second
    angle = math.acos(float(np.clip((np.trace(axes_a @ axes_b.T) - 1) / 2, -1, 1)))
    require(angle < .01, 'Unexpected half-tick rotation')
    return float(np.linalg.norm(origin_a - origin_b)) + 2 * local_radius * math.sin(angle / 2)


def same_pose(first, second):
    return np.array_equal(first[0], second[0]) and np.array_equal(first[1], second[1])


def substeps(path, samples, tick_entries, bodies, resources, trace_result):
    ids = set(samples[next(iter(samples))])
    first_tick = min(samples)
    offset = None
    wheel_radius = max(radius(points) for points, _ in resources.wheel_envelopes())
    body_radius = {}
    for uid in ids:
        body = bodies[uid]
        boxes = [points for block in body['blocks'] for points, _ in resources.boxes(block)]
        if body['role'] in ('chassis', 'ordinary'):
            boxes += [points for points, _ in resources.model_boxes(mesh.BOTTOM_MODEL, 'nativeLowerBearing')]
        body_radius[uid] = max(map(radius, boxes))
    rider_radius, player_radius = {}, {}
    player_low, player_high = mesh.vanilla_player_mesh_bounds()
    for uid in ids:
        rider_radius[uid] = player_radius[uid] = 0.
    for entry in tick_entries:
        current = samples[entry['tick']]
        for rider in entry['riders']:
            uid = rider['body']
            require(uid in current and bodies[uid]['role'] == 'carrier', 'Wrong mounted carrier')
            origin, axes = current[uid]
            local_eye = (np.array(rider['eye']) - origin) @ axes.T
            width, height, eye_height = (float(rider[key]) for key in ('width', 'height', 'eyeHeight'))
            require(abs(width - .9) < 1e-5 and abs(height - .9) < 1e-5
                    and abs(eye_height - .765) < 1e-5, 'Unexpected fixture rider dimensions')
            local_rider = corners((-width / 2, -eye_height, -width / 2),
                                  (width / 2, height - eye_height, width / 2)) + local_eye
            local_player = corners(player_low, player_high) + local_eye + \
                mesh.PLAYER_EYE_FROM_FIXTURE_EYE * np.array((0., 1., 0.))
            rider_radius[uid] = max(rider_radius[uid], radius(local_rider))
            player_radius[uid] = max(player_radius[uid], radius(local_player))
    with gzip.open(path, 'rt') as stream:
        first = json.loads(next(stream))
        require(first['type'] == 'trace' and set(first['bodyIds']) == ids, 'Wrong substep bodies')
        previous_step, previous_time, steps, completed = 0, None, 0, False
        full_tick_matches = set()
        max_motion = {'body': 0., 'wheel': 0., 'rider': 0., 'player': 0.}
        worst = {}
        for line in stream:
            entry = json.loads(line)
            if entry['type'] == 'complete':
                require(entry['steps'] == steps, 'Substep completion count differs')
                completed = True
                break
            require(entry['type'] == 'substep' and entry['step'] == previous_step + 1,
                    'Missing or out-of-order physics substep')
            require(entry['timeStep'] == .025 and entry['partialPhysicsTick'] in (.5, 1.),
                    'Unexpected Rapier substep timing')
            require(previous_time is None or entry['gameTime'] in (previous_time, previous_time + 1),
                    'Substep game-time gap')
            require(len(entry['poses']) == len(ids)
                    and {item['id'] for item in entry['poses']} == ids, 'Missing substep body')
            previous_step, previous_time = entry['step'], entry['gameTime']
            steps += 1
            current = {item['id']: pose(item) for item in entry['poses']}
            if offset is None and entry['partialPhysicsTick'] == 1.:
                if all(same_pose(current[uid], samples[first_tick][uid]) for uid in ids):
                    offset = entry['gameTime'] - first_tick
            if offset is None:
                continue
            tick = entry['gameTime'] - offset
            if tick < first_tick or tick > max(samples):
                continue
            if entry['partialPhysicsTick'] == 1.:
                require(tick in samples and all(same_pose(current[uid], samples[tick][uid]) for uid in ids),
                        'Full substep does not match native tick sample')
                full_tick_matches.add(tick)
            anchors = [samples[t] for t in (tick - 1, tick) if t in samples]
            require(anchors, 'Unanchored physics substep')
            for uid in ids:
                def nearest(local_radius):
                    return min(motion(anchor[uid], current[uid], local_radius) for anchor in anchors)
                values = {'body': nearest(body_radius[uid])}
                if bodies[uid]['role'] in ('chassis', 'ordinary'):
                    values['wheel'] = nearest(wheel_radius)
                if rider_radius[uid]:
                    values['rider'] = nearest(rider_radius[uid])
                    values['player'] = nearest(player_radius[uid])
                for kind, value in values.items():
                    if value > max_motion[kind]:
                        max_motion[kind] = value
                        worst[kind] = {'tick': tick, 'step': steps, 'body': uid}
        require(completed and steps > 2 * len(samples), 'Incomplete substep trace')
        require(full_tick_matches == set(samples), 'Not every sampled tick matched a physics substep')
        gaps = {
            'body': trace_result['minimumSeparatingBound'],
            'wheel': trace_result['wheelMinimumSeparatingBound'],
            'rider': trace_result['riderMinimumSeparatingBound'],
            'player': trace_result['playerMeshMinimumSeparatingBound'],
        }
        lower = {kind: gaps[kind] - max_motion[kind] for kind in gaps}
        require(all(value >= trace_result['requiredGap'] for value in lower.values()),
                'Physics substep pose exhausted sampled clearance')
        require(trace_result['neighborMinimumSeparatingBound'] - 2 * max(max_motion.values())
                >= trace_result['requiredGap'], 'Physics substep neighbor clearance failed')
        return {'steps': steps, 'substepsPerTick': 2, 'gameTimeOffset': offset,
                'matchedTickSamples': len(full_tick_matches), 'maximumMotionFromNearestTick': max_motion,
                'worstMotion': worst, 'substepSeparatingLowerBounds': lower}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ('base', 'minecraft', 'styles', 'rapier', 'frames', 'tick_trace', 'substep_trace'):
        parser.add_argument(name, type=Path)
    args = parser.parse_args()
    require(sys.flags.optimize == 0, 'Run without Python -O')
    paths = {name: getattr(args, name) for name in ('base', 'minecraft', 'styles', 'rapier',
                                                    'frames', 'tick_trace', 'substep_trace')}
    hashes = {name: sha256(path) for name, path in paths.items()}
    require(hashes['base'] == mesh.BASE_PIN and hashes['minecraft'] == mesh.MINECRAFT_PIN
            and hashes['styles'] == styles.STYLES_PIN and hashes['rapier'] == RAPIER_PIN,
            'Inspected dependency JAR changed')
    source_pins = {**mesh.SOURCE_PINS, **styles.SOURCE_PINS, **FIXTURE_PINS}
    for name, pin in source_pins.items():
        require(sha256(ROOT / name) == pin, 'Source changed: ' + name)
    routes = styles.read_json(args.frames)
    inputs.verify(routes)
    pairing = inputs.verify_trace(routes, args.tick_trace)
    route_geometry.verify(args.frames, train_routes=True)
    arrow = [route for route in routes if route['family'] == 'ARROW']
    require(len(arrow) == 1 and arrow[0]['carCount'] == 2, 'Not a two-car Arrow route')
    route = arrow[0]
    with args.tick_trace.open() as stream:
        entries = [json.loads(line) for line in stream]
    bodies = {entry['id']: entry for entry in entries if entry['type'] == 'body'}
    samples = {entry['tick']: {p['id']: pose(p) for p in entry['poses']}
               for entry in entries if entry['type'] == 'sample'
               and entry.get('variant') == route['variant']}
    tick_entries = [entry for entry in entries if entry['type'] == 'sample'
                    and entry.get('variant') == route['variant']]
    require(len(samples) > 5000, 'Incomplete Arrow tick trace')
    with zipfile.ZipFile(args.base) as base, zipfile.ZipFile(args.minecraft) as minecraft, \
            zipfile.ZipFile(args.styles) as provider:
        resources = mesh.Resources(base, minecraft)
        plans = {
            'arrow_4d': {section['rail']: 'arrow_4d' for section in route['sections']},
            'arrow_4d_dbl_spine': {section['rail']: 'arrow_4d_dbl_spine' for section in route['sections']},
            'transition': {section['rail']: style for section, style in zip(route['sections'],
                              ('original', 'arrow_4d', 'arrow_4d_dbl_spine', 'original', 'arrow_4d'))},
        }
        models, model_hashes = styles.models(provider, {'arrow_4d', 'arrow_4d_dbl_spine'})
        results = {}
        for name, plan in plans.items():
            surface, counts = styles.surface(route, plan, models)
            sampled = mesh.audit(route, entries, resources, .02, surface=surface)
            require(sampled['passes'], 'Sampled Styles clearance failed: ' + name)
            physics = substeps(args.substep_trace, samples, tick_entries, bodies, resources, sampled)
            results[name] = {'sampled': sampled, 'substeps': physics, 'geometryCounts': counts}
    require({name: sha256(path) for name, path in paths.items()} == hashes,
            'Inputs changed during analysis')
    print(json.dumps({'passes': True, 'scope': 'Every recorded Rapier post-physics substep on the coupled occupied Arrow recovery route, checked against prospective default Styles surfaces and vanilla player envelopes. The native solver does not expose a continuous trajectory inside a discrete substep.',
                      'inputHashes': hashes, 'sourceHashes': source_pins,
                      'providerResourceHashes': model_hashes,
                      'tracePairing': pairing, 'results': results}, indent=2))


if __name__ == '__main__':
    main()
