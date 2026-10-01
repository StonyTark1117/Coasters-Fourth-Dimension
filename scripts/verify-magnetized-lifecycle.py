#!/usr/bin/env python3
"""Audit real occupied saves across Magnetized removal, before and after strength retention."""
import copy
import gzip
import hashlib
import json
import re
import sys
import subprocess
import uuid
import tempfile
import zipfile
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
def data(name):
    return json.loads((root/name).read_text())
def log(name):
    return gzip.decompress((root/name).read_bytes()).decode()
def uid(words):
    return str(uuid.UUID(int=sum((w & 0xffffffff) << (96-32*i) for i,w in enumerate(words))))
def stable(value, unloaded=False):
    result = copy.deepcopy(value)
    result['vehicles'].sort(key=lambda v:str(v['chassis']))
    for v in result['vehicles']:
        if unloaded:
            assert v['restraints'] == 0 and v['revision'] == 2
            v['restraints'] = 1
            v['revision'] = 1
        for c in v['carriers']:
            c.pop('angle'); c.pop('omega')
            if unloaded:
                assert not c['occupants']
                original = next(b for a in baseline['vehicles'] for b in a['carriers'] if b['body']==c['body'])
                c['occupants'] = original['occupants']
    return result

manifest = data('manifest.json')
for name, expected in manifest['files'].items():
    assert hashlib.sha256((root/name).read_bytes()).hexdigest() == expected, name
baseline = data('before/seed-foundation.json')['data']
assert baseline['schema'] == 5 and len(baseline['rails']) == len(baseline['vehicles']) == 5
assert {v['family'] for v in baseline['vehicles']} == {'arrow_controlled','zacspin_legacy','sns_free_spin','zacspin_revised','pax_shuttle'}
assert sum(len(v['carriers']) for v in baseline['vehicles']) == 14
assert sum(bool(r['magnets']) for r in baseline['rails']) == 1
modes = data('before/seed-modes.json')
assert len(modes['data']['Anchors']) == 10
native = data('before/seed-entities.json')
assert len(native)==5 and all(r['health']==10 and r['parent_type']=='create:seat' and r['parent_id'] for r in native)
seats = {r['id']:r['parent_id'] for r in native}
mounts = {uid(c['body']):sorted(uid(r) for r in c['occupants']) for v in baseline['vehicles'] for c in v['carriers']}
cases = ['seed','control','removed','reinstalled','combined','combined-removed','combined-reinstalled']
result = {}
for version in ('before','fixed'):
    results = {}
    for case in cases:
        folder = version+'/'+case
        raw = log(folder+'.log.gz')
        assert (root/(folder+'.exit')).read_text().strip()=='0' and 'BUILD SUCCESSFUL' in raw
        reports = {r['label']:r for line in raw.splitlines() if 'FD_EQUIPMENT_LIFECYCLE {' in line
                   for r in [json.loads(line.split('FD_EQUIPMENT_LIFECYCLE ',1)[1])]}
        label = 'occupied' if case=='seed' else 'restored'
        snap = data(folder+'-'+label+'.json')
        assert snap==reports[label]
        installed = case not in ('removed','combined-removed')
        assert snap['magnetized']==installed
        assert ('Coasters: Magnetized 1.1.0 (coastersmagnetized)' in raw)==installed
        if not installed: assert 'coastersmagnetized (version 1.1.0 -> MISSING)' in raw
        assert len(snap['anchors'])==10 and len(snap['cars'])==5
        assert {c['chassis'] for c in snap['cars']}=={uid(v['chassis']) for v in baseline['vehicles']}
        actual_mounts = {}
        for c in snap['cars']:
            assert c['ready'] and c['restraints'] and c['stopping'] and not c['fault'] and not c['externalDriveAllowed']
            for b in c['carriers']:
                assert b['loaded'] and b['capture']
                actual_mounts[b['body']] = sorted(r['id'] for r in b['riders'])
                assert all(r['alive'] and r['mounted'] and r['carrier']==b['body'] for r in b['riders'])
        assert actual_mounts==mounts
        strengths = []
        for a in snap['anchors']:
            assert a['powered'] and a['nbt']
            brake = a['pos'].endswith('-140')
            expected = 63 if brake else 37
            if version=='before' and case=='reinstalled':expected=0
            values=re.findall(r'(?<!\w)ScrollValue:(-?\d+)',a['nbt'])
            if version=='before' and case=='removed':assert not values
            else:assert values==[str(expected)],(version,case,a)
            if installed:
                assert a['equipment']=={'mode':'BRAKE' if brake else 'MAGNET','percent':expected}
            else:assert 'equipment' not in a
            strengths.append(expected if values else None)
        unloaded = 'reinstalled' in case
        assert stable(data(folder+'-foundation.json')['data'],unloaded)==stable(baseline),(version,case)
        assert data(folder+'-modes.json')==modes,(version,case)
        for short, full in [('foundation','fourthdimension_foundation'),('modes','coastersmagnetized_magnetized_anchors')]:
            decoded=json.loads(subprocess.check_output([sys.executable,'scripts/inspect-foundation-save.py',str(root/(folder+'-'+full+'.dat'))],text=True))
            assert decoded==data(folder+'-'+short+'.json')
        entities=data(folder+'-entities.json')
        with tempfile.TemporaryDirectory(prefix='fd-magnetized-audit-') as temporary:
            saved=Path(temporary)/'entities';saved.mkdir()
            with zipfile.ZipFile(root/(folder+'-entity-regions.zip')) as archive:
                for name in archive.namelist():
                    assert Path(name).name==name and name.endswith(('.mca','.mcc'))
                    (saved/name).write_bytes(archive.read(name))
            decoded=json.loads(subprocess.check_output([sys.executable,str(root/version/'inspect-entities.py'),temporary],text=True))
            assert decoded==entities
        assert {e['id'] for e in entities}==set(seats) and all(e['health']==10 for e in entities)
        if unloaded:
            u=data(folder+'-unloaded.json');assert u==reports['unloaded']
            assert all(not c['restraints'] and c['ready'] and c['stopping'] and not c['fault'] for c in u['cars'])
            assert all(not b['riders'] for c in u['cars'] for b in c['carriers'])
            assert all(e['parent_type'] is None and e['parent_id'] is None for e in entities)
        else:assert {e['id']:e['parent_id'] for e in entities}==seats and all(e['parent_type']=='create:seat' for e in entities)
        results[case]={'occupied_restored':5,'carriers':14,'mode_records':10,'saved_strengths':strengths,'normal_unload':unloaded}
    restored=data(version+'/settings-restored.json');assert restored['before']==restored['after']
    result[version]=results
native_results={}
for profile,skips in [('base',7),('magnetized',5),('combined',0)]:
    text=log('native-'+profile+'.log.gz')
    assert (root/('native-'+profile+'.exit')).read_text().strip()=='0'
    assert '34 GAME TESTS COMPLETE' in text and 'All 34 required tests passed' in text and 'BUILD SUCCESSFUL' in text
    assert len(re.findall(r'^FD_\w+_SKIPPED:',text,re.M))==skips
    native_results[profile]={'registered':34,'absent_addon_skips':skips}
result['native_regressions']=native_results
suites=[ET.parse(p).getroot() for p in (root/'unit-tests').glob('TEST-*.xml')]
assert sum(int(s.attrib['tests']) for s in suites)==19
assert all(int(s.attrib[k])==0 for s in suites for k in ('failures','errors','skipped'))
result['unit_tests']=19
print(json.dumps(result,indent=2))
