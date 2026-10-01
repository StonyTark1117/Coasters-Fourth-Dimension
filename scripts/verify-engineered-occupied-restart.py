#!/usr/bin/env python3
"""Check cold-restart passenger retention, mechanical persistence and FE operation."""
import copy
import json
import math
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
runs = json.loads((root / 'runs.json').read_text())
expected = {'ARROW': (2, 12345), 'ZACSPIN': (1, 12346),
            'FREE_SPIN': (1, 12347), 'ZACSPIN_REVISED': (1, 12348)}
def records(name, label):
    result = [d for d in json.loads((root / (name + '-records.json')).read_text()) if d['label'] == label]
    assert len(result) == 4
    return {d['family']: d for d in result}
def assignments(d):
    return {c['chassis']: {a['body']: [r['id'] for r in a['riders']] for a in c['carriers']} for c in d['cars']}
def state(d, occupied, held):
    cars, _ = expected[d['family']]
    assert d['required'] and len(d['cars']) == cars
    for c in d['cars']:
        assert not c['fault'] and c['consist'] == cars
        assert math.isfinite(c['speed']) and math.isfinite(c['distance'])
        assert c['restraints'] == occupied
        if held:
            assert c['ready'] and all(a['capture'] for a in c['carriers'])
        for a in c['carriers']:
            assert a['loaded']
            assert all(r['alive'] and r['mounted'] and r['carrier'] == a['body'] for r in a['riders'])
        assert sum(len(a['riders']) for a in c['carriers']) == int(occupied)
def mechanics(name):
    data = copy.deepcopy(json.loads((root / (name + '.json')).read_text())['data'])
    assert data['schema'] == 5 and len(data['rails']) == 4 and len(data['vehicles']) == 5
    data['rails'].sort(key=lambda r: r['id'])
    data['vehicles'].sort(key=lambda v: v['chassis'])
    for v in data['vehicles']:
        for c in v['carriers']:
            # These are live physical measurements, not saved seat or mechanical identity.
            del c['angle'], c['omega']
    return data
def controller(s):
    return re.sub(r'CoastersEngineeredLastCart:\{[^}]*\},', '', s)

original = records('combined-before', 'before')
lost = records('combined-after', 'restored')
fixed = records('fixed-before', 'fixed_before')
assert sum(len(a['riders']) for d in lost.values() for c in d['cars'] for a in c['carriers']) == 0
trace = (root / 'probe.txt').read_text()
assert trace.count('mounting=true removed=false binding=true held=true saved=true') == 5
for family, d in fixed.items():
    state(d, True, True)
    assert assignments(d) == assignments(original[family])
    assert d['energy'] == original[family]['energy'] == expected[family][1]
assert mechanics('combined-before') == mechanics('fixed-before')
assert len({a['body'] for d in fixed.values() for c in d['cars'] for a in c['carriers']}) == 14
assert len({r['id'] for d in fixed.values() for c in d['cars'] for a in c['carriers'] for r in a['riders']}) == 5
report = {'scope': 'Five native pig passengers; four straight FE-driven families; cold dedicated-server restart', 'dedicated': {}}
for case in ('fixed-after', 'isolated'):
    assert runs[case]['exit_status'] == 0 and runs[case]['operation_exit_status'] == 0
    mods = (root / (case + '-mods.txt')).read_text()
    assert 'Engineered 1.1.1' in mods
    if case == 'fixed-after':
        assert all(v in mods for v in ('Track Styles 1.2.0', 'Additions 0.2.0', 'Magnetized 1.1.0', 'Immersion 0.2.3'))
    else:
        assert all(v not in mods for v in ('coasterfins', 'coastersmagnetized', 'coasters_immersion', 'coasterssimulatedextratypes'))
    restored = records(case, 'restored')
    forward = records(case, 'forward')
    braked = records(case, 'braked')
    captured = records(case, 'captured')
    unloaded = records(case, 'unloaded')
    assert mechanics(case + '-restored') == mechanics('fixed-before')
    result = {}
    for family, d in restored.items():
        for phase in (d, forward[family], braked[family], captured[family]):
            state(phase, True, phase['label'] in ('restored', 'captured'))
            assert assignments(phase) == assignments(fixed[family])
        state(unloaded[family], False, True)
        assert d['energy'] == expected[family][1]
        assert d['span'] == fixed[family]['span']
        assert controller(d['motor']) == controller(fixed[family]['motor'])
        if case == 'fixed-after':
            assert d['anchor'] == fixed[family]['anchor']
        assert 0 <= forward[family]['energy'] < d['energy']
        old = {c['chassis']: c for c in d['cars']}
        assert all(c['speed'] > 1 and c['distance'] > old[c['chassis']]['distance'] + 2 for c in forward[family]['cars'])
        assert braked[family]['energy'] > 0 and all(abs(c['speed']) < 1.3 for c in braked[family]['cars'])
        result[family] = {'restored_fe': d['energy'], 'after_launch_fe': forward[family]['energy'],
                          'after_braking_fe': braked[family]['energy'], 'riders': len(d['cars'])}
    commands = (root / (case + '-commands.txt')).read_text()
    assert commands.count('Pig has the following entity data:') == 5
    assert 'unexpected error' not in commands
    assert forward['ZACSPIN_REVISED']['energy'] == 0 and 'CoastersEngineeredStarving:1b' in forward['ZACSPIN_REVISED']['motor']
    report['dedicated'][case] = result

for profile, skips in {'base': 5, 'engineered': 3, 'styles': 5, 'additions': 3,
                       'magnetized': 4, 'immersion': 5, 'combined': 0}.items():
    name = 'native-' + profile
    text = (root / (name + '.txt')).read_text()
    assert runs[name]['exit_status'] == 0 and 'All 28 required tests passed' in text
    assert 'BUILD SUCCESSFUL' in text and 'required tests failed' not in text
    assert text.count('_SKIPPED:') == skips
    if profile in ('engineered', 'combined'):
        assert 'FD_ENGINEERED_ENERGY_COMPLETE families=4 cars=5 riders=5' in text
        assert 'FD_ENGINEERED_ENERGY_TUNING_RESTORED buffer=100000 regen=0.3' in text
    assert 'FD_SAVED_PASSENGER nativeReplay=true unknownRejected=true liveUnchanged=true' in text
    assert sum(l.startswith('FD_TRACK_PARTIAL_RELOADED family=') for l in text.splitlines()) == 10
unit = 0
for p in (root / 'junit').glob('TEST-*.xml'):
    suite = ET.parse(p).getroot()
    assert all(int(suite.attrib[k]) == 0 for k in ('failures', 'errors', 'skipped'))
    unit += int(suite.attrib['tests'])
assert unit == 19
assert all(runs[n]['exit_status'] == 0 for n in ('setup', 'before', 'combined-before', 'combined-after', 'probe', 'fixed-before'))
assert 'within 1024 blocks' in (root / 'setup-commands.txt').read_text()
assert 'Track placement is out of reach' in (root / 'before-commands.txt').read_text()
assert 'Vehicle chassis is unavailable' in (root / 'combined-after-commands.txt').read_text()
report.update({'native_profiles': 7, 'registered_tests_per_profile': 28, 'unit_tests': unit,
               'preserved_passengers': 5, 'preserved_carrier_bodies': 14})
print(json.dumps(report, indent=2))
