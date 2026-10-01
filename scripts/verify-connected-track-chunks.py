#!/usr/bin/env python3
"""Verify the bounded native connected-route chunk interruption evidence."""
import csv
import json
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
runs = json.loads((root / 'runs.json').read_text())
families = ('ARROW', 'ZACSPIN', 'FREE_SPIN', 'ZACSPIN_REVISED', 'PAX')
profiles = {'base': (5, None), 'engineered': (3, 'Engineered 1.1.1'),
            'styles': (5, 'Track Styles 1.2.0'), 'additions': (3, 'Additions 0.2.0'),
            'magnetized': (4, 'Magnetized 1.1.0'), 'immersion': (5, 'Immersion 0.2.3'),
            'combined': (0, None)}
report = {'scope': 'Ten unoccupied connected routes, native save flush, partial eviction and reload',
          'profiles': {}}
for profile, (skips, version) in profiles.items():
    text = (root / (profile + '.txt')).read_text()
    mods = (root / (profile + '-mods.txt')).read_text()
    lines = text.splitlines()
    assert runs[profile]['exit_status'] == 0
    assert 'All 28 required tests passed' in text and 'BUILD SUCCESSFUL' in text
    assert 'required tests failed' not in text and text.count('_SKIPPED:') == skips
    assert lines.count('FD_TRACK_PARTIAL_FLUSH native=true') == 1
    waited = [line for line in lines if line.startswith('FD_TRACK_PARTIAL_WAITED ticks=')]
    assert len(waited) == 1 and 200 <= int(waited[0].split('=')[1]) < 1700
    assert 'FD_TRACK_PARTIAL_PENDING' not in text
    for family in families:
        for banked in ('false', 'true'):
            partial = f'FD_TRACK_PARTIAL family={family} banked={banked} evicted=true valid=true endpoints=1'
            restored = f'FD_TRACK_PARTIAL_RELOADED family={family} banked={banked} fresh=true geometry=true endpoints=2'
            assert lines.count(partial) == 1 and lines.count(restored) == 1
            assert lines.index('FD_TRACK_PARTIAL_FLUSH native=true') < lines.index(partial) < lines.index(restored)
    assert sum(line.startswith('FD_TRACK_PARTIAL family=') for line in lines) == 10
    assert sum(line.startswith('FD_TRACK_PARTIAL_RELOADED family=') for line in lines) == 10
    if version:
        assert version in mods
    if profile == 'combined':
        assert all(v in mods for _, v in profiles.values() if v)
    if profile in ('engineered', 'combined'):
        assert 'FD_ENGINEERED_ENERGY_COMPLETE families=4 cars=5 riders=5' in text
        assert 'FD_ENGINEERED_ENERGY_RESTORED requireEnergy=false' in text
    else:
        assert 'coastersengineered' not in mods and 'FD_ENGINEERED_ENERGY_SKIPPED:' in text
    report['profiles'][profile] = {'registered_tests': 28, 'optional_skips': skips,
                                   'routes_reloaded': 10, 'geometry_sections': 60,
                                   'eviction_observed_at_tick': int(waited[0].split('=')[1])}

for i in range(1, 8):
    text = (root / f'attempt{i}.txt').read_text()
    assert runs[f'attempt{i}']['exit_status'] == 1 and '1 required tests failed' in text
    assert ('Connected end did not unload' in text if i < 7
            else 'Connected route eviction/recovery did not complete' in text)
assert runs['pre_cleanup']['exit_status'] == 0
assert 'All 28 required tests passed' in (root / 'pre_cleanup.txt').read_text()
trace = (root / 'request-trace.txt').read_text()
assert all(s in trace for s in ('FD_PARTIAL_CHUNK_TRACE', 'BezierBankBlend.neighbourCoasterCurveAtAnchor',
                               'BezierBankBlend.orderedAnchorsAlongJointBankChain',
                               'CoasterCartTrackHitCache.openEndRefs', 'Level.getBlockEntity'))
with (root / 'before-fix-anchor-tickets.csv').open() as f:
    rows = list(csv.DictReader(f))
assert len(rows) == 10
assert all('unknown 33' in r['ticket'] for r in rows if int(r['x']) in (20, 23, 26, 29, 32))
with (root / 'after-fix-pending-save.csv').open() as f:
    rows = list(csv.DictReader(f))
assert rows and all(r['ticket'] == 'no_ticket' and r['full_status'] == 'INACCESSIBLE' for r in rows)
assert (root / 'attempt7.txt').read_text().count('ticking=false accessible=false resident=false') == 10
unit_tests = 0
for path in (root / 'junit').glob('TEST-*.xml'):
    suite = ET.parse(path).getroot()
    assert all(int(suite.attrib[k]) == 0 for k in ('failures', 'errors', 'skipped'))
    unit_tests += int(suite.attrib['tests'])
assert unit_tests == 19 and runs['build']['exit_status'] == 0
report['unit_tests_passed'] = unit_tests
report['failed_attempts_preserved'] = 7
print(json.dumps(report, indent=2))
