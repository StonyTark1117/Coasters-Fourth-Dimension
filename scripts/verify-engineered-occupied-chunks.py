#!/usr/bin/env python3
"""Verify retained parked/moving native-passenger chunk interruption evidence."""
import copy
import json
import math
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
runs = json.loads((root / 'runs.json').read_text())
families = {'ARROW': (2, 12345), 'ZACSPIN': (1, 12346),
            'FREE_SPIN': (1, 12347), 'ZACSPIN_REVISED': (1, 12348)}

def assignments(d):
    return {c['chassis']: {a['body']: [r['id'] for r in a['riders']]
                          for a in c['carriers']} for c in d['cars']}

def controller(s):
    # This native diagnostic changes as held bodies settle after reloading.
    return re.sub(r'CoastersEngineeredLastCart:\{[^}]*\},', '', s)

def mechanics(name):
    data = copy.deepcopy(json.loads((root / (name + '-save.json')).read_text())['data'])
    assert data['schema'] == 5 and len(data['rails']) == 4 and len(data['vehicles']) == 5
    data['rails'].sort(key=lambda r: r['id'])
    data['vehicles'].sort(key=lambda v: v['chassis'])
    for v in data['vehicles']:
        # Final save follows intentional normal unloading. Identity/configuration remain exact.
        assert v['revision'] == (1 if name == 'baseline' else 4)
        assert v['restraints'] == int(name == 'baseline')
        del v['restraints'], v['revision']
        for c in v['carriers']:
            for field in ('angle', 'omega', 'occupants'):
                del c[field]
    return data

summary = {}
for case in ('combined', 'isolated'):
    assert runs[case]['exit_status'] == runs[case]['operation_exit_status'] == 0
    records = json.loads((root / (case + '-records.json')).read_text())
    probes = json.loads((root / (case + '-probes.json')).read_text())
    phases = {}
    for label in ('before_chunks', 'parked_restored', 'moving_before', 'moving_restored', 'captured', 'unloaded'):
        values = [d for d in records if d['label'] == label]
        assert len(values) == 4 and {d['family'] for d in values} == set(families)
        phases[label] = {d['family']: d for d in values}
    baseline = phases['before_chunks']
    assert len({a['body'] for d in baseline.values() for c in d['cars'] for a in c['carriers']}) == 14
    assert len({r['id'] for d in baseline.values() for c in d['cars'] for a in c['carriers'] for r in a['riders']}) == 5
    for label, values in phases.items():
        occupied = label != 'unloaded'
        held = label in ('before_chunks', 'parked_restored', 'captured', 'unloaded')
        for family, d in values.items():
            count, initial_fe = families[family]
            assert d['required'] and len(d['cars']) == count and d['energy'] >= 0
            assert d['span'] == baseline[family]['span']
            assert {c['chassis']: set(a['body'] for a in c['carriers']) for c in d['cars']} == {
                c['chassis']: set(a['body'] for a in c['carriers']) for c in baseline[family]['cars']}
            if occupied:
                assert assignments(d) == assignments(baseline[family])
            if label in ('before_chunks', 'parked_restored'):
                assert d['energy'] == initial_fe
                assert d['anchor'] == baseline[family]['anchor']
                assert controller(d['motor']) == controller(baseline[family]['motor'])
            for c in d['cars']:
                assert not c['fault'] and c['consist'] == count
                assert c['restraints'] == occupied and c['ready'] == held
                assert math.isfinite(c['distance']) and math.isfinite(c['speed'])
                assert all(a['loaded'] and a['capture'] == held for a in c['carriers'])
                assert sum(len(a['riders']) for a in c['carriers']) == int(occupied)
                assert all(r['alive'] and r['mounted'] and r['carrier'] == a['body']
                           for a in c['carriers'] for r in a['riders'])
                if not held:
                    assert c['speed'] > .05
                    previous = {v['chassis']: v for v in phases[
                        'parked_restored' if label == 'moving_before' else 'moving_before'][family]['cars']}
                    assert c['distance'] > previous[c['chassis']]['distance'] + 1
            if label == 'moving_restored':
                assert d['energy'] < phases['moving_before'][family]['energy'] < initial_fe
    for cycle in (1, 2):
        released = [d for d in probes if d['cycle'] == cycle and d['action'] == 'release_chunks']
        assert len(released) == 4
        for d in released:
            assert all(b['resident'] and not b['oldRemoved'] and not b['fresh'] for b in d['chunks'])
            assert all(b['loaded'] and not b['oldRemoved'] and not b['fresh'] for c in d['cars'] for b in [c] + c['carriers'])
        for loaded in (False, True):
            checked = [d for d in probes if d['cycle'] == cycle and d['action'] == 'check_chunks'
                       and all(c['resident'] == loaded for c in d['chunks'])]
            assert {d['family'] for d in checked} == set(families)
            for d in checked:
                start = next(v for v in released if v['family'] == d['family'])
                assert d['tick'] - start['tick'] == (600 if loaded else 500)
                assert all(b['ticking'] == loaded and b['oldRemoved'] and b['fresh'] == loaded for b in d['chunks'])
                assert all(b['loaded'] == loaded and b['oldRemoved'] and b['fresh'] == loaded
                           for c in d['cars'] for b in [c] + c['carriers'])
                assert all(c['restraints'] and c['occupants'] == 1 and not c['fault'] and c['stopping'] == (cycle == 1)
                           and all(a['capture'] == (cycle == 1) for a in c['carriers']) for c in d['cars'])
    assert mechanics(case) == mechanics('baseline')
    commands = (root / (case + '-commands.txt')).read_text()
    assert commands.count('Pig has the following entity data:') == 5
    assert 'unexpected error' not in commands and 'Incorrect argument' not in commands
    mods = (root / (case + '-mods.txt')).read_text()
    assert 'Engineered 1.1.1' in mods
    for mod in ('Track Styles 1.2.0', 'Additions 0.2.0', 'Magnetized 1.1.0', 'Immersion 0.2.3'):
        assert (mod in mods) == (case == 'combined')
    summary[case] = {f: {'parked_fe': phases['parked_restored'][f]['energy'],
                         'moving_before_fe': phases['moving_before'][f]['energy'],
                         'moving_restored_fe': phases['moving_restored'][f]['energy']} for f in families}

for profile, skips in {'base': 5, 'engineered': 3, 'combined': 0}.items():
    name = 'native-' + profile
    assert runs[name]['exit_status'] == 0
    log = (root / (name + '-checks.txt')).read_text()
    assert 'All 28 required tests passed' in log
    assert 'BUILD SUCCESSFUL' in log and 'required tests failed' not in log
    assert log.count('_SKIPPED:') == skips
    assert 'FD_SAVED_PASSENGER nativeReplay=true unknownRejected=true liveUnchanged=true' in log
    assert sum(l.startswith('FD_TRACK_PARTIAL_RELOADED family=') for l in log.splitlines()) == 10
    if profile != 'base':
        assert 'FD_ENGINEERED_ENERGY_COMPLETE families=4 cars=5 riders=5' in log
        assert 'FD_ENGINEERED_ENERGY_TUNING_RESTORED buffer=100000 regen=0.3' in log
tests = failures = errors = 0
for path in (root / 'unit').glob('TEST-*.xml'):
    data = ET.parse(path).getroot().attrib
    assert int(data['skipped']) == 0
    tests += int(data['tests']); failures += int(data['failures']); errors += int(data['errors'])
assert (tests, failures, errors) == (19, 0, 0)
print(json.dumps({'scope': 'Five native pigs; parked and moving chunk eviction/reload; four straight families',
                  'profiles': summary, 'native_profiles': 3, 'unit_tests': tests}, indent=2))
