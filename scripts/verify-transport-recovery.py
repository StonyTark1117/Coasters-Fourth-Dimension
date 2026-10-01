#!/usr/bin/env python3
"""Verify retained transport/tracking evidence; does not infer a network race fix."""
import json
import math
import re
import sys
import uuid
from pathlib import Path

root = Path(sys.argv[1])


def saved(name):
    return json.loads((root / (name + '.json')).read_text())['data']


def stable(value):
    if isinstance(value, dict):
        return {k: stable(v) for k, v in value.items() if k not in ('angle', 'omega')}
    if isinstance(value, list):
        return [stable(v) for v in value]
    return value


def body_id(values):
    return str(uuid.UUID(bytes=b''.join((v & 0xffffffff).to_bytes(4, 'big') for v in values)))


def fields(line):
    return dict(re.findall(r'(\w+)=([^ ]+)', line.split(' camera=', 1)[0]))


def seconds(text):
    h, m, s = map(float, text.split(':'))
    return h * 3600 + m * 60 + s


def packets(text):
    return [tuple(fields(l)[k] for k in ('player', 'dimension', 'tick', 'sha256', 'railsSha256', 'rails', 'vehicles'))
            for l in text.splitlines() if 'FD_SNAPSHOT ' in l]


before = saved('streaming-before')
expected = {body_id(c['body']) for v in before['vehicles']
            if v['family'] in ('sns_free_spin', 'zacspin_revised', 'pax_shuttle') for c in v['carriers']}
assert len(expected) == 16
manifest = json.loads((root / 'manifest.json').read_text())
assert manifest['client_exit'] == 0 and manifest['config_restored']
report = {'scope': 'One client, combined pack; tracking range transitions and matched occupied cold restarts.', 'cases': {}}
for case, info in manifest['cases'].items():
    server = (root / (case + '-server-events.txt')).read_text()
    client = (root / (case + '-client-events.txt')).read_text()
    warnings = (root / (case + '-client-warnings.txt')).read_text()
    assert info['server_exit'] == 0
    rows = packets(client)
    assert len(rows) >= 10 and all(row in set(packets(server)) for row in rows)
    assert ('Received UDP authentication request' in client) == info['attempt_udp_networking']
    assert ('UDP authentication complete' in server) == info['attempt_udp_networking']
    for mod in ('Track Styles 1.2.0', 'Engineered 1.1.1', 'Immersion 0.2.3', 'Magnetized 1.1.0', 'Additions 0.2.0'):
        assert mod in (root / (case + '-mods.txt')).read_text()
    native = [(seconds(l[1:9]), fields(l)['body']) for l in client.splitlines() if 'FD_CLIENT ' in l]
    assert {body for _, body in native} == expected
    errors = [l for l in warnings.splitlines() if 'movement packet for a non-existent sub-level' in l]
    result = {'matched_snapshots': len(rows), 'native_carriers_observed': len(expected), 'missing_body_errors': len(errors)}
    if not case.endswith('cold'):
        assert stable(before) == stable(saved(case + '-after'))
        transitions = []
        for line in (root / (case + '-commands.txt')).read_text().splitlines():
            if line.startswith('2026-'):
                time = seconds(line.split('T')[1])
            elif line.startswith('tp Dev '):
                transitions.append((time, float(line.split()[2]) > 1000))
        assert len(transitions) == 13 and sum(far for _, far in transitions) == 6
        for index, (start, far) in enumerate(transitions):
            end = transitions[index + 1][0] if index + 1 < len(transitions) else time
            # Exclude both boundaries: log timestamps omit fractional seconds.
            observed = {body for stamp, body in native if start + 1 <= stamp < end - 1}
            assert observed == (set() if far else expected), (case, index, len(observed))
            assert sum(start + 1 <= seconds(l[1:9]) < end - 1 for l in client.splitlines()
                       if 'FD_SNAPSHOT ' in l) >= 2, (case, index, 'client stopped receiving')
        assert not errors
        result['verified_departures_and_returns'] = 6
    else:
        cold = saved('cold-before')
        assert cold == saved(case + '-frozen')
        assert stable(cold) == stable(saved(case + '-resumed'))
        ride = next(v for v in cold['vehicles'] if v['family'] == 'zacspin_revised')
        occupied = [c for c in ride['carriers'] if c['occupants']]
        assert len(occupied) == 1 and len(occupied[0]['occupants']) == 1
        mounted = [fields(l) for l in client.splitlines() if 'FD_IMMERSION' in l and 'riding=true' in l]
        assert len(mounted) > 20
        assert all(r['body'] == body_id(occupied[0]['body']) and 0 <= float(r['rawError']) < 1e-6 for r in mounted)
        if case.startswith('udp'):
            assert errors and len({l[1:9] for l in errors}) == 1
            last_error = max(seconds(l[1:9]) for l in errors)
            assert {body for stamp, body in native if stamp > last_error + 2} == expected
            result['recovered_after_error_burst'] = True
        else:
            assert not errors
        result['occupied_restart_exact'] = True
        result['restored_mounted_samples'] = len(mounted)
    report['cases'][case] = result

final = saved('final-exited')
ride = next(v for v in final['vehicles'] if v['family'] == 'zacspin_revised')
assert ride['boarding_ready'] and not ride['restraints'] and not ride['fault']
assert all(not c['occupants'] and not c['rider_seats'] for c in ride['carriers'])
camera = [fields(l) for l in (root / 'udp-cold-client-events.txt').read_text().splitlines() if 'FD_IMMERSION' in l][-1]
assert camera['body'] == 'none' and camera['riding'] == 'false' and float(camera['travel']) == 0
assert abs(float(camera['cameraUp']) - math.cos(math.radians(float(camera['pitch'])))) < .001
report['normal_exit_after_udp_recovery'] = True
report['diagnosis'] = 'Supports cross-transport ordering; no packet-level ordering capture and no upstream fix claimed.'
print(json.dumps(report, indent=2))
