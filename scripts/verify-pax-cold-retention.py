#!/usr/bin/env python3
"""Audit PAX cold-fault retention and occupied outbound recovery evidence."""
import json
import math
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
runs = json.loads((root / 'runs.json').read_text())
FAULT = 'Shuttle reloaded: inspect and explicitly resume or recover'


def records(case):
    result = {}
    for line in (root / (case + '-checks.txt')).read_text().splitlines():
        if 'FD_PAX_ENERGY_RESTART {' not in line:
            continue
        d = json.loads(line.split('FD_PAX_ENERGY_RESTART ', 1)[1])
        assert d['label'] not in result, (case, d['label'])
        result[d['label']] = d
    return result


def assignments(d):
    return {c['chassis']: {b['body']: sorted(r['id'] for r in b['riders'])
                          for b in c['carriers']} for c in d['cars']}


def occupied(d):
    assert d['required'] and d['energy'] == 12345 and not d['externalDriveAllowed']
    assert len(d['cars']) == 4 and len({c['chassis'] for c in d['cars']}) == 4
    assert all(c['consist'] == 4 and c['restraints'] for c in d['cars'])
    bodies = [b for c in d['cars'] for b in c['carriers']]
    assert len(bodies) == len({b['body'] for b in bodies}) == 8
    assert all(b['loaded'] and len(b['riders']) == 2 for b in bodies)
    assert len({r['id'] for b in bodies for r in b['riders']}) == 16
    assert all(r['alive'] and r['mounted'] and r['carrier'] == b['body']
               for b in bodies for r in b['riders'])
    t = d['telemetry']
    assert t['available'] and t['family'] == 'pax_shuttle' and t['cars'] == 4
    assert t['occupied'] == t['capacity'] == 16 and t['restraintsClosed']
    assert t['phase'] == d['phase']
    if d['phase'] != 'BOARDING':
        assert not t['dispatchReady']


def mechanics(name):
    data = json.loads((root / (name + '-save.json')).read_text())['data']
    assert data['schema'] == 5 and len(data['rails']) == 3 and len(data['vehicles']) == 4
    result = {}
    for v in data['vehicles']:
        row = {k: v[k] for k in ('chassis', 'family', 'variant', 'damping', 'ballast', 'maintenance')}
        row['carriers'] = [{k: c[k] for k in ('body', 'side', 'pivot_height', 'longitudinal',
                                            'child_pivot_offset', 'seats', 'ballast', 'rider_seats')}
                           for c in v['carriers']]
        result[str(v['chassis'])] = row
    return sorted(data['rails'], key=lambda r: r['id']), result


before = records('before')['before']
occupied(before)
assert before['phase'] == 'OUTBOUND' and before['cycles'] == 0
assert max(c['speed'] for c in before['cars']) > .2
broken = records('broken')['restored']
occupied(broken)
assert assignments(before) == assignments(broken)
assert broken['phase'] == 'FAULT' and broken['interrupted'] == 'OUTBOUND'
assert broken['fault'] == FAULT and not broken['telemetry']['captured']
assert all(not b['capture'] for c in broken['cars'] for b in c['carriers'])
saved_broken = json.loads((root / 'broken-save.json').read_text())['data']['vehicles']
assert all(not v['stopping'] and all(not c['capture'] for c in v['carriers']) for v in saved_broken)
assert runs['before']['exit_status'] == runs['broken']['exit_status'] == 0
assert runs['before']['operation_exit_status'] == runs['broken']['operation_exit_status'] == 0
assert runs['beforefix-native']['exit_status'] == 1
failure = (root / 'beforefix-native-checks.txt').read_text()
assert 'Cold LIFTING fault did not retain every native car and carrier' in failure
assert '1 required tests failed' in failure
summary = {}
for case in ('engineered', 'combined'):
    assert runs[case]['exit_status'] == runs[case]['operation_exit_status'] == 0
    assert runs[case]['server_pid'] not in (runs['before']['server_pid'], runs['broken']['server_pid'])
    text = (root / (case + '-checks.txt')).read_text()
    assert 'loaded from union:/tmp/fd-pax-cold-retain.jar' in text
    mods = (root / (case + '-mods.txt')).read_text()
    for name in ('Minecraft 1.21.1', 'NeoForge 21.1.248', 'Create 6.0.10',
                 'Coasters Simulated 0.1.5', 'Sable 2.0.3', 'Engineered 1.1.1'):
        assert name in mods
    for name in ('Track Styles 1.2.0', 'Additions 0.2.0', 'Magnetized 1.1.0',
                 'Immersion 0.2.3', 'Aeronautics 1.3.2', 'Interiors 0.6.1'):
        assert (name in mods) == (case == 'combined')
    rows = records(case)
    restored, complete, unloaded = [rows[k] for k in ('restored', 'complete', 'unloaded')]
    for field in ('controller', 'observer', 'span'):
        assert restored[field] == before[field], (case, field)
    waiting = [d for label, d in rows.items() if label.startswith('braking_')]
    recovery = [d for label, d in rows.items() if label.startswith('recovery_')]
    assert waiting and recovery
    for d in [restored, *waiting, *recovery, complete]:
        occupied(d)
        assert assignments(d) == assignments(before)
        assert d['carriageLoaded'] and d['carriage'] == before['carriage']
        assert all(not c['fault'] or (d['phase'] in ('FAULT', 'CAPTURING') and c['fault'] == FAULT)
                   for c in d['cars'])
    for d in [restored, *waiting]:
        assert d['phase'] == 'FAULT' and d['interrupted'] == 'OUTBOUND' and d['fault'] == FAULT
        assert d['cycles'] == 0 and d['telemetry']['captured']
        assert all(c['stopping'] and c['restraints'] and all(b['capture'] for b in c['carriers'])
                   for c in d['cars'])
    max_carriage_drift = max(math.dist(d['carriagePosition'], restored['carriagePosition']) for d in waiting)
    assert max_carriage_drift < .3
    assert all(c['ready'] for c in waiting[-1]['cars'])
    assert waiting[-1]['tick'] - restored['tick'] < 6500
    assert {d['phase'] for d in recovery} == {'CAPTURING', 'BOARDING'}
    assert complete['phase'] == 'BOARDING' and complete['cycles'] == 1
    assert all(c['ready'] and not c['fault'] for c in complete['cars'])
    assert unloaded['energy'] == 12345 and unloaded['telemetry']['occupied'] == 0
    assert unloaded['phase'] == 'BOARDING' and unloaded['cycles'] == 1
    assert all(c['ready'] and not c['restraints'] and not c['fault'] for c in unloaded['cars'])
    assert all(not b['riders'] for c in unloaded['cars'] for b in c['carriers'])
    commands = (root / (case + '-commands.txt')).read_text()
    assert 'resume: Interrupted gravity run requires manual recovery' in commands
    assert 'recover: Stop and capture every carrier before shuttle recovery' in commands
    assert commands.count('recover: PAX energy restart fixture: recover') == 1
    assert 'dispatch: Bind and capture the shuttle before starting' in commands
    assert 'unload: Shuttle restraints remain closed until final station capture' in commands
    assert commands.count('Pig has the following entity data:') == 16
    assert 'unexpected error' not in commands
    assert mechanics(case) == mechanics('before')
    old = {c['chassis']: c['position'] for c in restored['cars']}
    travel = max(math.dist(c['position'], old[c['chassis']]) for c in waiting[-1]['cars'])
    summary[case] = {'restored_fe': 12345, 'riders': 16, 'braking_samples': len(waiting),
                     'ticks_to_manual_recovery_after_initial_100': waiting[-1]['tick'] - restored['tick'],
                     'chassis_travel_while_braking_blocks': travel,
                     'empty_carriage_drift_blocks': max_carriage_drift}

for profile, skips in [('base', 6), ('engineered', 3), ('styles', 6), ('additions', 4),
                       ('magnetized', 5), ('immersion', 6), ('combined', 0)]:
    case = 'native-' + profile
    text = (root / (case + '-checks.txt')).read_text()
    assert runs[case]['exit_status'] == 0 and 'All 33 required tests passed' in text
    assert 'BUILD SUCCESSFUL' in text and 'required tests failed' not in text
    assert text.count('_SKIPPED:') == skips
    mods = (root / (case + '-mods.txt')).read_text()
    for name in ('Minecraft 1.21.1', 'NeoForge 21.1.248', 'Create 6.0.10',
                 'Coasters Simulated 0.1.5', 'Sable 2.0.3'):
        assert name in mods
    for addon, name in [('engineered', 'Engineered 1.1.1'), ('styles', 'Track Styles 1.2.0'),
                        ('additions', 'Additions 0.2.0'), ('magnetized', 'Magnetized 1.1.0'),
                        ('immersion', 'Immersion 0.2.3')]:
        assert (name in mods) == (profile in (addon, 'combined'))
    assert ('Aeronautics 1.3.2' in mods) == (profile in ('additions', 'immersion', 'combined'))
    assert ('Interiors 0.6.1' in mods) == (profile in ('additions', 'combined'))
    for phase in ('LIFTING', 'OUTBOUND', 'RETURNING', 'CAPTURING'):
        assert text.count(f'FD_SHUTTLE_COLD_RETAIN phase={phase} cars=4 carriers=8 braking=true restraints=true capture=true') == 1
        assert f'FD_PAX_PHASE_COMPLETE target={phase}' in text
        required = str(profile in ('engineered', 'combined')).lower()
        assert f'FD_PAX_PHASE_ENERGY target={phase} required={required}' in text
    assert 'FD_SHUTTLE_CHUNK_RELOAD recovered=true' in text
    if profile in ('engineered', 'combined'):
        assert 'FD_ENGINEERED_PAX_FE_COMPLETE cycles=1 observer=true' in text
        assert text.count('freshMotor=true freshObserver=true freshEnergy=true controller=true span=true observer=true') == 4

unit = 0
for path in (root / 'unit').glob('TEST-*.xml'):
    a = ET.parse(path).getroot().attrib
    assert all(int(a[k]) == 0 for k in ('failures', 'errors', 'skipped'))
    unit += int(a['tests'])
assert unit == 19
settings = json.loads((root / 'settings-restored.json').read_text())
assert len(settings) == 2 and all(r['original_sha256'] == r['restored_sha256'] for r in settings.values())
print(json.dumps({'scope': 'Cold-fault train retention plus occupied outbound packaged restart and manual recovery',
                  'dedicated': summary, 'native_profiles': 7, 'registered_tests': 33,
                  'unit_tests': unit}, indent=2, sort_keys=True))
