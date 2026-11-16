"""Compare captured native state to its stopped-phase identity; never operate the game."""
import argparse
import json
import math
from pathlib import Path

p = argparse.ArgumentParser()
p.add_argument('baseline', type=Path)
p.add_argument('states', type=Path, nargs='+')
p.add_argument('--riders', action='store_true')
a = p.parse_args()

def read(path):
    value = json.loads(path.read_text())
    if 'answer' in value:
        value = json.loads(value['answer'].removeprefix('FD902_STATE '))
    return value

def identity(s):
    return {c['id']: sorted(x['id'] for x in c['carriers']) for c in s['cars']}

def seats(s):
    return sorted((r['id'], r['seat']) for r in s['riders'])

baseline = read(a.baseline)
expected = identity(baseline)
previous = None
results = []
for path in a.states:
    state = read(path)
    assert identity(state) == expected, 'Chassis/carrier identity changed'
    assert state['lead'] == baseline['lead'] and state['winch'] == baseline['winch']
    assert state['carriage']['id'] == baseline['carriage']['id']
    assert state['carriage']['loaded'], 'Persisted carriage unavailable'
    for car in state['cars']:
        assert car['loaded'] and set(car['links']) == set(expected), 'Native consist broken'
        assert all(c['loaded'] for c in car['carriers']), 'Native carrier unavailable'
    if a.riders:
        assert seats(baseline) and seats(state) == seats(baseline), 'Logical rider seat changed'
    result = {'file': str(path), 'tick': state['tick'], 'phase': state['phase'],
              'distance': state['distance'], 'cycles': state['cycles'],
              'riders': len(state['riders']), 'originalBodiesAndLinks': True}
    if previous:
        old = {c['id']: c for c in previous['cars']}
        result['maximumChassisDisplacement'] = max(
            math.dist(c['position'], old[c['id']]['position']) for c in state['cars'])
        result['carriageDisplacement'] = math.dist(
            state['carriage']['position'], previous['carriage']['position'])
    results.append(result)
    previous = state
print(json.dumps(results, indent=2))
