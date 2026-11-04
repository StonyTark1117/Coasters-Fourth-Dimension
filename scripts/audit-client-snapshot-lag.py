#!/usr/bin/env python3
"""Summarize a complete opt-in client pose-buffer trace without inferring clearance."""

import argparse
import gzip
import hashlib
import json
import math
from pathlib import Path


def sha256(path):
    digest = hashlib.sha256()
    with path.open('rb') as stream:
        for part in iter(lambda: stream.read(1024 * 1024), b''):
            digest.update(part)
    return digest.hexdigest()


def require(condition, message):
    if not condition:
        raise ValueError(message)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('trace', type=Path)
    parser.add_argument('client_log', type=Path)
    args = parser.parse_args()
    with gzip.open(args.trace, 'rt') as stream:
        records = [json.loads(line) for line in stream]
    require(len(records) > 100 and records[0]['type'] == 'header'
            and records[-1]['type'] == 'complete', 'Incomplete client trace')
    header, complete = records[0], records[-1]
    samples = records[1:-1]
    require(header['delay'] == 1.5 and len(samples) == complete['samples'],
            'Unexpected delay or sample count')
    target = header['mountedChassis']
    roles = {}
    previous_position = None
    backward_game_time_samples = 0
    previous_time = None
    first_ready = None
    moving = 0
    per_body = {}
    stale_samples = []
    moving_stale_samples = []
    fast_stale_samples = []
    maximum_moving_lead = 0.
    maximum_fast_lead = 0.
    for index, sample in enumerate(samples):
        require(sample['type'] == 'sample' and sample['mountedChassis'] == target,
                'Mounted ride changed')
        game_time = sample['clientGameTime']
        if previous_time is not None and game_time < previous_time:
            backward_game_time_samples += 1
        previous_time = game_time
        position = (sample['chassisX'], sample['chassisZ'])
        distance = (math.dist(position, previous_position)
                    if previous_position is not None else 0.)
        previous_position = position
        is_moving = distance > .001
        is_fast = distance > .05
        moving += is_moving
        bodies = [body for body in sample['bodies'] if body['chassis'] == target]
        require(len(bodies) == 3 and len({body['id'] for body in bodies}) == 3,
                'Missing or duplicate mounted-car body')
        if first_ready is None and all(body['bufferSize'] >= 2 for body in bodies):
            first_ready = index
        sample_stale = []
        for body in bodies:
            uid = body['id']
            role = body['role']
            require(uid not in roles or roles[uid] == role, 'Body role changed')
            roles[uid] = role
            stats = per_body.setdefault(uid, {'role': role, 'readySamples': 0,
                                               'missingSamples': 0, 'staleSamples': 0,
                                               'maximumPointerLead': 0.})
            if body['bufferSize'] < 2:
                stats['missingSamples'] += 1
                continue
            require(body['previousTick'] < body['latestTick'], 'Invalid pose pair')
            stats['readySamples'] += 1
            lead = sample['pointer'] - body['latestTick']
            stats['maximumPointerLead'] = max(stats['maximumPointerLead'], lead)
            if lead > 1e-8:
                stats['staleSamples'] += 1
                sample_stale.append({'body': uid, 'role': role, 'lead': lead,
                                     'snapshotInterval': body['latestTick'] - body['previousTick']})
        if sample_stale:
            item = {'index': index, 'clientGameTime': game_time,
                    'informationTick': sample['informationTick'],
                    'pointer': sample['pointer'], 'chassisStepDistance': distance,
                    'maximumLead': max(body['lead'] for body in sample_stale),
                    'bodies': len(sample_stale)}
            stale_samples.append(item)
            if is_moving:
                moving_stale_samples.append(item)
                maximum_moving_lead = max(maximum_moving_lead, item['maximumLead'])
            if is_fast:
                fast_stale_samples.append(item)
                maximum_fast_lead = max(maximum_fast_lead, item['maximumLead'])
    require(first_ready is not None and moving == complete['movingSamples'],
            'Missing pose readiness or moving-sample count mismatch')
    require({value['role'] for value in per_body.values()} == {'chassis', 'carrier'},
            'Unexpected body roles')
    log = args.client_log.read_text(errors='replace')
    require('Validation boarded Dev and closed restraints' in log
            and 'Validation dispatched Dev' in log
            and 'Successfully filled 1 block(s)' in log
            and 'BUILD SUCCESSFUL' in log, 'Missing run milestones or clean exit')
    print(json.dumps({
        'traceComplete': True,
        'clearanceCertified': False,
        'scope': 'Client tick pointer versus latest native body-pose snapshot during one instrumented occupied Styles Arrow ride; no render-frame or pose-delta capture.',
        'inputHashes': {'trace': sha256(args.trace), 'clientLog': sha256(args.client_log)},
        'mountedChassis': target,
        'delay': header['delay'],
        'samples': len(samples),
        'backwardGameTimeSamples': backward_game_time_samples,
        'firstAllBodiesReadyIndex': first_ready,
        'movingSamplesAbove0.001': moving,
        'perBody': per_body,
        'staleSampleCount': len(stale_samples),
        'firstStale': stale_samples[0] if stale_samples else None,
        'lastStale': stale_samples[-1] if stale_samples else None,
        'movingStaleSamples': moving_stale_samples,
        'maximumMovingPointerLead': maximum_moving_lead,
        'fastStaleSamplesAbove0.05': fast_stale_samples,
        'maximumFastPointerLead': maximum_fast_lead,
    }, indent=2))


if __name__ == '__main__':
    main()
