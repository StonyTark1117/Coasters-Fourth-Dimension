#!/usr/bin/env python3
"""Verify recorded occupied cold restart, removal, and saved-passenger regression evidence."""
from pathlib import Path
import gzip
import hashlib
import json
import math
import re
import sys
import xml.etree.ElementTree as ET

root = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parents[1] / 'docs/evidence/integrations/2026-10-27/engineered-restart'
manifest = json.loads((root / 'manifest.json').read_text())
for name, digest in manifest['files'].items():
    assert hashlib.sha256((root / name).read_bytes()).hexdigest() == digest, name


def read(name):
    with gzip.open(root / name, 'rt') as stream:
        return stream.read()


families = {'ARROW', 'ZACSPIN', 'FREE_SPIN', 'ZACSPIN_REVISED', 'PAX'}
uuid = r'([0-9a-f-]{36})'
before = read('before-restore.log.gz')
rejected = re.findall(r'FD_ENGINEERED_RESTART mount family=(\w+) rider=' + uuid + r' canceled=true restraints=true savedOccupant=true', before)
assert len(rejected) == 5 and {r[0] for r in rejected} == families
assert 'Occupied rider state lost: ARROW exists=true passenger=false health=10.0' in before
assert 'FD_ENGINEERED_RESTART RESTORED' not in before
before_commands = json.loads((root / 'before-commands.json').read_text())
assert before_commands[-1]['phase'] == 'restore' and before_commands[-1]['success'] is False
assert before_commands[-1]['exit'] == 0  # A clean server stop does not mean its GameTest passed.

for profile in ('engineered', 'combined'):
    commands = json.loads((root / (profile + '-commands.json')).read_text())
    assert [r['phase'] for r in commands] == ['prepare', 'restore', 'restoreAbsent']
    assert all(r['exit'] == 0 and r['success'] and '-PfdPackaged' in r['command'] for r in commands)
    expected_profiles = ['engineered', 'engineered', ''] if profile == 'engineered' else ['styles,engineered', 'styles,engineered', 'styles']
    assert [r['profiles'] for r in commands] == expected_profiles
    fixture = json.loads((root / (profile + '-fixture.json')).read_text())
    identities = {(r['family'], r['chassis'], r['rider']) for r in fixture}
    assert len(identities) == 5 and {r[0] for r in identities} == families
    for phase, marker in [('prepare', 'PREPARED'), ('restore', 'RESTORED'), ('restoreAbsent', 'REMOVED')]:
        log = read(f'{profile}-{phase}.log.gz')
        assert f'FD_ENGINEERED_RESTART {marker} families=5' in log
        assert 'failed at' not in log and 'BUILD SUCCESSFUL' in log
        assert 'ThreadedAnvilChunkStorage: All dimensions are saved' in log
        assert ('Coasters: Engineered 1.1.1 (coastersengineered)' in log) == (phase != 'restoreAbsent')
        assert ('Coasters Simulated: Track Styles 1.2.0 (coasterssimulatedextratypes)' in log) == (profile == 'combined')
        controls = re.findall(r'FD_ENGINEERED_RESTART newBoardingRejected family=(\w+)', log)
        assert len(controls) == 5 and set(controls) == families
        if phase == 'prepare':
            rows = re.findall(r'FD_ENGINEERED_RESTART prepared family=(\w+) chassis=' + uuid + r' rider=' + uuid + r' mode=LAUNCH retained=true', log)
        else:
            accepted = re.findall(r'FD_ENGINEERED_RESTART mount family=(\w+) rider=' + uuid + r' canceled=false restraints=true savedOccupant=true', log)
            assert len(accepted) == 5 and set(accepted) == {(f, r) for f, c, r in identities}
            if phase == 'restore':
                rows = re.findall(r'FD_ENGINEERED_RESTART restored family=(\w+) chassis=' + uuid + r' rider=' + uuid + r' mode=LAUNCH retained=true span=24 observer=true', log)
                departures = re.findall(r'FD_ENGINEERED_RESTART departed family=(\w+) travel=(\S+) observer=true mode=COAST', log)
                assert len(departures) == 4 and {r[0] for r in departures} == families - {'PAX'}
                assert all(math.isfinite(float(r[1])) and float(r[1]) > 12 for r in departures)
                complete = re.findall(r'FD_ENGINEERED_RESTART complete family=(\w+) departed=(true|false) observer=(true|false) ready=true unloaded=true', log)
                assert len(complete) == 5 and {r[0] for r in complete} == families
                assert all(r[1:] == (('false', 'false') if r[0] == 'PAX' else ('true', 'true')) for r in complete)
            else:
                rows = re.findall(r'FD_ENGINEERED_RESTART removedAddon family=(\w+) chassis=' + uuid + r' rider=' + uuid + r' retained=true addonBlocksAbsent=true', log)
                complete = re.findall(r'FD_ENGINEERED_RESTART removedComplete family=(\w+) ready=true unloaded=true', log)
                assert len(complete) == 5 and set(complete) == families
        assert len(rows) == 5 and set(rows) == identities
    print(f'PASS {profile}: same five cars/riders across restart and removal; four native departures, held PAX, forced boarding rejected')

unit = {k: 0 for k in ('tests', 'failures', 'errors', 'skipped')}
for name in manifest['files']:
    if name.startswith('TEST-') and name.endswith('.xml.gz'):
        with gzip.open(root / name) as stream:
            attrs = ET.parse(stream).getroot().attrib
        for key in unit:
            unit[key] += int(attrs[key])
assert unit == manifest['unitTests'] == {'tests': 26, 'failures': 0, 'errors': 0, 'skipped': 0}
for profile in ('base', 'combined'):
    log = read(f'{profile}-gametest.log.gz')
    assert '44 GAME TESTS COMPLETE' in log and 'All 44 required tests passed' in log
    assert 'FD_ENGINEERED_RESTART no dedicated phase: cold restart not exercised' in log
    assert 'BUILD SUCCESSFUL' in log and 'required tests failed' not in log
    if profile == 'combined':
        assert 'FD_ENGINEERED_INTERLOCK complete blocked=18 positive=4 nativeCalls=22' in log
        assert len(re.findall(r'FD_ENGINEERED_SEQUENCE complete family=', log)) == 5
    print(f'PASS {profile}: 44 registered native tests; cold restart explicitly separate')
print('PASS original canceled-restoration failure, exact identity retention, and 26 unit tests')
