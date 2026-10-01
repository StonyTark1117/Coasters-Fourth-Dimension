#!/usr/bin/env python3
"""Audit the bounded occupied lifting cold restart in two packaged addon profiles."""
import json
import math
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
runs = json.loads((root / 'runs.json').read_text())


def records(case):
    return {d['label']: d for line in (root / (case + '-checks.txt')).read_text().splitlines()
            if 'FD_PAX_ENERGY_RESTART {' in line
            for d in [json.loads(line.split('FD_PAX_ENERGY_RESTART ', 1)[1])]}


def mounts(d):
    return {c['chassis']: {b['body']: sorted(r['id'] for r in b['riders'])
                          for b in c['carriers']} for c in d['cars']}


def occupied(d):
    assert d['required'] and d['energy'] == 12345 and not d['externalDriveAllowed']
    assert len(d['cars']) == 4
    assert all(c['consist'] == 4 and c['restraints'] and not c['fault'] for c in d['cars'])
    bodies = [b for c in d['cars'] for b in c['carriers']]
    assert len(bodies) == len({b['body'] for b in bodies}) == 8
    assert all(b['loaded'] and len(b['riders']) == 2 for b in bodies)
    assert len({r['id'] for b in bodies for r in b['riders']}) == 16
    assert all(r['alive'] and r['mounted'] and r['carrier'] == b['body']
               for b in bodies for r in b['riders'])
    t = d['telemetry']
    assert t['family'] == 'pax_shuttle'
    assert t['available'] and t['cars'] == 4 and t['capacity'] == 16 and t['occupied'] == 16
    assert t['restraintsClosed'] and t['phase'] == d['phase']
    if d['phase'] != 'BOARDING':
        assert not t['dispatchReady']


def travel(a, b):
    old = {c['chassis']: c['position'] for c in a['cars']}
    return max(math.dist(old[c['chassis']], c['position']) for c in b['cars'])


def mechanical(name):
    data = json.loads((root / (name + '-save.json')).read_text())['data']
    assert data['schema'] == 5 and len(data['rails']) == 3 and len(data['vehicles']) == 4
    vehicles = {}
    for v in data['vehicles']:
        # Explicit mechanical identity fields; route segment and live operating state change in a cycle.
        row = {k: v[k] for k in ('chassis', 'family', 'variant', 'damping', 'ballast', 'maintenance')}
        row['carriers'] = [{k: c[k] for k in ('body', 'side', 'pivot_height', 'longitudinal',
                                            'child_pivot_offset', 'seats', 'ballast', 'rider_seats')}
                           for c in v['carriers']]
        vehicles[str(v['chassis'])] = row
    return sorted(data['rails'], key=lambda r: r['id']), vehicles


before = records('before')['before_live']
occupied(before)
assert before['phase'] == 'LIFTING' and before['cycles'] == 0 and before['carriage']
assert max(c['speed'] for c in before['cars']) > .05
assert runs['before']['exit_status'] == 0
summary = {}
for case in ('engineered', 'combined'):
    assert runs[case]['exit_status'] == runs[case]['operation_exit_status'] == 0
    assert runs[case]['server_pid'] != runs['before']['server_pid']
    mods = (root / (case + '-mods.txt')).read_text()
    for name in ('Minecraft 1.21.1', 'NeoForge 21.1.248', 'Create 6.0.10',
                 'Coasters Simulated 0.1.5', 'Sable 2.0.3', 'Engineered 1.1.1'):
        assert name in mods
    for name in ('Track Styles 1.2.0', 'Additions 0.2.0', 'Magnetized 1.1.0',
                 'Immersion 0.2.3', 'Aeronautics 1.3.2', 'Interiors 0.6.1'):
        assert (name in mods) == (case == 'combined')
    assert 'loaded from union:/tmp/fd-pax-cold1.jar' in (root / (case + '-checks.txt')).read_text()
    rows = records(case)
    restored, held, complete, unloaded = [rows[k] for k in ('restored', 'held', 'complete', 'unloaded')]
    for d in (restored, held):
        assert d['phase'] == 'FAULT' and d['interrupted'] == 'LIFTING' and d['cycles'] == 0
        assert d['carriage'] == before['carriage'] and 'reloaded' in d['fault']
    for field in ('controller', 'observer', 'span'):
        assert restored[field] == before[field], (case, field)
    assert 0 <= travel(before, restored) < 1.5 and 0 <= travel(restored, held) < .3
    assert held['tick'] - restored['tick'] >= 60
    cycles = [d for label, d in rows.items() if label.startswith('cycle_')]
    phases = {d['phase'] for d in cycles}
    expected = {'LIFTING', 'OUTBOUND', 'RETURNING', 'CAPTURING', 'BOARDING'}
    assert phases == expected and any(d['observerPower'] > 0 for d in cycles)
    assert expected - {'BOARDING'} <= {d['phase'] for d in cycles if d['motorDriving']}
    for d in [restored, held, *cycles, complete]:
        occupied(d)
        assert mounts(d) == mounts(before) and d['carriage'] == before['carriage']
    assert complete['phase'] == 'BOARDING' and complete['cycles'] == 1
    assert all(c['ready'] for c in complete['cars'])
    assert unloaded['required'] and unloaded['energy'] == 12345
    assert unloaded['phase'] == 'BOARDING' and unloaded['cycles'] == 1
    assert unloaded['telemetry']['occupied'] == 0
    assert all(c['ready'] and not c['restraints'] and not c['fault'] for c in unloaded['cars'])
    assert all(not b['riders'] for c in unloaded['cars'] for b in c['carriers'])
    commands = (root / (case + '-commands.txt')).read_text()
    assert 'dispatch: Bind and capture the shuttle before starting' in commands
    assert 'unload: Shuttle restraints remain closed until final station capture' in commands
    assert commands.index('report held:') < commands.index('resume: PAX energy restart fixture: resume')
    for action in ('forward', 'reverse', 'stop_motor', 'trim', 'resume'):
        assert f'{action}: PAX energy restart fixture: {action}' in commands
    assert commands.count('Pig has the following entity data:') == 16
    assert 'unexpected error' not in commands
    assert mechanical(case) == mechanical('baseline')
    summary[case] = {'restored_fe': restored['energy'], 'riders': 16,
                     'initial_reload_motion_blocks': travel(before, restored),
                     'powered_fault_hold_drift_blocks': travel(restored, held),
                     'cycle_samples': len(cycles), 'phases': sorted(phases)}

for profile, skips in [('base', 6), ('engineered', 3), ('combined', 0)]:
    name = 'native-' + profile
    text = (root / (name + '-checks.txt')).read_text()
    assert runs[name]['exit_status'] == 0 and 'All 33 required tests passed' in text
    assert 'BUILD SUCCESSFUL' in text and text.count('_SKIPPED:') == skips
    assert 'required tests failed' not in text
    assert 'FD_SAVED_PASSENGER nativeReplay=true unknownRejected=true liveUnchanged=true' in text
    assert text.count('FD_PAX_PHASE_COMPLETE target=') == 4
    if profile != 'base':
        assert 'FD_ENGINEERED_PAX_FE_COMPLETE cycles=1 observer=true' in text

unit = 0
for p in (root / 'unit').glob('TEST-*.xml'):
    a = ET.parse(p).getroot().attrib
    assert all(int(a[k]) == 0 for k in ('failures', 'errors', 'skipped'))
    unit += int(a['tests'])
assert unit == 19
settings = json.loads((root / 'settings-restored.json').read_text())
assert len(settings) == 2 and all(d['original_sha256'] == d['restored_sha256'] for d in settings.values())
print(json.dumps({'scope': 'Occupied compact PAX lifting cold restart with direct native FE input',
                  'dedicated': summary, 'native_profiles': 3, 'registered_tests': 33,
                  'unit_tests': unit}, indent=2, sort_keys=True))
