#!/usr/bin/env python3
"""Check the recorded native PAX ownership scenario, without widening its scope."""
import json
import re
import sys
from pathlib import Path

root = Path(sys.argv[1])
expected = {
    'LIFTING': ('LAUNCH', 'BACKWARD', '100'),
    'FAULT': ('LAUNCH', 'FORWARD', '100'),
    'OUTBOUND': ('STOP', 'FORWARD', '0'),
    'RETURNING': ('TRIM', 'ROLLING', '1'),
    'CAPTURING': ('LAUNCH', 'BACKWARD', '100'),
    'BOARDING': ('LAUNCH', 'FORWARD', '100'),
}
report = {'scope': 'Four PAX cars, sixteen native passengers, compact fixed shuttle route',
          'profiles': {}}
for profile in ('engineered', 'combined'):
    text = (root / (profile + '.txt')).read_text()
    assert 'All 25 required tests passed' in text and 'required tests failed' not in text
    assert text.count('_SKIPPED:') == (3 if profile == 'engineered' else 0)
    assert 'FD_ENGINEERED_PAX_PROBE segments=3 energyRequired=false cars=4' in text
    commands = re.findall(r'FD_ENGINEERED_PAX_COMMAND phase=(\w+) mode=(\w+) heading=(\w+) value=(\d+)', text)
    assert set(p for p, *_ in commands) == set(expected)
    for phase, mode, heading, value in commands:
        assert (mode, heading, value) == expected[phase]
    final = next(l for l in text.splitlines() if 'FD_ENGINEERED_PAX_COMPLETE' in l)
    assert 'cycles=1 observer=true' in final
    sightings = {k: int(v) for k, v in re.findall(r'(\w+)=(\d+)', final.split('sightings=', 1)[1])}
    assert all(sightings.get(p, 0) > 2 for p in expected)
    mods = (root / (profile + '-mods.txt')).read_text()
    assert 'Engineered 1.1.1' in mods
    if profile == 'combined':
        for version in ('Track Styles 1.2.0', 'Additions 0.2.0', 'Magnetized 1.1.0', 'Immersion 0.2.3'):
            assert version in mods
        assert 'FD_ADDITIONS_PAX_PROBE' in text and 'FD_MAGNETIZED_PAX_PROBE' in text
    report['profiles'][profile] = {'commands': len(commands), 'native_presence_ticks': sightings}
base = (root / 'base.txt').read_text()
assert 'All 25 required tests passed' in base and base.count('_SKIPPED:') == 4
assert 'FD_ENGINEERED_PAX_' not in base
assert 'coastersengineered' not in (root / 'base-mods.txt').read_text()
report['base_optional_isolation_passed'] = True
print(json.dumps(report, indent=2))
