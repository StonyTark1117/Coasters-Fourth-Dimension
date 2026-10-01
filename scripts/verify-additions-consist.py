#!/usr/bin/env python3
"""Verify bounded Additions consist/fin evidence, without extending its scope."""
import json
import math
import re
import sys
from pathlib import Path

root = Path(sys.argv[1])
report = {'scope': 'Two linked Arrow cars, two native passengers, fixed straight track', 'profiles': {}}
for profile in ('additions', 'combined'):
    text = (root / (profile + '.txt')).read_text()
    assert 'All 25 required tests passed' in text
    assert 'required tests failed' not in text
    assert text.count('_SKIPPED:') == (2 if profile == 'additions' else 0)
    for kind in ('uncoupled 4D car', 'ordinary cart'):
        line = next(l for l in text.splitlines() if 'FD_ADDITIONS_REJECT kind=' + kind in l)
        assert 'available=false' in line and 'exactly one complete 4D consist' in line
    station = next(l for l in text.splitlines() if 'FD_ADDITIONS_CONSIST linked=' in l)
    for value in ('ambiguousRejected=true', 'mixedRejected=true', 'partialRejected=true',
                  'threshold=5', 'cooldown=true', 'continuousDelay=true', 'departureLatch=true',
                  'cars=2', 'occupied=2', 'capacity=8', 'restraintsClosed=true'):
        assert value in station
    results = {}
    for line in text.splitlines():
        if 'FD_ADDITIONS_LINKED_FIN ' not in line:
            continue
        fields = dict(re.findall(r'(\w+)=([^ ]+)', line))
        mode, car = fields['mode'], int(fields['car'])
        speed = float(fields['speed'])
        assert car in (0, 1) and math.isfinite(speed)
        if mode == 'BOOST':
            assert speed > 1 and float(fields['travel']) > .3
        elif mode == 'BRAKE':
            assert abs(speed - 3) < .3
        elif mode == 'REVERSE':
            assert abs(speed + 3) < .3
        elif mode == 'STOP':
            assert 0 <= speed < .15
        else:
            raise AssertionError(mode)
        assert (mode, car) not in results
        results[mode, car] = speed
    assert len(results) == 8
    mods = (root / (profile + '-mods.txt')).read_text()
    assert 'Additions 0.2.0 (coasterfins)' in mods
    if profile == 'combined':
        for version in ('Engineered 1.1.1', 'Track Styles 1.2.0', 'Magnetized 1.1.0', 'Immersion 0.2.3'):
            assert version in mods
    report['profiles'][profile] = {mode: [results[mode, car] for car in (0, 1)]
                                   for mode in ('BOOST', 'BRAKE', 'REVERSE', 'STOP')}
base = (root / 'base.txt').read_text()
assert 'All 25 required tests passed' in base and base.count('_SKIPPED:') == 4
assert 'FD_ADDITIONS_CONSIST_SKIPPED' in base
assert 'FD_ADDITIONS_CONSIST linked=' not in base
assert 'coasterfins' not in (root/'base-mods.txt').read_text()
report['base_optional_isolation_passed'] = True
print(json.dumps(report, indent=2))
