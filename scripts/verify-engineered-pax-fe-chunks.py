#!/usr/bin/env python3
"""Verify occupied native PAX phase recovery with persistent Engineered energy."""
import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
runs = json.loads((root / 'runs.json').read_text())
energies = {'LIFTING': 12346, 'OUTBOUND': 12347, 'RETURNING': 12348, 'CAPTURING': 12349}
commands = {'LIFTING': ('LAUNCH', 'BACKWARD', '100'),
            'FAULT': ('LAUNCH', 'FORWARD', '100'),
            'OUTBOUND': ('STOP', 'FORWARD', '0'),
            'RETURNING': ('TRIM', 'ROLLING', '1'),
            'CAPTURING': ('LAUNCH', 'BACKWARD', '100'),
            'BOARDING': ('LAUNCH', 'FORWARD', '100')}
summary = {}
for profile, skips, fe in [('base', 6, False), ('engineered_default', 3, False),
                          ('engineered_fe', 3, True), ('combined_fe', 0, True)]:
    text = (root / (profile + '-checks.txt')).read_text()
    mods = (root / (profile + '-mods.txt')).read_text()
    assert runs[profile]['exit_status'] == 0
    assert 'All 33 required tests passed' in text and 'BUILD SUCCESSFUL' in text
    assert 'failed at' not in text and text.count('_SKIPPED:') == skips
    assert ('Engineered 1.1.1' in mods) == (profile != 'base')
    for v in ('Track Styles 1.2.0', 'Additions 0.2.0', 'Magnetized 1.1.0',
              'Immersion 0.2.3', 'Aeronautics 1.3.2', 'Interiors 0.6.1'):
        assert (v in mods) == (profile == 'combined_fe')
    for v in ('Minecraft 1.21.1', 'NeoForge 21.1.248', 'Create 6.0.10',
              'Coasters Simulated 0.1.5', 'Sable 2.0.3'):
        assert v in mods
    events = {}
    for event in ('ENERGY', 'DEPENDENCIES', 'RELEASE', 'EVICTED', 'STAGGERED', 'RESTORED', 'RECOVERY', 'COMPLETE'):
        rows = re.findall(r'^FD_PAX_PHASE_' + event + r' target=([A-Z]+) (.+)$', text, re.M)
        assert len(rows) == 4 and {p for p, _ in rows} == set(energies), (profile, event)
        events[event] = {p: dict(re.findall(r'(\w+)=([^ ]+)', row)) for p, row in rows}
    results = {}
    for phase, expected_energy in energies.items():
        assert events['ENERGY'][phase]['required'] == str(fe).lower()
        assert events['DEPENDENCIES'][phase] == {'seeds': '12', 'requiredBodies': '12', 'complete': 'true'}
        release = events['RELEASE'][phase]
        evicted = events['EVICTED'][phase]
        staggered = events['STAGGERED'][phase]
        restored = events['RESTORED'][phase]
        recovery = events['RECOVERY'][phase]
        complete = events['COMPLETE'][phase]
        assert release['bodies'] == evicted['bodies'] == '13'
        assert release['riders'] == evicted['occupants'] == restored['riders'] == '16'
        assert float(release['speed']) > .05
        elapsed = int(evicted['tick']) - int(release['tick'])
        assert 250 <= elapsed < 8000
        assert int(evicted['blocks']) == (6 if profile == 'base' else 8)
        assert evicted['phase'] == restored['phase'] == 'FAULT'
        assert evicted['interrupted'] == restored['interrupted'] == phase
        assert restored['fresh'] == 'true'
        assert staggered['winchTicking'] == 'false' and staggered['carriage'] == 'true'
        assert staggered['ticks'] == '100' and 0 <= float(staggered['travel']) < 1.5
        assert 0 <= float(recovery['carriageDrift']) < .3
        assert 0 <= float(recovery['initialCarriageTravel']) < 1.5
        assert 0 <= float(recovery['maxCarriageStep']) < 1.5
        assert 0 <= float(recovery['maxReloadStep']) < 1.5
        gravity = phase in ('OUTBOUND', 'RETURNING')
        assert recovery['action'] == ('recover' if gravity else 'resume')
        if gravity:
            assert 0 <= int(recovery['waitTicks']) < 5900
        else:
            assert int(recovery['waitTicks']) == 60 and 0 <= float(recovery['drift']) < .3
        assert all(complete[k] == v for k, v in {'cycles': '1', 'cars': '4', 'riders': '16',
                   'alive': 'true', 'unloaded': 'true', 'telemetry': 'true'}.items())
        row = {'eviction_ticks': elapsed, 'action': recovery['action'],
               'wait_after_initial_100_ticks': int(recovery['waitTicks']),
               'lead_travel_during_wait_blocks': float(recovery['drift']),
               'largest_initial_reload_step_blocks': float(recovery['maxReloadStep'])}
        if profile != 'base':
            prefix = ('FD_ENGINEERED_PAX_FE_PHASE_' if fe else 'FD_ENGINEERED_PAX_PHASE_') + phase
            if fe:
                assert f'{prefix}_SEEDED stored={expected_energy} capacity=100000' in text
                assert text.count(prefix + '_RELOADED ') == 1
                assert f'{prefix}_RELOADED stored={expected_energy} freshMotor=true freshObserver=true freshEnergy=true controller=true span=true observer=true' in text
                assert text.index(prefix + '_RELOADED ') < text.index(prefix + '_COMMAND phase=FAULT')
                row['restored_energy_fe'] = expected_energy
            seen = re.search(prefix + r'_COMPLETE cycles=1 observer=true sightings=\{([^}]+)\}', text)
            assert seen
            counts = {k: int(v) for k, v in re.findall(r'([A-Z]+)=(\d+)', seen[1])}
            required = set(commands) - ({'RETURNING'} if phase == 'OUTBOUND' else set())
            assert required <= set(counts) and all(counts[k] > 2 for k in required)
            for command_phase in required:
                mode, heading, value = commands[command_phase]
                assert f'{prefix}_COMMAND phase={command_phase} mode={mode} heading={heading} value={value}' in text
            row['controller_presence_ticks'] = counts
        results[phase] = row
    if profile != 'base':
        assert text.count('FD_ENGINEERED_PAX_FE_RESTORED requireEnergy=false') == (5 if fe else 1)
        assert 'FD_ENGINEERED_PAX_FE_COMPLETE cycles=1 observer=true' in text
        assert 'FD_PAX_POWER_COMPLETE energyRequired=true cars=4 riders=16 alive=true unloaded=true mounts=true telemetry=true cycles=1' in text
        assert 'FD_ENGINEERED_ENERGY_COMPLETE families=4 cars=5 riders=5' in text
    summary[profile] = results

tests = 0
for path in (root / 'unit').glob('TEST-*.xml'):
    data = ET.parse(path).getroot().attrib
    assert all(int(data[k]) == 0 for k in ('failures', 'errors', 'skipped'))
    tests += int(data['tests'])
assert tests == 19
print(json.dumps({'scope': 'Four occupied PAX native chunk interruption phases with exact FE and selected equipment configuration restoration',
                  'profiles': summary, 'registered_tests': 33, 'unit_tests': tests}, indent=2, sort_keys=True))
