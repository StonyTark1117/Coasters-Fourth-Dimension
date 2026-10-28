#!/usr/bin/env python3
"""Verify bounded telemetry/Create evidence, without claiming client rendering acceptance."""
from pathlib import Path
import gzip
import hashlib
import json
import re
import sys
import xml.etree.ElementTree as ET

root = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parents[1] / 'docs/evidence/integrations/2026-10-27/reconciled-telemetry'
manifest = json.loads((root / 'manifest.json').read_text())
for name, digest in manifest['files'].items():
    assert hashlib.sha256((root / name).read_bytes()).hexdigest() == digest, name


def read(name):
    with gzip.open(root / name, 'rt') as stream:
        return stream.read()


expected = {
    ('ARROW', '2', '2', '8', 'true'),
    ('ZACSPIN', '1', '1', '8', 'true'),
    ('FREE_SPIN', '1', '1', '8', 'true'),
    ('ZACSPIN_REVISED', '1', '1', '8', 'true'),
    ('PAX', '4', '4', '16', 'false'),
}
for name in ('targeted', 'base-gametest', 'combined-gametest'):
    log = read(name + '.log.gz')
    rows = re.findall(r'FD_TELEMETRY family=(\w+) cars=(\d+) occupied=(\d+) capacity=(\d+) stationDispatch=(true|false) sources=6 packet=true', log)
    assert len(rows) == 5 and set(rows) == expected, name
    assert 'FD_TELEMETRY guards=7 readOnly=true recoveryPreserved=true staleSnapshotRejected=true' in log
    assert 'FD_TELEMETRY paxPhases=BOARDING,LIFTING,FAULT winchAuthority=true faultVisible=true' in log
    assert 'FD_TELEMETRY COMPLETE' in log and 'BUILD SUCCESSFUL' in log
    assert 'failed at' not in log and 'required tests failed' not in log
    if name.endswith('gametest'):
        assert '45 GAME TESTS COMPLETE' in log and 'All 45 required tests passed' in log
        assert 'FD_ENGINEERED_RESTART no dedicated phase: cold restart not exercised' in log
    if name == 'combined-gametest':
        assert 'FD_ENGINEERED_INTERLOCK complete blocked=18 positive=4 nativeCalls=22' in log
        assert len(re.findall(r'FD_ENGINEERED_SEQUENCE complete family=', log)) == 5
    print(f'PASS {name}: five families, six native display sources, seven guards, PAX authority')

failed = read('headless-client-path-diagnostic.log.gz')
assert 'Attempted to load class net/minecraft/client/Minecraft for invalid dist DEDICATED_SERVER' in failed
assert 'FD_TELEMETRY COMPLETE' not in failed
unit = {k: 0 for k in ('tests', 'failures', 'errors', 'skipped')}
for name in manifest['files']:
    if name.startswith('TEST-') and name.endswith('.xml.gz'):
        with gzip.open(root / name) as stream:
            attrs = ET.parse(stream).getroot().attrib
        for key in unit:
            unit[key] += int(attrs[key])
assert unit == manifest['unitTests'] == {'tests': 26, 'failures': 0, 'errors': 0, 'skipped': 0}
assert all(v == 0 for v in manifest['processExits'].values())
print('PASS hashes and 26 unit tests; client overlay/packet rendering remains separate')
