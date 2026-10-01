#!/usr/bin/env python3
"""Check retained two-client revised/PAX combined-pack evidence, not a live world."""
import json
import math
import re
import sys
import uuid
from pathlib import Path

root = Path(sys.argv[1])


def saved(name):
    return json.loads((root / (name + '.json')).read_text())['data']


def selected(data):
    return [v for v in data['vehicles'] if v['family'] in ('zacspin_revised', 'pax_shuttle')]


def fields(line):
    return dict(re.findall(r'(\w+)=([^ ]+)', line.split(' camera=', 1)[0]))


def packets(name):
    rows = []
    for line in (root / (name + '-events.txt')).read_text().splitlines():
        if 'FD_SNAPSHOT ' in line:
            row = fields(line)
            rows.append(tuple(row[k] for k in ('player', 'dimension', 'tick', 'sha256', 'railsSha256', 'rails', 'vehicles')))
    return rows


before, frozen, resumed, running, returned, exited = (
    saved(n) for n in ('occupied-before', 'occupied-restarted', 'resumed', 'lifting', 'returned', 'exited'))
assert before == frozen
assert before['schema'] == 5 and len(before['rails']) == 35 and len(before['vehicles']) == 8
assert before['rails'] == resumed['rails'] == returned['rails'] == exited['rails']
assert len(selected(before)) == 5
for a, b, c, d, e in zip(*(selected(s) for s in (before, resumed, running, returned, exited))):
    assert a['stopping'] and a['boarding_ready'] and a['restraints'] and not a['fault']
    assert {k:v for k,v in a.items() if k != 'carriers'} == {k:v for k,v in b.items() if k != 'carriers'}
    for x, y in zip(a['carriers'], b['carriers']):
        assert {k:v for k,v in x.items() if k not in ('angle', 'omega')} == {k:v for k,v in y.items() if k not in ('angle', 'omega')}
    assert not c['stopping'] and not c['boarding_ready'] and c['restraints'] and not c['fault']
    assert d['stopping'] and d['boarding_ready'] and d['restraints'] and not d['fault']
    assert e['stopping'] and e['boarding_ready'] and not e['restraints'] and not e['fault']
    for x, y, z in zip(a['carriers'], d['carriers'], e['carriers']):
        assert x['body'] == y['body'] == z['body']
        assert x['occupants'] == y['occupants'] and x['rider_seats'] == y['rider_seats']
        assert not z['occupants'] and not z['rider_seats']
for family in ('zacspin_revised', 'pax_shuttle'):
    assert sum(len(c['occupants']) for v in selected(before) if v['family'] == family for c in v['carriers']) == 1

for name in ('ready', 'resumed', 'return-readouts'):
    assert (root / (name + '.txt')).read_text().count('Ready to dispatch') == 2
assert '1 / 8' in (root / 'occupancy.txt').read_text()
assert '1 / 16' in (root / 'occupancy.txt').read_text()
assert '0 / 8' in (root / 'stop2.txt').read_text() and '0 / 16' in (root / 'stop2.txt').read_text()
phases = (root / 'lifting.txt').read_text() + (root / 'phases.txt').read_text()
for phase in ('LIFTING', 'OUTBOUND', 'RETURNING', 'CAPTURING', 'BOARDING'):
    assert 'shuttle_phase: "' + phase + '"' in phases
for label in ('Lifting', 'Outbound', 'Returning', 'Capturing', 'Boarding'):
    assert label in phases
assert 'shuttle_cycles: 1' in phases and 'shuttle_phase: "FAULT"' not in phases
assert 'has the following block data: 0b' in (root / 'dispatch-preflight.txt').read_text()
assert 'unlinked ' not in (root / 'server2-events.txt').read_text()
links = re.findall(r'restored link ([\da-f-]+) <-> ([\da-f-]+)', (root / 'server2-events.txt').read_text())
chassis = {str(uuid.UUID(bytes=b''.join((n & 0xffffffff).to_bytes(4, 'big') for n in v['chassis'])))
           for v in selected(before) if v['family'] == 'pax_shuttle'}
assert len(links) == 3 and {n for pair in links for n in pair} == chassis

server = set(packets('server1') + packets('server2'))
report = {'scope': 'Combined pack; two local clients; one revised car and four-car PAX, one rider each.',
          'cold_restart_exact': True, 'pax_completed_cycles': 1, 'clients': {}}
received = {}
for name in ('dev', 'observer'):
    rows = packets(name)
    assert len(rows) > 100 and all(r in server for r in rows)
    received[name] = {r[1:] for r in rows}
    camera = [fields(l) for l in (root / (name + '-events.txt')).read_text().splitlines() if 'FD_IMMERSION' in l]
    mounted = [r for r in camera if r['riding'] == 'true']
    assert len(mounted) > 100 and len({r['body'] for r in mounted}) == 1
    assert all(r['metric'] == 'normalized-v1' and r['body'] != 'none' and 0 <= float(r['rawError']) < 1e-6 for r in mounted)
    last = camera[-1]
    assert last['riding'] == 'false' and last['body'] == 'none' and float(last['travel']) == 0
    assert abs(float(last['cameraUp']) - math.cos(math.radians(float(last['pitch'])))) < .001
    yaw_span = max(float(r['yaw']) for r in mounted) - min(float(r['yaw']) for r in mounted)
    assert yaw_span > 5
    lines = [(root / (name + '-' + suffix + '-camera.txt')).read_text().splitlines()[-1] for suffix in ('before', 'after')]
    assert fields(lines[0])['body'] == fields(lines[1])['body'] != 'none'
    q, r = ([float(x) for x in re.findall(r'[+-]?\d\.\d+E[+-]\d+', line.split('camera=', 1)[1])] for line in lines)
    assert len(q) == len(r) == 4
    dot = abs(sum(x*y for x,y in zip(q,r))) / math.sqrt(sum(x*x for x in q) * sum(x*x for x in r))
    delta = math.degrees(2 * math.acos(min(1, dot)))
    assert delta < .25
    report['clients'][name] = {'matched_packets': len(rows), 'mounted_samples': len(mounted),
        'cold_view_delta_degrees': delta, 'mouse_look_yaw_span': yaw_span,
        'minimum_carrier_up': min(float(r['carrierUp']) for r in mounted),
        'maximum_raw_error_radians': max(float(r['rawError']) for r in mounted), 'normal_exit_restored': True}
report['identical_both_client_snapshots'] = len(received['dev'] & received['observer'])
assert report['identical_both_client_snapshots'] > 100

manifest = json.loads((root / 'manifest.json').read_text())
expected = ('Track Styles 1.2.0', 'Engineered 1.1.1', 'Immersion 0.2.3', 'Magnetized 1.1.0', 'Additions 0.2.0')
for role in ('server1', 'server2', 'dev', 'observer'):
    assert manifest[role]['exit'] == 0
    assert all(mod in (root / (role + '-mods.txt')).read_text() for mod in expected)
# Retain the invalid first attempt, without using it as a passing cycle.
assert any(v['fault'] for v in selected(saved('invalid-running')) if v['family'] == 'pax_shuttle')
assert 'unlinked ' in (root / 'invalid-events.txt').read_text()
report['invalid_first_attempt_preserved'] = True
print(json.dumps(report, indent=2))
