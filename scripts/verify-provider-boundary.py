#!/usr/bin/env python3
"""Audit moving provider/family boundary rejection and matching crossings."""
import hashlib
import itertools
import json
import math
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
def read(name): return json.loads((root / name).read_text())
def rows(text):
    result = []
    for line in text.splitlines():
        if 'FD_BOUNDARY_CASE ' not in line:
            continue
        fields, fault = line.split(' fault=', 1)
        row = dict(re.findall(r'(\w+)=(\S+)', fields))
        row['fault'] = fault
        result.append(row)
    return result

def check(row, stopped):
    z, speed = float(row['z']), float(row['speed'])
    assert math.isfinite(z) and math.isfinite(speed)
    assert row['restraints'] == 'true'
    assert row['stopping'] == str(stopped).lower()
    assert row['originalBinding'] == str(stopped).lower()
    assert row['nextBinding'] == str(not stopped).lower()
    if stopped:
        assert 301 < z < 340 and speed < .1
        assert row['fault'] == 'End of compatible track; capture and inspect the route'
    else:
        assert (z < 299 if row['direction'] == 'REVERSE' else z > 341)
        assert speed > .1 and not row['fault']

runs = read('runs.json')
profiles = {'base': 6, 'engineered': 3, 'styles': 6, 'additions': 4,
            'magnetized': 5, 'immersion': 6, 'combined': 0}
assert set(runs) == {'red', 'timing-base', *profiles}
timing = (root / 'timing-base-checks.txt').read_text()
assert runs['timing-base']['exit_status'] == 1
assert 'Matching boundary did not cross LEGACY/MATCHING/FORWARD' in timing
assert '1 required tests failed' in timing and 'BUILD FAILED' in timing
red = (root / 'red-checks.txt').read_text()
assert runs['red']['exit_status'] == 1
assert 'Unusable boundary did not brake ARROW/MISSING_PROVIDER/FORWARD' in red
assert '1 required tests failed' in red and 'BUILD FAILED' in red
before_rows = rows(red)
assert len(before_rows) == 6 and all(r['family'] == 'ARROW' for r in before_rows)
for row in before_rows:
    if row['mode'] == 'MISSING_PROVIDER':
        assert row['stopping'] == 'false' and not row['fault']
        assert row['originalBinding'] == 'true' and row['nextBinding'] == 'false'
        assert (float(row['z']) < 300 if row['direction'] == 'REVERSE' else float(row['z']) > 340)
    else:
        check(row, row['mode'] == 'WRONG_FAMILY')
expected = set(itertools.product(('ARROW', 'LEGACY', 'FREE_SPIN', 'REVISED'),
                               ('WRONG_FAMILY', 'MISSING_PROVIDER', 'MATCHING'), ('FORWARD', 'REVERSE')))
results = {}
for profile, skips in profiles.items():
    text = (root / (profile + '-checks.txt')).read_text()
    assert runs[profile]['exit_status'] == 0
    assert 'All 33 required tests passed' in text and 'BUILD SUCCESSFUL' in text
    assert text.count('_SKIPPED:') == skips
    samples = rows(text)
    assert len(samples) == 24
    assert {(r['family'], r['mode'], r['direction']) for r in samples} == expected
    for row in samples:
        check(row, row['mode'] != 'MATCHING')
    assert text.count('FD_BOUNDARY_MATRIX families=4 directions=2 wrongFamilyStops=8 missingProviderStops=8 matchingCrossings=8') == 1
    for i in range(2):
        assert f'FD_LINKED_SUPPORT damaged={i} eligible=false filtered=false drive=[false, false]' in text
    assert 'FD_CONNECTED_MATRIX cases=200 matchingAccepted=40 wrongFamilyRejected=160 endpoints=2 appearances=4 bindingRestored=true' in text
    assert 'FD_DELAYED_PASSENGER pendingIdentity=true pendingMass=true delayedReplay=true loadedEmpty=true' in text
    for phase in ('LIFTING', 'OUTBOUND', 'RETURNING', 'CAPTURING'):
        assert f'FD_PAX_PHASE_COMPLETE target={phase}' in text
        energy = str(profile in ('engineered', 'combined')).lower()
        assert f'FD_PAX_PHASE_ENERGY target={phase} required={energy}' in text
    mods = (root / (profile + '-mods.txt')).read_text()
    for version in ('Minecraft 1.21.1', 'NeoForge 21.1.248', 'Create 6.0.10', 'Sable 2.0.3'):
        assert version in mods
    for addon, version in {'styles': 'Track Styles 1.2.0', 'engineered': 'Engineered 1.1.1',
                           'additions': 'Additions 0.2.0', 'magnetized': 'Magnetized 1.1.0',
                           'immersion': 'Immersion 0.2.3'}.items():
        assert (version in mods) == (profile in (addon, 'combined'))
    results[profile] = {'registered': 33, 'absent_addon_skips': skips,
                       'wrong_family_stops': 8, 'missing_provider_stops': 8, 'matching_crossings': 8}
warnings = read('additional-warning-categories.json')
assert set(warnings) == set(profiles) and not any(warnings.values())
classes, before = read('classes.json'), read('before.json')
assert hashlib.sha256((root / 'BoundaryGuard-before.java.txt').read_bytes()).hexdigest() == before['source_sha256']
hashes = classes['dev/fourthdimension/vehicle/BoundaryGuard.class']
assert hashes['prior_jar_sha256'] == before['class_sha256']
assert hashes['compiled_sha256'] == hashes['jar_sha256'] != hashes['prior_jar_sha256']
assert all(v['compiled_sha256'] == v['jar_sha256'] for v in classes.values())
artifact = read('artifact-diff.json')
assert not artifact['removed']
assert all(n.startswith('dev/fourthdimension/gametest/BoundaryGameTests$') for n in artifact['added'])
assert set(artifact['changed']) == {'dev/fourthdimension/vehicle/BoundaryGuard.class', 'dev/fourthdimension/gametest/BoundaryGameTests.class'}
unit = 0
for path in (root / 'unit').glob('TEST-*.xml'):
    attrs = ET.parse(path).getroot().attrib
    assert all(int(attrs[k]) == 0 for k in ('failures', 'errors', 'skipped'))
    unit += int(attrs['tests'])
assert unit == 19
print(json.dumps({'scope': 'Four non-shuttle families, single cars, flat connected tracks, both directions, initial 4 m/s; not loaded trains, slopes or arbitrary stopping speeds',
                  'pre_fix_crossing_reproduced': True, 'profiles': results, 'unit_tests': unit}, indent=2))
