#!/usr/bin/env python3
"""Audit occupied returning/capture cold restarts, including independent saved entity NBT."""
import json
import math
import sys
from pathlib import Path
root = Path(sys.argv[1])
runs = json.loads((root / 'runs.json').read_text())
state = json.loads((root / 'packaged-status.json').read_text())
assert state['status'] == 'complete'
FAULT = 'Shuttle reloaded: inspect and explicitly resume or recover'

def records(case):
    rows = {}
    for line in (root / (case + '-checks.txt')).read_text().splitlines():
        if 'FD_PAX_ENERGY_RESTART {' in line:
            d = json.loads(line.split('FD_PAX_ENERGY_RESTART ', 1)[1])
            assert d['label'] not in rows
            rows[d['label']] = d
    return rows

def mounts(d):
    return {c['chassis']: {b['body']: sorted(r['id'] for r in b['riders'])
                          for b in c['carriers']} for c in d['cars']}

def occupied(d):
    assert d['required'] and d['energy'] == 12345 and not d['externalDriveAllowed']
    assert len(d['cars']) == len({c['chassis'] for c in d['cars']}) == 4
    assert d['carriageLoaded']
    assert all(c['restraints'] and c['consist'] == 4 for c in d['cars'])
    bodies = [b for c in d['cars'] for b in c['carriers']]
    assert len(bodies) == len({b['body'] for b in bodies}) == 8
    assert all(b['loaded'] and len(b['riders']) == 2 for b in bodies)
    assert len({r['id'] for b in bodies for r in b['riders']}) == 16
    assert all(r['alive'] and r['mounted'] and r['carrier'] == b['body']
               for b in bodies for r in b['riders'])
    t = d['telemetry']
    assert t['available'] and t['cars'] == 4 and t['occupied'] == t['capacity'] == 16
    assert t['restraintsClosed'] and t['phase'] == d['phase']
    if d['phase'] != 'BOARDING':
        assert not t['dispatchReady']
    if d['phase'] == 'FAULT':
        assert d['fault'] == FAULT
        assert all(c['stopping'] and all(b['capture'] for b in c['carriers']) for c in d['cars'])
    assert all(not c['fault'] or (d['phase'] in ('FAULT', 'CAPTURING') and c['fault'] == FAULT)
               for c in d['cars'])

def mechanics(case):
    d = json.loads((root / (case + '-save.json')).read_text())['data']
    assert d['schema'] == 5 and len(d['rails']) == 3 and len(d['vehicles']) == 4
    cars = {}
    for v in d['vehicles']:
        row = {k: v[k] for k in ('chassis', 'family', 'variant', 'damping', 'ballast', 'maintenance')}
        row['carriers'] = [{k: c[k] for k in ('body', 'side', 'pivot_height', 'longitudinal',
                                            'child_pivot_offset', 'seats', 'ballast', 'rider_seats')}
                           for c in v['carriers']]
        cars[str(v['chassis'])] = row
    return sorted(d['rails'], key=lambda r: r['id']), cars

summary = {}
for phase in ('returning', 'capturing'):
    base = phase + '-before'
    before = records(base)['before']
    occupied(before)
    assert before['phase'] == phase.upper() and before['cycles'] == 0
    assert max(c['speed'] for c in before['cars']) > .2
    assert runs[base]['status'] == 'complete' and runs[base]['exit'] == 0
    entities_before = json.loads((root / (base + '-entities.json')).read_text())
    assert len(entities_before) == 16
    assert all(e['parent_type'] == 'create:seat' and e['health'] == 10 for e in entities_before)
    for profile in ('engineered', 'combined'):
        case = phase + '-' + profile
        assert runs[case]['status'] == 'complete' and runs[case]['exit'] == 0
        text = (root / (case + '-checks.txt')).read_text()
        assert 'loaded from union:' in text and 'passenger-load-fixed.jar' in text
        mods = (root / (case + '-mods.txt')).read_text()
        for name in ('Minecraft 1.21.1', 'NeoForge 21.1.248', 'Create 6.0.10',
                     'Coasters Simulated 0.1.5', 'Sable 2.0.3', 'Engineered 1.1.1'):
            assert name in mods
        for name in ('Track Styles 1.2.0', 'Additions 0.2.0', 'Magnetized 1.1.0',
                     'Immersion 0.2.3', 'Aeronautics 1.3.2', 'Interiors 0.6.1'):
            assert (name in mods) == (profile == 'combined')
        rows = records(case)
        restored = rows['restored']
        assert restored['phase'] == 'FAULT' and restored['interrupted'] == phase.upper()
        assert restored['tick'] - before['tick'] == 100
        for field in ('controller', 'observer', 'span', 'carriage'):
            assert restored[field] == before[field], (case, field)
        waiting = [d for label, d in rows.items() if label.startswith('brake_') or label == 'held']
        cycle = [d for label, d in rows.items() if label.startswith('cycle_')]
        assert waiting and cycle and rows['station']['phase'] == 'BOARDING'
        for d in [restored, *waiting, *cycle, rows['station']]:
            occupied(d)
            assert mounts(d) == mounts(before) and d['carriage'] == before['carriage']
        assert all(d['phase'] == 'FAULT' and d['interrupted'] == phase.upper() for d in waiting)
        drift = max(math.dist(d['carriagePosition'], restored['carriagePosition']) for d in waiting)
        assert drift < .3
        if phase == 'returning':
            assert all(c['ready'] for c in waiting[-1]['cars'])
        station = rows['station']
        assert station['cycles'] == before['cycles'] + 1
        assert all(c['ready'] and not c['fault'] for c in station['cars'])
        unloaded = rows['unloaded']
        assert unloaded['phase'] == 'BOARDING' and unloaded['cycles'] == station['cycles']
        assert unloaded['energy'] == 12345 and unloaded['telemetry']['occupied'] == 0
        assert all(not c['restraints'] and not c['fault'] and c['ready'] for c in unloaded['cars'])
        assert all(not b['riders'] for c in unloaded['cars'] for b in c['carriers'])
        commands = (root / (case + '-commands.txt')).read_text()
        assert 'dispatch: Bind and capture the shuttle before starting' in commands
        assert 'unload: Shuttle restraints remain closed until final station capture' in commands
        if phase == 'returning':
            assert 'resume: Interrupted gravity run requires manual recovery' in commands
            assert commands.count('recover: PAX energy restart fixture: recover') == 1
        else:
            assert commands.count('resume: PAX energy restart fixture: resume') == 1
        assert commands.count('Pig has the following entity data:') == 16
        entities = json.loads((root / (case + '-entities.json')).read_text())
        assert len(entities) == 16 and {e['id'] for e in entities} == {e['id'] for e in entities_before}
        assert all(e['parent_type'] is None and e['health'] == 10 for e in entities)
        assert mechanics(case) == mechanics(base)
        summary[case] = {'riders_restored_and_unloaded_alive': 16, 'retained_fe': 12345,
                         'fault_samples': len(waiting), 'cycle_samples': len(cycle),
                         'ticks_from_restore_to_recovery_sample': waiting[-1]['tick'] - restored['tick'],
                         'empty_carriage_drift_blocks': drift}
settings = json.loads((root / 'settings-restored.json').read_text())
assert len(settings) == 2 and all(d['original_sha256'] == d['restored_sha256'] for d in settings.values())
print(json.dumps({'scope': 'Native pig occupied returning/capture packaged restart and recovery',
                  'profiles': summary, 'human_clients': 'not established'}, indent=2, sort_keys=True))
