#!/usr/bin/env python3
"""Audit recorded native Magnetized coupled-train checks and regression profiles."""
import gzip
import hashlib
import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
manifest = json.loads((root/'manifest.json').read_text())
for name, expected in manifest['files'].items():
    assert hashlib.sha256((root/name).read_bytes()).hexdigest() == expected, name
result = {}
for profile, skips in [('magnetized',5),('combined',0),('base',7)]:
    log = gzip.decompress((root/(profile+'.log.gz')).read_bytes()).decode()
    assert (root/(profile+'.exit')).read_text().strip() == '0'
    assert '34 GAME TESTS COMPLETE' in log and 'All 34 required tests passed' in log and 'BUILD SUCCESSFUL' in log
    assert len(re.findall(r'^FD_\w+_SKIPPED:',log,re.M)) == skips
    if profile == 'base':
        assert 'FD_MAGNETIZED_LINKED_SKIPPED' in log
        result[profile] = {'registered':34,'absent_addon_skips':skips}
        continue
    guards = re.findall(r'FD_MAGNETIZED_LINKED_PEER_GUARD invalid=(\d) unchanged=true healthyDelta=([\d.Ee+-]+)',log)
    assert {int(i) for i,d in guards} == {0,1,2,3} and len(guards) == 4
    assert all(float(d)>.001 for i,d in guards)
    measures = {}
    for phase, sign in [('FORWARD',1),('ROLLBACK',-1),('BRAKE',0)]:
        values = [float(v) for v in re.findall(r'FD_MAGNETIZED_LINKED_'+phase+r' family=ARROW speed=([\d.Ee+-]+)',log)]
        assert len(values) == 2, (profile,phase,values)
        assert all(v*sign>1.1 if sign else 0<=v<.15 for v in values), values
        measures[phase.lower()] = values
    reverse = re.findall(r'FD_MAGNETIZED_LINKED_REVERSE_BRAKE car=(\d) speed=([\d.Ee+-]+)',log)
    assert {int(i) for i,v in reverse} == {0,1} and len(reverse) == 2
    assert all(0<=float(v)<.15 for i,v in reverse)
    captures = re.findall(r'FD_MAGNETIZED_LINKED_CAPTURE car=(\d) ready=true rider=true',log)
    assert sorted(captures) == ['0','1']
    # The pre-existing independent seat-magnet and PAX ownership checks still execute.
    assert 'FD_MAGNETIZED_FORWARD family=PAX' in log and 'FD_MAGNETIZED_ROLLBACK family=PAX' in log
    assert len(re.findall(r'FD_MAGNETIZED_SEAT group=',log)) == 4
    result[profile] = {'registered':34,'absent_addon_skips':skips,'peer_guard_cases':4,
                       'healthy_impulses':[float(d) for i,d in guards],'native_speeds':measures,
                       'reverse_braked_speeds':[float(v) for i,v in reverse],'recaptured_occupied_cars':2}
suites = [ET.parse(p).getroot() for p in (root/'unit-tests').glob('TEST-*.xml')]
assert sum(int(s.attrib['tests']) for s in suites) == 19
assert all(int(s.attrib[k]) == 0 for s in suites for k in ('failures','errors','skipped'))
result['unit_tests'] = 19
print(json.dumps(result,indent=2))
