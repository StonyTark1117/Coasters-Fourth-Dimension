#!/usr/bin/env python3
"""Verify native limited charging, refill recovery and regeneration-boundary evidence."""
import json
import math
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
runs = json.loads((root / 'runs.json').read_text())
families = {'ARROW': 2, 'ZACSPIN': 1, 'FREE_SPIN': 1, 'ZACSPIN_REVISED': 1}
phases = ('PULSE', 'COAST', 'TRICKLE', 'REFILLED', 'REGEN_DISABLED',
          'REGEN_SETUP_REVERSE', 'REGEN_SATURATED')
report = {'scope': 'Five occupied cars on four straight tracks, native FE capability input', 'profiles': {}}
for profile, skips in (('engineered', 3), ('combined', 0), ('base', 5)):
    text = (root / (profile + '.txt')).read_text()
    mods = (root / (profile + '-mods.txt')).read_text()
    lines = text.splitlines()
    assert runs[profile]['exit_status'] == 0 and 'All 28 required tests passed' in text
    assert 'BUILD SUCCESSFUL' in text and 'required tests failed' not in text
    assert text.count('_SKIPPED:') == skips
    result = {'registered_tests': 28, 'optional_skips': skips}
    assert sum(l.startswith('FD_TRACK_PARTIAL_RELOADED family=') for l in lines) == 10
    if profile == 'base':
        assert 'coastersengineered' not in mods
        assert 'FD_ENGINEERED_ENERGY_SKIPPED:' in text
        assert 'FD_ENGINEERED_ENERGY_PULSE' not in text
        report['profiles'][profile] = result
        continue
    assert 'Engineered 1.1.1' in mods
    if profile == 'combined':
        assert all(v in mods for v in ('Track Styles 1.2.0', 'Additions 0.2.0',
                                      'Magnetized 1.1.0', 'Immersion 0.2.3'))
    result['families'] = {}
    for family, cars in families.items():
        assert lines.count(f'FD_ENGINEERED_ENERGY_INPUT family={family} pulse=100 trickle=200 ticks=40') == 1
        observed = {}
        indices = []
        for phase in phases:
            rows = re.findall(r'^FD_ENGINEERED_ENERGY_' + phase + ' family=' + family
                              + r' stored=(\d+) speeds=\[([^\]]+)\]$', text, re.M)
            assert len(rows) == 1, (profile, family, phase)
            stored, velocities = rows[0]
            stored = int(stored)
            velocities = [float(v) for v in velocities.split(',')]
            assert len(velocities) == cars and all(map(math.isfinite, velocities))
            if phase in ('PULSE', 'COAST', 'TRICKLE', 'REGEN_DISABLED'):
                assert stored == 0
            elif phase == 'REGEN_SATURATED':
                assert stored == 1000
            else:
                assert 0 < stored < 100000
            if phase == 'PULSE':
                assert all(.02 < v < 2 for v in velocities)
            elif phase == 'COAST':
                assert all(abs(v) <= old + .25 for v, old in zip(velocities, observed['PULSE']['speeds']))
            elif phase == 'TRICKLE':
                assert all(old + .1 < v < 3 for v, old in zip(velocities, observed['COAST']['speeds']))
            elif phase == 'REFILLED':
                assert all(v > old + 1 for v, old in zip(velocities, observed['TRICKLE']['speeds']))
            elif phase == 'REGEN_SETUP_REVERSE':
                assert all(v < -3 for v in velocities)
            else:
                assert all(abs(v) < 1.3 for v in velocities)
            observed[phase] = {'stored_fe': stored, 'speeds': velocities}
            indices.append(next(i for i, line in enumerate(lines)
                                if line.startswith(f'FD_ENGINEERED_ENERGY_{phase} family={family} ')))
        assert indices == sorted(indices)
        result['families'][family] = observed
    assert lines.count('FD_ENGINEERED_ENERGY_COMPLETE families=4 cars=5 riders=5') == 1
    tuning = 'FD_ENGINEERED_ENERGY_TUNING_RESTORED buffer=100000 regen=0.3'
    restored = 'FD_ENGINEERED_ENERGY_RESTORED requireEnergy=false'
    assert lines.count(tuning) == 1 and lines.count(restored) == 1
    assert lines.index(tuning) < lines.index(restored)
    assert 'FD_ENGINEERED_PAX_PROBE segments=3 energyRequired=false cars=4' in text
    report['profiles'][profile] = result
for i in (1, 2, 3):
    text = (root / f'attempt{i}.txt').read_text()
    assert runs[f'attempt{i}']['exit_status'] == 1 and '1 required tests failed' in text
    assert 'FD_ENGINEERED_ENERGY_TUNING_RESTORED buffer=100000 regen=0.3' in text
    assert 'FD_ENGINEERED_ENERGY_RESTORED requireEnergy=false' in text
    assert ('Limited-power motor lost presence or starvation state' in text if i == 1
            else 'Recovery setup did not relaunch' in text)
assert 'End of compatible track; capture and inspect the route' in (root / 'attempt3.txt').read_text()
unit_tests = 0
for path in (root / 'junit').glob('TEST-*.xml'):
    suite = ET.parse(path).getroot()
    assert all(int(suite.attrib[k]) == 0 for k in ('failures', 'errors', 'skipped'))
    unit_tests += int(suite.attrib['tests'])
assert unit_tests == 19
energy = json.loads((root / 'config-after.json').read_text())['energy']
assert energy['requireEnergy'] is False and energy['energyBuffer'] == 100000 and energy['regenEfficiency'] == .3
report['unit_tests_passed'] = unit_tests
report['rejected_fixture_attempts_preserved'] = 3
print(json.dumps(report, indent=2))
