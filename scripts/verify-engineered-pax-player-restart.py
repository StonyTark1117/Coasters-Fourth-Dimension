#!/usr/bin/env python3
"""Audit one occupied Engineered PAX return restart using two actual packaged clients."""
import json
import math
import re
import sys
import uuid
from pathlib import Path
root = Path(sys.argv[1])
def read(name): return json.loads((root / name).read_text())
def uid(values): return str(uuid.UUID(bytes=b''.join((v & 0xffffffff).to_bytes(4, 'big') for v in values)))
def records(name):
    result = {}
    for line in (root / (name + '-events.txt')).read_text().splitlines():
        if 'FD_PAX_ENERGY_RESTART {' in line:
            row = json.loads(line.split('FD_PAX_ENERGY_RESTART ', 1)[1])
            assert row['label'] not in result
            result[row['label']] = row
    return result
old, new = records('server'), records('server-restart')
before = old['player_before_returning']
restored, late, station, unloaded = [new[k] for k in ('player_restored', 'observer_late_join', 'player_station', 'player_unloaded')]
identity = {r['id']: b['body'] for c in before['cars'] for b in c['carriers'] for r in b['riders']}
assert len(identity) == 1
rider, carrier = next(iter(identity.items()))
assert before['phase'] == 'RETURNING' and max(c['speed'] for c in before['cars']) > .2
assert restored['phase'] == 'FAULT' and restored['interrupted'] == 'RETURNING'
for key in ('span', 'controller', 'observer', 'carriage'):
    assert before[key] == restored[key], key
brakes = [d for label, d in new.items() if label.startswith('player_brake_')]
recovery = [d for label, d in new.items() if label.startswith('player_recovery_')]
preparation = [d for label, d in old.items() if label.startswith('player_prepare_')]
assert {'LIFTING', 'OUTBOUND', 'RETURNING'} <= {d['phase'] for d in preparation}
assert brakes and recovery and {d['phase'] for d in recovery} == {'CAPTURING', 'BOARDING'}
for d in [before, restored, late, station, *preparation, *brakes, *recovery]:
    assert d['required'] and d['energy'] == 12345 and not d['externalDriveAllowed']
    assert d['carriageLoaded'] and d['carriage'] == before['carriage']
    assert len(d['cars']) == len({c['chassis'] for c in d['cars']}) == 4
    assert all(c['restraints'] and c['consist'] == 4 for c in d['cars'])
    bodies = [b for c in d['cars'] for b in c['carriers']]
    assert len(bodies) == len({b['body'] for b in bodies}) == 8 and all(b['loaded'] for b in bodies)
    assert {r['id']: b['body'] for b in bodies for r in b['riders']} == identity
    assert all(r['alive'] and r['mounted'] and r['carrier'] == b['body'] for b in bodies for r in b['riders'])
    t = d['telemetry']
    assert t['available'] and t['occupied'] == 1 and t['capacity'] == 16 and t['cars'] == 4
    assert t['restraintsClosed'] and t['phase'] == d['phase']
    if d['phase'] == 'FAULT':
        assert d['interrupted'] == 'RETURNING'
        assert all(c['stopping'] and all(b['capture'] for b in c['carriers']) for c in d['cars'])
assert all(c['ready'] for c in brakes[-1]['cars'])
assert not all(c['ready'] for c in restored['cars'])
assert late['tick'] > restored['tick']
restart_lines = (root / 'server-restart-events.txt').read_text().splitlines()
def seconds(line):
    h, m, s = (int(x) for x in re.search(r'\[(\d+):(\d+):(\d+)\]', line).groups())
    return h * 3600 + m * 60 + s
logins = {}
for line in restart_lines:
    if 'PlayerList' in line and 'logged in' in line:
        name = re.search(r'PlayerList\]: (Dev|Observer)\[', line)[1]
        assert name not in logins
        logins[name] = seconds(line)
restored_time = seconds(next(l for l in restart_lines if '"label":"player_restored"' in l))
assert set(logins) == {'Dev', 'Observer'} and logins['Observer'] > restored_time >= logins['Dev']
assert station['cycles'] == 1 and station['phase'] == 'BOARDING' and all(c['ready'] for c in station['cars'])
assert unloaded['energy'] == 12345 and unloaded['telemetry']['occupied'] == 0
assert all(not c['restraints'] and not c['fault'] and c['ready'] for c in unloaded['cars'])
assert all(not b['riders'] for c in unloaded['cars'] for b in c['carriers'])
a, frozen, z = [read(k + '-save.json')['data'] for k in ('before', 'frozen', 'unloaded')]
assert a == frozen  # On-disk foundation state before the first login, not a runtime proof.
assert a['schema'] == z['schema'] == 5 and a['rails'] == z['rails'] and len(a['rails']) == 3
saved_carrier = next(c for v in a['vehicles'] for c in v['carriers'] if uid(c['body']) == carrier)
assert len(saved_carrier['rider_seats']) == 1 and uid(saved_carrier['rider_seats'][0]['player']) == rider
assert all(not c['rider_seats'] and not c['occupants'] for v in z['vehicles'] for c in v['carriers'])
def mechanics(d):
    return {uid(v['chassis']): ({k: v[k] for k in ('family', 'variant', 'damping', 'ballast', 'maintenance')},
            [{k: c[k] for k in ('body', 'side', 'pivot_height', 'longitudinal', 'child_pivot_offset', 'seats', 'ballast')}
             for c in v['carriers']]) for v in d['vehicles']}
assert mechanics(a) == mechanics(z)
p, q = read('before-player.json'), read('after-player.json')
assert uid(p['UUID']) == uid(q['UUID']) == rider and p['Health'] == q['Health'] == 20
assert p['RootVehicle']['Entity']['id'] == 'create:seat' and q['RootVehicle'] is None
assert q['NoGravity'] in (None, False, 0) and q['FallDistance'] == 0
commands = (root / 'commands.txt').read_text()
assert 'resume: Interrupted gravity run requires manual recovery' in commands
assert 'recover: Stop and capture every carrier before shuttle recovery' in commands
assert commands.count('recover: PAX energy restart fixture: recover') == 1
assert '"1 / 16"' in commands and '"0 / 16"' in commands
roots = [l for l in commands.splitlines() if l.startswith('data get entity Dev RootVehicle:') and '{Entity:' in l]
restored_seat = [float(x.strip().rstrip('d')) for x in re.search(r'Pos: \[([^]]+)\]', roots[-1])[1].split(',')]
assert restored_seat == p['RootVehicle']['Entity']['Pos']
look_lines = [l for l in commands.splitlines() if l.startswith('data get entity Dev Rotation:')]
look = [float(x.rstrip('f')) for x in re.search(r'data: \[([^]]+)\]', look_lines[-2])[1].split(', ')]
look_error = [abs((look[0] - p['Rotation'][0] + 180) % 360 - 180), abs(look[1] - p['Rotation'][1])]
assert max(look_error) < .25

def packets(name):
    result = []
    for l in (root / (name + '-events.txt')).read_text().splitlines():
        if 'FD_SNAPSHOT ' in l:
            fields = dict(re.findall(r'(\w+)=([^ ]+)', l))
            result.append(tuple(fields[k] for k in ('player', 'dimension', 'tick', 'sha256', 'railsSha256', 'rails', 'vehicles')))
    return result
server = set(packets('server') + packets('server-restart'))
clients = {}; common = []
expected_bodies = {b['body'] for c in before['cars'] for b in c['carriers']}
for role in ('dev', 'observer'):
    rows = packets(role);assert len(rows) > 100 and all(r in server for r in rows)
    common.append({r[1:] for r in rows})
    events = (root / (role + '-events.txt')).read_text()
    native = [l for l in events.splitlines() if 'FD_CLIENT ' in l]
    assert {re.search(r'body=([^ ]+)', l)[1] for l in native[-8:]} == expected_bodies
    if role == 'dev':
        assert any('body=' + carrier in l and rider in l and 'riding=true' in l for l in native)
        assert all('riding=false' in l for l in native[-8:])
    clients[role] = {'server_matched_snapshots': len(rows), 'native_carriers_available_at_end': 8,
                     'transient_missing_body_messages': (root / (role + '-warnings.txt')).read_text().count('movement packet for a non-existent sub-level')}
assert len(common[0] & common[1]) > 100
manifest = read('manifest.json')
for role in ('server', 'server-restart', 'dev', 'observer'):
    assert manifest[role]['exit'] == 0
    mods = (root / (role + '-mods.txt')).read_text()
    for name in ('Minecraft 1.21.1', 'NeoForge 21.1.248', 'Create 6.0.10', 'Sable 2.0.3', 'Engineered 1.1.1'):
        assert name in mods
    for name in ('Track Styles', 'Additions', 'Immersion', 'Magnetized'):
        assert name not in mods
    assert 'passenger-load-fixed.jar' in (root / (role + '-events.txt')).read_text()
assert all(x['original_sha256'] == x['restored_sha256'] for x in read('settings-restored.json').values())
assert all(x['client_exit'] == 0 and x['display_stop_signal'] == 'SIGTERM' for x in read('clients-stopped.json'))
print(json.dumps({'scope': 'Engineered-only occupied returning cold restart; one real client rider, one late-joining spectator',
                  'rider_uuid': rider, 'carrier_uuid': carrier, 'fe_retained': 12345, 'completed_cycles': 1,
                  'normal_unload_health': q['Health'], 'saved_rider_maps_cleared': True,
                  'observer_join_delay_seconds': logins['Observer'] - logins['Dev'],
                  'recorded_local_look_error_degrees': look_error, 'clients': clients,
                  'identical_both_client_snapshots': len(common[0] & common[1]),
                  'fault_sample_to_ready_ticks': brakes[-1]['tick'] - restored['tick'],
                  'empty_carriage_drift_blocks': max(math.dist(d['carriagePosition'], restored['carriagePosition']) for d in [late, *brakes])}, indent=2))
