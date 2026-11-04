#!/usr/bin/env python3
"""Extend retained swept Styles clearance through the observed client pointer lead."""

import argparse, gzip, hashlib, importlib.util, itertools, json, math, zipfile
from pathlib import Path
import numpy as np

ROOT = Path(__file__).resolve().parents[1]
EVIDENCE = ROOT / 'docs/evidence/0.9'
REPORTS = EVIDENCE / 'fd401-banked-player-clearance-a5df9b8'
INPUTS = EVIDENCE / 'fd402-isolated-base-ff7c831/native-exports'


def load(name, filename):
    spec = importlib.util.spec_from_file_location(name, ROOT / 'scripts' / filename)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


mesh = load('observed_client_mesh', 'audit-recovery-mesh-trace.py')
swept = load('observed_client_swept', 'audit-recovery-swept-styles.py')


def require(condition, message):
    if not condition:
        raise ValueError(message)


def digest(path):
    h = hashlib.sha256()
    with path.open('rb') as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(chunk)
    return h.hexdigest()


def text(path):
    opener = gzip.open if path.suffix == '.gz' else open
    with opener(path, 'rt', errors='replace') as stream:
        return stream.read()


def rotate(quaternion, vector):
    x, y, z, w = quaternion
    norm = math.sqrt(x*x + y*y + z*z + w*w)
    x, y, z, w = (value / norm for value in (x, y, z, w))
    first = np.cross((x, y, z), vector)
    return np.array(vector) + 2*w*first + 2*np.cross((x, y, z), first)


def pose(value, plot_center):
    offset = (np.array(plot_center) - np.array(value['rotationPoint'])) * np.array(value['scale'])
    origin = np.array(value['position']) + rotate(value['orientation'], offset)
    axes = np.array([rotate(value['orientation'], axis) for axis in np.eye(3)])
    require(np.allclose(axes @ axes.T, np.eye(3), atol=1e-6), 'Invalid client orientation')
    return origin, axes


def rotation_angle(first, second):
    cosine = float(np.clip((np.trace(second @ first.T) - 1) / 2, -1, 1))
    return math.acos(cosine)


def extrapolated_motion(first, second, radius, lead):
    translation = float(np.linalg.norm(second[0] - first[0]))
    angle = rotation_angle(first[1], second[1])
    require(angle < .2, 'Unexpected one-snapshot client rotation')
    half = angle / 2
    # JOML nlerp at t=1+lead on the inspected short quaternion arc.
    extra_angle = 2*math.atan2(lead*math.sin(half), 1 + lead*(1-math.cos(half)))
    return lead*translation + 2*radius*math.sin(extra_angle/2)


def retained_bodies(trace_path, resources):
    bodies, sample = [], None
    with gzip.open(trace_path, 'rt') as stream:
        for line in stream:
            entry = json.loads(line)
            if entry['type'] == 'body' and len(bodies) < 3:
                bodies.append(entry)
            if entry['type'] == 'sample' and entry.get('variant') == 'arrow_four_rail_v1':
                sample = entry
                break
    require(len(bodies) == 3 and sample is not None
            and [body['role'] for body in bodies] == ['chassis', 'carrier', 'carrier'],
            'Missing retained one-car Arrow bodies')
    poses = {item['id']: item for item in sample['poses']}
    player_low, player_high = mesh.vanilla_player_mesh_bounds()
    result = []
    for body in bodies:
        vehicle = [points for block in body['blocks']
                   for points, _ in resources.boxes(block)]
        wheels = []
        if body['role'] == 'chassis':
            vehicle += [points for points, _ in resources.model_boxes(
                mesh.BOTTOM_MODEL, 'nativeLowerBearing')]
            wheels = [points for points, _ in resources.wheel_envelopes()]
        riders, players = [], []
        if body['role'] == 'carrier':
            axes = np.array(poses[body['id']]['axes'])
            origin = np.array(poses[body['id']]['origin'])
            for rider in (item for item in sample['riders'] if item['body'] == body['id']):
                eye = (np.array(rider['eye']) - origin) @ axes.T
                width, height, eye_height = (float(rider[key])
                                             for key in ('width', 'height', 'eyeHeight'))
                riders.append(np.array(list(itertools.product(
                    *zip((-width/2, -eye_height, -width/2),
                         (width/2, height-eye_height, width/2))))) + eye)
                player_eye = eye + mesh.PLAYER_EYE_FROM_FIXTURE_EYE*np.array((0., 1., 0.))
                players.append(np.array(list(itertools.product(*zip(player_low, player_high))))
                               + player_eye)
        result.append({'vehicle': vehicle, 'wheel': wheels,
                       'rider': riders, 'player': players})
    require(sum(len(body['rider']) for body in result) == 4
            and sum(len(body['player']) for body in result) == 4,
            'Missing retained occupied-seat envelopes')
    return result


def client_snapshots(trace_path):
    with gzip.open(trace_path, 'rt') as stream:
        records = [json.loads(line) for line in stream]
    require(records[0]['type'] == 'header' and records[-1]['type'] == 'complete',
            'Incomplete client trace')
    by_tick, ids = {}, None
    for sample in records[1:-1]:
        bodies = sample['bodies']
        if len(bodies) != 3 or any('latestSnapshotPose' not in body for body in bodies):
            continue
        ticks = {body['latestTick'] for body in bodies}
        if len(ticks) != 1:
            continue
        tick = ticks.pop()
        current_ids = tuple(body['id'] for body in bodies)
        ids = current_ids if ids is None else ids
        require(current_ids == ids, 'Client body order changed')
        current = [pose(body['latestSnapshotPose'], body['plotCenter']) for body in bodies]
        if tick in by_tick:
            require(all(np.allclose(a[0], b[0]) and np.allclose(a[1], b[1])
                        for a, b in zip(by_tick[tick], current)), 'Snapshot tick pose changed')
        else:
            by_tick[tick] = current
    ticks = sorted(by_tick)
    require(len(ticks) > 1000, 'Insufficient full-turn snapshot coverage')
    return ids, [(tick, by_tick[tick]) for tick in ticks]


def measured_extrapolation(trace_path, body_specs, lead):
    ids, snapshots = client_snapshots(trace_path)
    maximum = {key: {'extraMotion': 0., 'ticks': None, 'body': None}
               for key in ('vehicle', 'wheel', 'rider', 'player')}
    intervals = 0
    for (tick0, before), (tick1, after) in zip(snapshots, snapshots[1:]):
        if tick1 != tick0 + 1:
            continue
        intervals += 1
        for index, (spec, first, second) in enumerate(zip(body_specs, before, after)):
            for key in maximum:
                for points in spec[key]:
                    radius = float(np.linalg.norm(points, axis=1).max())
                    extra = extrapolated_motion(first, second, radius, lead)
                    if extra > maximum[key]['extraMotion']:
                        maximum[key] = {'extraMotion': extra, 'ticks': [tick0, tick1],
                                        'body': ids[index]}
    require(intervals > 1000 and all(item['extraMotion'] > 0 for item in maximum.values()),
            'Client trace lacks moving snapshot-pair coverage')
    return len(snapshots), intervals, maximum


def recovery_motion(trace_path, report, resources, lead):
    route = report['result']
    gap, required = float(route['minimumSeparatingBound']), float(route['requiredGap'])
    rider_gap = float(route['riderMinimumSeparatingBound'])
    player_gap = float(route['playerMeshMinimumSeparatingBound'])
    wheel_radius = max(swept.radius(points) for points, _ in resources.wheel_envelopes())
    player_low, player_high = mesh.vanilla_player_mesh_bounds()
    specs, previous = {}, {}
    maximum = {key: 0. for key in ('vehicle', 'wheel', 'rider', 'player')}
    maximum_nonexception = 0.
    exceptions, samples = [], 0
    with gzip.open(trace_path, 'rt') as stream:
        for line in stream:
            entry = json.loads(line)
            if entry['type'] == 'body':
                boxes = [points for block in entry['blocks']
                         for points, _ in resources.boxes(block)]
                if entry['role'] in ('chassis', 'ordinary'):
                    boxes += [points for points, _ in resources.model_boxes(
                        mesh.BOTTOM_MODEL, 'nativeLowerBearing')]
                specs[entry['id']] = {'role': entry['role'],
                                      'radius': max(swept.radius(points) for points in boxes)}
                continue
            if entry['type'] != 'sample' or entry.get('variant') != 'arrow_four_rail_v1':
                continue
            samples += 1
            current = {item['id']: (np.array(item['origin']), np.array(item['axes']))
                       for item in entry['poses']}
            for uid, value in current.items():
                if uid not in previous:
                    continue
                body = specs[uid]
                bound = (swept.motion(previous[uid], value, body['radius'])
                         + extrapolated_motion(previous[uid], value, body['radius'], lead))
                maximum['vehicle'] = max(maximum['vehicle'], bound)
                if bound > gap-required:
                    exceptions.append({'tick': entry['tick'], 'body': uid,
                                       'bound': bound, 'role': body['role']})
                else:
                    maximum_nonexception = max(maximum_nonexception, bound)
                if body['role'] in ('chassis', 'ordinary'):
                    maximum['wheel'] = max(maximum['wheel'],
                        swept.motion(previous[uid], value, wheel_radius)
                        + extrapolated_motion(previous[uid], value, wheel_radius, lead))
            for rider in entry['riders']:
                uid = rider['body']
                if uid not in previous:
                    continue
                origin, axes = current[uid]
                eye = (np.array(rider['eye']) - origin) @ axes.T
                width, height, eye_height = (float(rider[key])
                                             for key in ('width', 'height', 'eyeHeight'))
                rider_points = np.array(list(itertools.product(
                    *zip((-width/2, -eye_height, -width/2),
                         (width/2, height-eye_height, width/2))))) + eye
                player_points = (np.array(list(itertools.product(*zip(player_low, player_high))))
                                 + eye + mesh.PLAYER_EYE_FROM_FIXTURE_EYE*np.array((0., 1., 0.)))
                maximum['rider'] = max(maximum['rider'],
                    swept.motion(previous[uid], current[uid], swept.radius(rider_points))
                    + extrapolated_motion(previous[uid], current[uid],
                                          swept.radius(rider_points), lead))
                maximum['player'] = max(maximum['player'],
                    swept.motion(previous[uid], current[uid], swept.radius(player_points))
                    + extrapolated_motion(previous[uid], current[uid],
                                          swept.radius(player_points), lead))
            previous = current
    require(samples == route['samples'], 'Retained trace/report sample count changed')
    require(gap-maximum['wheel'] >= required, 'Wheel extrapolation exhausts clearance')
    require(rider_gap-maximum['rider'] >= required, 'Rider extrapolation exhausts clearance')
    require(player_gap-maximum['player'] >= required, 'Player extrapolation exhausts clearance')
    return maximum, maximum_nonexception, exceptions


def check_exceptions(trace_path, frames_path, report, exceptions, resources, styles_jar):
    if not exceptions:
        return {'count': 0, 'minimumSweptLowerBound': None,
                'maximumMotionBound': None, 'firstInterval': None, 'lastInterval': None}
    routes = swept.styles.read_json(frames_path)
    route = next(item for item in routes if item['family'] == 'ARROW')
    plan = report['prospectiveStylePlan']
    used = set(plan.values()) - {'original'}
    with zipfile.ZipFile(styles_jar) as archive:
        models, _ = swept.styles.models(archive, used)
    triangles = swept.styles.surface(route, plan, models)[0][0]
    tri_low, tri_high = triangles.min(axis=1), triangles.max(axis=1)
    wanted = {(item['tick']-1, item['body']) for item in exceptions}
    boxes, poses = {}, {}
    with gzip.open(trace_path, 'rt') as stream:
        for line in stream:
            entry = json.loads(line)
            if entry['type'] == 'body' and any(uid == entry['id'] for _, uid in wanted):
                values = [points for block in entry['blocks']
                          for points, _ in resources.boxes(block)]
                if entry['role'] in ('chassis', 'ordinary'):
                    values += [points for points, _ in resources.model_boxes(
                        mesh.BOTTOM_MODEL, 'nativeLowerBearing')]
                boxes[entry['id']] = values
            if entry['type'] == 'sample':
                for value in entry['poses']:
                    if (entry['tick'], value['id']) in wanted:
                        poses[entry['tick'], value['id']] = (
                            np.array(value['axes']), np.array(value['origin']))
    checked = []
    for item in exceptions:
        key = (item['tick']-1, item['body'])
        require(key in poses and item['body'] in boxes, 'Missing local exception pose/body')
        search = item['bound'] + .03
        local = min(swept.near_track_minimum(
            points, poses[key], triangles, tri_low, tri_high, search)
                    for points in boxes[item['body']])
        require(local-item['bound'] >= .02, 'Local extrapolated clearance failed')
        checked.append({'tickInterval': [item['tick']-1, item['tick']],
                        'body': item['body'], 'role': item['role'],
                        'sampledLocalBound': local, 'motionBound': item['bound'],
                        'sweptLowerBound': local-item['bound']})
    return {'count': len(checked),
            'minimumSweptLowerBound': min(item['sweptLowerBound'] for item in checked),
            'maximumMotionBound': max(item['motionBound'] for item in checked),
            'firstInterval': checked[0]['tickInterval'],
            'lastInterval': checked[-1]['tickInterval']}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('trace', type=Path)
    parser.add_argument('client_log', type=Path)
    parser.add_argument('lag_report', type=Path)
    parser.add_argument('body_trace', type=Path)
    args = parser.parse_args()
    try:
        log = text(args.client_log)
        require('Validation boarded Dev and closed restraints' in log
                and 'Validation dispatched Dev' in log and 'capture=true' in log
                and 'Validation availability chassis=1/3 carriers=2/6 assignments=0' in log
                and 'BUILD SUCCESSFUL' in log, 'Missing normal occupied-cycle milestones')
        lag = json.loads(args.lag_report.read_text())
        require(lag['traceComplete'] and len(lag['fastStaleSamplesAbove0.05']) == 1,
                'Archived fast pointer-lead observation changed')
        lead = lag['maximumFastPointerLead']
        require(0 < lead < .01, 'Observed fast pointer lead outside reviewed bound')
        config = json.loads((REPORTS / 'config.json').read_text())
        jars = {key: Path(value) for key, value in config['jars'].items()}
        for key, path in jars.items():
            require(digest(path) == config['dependencyHashes'][key], 'Dependency changed: ' + key)
        with zipfile.ZipFile(jars['base']) as base, zipfile.ZipFile(jars['minecraft']) as minecraft:
            resources = mesh.Resources(base, minecraft)
            body_specs = retained_bodies(args.body_trace, resources)
            snapshot_count, intervals, extra = measured_extrapolation(args.trace, body_specs, lead)
            train_trace = INPUTS / 'recovery-bank-train-mesh-trace.jsonl.gz'
            train_frames = INPUTS / 'recovery-bank-train-native-frames.json.gz'
            results = {}
            for style in ('arrow_4d', 'arrow_4d_dbl_spine', 'transition'):
                report = json.loads((REPORTS / ('train-' + style + '.json')).read_text())
                maximum, nonexception, exceptions = recovery_motion(
                    train_trace, report, resources, lead)
                checked = check_exceptions(train_trace, train_frames, report, exceptions,
                                           resources, jars['styles'])
                route = report['result']
                local = (checked['minimumSweptLowerBound']
                         if checked['minimumSweptLowerBound'] is not None else math.inf)
                lower = {
                    'vehicle': min(float(route['minimumSeparatingBound'])-nonexception, local),
                    'wheel': float(route['wheelMinimumSeparatingBound'])-maximum['wheel'],
                    'rider': float(route['riderMinimumSeparatingBound'])-maximum['rider'],
                    'player': float(route['playerMeshMinimumSeparatingBound'])-maximum['player'],
                }
                require(min(lower.values()) >= .02,
                        'Observed extrapolation exhausts '+style+' clearance: '+repr(lower))
                results[style] = {'maximumCombinedMotion': maximum,
                                  'maximumNonexceptionVehicleMotion': nonexception,
                                  'locallyCheckedVehicleIntervals': checked,
                                  'minimumSeparatingLowerBounds': lower}
        output = {
            'passes': True, 'clearanceCertified': True, 'requiredGap': .02,
            'scope': ('Retained coupled Arrow default-model Styles snapshot pairs extended by the maximum '
                      'fast pointer lead observed in the occupied saved-world client ride. The independent '
                      'full-pose client lap verifies complete client snapshot-pair capture and reports its '
                      'own conservative motion bound; the clearance results use each retained recovery-route '
                      'pair directly. Excludes larger packet delays, solver paths inside physics substeps, '
                      'equipment/cosmetics and arbitrary resources.'),
            'observedFastPointerLeadTicks': lead,
            'clientSnapshotTicks': snapshot_count,
            'clientConsecutiveIntervals': intervals,
            'maximumClientExtrapolationMotion': extra,
            'results': results,
            'inputHashes': {name: digest(path) for name, path in {
                'trace': args.trace, 'clientLog': args.client_log,
                'lagReport': args.lag_report, 'bodyTrace': args.body_trace,
                'trainTrace': INPUTS/'recovery-bank-train-mesh-trace.jsonl.gz',
                'trainFrames': INPUTS/'recovery-bank-train-native-frames.json.gz',
                'base': jars['base'], 'minecraft': jars['minecraft'], 'styles': jars['styles']}.items()},
            'diagnosticSourceHash': digest(ROOT/'src/main/java/dev/fourthdimension/client/ClientSnapshotTrace.java'),
            'auditorHash': digest(Path(__file__)),
        }
        print(json.dumps(output, indent=2))
    except (OSError, ValueError, KeyError, TypeError, IndexError, AssertionError,
            gzip.BadGzipFile, zipfile.BadZipFile) as error:
        parser.exit(1, 'FAIL: '+str(error)+'\n')


if __name__ == '__main__':
    main()
