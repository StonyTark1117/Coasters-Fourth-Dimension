#!/usr/bin/env python3
"""Audit native Create telemetry records against raw server and RCON evidence."""
import ast
import gzip
import hashlib
import json
import struct
import sys
from pathlib import Path

root = Path(sys.argv[1])
def read(path):
    p = root / path
    return (gzip.decompress(p.read_bytes()).decode() if p.suffix == '.gz' else p.read_text())
def data(path):
    return json.loads(read(path))

manifest = data('manifest.json')
for name, expected in manifest['files'].items():
    assert hashlib.sha256((root / name).read_bytes()).hexdigest() == expected, name
artifact = Path(manifest['artifact']['path'])
if artifact.exists():
    assert hashlib.sha256(artifact.read_bytes()).hexdigest() == manifest['artifact']['sha256']
server = read('server.log.gz')
reports = {r['label']: r for line in server.splitlines() if 'FD_PAX_ENERGY_RESTART {' in line
           for r in [json.loads(line.split('FD_PAX_ENERGY_RESTART ', 1)[1])]}
phases = {'BOARDING', 'LIFTING', 'OUTBOUND', 'RETURNING', 'CAPTURING', 'FAULT'}
fields = {'status', 'occupancy', 'restraints', 'dispatch', 'phase', 'fault'}
coverage = {source: {phase: set() for phase in phases} for source in ('winch', 'station')}
result = {}
identities = None
for run, initial_cycle in [('pass1', 4), ('pass2', 5)]:
    rows = data(run+'/samples.json.gz')
    links = data(run+'/links.json')
    commands = read(run+'/commands.raw.txt.gz')
    assert read(run+'/run.exit').strip() == '0'
    assert read(run+'/run.complete').strip() == 'complete'
    assert len(links) == 10 and len({tuple(l['target']) for l in links}) == 10
    rider_ids = None
    for row in rows:
        for side in ('before', 'after'):
            d = row[side]
            assert reports[d['label']] == d
            t = d['telemetry']
            assert d['energy'] == 12345 and not d['externalDriveAllowed'] and d['carriageLoaded']
            assert t['available'] and t['family'] == 'pax_shuttle' and t['cars'] == 4
            assert t['occupied'] == t['capacity'] == 16 and t['restraintsClosed']
            assert d['cycles'] in (initial_cycle, initial_cycle+1)
            cars = d['cars']
            bodies = [b for c in cars for b in c['carriers']]
            riders = [r for b in bodies for r in b['riders']]
            ids = (d['carriage'], tuple(c['chassis'] for c in cars), tuple(b['body'] for b in bodies))
            identities = identities or ids
            assert ids == identities and len(cars) == 4 and len(bodies) == 8
            assert all(b['loaded'] for b in bodies)
            assert len(riders) == 16 and len({r['id'] for r in riders}) == 16
            assert all(r['alive'] and r['mounted'] for r in riders)
            rider_ids = rider_ids or {r['id'] for r in riders}
            assert {r['id'] for r in riders} == rider_ids
        t = row['after']['telemetry']
        expected = {}
        start = commands.index('fd validation pax_energy_restart report '+row['before']['label']+':')
        end = commands.index('fd validation pax_energy_restart report '+row['after']['label']+':', start)
        raw = commands[start:end]
        for link in links:
            source, field = link['source'], link['field']
            if run == 'pass1' and source == 'winch' and field == 'status' and row['label'] in ('native_fault_source', 'power_restored_latched'):
                field = 'fault'
            key = source+'_'+field
            ready = t['dispatchReady'] if source == 'winch' else False
            denial = t['dispatchDenial'] if source == 'winch' else 'Vehicle outside station range'
            expected[key] = {
                'status': t['fault'] or ('Ready to dispatch' if ready else t['phase'].capitalize()),
                'occupancy': f"{t['occupied']} / {t['capacity']}",
                'restraints': 'Restraints closed: '+('Yes' if t['restraintsClosed'] else 'No'),
                'dispatch': 'Ready to dispatch' if ready else denial,
                'phase': t['phase'].capitalize(), 'fault': t['fault'],
            }[field]
            prefix = 'data get block '+' '.join(map(str, link['target']))+' front_text.messages[0]: '
            line = next(line for line in raw.splitlines() if line.startswith(prefix))
            actual = json.loads(ast.literal_eval(line.split('block data: ', 1)[1]))
            assert row['signs'][key] == actual
        same = row['before']['telemetry'] == t
        converged = same and row['signs'] == expected
        assert row['expected'] == expected and row['same_telemetry'] == same and row['converged'] == converged
        if converged:
            for key in expected:
                source, field = key.split('_', 1)
                coverage[source][t['phase']].add(field)
    shots = data(run+'/captures.json')
    labels = {'ready', 'first_lift', 'power_fault', 'native_fault_source', 'power_restored_latched', 'resumed_lift', 'outbound', 'returning', 'capturing', 'boarding'}
    assert {(s['label'], s['client']) for s in shots} == {(l, c) for l in labels for c in ('dev', 'observer')}
    assert len(shots) == 20
    by_label = {}
    for shot in shots:
        row = next(r for r in rows if r['label'] == shot['sample_label'] and r['after']['tick'] == shot['server_tick'])
        assert row['converged'] and row['after']['phase'] == shot['phase']
        image = (root/run/shot['image']).read_bytes()
        assert image[:8] == b'\x89PNG\r\n\x1a\n' and struct.unpack('>II', image[16:24]) == (1280, 720)
        by_label[shot['label']] = row['after']
    expected_phases = ['BOARDING','LIFTING','FAULT','FAULT','FAULT','LIFTING','OUTBOUND','RETURNING','CAPTURING','BOARDING']
    order = ['ready','first_lift','power_fault','native_fault_source','power_restored_latched','resumed_lift','outbound','returning','capturing','boarding']
    assert [by_label[l]['phase'] for l in order] == expected_phases
    assert all(by_label[b]['tick'] > by_label[a]['tick'] for a, b in zip(order, order[1:]))
    for l in ('power_fault','native_fault_source','power_restored_latched'):
        assert 'Winch power lost' in by_label[l]['fault'] and not by_label[l]['telemetry']['dispatchReady']
    for l in ('resumed_lift','outbound','returning','capturing','boarding'):
        assert not by_label[l]['fault']
    assert by_label['ready']['telemetry']['dispatchReady'] and by_label['boarding']['telemetry']['dispatchReady']
    assert by_label['boarding']['cycles'] == initial_cycle+1
    assert 'fd validation pax_energy_restart dispatch: Bind and capture the shuttle before starting' in commands
    assert 'fd validation pax_energy_restart unload: Shuttle restraints remain closed until final station capture' in commands
    assert 'setblock -607 180 608 minecraft:air: Changed the block' in commands
    assert 'setblock -607 180 608 create:creative_motor[facing=east]{ScrollValue:128}: Changed the block' in commands
    assert 'fd validation pax_energy_restart resume: PAX energy restart fixture: resume' in commands
    unloaded = data(run+'/unloaded.json')
    assert reports[unloaded['label']] == unloaded
    assert unloaded['phase'] == 'BOARDING' and unloaded['cycles'] == initial_cycle+1 and unloaded['energy'] == 12345
    assert unloaded['telemetry']['occupied'] == 0 and not unloaded['telemetry']['restraintsClosed']
    assert not [r for c in unloaded['cars'] for b in c['carriers'] for r in b['riders']]
    transitions = []
    for i, row in enumerate(rows[1:], 1):
        old = rows[i-1]['after']
        if row['after']['phase'] != old['phase']:
            settled = next(r['after'] for r in rows[i:] if r['converged'] and r['after']['phase'] == row['after']['phase'])
            transitions.append({'phase': settled['phase'], 'last_old_sample_tick': old['tick'], 'first_converged_tick': settled['tick'], 'bracket_ticks': settled['tick']-old['tick']})
    result[run] = {'samples': len(rows), 'converged': sum(r['converged'] for r in rows), 'captures':len(shots),'final_cycle':unloaded['cycles'],'transitions':transitions}
for source in coverage:
    assert all(v == fields for v in coverage[source].values()), coverage[source]
result['six_field_phase_coverage'] = {s: sorted(coverage[s]) for s in coverage}
binding = data('station-binding.json')
assert binding[0] == {'state':'unbound','signs':dict.fromkeys(fields,'No bound vehicle')}
assert binding[1] == {'state':'rebound','signs':{'status':'Boarding','occupancy':'0 / 16','restraints':'Restraints closed: No','dispatch':'Vehicle outside station range','phase':'Boarding','fault':''}}
binding_commands = read('pass1/commands.raw.txt.gz')
start = binding_commands.index('data remove block -616 180 608 vehicle:')
rebound = binding_commands.index('data merge block -616 180 608 {vehicle:', start)
for state, raw in [(binding[0], binding_commands[start:rebound]), (binding[1], binding_commands[rebound:])]:
    for link in data('pass1/links.json'):
        if link['source'] != 'station':
            continue
        prefix = 'data get block '+' '.join(map(str, link['target']))+' front_text.messages[0]: '
        line = next(line for line in raw.splitlines() if line.startswith(prefix))
        assert json.loads(ast.literal_eval(line.split('block data: ', 1)[1])) == state['signs'][link['field']]
for client in ('dev','observer'):
    log = read(client+'.log.gz')
    for phase in ('BOARDING','LIFTING','OUTBOUND','FAULT'):
        assert any('FD_STATION ' in line and 'phase='+phase in line and 'occupied=16, capacity=16' in line for line in log.splitlines())
    assert 'Vehicle outside station range' in log
    # Five-second diagnostics can miss the brief unbound interval; its HUD has screenshots.
    for forbidden in ('Error executing packet handle task','NullPointerException','Connection reset by peer','Unable to find body','Received passengers for unknown entity'):
        assert forbidden not in log, (client,forbidden)
    assert read(client+'.exit').strip() == '0'
    for prefix in ('station-', 'station-unbound-'):
        assert (root/(prefix+client+'.png')).is_file()
assert read('server.exit').strip() == '0'
cleanup = data('cleanup.json')
assert all(cleanup[k] for k in ('wrappers_gone','displays_gone','configs_restored','world_saved'))
result['visual_review'] = manifest['visual_review']
result['clean_shutdown'] = True
print(json.dumps(result, indent=2))
