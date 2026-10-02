#!/usr/bin/env python3
"""Audit five-family native ordinary-cart access across exact optional profiles."""
import gzip
import hashlib
import itertools
import json
import math
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
def read(name):
    return json.loads((root / name).read_text())
def log(name):
    return gzip.decompress((root / (name + '.log.gz')).read_bytes()).decode()
def rows(text, prefix):
    return [dict(re.findall(r'(\w+)=(\S+)', line)) for line in text.splitlines()
            if line.startswith(prefix + ' ')]

manifest = read('manifest.json')
assert manifest
for name, expected_hash in manifest.items():
    assert hashlib.sha256((root / name).read_bytes()).hexdigest() == expected_hash, name
profiles = {'base': 7, 'engineered': 4, 'styles': 7, 'additions': 5,
            'magnetized': 5, 'immersion': 7, 'combined': 0}
families = ('arrow_controlled', 'zacspin_legacy', 'sns_free_spin', 'zacspin_revised', 'pax_shuttle')
conditions = {'bare': True, 'body_above': False, 'body_beside': False,
              'ordinary_track': True, 'invalid_rail': False, 'missing:functional': False,
              'coasterssimulatedextratypes:spines': False}
expected = {(f, str(a), c): str(allowed).lower()
            for f, a, (c, allowed) in itertools.product(families, range(4), conditions.items())}
runs = read('runs.json')
assert set(runs) == {*profiles, 'setup-base', 'engagement-base', 'precision-base'}
results = {}
for profile, skips in profiles.items():
    text = log(profile)
    assert hashlib.sha256(text.encode()).hexdigest() == runs[profile]['raw_log_sha256']
    assert runs[profile]['exit_status'] == 0
    assert 'All 37 required tests passed' in text and 'BUILD SUCCESSFUL' in text
    assert text.count('_SKIPPED:') == skips
    decisions = rows(text, 'FD_ORDINARY_DECISION')
    actual = {(r['family'], r['appearance'], r['condition']): r['accepted'] for r in decisions}
    assert len(actual) == len(decisions) == 140 and actual == expected
    assert all(r['nativeFilter'] == r['initialSnap'] == r['bindingUnregistered'] == 'true' for r in decisions)
    assert all(math.isfinite(float(r['bearingDrift'])) and 0 <= float(r['bearingDrift']) < 1e-4 for r in decisions if r['accepted'] == 'false')
    assert text.count('FD_ORDINARY_MATRIX families=5 appearances=4 accepted=40 rejected=100 traversals=10 registered4D=0') == 1
    traversals = rows(text, 'FD_ORDINARY_TRAVERSAL')
    assert len(traversals) == 10
    assert {(r['family'], r['direction']) for r in traversals} == set(itertools.product(families, ('forward', 'reverse')))
    for r in traversals:
        progress, lateral, vertical = (float(r[k]) for k in ('progress', 'lateral', 'vertical'))
        assert all(math.isfinite(v) for v in (progress, lateral, vertical))
        assert progress > .5 and 0 <= lateral < .2 and 0 <= vertical < .2
        assert r['engaged'] == 'true' and r['registered4D'] == 'false'
    assert 'FD_CONNECTED_MATRIX cases=200 matchingAccepted=40 wrongFamilyRejected=160' in text
    assert 'FD_DELAYED_PASSENGER pendingIdentity=true pendingMass=true delayedReplay=true loadedEmpty=true' in text
    for phase in ('LIFTING', 'OUTBOUND', 'RETURNING', 'CAPTURING'):
        assert f'FD_PAX_PHASE_COMPLETE target={phase}' in text
        assert f'FD_PAX_PHASE_ENERGY target={phase} required={str(profile in ("engineered", "combined")).lower()}' in text
    mods = text.split('Mod List:', 1)[1].split('[', 1)[0]
    for version in ('Minecraft 1.21.1', 'NeoForge 21.1.248', 'Create 6.0.10', 'Sable 2.0.3', 'Coasters Simulated 0.1.5'):
        assert version in mods
    for addon, version in {'styles': 'Track Styles 1.2.0', 'engineered': 'Engineered 1.1.1',
                           'additions': 'Additions 0.2.0', 'magnetized': 'Magnetized 1.1.0',
                           'immersion': 'Immersion 0.2.3'}.items():
        assert (version in mods) == (profile in (addon, 'combined'))
    results[profile] = {'registered_tests': 37, 'absent_addon_skips': skips,
                        'ordinary_access_decisions': 140, 'guided_traversals': 10,
                        'minimum_progress': min(float(r['progress']) for r in traversals),
                        'maximum_vertical_drift': max(float(r['vertical']) for r in traversals),
                        'maximum_rejected_snap_drift': max(float(r['bearingDrift']) for r in decisions if r['accepted'] == 'false')}
for name, reason in {'setup-base': 'Ordinary fixture has no native bearing',
                     'engagement-base': 'Rejected initial snap changed engagement or pose arrow_controlled/0/body_above',
                     'precision-base': 'Rejected initial snap changed engagement or bearing position arrow_controlled/0/body_above disengaged=true'}.items():
    text = log(name)
    assert runs[name]['exit_status'] == 1
    assert reason in text and '1 required tests failed' in text and 'BUILD FAILED' in text
    assert 'FD_ORDINARY_MATRIX ' not in text
cleanup = read('cleanup.json')
assert cleanup['matrix_exit'] == 0
assert cleanup['profile_launchers_stopped'] == {name: True for name in profiles}
artifact = read('artifact.json')
assert artifact['diff'] == {'added': ['dev/fourthdimension/gametest/OrdinaryAccessGameTests.class'], 'removed': [], 'changed': []}
assert artifact['fixture']['compiled_sha256'] == artifact['fixture']['jar_sha256']
assert hashlib.sha256((root / 'fixture.java.txt').read_bytes()).hexdigest() == artifact['fixture']['source_sha256']
unit = 0
for path in (root / 'unit').glob('TEST-*.xml'):
    attrs = ET.parse(path).getroot().attrib
    assert all(int(attrs[k]) == 0 for k in ('failures', 'errors', 'skipped'))
    unit += int(attrs['tests'])
assert unit == 19
print(json.dumps({'scope': 'Bare ordinary bearing: native eligibility/filter/initial-snap/binding across five families and four appearances, unavailable providers, two uncertified bodies, flat forward/reverse travel. Not towing, rendering or every assembly/item entry path.',
                  'profiles': results, 'unit_tests': unit, 'production_classes_unchanged': True,
                  'manifest_artifacts': len(manifest)}, indent=2))
