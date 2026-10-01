#!/usr/bin/env python3
"""Verify recorded dev.12 observations; does not run a client or certify open gates."""
import json
import re
from pathlib import Path

root = Path(__file__).resolve().parents[1] / 'docs/evidence/0.4'
def read(name):
    return json.loads((root / name).read_text())

def cycles(name, count):
    rows = read(name)
    assert [r['cycle'] for r in rows] == list(range(1, count + 1))
    for row in rows:
        for key in ['resource_manager_starts', 'catalog_bakes', 'query_acknowledgements']:
            assert row[key] == row['cycle'] + 1
    return rows

rows = cycles('scheduler-reload-cycles.json', 64)
assert rows[6]['reload_seconds'] is None and 'same JVM' in rows[6]['observation']
assert all(0 < r['reload_seconds'] < 45 for r in rows if r['reload_seconds'] is not None)
cycles('scheduler-cold-reload-cycles.json', 4)
logs = (root / 'scheduler-client-events.txt').read_text()
assert 'FD_SCHEDULER passed schedules=512 completedTasks=3072 emptySchedules=128 maxWorkers=6' in logs
assert logs.count('Reloading ResourceManager:') == 65
assert logs.count('inspected 55 decorative model entries') == 65
assert logs.count('[CHAT] Dev has the following entity data: {count: 1, id: "minecraft:red_dye"}') == 65
assert 'id: "create:seat"' in logs and 'riding=true' in logs and 'BUILD SUCCESSFUL' in logs
peak = max(float(m[1]) for m in re.finditer(r'nativeAngle=([^ ]+) occupants=\[[^]]+\] riding=true', logs))
assert peak > 6.2

before = read('scheduler-before-ride.json')['data']
final = read('scheduler-final-save.json')['data']
cold = read('scheduler-cold-save.json')['data']
assert before['schema'] == final['schema'] == cold['schema'] == 5
assert before['rails'] == final['rails'] == cold['rails']
assert len(final['rails']) == 43
families = {'arrow_controlled', 'zacspin_legacy', 'sns_free_spin', 'zacspin_revised', 'pax_shuttle'}
styles = [r for r in final['rails'] if r['appearance']['id'] != 'fourthdimension:original']
assert len(styles) == 1
assert styles[0]['appearance'] == {'tint': 4886754, 'id': 'coasterssimulatedextratypes:arrow_4d_dbl_spine'}
assert [k['distance'] for k in styles[0]['profile']] == [0, 18, 36]
assert abs(styles[0]['profile'][1]['angle'] - 6.283185307179586) < 1e-12
for data in [before, final, cold]:
    assert len(data['vehicles']) == 9
    assert {v['family'] for v in data['vehicles']} == families
    assert sum(len(v['carriers']) for v in data['vehicles']) == 22
    assert all(not c['occupants'] and not c['rider_seats'] and c['capture'] for v in data['vehicles'] for c in v['carriers'])
assert all(not v['fault'] and not v['restraints'] and v['stopping'] and v['boarding_ready'] for v in final['vehicles'])

def mechanical(vehicle):
    return {k: vehicle[k] for k in ['chassis', 'family', 'variant', 'boundFrom', 'boundTo']}

def keyed(data):
    return {tuple(v['chassis']): v for v in data['vehicles']}

b, f, c = keyed(before), keyed(final), keyed(cold)
assert b.keys() == f.keys() == c.keys()
for id in f:
    assert mechanical(b[id]) == mechanical(f[id]) == mechanical(c[id])
    for a, d in zip(f[id]['carriers'], c[id]['carriers']):
        assert {k: v for k, v in a.items() if k not in ['angle', 'omega']} == {k: v for k, v in d.items() if k not in ['angle', 'omega']}
    assert [x['body'] for x in b[id]['carriers']] == [x['body'] for x in f[id]['carriers']]
    if f[id]['family'] == 'pax_shuttle':
        # This is the recorded failure, not a passing PAX lifecycle claim.
        assert c[id]['fault'] == 'Shuttle lead car unavailable' and c[id]['restraints']
    else:
        assert {k: v for k, v in f[id].items() if k != 'carriers'} == {k: v for k, v in c[id].items() if k != 'carriers'}
for name in ['scheduler-final-player.json', 'scheduler-cold-player.json']:
    player = read(name)
    assert 'RootVehicle' not in player and not player.get('NoGravity', 0)
assert read('scheduler-unit.json') == {'tests': 25, 'failures': 0, 'errors': 0, 'skipped': 0}
report = read('scheduler-validation.json')
assert report['cold_client']['new_pax_faults'] == 4
assert not report['cold_client']['accepted_as_fault_free_lifecycle']
print('Verified recorded dev.12 scheduler/reload/Arrow ride data; four cold PAX faults remain explicitly open.')
