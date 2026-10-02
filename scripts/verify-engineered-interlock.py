#!/usr/bin/env python3
"""Check the recorded native Engineered motor-entry regression and profile matrix."""
import gzip
import hashlib
import json
import math
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

root = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parents[1] / 'docs/evidence/integrations/2026-10-02/engineered-interlock'
manifest = json.loads((root / 'manifest.json').read_text())
for name, digest in manifest['files'].items():
    assert hashlib.sha256((root / name).read_bytes()).hexdigest() == digest, name


unit = {key: 0 for key in ('tests', 'failures', 'errors', 'skipped')}
for name in manifest['files']:
    if name.startswith('TEST-') and name.endswith('.xml.gz'):
        with gzip.open(root / name) as stream:
            attributes = ET.parse(stream).getroot().attrib
        for key in unit:
            unit[key] += int(attributes[key])
assert unit == manifest['unitTests'] == {'tests': 26, 'failures': 0, 'errors': 0, 'skipped': 0}
test_source = 'src/main/java/dev/fourthdimension/gametest/EngineeredInterlockGameTests.java'
assert json.loads((root / 'before-source.json').read_text())['files'][test_source] == manifest['sourceSha256'][test_source]
if (root / 'artifact-diff.json').exists():
    comparison = json.loads((root / 'artifact-diff.json').read_text())
    assert comparison['finalJarSha256'] == manifest['jarSha256']
    assert manifest['baseCommit'] == 'eaa23bcb6ca7939d7e2119beec4275e292d1bd18'
    assert manifest['regressionBaseCommit'] == '8da29836e7c16da3036a01b95af08a1172273c62'
    assert comparison['added'] == comparison['removed'] == []
    assert comparison['changed'] == ['dev/fourthdimension/client/CarrierTransformValidation.class', 'dev/fourthdimension/runtime/CarrierValidationFixture.class']
    assert comparison['otherEntriesIdentical'] == 338
    assert comparison['preRebaseJarSha256'] == json.loads((root / 'pre-rebase/manifest.json').read_text())['jarSha256']
commands = json.loads((root / 'commands.json').read_text())
assert all(record['exit'] == 0 for record in commands)
assert {record['profile'] for record in commands} == {'build', 'base', 'styles', 'engineered', 'combined'}
assert all('-PfdPackaged' in record['command'] for record in commands if record['profile'] != 'build')


def read(name):
    with gzip.open(root / name, 'rt') as stream:
        return stream.read()


pattern = re.compile(r'FD_ENGINEERED_INTERLOCK source=(\d) state=(\S+) blocked=(true|false) maxImpulse=(\S+)')
before = read('before.log.gz')
rows = pattern.findall(before)
assert rows[0][:3] == ('0', 'healthy-before', 'false') and float(rows[0][3]) > .001
assert rows[-1][:3] == ('0', 'restraints-car0', 'true') and float(rows[-1][3]) > .001
assert '1 required tests failed' in before and 'BUILD FAILED' in before
assert 'Native motor interlock mismatch: 0/restraints-car0' in before

states = {f'{state}-car{car}' for state in ('stopping', 'maintenance', 'restraints', 'fault') for car in (0, 1)} | {'missing-peer'}
for profile in ('base', 'styles', 'engineered', 'combined'):
    log = read(profile + '.log.gz')
    assert '42 GAME TESTS COMPLETE' in log and 'All 42 required tests passed' in log, profile
    assert 'BUILD SUCCESSFUL' in log and 'required tests failed' not in log, profile
    installed = profile in ('engineered', 'combined')
    assert ('Coasters: Engineered 1.1.1 (coastersengineered)' in log) == installed, profile
    assert ('(coasterssimulatedextratypes)' in log) == (profile in ('styles', 'combined')), profile
    rows = pattern.findall(log)
    if installed:
        expected = {(str(source), state, 'true') for source in (0, 1) for state in states}
        expected |= {(str(source), state, 'false') for source in (0, 1) for state in ('healthy-before', 'healthy-after')}
        assert len(rows) == 22 and {row[:3] for row in rows} == expected, profile
        for source, state, blocked, raw in rows:
            value = float(raw)
            assert math.isfinite(value) and (value < 1e-9 if blocked == 'true' else value > .001), (profile, source, state)
        assert 'complete blocked=18 positive=4 nativeCalls=22' in log, profile
    else:
        assert not rows and 'FD_ENGINEERED_INTERLOCK absent/uninspected: scenario not exercised' in log, profile
    print(f'PASS {profile}: 42 registered native tests; interlock ' + ('18 rejections / 4 positive controls' if installed else 'explicitly skipped'))
print('PASS regression: old guard applies native impulse with open restraints; corrected guard rejects it')
