#!/usr/bin/env python3
"""Check captured Arrow view-recovery samples and seat lifecycle evidence."""
import json
import math
import re
import sys
from pathlib import Path

root = Path(sys.argv[1])

def samples(name):
    rows = []
    for line in (root / name).read_text().splitlines():
        if 'FD_IMMERSION' not in line:
            continue
        row = dict(re.findall(r'(\w+)=([^ ]+)', line.split(' camera=', 1)[0]))
        assert row['metric'] == 'normalized-v1'
        for k in ('yaw', 'pitch', 'carrierUp', 'cameraUp', 'rawError', 'travel'):
            row[k] = float(row[k])
            assert math.isfinite(row[k])
        row['q'] = [float(x) for x in re.findall(r'[+-]?\d\.\d+E[+-]\d+', line.split('camera=', 1)[1])]
        assert len(row['q']) == 4
        rows.append(row)
    assert rows
    return rows

def camera_angle(a, b):
    q, r = a['q'], b['q']
    dot = abs(sum(x*y for x, y in zip(q, r)))
    dot /= math.sqrt(sum(x*x for x in q) * sum(x*x for x in r))
    return math.degrees(2*math.acos(min(1, dot)))

def delta(a, b):
    return abs((a-b+180) % 360-180)

report = {'scope': 'One real Arrow rider, exact combined pack; not all-family acceptance', 'phases': {}}
for label, prefix, stationary in [('initial_cold', '', True), ('live_reconnect', 'live-', True),
                                  ('moving_reconnect', 'moving-', False), ('frozen_reconnect', 'frozen-', True),
                                  ('final_cold', 'final-cold-', True),
                                  ('open_frozen_reconnect', 'open-live-', True), ('open_cold', 'open-cold-', True)]:
    before, after = samples(prefix+'before.txt'), samples(prefix+'after.txt')
    a, b = before[-1], after[-1]
    assert a['body'] == b['body'] != 'none'
    assert all(r['riding'] == 'true' and r['body'] == a['body'] and 0 <= r['rawError'] < 1e-6 for r in before+after)
    yaw, pitch = delta(a['yaw'], b['yaw']), delta(a['pitch'], b['pitch'])
    assert yaw < .25 and pitch < .25, (label, 'changed saved local mouse angles', yaw, pitch)
    measured = {'yaw_delta_degrees': yaw, 'pitch_delta_degrees': pitch, 'same_carrier': a['body']}
    if stationary:
        angle = camera_angle(a, b)
        assert angle < .25, (label, 'rendered view turned', angle)
        measured['rendered_view_delta_degrees'] = angle
    else:
        assert any(r['carrierUp'] < -.9 and r['cameraUp'] < -.5 for r in before)
        measured['disconnected_during_inversion'] = True
    report['phases'][label] = measured

old_a, old_b = samples('baseline-before.txt')[-1], samples('baseline-after.txt')[-1]
report['old_cold_turn_degrees'] = camera_angle(old_a, old_b)
assert report['old_cold_turn_degrees'] > 170
assert all(r['body'] == 'none' for r in samples('rejoin-after.txt')), 'Missing pre-cleanup frozen tracking failure'

before = json.loads((root/'final-before-save.json').read_text())
after = json.loads((root/'final-after-frozen-save.json').read_text())
assert before == after
v = before['data']['vehicles'][0]
assert v['boarding_ready'] and v['restraints'] and not v['fault']
assert sum(len(c['occupants']) for c in v['carriers']) == 1
assert sum(len(c['rider_seats']) for c in v['carriers']) == 1
exited = json.loads((root/'final-exited-save.json').read_text())['data']
assert before['data']['rails'] == exited['rails']
v = exited['vehicles'][0]
assert v['boarding_ready'] and not v['restraints'] and not v['fault']
assert all(not c['occupants'] and not c['rider_seats'] for c in v['carriers'])
rows = samples('client2-events.txt')
exits = [b for a, b in zip(rows, rows[1:]) if a['riding'] == 'true' and b['riding'] == 'false']
assert exits
last = exits[-1]
assert last['body'] == 'none' and last['travel'] == 0
assert abs(last['cameraUp'] - math.cos(math.radians(last['pitch']))) < .001
open_before = json.loads((root/'open-before-save.json').read_text())
open_after = json.loads((root/'open-after-frozen-save.json').read_text())
assert open_before == open_after
open_v = open_before['data']['vehicles'][0]
assert not open_v['restraints'] and open_v['boarding_ready']
assert sum(len(c['occupants']) for c in open_v['carriers']) == 1
open_exit = json.loads((root/'open-exited-save.json').read_text())['data']['vehicles'][0]
assert all(not c['occupants'] and not c['rider_seats'] for c in open_exit['carriers'])
report['open_restraint_restart_preserved_seat_then_exited'] = True
for name in ['build5.txt', 'base-last.txt']:
    assert 'All 24 required tests passed' in (root/name).read_text()
report.update(saved_ride_data_exact=True, normal_exit_cleared_seat=True, normal_exit_camera_restored=True)
print(json.dumps(report, indent=2))
