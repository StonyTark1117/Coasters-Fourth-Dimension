#!/usr/bin/env python3
"""Check real chunk eviction, exact equipment persistence, and post-reload operations."""
import csv
import json
import math
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
runs = json.loads((root / 'runs.json').read_text())
families = {'ARROW': (2, 12345), 'ZACSPIN': (1, 12346),
            'FREE_SPIN': (1, 12347), 'ZACSPIN_REVISED': (1, 12348)}
profiles = {'engineered': (3, 'Engineered 1.1.1'), 'combined': (0, None),
            'base': (5, None), 'styles': (5, 'Track Styles 1.2.0'),
            'additions': (3, 'Additions 0.2.0'), 'magnetized': (4, 'Magnetized 1.1.0'),
            'immersion': (5, 'Immersion 0.2.3')}
report = {'scope': 'Unoccupied equipment chunk eviction/reload, followed by five occupied cars on four straight tracks',
          'profiles': {}}
for profile, (skips, version) in profiles.items():
    text = (root / (profile + '.txt')).read_text()
    mods = (root / (profile + '-mods.txt')).read_text()
    assert runs[profile]['exit_status'] == 0 and 'All 27 required tests passed' in text
    assert 'required tests failed' not in text and 'BUILD SUCCESSFUL' in text
    assert text.count('_SKIPPED:') == skips
    assert 'FD_ENDPOINT_AVAILABILITY inactiveRefs=3 ordinaryRefs=2 falseJunction=false' in text
    assert 'FD_ENDPOINT_AVAILABILITY restoredRefs=4 nativeMatch=true' in text
    if version:
        assert version in mods
    if profile == 'combined':
        assert all(v in mods for _, v in profiles.values() if v)
    result = {'registered_tests': 27, 'optional_skips': skips, 'endpoint_topology_passed': True}
    if profile not in ('engineered', 'combined'):
        assert 'coastersengineered' not in mods and 'FD_ENGINEERED_ENERGY_SKIPPED:' in text
        assert 'FD_ENGINEERED_ENERGY_RELOADED' not in text
    else:
        result['families'] = {}
        for family, (cars, stored) in families.items():
            markers = [f'FD_ENGINEERED_ENERGY_UNLOAD family={family} stored={stored}',
                       f'FD_ENGINEERED_ENERGY_EVICTED family={family}',
                       f'FD_ENGINEERED_ENERGY_RELOADED family={family} stored={stored} span=true controller=true fresh=true',
                       f'FD_ENGINEERED_ENERGY_HELD family={family} cars={cars} stored=100000',
                       f'FD_ENGINEERED_ENERGY_STARVED family={family} cars={cars}']
            lines = text.splitlines()
            assert all(lines.count(marker) == 1 for marker in markers)
            positions = [lines.index(marker) for marker in markers]
            assert positions == sorted(positions)
            measurements = {}
            for phase in ('FORWARD', 'BRAKE', 'REVERSE', 'REVERSE_BRAKE'):
                rows = re.findall(r'FD_ENGINEERED_ENERGY_' + phase + ' family=' + family
                                  + r' stored=(\d+) speeds=\[([^\]]+)\]', text)
                assert len(rows) == 1
                fe, speeds = rows[0]
                speeds = [float(v) for v in speeds.split(',')]
                assert 0 < int(fe) < 100000 and len(speeds) == cars and all(map(math.isfinite, speeds))
                assert all(v > 1 if phase == 'FORWARD' else v < -1 if phase == 'REVERSE'
                           else abs(v) < 1.3 for v in speeds)
                measurements[phase] = {'stored_fe': int(fe), 'speeds': speeds}
            result['families'][family] = {'persisted_fe': stored, 'measurements': measurements}
        assert 'FD_ENGINEERED_ENERGY_COMPLETE families=4 cars=5 riders=5' in text
        assert 'FD_ENGINEERED_ENERGY_RESTORED requireEnergy=false' in text
        assert 'FD_ENGINEERED_PAX_PROBE segments=3 energyRequired=false cars=4' in text
    report['profiles'][profile] = result

for attempt in ('attempt1', 'attempt2', 'attempt3', 'attempt4', 'attempt5', 'attempt6'):
    text = (root / (attempt + '.txt')).read_text()
    assert runs[attempt]['exit_status'] == 1
    assert 'Energy equipment chunk did not unload' in text and '1 required tests failed' in text
    assert 'FD_ENGINEERED_ENERGY_RESTORED requireEnergy=false' in text
trace = (root / 'request-trace.txt').read_text()
assert all(s in trace for s in ('FD_CHUNK_REQUEST_TRACE', 'BezierBankBlend.jointBankRadiansAt',
                               'CoasterCartTrackHitCache.openEndRefs', 'Level.getBlockEntity'))
trace = (root / 'additions-request-trace.txt').read_text()
assert all(s in trace for s in ('FD_CHUNK_REQUEST_TRACE', 'AnchorSpanBehaviour.getSignal',
                               'AnchorSpanBehaviour.tickRaise', 'FinForceSpanHandler.apply'))
with (root / 'anchor-tickets.csv').open() as f:
    rows = list(csv.DictReader(f))
assert len(rows) == 8 and all('unknown 33' in r['ticket'] for r in rows)
tests = 0
for path in (root / 'junit').glob('TEST-*.xml'):
    suite = ET.parse(path).getroot()
    assert all(int(suite.attrib[k]) == 0 for k in ('failures', 'errors', 'skipped'))
    tests += int(suite.attrib['tests'])
assert tests == 19
report['unit_tests_passed'] = tests
report['failed_unload_runs_preserved'] = 6
print(json.dumps(report, indent=2))
