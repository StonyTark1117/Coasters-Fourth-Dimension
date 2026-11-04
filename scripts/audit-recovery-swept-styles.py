#!/usr/bin/env python3
"""Certify clearance throughout interpolation between retained native tick poses.

The certificate uses a rigid body's endpoint translation plus its total angular
change as an upper bound on movement of every point in a local model envelope.
It is a sweep of Sable's between-snapshot interpolation, not a physics-substep
or network extrapolation claim. Historical per-tick reports remain immutable.
"""

import gzip
import hashlib
import itertools
import json
import math
import copy
import zipfile
from pathlib import Path

import numpy as np

ROOT = Path(__file__).resolve().parents[1]
EVIDENCE = ROOT / 'docs/evidence/0.9'
REPORTS = EVIDENCE / 'fd401-banked-player-clearance-a5df9b8'
INPUTS = EVIDENCE / 'fd402-isolated-base-ff7c831/native-exports'
SABLE = Path('/home/braydon/.gradle/caches/modules-2/files-2.1/dev.ryanhcode.sable/sable-neoforge-1.21.1/2.0.3/f3ba9a497122ba29d9e94ebe6f9a3a3aaa5fa756/sable-neoforge-1.21.1-2.0.3.jar')
SABLE_COMPANION = Path('/home/braydon/.gradle/caches/modules-2/files-2.1/dev.ryanhcode.sable-companion/sable-companion-common-1.21.1/1.6.0/370be7db705926176e05c0215ed676150c45daae/sable-companion-common-1.21.1-1.6.0.jar')
SABLE_PIN = 'da6c3b66238586603d1dcaa2afb012d36815fbce0a2d5938fbb2936701d42279'
SABLE_COMPANION_PIN = '873633e35046e3761b277ff8a1ecad0d55d9a3014fa81a0b084c9aecba1f3bed'


def load(name, file):
    import importlib.util
    spec = importlib.util.spec_from_file_location(name, ROOT / 'scripts' / file)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


mesh = load('mesh', 'audit-recovery-mesh-trace.py')
styles = load('styles', 'audit-recovery-styles.py')


def require(condition, message):
    if not condition:
        raise ValueError(message)


def digest(path):
    h = hashlib.sha256()
    with path.open('rb') as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(chunk)
    return h.hexdigest()


def payload_digest(path):
    h = hashlib.sha256()
    with gzip.open(path, 'rb') as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(chunk)
    return h.hexdigest()


def corners(low, high):
    return np.array(list(itertools.product(*zip(low, high))), dtype=float)


def radius(points):
    return float(np.linalg.norm(points, axis=1).max())


def motion(previous, current, radius):
    o0, a0 = previous
    o1, a1 = current
    translation = float(np.linalg.norm(o1 - o0))
    cosine = float(np.clip((np.trace(a1 @ a0.T) - 1) / 2, -1, 1))
    angle = math.acos(cosine)
    require(angle < .01, 'Unexpected one-tick rotation')
    # Every nlerp orientation stays on the short quaternion arc; its point
    # displacement from the first orientation is at most 2*r*sin(angle/2).
    return translation + 2 * radius * math.sin(angle / 2)


def near_track_minimum(points, pose, triangles, tri_low, tri_high, margin):
    axes, origin = pose
    world = points @ axes + origin
    low, high = world.min(axis=0), world.max(axis=0)
    aabb = np.linalg.norm(np.maximum(0, np.maximum(low - tri_high, tri_low - high)), axis=1)
    candidates = np.flatnonzero(aabb < margin)
    if not len(candidates):
        return margin
    return min(margin, float(mesh.separation(world, axes, triangles[candidates]).min()))


def audit_trace(trace_path, report, resources, wheel_radius):
    route = report['result']
    margin = float(route['requiredGap'])
    global_gap = float(route['minimumSeparatingBound'])
    player_gap = float(route['playerMeshMinimumSeparatingBound'])
    rider_gap = float(route['riderMinimumSeparatingBound'])
    rack_gap = float(route['riderRackMinimumSeparatingBound'])
    neighbor_gap = float(route['neighborMinimumSeparatingBound'])
    require(route['passes'] and margin == .02 and route['cars'] in (1, 2), 'Uncertified sampled report')
    require(min(global_gap, player_gap, rider_gap, rack_gap, neighbor_gap) > margin,
            'Sampled margin is already insufficient')

    bodies, previous, eyes = {}, {}, {}
    max_motion = {'body': 0., 'wheel': 0., 'rider': 0., 'player': 0.}
    worst = {}
    exceptions = []
    max_eye_drift = 0.
    samples = 0
    last_tick = None
    player_low, player_high = mesh.vanilla_player_mesh_bounds()
    with gzip.open(trace_path, 'rt') as stream:
        for line in stream:
            entry = json.loads(line)
            if entry['type'] == 'body':
                boxes = [points for block in entry['blocks'] for points, _ in resources.boxes(block)]
                if entry['role'] in ('chassis', 'ordinary'):
                    boxes += [points for points, _ in resources.model_boxes(mesh.BOTTOM_MODEL, 'nativeLowerBearing')]
                bodies[entry['id']] = {'role': entry['role'], 'boxes': boxes,
                                       'radius': max(map(radius, boxes))}
            if entry['type'] != 'sample' or entry.get('variant') != 'arrow_four_rail_v1':
                continue
            tick = entry['tick']
            require(last_tick is None or tick == last_tick + 1, 'Native sample gap')
            last_tick = tick
            samples += 1
            current = {p['id']: (np.array(p['origin']), np.array(p['axes'])) for p in entry['poses']}
            for uid, pose in current.items():
                require(uid in bodies, 'Unknown native body')
                body = bodies[uid]
                if uid not in previous:
                    continue
                bound = motion(previous[uid], pose, body['radius'])
                if bound > max_motion['body']:
                    max_motion['body'] = bound
                    worst['body'] = [tick, uid]
                if bound > global_gap - margin:
                    exceptions.append({'tick': tick, 'body': uid, 'bound': bound,
                                       'role': body['role']})
                if body['role'] in ('chassis', 'ordinary'):
                    wheel_bound = motion(previous[uid], pose, wheel_radius)
                    if wheel_bound > max_motion['wheel']:
                        max_motion['wheel'] = wheel_bound
                        worst['wheel'] = [tick, uid]
            for rider in entry['riders']:
                uid = rider['body']
                require(uid in current and bodies[uid]['role'] == 'carrier', 'Unpaired rider')
                origin, axes = current[uid]
                local_eye = (np.array(rider['eye']) - origin) @ axes.T
                old_eye = eyes.get(rider['id'])
                if old_eye is not None:
                    max_eye_drift = max(max_eye_drift, float(np.linalg.norm(local_eye - old_eye)))
                eyes[rider['id']] = local_eye
                if uid not in previous:
                    continue
                width, height, eye_height = (float(rider[k]) for k in ('width', 'height', 'eyeHeight'))
                require(abs(width - .9) < 1e-5 and abs(height - .9) < 1e-5
                        and abs(eye_height - .765) < 1e-5, 'Unexpected fixture rider')
                rider_points = corners((-width / 2, -eye_height, -width / 2),
                                       (width / 2, height - eye_height, width / 2)) + local_eye
                player_points = corners(player_low, player_high) + local_eye + \
                    mesh.PLAYER_EYE_FROM_FIXTURE_EYE * np.array([0., 1., 0.])
                max_motion['rider'] = max(max_motion['rider'], motion(previous[uid], current[uid], radius(rider_points)))
                max_motion['player'] = max(max_motion['player'], motion(previous[uid], current[uid], radius(player_points)))
            previous = current
    require(samples == route['samples'], 'Report and trace sample counts differ')
    require(max_eye_drift < 1e-8, 'Rider local eye moved within carrier')
    require(max_motion['wheel'] < global_gap - margin, 'Wheel sweep exceeds sampled margin')
    require(max_motion['rider'] < rider_gap - margin, 'Rider sweep exceeds sampled margin')
    require(max_motion['player'] < player_gap - margin, 'Player sweep exceeds sampled margin')
    require(2 * max(max_motion.values()) < neighbor_gap - margin, 'Neighbor sweep exceeds sampled margin')
    require(max_eye_drift < rack_gap - margin, 'Rider/rack local gap changed')
    return {'samples': samples, 'globalSampledGap': global_gap,
            'maximumMotion': max_motion, 'worst': worst,
            'localEyeDrift': max_eye_drift, 'exceptions': exceptions}


def check_exceptions(trace_path, frames_path, report, exceptions, resources, styles_jar):
    if not exceptions:
        return {'count': 0, 'minimumSweptLowerBound': None}
    routes = styles.read_json(frames_path)
    route = next(r for r in routes if r['family'] == 'ARROW')
    plan = report['prospectiveStylePlan']
    used = set(plan.values()) - {'original'}
    with zipfile.ZipFile(styles_jar) as archive:
        models, _ = styles.models(archive, used)
    track, _ = styles.surface(route, plan, models)
    triangles = track[0]
    tri_low, tri_high = triangles.min(axis=1), triangles.max(axis=1)
    wanted = {(item['tick'] - 1, item['body']) for item in exceptions}
    boxes = {}
    poses = {}
    with gzip.open(trace_path, 'rt') as stream:
        for line in stream:
            entry = json.loads(line)
            if entry['type'] == 'body' and any(uid == entry['id'] for _, uid in wanted):
                boxes[entry['id']] = [points for block in entry['blocks']
                                      for points, _ in resources.boxes(block)]
            if entry['type'] == 'sample':
                for pose in entry['poses']:
                    if (entry['tick'], pose['id']) in wanted:
                        poses[entry['tick'], pose['id']] = (np.array(pose['axes']), np.array(pose['origin']))
    checked = []
    for item in exceptions:
        require(item['role'] == 'carrier', 'Unexpected non-carrier local exception')
        key = (item['tick'] - 1, item['body'])
        require(key in poses and item['body'] in boxes, 'Missing exception pose/body')
        search = item['bound'] + .02 + .01
        local = min(near_track_minimum(points, poses[key], triangles, tri_low, tri_high, search)
                    for points in boxes[item['body']])
        require(local - item['bound'] >= .02, 'Local swept clearance failed')
        checked.append({'tickInterval': [item['tick'] - 1, item['tick']],
                        'body': item['body'], 'sampledLocalBound': local,
                        'motionBound': item['bound'], 'sweptLowerBound': local - item['bound']})
    return {'count': len(checked),
            'minimumSweptLowerBound': min(item['sweptLowerBound'] for item in checked),
            'maximumMotionBound': max(item['motionBound'] for item in checked),
            'firstInterval': checked[0]['tickInterval'],
            'lastInterval': checked[-1]['tickInterval']}


def main():
    config = json.loads((REPORTS / 'config.json').read_text())
    require(digest(SABLE) == SABLE_PIN and digest(SABLE_COMPANION) == SABLE_COMPANION_PIN,
            'Inspected Sable interpolation implementation changed')
    jars = {key: Path(value) for key, value in config['jars'].items()}
    for name, path in jars.items():
        require(digest(path) == config['dependencyHashes'][name], 'Dependency changed: ' + name)
    require(digest(ROOT / 'scripts/audit-recovery-mesh-trace.py') == config['toolHashes']['audit-recovery-mesh-trace.py'],
            'Sampled auditor changed')
    require(digest(ROOT / 'scripts/audit-recovery-styles.py') == config['toolHashes']['audit-recovery-styles.py'],
            'Styles auditor changed')
    with zipfile.ZipFile(jars['base']) as base, zipfile.ZipFile(jars['minecraft']) as minecraft:
        resources = mesh.Resources(base, minecraft)
        wheel_radius = max(radius(points) for points, _ in resources.wheel_envelopes())
        results = {}
        input_hashes = {}
        for name, prefix in (('single', 'recovery-bank'), ('train', 'recovery-bank-train')):
            trace = INPUTS / (prefix + '-mesh-trace.jsonl.gz')
            frames = INPUTS / (prefix + '-native-frames.json.gz')
            trace_hash, frames_hash = payload_digest(trace), payload_digest(frames)
            input_hashes[name] = {'traceArchive': digest(trace), 'framesArchive': digest(frames),
                                  'tracePayload': trace_hash, 'framesPayload': frames_hash}
            scanned = None
            sampled_metrics = None
            for style in (('arrow_4d', 'arrow_4d_dbl_spine') if name == 'single'
                          else ('arrow_4d', 'arrow_4d_dbl_spine', 'transition')):
                report = json.loads((REPORTS / (name + '-' + style + '.json')).read_text())
                for path, pin in report['sourceHashes'].items():
                    require(digest(ROOT / path) == pin, 'Candidate source changed: ' + path)
                require(report['inputHashes']['trace'] == trace_hash
                        and report['inputHashes']['frames'] == frames_hash,
                        'Retained native input differs from sampled report')
                require(report['inputHashes']['styles_jar'] == config['dependencyHashes']['styles'],
                        'Styles dependency differs from sampled report')
                metrics = {key: value for key, value in report['result'].items()
                           if key in ('samples', 'requiredGap', 'minimumSeparatingBound',
                                      'riderMinimumSeparatingBound', 'playerMeshMinimumSeparatingBound',
                                      'riderRackMinimumSeparatingBound', 'neighborMinimumSeparatingBound')}
                if scanned is None:
                    scanned = audit_trace(trace, report, resources, wheel_radius)
                    sampled_metrics = metrics
                else:
                    require(metrics == sampled_metrics, 'Style reports have different sampled gaps')
                result = copy.deepcopy(scanned)
                checked = check_exceptions(trace, frames, report, result.pop('exceptions'), resources, jars['styles'])
                result['locallyCheckedIntervals'] = checked
                require(result['maximumMotion']['body'] < result['globalSampledGap'] - .02 or checked['count'] > 0,
                        'Unresolved body sweep')
                results[name + '-' + style] = result
    print(json.dumps({'passes': True,
                      'scope': 'Continuous Sable between-snapshot interpolation of retained default-model Arrow recovery poses and conservative vanilla seated-player envelope; excludes physics substeps, client extrapolation, equipment/cosmetics, arbitrary models and observed rendering.',
                      'dependencyHashes': config['dependencyHashes'],
                      'interpolationJarHashes': {'sable': SABLE_PIN, 'companion': SABLE_COMPANION_PIN},
                      'sampledEvidence': str(REPORTS.relative_to(ROOT)),
                      'nativeInputs': str(INPUTS.relative_to(ROOT)),
                      'inputHashes': input_hashes,
                      'wheelLocalRadius': wheel_radius,
                      'results': results}, indent=2))


if __name__ == '__main__':
    main()
