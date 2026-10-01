#!/usr/bin/env python3
"""Audit recorded native Ponder playback; screenshots still require visual review."""
import ast
import gzip
import hashlib
import json
import re
import struct
import sys
import zipfile
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
manifest = json.loads((root / 'manifest.json').read_text())
pattern = re.compile(r'FD_PONDER player=(\w+) scene=fourthdimension:(\w+) time=(\d+) total=(\d+) keyframes=([\d,]*) finished=(true|false) width=(\d+) height=(\d+)')
scenes = {
    'family_arrow': (550, [6, 106, 206, 326]),
    'family_legacy': (550, [6, 106, 206, 326]),
    'family_free_spin': (550, [6, 106, 206, 326]),
    'family_revised': (550, [6, 106, 206, 326]),
    'family_pax': (760, [6, 106, 206, 316, 416, 536]),
    'station_operations': (570, [6, 116, 226, 346, 466]),
}
result = {}
for run in ('baseline', 'framed'):
    folder = root / run
    captures = json.loads((folder / 'captures.json').read_text())
    assert len(captures) == (76 if run == 'baseline' else 110), (run, len(captures))
    assert len({r['image'] for r in captures}) == len(captures)
    clients = {}
    for client, player in [('dev', 'Dev'), ('observer', 'Observer')]:
        text = gzip.decompress((folder / (client + '.log.gz')).read_bytes()).decode()
        samples = [m for line in text.splitlines() if (m := pattern.search(line))]
        playback = {}
        for scene, (total, keys) in scenes.items():
            rows = [m for m in samples if m[1] == player and m[2] == scene]
            # Baseline contains an earlier toast-obscured attempt. Audit its last playback.
            starts = [i for i, m in enumerate(rows) if i == 0 or int(m[3]) < int(rows[i-1][3])]
            rows = rows[starts[-1]:]
            times = [int(m[3]) for m in rows]
            assert times[0] <= 20 and times[-1] >= total and rows[-1][6] == 'true', (run, client, scene)
            assert all(0 <= b-a <= 20 for a, b in zip(times, times[1:])), (run, client, scene, times)
            assert all(int(m[4]) == total and [int(x) for x in m[5].split(',')] == keys for m in rows)
            assert all((int(m[7]), int(m[8])) == (427, 240) for m in rows)
            targets = {f'caption_{i+1}': k+35 for i, k in enumerate(keys)}
            if scene != 'station_operations':
                rotation = keys[4 if scene == 'family_pax' else 2]
                targets.update(appearance_warning=total-65, rotation_end=rotation+85)
                if run == 'framed':
                    targets.update({f'rotation_{offset}': rotation+offset for offset in (15, 55, 75)})
                if scene == 'family_pax':
                    targets['lift_end'] = keys[3]+65
                    if run == 'framed':
                        targets.update({f'lift_{offset}': keys[3]+offset for offset in (15, 55)})
            shots = [r for r in captures if r['client'] == client and r['scene'] == scene]
            assert {r['phase']: r['target'] for r in shots} == targets
            assert len(shots) == len(targets)
            for shot in shots:
                assert 0 <= shot['time']-shot['target'] <= 20
                assert shot['sample'] in [m[0] for m in rows]
                assert shot['total'] == total and shot['keyframes'] == keys
                image = (folder / shot['image']).read_bytes()
                assert image[:8] == b'\x89PNG\r\n\x1a\n'
                assert struct.unpack('>II', image[16:24]) == (854, 480)
            playback[scene] = {'samples': len(rows), 'captures': len(shots), 'first_tick': times[0], 'last_tick': times[-1]}
        clients[client] = playback
        assert (folder / (client + '.exit')).read_text().strip() == '0'
        assert 'Error executing packet handle task' not in text and 'NullPointerException' not in text
    assert (folder / 'server.exit').read_text().strip() == '0'
    assert (folder / 'capture-scenes.exit').read_text().strip() == '0'
    commands = (folder / 'commands.txt').read_text()
    assert all('ponder fourthdimension:' + item + ' @a' in commands for item in (
        'arrow_car', 'legacy_zacspin_car', 'free_spin_car', 'revised_zacspin_car', 'pax_car', 'boarding_station'))
    result[run] = clients
folder = root / 'framed'
server = gzip.decompress((folder / 'server.log.gz').read_bytes()).decode()
reports = {d['label']: d for line in server.splitlines() if 'FD_PAX_ENERGY_RESTART {' in line
           for d in [json.loads(line.split('FD_PAX_ENERGY_RESTART ', 1)[1])]}
displays = json.loads((folder / 'winch-displays.json').read_text())
assert len(displays) == 12 and (folder / 'winch-displays.exit').read_text().strip() == '0'
winch = {}
for state, occupied in [('empty_open', 0), ('occupied_closed', 16)]:
    before, after = (reports[state+'_'+suffix] for suffix in ('before', 'after'))
    assert before['telemetry'] == after['telemetry']
    for d in (before, after):
        t = d['telemetry']
        assert d['phase'] == 'BOARDING' and not d['fault'] and d['cycles'] == 4 and d['energy'] == 12345
        assert not d['externalDriveAllowed'] and d['carriageLoaded'] and len(d['cars']) == 4
        assert t['available'] and t['cars'] == 4 and t['capacity'] == 16 and t['occupied'] == occupied
        assert t['boardingReady'] and t['restraintsClosed'] == bool(occupied) and t['dispatchReady'] == bool(occupied)
        riders = [r for c in d['cars'] for body in c['carriers'] for r in body['riders']]
        assert len(riders) == occupied and len({r['id'] for r in riders}) == occupied
        assert all(r['alive'] and r['mounted'] for r in riders)
    expected = {
        'status': 'Ready to dispatch' if occupied else 'Boarding',
        'occupancy': f'{occupied} / 16',
        'restraints': 'Restraints closed: '+('Yes' if occupied else 'No'),
        'dispatch': 'Ready to dispatch' if occupied else 'Every shuttle car must be available, retained on matching track, and restrained',
        'phase': 'Boarding', 'fault': '',
    }
    actual = {r['field']: json.loads(ast.literal_eval(r['sign_response'].split('block data: ', 1)[1].strip()))
              for r in displays if r['state'] == state}
    assert actual == expected, (state, actual)
    for client in ('dev', 'observer'):
        text = gzip.decompress((folder / (client+'.log.gz')).read_bytes()).decode()
        assert any('FD_STATION ' in line and 'x=-606, y=180, z=608' in line
                   and f'occupied={occupied}, capacity=16' in line
                   and f'dispatchReady={str(bool(occupied)).lower()}' in line
                   and 'phase=BOARDING' in line for line in text.splitlines()), (client, state)
        assert (folder / (client+'-goggles-'+state+'.png')).exists()
    winch[state] = actual
assert reports['guidance_unloaded']['telemetry'] == reports['empty_open_before']['telemetry']
result['winch_display_fields'] = winch
suites = [ET.parse(f).getroot() for f in (root/'unit-tests').glob('TEST-*.xml')]
assert sum(int(s.attrib['tests']) for s in suites) == 19
assert all(int(s.attrib[k]) == 0 for s in suites for k in ('failures', 'errors', 'skipped'))
result['unit_tests'] = 19
for relative, expected in manifest['files'].items():
    assert hashlib.sha256((root / relative).read_bytes()).hexdigest() == expected, relative
for artifact in manifest['artifacts'].values():
    path = Path(artifact['path'])
    if path.exists():
        assert hashlib.sha256(path.read_bytes()).hexdigest() == artifact['sha256'], path
        with zipfile.ZipFile(path) as jar:
            for name, expected in artifact['classes'].items():
                assert hashlib.sha256(jar.read(name)).hexdigest() == expected, name
result['visual_review'] = manifest['visual_review']
print(json.dumps(result, indent=2))
