#!/usr/bin/env python3
"""Audit connected native family rejection and its deliberate guard-mutation control."""
import hashlib
import itertools
import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
def read(name): return json.loads((root / name).read_text())
def rows(text):
    return [dict(re.findall(r'(\w+)=(\S+)', line))
            for line in text.splitlines() if 'FD_CONNECTED_FAMILY ' in line]
runs = read('runs.json')
profiles = {'base': 6, 'styles': 6, 'engineered': 3, 'combined': 0,
            'additions': 4, 'magnetized': 5, 'immersion': 6}
assert set(runs) == {'mutant', *profiles}
families = ('arrow_controlled', 'zacspin_legacy', 'sns_free_spin', 'zacspin_revised', 'pax_shuttle')
appearances = ('fourthdimension:family_default', 'coasterssimulatedextratypes:arrow_4d',
               'coasterssimulatedextratypes:arrow_4d_dbl_spine', 'missing:style')
expected = {(car, track, side, appearance): str(car == track).lower()
            for car, track, side, appearance in itertools.product(families, families, ('from', 'to'), appearances)}
results = {}
for profile, skips in profiles.items():
    text = (root / (profile + '-checks.txt')).read_text()
    assert runs[profile]['exit_status'] == 0
    assert 'All 33 required tests passed' in text and 'BUILD SUCCESSFUL' in text
    assert text.count('_SKIPPED:') == skips
    samples = rows(text)
    actual = {(r['car'], r['track'], r['side'], r['appearance']): r['accepted'] for r in samples}
    assert len(samples) == len(actual) == 200 and actual == expected
    assert text.count('FD_CONNECTED_MATRIX cases=200 matchingAccepted=40 wrongFamilyRejected=160 endpoints=2 appearances=4 bindingRestored=true') == 1
    assert 'FD_FAMILY_MATRIX decisions=100 factoryRejects=80 lockedNativeInitialSnapRejects=80 bindingPreserved=true' in text
    assert 'survivalItemRejects=95 survivalItemPlacements=5 savedMechanicsPreserved=true' in text
    assert 'stylesAvailable=' + str(profile in ('styles', 'combined')).lower() in text
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
                       'connected_matching_accepts': 40, 'connected_wrong_family_rejections': 160}
mutant = (root / 'mutant-checks.txt').read_text()
assert runs['mutant']['exit_status'] == 1
assert 'Connected family eligibility mismatch ZACSPIN/ARROW' in mutant
assert '1 required tests failed' in mutant and 'BUILD FAILED' in mutant
control = rows(mutant)
assert len(control) == 10 and all(r['car'] == 'arrow_controlled' for r in control)
assert 'FD_CONNECTED_MATRIX ' not in mutant
mutation, restored = read('mutation.json'), read('restored.json')
assert mutation['original_sha256'] == restored['original_sha256'] == restored['restored_sha256']
assert hashlib.sha256((root / 'production-original.java.txt').read_bytes()).hexdigest() == mutation['original_sha256']
assert hashlib.sha256((root / 'mutation.java.txt').read_bytes()).hexdigest() == mutation['mutant_sha256']
classes = read('classes.json')
assert len(classes) == 3 and all(v['compiled_sha256'] == v['jar_sha256'] for v in classes.values())
for name in ('runtime/EligibilityHooks', 'vehicle/VehicleFactory'):
    hashes = classes['dev/fourthdimension/' + name + '.class']
    assert hashes['compiled_sha256'] == hashes['prior_jar_sha256']
assert (root / 'mutant-class.sha256').read_text().strip() != classes['dev/fourthdimension/runtime/EligibilityHooks.class']['compiled_sha256']
artifact = read('artifact-diff.json')
assert not artifact['added'] and not artifact['removed']
assert artifact['changed'] and all(n.startswith('dev/fourthdimension/gametest/FamilyEligibilityGameTests') for n in artifact['changed'])
unit = 0
for path in (root / 'unit').glob('TEST-*.xml'):
    attrs = ET.parse(path).getroot().attrib
    assert all(int(attrs[k]) == 0 for k in ('failures', 'errors', 'skipped'))
    unit += int(attrs['tests'])
assert unit == 19
print(json.dumps({'scope': 'Stationary native connected family/filter/initial-snap/binding matrix; not dynamic traversal, linked consists or every external material',
                  'guard_mutation_detected': True, 'production_guard_restored': True,
                  'profiles': results, 'unit_tests': unit}, indent=2))
