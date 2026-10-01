#!/usr/bin/env python3
"""Audit live outbound/return/capture rider reconnects in two pinned addon packs."""
import json
import math
import re
import sys
import uuid
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
manifest = json.loads((root / 'manifest.json').read_text())
assert manifest['artifact']['sha256'] == '08a1f0e6a51b8aad8ca31b65b104f3dda315c3913ee5361c28f8202fa1ad52ed'


def uid(values):
    return str(uuid.UUID(bytes=b''.join((v & 0xffffffff).to_bytes(4, 'big') for v in values)))


def fields(line):
    return dict(re.findall(r'(\w+)=([^ ]+)', line))


def packets(text):
    return [tuple(fields(l)[k] for k in ('player', 'dimension', 'tick', 'sha256', 'railsSha256', 'rails', 'vehicles'))
            for l in text.splitlines() if 'FD_SNAPSHOT ' in l]


def reservations(d):
    return {uid(c['body']): c['rider_seats'] for v in d['vehicles'] for c in v['carriers']}


def mechanics(d):
    return {uid(v['chassis']): (
        {k: v[k] for k in ('family', 'variant', 'damping', 'ballast', 'maintenance')},
        [{k: c[k] for k in ('body', 'side', 'pivot_height', 'longitudinal', 'child_pivot_offset', 'seats', 'ballast')}
         for c in v['carriers']]) for v in d['vehicles']}


results = {}
for profile in ('combined', 'engineered'):
    folder = root / profile

    def read(name):
        return json.loads((folder / name).read_text())

    events = {r: (folder / (r + '-events.txt')).read_text() for r in ('server', 'dev', 'observer')}
    before = read('before.json')
    identity = {r['id']: b['body'] for c in before['cars'] for b in c['carriers'] for r in b['riders']}
    assert len(identity) == 1
    rider, carrier = next(iter(identity.items()))
    cars = {c['chassis'] for c in before['cars']}
    bodies = {b['body'] for c in before['cars'] for b in c['carriers']}
    assert len(cars) == 4 and len(bodies) == 8
    records = {}
    for l in events['server'].splitlines():
        if 'FD_PAX_ENERGY_RESTART {' in l:
            d = json.loads(l.split('FD_PAX_ENERGY_RESTART ', 1)[1])
            assert d['label'] not in records
            records[d['label']] = d

    def check(d, occupied, unloaded=False):
        assert d['required'] and d['energy'] == 12345 and not d['externalDriveAllowed']
        assert d['carriageLoaded'] and d['carriage'] == before['carriage']
        assert len(d['cars']) == 4 and {c['chassis'] for c in d['cars']} == cars
        bs = [b for c in d['cars'] for b in c['carriers']]
        assert len(bs) == 8 and {b['body'] for b in bs} == bodies and all(b['loaded'] for b in bs)
        rs = [(r['id'], b['body']) for b in bs for r in b['riders']]
        assert len(rs) == occupied and dict(rs) == (identity if occupied else {})
        assert all(r['alive'] and r['mounted'] and r['carrier'] == b['body'] for b in bs for r in b['riders'])
        assert all(c['consist'] == 4 and c['restraints'] == (not unloaded) for c in d['cars'])
        t = d['telemetry']
        assert t['available'] and t['occupied'] == occupied and t['cars'] == 4 and t['capacity'] == 16
        assert t['phase'] == d['phase'] and t['restraintsClosed'] == (not unloaded)

    base = read('before-save.json')['data']
    initial_player = read('before-player.json')
    assert len(base['rails']) == 3 and len(reservations(base)[carrier]) == 1
    phases = {}
    for index, phase in enumerate(('outbound', 'returning', 'capturing'), start=1):
        rows = [d for k, d in records.items() if k.startswith(phase + '_')]
        assert rows
        for d in rows:
            assert d['phase'] != 'FAULT' and not d['fault'] and all(not c['fault'] for c in d['cars'])
            occupancy = d['telemetry']['occupied']
            assert occupancy in (0, 1)
            check(d, occupancy)
            if '_approach_' in d['label'] or '_finish_' in d['label'] or d['label'].endswith('_station_before'):
                assert occupancy == 1
        pre = records[phase + '_station_before']
        assert pre['phase'] == 'BOARDING' and pre['cycles'] == index
        approach = [d for d in rows if '_approach_' in d['label']]
        disconnect = [d for d in rows if '_disconnect_' in d['label']]
        join = [d for d in rows if '_join_' in d['label']]
        finish = [d for d in rows if '_finish_' in d['label']]
        assert approach[-1]['phase'] == phase.upper()
        assert disconnect[-1]['telemetry']['occupied'] == 0
        assert join[-1]['telemetry']['occupied'] == 1
        assert approach[-1]['tick'] < disconnect[-1]['tick'] < join[-1]['tick']
        assert finish[-1]['phase'] == 'BOARDING' and finish[-1]['cycles'] == index + 1
        assert all(c['ready'] for c in finish[-1]['cars'])
        assert {'LIFTING', 'OUTBOUND', 'RETURNING', 'CAPTURING', 'BOARDING'} <= {d['phase'] for d in rows}
        player_states = []
        for suffix in ('offline', 'station'):
            saved = read(phase + '-' + suffix + '-save.json')['data']
            assert saved['schema'] == base['schema'] == 5 and saved['rails'] == base['rails']
            assert mechanics(saved) == mechanics(base) and reservations(saved) == reservations(base)
            p = read(phase + '-' + suffix + '-player.json')
            assert uid(p['UUID']) == rider and p['Health'] == 20
            assert p['NoGravity'] in (None, False, 0) and 'fourthdimension_restore_gravity' not in (p['ForgeData'] or {})
            assert p['RootVehicle']['Entity']['id'] == 'create:seat'
            assert p['RootVehicle']['Entity']['Pos'] == initial_player['RootVehicle']['Entity']['Pos']
            player_states.append(p)
        old, new = [p['Rotation'] for p in player_states]
        error = [abs((new[0] - old[0] + 180) % 360 - 180), abs(new[1] - old[1])]
        assert max(error) < .25, (profile, phase, error)
        phases[phase] = {'disconnect_phase': disconnect[-1]['phase'], 'rejoin_phase': join[-1]['phase'],
                         'offline_simulation_ticks': join[-1]['tick'] - disconnect[-1]['tick'],
                         'look_drift_degrees': error, 'completed_cycles': finish[-1]['cycles']}
    unloaded = records['unloaded']
    check(unloaded, 0, unloaded=True)
    assert unloaded['phase'] == 'BOARDING' and unloaded['cycles'] == 4 and all(c['ready'] for c in unloaded['cars'])
    saved = read('unloaded-save.json')['data']
    assert saved['rails'] == base['rails'] and mechanics(saved) == mechanics(base)
    assert all(not c['rider_seats'] and not c['occupants'] for v in saved['vehicles'] for c in v['carriers'])
    p = read('unloaded-player.json')
    assert uid(p['UUID']) == rider and p['Health'] == 20 and p['RootVehicle'] is None and p['FallDistance'] == 0
    assert p['NoGravity'] in (None, False, 0) and 'fourthdimension_restore_gravity' not in (p['ForgeData'] or {})
    assert math.dist(p['Pos'], [-605.5, 181.5625, 608.5]) < .01
    server_packets = set(packets(events['server']))
    clients = {}
    for role in ('dev', 'observer'):
        ps = packets(events[role])
        assert len(ps) > 100 and all(p in server_packets for p in ps)
        native = [l for l in events[role].splitlines() if 'FD_CLIENT ' in l]
        assert {fields(l)['body'] for l in native[-8:]} == bodies
        assert all('riding=false' in l for l in native[-8:])
        warnings = (folder / (role + '-warnings.txt')).read_text()
        assert 'Error executing packet handle task' not in warnings and 'NullPointerException' not in warnings
        clients[role] = {'server_matched_snapshots': len(ps),
                         'missing_body_warnings': warnings.count('movement packet for a non-existent sub-level'),
                         'unknown_passenger_warnings': warnings.count('Received passengers for unknown entity'),
                         'tcp_peer_reset': 'Connection reset by peer' in warnings}
    native_dev = [l for l in events['dev'].splitlines() if 'FD_CLIENT ' in l]
    q = list(map(float, re.search(r'camera=\(([^)]+)\)', native_dev[-1])[1].split()))
    norm = math.sqrt(sum(v*v for v in q))
    x, y, z, w = [v / norm for v in q]
    camera_up = [2*(x*y-w*z), 1-2*(x*x+z*z), 2*(y*z+w*x)]
    yaw, pitch = map(math.radians, p['Rotation'])
    ordinary_up = [-math.sin(yaw)*math.sin(pitch), math.cos(pitch), math.cos(yaw)*math.sin(pitch)]
    # Native diagnostics print only four significant digits per quaternion component.
    assert math.dist(camera_up, ordinary_up) < .003
    server_look = [fields(l) for l in events['server'].splitlines() if 'FD_RIDER_LOOK ' in l]
    client_look = [fields(l) for l in events['dev'].splitlines() if 'FD_RIDER_LOOK ' in l]
    assert len(server_look) == len(client_look) == 4
    for a, b in zip(server_look, client_look):
        assert all(a[k] == b[k] for k in ('player', 'carrier', 'seat', 'yaw', 'pitch'))
        assert a['player'] == rider and a['carrier'] == carrier
        assert abs(float(a['yaw']) - initial_player['Rotation'][0]) < .25
        assert abs(float(a['pitch']) - initial_player['Rotation'][1]) < .25
    camera = None
    if profile == 'combined':
        cs = [fields(l) for l in events['dev'].splitlines() if 'FD_IMMERSION ' in l]
        mounted = [c for c in cs if c['riding'] == 'true' and float(c['rawError']) >= 0]
        assert len(mounted) > 100 and all(c['body'] == carrier for c in mounted)
        error = max(float(c['rawError']) for c in mounted)
        assert error < 1e-6
        assert cs[-1]['body'] == 'none' and float(cs[-1]['travel']) == 0
        assert abs(float(cs[-1]['cameraUp']) - math.cos(math.radians(float(cs[-1]['pitch'])))) < 1e-5
        mouse = read('mouse-look.json')
        assert mouse['rawMouseInput'] and mouse['after_two_relative_moves'] == mouse['after_one_second']
        assert abs(mouse['after_one_second'][0] - mouse['before'][0]) >= 2
        look_text = re.findall(r'data get entity Dev Rotation: Dev has the following entity data: \[([^]]+)\]', (folder / 'commands.txt').read_text())
        for expected in (mouse['before'], mouse['after_one_second']):
            assert any(all(abs(float(a.rstrip('f')) - b) < .001 for a, b in zip(v.split(', '), expected)) for v in look_text)
        camera = {'mounted_samples': len(mounted), 'maximum_raw_error_radians': error,
                  'mouse_yaw_change_degrees': mouse['after_one_second'][0] - mouse['before'][0]}
    else:
        assert 'FD_IMMERSION ' not in events['dev']
    logins = [l for l in events['server'].splitlines() if '[minecraft/PlayerList]' in l and 'logged in' in l]
    assert sum('Dev[' in l for l in logins) == 4 and sum('Observer[' in l for l in logins) == 1
    commands = (folder / 'commands.txt').read_text()
    assert 'tp Dev ' not in commands and 'data merge entity Dev' not in commands and 'ride Dev' not in commands
    assert commands.count('dispatch: PAX energy restart fixture: dispatch') == 3
    assert commands.count('recover: PAX energy restart fixture: recover') == 1
    assert 'pax_energy_restart unload' not in commands
    run = manifest['profiles'][profile]
    assert all(v == 0 for v in run['scenarios'].values())
    for role in ('server', 'dev', 'observer'):
        assert run[role]['exit_status'] == 0
        mods = (folder / (role + '-mods.txt')).read_text()
        for name in ('Minecraft 1.21.1', 'NeoForge 21.1.248', 'Create 6.0.10', 'Sable 2.0.3', 'Engineered 1.1.1'):
            assert name in mods
        for name in ('Styles 1.2.0', 'Additions 0.2.0', 'Magnetized 1.1.0', 'Immersion 0.2.3'):
            assert (name in mods) == (profile == 'combined')
        launch = (folder / (role + '.sh.txt')).read_text()
        assert '-PfdValidation' in launch and '-PfdUdpProbe' not in launch
    assert all(c['exit_status'] == 0 for c in read('clients-stopped.json'))
    results[profile] = {'phases': phases, 'clients': clients, 'immersion_camera': camera, 'dismount_camera_up_error': math.dist(camera_up, ordinary_up)}
baseline = root / 'baseline-combined'
a = json.loads((baseline / 'outbound-offline-player-full.json').read_text())['Rotation']
b = json.loads((baseline / 'outbound-station-player-full.json').read_text())['Rotation']
baseline_error = [abs((b[0] - a[0] + 180) % 360 - 180), abs(b[1] - a[1])]
assert baseline_error[0] > 1
assert all(manifest['baseline'][r]['exit_status'] == 0 for r in ('server', 'dev', 'observer'))
assert 'FD_RIDER_LOOK ' not in (baseline / 'dev-events.txt').read_text()
for name in ('native-base', 'native-engineered', 'native-combined'):
    assert manifest['native'][name]['exit_status'] == 0
    text = (root / (name + '-events.txt')).read_text()
    assert 'All 33 required tests passed' in text and 'BUILD SUCCESSFUL' in text
unit = [ET.parse(p).getroot() for p in (root / 'unit').glob('TEST-*.xml')]
assert sum(int(x.attrib['tests']) for x in unit) == 19
assert all(int(x.attrib['failures']) == int(x.attrib['errors']) == 0 for x in unit)
assert all(json.loads((root / 'settings-restored.json').read_text()).values())
print(json.dumps({'result': 'PASS', 'baseline_outbound_look_error_degrees': baseline_error, 'profiles': results}, indent=2))
