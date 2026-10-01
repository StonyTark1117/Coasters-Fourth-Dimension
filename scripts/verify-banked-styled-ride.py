#!/usr/bin/env python3
"""Verify dev.6 saved banked GUI observations; screenshots require visual review.

This is a bounded circuit/lifecycle check, not arbitrary geometry, vehicle
clearance, every render backend, shader, or performance certification.
"""
import json
import math
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
evidence = root / 'docs/evidence/0.4'


def saved(name):
    result = json.loads((evidence / f'bank-gui-{name}.json').read_text())['data']
    assert result['schema'] == 5
    return result


def index(records, key):
    result = {tuple(r[key]): r for r in records}
    assert len(result) == len(records), 'Duplicate identity'
    return result


def position(value):
    x, y, z = value >> 38, value & 4095, (value >> 12) & 67108863
    return x, y if y < 2048 else y - 4096, z if z < 33554432 else z - 67108864


def excluding(record, *keys):
    return {k: v for k, v in record.items() if k not in keys}


baseline = saved('baseline')
styled = saved('styled-autosave')
final = saved('final-save')
old_rails = index(baseline['rails'], 'id')
old_cars = index(baseline['vehicles'], 'chassis')
assert len(old_rails) == 35 and len(old_cars) == 8
for state in (styled, final):
    rails, cars = index(state['rails'], 'id'), index(state['vehicles'], 'chassis')
    assert len(rails) == 43 and len(cars) == 9
    assert sum(len(v['carriers']) for v in cars.values()) == 22
    for identity, rail in old_rails.items():
        assert rails[identity] == rail, 'Unedited original rail changed'
    for identity, old in old_cars.items():
        car = cars[identity]
        assert excluding(car, 'carriers') == excluding(old, 'carriers')
        carriers = index(car['carriers'], 'body')
        previous = index(old['carriers'], 'body')
        assert carriers.keys() == previous.keys()
        for body, c in carriers.items():
            assert excluding(c, 'angle', 'omega') == excluding(previous[body], 'angle', 'omega')
            assert abs(c['angle'] - previous[body]['angle']) < .02
    new_rails = [r for k, r in rails.items() if k not in old_rails]
    assert len(new_rails) == 8
    bank = next(r for r in new_rails if position(r['from']) == (-342, 210, -148))
    curve = next(r for r in new_rails if position(r['from']) == (-326, 210, -148))
    entry = next(r for r in new_rails if position(r['from']) == (-350, 210, -156))
    assert all(r['family'] == 'arrow_controlled' and r['valid'] for r in new_rails)
    assert all(r['provider'] == 'fourthdimension:original' for r in new_rails)
    assert bank['appearance'] == {'id': 'coasterssimulatedextratypes:arrow_4d_dbl_spine', 'tint': 0x4A90E2}
    assert curve['appearance'] == {'id': 'coasterssimulatedextratypes:arrow_4d', 'tint': 0x9B59B6}
    assert bank['revision'] == curve['revision'] == 1
    assert bank['profile'] == [
        {'distance': 0., 'slope': 0., 'angle': 0.},
        {'distance': 8., 'slope': 0., 'angle': 2 * math.pi},
        {'distance': 16., 'slope': 0., 'angle': 0.},
    ]
    assert all(k['angle'] == 0 for k in curve['profile'])
    assert entry['revision'] == 0 and entry['appearance']['id'] == 'fourthdimension:original'
    car = next(v for k, v in cars.items() if k not in old_cars)
    assert car['family'] == 'arrow_controlled' and car['variant'] == 'arrow_four_rail_v1'
    assert car['boarding_ready'] and car['stopping'] and not car['restraints'] and not car['fault']
    assert all(c['capture'] and not c['occupants'] and not c['rider_seats'] for c in car['carriers'])
assert index(final['rails'], 'id') == index(styled['rails'], 'id'), 'Ride changed track/profile/style'
new_id = next(k for k in index(final['vehicles'], 'chassis') if k not in old_cars)
start = index(styled['vehicles'], 'chassis')[new_id]
end = index(final['vehicles'], 'chassis')[new_id]
assert end['revision'] == start['revision'] + 4, 'Unexpected operator transaction count'
assert index(start['carriers'], 'body').keys() == index(end['carriers'], 'body').keys()
player = json.loads((evidence / 'bank-gui-final-player.json').read_text())
assert not player['RootVehiclePresent'] and not player['NoGravityPresent'] and not player['NoGravity']
assert 'fourthdimension_restore_gravity' not in player['NeoForgeData']
log = (evidence / 'bank-gui-client.txt').read_text()
second = log.split('SECOND CLIENT:', 1)[1]
occupied = [line for line in second.splitlines()
            if 'occupants=[380df991-f603-344c-a090-369bad2a924a]' in line and 'riding=true' in line]
angles = [(float(re.search(r'snapshotAngle=([^ ]+)', line)[1]),
           float(re.search(r'nativeAngle=([^ ]+)', line)[1])) for line in occupied]
assert len(angles) >= 12 and all(math.isfinite(v) for pair in angles for v in pair)
bank_index = next(i for i, (s, n) in enumerate(angles) if s > 5.8 and n > 5.4)
reset_index = next(i for i in range(bank_index + 1, len(angles))
                   if abs(angles[i][0]) < .05 and abs(angles[i][1]) < .05)
assert any(s > 6.25 and n > 6.25 for s, n in angles[reset_index + 1:]), 'No distinct occupied level full-turn observation'
assert 'occupants=[] riding=false' in second
assert 'All dimensions are saved' in second and 'BUILD SUCCESSFUL' in second
assert 'BUILD FAILED' not in second and 'X connection' not in second
assert '22:35:47' in log and 'Reloading ResourceManager' in log
assert '22:35:48' in log and 'inspected 55 decorative model entries' in log
print('Banked GUI styles/profiles, occupied rotation, normal unload and clean save/quit verified.')
