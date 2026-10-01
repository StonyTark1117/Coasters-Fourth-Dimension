#!/usr/bin/env python3
"""Audit native Sable queued-packet identity, reconnect and live-motion evidence."""
import json
import math
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
def read(name): return json.loads((root / name).read_text())
def events(name): return (root / (name + '-events.txt')).read_text()
def packets(name):
    result = []
    for line in events(name).splitlines():
        if 'FD_SNAPSHOT ' in line:
            r = dict(re.findall(r'(\w+)=([^ ]+)', line))
            result.append(tuple(r[k] for k in ('player', 'dimension', 'tick', 'sha256', 'railsSha256', 'rails', 'vehicles')))
    return result
runs = read('runs.json')
assert set(runs) == {f'{phase}-{role}' for phase in ('baseline', 'fixed') for role in ('server', 'dev', 'observer')} | {'native-base', 'native-combined'}
assert all(r['exit_status'] == 0 for r in runs.values())
clients = {}
for phase in ('baseline', 'fixed'):
    server = set(packets(phase + '-server'))
    for role, player in (('dev', 'Dev'), ('observer', 'Observer')):
        name = phase + '-' + role
        probe = read(name + '-probe.json')
        assert probe['player'] == player and probe['phase'] == 'complete'
        assert probe['initialActiveCallbacks'] == probe['reconnectedActiveCallbacks'] == 1
        assert probe['closedChannelMissing'] and probe['closedConnection'] and probe['newConnection'] and probe['oldChannelInactive']
        assert probe['oldChannel'] != probe['newChannel']
        assert probe['closedCallbacks'] == probe['replacedCallbacks'] == (1 if phase == 'baseline' else 0)
        log = events(name)
        assert 'udp-lifecycle-' + phase + '.jar' in log
        warnings = (root / (name + '-warnings.txt')).read_text()
        errors = warnings.count('Error executing packet handle task')
        if phase == 'baseline':
            assert errors >= 1 and 'SableUDPClientboundKeepAlivePacket.handleClient' in warnings and 'NullPointerException' in warnings
        else:
            assert errors == 0 and 'NullPointerException' not in warnings
            assert log.count('FD_UDP_DROP currentConnection=true connected=false currentChannel=false activeChannel=false') >= 2
            assert log.count('FD_UDP_DROP currentConnection=false connected=false currentChannel=false activeChannel=false') >= 2
        snapshots = packets(name)
        assert len(snapshots) > 10 and all(r in server for r in snapshots)
        clients[name] = {'queued_packet_errors': errors, 'matched_snapshots': len(snapshots),
                         'missing_body_warnings': warnings.count('movement packet for a non-existent sub-level'),
                         'unknown_passenger_warnings': warnings.count('Received passengers for unknown entity')}
        mods = (root / (name + '-mods.txt')).read_text()
        for version in ('Sable 2.0.3', 'Engineered 1.1.1', 'Track Styles 1.2.0', 'Additions 0.2.0', 'Magnetized 1.1.0', 'Immersion 0.2.3'):
            assert version in mods
ride = read('ride.json');assert (root / 'ride.exit').read_text().strip() == '0'
assert {r['phase'] for r in ride} == {'BOARDING', 'LIFTING', 'OUTBOUND', 'RETURNING', 'CAPTURING'}
assert all(not r['fault'] for r in ride) and ride[-1]['cycles'] == ride[0]['cycles'] + 1
assert ride[-1]['phase'] == 'BOARDING' and max(r['distance'] for r in ride) - min(r['distance'] for r in ride) > 20
interval = read('ride-interval.json')
for role in ('dev', 'observer'):
    values = {}
    for line in events('fixed-' + role).splitlines():
        if 'FD_CLIENT ' not in line or not interval['start'] <= line[1:9] <= interval['end']:
            continue
        row = dict(re.findall(r'(\w+)=(\S+)', line))
        angle = float(row['nativeAngle']);assert math.isfinite(angle)
        assert row['occupants'] == '[]' and row['riding'] == 'false'
        values.setdefault(row['body'], []).append(angle)
    assert len(values) == 8 and all(len(v) >= 3 and max(v) - min(v) > .01 for v in values.values())
    clients['fixed-' + role]['moving_native_samples'] = sum(map(len, values.values()))
    clients['fixed-' + role]['moving_native_carriers'] = len(values)
for name, skips in (('native-base', 6), ('native-combined', 0)):
    text = events(name)
    assert 'All 33 required tests passed' in text and 'BUILD SUCCESSFUL' in text and text.count('_SKIPPED:') == skips
    assert 'FD_BOUNDARY_MATRIX families=4 directions=2 wrongFamilyStops=8 missingProviderStops=8 matchingCrossings=8' in text
    assert 'FD_DELAYED_PASSENGER pendingIdentity=true pendingMass=true delayedReplay=true loadedEmpty=true' in text
    for phase in ('LIFTING', 'OUTBOUND', 'RETURNING', 'CAPTURING'):
        assert f'FD_PAX_PHASE_COMPLETE target={phase}' in text
        assert f'FD_PAX_PHASE_ENERGY target={phase} required={str(name == "native-combined").lower()}' in text
artifact = read('artifact-diff.json')
assert artifact['added'] == ['dev/fourthdimension/mixin/optional/SableClientQueueMixin.class'] and not artifact['removed']
assert set(artifact['changed']) == {'dev/fourthdimension/integration/OptionalMixinPlugin.class', 'fourthdimension.optional.mixins.json'}
assert all(v['compiled_sha256'] == v['jar_sha256'] for v in read('classes.json').values())
assert all(v['original_sha256'] == v['restored_sha256'] for v in read('settings-restored.json').values())
assert len(read('clients-stopped.json')) == 4 and all(v['client_exit'] == 0 and v['display_stop_signal'] == 'SIGTERM' for v in read('clients-stopped.json'))
unit = 0
for p in (root / 'unit').glob('TEST-*.xml'):
    a = ET.parse(p).getroot().attrib
    assert all(int(a[k]) == 0 for k in ('failures', 'errors', 'skipped'))
    unit += int(a['tests'])
assert unit == 19
print(json.dumps({'scope': 'Combined-pack two-client native queued disconnect/reconnect probes, empty PAX cycle and subsequent server stop; not adverse-network or occupied reconnect certification',
                  'clients': clients, 'native_profiles': {'base': 33, 'combined': 33}, 'unit_tests': unit,
                  'identical_probe_classes_in_baseline_and_fixed': True}, indent=2))
