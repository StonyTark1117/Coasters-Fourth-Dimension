#!/usr/bin/env python3
"""Verify bounded native FE evidence, including optional isolation and failed setups."""
import json
import math
import re
import sys
import tomllib
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
runs = json.loads((root / 'runs.json').read_text())
families = {'ARROW': 2, 'ZACSPIN': 1, 'FREE_SPIN': 1, 'ZACSPIN_REVISED': 1}
report = {'scope': 'Four straight tracks; linked Arrow pair and three single cars; five native passengers',
          'profiles': {}}
for profile, skips in (('engineered', 3), ('combined', 0), ('base', 5)):
    text = (root / (profile + '.txt')).read_text()
    mods = (root / (profile + '-mods.txt')).read_text()
    assert runs[profile]['exit_status'] == 0
    assert 'All 26 required tests passed' in text and 'required tests failed' not in text
    assert 'BUILD SUCCESSFUL' in text and text.count('_SKIPPED:') == skips
    for version in ('Minecraft 1.21.1', 'NeoForge 21.1.248', 'Create 6.0.10',
                    'Sable 2.0.3', 'Coasters Simulated 0.1.5'):
        assert version in mods
    if profile == 'base':
        assert 'FD_ENGINEERED_ENERGY_SKIPPED:' in text
        assert 'FD_ENGINEERED_ENERGY_COMPLETE' not in text and 'coastersengineered' not in mods
        report['profiles'][profile] = {'registered_tests': 26, 'optional_skips': skips}
        continue
    assert 'Engineered 1.1.1' in mods
    if profile == 'combined':
        for version in ('Track Styles 1.2.0', 'Additions 0.2.0', 'Magnetized 1.1.0', 'Immersion 0.2.3'):
            assert version in mods
    held = re.findall(r'FD_ENGINEERED_ENERGY_HELD family=(\w+) cars=(\d+) stored=(\d+)', text)
    assert len(held) == 4 and {f: int(c) for f, c, _ in held} == families
    assert all(int(stored) == 100000 for _, _, stored in held)
    starved = re.findall(r'FD_ENGINEERED_ENERGY_STARVED family=(\w+) cars=(\d+)', text)
    assert len(starved) == 4 and {f: int(c) for f, c in starved} == families
    phases = {}
    for phase in ('FORWARD', 'BRAKE', 'REVERSE', 'REVERSE_BRAKE'):
        rows = re.findall(r'FD_ENGINEERED_ENERGY_' + phase
                          + r' family=(\w+) stored=(\d+) speeds=\[([^\]]+)\]', text)
        assert len(rows) == 4 and {r[0] for r in rows} == set(families)
        phases[phase] = {}
        for family, stored, speeds in rows:
            stored = int(stored)
            speeds = [float(v) for v in speeds.split(',')]
            assert len(speeds) == families[family] and all(math.isfinite(v) for v in speeds)
            assert 0 < stored < 100000
            if phase == 'FORWARD':
                assert all(v > 1 for v in speeds)
            elif phase == 'REVERSE':
                assert all(v < -1 for v in speeds)
            else:
                assert all(abs(v) < 1.3 for v in speeds)
            phases[phase][family] = {'stored_fe': stored, 'speeds': speeds}
    assert text.count('FD_ENGINEERED_ENERGY_COMPLETE families=4 cars=5 riders=5') == 1
    assert text.count('FD_ENGINEERED_ENERGY_RESTORED requireEnergy=false') == 1
    assert text.index('FD_ENGINEERED_ENERGY_RESTORED') < text.index('FD_ENGINEERED_PAX_PROBE')
    assert 'FD_ENGINEERED_PAX_PROBE segments=3 energyRequired=false cars=4' in text
    report['profiles'][profile] = {'registered_tests': 26, 'optional_skips': skips, 'phases': phases}

for attempt, message in (('attempt1', 'within 1024 blocks of origin'),
                         ('attempt2', 'Track placement is out of reach')):
    text = (root / (attempt + '.txt')).read_text()
    assert runs[attempt]['exit_status'] == 1 and message in text
    assert '1 required tests failed' in text and 'FD_ENGINEERED_ENERGY_COMPLETE' not in text
    assert 'FD_ENGINEERED_ENERGY_RESTORED requireEnergy=false' in text

config = tomllib.loads((root / 'coastersengineered-server.toml').read_text())['energy']
assert not config['requireEnergy'] and config['energyPerJoule'] == 10 and config['regenEfficiency'] == .3
tests = 0
for path in (root / 'junit').glob('TEST-*.xml'):
    suite = ET.parse(path).getroot()
    assert all(int(suite.attrib[key]) == 0 for key in ('failures', 'errors', 'skipped'))
    tests += int(suite.attrib['tests'])
assert tests == 19
report['unit_tests_passed'] = tests
report['energy_configuration_restored'] = True
report['setup_failures_preserved'] = 2
print(json.dumps(report, indent=2))
