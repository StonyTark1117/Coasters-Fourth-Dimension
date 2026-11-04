#!/usr/bin/env python3
"""Verify provider boundary evidence and the occupied restart regression on its final JAR."""
from pathlib import Path
import gzip
import hashlib
import json
import re
import sys
import xml.etree.ElementTree as ET

root = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parents[1] / 'docs/evidence/integrations/2026-10-27/reconciled-providers'
manifest = json.loads((root / 'manifest.json').read_text())
for name, digest in manifest['files'].items():
    assert hashlib.sha256((root / name).read_bytes()).hexdigest() == digest, name


def read(name):
    return gzip.decompress((root / name).read_bytes()).decode()


families = {'ARROW', 'ZACSPIN', 'FREE_SPIN', 'ZACSPIN_REVISED', 'PAX'}
for profile in ('base', 'combined'):
    log = read(profile + '-gametest.log.gz')
    assert '47 GAME TESTS COMPLETE' in log and 'All 47 required tests passed' in log
    assert 'failed at' not in log and 'BUILD SUCCESSFUL' in log
    assert 'FD_PROVIDER persistence families=5 unknownPreserved=true magnetic=true appearance=true packet=true legacySchemas=6 invalidRejected=4 exactFactoryCalls=2 failedFactoryIsolated=true frozen=true' in log
    rows = re.findall(r'FD_PROVIDER native family=(\w+) placement=true snap=true rejected=10 restored=true recovery=true', log)
    assert len(rows) == 5 and set(rows) == families
    assert 'FD_PROVIDER COMPLETE nativeFamilies=5 rejected=50' in log
    for provider in ('failed_factory', 'contract_error', 'resolve_error'):
        assert log.count(f'Functional track provider fdtest:{provider} unavailable') == 1
    assert 'FD903_HISTORICAL schema3Rails=24 chassis=3 providerConversionRejected=true unavailableIdentityPreserved=true' in log
    assert 'FD_TELEMETRY COMPLETE' in log
    if profile == 'combined':
        assert 'FD_ENGINEERED_INTERLOCK complete blocked=18 positive=4 nativeCalls=22' in log
        assert len(re.findall(r'FD_ENGINEERED_SEQUENCE complete family=', log)) == 5
    print(f'PASS {profile}: 47 native tests; five provider families, 50 rejects, migration and telemetry')

commands = json.loads((root / 'restart-commands.json').read_text())
assert [r['phase'] for r in commands] == ['prepare', 'restore', 'restoreAbsent']
assert [r['profiles'] for r in commands] == ['styles,engineered', 'styles,engineered', 'styles']
assert all(r['success'] and r['exit'] == 0 and '-PfdPackaged' in r['command'] for r in commands)
fixture = json.loads((root / 'restart-fixture.json').read_text())
identities = {(r['family'], r['chassis'], r['rider']) for r in fixture}
assert len(identities) == 5 and {r[0] for r in identities} == families
uuid = r'([0-9a-f-]{36})'
for phase, marker in [('prepare', 'PREPARED'), ('restore', 'RESTORED'), ('restoreAbsent', 'REMOVED')]:
    log = read('restart-' + phase + '.log.gz')
    assert f'FD_ENGINEERED_RESTART {marker} families=5' in log
    assert 'BUILD SUCCESSFUL' in log and 'failed at' not in log
    assert 'Synthetic unavailable adapter initializer' not in log and 'FD_PROVIDER' not in log
    assert 'ThreadedAnvilChunkStorage: All dimensions are saved' in log
    assert ('Coasters: Engineered 1.1.1 (coastersengineered)' in log) == (phase != 'restoreAbsent')
    assert set(re.findall(r'FD_ENGINEERED_RESTART newBoardingRejected family=(\w+)', log)) == families
    pattern = {'prepare': 'prepared', 'restore': 'restored', 'restoreAbsent': 'removedAddon'}[phase]
    rows = re.findall(r'FD_ENGINEERED_RESTART ' + pattern + r' family=(\w+) chassis=' + uuid + r' rider=' + uuid, log)
    assert len(rows) == 5 and set(rows) == identities
    if phase == 'restore':
        departures = re.findall(r'FD_ENGINEERED_RESTART departed family=(\w+) travel=\S+ observer=true mode=COAST', log)
        assert len(departures) == 4 and set(departures) == families - {'PAX'}
        complete = re.findall(r'FD_ENGINEERED_RESTART complete family=(\w+) departed=(true|false) observer=(true|false) ready=true unloaded=true', log)
        assert len(complete) == 5 and {r[0] for r in complete} == families
        assert all(r[1:] == (('false', 'false') if r[0] == 'PAX' else ('true', 'true')) for r in complete)
    if phase == 'restoreAbsent':
        assert set(re.findall(r'FD_ENGINEERED_RESTART removedComplete family=(\w+) ready=true unloaded=true', log)) == families
print('PASS final-JAR combined occupied cold restart and Engineered removal, with test providers disabled')

unit = {k: 0 for k in ('tests', 'failures', 'errors', 'skipped')}
for name in manifest['files']:
    if name.startswith('TEST-') and name.endswith('.xml.gz'):
        attrs = ET.fromstring(gzip.decompress((root / name).read_bytes())).attrib
        for key in unit:
            unit[key] += int(attrs[key])
assert unit == manifest['unitTests'] == {'tests': 26, 'failures': 0, 'errors': 0, 'skipped': 0}
assert all(code == 0 for code in manifest['processExits'].values())
print('PASS hashes and 26 unit tests; external functional addons remain uncertified')
