#!/usr/bin/env python3
"""Check recorded dev.14 one-rider boarding/reopen/ride evidence; never certifies all FD902."""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1] / 'docs/evidence/0.9'


def read(name):
    return json.loads((root / name).read_text())


def keyed(data):
    return {tuple(v['chassis']): v for v in data['vehicles']}


saved, restored, final = [read(f'occupied-{name}-foundation.json')['data']
                          for name in ['saved', 'restored', 'final']]
s, r, f = [keyed(data) for data in [saved, restored, final]]
assert s.keys() == r.keys() == f.keys() and len(s) == 13
assert all(data['schema'] == 5 and len(data['rails']) == 46 and data['rails'] == saved['rails']
           for data in [saved, restored, final])
assert all(sum(len(v['carriers']) for v in data['vehicles']) == 30
           for data in [saved, restored, final])
assert all(not v['fault'] for data in [saved, restored, final] for v in data['vehicles'])
sp, rp, fp = [read(f'occupied-{name}-player.json') for name in ['saved', 'restored', 'final']]
assert sp['UUID'] == rp['UUID'] == fp['UUID']
assignments = [(v, c, slot) for v in saved['vehicles'] for c in v['carriers'] for slot in c['rider_seats']]
assert len(assignments) == 1
vehicle, carrier, slot = assignments[0]
assert vehicle['family'] == 'pax_shuttle' and vehicle['restraints'] and slot['player'] == sp['UUID']
assert slot['seat'] == 274877911039 and carrier['occupants'] == [sp['UUID']]
for id in s:
    for key in ['chassis', 'family', 'variant', 'boundFrom', 'boundTo']:
        assert s[id][key] == r[id][key] == f[id][key]
    assert {k: x for k, x in s[id].items() if k != 'carriers'} == {
        k: x for k, x in r[id].items() if k != 'carriers'}
    for before, after, end in zip(s[id]['carriers'], r[id]['carriers'], f[id]['carriers']):
        assert {k: x for k, x in before.items() if k not in ['angle', 'omega']} == {
            k: x for k, x in after.items() if k not in ['angle', 'omega']}
        for key in ['body', 'seats', 'side', 'pivot_height', 'longitudinal', 'child_pivot_offset']:
            assert before[key] == end[key]
        assert not end['rider_seats'] and not end['occupants'] and end['capture']
    assert not f[id]['restraints'] and f[id]['boarding_ready'] and f[id]['stopping']
for p in [sp, rp]:
    assert not p.get('NoGravity', 0)
    mount = p['RootVehicle']
    assert mount['Attach'] == mount['Entity']['UUID'] and mount['Entity']['id'] == 'create:seat'
assert sp['RootVehicle']['Entity']['Pos'] == rp['RootVehicle']['Entity']['Pos']
assert sp['RootVehicle']['Attach'] != rp['RootVehicle']['Attach']  # Transient seat entity recreated.
assert 'RootVehicle' not in fp and not fp.get('NoGravity', 0) and fp['OnGround']
assert fp['Pos'] == [94.5, 86.0, -26.5]
sw, rw, fw = [read(f'occupied-{name}-winch.json') for name in ['saved', 'restored', 'final']]
assert {k: v for k, v in sw.items() if k.startswith('shuttle_')} == {
    k: v for k, v in rw.items() if k.startswith('shuttle_')}
for w in [sw, rw, fw]:
    assert w['shuttle_phase'] == 'BOARDING' and not w['shuttle_fault']
    assert w['shuttle_carriage'] == sw['shuttle_carriage'] and w['shuttle_cars'] == sw['shuttle_cars']
    assert w['shuttle_distance'] == w['shuttle_station'] == sw['shuttle_station']
assert sw['shuttle_cycles'] == rw['shuttle_cycles'] == 1 and sw['shuttle_maximum'] == 0
assert fw['shuttle_cycles'] == 2 and fw['shuttle_maximum'] > fw['shuttle_station'] + 25
records = read('occupied-ride-phases.json')
phases = [record['phase'] for i, record in enumerate(records)
          if i == 0 or record['phase'] != records[i-1]['phase']]
assert phases == ['LIFTING', 'OUTBOUND', 'RETURNING', 'CAPTURING', 'BOARDING']
for record in records:
    assert f'block data: "{record["phase"]}"' in record['native_line']
for name in ['occupied-first-flags.txt', 'occupied-cold-flags.txt']:
    flags = (root / name).read_text()
    for flag in ['validation', 'ponderValidation', 'schedulerValidation', 'shuttleValidation']:
        assert f'-Dfourthdimension.{flag}=false' in flags
logs = [(root / f'occupied-{name}-events.txt').read_text() for name in ['first', 'cold', 'ride']]
for log in logs:
    assert 'BUILD SUCCESSFUL' in log and 'All dimensions are saved' in log
    assert 'Exception in' not in log and 'moved too' not in log
assert logs[0].count('Seat has the following entity data: [I;') == 1
assert logs[1].count('Seat has the following entity data: [I;') == 1
assert logs[2].count('Seat has the following entity data: [I;') == 2  # Before and after the ride.
assert 'Player logged in with tracking point in non-loaded sub-level. Attempting to load.' in logs[1]
assert 'Player logged in with tracking point in non-loaded sub-level. Attempting to load.' in logs[2]
assert 'Found no elements matching RootVehicle' in logs[2]
assert 'Found no elements matching NoGravity' in logs[2] and 'No entity was found' in logs[2]
print('Verified recorded one-rider PAX boarding cold restores, full post-reopen ride and normal unloading; broader FD902 remains open.')
