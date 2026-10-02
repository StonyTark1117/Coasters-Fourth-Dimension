#!/usr/bin/env python3
"""Verify native held-cart presence, redstone sequence and observer evidence."""
from pathlib import Path
import gzip
import hashlib
import json
import math
import re
import sys
import xml.etree.ElementTree as ET

root = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parents[1] / 'docs/evidence/integrations/2026-10-02/engineered-presence'
manifest = json.loads((root / 'manifest.json').read_text())
for name, digest in manifest['files'].items():
    assert hashlib.sha256((root / name).read_bytes()).hexdigest() == digest, name


def read(name):
    with gzip.open(root / name, 'rt') as stream:
        return stream.read()


unit = {key: 0 for key in ('tests', 'failures', 'errors', 'skipped')}
for name in manifest['files']:
    if name.startswith('TEST-') and name.endswith('.xml.gz'):
        with gzip.open(root / name) as stream:
            attrs = ET.parse(stream).getroot().attrib
        for key in unit:
            unit[key] += int(attrs[key])
assert unit == manifest['unitTests'] == {'tests': 26, 'failures': 0, 'errors': 0, 'skipped': 0}
source = 'src/main/java/dev/fourthdimension/gametest/EngineeredSequenceGameTests.java'
assert json.loads((root / 'before-source.json').read_text())['files'][source] == manifest['sourceSha256'][source]
for name in ('before', 'motor-only-attempt'):
    log = read(name + '.log.gz')
    assert 'held family=ARROW mode=STOP nativePresent=false' in log, name
    assert 'Captured car disappeared from native controller: ARROW' in log, name
    assert '1 required tests failed' in log and 'BUILD FAILED' in log, name
    assert 'FD_ENGINEERED_INTERLOCK complete blocked=18 positive=4 nativeCalls=22' in log, name

families = {'ARROW', 'ZACSPIN', 'FREE_SPIN', 'ZACSPIN_REVISED', 'PAX'}
for profile in ('base', 'styles', 'engineered', 'combined'):
    log = read(profile + '.log.gz')
    assert '43 GAME TESTS COMPLETE' in log and 'All 43 required tests passed' in log, profile
    assert 'BUILD SUCCESSFUL' in log and 'required tests failed' not in log, profile
    installed = profile in ('engineered', 'combined')
    assert ('Coasters: Engineered 1.1.1 (coastersengineered)' in log) == installed, profile
    assert ('(coasterssimulatedextratypes)' in log) == (profile in ('styles', 'combined')), profile
    if not installed:
        assert 'FD_ENGINEERED_SEQUENCE absent/uninspected: scenario not exercised' in log, profile
        assert 'FD_ENGINEERED_SEQUENCE held' not in log, profile
        print(f'PASS {profile}: 43 registered tests, sequence explicitly skipped')
        continue
    held = re.findall(r'FD_ENGINEERED_SEQUENCE held family=(\w+) mode=STOP nativePresent=true', log)
    pulse = re.findall(r'FD_ENGINEERED_SEQUENCE pulseHeld family=(\w+) drift=(\S+) mode=LAUNCH nativePresent=true', log)
    departed = re.findall(r'FD_ENGINEERED_SEQUENCE departed family=(\w+) travel=(\S+) observer=true maxSignal=(\d+) mode=COAST tick=(\d+)', log)
    complete = re.findall(r'FD_ENGINEERED_SEQUENCE complete family=(\w+) departed=(true|false) observer=(true|false) ready=true unloaded=true', log)
    assert len(held) == 5 and set(held) == families, profile
    assert len(pulse) == 5 and {r[0] for r in pulse} == families, profile
    assert all(math.isfinite(float(r[1])) and float(r[1]) < .1 for r in pulse), profile
    assert len(departed) == 4 and {r[0] for r in departed} == families - {'PAX'}, profile
    assert all(math.isfinite(float(r[1])) and float(r[1]) > 12 and 1 <= int(r[2]) <= 15 and 200 < int(r[3]) < 500 for r in departed), profile
    assert len(complete) == 5 and {r[0] for r in complete} == families, profile
    assert all(r[1:] == (('false', 'false') if r[0] == 'PAX' else ('true', 'true')) for r in complete), profile
    impulses = re.findall(r'FD_ENGINEERED_INTERLOCK source=(\d) state=(\S+) blocked=(true|false) maxImpulse=(\S+)', log)
    assert len(impulses) == 22 and sum(r[2] == 'true' for r in impulses) == 18, profile
    assert all(math.isfinite(float(r[3])) and (float(r[3]) < 1e-9 if r[2] == 'true' else float(r[3]) > .001) for r in impulses), profile
    assert 'FD_ENGINEERED_INTERLOCK complete blocked=18 positive=4 nativeCalls=22' in log, profile
    print(f'PASS {profile}: five held presences, five blocked pulses, four occupied departures/observer signals, retained PAX, native capture/unload')
commands = json.loads((root / 'commands.json').read_text())
assert {row['profile'] for row in commands} == {'build', 'base', 'styles', 'engineered', 'combined'}
assert all(row['exit'] == 0 for row in commands)
assert all('-PfdPackaged' in row['command'] for row in commands if row['profile'] != 'build')
print('PASS original failure and insufficient motor-only fix retained; 26 unit tests and final packaged matrix verified')
