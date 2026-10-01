#!/usr/bin/env python3
"""Audit the bounded dedicated-server benchmark against raw native log samples."""
import gzip
import hashlib
import json
import math
import re
import statistics
import sys
import subprocess
import uuid
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
def read(name):
    p = root/name
    return gzip.decompress(p.read_bytes()).decode() if p.suffix == '.gz' else p.read_text()
def load(name):
    return json.loads(read(name))
manifest = load('manifest.json')
for name, expected in manifest['files'].items():
    assert hashlib.sha256((root/name).read_bytes()).hexdigest() == expected, name
artifact = Path(manifest['artifact']['path'])
if artifact.exists():
    assert hashlib.sha256(artifact.read_bytes()).hexdigest() == manifest['artifact']['sha256']
state = load('status.json')
assert state['status'] == 'complete' and read('run.exit').strip() == '0'
restored = load('settings-restored.json')
assert restored['before'] == restored['after']
for profile,versions in manifest['loaded_mod_versions'].items():
    log=read(profile+'-parked.log.gz')
    actual={mod:version for version,mod in re.findall(r'^\t\t.+ ([^ ]+) \(([^)]+)\)$',log,re.M)}
    assert actual==versions and versions['fourthdimension']=='0.9.0-dev.1'
cleanup=load('cleanup.json.gz')
assert not cleanup['live'] and cleanup['settings_sha256']==restored['before']
records = {}
for run in state['runs']:
    case = run['case']
    log = read(case+'.log.gz')
    assert run['status'] == 'complete' and read(case+'.exit').strip() == '0'
    assert 'BUILD SUCCESSFUL' in log and 'Stopping server' in log
    for line in log.splitlines():
        if 'FD_PERFORMANCE_SAMPLE {' in line:
            d = json.loads(line.split('FD_PERFORMANCE_SAMPLE ',1)[1])
            assert d['label'] not in records
            assert d == load(d['label']+'.json.gz')
            records[d['label']] = d
expected = {f'reference-{n}' for n in range(3)} | {
    f'{profile}-{mode}-{n}' for profile in ('base','combined')
    for mode in ('parked','operating') for n in range(3)}
assert set(records) == expected
profile_state = load('profile-status.json')
assert profile_state['status']=='complete' and read('profile.exit').strip()=='0'
profile_restored = load('profile-settings-restored.json.gz')
assert profile_restored['before']==profile_restored['after']==restored['before']
profiled = {}
for run in profile_state['runs']:
    case = run['case']
    assert run['status']=='complete' and read(case+'.exit').strip()=='0'
    log = read(case+'.log.gz')
    assert 'BUILD SUCCESSFUL' in log
    for line in log.splitlines():
        if 'FD_PERFORMANCE_SAMPLE {' in line:
            d=json.loads(line.split('FD_PERFORMANCE_SAMPLE ',1)[1])
            assert d==load(d['label']+'.json.gz') and d['label'] not in profiled
            profiled[d['label']]=d
assert set(profiled)=={f'profile-{p}-{m}' for p in ('base','combined') for m in ('parked','operating')}
records.update(profiled)
identities = None
forced = set()
result = {'samples':{}, 'scope':'NeoForge server Pre-to-Post elapsed time; no clients; zero-angle Arrow profile'}
def metrics(values):
    values = sorted(values)
    return {'mean_ms':statistics.mean(values)/1e6,
            'p50_ms':values[math.ceil(len(values)*.5)-1]/1e6,
            'p95_ms':values[math.ceil(len(values)*.95)-1]/1e6,
            'p99_ms':values[math.ceil(len(values)*.99)-1]/1e6,
            'max_ms':max(values)/1e6,
            'over_50ms':sum(v>50_000_000 for v in values)}
for label,d in sorted(records.items()):
    reference = label.startswith('reference')
    for key in ('tickNanos','carrierNanos','carrierSteps'):
        assert len(d[key]) == 600 and all(isinstance(v,int) and v>=0 for v in d[key]), (label,key)
    assert all(v>0 for v in d['tickNanos']) and sum(d['carrierSteps'])>0
    assert d['after']['tick'] - d['before']['tick'] in (600,601), label
    assert d['wallNanos']>0
    for scene in (d['before'],d['after']):
        forced.add(scene['forcedChunks'])
        assert scene['rails'] == (10 if reference else 13)
        assert len(scene['cars']) == (0 if reference else 21)
        if reference: continue
        assert scene['phase'] != 'FAULT'
        by_family = {f:[c for c in scene['cars'] if c['family']==f] for f in ('ARROW','FREE_SPIN','PAX')}
        assert {k:len(v) for k,v in by_family.items()} == {'ARROW':14,'FREE_SPIN':3,'PAX':4}
        bodies = [b for c in scene['cars'] for b in c['carriers']]
        riders = [r for b in bodies for r in b['riders']]
        assert len(bodies)==48 and len(riders)==96
        assert all(c['loaded'] and c['restraints'] and not c['fault'] for c in scene['cars'])
        assert all(c['linked']==(7 if c['family']=='ARROW' else 4 if c['family']=='PAX' else 1) for c in scene['cars'])
        assert all(b['loaded'] and len(b['riders'])==2 for b in bodies)
        assert all(r['alive'] and r['mounted'] and r['carrier']==b['id'] for b in bodies for r in b['riders'])
        ids = (frozenset(c['id'] for c in scene['cars']), frozenset(b['id'] for b in bodies),
               frozenset((r['id'],r['seat'],b['id']) for b in bodies for r in b['riders']))
        assert len(ids[0])==21 and len(ids[1])==48 and len(ids[2])==96
        identities = identities or ids
        assert identities == ids, (label,'identity changed')
    assert set(d['carrierSteps'])=={6}, label
    row = metrics(d['tickNanos'])
    row['profiled'] = label.startswith('profile-')
    row.update({'wall_seconds':d['wallNanos']/1e9,'carrier_callback_ms_per_tick':sum(d['carrierNanos'])/600/1e6,
                'physics_callbacks':sum(d['carrierSteps']),'phases':d['phases']})
    if not reference:
        assert sum(d['phases'].values()) == 600 and 'FAULT' not in d['phases']
        row['amortized_callback_us_per_carrier_per_server_tick'] = sum(d['carrierNanos'])/600/48/1e3
        if '-parked-' in label or label.endswith('-parked'):
            assert d['phases'] == {'BOARDING':600}
            assert not d['before']['carriage'] and not d['after']['carriage']
            assert all(c['held'] for c in d['after']['cars'])
        else:
            assert 'LIFTING' in d['phases'] and d['after']['carriage']
            first = {c['id']:c for c in d['before']['cars']}
            distances = [abs(c['z']-first[c['id']]['z']) for c in d['after']['cars'] if c['family']!='PAX']
            assert min(distances)>.01, (label,distances)
            row['non_pax_displacement_blocks'] = {'min':min(distances),'max':max(distances)}
    result['samples'][label] = row
# Reparse native SavedData, rather than trusting only the diagnostic JSON.
def native_uuid(parts):
    return str(uuid.UUID(int=sum((part & 0xffffffff) << (96-32*i) for i,part in enumerate(parts))))
for run in state['runs']+profile_state['runs']:
    saved = json.loads(subprocess.check_output([sys.executable,
        str(Path(__file__).with_name('inspect-foundation-save.py')),
        str(root/(run['case']+'-foundation.dat'))],text=True))['data']
    assert saved['schema']==5 and len(saved['rails'])==13 and len(saved['vehicles'])==21
    assert all(r['valid'] and r['provider']=='fourthdimension:original' for r in saved['rails'])
    assert {native_uuid(v['chassis']) for v in saved['vehicles']} == set(identities[0])
    assert {native_uuid(b['body']) for v in saved['vehicles'] for b in v['carriers']} == set(identities[1])
    assert {(native_uuid(r), native_uuid(b['body'])) for v in saved['vehicles'] for b in v['carriers'] for r in b['occupants']} == {(r,b) for r,seat,b in identities[2]}
    assert all(v['restraints'] and not v['fault'] for v in saved['vehicles'])
assert len(forced)==1, forced
result['scene']={'cars':21,'carriers':48,'riders':96,'forced_chunks':next(iter(forced))}
result['groups']={}
for group in ('reference','base-parked','combined-parked','base-operating','combined-operating'):
    rows=[records[f'{group}-{n}'] for n in range(3)]
    values=[v for row in rows for v in row['tickNanos']]
    result['groups'][group]=metrics(values)
    result['groups'][group]['p95_development_budget_met']=result['groups'][group]['p95_ms']<=50
    result['groups'][group]['window_mean_ms']=[statistics.mean(row['tickNanos'])/1e6 for row in rows]
suites=[ET.parse(p).getroot() for p in (root/'unit-tests').glob('TEST-*.xml')]
assert sum(int(s.attrib['tests']) for s in suites)==19
assert all(int(s.attrib[k])==0 for s in suites for k in ('failures','errors','skipped'))
result['unit_tests']=19
jfr_summary=json.loads(subprocess.check_output([sys.executable,str(Path(__file__).with_name('summarize-benchmark-jfr.py')),str(root)],text=True))
assert jfr_summary==load('jfr-summary.json.gz')
assert set(jfr_summary)=={f'{p}-{m}.jfr' for p in ('base','combined') for m in ('parked','operating')}
result['jfr_recordings']=len(jfr_summary)
print(json.dumps(result,indent=2))
