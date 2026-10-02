#!/usr/bin/env python3
"""Audit occupied Magnetized inclines, including the observed lack of a rigid hold."""
import gzip, hashlib, itertools, json, math, re, sys
import xml.etree.ElementTree as ET
from pathlib import Path

root=Path(sys.argv[1])
def read(name): return json.loads((root/name).read_text())
def log(name): return gzip.decompress((root/(name+'.log.gz')).read_bytes()).decode()
def rows(text,prefix):
    return [dict(re.findall(r'(\w+)=(\S+)',line)) for line in text.splitlines() if line.startswith(prefix+' ')]
manifest=read('manifest.json')
for name,digest in manifest.items():
    assert hashlib.sha256((root/name).read_bytes()).hexdigest()==digest,name
runs=read('runs.json')
assert set(runs)=={'first','observation-magnetized','prebatch-magnetized','capacity-combined','magnetized','combined','base'}
families=('ARROW','LEGACY','FREE_SPIN','REVISED')
phases=('release','gravity_rollback','rollback_brake','uphill','uphill_brake',
        'brake_drift_10','brake_drift_20','brake_drift_30','brake_drift_40','capture')
results={}
for profile,skips in {'magnetized':5,'combined':0,'base':8}.items():
    text=log(profile)
    assert runs[profile]['exit_status']==0
    assert hashlib.sha256(text.encode()).hexdigest()==runs[profile]['raw_log_sha256']
    assert 'All 38 required tests passed' in text and 'BUILD SUCCESSFUL' in text
    assert text.count('_SKIPPED:')==skips
    mods=text.split('Mod List:',1)[1].split('[',1)[0]
    assert ('Magnetized 1.1.0' in mods)==(profile!='base')
    for name in ('Minecraft 1.21.1','NeoForge 21.1.248','Create 6.0.10','Sable 2.0.3','Coasters Simulated 0.1.5'): assert name in mods
    for name in ('Engineered 1.1.1','Track Styles 1.2.0','Additions 0.2.0','Immersion 0.2.3'):
        assert (name in mods)==(profile=='combined')
    assert 'FD_ORDINARY_MATRIX families=5 appearances=4 accepted=40 rejected=100 traversals=10 registered4D=0' in text
    for phase in ('LIFTING','OUTBOUND','RETURNING','CAPTURING'):
        assert f'FD_PAX_PHASE_COMPLETE target={phase}' in text
        assert f'FD_PAX_PHASE_ENERGY target={phase} required={str(profile=="combined").lower()}' in text
    samples=rows(text,'FD_MAGNETIZED_SLOPE')
    if profile=='base':
        assert not samples and 'FD_MAGNETIZED_SLOPE_SKIPPED:' in text
        results[profile]={'registered':38,'absent_addon_skips':skips,'slope_test_skipped':True}
        continue
    assert 'FD_MAGNETIZED_SLOPE_CONFIG acceleration=32.0 maxSpeed=128.0 rise=12 run=60 rollbackPercent=10 uphillPercent=30 brakePercent=100' in text
    assert 'FD_MAGNETIZED_SLOPE_COMPLETE families=4 lanes=8 riders=8 phases=10 paxExternalDrive=false' in text
    samples_by_key={(r['family'],r['powered'],r['phase']):r for r in samples}
    assert len(samples)==len(samples_by_key)==80
    assert set(samples_by_key)==set(itertools.product(families,('false','true'),phases))
    for r in samples:
        for key in ('y','z','tangentY','along','speed','brakeApproachSpeed','health'): assert math.isfinite(float(r[key]))
        assert r['rider']==r['restraints']==r['matchingSupport']=='true' and r['fault']=='none'
        assert float(r['health'])==10 and 404<float(r['z'])<456
        assert abs(float(r['tangentY'])-12/math.sqrt(60**2+12**2))<1e-5
        assert r['stopping']==str(r['phase']=='capture').lower()
    ratios=[];brake_drift=[];capture_drift=[];uphill=[]
    for f in families:
        for powered in ('false','true'):
            def get(phase): return samples_by_key[f,powered,phase]
            start,rollback=get('release'),get('gravity_rollback')
            assert float(start['speed'])<1e-6
            offset=float(start['y'])-.2*float(start['z'])
            assert all(abs(float(get(phase)['y'])-.2*float(get(phase)['z'])-offset)<.005 for phase in phases)
            assert float(rollback['z'])<float(start['z'])-.25 and float(rollback['y'])<float(start['y'])-.05
            assert float(rollback['along'])<-.2
            for approach,braked in (('gravity_rollback','rollback_brake'),('uphill','uphill_brake')):
                a,b=get(approach),get(braked)
                assert abs(float(a['speed'])-float(b['brakeApproachSpeed']))<1e-8
                ratio=float(b['speed'])/float(a['speed']);assert 0<=ratio<.1;ratios.append(ratio)
            if powered=='true':
                assert float(get('uphill')['along'])>2.1
                assert float(get('uphill')['z'])>float(get('rollback_brake')['z'])+1
                uphill.append(float(get('uphill')['along']))
            # Readiness is distinct from rigid position retention; preserve the observed creep.
            assert float(get('capture')['speed'])<.08
            brake_drift.append(float(get('uphill_brake')['z'])-float(get('brake_drift_40')['z']))
            capture_drift.append(float(get('brake_drift_40')['z'])-float(get('capture')['z']))
        assert float(samples_by_key[f,'true','gravity_rollback']['along'])<float(samples_by_key[f,'false','gravity_rollback']['along'])-.1
        assert float(samples_by_key[f,'true','uphill']['along'])>float(samples_by_key[f,'false','uphill']['along'])+.2
    assert all(d>.05 for d in brake_drift) and all(d>.05 for d in capture_drift)
    seat=rows(text,'FD_MAGNETIZED_SLOPE_SEAT')
    assert len(seat)==8 and {(r['powered'],r['group']) for r in seat}==set(itertools.product(('false','true'),map(str,range(4))))
    for r in seat:
        assert (int(r['impulses'])>0) if r['group']=='0' else (int(r['impulses'])==0)
        assert float(r['energy'])<=1e-6
    results[profile]={'registered':38,'absent_addon_skips':skips,'occupied_lanes':8,'phase_samples':80,
        'worst_remaining_speed_fraction':max(ratios),'powered_uphill_speed_range':[min(uphill),max(uphill)],
        'brake_downhill_drift_over_40_ticks':[min(brake_drift),max(brake_drift)],
        'capture_downhill_drift_over_180_ticks':[min(capture_drift),max(capture_drift)],'rigid_hold_certified':False}
first=log('first')
assert runs['first']['exit_status']==1 and 'Uphill brake failed LEGACY' in first and '1 required tests failed' in first
observed=log('observation-magnetized')
assert runs['observation-magnetized']['exit_status']==0 and 'All 38 required tests passed' in observed
assert len(rows(observed,'FD_MAGNETIZED_SLOPE'))==80
assert 'riders=8 phases=6' in observed
assert read('observation-audit.json')['exit_status']==1
prebatch=log('prebatch-magnetized')
assert runs['prebatch-magnetized']['exit_status']==0 and 'All 38 required tests passed' in prebatch
capacity=log('capacity-combined')
assert runs['capacity-combined']['exit_status']==1 and 'Vehicle capacity reached' in capacity and '1 required tests failed' in capacity
assert all('fourthdimension_magnetized_slopes:0' in log(p) for p in ('magnetized','combined','base'))
artifact=read('artifact.json')
assert artifact['diff']=={'added':['dev/fourthdimension/integration/magnetized/MagnetizedSlopeCheck$Lane.class','dev/fourthdimension/integration/magnetized/MagnetizedSlopeCheck.class'],
                         'removed':[],'changed':['dev/fourthdimension/gametest/MagnetizedIntegrationGameTests.class']}
assert all(v['compiled_sha256']==v['jar_sha256'] for v in artifact['classes'].values())
for name,digest in artifact['sources'].items(): assert hashlib.sha256((root/name).read_bytes()).hexdigest()==digest
assert read('cleanup.json')=={'profile_launchers_stopped':{'magnetized':True,'combined':True,'base':True},'matrix_exit':0}
unit=0
for path in (root/'unit').glob('TEST-*.xml'):
    attrs=ET.parse(path).getroot().attrib
    assert all(int(attrs[k])==0 for k in ('failures','errors','skipped'))
    unit+=int(attrs['tests'])
assert unit==19
print(json.dumps({'scope':'Occupied single-car 12:60 original inclines; gravity rollback, propulsion, at least 90% braking speed reduction, passive seat magnets and capture readiness. Powered brakes and normal capture do not certify a rigid inclined station hold.',
                  'profiles':results,'unit_tests':unit,'existing_gameplay_entries_unchanged':True,'manifest_artifacts':len(manifest)},indent=2))
