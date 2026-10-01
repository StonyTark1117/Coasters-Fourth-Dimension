#!/usr/bin/env python3
"""Verify native PAX lift eviction, fault hold, explicit resume and addon ownership."""
import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
runs = json.loads((root / 'runs.json').read_text())
summary = {}
for profile, skips in {'base': 5, 'engineered': 3, 'combined': 0}.items():
    text = (root / (profile + '-checks.txt')).read_text()
    assert runs[profile]['exit_status'] == 0
    assert 'All 29 required tests passed' in text and 'BUILD SUCCESSFUL' in text
    assert 'required tests failed' not in text and text.count('_SKIPPED:') == skips
    assert 'FD_PAX_EVICTION_RELEASE phase=LIFTING bodies=13 riders=16' in text
    match = re.search(r'FD_PAX_EVICTED tick=(\d+) blocks=(\d+) bodies=13 occupants=16 phase=FAULT interrupted=LIFTING', text)
    assert match and 400 <= int(match[1]) < 8000
    assert int(match[2]) == (6 if profile == 'base' else 8)
    assert 'FD_PAX_RESTORED fresh=true riders=16 phase=FAULT interrupted=LIFTING' in text
    drift = re.search(r'FD_PAX_RESUMED holdTicks=60 drift=([\d.Ee+-]+) phase=LIFTING', text)
    assert drift and 0 <= float(drift[1]) < .3
    assert 'FD_PAX_EVICTION_COMPLETE cycles=1 cars=4 riders=16 alive=true unloaded=true phases=[BOARDING, LIFTING, OUTBOUND, RETURNING, CAPTURING]' in text
    assert 'FD_SAVED_PASSENGER nativeReplay=true unknownRejected=true liveUnchanged=true' in text
    assert sum(l.startswith('FD_TRACK_PARTIAL_RELOADED family=') for l in text.splitlines()) == 10
    result = {'evicted_tick': int(match[1]), 'block_entities': int(match[2]),
              'native_bodies': 13, 'riders': 16, 'hold_drift_blocks': float(drift[1])}
    if profile != 'base':
        assert 'energyRequired=false cars=4' in text
        expected = {'LIFTING': ('LAUNCH', 'BACKWARD', '100'), 'FAULT': ('LAUNCH', 'FORWARD', '100'),
                    'OUTBOUND': ('STOP', 'FORWARD', '0'), 'RETURNING': ('TRIM', 'ROLLING', '1'),
                    'CAPTURING': ('LAUNCH', 'BACKWARD', '100'), 'BOARDING': ('LAUNCH', 'FORWARD', '100')}
        for phase, (mode, heading, value) in expected.items():
            assert f'FD_ENGINEERED_PAX_RELOAD_COMMAND phase={phase} mode={mode} heading={heading} value={value}' in text
        seen = re.search(r'FD_ENGINEERED_PAX_RELOAD_COMPLETE cycles=1 observer=true sightings=\{([^}]+)\}', text)
        assert seen
        counts = {k: int(v) for k, v in re.findall(r'([A-Z]+)=(\d+)', seen[1])}
        assert set(counts) == set(expected) and all(v > 2 for v in counts.values())
        result['controller_presence_ticks'] = counts
        # The original occupied power-loss check remains green after sharing the helper.
        assert 'FD_ENGINEERED_PAX_COMPLETE cycles=1 observer=true' in text
        assert 'FD_ENGINEERED_ENERGY_COMPLETE families=4 cars=5 riders=5' in text
    mods = (root / (profile + '-mods.txt')).read_text()
    assert ('Engineered 1.1.1' in mods) == (profile != 'base')
    for name in ('Track Styles 1.2.0', 'Additions 0.2.0', 'Magnetized 1.1.0', 'Immersion 0.2.3'):
        assert (name in mods) == (profile == 'combined')
    summary[profile] = result

assert runs['attempt1']['exit_status'] == runs['attempt2']['exit_status'] == 1
assert 'Expected four PAX chassis, four carriers and one carriage' in (root / 'attempt1-checks.txt').read_text()
assert 'PAX fixture never physically evicted' in (root / 'attempt2-checks.txt').read_text()
assert runs['exploratory']['exit_status'] == 0
assert 'FD_PAX_EVICTION_COMPLETE' in (root / 'exploratory-checks.txt').read_text()
tests = 0
for path in (root / 'unit').glob('TEST-*.xml'):
    data = ET.parse(path).getroot().attrib
    assert all(int(data[k]) == 0 for k in ('failures', 'errors', 'skipped'))
    tests += int(data['tests'])
assert tests == 19
print(json.dumps({'scope': 'Occupied PAX lift eviction and explicit recovery; native pigs; default Engineered energy mode',
                  'profiles': summary, 'registered_tests': 29, 'unit_tests': tests}, indent=2))
