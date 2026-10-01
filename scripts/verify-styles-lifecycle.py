#!/usr/bin/env python3
"""Audit the occupied Styles/base/Styles round trip from logs and native saves."""
import copy
import gzip
import hashlib
import json
import re
import subprocess
import sys
import tempfile
import uuid
import xml.etree.ElementTree as ET
import zipfile
from pathlib import Path

root = Path(sys.argv[1])


def data(name):
    return json.loads((root / name).read_text())


def log(name):
    return gzip.decompress((root / name).read_bytes()).decode()


def uid(words):
    return str(uuid.UUID(int=sum((w & 0xffffffff) << (96 - 32 * i) for i, w in enumerate(words))))


def stable(foundation, players=True, unloaded=False):
    value = copy.deepcopy(foundation)
    value['vehicles'].sort(key=lambda v: str(v['chassis']))
    for v in value['vehicles']:
        if unloaded:
            assert v['restraints'] == 0 and v['revision'] == 2
            v['restraints'], v['revision'] = 1, 1
        for c in v['carriers']:
            c.pop('angle')
            c.pop('omega')
            original = next(b for a in baseline['vehicles'] for b in a['carriers'] if b['body'] == c['body'])
            if unloaded:
                assert not c['occupants'] and not c['rider_seats']
                c['occupants'] = original['occupants']
                c['rider_seats'] = original['rider_seats']
            if not players:
                # Offline player reservations persist, but live occupants contain only pigs.
                c['occupants'] = [p for p in c['occupants'] if uid(p) not in player_ids]
            c['occupants'].sort()
            c['rider_seats'].sort(key=str)
    return value


manifest = data('manifest.json')
for name, expected in manifest['files'].items():
    assert hashlib.sha256((root / name).read_bytes()).hexdigest() == expected, name
baseline = data('seed/occupied-final.json')['foundation']
assert baseline['schema'] == 5 and len(baseline['rails']) == len(baseline['vehicles']) == 3
assert len({str(r['id']) for r in baseline['rails']}) == 3
assert all(r['provider'] == 'fourthdimension:original' and r['family'] == 'arrow_controlled' for r in baseline['rails'])
assert {r['appearance_style'] for r in baseline['rails']} == {
    'coasterssimulatedextratypes:arrow_4d',
    'coasterssimulatedextratypes:arrow_4d_dbl_spine',
    'fourthdimension:family_default',
}
assert all(r['profile'] == baseline['rails'][0]['profile'] for r in baseline['rails'])
assert baseline['rails'][0]['profile'][2]['angle'] > 6
assert sum(len(v['carriers']) for v in baseline['vehicles']) == 6
assert sum(len(c['occupants']) for v in baseline['vehicles'] for c in v['carriers']) == 5
player_ids = {uid(p['player']) for v in baseline['vehicles'] for c in v['carriers'] for p in c['rider_seats']}
assert len(player_ids) == 2
native = data('seed/native-pigs.json')
seats = {r['id']: r['parent_id'] for r in native}
assert len(seats) == 3
mounts = {uid(c['body']): sorted(uid(r) for r in c['occupants']) for v in baseline['vehicles'] for c in v['carriers']}
results = {}
for case in ['seed', 'removed', 'reinstalled']:
    installed = case != 'removed'
    raw = log(case + '/server.log.gz')
    assert ('Coasters Simulated: Track Styles 1.2.0' in raw) == installed
    if not installed:
        assert 'coasterssimulatedextratypes (version 1.2.0 -> MISSING)' in raw
    reports = [json.loads(line.split('FD_STYLES_LIFECYCLE ', 1)[1]) for line in raw.splitlines() if 'FD_STYLES_LIFECYCLE {' in line]
    label = 'occupied-verified' if case == 'reinstalled' else 'occupied-final'
    snap = data(case + '/' + label + '.json')
    assert snap in reports and snap['styles'] == installed
    assert stable(snap['foundation']) == stable(baseline), case
    actual_mounts = {}
    for v in snap['cars']:
        assert v['loaded'] and v['ready'] and v['restraints'] and not v['fault']
        for c in v['carriers']:
            assert c['loaded']
            actual_mounts[c['id']] = sorted(r['id'] for r in c['riders'])
            for r in c['riders']:
                assert r['alive'] and r['carrier'] == c['id'] and r['seat']
                assert r['player'] == (r['id'] in player_ids)
                if not r['player']:
                    assert r['seat'] == seats[r['id']]
    assert actual_mounts == mounts
    for r in snap['rails']:
        saved = next(s for s in baseline['rails'] if uid(s['id']) == r['id'])
        external = installed and saved['appearance_style'].startswith('coasterssimulatedextratypes:')
        assert r['resolved']
        assert r['effectiveStyle'] == (saved['appearance_style'] if external else 'fourthdimension:family_default')
        assert r['owners']['SPINE'] == ('coasterssimulatedextratypes:spines' if external else 'fourthdimension:original')
        assert all(owner == 'fourthdimension:original' for part, owner in r['owners'].items() if part != 'SPINE')
    commands = (root / case / 'commands.txt').read_text()
    assert 'reject_edit: Styles lifecycle fixture: reject_edit' in commands
    if case != 'seed':
        assert 'board_player' not in commands and 'styles_restart pigs' not in commands
    server_hashes = set(re.findall(r'FD_SNAPSHOT side=server.*?tick=(\d+) sha256=([a-f0-9]+) railsSha256=([a-f0-9]+)', raw))
    matches = {}
    for client, player in [('dev', 'Dev'), ('observer', 'Observer')]:
        client_raw = log(case + '/' + client + '.log.gz')
        assert (root / case / (client + '.exit')).read_text().strip() == '0' and 'BUILD SUCCESSFUL' in client_raw
        client_hashes = set(re.findall(r'FD_SNAPSHOT side=client.*?tick=(\d+) sha256=([a-f0-9]+) railsSha256=([a-f0-9]+)', client_raw))
        matches[client] = len(client_hashes & server_hashes)
        assert matches[client] >= 10
        views = re.findall(r'FD_STYLE_VIEW player=' + player + r' rail=(\S+) savedStyle=(\S+) effectiveStyle=(\S+) externalSpine=(true|false)', client_raw)
        for saved in baseline['rails']:
            if not saved['appearance_style'].startswith('coasterssimulatedextratypes:'):
                continue
            expected = (uid(saved['id']), saved['appearance_style'], saved['appearance_style'] if installed else 'fourthdimension:family_default', str(installed).lower())
            assert expected in views
    assert (root / case / 'server.exit').read_text().strip() == '0' and 'BUILD SUCCESSFUL' in raw
    # Decode retained native data rather than trusting its JSON transcription.
    checkpoints = [('world', '', case == 'reinstalled')]
    if case == 'reinstalled':
        checkpoints.append(('occupied-world', 'occupied-', False))
    for folder, prefix, unloaded in checkpoints:
        saved = data(case + '/' + prefix + 'foundation.json')['data']
        occupied_players = folder == 'occupied-world'
        assert stable(saved, players=occupied_players, unloaded=unloaded) == stable(baseline, players=occupied_players), (case, folder)
        decoded = json.loads(subprocess.check_output([sys.executable, 'scripts/inspect-foundation-save.py', str(root / case / folder / 'data/fourthdimension_foundation.dat')], text=True))
        assert decoded['data'] == saved
        pigs = data(case + '/' + prefix + 'native-pigs.json')
        with tempfile.TemporaryDirectory(prefix='fd-styles-audit-') as temporary:
            entity_dir = Path(temporary) / 'entities'
            entity_dir.mkdir()
            with zipfile.ZipFile(root / case / (prefix + 'entity-regions.zip')) as archive:
                for name in archive.namelist():
                    assert Path(name).name == name and name.endswith(('.mca', '.mcc'))
                    (entity_dir / name).write_bytes(archive.read(name))
            parsed = json.loads(subprocess.check_output([sys.executable, str(root / 'drivers/inspect-entities.py'), temporary], text=True))
            assert parsed == pigs
        assert {p['id'] for p in pigs} == set(seats) and all(p['health'] == 10 for p in pigs)
        if unloaded:
            assert all(p['parent_type'] is None and p['parent_id'] is None for p in pigs)
        else:
            assert all(p['parent_type'] == 'create:seat' and p['parent_id'] == seats[p['id']] for p in pigs)
    results[case] = {'occupied_riders': 5, 'carriers': 6, 'client_snapshot_matches': matches, 'external_spines': installed}

unloaded = data('reinstalled/unloaded.json')
assert unloaded in reports
assert all(c['ready'] and not c['restraints'] and not c['fault'] for c in unloaded['cars'])
assert all(not c['riders'] for v in unloaded['cars'] for c in v['carriers'])
for player in player_ids:
    p = data('reinstalled/' + player + '-player.json')
    decoded = json.loads(subprocess.check_output([sys.executable, 'scripts/inspect-foundation-save.py', str(root / 'reinstalled/world/playerdata' / (player + '.dat'))], text=True))
    assert p == decoded
    assert 'RootVehicle' not in p and not p.get('NoGravity', 0) and not p['abilities']['flying']
    assert p['OnGround'] and p['Health'] == 20 and p['Pos'][1] == 116
cleanup = data('cleanup.json')
assert cleanup['settings_before'] == cleanup['settings_after'] and cleanup['all_gone']
assert len(set(cleanup['pids'])) == 15 and cleanup['ports_closed'] == [25585, 25586]
suites = [ET.parse(p).getroot() for p in (root / 'unit-tests').glob('TEST-*.xml')]
assert sum(int(s.attrib['tests']) for s in suites) == 19
assert all(int(s.attrib[k]) == 0 for s in suites for k in ['failures', 'errors', 'skipped'])
assert (root / 'build.exit').read_text().strip() == '0' and 'BUILD SUCCESSFUL' in log('build.log.gz')
print(json.dumps({'cases': results, 'unit_tests': 19, 'normal_unload': True, 'cleanup': True}, indent=2))
