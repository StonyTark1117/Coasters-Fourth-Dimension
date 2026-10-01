#!/usr/bin/env python3
"""Verify the native orbit-camera regression, real wall collisions and occupied combined run."""
import gzip
import hashlib
import json
import math
import re
import subprocess
import sys
import uuid
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
def data(name):
    return json.loads((root / name).read_text())
def raw(name):
    return gzip.decompress((root / name).read_bytes())
def groups(blob):
    return [json.loads(x.split('FD_CAMERA_GROUP ', 1)[1]) for x in blob.decode().splitlines(keepends=True) if x.endswith('\n') and 'FD_CAMERA_GROUP {' in x]
def camera(blob):
    result = []
    for line in blob.decode().splitlines(keepends=True):
        if line.endswith('\n') and 'FD_IMMERSION metric=' in line and ' camera=(' in line:
            row = dict(re.findall(r'(\w+)=([^ ]+)', line.split(' camera=', 1)[0]))
            assert row['metric'] == 'normalized-v1'
            result.append(row)
    return result
for name, digest in data('manifest.json')['files'].items():
    assert hashlib.sha256((root / name).read_bytes()).hexdigest() == digest, name
artifacts = data('artifacts.json')
assert 'CarrierConnectionMixin' not in artifacts['before']['mixins']['client']
assert 'CarrierConnectionMixin' in artifacts['fixed']['mixins']['client']
assert artifacts['before']['sha256'] != artifacts['fixed']['sha256']
a, b = artifacts['before']['classes'], artifacts['fixed']['classes']
assert all(b.get(name) == digest for name, digest in a.items())
assert set(b) - set(a) == {'dev/fourthdimension/mixin/CarrierConnectionMixin.class'}
results = {}
rails = None
for case in ['before', 'after', 'base', 'combined']:
    fixed = case != 'before'
    server = raw(case + '/server.log.gz').decode()
    client = raw(case + '/dev.log.gz')
    assert ('Coasters: Immersion 0.2.3' in server) == (case != 'base')
    assert ('Coasters: Engineered 1.1.1' in server) == (case == 'combined')
    assert 'BUILD SUCCESSFUL' in server and b'BUILD SUCCESSFUL' in client
    assert (root / case / 'server.exit').read_text().strip() == (root / case / 'dev.exit').read_text().strip() == '0'
    events = data(case + '/group-events.json')
    assert events[-1]['kind'] == 'complete'
    phases = {}
    for start in [e for e in events if e['kind'] == 'phase-start']:
        end = next(e for e in events if e['kind'] == 'phase-end' and e['name'] == start['name'])
        rows = groups(client[start['offset']:end['offset']])
        assert len(rows) >= 12
        for row in rows:
            assert all(math.isfinite(row[k]) for k in ['distance', 'zoom', 'cameraX', 'cameraY', 'cameraZ', 'centerX', 'centerY', 'centerZ'])
            assert len(row['vehicleBodies']) == 5 and row['chassis'] in row['vehicleBodies']
            assert set(row['group']) == (set(row['vehicleBodies']) if fixed else {row['body']})
        distances = [r['distance'] for r in rows]
        name = start['name']
        settled = distances[4:] if name in ['wall', 'wall-removed'] else distances
        assert len(settled) >= 12
        if name in ['SUB_LEVEL_VIEW', 'SUB_LEVEL_VIEW_UNLOCKED']:
            assert all(abs(v - 7) < .01 for v in distances) if fixed else all(v < 1.1 for v in distances)
        if name in ['orbit-zoom', 'wall-removed']:
            assert all(abs(r['zoom'] - 4) < .001 for r in rows)
            assert all(abs(v - 23) < .01 for v in settled) if fixed else all(v < 1.1 for v in distances)
        if name == 'wall':
            assert fixed and all(8 < v < 14 for v in settled)
        phases[name] = {'samples': len(rows), 'distance_min': min(distances), 'distance_max': max(distances), 'settled_samples': len(settled), 'settled_min': min(settled), 'settled_max': max(settled)}
    assert len(phases) == (8 if fixed else 6)
    if fixed:
        assert len([e for e in events if e['kind'] == 'wall-create']) == 1
        commands = (root / case / 'commands.txt').read_text()
        assert 'minecraft:stone: Successfully filled 81 block(s)' in commands
        assert 'minecraft:air: Successfully filled 81 block(s)' in commands
    player = (root / case / 'player-final.txt').read_text()
    assert 'Health: 20.0f' in player and 'OnGround: 1b' in player and 'RootVehicle:' not in player and 'NoGravity: 1b' not in player
    saved = json.loads(subprocess.check_output([sys.executable, 'scripts/inspect-foundation-save.py', str(root / case / 'world/data/fourthdimension_foundation.dat')], text=True))['data']
    assert len(saved['vehicles']) == 1 and len(saved['rails']) == 8
    if rails is None:
        rails = saved['rails']
    assert saved['rails'] == rails
    assert all(not c['occupants'] and not c['rider_seats'] for c in saved['vehicles'][0]['carriers'])
    assert ('FD_IMMERSION' in client.decode()) == (case != 'base')
    server_hashes = set(re.findall(r'FD_SNAPSHOT side=server.*?tick=(\d+) sha256=([a-f0-9]+) railsSha256=([a-f0-9]+)', server))
    client_hashes = set(re.findall(r'FD_SNAPSHOT side=client.*?tick=(\d+) sha256=([a-f0-9]+) railsSha256=([a-f0-9]+)', client.decode()))
    assert len(server_hashes & client_hashes) >= 20
    results[case] = phases

# The isolated code change preserves parked first-person and vanilla third-person views.
before_events = data('before/group-events.json')
after_events = data('after/group-events.json')
for name in ['first', 'THIRD_PERSON_BACK', 'THIRD_PERSON_FRONT']:
    a = next(e['sample'] for e in before_events if e['kind'] == 'phase-end' and e['name'] == name)
    b = next(e['sample'] for e in after_events if e['kind'] == 'phase-end' and e['name'] == name)
    assert a['body'] == b['body']
    assert all(abs(a[key] - b[key]) < .001 for key in ['cameraX', 'cameraY', 'cameraZ', 'yaw', 'pitch'])

# The combined run also exercises two occupied complete circuits, changing all four carriers.
events = data('combined/accept-events.json')
assert events[-1]['kind'] == 'complete'
server = raw('combined/server.log.gz').decode()
reports = [json.loads(x.split('FD_CAMERA_RIDE ', 1)[1]) for x in server.splitlines() if 'FD_CAMERA_RIDE {' in x]
reports = [r for r in reports if r['label'].startswith('accepted-')]
for prefix in ['first', 'second']:
    returned = [r for r in reports if r['label'].startswith('accepted-' + prefix + '-return-')][-1]
    assert returned['ready'] and returned['restraints'] and returned['stopping'] and not returned['fault'] and returned['speed'] < .01
    assert sum(len(c['riders']) for c in returned['carriers']) == 2
    unloaded = next(r for r in reports if r['label'] == 'accepted-' + prefix + '-unloaded')
    assert unloaded['ready'] and not unloaded['restraints'] and not any(c['riders'] for c in unloaded['carriers'])
assert max(r['x'] for r in reports) - min(r['x'] for r in reports) > 30
assert max(r['z'] for r in reports) - min(r['z'] for r in reports) > 45
moving = {}
for name, first_index, second_index in [('dev', 0, 3), ('observer', 2, 1)]:
    client = raw('combined/' + name + '.log.gz')
    assert (root / 'combined' / (name + '.exit')).read_text().strip() == '0' and b'BUILD SUCCESSFUL' in client
    first = next(r for r in reports if r['label'] == 'accepted-boarded')['carriers'][first_index]
    second = next(r for r in reports if r['label'] == 'accepted-second-boarded')['carriers'][second_index]
    assert first['body'] != second['body'] and first['riders'] == second['riders'] and len(first['riders']) == 1
    total = 0
    phase_rows = {}
    for start in [e for e in events if e['kind'] == 'phase-start']:
        end = next(e for e in events if e['kind'] == 'phase-end' and e['name'] == start['name'])
        window = client[start['offsets'][name]:end['offsets'][name]]
        rows = camera(window); group_rows = groups(window)
        assert len(rows) >= 20 and len(group_rows) >= 20
        phase_rows[start['name']] = rows
        expected_body = (first if start['name'].isupper() else second)['body']
        expected_mode = start['name'] if start['name'].isupper() else 'FIRST_PERSON'
        assert all(r['body'] == expected_body and r['riding'] == 'true' and r['perspective'] == expected_mode and 0 <= float(r['rawError']) < 1e-6 for r in rows)
        assert all(r['body'] == expected_body and set(r['group']) == set(r['vehicleBodies']) for r in group_rows)
        if start['name'] == 'disabled-first':
            assert all(r['enabled'] == 'false' and r['processing'] == 'false' and float(r['travel']) == 0 for r in rows)
        if start['name'] != 'disabled-first':
            expected_inertia = {'zero-first': 0, 'maximum-first': .15}.get(start['name'], .04)
            assert all(r['enabled'] == 'true' and float(r['inertia']) == expected_inertia for r in rows)
        if expected_mode.startswith('SUB_LEVEL_VIEW'):
            assert all(abs(r['distance'] - 7) < .01 for r in group_rows)
        total += len(rows)
    a, b = phase_rows['zero-first'][-1], phase_rows['restored-first'][-1]
    assert abs((float(b['yaw']) - float(a['yaw']) + 180) % 360 - 180) > 10
    assert abs(float(b['pitch']) - float(a['pitch'])) > 4
    config = (root / 'combined' / ('accept-config-' + name + '-True-150-True.toml')).read_text()
    assert all(s in config for s in ['head_inertia_ms = 150', 'roll_stabilization = 100', 'pitch_stabilization = 100'])
    for prefix in ['first', 'second']:
        event = next(e for e in events if e['kind'] == 'unloaded' and e['name'] == prefix)
        row = event['samples'][name]
        assert row['riding'] == 'false' and row['body'] == 'none' and row['perspective'] == 'FIRST_PERSON' and float(row['travel']) == 0
        assert abs(float(row['cameraUp']) - math.cos(math.radians(float(row['pitch'])))) < .001
        start_name = 'FIRST_PERSON' if prefix == 'first' else 'zero-first'
        start = next(e for e in events if e['kind'] == 'phase-start' and e['name'] == start_name)
        transition_rows = camera(client[start['offsets'][name]:event['offsets'][name]])
        assert any(a['body'] != 'none' and a['riding'] == 'true' and b['body'] == 'none' and b['riding'] == 'false'
                   for a, b in zip(transition_rows, transition_rows[1:]))
        player = (root / 'combined' / ('accept-' + prefix + '-' + name + '-player.txt')).read_text()
        assert 'Health: 20.0f' in player and 'OnGround: 1b' in player and 'RootVehicle:' not in player and 'NoGravity: 1b' not in player
    server_hashes = set(re.findall(r'FD_SNAPSHOT side=server.*?tick=(\d+) sha256=([a-f0-9]+) railsSha256=([a-f0-9]+)', server))
    client_hashes = set(re.findall(r'FD_SNAPSHOT side=client.*?tick=(\d+) sha256=([a-f0-9]+) railsSha256=([a-f0-9]+)', client.decode()))
    matches = len(server_hashes & client_hashes)
    assert matches >= 100
    moving[name] = {'measured_samples': total, 'normal_exits': 2, 'matched_snapshots': matches}
units = [ET.parse(p).getroot() for p in (root / 'unit-tests').glob('TEST-*.xml')]
assert sum(int(r.attrib['tests']) for r in units) == 19 and all(r.attrib['errors'] == r.attrib['failures'] == '0' for r in units)
cleanup = data('cleanup.json')
assert cleanup['handles_gone'] and cleanup['ports_closed'] and cleanup['settings_restored']
print(json.dumps({'parked': results, 'moving_combined': moving, 'unit_tests': 19,
                  'scope': 'Native Sable orbit-camera connection fix on Revised; vanilla third-person collision policy and full release matrix remain separate'}, indent=2))
