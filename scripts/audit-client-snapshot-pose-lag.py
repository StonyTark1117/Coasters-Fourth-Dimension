#!/usr/bin/env python3
"""Inspect measured pose pairs for the opt-in Sable client pointer-lead trace."""

import argparse
import gzip
import hashlib
import json
import math
from pathlib import Path


def require(ok, message):
    if not ok:
        raise ValueError(message)


def sha256(path):
    digest = hashlib.sha256()
    with path.open('rb') as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b''):
            digest.update(chunk)
    return digest.hexdigest()


def rotate(quaternion, vector):
    x, y, z, w = quaternion
    norm = math.sqrt(x*x + y*y + z*z + w*w)
    x, y, z, w = (value / norm for value in (x, y, z, w))
    a = (y*vector[2] - z*vector[1], z*vector[0] - x*vector[2],
         x*vector[1] - y*vector[0])
    b = (y*a[2] - z*a[1], z*a[0] - x*a[2], x*a[1] - y*a[0])
    return tuple(value + 2*w*first + 2*second
                 for value, first, second in zip(vector, a, b))


def world_center(pose, plot_center):
    offset = tuple((center - origin) * scale for center, origin, scale in
                   zip(plot_center, pose['rotationPoint'], pose['scale']))
    return tuple(position + rotated for position, rotated in
                 zip(pose['position'], rotate(pose['orientation'], offset)))


def angle(first, second):
    a, b = first['orientation'], second['orientation']
    dot = abs(sum(x*y for x, y in zip(a, b)))
    norm = math.sqrt(sum(x*x for x in a) * sum(y*y for y in b))
    return 2*math.acos(min(1., max(-1., dot / norm)))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('trace', type=Path)
    parser.add_argument('lag_report', type=Path)
    args = parser.parse_args()
    lag = json.loads(args.lag_report.read_text())
    require(lag['traceComplete'] and lag['inputHashes']['trace'] == sha256(args.trace),
            'Incomplete or mismatched lag trace')
    with gzip.open(args.trace, 'rt') as stream:
        records = [json.loads(line) for line in stream]
    require(records[0]['type'] == 'header' and records[-1]['type'] == 'complete'
            and len(records[1:-1]) == lag['samples'], 'Trace record count changed')
    target = lag['mountedChassis']
    previous_position = None
    measured = 0
    missing = 0
    moving_measured = 0
    maximum_center_motion = (0., None)
    maximum_angle = (0., None)
    maximum_lead = (0., None)
    for index, sample in enumerate(records[1:-1]):
        position = (sample['chassisX'], sample['chassisZ'])
        moving = previous_position is not None and math.dist(position, previous_position) > .001
        previous_position = position
        for body in sample['bodies']:
            if body['chassis'] != target or body.get('latestTick') is None:
                continue
            lead = sample['pointer'] - body['latestTick']
            if lead <= 1e-8 or body['bufferSize'] < 2:
                continue
            if 'previousSnapshotPose' not in body or 'latestSnapshotPose' not in body:
                missing += 1
                continue
            first, second = body['previousSnapshotPose'], body['latestSnapshotPose']
            require(first['scale'] == [1., 1., 1.] and second['scale'] == [1., 1., 1.],
                    'Unexpected snapshot pose scale')
            measured += 1
            moving_measured += moving
            center_motion = math.dist(world_center(first, body['plotCenter']),
                                      world_center(second, body['plotCenter']))
            rotation = angle(first, second)
            where = {'sample': index, 'body': body['id'], 'role': body['role']}
            if center_motion > maximum_center_motion[0]:
                maximum_center_motion = (center_motion, where)
            if rotation > maximum_angle[0]:
                maximum_angle = (rotation, where)
            if lead > maximum_lead[0]:
                maximum_lead = (lead, where)
    require(missing == 0 and measured > 0 and moving_measured == 0,
            'Missing pose payload or unexpected moving extrapolation')
    require(measured == sum(body['staleSamples'] for body in lag['perBody'].values()),
            'Pose payload and lag audit disagree')
    print(json.dumps({
        'traceComplete': True,
        'clearanceCertified': False,
        'scope': 'Snapshot-pair transform displacement for recorded client pointer leads on one occupied styled Arrow ride; no lead occurred during observed chassis movement.',
        'inputHashes': {'trace': sha256(args.trace), 'lagReport': sha256(args.lag_report)},
        'mountedChassis': target,
        'samples': lag['samples'],
        'movingSamples': lag['movingSamplesAbove0.001'],
        'staleSamples': lag['staleSampleCount'],
        'measuredBodyPosePairs': measured,
        'movingMeasuredPairs': moving_measured,
        'maximumSnapshotCenterMotion': maximum_center_motion[0],
        'maximumSnapshotCenterMotionAt': maximum_center_motion[1],
        'maximumSnapshotAngularChangeRadians': maximum_angle[0],
        'maximumSnapshotAngularChangeAt': maximum_angle[1],
        'maximumPointerLeadTicks': maximum_lead[0],
        'maximumPointerLeadAt': maximum_lead[1],
    }, indent=2))


if __name__ == '__main__':
    main()
