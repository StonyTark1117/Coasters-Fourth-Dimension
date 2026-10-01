#!/usr/bin/env python3
"""Audit real PAX rider reconnects during fault recovery and a fresh powered lift."""
import json
import math
import re
import sys
import uuid
from pathlib import Path

root = Path(sys.argv[1])


def read(name):
    return json.loads((root / name).read_text())


def uid(values):
    return str(uuid.UUID(bytes=b''.join((v & 0xffffffff).to_bytes(4, 'big') for v in values)))


def fields(line):
    return dict(re.findall(r'(\w+)=([^ ]+)', line))


events = {role: (root / (role + '-events.txt')).read_text() for role in ('server', 'dev', 'observer')}
rows = {}
for line in events['server'].splitlines():
    if 'FD_PAX_ENERGY_RESTART {' in line:
        d = json.loads(line.split('FD_PAX_ENERGY_RESTART ', 1)[1])
        assert d['label'] not in rows
        rows[d['label']] = d
before = read('before.json')
identity = {r['id']: b['body'] for c in before['cars'] for b in c['carriers'] for r in b['riders']}
assert len(identity) == 1
rider, carrier = next(iter(identity.items()))
assert before['phase'] == 'RETURNING' and max(c['speed'] for c in before['cars']) > .2
expected_cars = {c['chassis'] for c in before['cars']}
expected_bodies = {b['body'] for c in before['cars'] for b in c['carriers']}
assert len(expected_cars) == 4 and len(expected_bodies) == 8
empty_labels = {'offline', 'lift_offline', 'lift_join_0', 'lift_join_1', 'lift_join_2', 'unloaded'}
assert empty_labels <= rows.keys()
for label, d in rows.items():
    assert d['required'] and d['energy'] == 12345 and not d['externalDriveAllowed'], label
    assert d['carriageLoaded'] and d['carriage'] == before['carriage']
    assert len(d['cars']) == 4 and {c['chassis'] for c in d['cars']} == expected_cars
    bodies = [b for c in d['cars'] for b in c['carriers']]
    assert len(bodies) == 8 and {b['body'] for b in bodies} == expected_bodies
    assert all(b['loaded'] for b in bodies)
    occupants = [(r['id'], b['body']) for b in bodies for r in b['riders']]
    occupied = label not in empty_labels
    assert len(occupants) == int(occupied) and dict(occupants) == (identity if occupied else {}), label
    assert all(r['alive'] and r['mounted'] and r['carrier'] == b['body'] for b in bodies for r in b['riders'])
    assert all(c['consist'] == 4 and c['restraints'] == (label != 'unloaded') for c in d['cars'])
    t = d['telemetry']
    assert t['available'] and t['occupied'] == int(occupied) and t['capacity'] == 16 and t['cars'] == 4
    assert t['phase'] == d['phase'] and t['restraintsClosed'] == (label != 'unloaded')
    if d['phase'] == 'FAULT':
        assert d['interrupted'] == 'RETURNING'
        assert all(c['stopping'] and all(b['capture'] for b in c['carriers']) for c in d['cars'])
    else:
        assert not d['fault'] and all(not c['fault'] for c in d['cars'])
for name in ('span', 'controller', 'observer'):
    assert rows['restored'][name] == before[name]
assert all(rows[k]['phase'] == 'FAULT' for k in ('restored', 'pre_disconnect', 'offline', 'rejoined'))
assert rows['rejoined']['tick'] > rows['offline']['tick'] > rows['pre_disconnect']['tick']
brakes = [d for k, d in rows.items() if k.startswith('player_brake_')]
recovery = [d for k, d in rows.items() if k.startswith('player_recovery_')]
assert brakes and all(c['ready'] for c in brakes[-1]['cars'])
assert {d['phase'] for d in recovery} == {'CAPTURING', 'BOARDING'}
assert rows['station']['phase'] == 'BOARDING' and rows['station']['cycles'] == 1
assert all(rows[k]['phase'] == 'LIFTING' for k in ('lift_before_disconnect', 'lift_offline', 'lift_join_3'))
assert max(c['speed'] for c in rows['lift_before_disconnect']['cars']) > .5
assert max(c['speed'] for c in rows['lift_join_3']['cars']) > 3
assert rows['lift_join_3']['tick'] > rows['lift_offline']['tick'] > rows['lift_before_disconnect']['tick']
cycle = [d for k, d in rows.items() if k.startswith('cycle_') and k != 'cycle_station']
assert {d['phase'] for d in cycle} == {'OUTBOUND', 'RETURNING', 'CAPTURING', 'BOARDING'}
assert rows['cycle_station']['cycles'] == rows['unloaded']['cycles'] == 2
assert all(c['ready'] for c in rows['unloaded']['cars'])


def mechanics(d):
    return {uid(v['chassis']): (
        {k: v[k] for k in ('family', 'variant', 'damping', 'ballast', 'maintenance')},
        [{k: c[k] for k in ('body', 'side', 'pivot_height', 'longitudinal', 'child_pivot_offset', 'seats', 'ballast')}
         for c in v['carriers']]) for v in d['vehicles']}


def reservations(d):
    return {uid(c['body']): c['rider_seats'] for v in d['vehicles'] for c in v['carriers']}


base = read('before-save.json')['data']
assert len(base['rails']) == 3
assert len(reservations(base)[carrier]) == 1
assert uid(reservations(base)[carrier][0]['player']) == rider
look_errors = {}
p = read('before-player.json')
for label in ('offline', 'rejoined', 'recovered', 'lift-offline', 'cycle-station', 'unloaded'):
    d = read(label + '-save.json')['data']
    assert d['schema'] == base['schema'] == 5 and d['rails'] == base['rails']
    assert mechanics(d) == mechanics(base)
    if label != 'unloaded':
        assert reservations(d) == reservations(base)
    else:
        assert all(not c['rider_seats'] and not c['occupants'] for v in d['vehicles'] for c in v['carriers'])
    q = read(label + '-player.json')
    assert uid(q['UUID']) == rider and q['Health'] == 20 and q['NoGravity'] in (None, False, 0)
    assert 'fourthdimension_restore_gravity' not in (q['ForgeData'] or {})
    if label != 'unloaded':
        assert q['RootVehicle']['Entity']['id'] == 'create:seat'
        assert q['RootVehicle']['Entity']['Pos'] == p['RootVehicle']['Entity']['Pos']
        error = [abs((q['Rotation'][0] - p['Rotation'][0] + 180) % 360 - 180),
                 abs(q['Rotation'][1] - p['Rotation'][1])]
        assert max(error) < .25, (label, error)
        look_errors[label] = error
    else:
        assert q['RootVehicle'] is None and q['FallDistance'] == 0
        assert math.dist(q['Pos'], [-605.5, 181.5625, 608.5]) < .01


def packets(text):
    return [tuple(fields(l)[k] for k in ('player', 'dimension', 'tick', 'sha256', 'railsSha256', 'rails', 'vehicles'))
            for l in text.splitlines() if 'FD_SNAPSHOT ' in l]


server_packets = set(packets(events['server']))
clients = {}
shared = []
for role in ('dev', 'observer'):
    ps = packets(events[role])
    assert len(ps) > 100 and all(p in server_packets for p in ps)
    shared.append({p[1:] for p in ps})
    native = [l for l in events[role].splitlines() if 'FD_CLIENT ' in l]
    assert {fields(l)['body'] for l in native[-8:]} == expected_bodies
    assert all('riding=false' in l for l in native[-8:])
    warnings = (root / (role + '-warnings.txt')).read_text()
    assert 'Error executing packet handle task' not in warnings and 'NullPointerException' not in warnings
    clients[role] = {'server_matched_snapshots': len(ps),
                     'missing_body_warnings': warnings.count('movement packet for a non-existent sub-level'),
                     'unknown_passenger_warnings': warnings.count('Received passengers for unknown entity'),
                     'tcp_peer_reset': 'Connection reset by peer' in warnings}
assert len(shared[0] & shared[1]) > 100
cameras = [fields(l) for l in events['dev'].splitlines() if 'FD_IMMERSION ' in l]
mounted = [c for c in cameras if c['riding'] == 'true' and float(c['rawError']) >= 0]
assert len(mounted) > 500 and all(c['body'] == carrier for c in mounted)
max_error = max(float(c['rawError']) for c in mounted)
assert max_error < 1e-6
end = cameras[-1]
assert end['body'] == 'none' and end['riding'] == 'false' and float(end['travel']) == 0
assert abs(float(end['cameraUp']) - math.cos(math.radians(float(end['pitch'])))) < 1e-5
logins = [l for l in events['server'].splitlines() if '[minecraft/PlayerList]' in l and 'logged in' in l]
assert sum('Dev[' in l for l in logins) == 3 and sum('Observer[' in l for l in logins) == 1
commands = (root / 'commands.txt').read_text()
assert 'tp Dev ' not in commands and 'data merge entity Dev' not in commands and 'ride Dev' not in commands
assert commands.count('recover: PAX energy restart fixture: recover') == 1
assert commands.count('dispatch: PAX energy restart fixture: dispatch') == 1
assert 'pax_energy_restart unload' not in commands
manifest = read('manifest.json')
assert manifest['artifact']['sha256'] == 'daf8e9c039240c6bffa96e49f946d396934c334808c4b2fc31b4dedc0177fa81'
assert all(v == 0 for v in manifest['probes'].values())
for role in ('server', 'dev', 'observer'):
    assert manifest[role]['exit_status'] == 0
    mods = (root / (role + '-mods.txt')).read_text()
    for name in ('Minecraft 1.21.1', 'NeoForge 21.1.248', 'Create 6.0.10', 'Sable 2.0.3',
                 'Engineered 1.1.1', 'Styles 1.2.0', 'Additions 0.2.0', 'Magnetized 1.1.0', 'Immersion 0.2.3'):
        assert name in mods, (role, name)
    launch = (root / (role + '.sh.txt')).read_text()
    assert '-PfdValidation' in launch and '-PfdUdpProbe' not in launch
assert all(read('settings-restored.json').values())
assert len(read('clients-stopped.json')) == 2
assert all(c['exit_status'] == 0 for c in read('clients-stopped.json'))
print(json.dumps({'result': 'PASS', 'rider': rider, 'carrier': carrier,
                  'reconnect_phases': ['FAULT (RETURNING)', 'LIFTING'],
                  'station_cycles_after_recovery_and_run': 2, 'retained_energy': 12345,
                  'look_error_degrees': look_errors, 'mounted_camera_samples': len(mounted),
                  'maximum_raw_carrier_error_radians': max_error,
                  'shared_snapshot_states': len(shared[0] & shared[1]), 'clients': clients}, indent=2))
