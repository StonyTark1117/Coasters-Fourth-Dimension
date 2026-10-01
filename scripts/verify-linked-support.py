#!/usr/bin/env python3
"""Audit the reproduced linked-provider gap and seven-profile support regression."""
import hashlib
import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
def read(name): return json.loads((root / name).read_text())
runs = read('runs.json')
profiles = {'base': 6, 'engineered': 3, 'styles': 6, 'additions': 4,
            'magnetized': 5, 'immersion': 6, 'combined': 0}
assert set(runs) == {'red', *profiles}
red = (root / 'red-checks.txt').read_text()
assert runs['red']['exit_status'] == 1
assert 'FD_LINKED_SUPPORT damaged=0 eligible=true filtered=true drive=[true, true]' in red
assert 'Unavailable partner provider passed linked eligibility or external drive' in red
assert '1 required tests failed' in red and 'BUILD FAILED' in red
results = {}
for profile, skips in profiles.items():
    text = (root / (profile + '-checks.txt')).read_text()
    assert runs[profile]['exit_status'] == 0
    assert 'All 33 required tests passed' in text and 'BUILD SUCCESSFUL' in text
    assert text.count('_SKIPPED:') == skips
    probes = re.findall(r'FD_LINKED_SUPPORT damaged=(\d) eligible=(\w+) filtered=(\w+) drive=\[(\w+), (\w+)\]', text)
    assert probes == [(str(i), 'false', 'false', 'false', 'false') for i in range(2)]
    turn = re.search(r'FD_TRAIN maxTurn=([\d.]+) maxAxleDifference=([\d.]+)', text)
    assert turn and float(turn[1]) > 6 and float(turn[2]) > 1
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
                       'linked_provider_loss_probes': len(probes)}
warnings = read('additional-warning-categories.json')
assert set(warnings) == set(profiles) and not any(warnings.values())
classes, before = read('classes.json'), read('before.json')
for name in ('vehicle/ConsistRules', 'integration/ExternalDrivePolicy'):
    source = (root / (name.split('/')[-1] + '-before.java.txt')).read_bytes()
    assert hashlib.sha256(source).hexdigest() == before[name]['before_source']
    hashes = classes['dev/fourthdimension/' + name + '.class']
    assert hashes['prior_jar_sha256'] == before[name]['before_class']
    assert hashes['compiled_sha256'] == hashes['jar_sha256'] != hashes['prior_jar_sha256']
assert all(v['compiled_sha256'] == v['jar_sha256'] for v in classes.values())
artifact = read('artifact-diff.json')
assert not artifact['added'] and not artifact['removed']
assert set(artifact['changed']) == set(classes)
unit = 0
for path in (root / 'unit').glob('TEST-*.xml'):
    attrs = ET.parse(path).getroot().attrib
    assert all(int(attrs[k]) == 0 for k in ('failures', 'errors', 'skipped'))
    unit += int(attrs['tests'])
assert unit == 19
print(json.dumps({'scope': 'Same-tick saved-provider loss during a native linked Arrow junction traversal; not addon removal or motor-force certification',
                  'pre_fix_failure_reproduced': True, 'profiles': results, 'unit_tests': unit}, indent=2))
