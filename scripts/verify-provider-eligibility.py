#!/usr/bin/env python3
"""Audit the native appearance/provider eligibility regression across seven profiles."""
import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
runs = json.loads((root / 'runs.json').read_text())
before = (root / 'before-checks.txt').read_text()
assert runs['before']['exit_status'] == 1
assert 'Factory accepted unavailable functional provider missing:functional for ARROW' in before
assert '1 required tests failed' in before and 'BUILD FAILED' in before
profiles = {'base': 6, 'styles': 6, 'engineered': 3, 'combined': 0,
            'additions': 4, 'magnetized': 5, 'immersion': 6}
assert set(runs) == {'before', *('fixed-' + p for p in profiles)}
results = {}
for profile, skips in profiles.items():
    name = 'fixed-' + profile
    text = (root / (name + '-checks.txt')).read_text()
    assert runs[name]['exit_status'] == 0
    assert 'All 33 required tests passed' in text and 'BUILD SUCCESSFUL' in text
    assert text.count('_SKIPPED:') == skips
    family = re.findall(r'FD_FAMILY_MATRIX (.*)', text)
    appearance = re.findall(r'FD_APPEARANCE_MATRIX (.*)', text)
    assert len(family) == len(appearance) == 1
    assert dict(re.findall(r'(\w+)=(\S+)', family[0])) == {
        'decisions': '100', 'factoryRejects': '80',
        'lockedNativeInitialSnapRejects': '80', 'bindingPreserved': 'true'}
    assert dict(re.findall(r'(\w+)=(\S+)', appearance[0])) == {
        'phases': '4', 'families': '5', 'unavailableProviderFactoryRejects': '10',
        'ordinaryFactoryRejects': '5', 'unavailableProviderNativeRejects': '10',
        'ordinaryNativeRejects': '5', 'survivalItemRejects': '95',
        'survivalItemPlacements': '5', 'savedMechanicsPreserved': 'true',
        'stylesAvailable': str(profile in ('styles', 'combined')).lower()}
    assert 'FD_DELAYED_PASSENGER pendingIdentity=true pendingMass=true delayedReplay=true loadedEmpty=true' in text
    for phase in ('LIFTING', 'OUTBOUND', 'RETURNING', 'CAPTURING'):
        assert f'FD_PAX_PHASE_COMPLETE target={phase}' in text
        energy = str(profile in ('engineered', 'combined')).lower()
        assert f'FD_PAX_PHASE_ENERGY target={phase} required={energy}' in text
    mods = (root / (name + '-mods.txt')).read_text()
    for version in ('Minecraft 1.21.1', 'NeoForge 21.1.248', 'Create 6.0.10', 'Sable 2.0.3'):
        assert version in mods
    for addon, version in {'styles': 'Track Styles 1.2.0', 'engineered': 'Engineered 1.1.1',
                           'additions': 'Additions 0.2.0', 'magnetized': 'Magnetized 1.1.0',
                           'immersion': 'Immersion 0.2.3'}.items():
        assert (version in mods) == (profile in (addon, 'combined'))
    results[profile] = {'registered': 33, 'absent_addon_skips': skips,
                        'family_appearance_decisions': 100, 'rejected_survival_placements': 95,
                        'successful_survival_placements': 5}
unit = 0
for path in (root / 'unit').glob('TEST-*.xml'):
    attrs = ET.parse(path).getroot().attrib
    assert all(int(attrs[k]) == 0 for k in ('failures', 'errors', 'skipped'))
    unit += int(attrs['tests'])
assert unit == 19
classes = json.loads((root / 'classes.json').read_text())
assert len(classes) == 3 and all(v['compiled_sha256'] == v['jar_sha256'] for v in classes.values())
control = json.loads((root / 'before-hashes.json').read_text())
assert control['vehicle/VehicleFactory']['class'] == control['baseline_artifact']['vehicle_factory_class_sha256']
assert control['vehicle/VehicleFactory']['class'] != classes['dev/fourthdimension/vehicle/VehicleFactory.class']['compiled_sha256']
print(json.dumps({'scope': 'Native factory, survival item use, locked snap/filter/binding, appearance save/load; not the complete FD901 entry-path or rendering matrix',
                  'negative_control_failed': True, 'profiles': results, 'unit_tests': unit}, indent=2))
