#!/usr/bin/env python3
"""Audit real Revised carrier camera runs; never infer inversion from camera tilt."""
import gzip
import hashlib
import json
import math
import re
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])

def data(path):
    return json.loads((root / path).read_text())

def log(path):
    return gzip.decompress((root / path).read_bytes())

def samples(raw):
    rows = []
    for line in raw.decode(errors='replace').splitlines():
        if 'FD_IMMERSION metric=' not in line or ' camera=(' not in line:
            continue
        row = dict(re.findall(r'(\w+)=([^ ]+)', line.split(' camera=', 1)[0]))
        assert row['metric'] == 'normalized-v1'
        for key in ['rawError', 'carrierUp', 'cameraUp', 'yaw', 'pitch', 'travel', 'inertia']:
            row[key] = float(row[key])
            assert math.isfinite(row[key]), (key, line)
        rows.append(row)
    return rows

manifest = data('manifest.json')
for name, expected in manifest['files'].items():
    assert hashlib.sha256((root / name).read_bytes()).hexdigest() == expected, name
results = {}
for case in ['immersion', 'combined']:
    server = log(case + '/server.log.gz').decode()
    assert 'Coasters: Immersion 0.2.3' in server
    assert ('Coasters: Engineered 1.1.1' in server) == (case == 'combined')
    reports = [json.loads(line.split('FD_CAMERA_RIDE ', 1)[1]) for line in server.splitlines() if 'FD_CAMERA_RIDE {' in line]
    accepted = [r for r in reports if r['label'].startswith('accept-')]
    assert len(accepted) >= 25
    assert all(r['family'] == 'ZACSPIN_REVISED' and not r['fault'] for r in accepted)
    assert len({r['chassis'] for r in accepted}) == 1
    assert max(r['x'] for r in accepted) - min(r['x'] for r in accepted) > 30
    assert max(r['z'] for r in accepted) - min(r['z'] for r in accepted) > 45
    expected_players = 1 if case == 'immersion' else 2
    for prefix in ['first', 'second']:
        returned = [r for r in accepted if r['label'].startswith('accept-' + prefix + '-return-')][-1]
        assert returned['ready'] and returned['stopping'] and returned['restraints'] and returned['speed'] < .01
        assert sum(len(c['riders']) for c in returned['carriers']) == expected_players
        unloaded = next(r for r in accepted if r['label'] == 'accept-' + prefix + '-unloaded')
        assert unloaded['ready'] and not unloaded['restraints'] and all(not c['riders'] for c in unloaded['carriers'])
    events = data(case + '/accept-events.json')
    assert events[-1]['kind'] == 'complete'
    phases = [e for e in events if e['kind'] == 'phase-start']
    assert len(phases) == 9
    client_results = {}
    for client in ['dev'] + (['observer'] if case == 'combined' else []):
        raw = log(case + '/' + client + '.log.gz')
        assert (root / case / (client + '.exit')).read_text().strip() == '0'
        assert b'BUILD SUCCESSFUL' in raw
        first = next(r for r in accepted if r['label'] == 'accept-boarded')
        second = next(r for r in accepted if r['label'] == 'accept-second-boarded')
        first_index = 0 if client == 'dev' else 2
        second_index = 3 if client == 'dev' else 1
        phase_results = {}
        for start in phases:
            end = next(e for e in events if e['kind'] == 'phase-end' and e['name'] == start['name'])
            rows = samples(raw[start['offsets'][client]:end['offsets'][client]])
            assert len(rows) >= 20, (case, client, start['name'], len(rows))
            expected_mode = start['name'] if start['name'].isupper() else 'FIRST_PERSON'
            assert all(r['perspective'] == expected_mode and r['riding'] == 'true' and r['body'] != 'none' for r in rows)
            source = first['carriers'][first_index] if start['name'].isupper() else second['carriers'][second_index]
            assert all(r['body'] == source['body'] for r in rows)
            assert all(0 <= r['rawError'] < 1e-6 for r in rows)
            if start['name'] == 'disabled-first':
                assert all(r['enabled'] == 'false' and r['processing'] == 'false' and r['travel'] == 0 and r['inertia'] == 0 for r in rows)
            else:
                inertia = {'zero-first': 0, 'maximum-first': .15}.get(start['name'], .04)
                assert all(r['enabled'] == 'true' and r['inertia'] == inertia for r in rows)
            phase_results[start['name']] = {'samples': len(rows), 'min_carrier_up': min(r['carrierUp'] for r in rows), 'max_raw_error': max(r['rawError'] for r in rows)}
        all_rows = samples(raw)
        mounted = [r for r in all_rows if r['riding'] == 'true' and r['body'] != 'none']
        assert max(r['yaw'] for r in mounted) - min(r['yaw'] for r in mounted) > 15
        assert max(r['pitch'] for r in mounted) - min(r['pitch'] for r in mounted) > 4
        assert min(r['carrierUp'] for r in mounted) < .95
        first = next(r for r in accepted if r['label'] == 'accept-boarded')
        second = next(r for r in accepted if r['label'] == 'accept-second-boarded')
        first_index = 0 if client == 'dev' else 2
        second_index = 3 if client == 'dev' else 1
        a, b = first['carriers'][first_index], second['carriers'][second_index]
        assert a['body'] != b['body'] and len(a['riders']) == len(b['riders']) == 1 and a['riders'] == b['riders']
        transitions = []
        for previous, current in zip(all_rows, all_rows[1:]):
            if previous['riding'] == 'true' and previous['body'] != 'none' and current['riding'] == 'false':
                assert current['body'] == 'none' and current['travel'] == 0
                if current['perspective'] == 'FIRST_PERSON':
                    assert abs(current['cameraUp'] - math.cos(math.radians(current['pitch']))) < .001
                    transitions.append(current)
        assert len(transitions) >= 2
        for prefix in ['first', 'second']:
            e = next(e for e in events if e['kind'] == 'unloaded' and e['name'] == prefix)
            row = e['samples'][client]
            assert row['body'] == 'none' and row['riding'] == 'false' and row['perspective'] == 'FIRST_PERSON'
            assert float(row['travel']) == 0
            assert abs(float(row['cameraUp']) - math.cos(math.radians(float(row['pitch'])))) < .001
            start_name = 'FIRST_PERSON' if prefix == 'first' else 'zero-first'
            start = next(x for x in phases if x['name'] == start_name)
            window = samples(raw[start['offsets'][client]:e['offsets'][client]])
            assert any(a['riding'] == 'true' and a['body'] != 'none' and b['riding'] == 'false' and b['body'] == 'none'
                       for a, b in zip(window, window[1:])), (case, client, prefix, 'missing real transition')
            player = (root / case / ('accept-' + prefix + '-' + client + '-player.txt')).read_text()
            assert 'Health: 20.0f' in player and 'OnGround: 1b' in player and 'RootVehicle:' not in player and 'NoGravity: 1b' not in player
        server_hashes = set(re.findall(r'FD_SNAPSHOT side=server.*?tick=(\d+) sha256=([a-f0-9]+) railsSha256=([a-f0-9]+)', server))
        client_hashes = set(re.findall(r'FD_SNAPSHOT side=client.*?tick=(\d+) sha256=([a-f0-9]+) railsSha256=([a-f0-9]+)', raw.decode()))
        assert len(server_hashes & client_hashes) >= 50
        maximum_config = (root / case / ('accept-config-' + client + '-True-150-True.toml')).read_text()
        assert all(setting in maximum_config for setting in ['head_inertia_ms = 150', 'roll_stabilization = 100', 'pitch_stabilization = 100'])
        client_results[client] = {'phases': phase_results, 'first_person_dismounts': len(transitions), 'matched_snapshots': len(server_hashes & client_hashes), 'min_carrier_up': min(r['carrierUp'] for r in mounted)}
    assert (root / case / 'server.exit').read_text().strip() == '0' and 'BUILD SUCCESSFUL' in server
    saved = json.loads(subprocess.check_output([sys.executable, 'scripts/inspect-foundation-save.py', str(root / case / 'world/data/fourthdimension_foundation.dat')], text=True))['data']
    assert len(saved['vehicles']) == 1 and len(saved['rails']) == 8
    assert all(not c['occupants'] and not c['rider_seats'] for c in saved['vehicles'][0]['carriers'])
    results[case] = client_results
units = [ET.parse(p).getroot() for p in (root / 'unit-tests').glob('TEST-*.xml')]
assert sum(int(x.attrib['tests']) for x in units) == 19
assert all(x.attrib['errors'] == x.attrib['failures'] == '0' for x in units)
assert (root / 'native-base.exit').read_text().strip() == '0'
native = log('native-base.log.gz').decode()
assert 'All 36 required tests passed' in native
assert len(re.findall(r'FD_\w+_SKIPPED:', native)) == 7
cleanup = data('cleanup.json')
assert cleanup['handles_gone'] and cleanup['ports_closed'] and cleanup['settings_restored']
print(json.dumps({'cases': results, 'unit_tests': 19, 'scope': 'Revised numerical tracking in five camera modes, first-person settings and two carrier assignments; external-view occlusion remains open; no full-inversion, shader, dimension-change or performance certification'}, indent=2))
