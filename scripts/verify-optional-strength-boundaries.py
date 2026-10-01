#!/usr/bin/env python3
"""Check actual anchor load/save/update-packet boundary results across native profiles."""
import gzip
import hashlib
import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root=Path(sys.argv[1])
manifest=json.loads((root/'manifest.json').read_text())
for name, expected in manifest['files'].items():
    assert hashlib.sha256((root/name).read_bytes()).hexdigest()==expected,name
result={}
for profile,skips in [('base',7),('additions',5),('magnetized',5)]:
    text=gzip.decompress((root/(profile+'.log.gz')).read_bytes()).decode()
    assert (root/(profile+'.exit')).read_text().strip()=='0'
    assert '35 GAME TESTS COMPLETE' in text and 'All 35 required tests passed' in text and 'BUILD SUCCESSFUL' in text
    assert len(re.findall(r'^FD_\w+_SKIPPED:',text,re.M))==skips
    events=[line for line in text.splitlines() if line.startswith('FD_OPTIONAL_STRENGTH ')]
    if profile=='base':
        assert len(events)==11
        assert re.findall(r'FD_OPTIONAL_STRENGTH retained=(\d+) clientPacket=false',text)==['0','37','100']
        assert 'FD_OPTIONAL_STRENGTH explicitDeletion=true' in events
        assert re.findall(r'FD_OPTIONAL_STRENGTH rejected=(.*?) type=(\d+)',text)==[
            ('-1','3'),('101','3'),('37L','4'),('"37"','8'),('37b','1')]
        assert 'FD_OPTIONAL_STRENGTH unrelatedAnchorUntouched=true' in events
        assert 'FD_OPTIONAL_STRENGTH replacementEmpty=true' in events
        result[profile]={'registered':35,'absent_addon_skips':skips,'retained_values':[0,37,100],
                         'invalid_values_rejected':5,'explicit_deletion':True,'unrelated_anchor_untouched':True,
                         'replacement_empty':True,'inactive_values_in_client_packet':False}
    else:
        expected='com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour' if profile=='additions' else 'net.antopfr.coastersmagnetized.magnet.MagnetScrollValue$1'
        assert events==['FD_OPTIONAL_STRENGTH owner='+expected+' remembered=83 saved=7 currentOwner=true']
        result[profile]={'registered':35,'absent_addon_skips':skips,'owner':expected,'remembered_value':83,'saved_current_value':7}
suites=[ET.parse(p).getroot() for p in (root/'unit-tests').glob('TEST-*.xml')]
assert sum(int(s.attrib['tests']) for s in suites)==19
assert all(int(s.attrib[k])==0 for s in suites for k in ('failures','errors','skipped'))
result['unit_tests']=19
print(json.dumps(result,indent=2))
