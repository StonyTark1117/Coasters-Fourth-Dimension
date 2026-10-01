#!/usr/bin/env python3
"""Verify the bounded Arrow combined-pack evidence directory, not a support matrix."""
import json
import math
import re
import sys
from pathlib import Path

root = Path(sys.argv[1])

def read(name):
    return (root / name).read_text()

def saved(name):
    return json.loads(read(name + '.json'))['data']

def packets(text):
    result = []
    for line in text.splitlines():
        if 'FD_SNAPSHOT ' in line:
            row = dict(re.findall(r'(\w+)=([^ ]+)', line.split('FD_SNAPSHOT ', 1)[1]))
            result.append(row)
    return result

def packet_key(row):
    return tuple(row[k] for k in ('player', 'dimension', 'tick', 'sha256', 'railsSha256', 'rails', 'vehicles'))

servers = read('server2-events.txt') + read('server3-events.txt')
server_packets = {packet_key(r) for r in packets(servers)}
clients = {name: read(file) for name, file in [('Dev', 'dev1-events.txt'), ('Observer', 'observer1-events.txt')]}
report = {'scope': 'Arrow, exact combined pack, two local clients; not full FD901–FD904 acceptance', 'clients': {}}
received = {}
for name, text in clients.items():
    rows = packets(text)
    assert len(rows) >= 100 and all(r['player'] == name and r['side'] == 'client' for r in rows)
    assert all(packet_key(r) in server_packets for r in rows), f'{name}: received an unmatched packet'
    assert all(r['rails'] == '8' and r['vehicles'] == '1' for r in rows)
    received[name] = {(r['dimension'], r['tick'], r['sha256']) for r in rows}
    states = [line.split('telemetry=', 1)[1] for line in text.splitlines() if 'FD_STATION ' in line]
    for expected in ('restraintsClosed=false', 'dispatchReady=true', 'retained=true, dispatchReady=false'):
        assert any(expected in state for state in states), f'{name}: missing {expected}'
    cameras = []
    for line in text.splitlines():
        if 'FD_IMMERSION ' not in line:
            continue
        row = dict(re.findall(r'(\w+)=([^ ]+)', line.split(' camera=', 1)[0]))
        assert row['metric'] == 'normalized-v1'
        for key in ('rawError', 'carrierUp', 'cameraUp', 'yaw', 'pitch', 'travel'):
            row[key] = float(row[key])
            assert math.isfinite(row[key])
        cameras.append(row)
    mounted = [r for r in cameras if r['riding'] == 'true' and r['body'] != 'none']
    assert len(mounted) >= 100
    assert all(0 <= r['rawError'] < 1e-6 for r in mounted)
    assert len({r['body'] for r in mounted}) == 1, 'Rider carrier changed across restart'
    inverted = [r for r in mounted if r['carrierUp'] < -.95]
    assert len(inverted) >= 3 and any(r['cameraUp'] < -.5 for r in inverted)
    transitions = [(p, c) for p, c in zip(cameras, cameras[1:]) if p['riding'] == 'true' and c['riding'] == 'false']
    assert transitions
    # The final transition is the normal exit after the resumed ride, before any later disconnect.
    _, exit_row = transitions[-1]
    assert exit_row['body'] == 'none' and exit_row['travel'] == 0
    assert abs(exit_row['cameraUp'] - math.cos(math.radians(exit_row['pitch']))) < .001
    report['clients'][name] = {
        'matched_packets': len(rows), 'station_states': len(set(states)),
        'mounted_samples': len(mounted), 'inverted_samples': len(inverted),
        'max_raw_error_radians': max(r['rawError'] for r in mounted),
        'carrier': mounted[0]['body'], 'normal_dismount_camera_restored': True,
    }
assert any('occupied=1, capacity=4' in line for line in clients['Observer'].splitlines() if 'FD_STATION' in line)
common = received['Dev'] & received['Observer']
assert len(common) >= 100
report['identical_both_client_snapshots'] = len(common)

before, frozen, resumed = (saved(n) for n in ('occupied-before', 'occupied-restarted-frozen', 'occupied-resumed'))
assert before == frozen, 'Cold restart changed saved data before simulation resumed'
assert before['schema'] == 5
assert before['rails'] == resumed['rails']
assert len(before['vehicles']) == len(resumed['vehicles']) == 1
b, a = before['vehicles'][0], resumed['vehicles'][0]
assert b['restraints'] == a['restraints'] == 1
assert {k: v for k, v in b.items() if k != 'carriers'} == {k: v for k, v in a.items() if k != 'carriers'}
assert len(b['carriers']) == len(a['carriers']) == 2
assert len({tuple(rider) for c in b['carriers'] for rider in c['occupants']}) == 2
for x, y in zip(b['carriers'], a['carriers']):
    assert len(x['occupants']) == len(y['occupants']) == 1
    assert len(x['rider_seats']) == len(y['rider_seats']) == 1
    assert x['rider_seats'][0]['player'] == x['occupants'][0]
    assert {k: v for k, v in x.items() if k not in ('angle', 'omega')} == {k: v for k, v in y.items() if k not in ('angle', 'omega')}
run = saved('running')['vehicles'][0]
assert not run['stopping'] and run['restraints'] and not run['fault']
assert all(len(c['occupants']) == 1 for c in run['carriers'])
opened = saved('opened')['vehicles'][0]
assert opened['stopping'] and opened['boarding_ready'] and not opened['restraints'] and not opened['fault']
assert all(len(c['occupants']) == 1 for c in opened['carriers'])
unloaded = saved('unloaded')['vehicles'][0]
assert all(not c['occupants'] and not c['rider_seats'] for c in unloaded['carriers'])
late = saved('style-latejoin')
assert len(before['rails']) == len(late['rails']) == 8
changes = []
for x, y in zip(before['rails'], late['rails']):
    if x != y:
        changes.append((x, y))
        assert {k: v for k, v in x.items() if k not in ('appearance_style', 'revision')} == {k: v for k, v in y.items() if k not in ('appearance_style', 'revision')}
assert len(changes) == 1
x, y = changes[0]
assert x['appearance_style'] == 'coasterssimulatedextratypes:arrow_4d' and y['appearance_style'] == 'coasterssimulatedextratypes:arrow_4d_dbl_spine'
assert y['revision'] == x['revision'] + 1
# Last Observer connection begins after the offline style change. Its initial packet
# must have a rail hash also received by Dev, with the two earlier styles observed.
observer_connections = servers.split('Observer joined the game')
assert len(observer_connections) == 4
late_packets = [r for r in packets(observer_connections[-1]) if r['player'] == 'Observer']
assert late_packets and any(r['railsSha256'] == late_packets[0]['railsSha256'] for r in packets(clients['Observer']))
assert len({r['railsSha256'] for r in packets(clients['Dev'])}) == 3
assert late_packets[0]['railsSha256'] in {r['railsSha256'] for r in packets(clients['Dev'])}
assert servers.count('Observer joined the game') == 3
assert servers.count('Dev joined the game') == 2
assert 'All 24 required tests passed' in read('native-tests.txt')
report.update(cold_restart_exact=True, resumed_seat_assignments_preserved=True,
              two_occupied_carriers_completed_return=True, normal_exit_cleared_assignments=True,
              style_change_preserved_mechanical_data=True)
print(json.dumps(report, indent=2))
