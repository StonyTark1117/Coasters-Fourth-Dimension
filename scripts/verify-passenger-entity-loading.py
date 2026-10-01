#!/usr/bin/env python3
"""Audit the deterministic native entity-load regression; no packaged acceptance claim."""
import json
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
root = Path(sys.argv[1])
runs = json.loads((root / 'runs.json').read_text())
before = (root / 'native-before-native-checks.txt').read_text()
assert runs['native-before-native']['exit_status'] == 1
assert 'Pending entity load erased saved passenger identity' in before
assert '1 required tests failed' in before and 'BUILD FAILED' in before
fixed = (root / 'native-engineered-checks.txt').read_text()
assert runs['native-engineered']['exit_status'] == 0
assert 'All 33 required tests passed' in fixed and 'BUILD SUCCESSFUL' in fixed
assert fixed.count('_SKIPPED:') == 3
assert 'FD_SAVED_PASSENGER nativeReplay=true unknownRejected=true liveUnchanged=true' in fixed
assert 'FD_DELAYED_PASSENGER pendingIdentity=true pendingMass=true delayedReplay=true loadedEmpty=true' in fixed
for phase in ('LIFTING', 'OUTBOUND', 'RETURNING', 'CAPTURING'):
    assert f'FD_PAX_PHASE_COMPLETE target={phase}' in fixed
    assert f'FD_PAX_PHASE_ENERGY target={phase} required=true' in fixed
mods = (root / 'native-engineered-mods.txt').read_text()
for name in ('Minecraft 1.21.1', 'NeoForge 21.1.248', 'Create 6.0.10',
             'Coasters Simulated 0.1.5', 'Sable 2.0.3', 'Engineered 1.1.1'):
    assert name in mods
unit = 0
for path in (root / 'unit').glob('TEST-*.xml'):
    a = ET.parse(path).getroot().attrib
    assert all(int(a[k]) == 0 for k in ('failures', 'errors', 'skipped'))
    unit += int(a['tests'])
assert unit == 19
classes = json.loads((root / 'classes.json').read_text())
assert len(classes) == 3 and all(v['compiled_sha256'] == v['jar_sha256'] for v in classes.values())
print(json.dumps({'scope': 'Native pending entity-load identity/mass boundary',
                  'pre_fix_failure_reproduced': True, 'native_profiles': 1,
                  'registered_tests': 33, 'absent_addon_skips': 3, 'unit_tests': unit,
                  'packaged_acceptance': 'pending'}, indent=2))
