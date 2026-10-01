#!/usr/bin/env python3
"""Verify occupied winch ownership with Engineered's real FE requirement enabled."""
import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
runs = json.loads((root / 'runs.json').read_text())
phases = {'BOARDING', 'LIFTING', 'OUTBOUND', 'RETURNING', 'CAPTURING', 'FAULT'}
commands = {'LIFTING': ('LAUNCH', 'BACKWARD', '100'),
            'FAULT': ('LAUNCH', 'FORWARD', '100'),
            'OUTBOUND': ('STOP', 'FORWARD', '0'),
            'RETURNING': ('TRIM', 'ROLLING', '1'),
            'CAPTURING': ('LAUNCH', 'BACKWARD', '100'),
            'BOARDING': ('LAUNCH', 'FORWARD', '100')}
summary = {}
for profile, skips in {'base': 6, 'engineered': 3, 'combined': 0}.items():
    text = (root / (profile + '-checks.txt')).read_text()
    mods = (root / (profile + '-mods.txt')).read_text()
    assert runs[profile]['exit_status'] == 0
    assert 'All 33 required tests passed' in text and 'BUILD SUCCESSFUL' in text
    assert 'failed at' not in text and text.count('_SKIPPED:') == skips
    for version in ('Minecraft 1.21.1', 'NeoForge 21.1.248', 'Create 6.0.10',
                    'Coasters Simulated 0.1.5', 'Sable 2.0.3', 'Fourth Dimension 0.9.0-dev.1'):
        assert version in mods
    assert ('Engineered 1.1.1' in mods) == (profile != 'base')
    for version in ('Track Styles 1.2.0', 'Additions 0.2.0', 'Magnetized 1.1.0',
                    'Immersion 0.2.3', 'Aeronautics 1.3.2', 'Interiors 0.6.1'):
        assert (version in mods) == (profile == 'combined')
    # The original power-loss and all four native eviction cases remain covered.
    assert 'FD_PAX_POWER_COMPLETE energyRequired=false cars=4 riders=16 alive=true unloaded=true mounts=true telemetry=true cycles=1' in text
    for phase in phases - {'BOARDING', 'FAULT'}:
        assert f'FD_PAX_PHASE_COMPLETE target={phase} cycles=1 cars=4 riders=16 alive=true unloaded=true telemetry=true' in text
        assert f'FD_PAX_PHASE_DEPENDENCIES target={phase} seeds=12 requiredBodies=12 complete=true' in text
    if profile == 'base':
        assert 'FD_ENGINEERED_PAX_FE_SKIPPED:' in text
        assert 'FD_ENGINEERED_PAX_FE_COMPLETE' not in text
        summary[profile] = {'energy_case': 'explicit absent-addon skip'}
        continue
    assert 'FD_ENGINEERED_PAX_PROBE segments=3 energyRequired=true cars=4' in text
    assert 'FD_PAX_POWER_COMPLETE energyRequired=true cars=4 riders=16 alive=true unloaded=true mounts=true telemetry=true cycles=1' in text
    assert 'FD_ENGINEERED_PAX_FE_RESTORED requireEnergy=false' in text
    hold = re.search(r'FD_PAX_POWER_HOLD energyRequired=true ticks=40 drift=([\d.Ee+-]+) phase=FAULT', text)
    assert hold and 0 <= float(hold[1]) < .3
    seen = re.search(r'FD_ENGINEERED_PAX_FE_COMPLETE cycles=1 observer=true sightings=\{([^}]+)\}', text)
    assert seen
    counts = {k: int(v) for k, v in re.findall(r'([A-Z]+)=(\d+)', seen[1])}
    assert set(counts) == phases and all(v > 2 for v in counts.values())
    rows = re.findall(r'^FD_ENGINEERED_PAX_FE_BUFFER phase=([A-Z]+) phaseTick=(\d+) stored=(\d+) capacity=(\d+)$', text, re.M)
    stages = {phase: set() for phase in phases}
    for phase, age, stored, capacity in rows:
        age, stored, capacity = int(age), int(stored), int(capacity)
        assert capacity == 100000 and age in (0, 5, 10, 15)
        assert stored == {0: 0, 5: capacity, 10: 0, 15: 12345}[age]
        stages[phase].add(age)
    assert all(ages == {0, 5, 10, 15} for ages in stages.values())
    for phase, (mode, heading, value) in commands.items():
        assert f'FD_ENGINEERED_PAX_FE_COMMAND phase={phase} mode={mode} heading={heading} value={value}' in text
    assert 'FD_ENGINEERED_ENERGY_COMPLETE families=4 cars=5 riders=5' in text
    summary[profile] = {'riders': 16, 'cars': 4, 'occupied_cycles': 1,
                        'hold_ticks': 40, 'hold_3d_drift_blocks': float(hold[1]),
                        'buffer_capacity_fe': 100000, 'partial_refill_fe': 12345,
                        'buffer_transition_records': len(rows),
                        'controller_presence_ticks': counts}

assert runs['early-snapshot']['exit_status'] == 1
assert 'Shuttle telemetry lost consist or phase' in (root / 'early-snapshot-checks.txt').read_text()
tests = 0
for path in (root / 'unit').glob('TEST-*.xml'):
    suite = ET.parse(path).getroot().attrib
    assert all(int(suite[k]) == 0 for k in ('failures', 'errors', 'skipped'))
    tests += int(suite['tests'])
assert tests == 19
print(json.dumps({'scope': 'Occupied compact PAX with FE-powered opposing Engineered commands and winch power interruption',
                  'profiles': summary, 'registered_tests': 33, 'unit_tests': tests}, indent=2, sort_keys=True))
