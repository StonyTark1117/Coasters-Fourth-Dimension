#!/usr/bin/env python3
"""Verify four PAX interruption phases, native eviction and explicit recovery."""
import json
import math
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
runs = json.loads((root / 'runs.json').read_text())
phases = {'LIFTING', 'OUTBOUND', 'RETURNING', 'CAPTURING'}
profiles = {'base': 5, 'engineered': 3, 'styles': 5, 'additions': 3,
            'magnetized': 4, 'immersion': 5, 'combined': 0}
summary = {}
for profile, skips in profiles.items():
    text = (root / (profile + '-checks.txt')).read_text()
    mods = (root / (profile + '-mods.txt')).read_text()
    for name, version in {'engineered': 'Engineered 1.1.1', 'styles': 'Track Styles 1.2.0',
                          'additions': 'Additions 0.2.0', 'magnetized': 'Magnetized 1.1.0',
                          'immersion': 'Immersion 0.2.3'}.items():
        assert (version in mods) == (profile in (name, 'combined'))
    assert all(version in mods for version in (
        'Minecraft 1.21.1', 'NeoForge 21.1.248', 'Sable 2.0.3',
        'Create 6.0.10', 'Coasters Simulated 0.1.5', 'Flywheel 1.0.6',
        'Ponder 1.0.82+mc1.21.1', 'Fourth Dimension 0.9.0-dev.1'))
    assert ('Create Aeronautics 1.3.2 (aeronautics)' in mods) == (profile in ('additions', 'immersion', 'combined'))
    assert ('Create: Interiors 0.6.1 (interiors)' in mods) == (profile in ('additions', 'combined'))
    assert runs[profile]['exit_status'] == 0
    assert 'All 32 required tests passed' in text and 'BUILD SUCCESSFUL' in text
    assert 'required tests failed' not in text and text.count('_SKIPPED:') == skips
    values = {}
    for event in ('DEPENDENCIES', 'RELEASE', 'EVICTED', 'STAGGERED', 'RESTORED', 'RECOVERY', 'COMPLETE'):
        rows = re.findall(r'^FD_PAX_PHASE_' + event + r' target=([A-Z]+) (.+)$', text, re.M)
        assert len(rows) == 4 and {p for p, _ in rows} == phases, (profile, event)
        values[event] = {p: dict(re.findall(r'(\w+)=([^ ]+)', row)) for p, row in rows}
    native_rows = re.findall(r'^FD_PAX_PHASE_NATIVE target=OUTBOUND stage=(partial|full) age=\d+ trainBodies=(\d+)$', text, re.M)
    assert {stage for stage, _ in native_rows} == {'partial', 'full'}
    assert all(int(count) in (0, 12) for _, count in native_rows)
    result = {}
    for phase in phases:
        release, evicted, staggered, restored, recovery, complete = [values[e][phase] for e in
            ('RELEASE', 'EVICTED', 'STAGGERED', 'RESTORED', 'RECOVERY', 'COMPLETE')]
        assert values['DEPENDENCIES'][phase] == {'seeds': '12', 'requiredBodies': '12', 'complete': 'true'}
        assert int(release['bodies']) == 13 and int(release['riders']) == 16
        assert math.isfinite(float(release['speed'])) and float(release['speed']) > .05
        assert int(evicted['bodies']) == 13 and int(evicted['occupants']) == 16
        assert int(evicted['blocks']) == (8 if profile in ('engineered', 'combined') else 6)
        assert evicted['phase'] == restored['phase'] == 'FAULT'
        assert evicted['interrupted'] == restored['interrupted'] == phase
        assert restored['fresh'] == 'true' and restored['riders'] == '16'
        assert staggered['winchTicking'] == 'false' and staggered['carriage'] == 'true'
        assert int(staggered['ticks']) == 100 and 0 <= float(staggered['travel']) < 1.5
        elapsed = int(evicted['tick']) - int(release['tick'])
        assert 250 <= elapsed < 8000
        assert float(recovery['carriageDrift']) < .3 and float(recovery['maxReloadStep']) < 1.5
        assert float(recovery['initialCarriageTravel']) < 1.5 and float(recovery['maxCarriageStep']) < 1.5
        gravity = phase in ('OUTBOUND', 'RETURNING')
        assert recovery['action'] == ('recover' if gravity else 'resume')
        assert recovery['phase'] == ('CAPTURING' if gravity else phase)
        if gravity:
            assert 0 <= int(recovery['waitTicks']) < 5900
        else:
            assert int(recovery['waitTicks']) == 60 and float(recovery['drift']) < .3
        assert all(complete[k] == v for k, v in {
            'cycles': '1', 'cars': '4', 'riders': '16', 'alive': 'true',
            'unloaded': 'true', 'telemetry': 'true'}.items())
        result[phase] = {'eviction_ticks': elapsed, 'recovery_action': recovery['action'],
                         'wait_after_initial_100_ticks': int(recovery['waitTicks']),
                         'lead_travel_during_wait_blocks': float(recovery['drift']),
                         'carriage_drift_blocks': float(recovery['carriageDrift']),
                         'staggered_carriage_travel_blocks': float(staggered['travel']),
                         'initial_carriage_travel_blocks': float(recovery['initialCarriageTravel']),
                         'largest_carriage_reload_step_blocks': float(recovery['maxCarriageStep']),
                         'largest_initial_reload_step_blocks': float(recovery['maxReloadStep'])}
        if profile in ('engineered', 'combined'):
            seen = re.search(r'FD_ENGINEERED_PAX_PHASE_' + phase + r'_COMPLETE cycles=1 observer=true sightings=\{([^}]+)\}', text)
            assert seen
            counts = {k: int(v) for k, v in re.findall(r'([A-Z]+)=(\d+)', seen[1])}
            required = phases | {'BOARDING', 'FAULT'}
            if phase == 'OUTBOUND':
                required = required - {'RETURNING'}
            assert required <= set(counts) and all(counts[k] > 2 for k in required)
            commands = {'LIFTING': ('LAUNCH', 'BACKWARD', '100'),
                        'FAULT': ('LAUNCH', 'FORWARD', '100'),
                        'OUTBOUND': ('STOP', 'FORWARD', '0'),
                        'RETURNING': ('TRIM', 'ROLLING', '1'),
                        'CAPTURING': ('LAUNCH', 'BACKWARD', '100'),
                        'BOARDING': ('LAUNCH', 'FORWARD', '100')}
            for command_phase in required:
                mode, heading, value = commands[command_phase]
                assert (f'FD_ENGINEERED_PAX_PHASE_{phase}_COMMAND phase={command_phase} '
                        f'mode={mode} heading={heading} value={value}') in text
            result[phase]['controller_presence_ticks'] = counts
    assert 'FD_SAVED_PASSENGER nativeReplay=true unknownRejected=true liveUnchanged=true' in text
    assert sum(l.startswith('FD_TRACK_PARTIAL_RELOADED family=') for l in text.splitlines()) == 10
    if profile in ('engineered', 'combined'):
        assert 'FD_ENGINEERED_ENERGY_COMPLETE families=4 cars=5 riders=5' in text
        assert 'FD_ENGINEERED_PAX_COMPLETE cycles=1 observer=true' in text
        assert 'energyRequired=false cars=4' in text
    summary[profile] = result

assert all(runs['attempt' + str(i)]['exit_status'] == 1 for i in range(1, 8))
assert 'Reloaded PAX jumped between ticks: 3.3087557343887783' in (root / 'attempt1-checks.txt').read_text()
assert 'PAX fixture never physically evicted' in (root / 'attempt4-checks.txt').read_text()
assert 'Interrupted empty carriage moved after reload' in (root / 'attempt6-checks.txt').read_text()
assert 'Interrupted gravity train never became ready for manual recovery' in (root / 'attempt7-checks.txt').read_text()
assert runs['load-race']['exit_status'] == 1
assert 'Interrupted carriage left its reload settling envelope: 1.6072505545166884' in (root / 'load-race-checks.txt').read_text()
assert runs['staggered-layout']['exit_status'] == 1
assert 'Staggered reload accidentally made the winch tick' in (root / 'staggered-layout-checks.txt').read_text()
assert runs['staggered-magnetized']['exit_status'] == 1
assert 'Reloaded PAX jumped between ticks: 1.510035661887908' in (root / 'staggered-magnetized-checks.txt').read_text()
assert runs['dependencies-before']['exit_status'] == 1
assert 'Incomplete native train loading group: expected=12 actual=3' in (root / 'dependencies-before-checks.txt').read_text()
tests = 0
for path in (root / 'unit').glob('TEST-*.xml'):
    suite = ET.parse(path).getroot().attrib
    assert all(int(suite[k]) == 0 for k in ('failures', 'errors', 'skipped'))
    tests += int(suite['tests'])
assert tests == 19
print(json.dumps({'scope': 'Four compact PAX occupied interruption phases; native pigs; default Engineered energy mode',
                  'profiles': summary, 'registered_tests': 32, 'unit_tests': tests}, indent=2, sort_keys=True))
