#!/usr/bin/env python3
"""Verify a bounded FAIL record; this is not a repair/lifecycle acceptance pass."""
import gzip, hashlib, json
from pathlib import Path
root=Path(__file__).resolve().parent
read=lambda name:json.loads((root/name).read_text())
rows=[json.loads(s) for s in (root/'observations.jsonl').read_text().splitlines()]
geo=read('decoded-controller-geometry.json')
assert len(rows)==len(geo)==8
assert [r['case'] for r in rows]==['pax-initial','pax-board-attempt','pax-board-lower-attempt','pax-occupied-closed','pax-support-removal-attempt','pax-restored-inert','pax-after-repair','pax-repair-geometry-blocked-final']
artifact=read('artifact.json')
assert artifact['sourceRevision']=='311078e8229e54514b1e3e0cbb4837bf87066ce5'
assert artifact['jarSha256']=='dc60cbdefe35cf2737573e57b1509f790cf82342bea6a81764a49066b2b72fd6'
input_world=read('input-world.json')
assert input_world['sourceFoundationSha256']==input_world['copyFoundationSha256']
pax=lambda r:[v for v in r['savedVehicles'] if v['family']=='pax_shuttle']
identity=lambda vs:[(v['chassis'],[(c['body'],c['seats']) for c in v['carriers']]) for v in vs]
baseline=identity(pax(rows[0]));assert len(baseline)==4
mapping=lambda vs:[(v['chassis'],c['body'],s) for v in vs for c in v['carriers'] for s in c['rider_seats']]
expected=mapping(pax(rows[2]));assert len(expected)==1
assert expected[0][0]==[859674760,2032226056,-1909381182,299761874]
assert expected[0][2]=={'seat':274877911039,'player':[940439953,-167562164,-1601161573,-1389718966]}
rail_signatures=lambda rs:sorted((r['from'],r['to'],r['geometry_hash'],json.dumps(r['profile'],sort_keys=True)) for r in rs if r['family']=='pax_shuttle')
for i,r in enumerate(rows):
    cars=pax(r);assert identity(cars)==baseline
    assert rail_signatures(r['savedRails'])==rail_signatures(rows[0]['savedRails'])
    assert mapping(cars)==([] if i<2 else expected)
    assert ('RootVehicle' in r['players']['Dev'])==(i>=2)
    assert r['availability'].endswith('assignments='+('0' if i<2 else '1'))
    for v in cars:
        assert v['restraints']==(0 if i<3 else 1)
        assert v['alignment_required']==(0 if i<4 else 1)
        assert v['fault']==('' if i<4 else 'Matching track support lost')
        assert v['stopping'] and all(c['capture'] for c in v['carriers'])
    w=geo[i]['winch']
    for k in ('shuttle_vehicle','shuttle_carriage','shuttle_cars','shuttle_start','shuttle_end'):
        assert w[k]==geo[0]['winch'][k]
    assert w['shuttle_phase']=='BOARDING' and w['shuttle_fault']==''
    assert geo[i]['case']==r['case']
A=140737488621653;B=140737488752725
connection=lambda row,key,peer:next((c for c in row[key]['AnchorPeerCurves'] if c['Peer']==peer),None)
before=connection(geo[0],'paxAnchorA',B)
after=connection(geo[5],'paxAnchorA',B)
assert before and after
assert connection(geo[4],'paxAnchorA',B) is None
assert connection(geo[4],'paxAnchorB',A) is None
assert before['Bezier']['Axes']==[{'V':[0.0,0.0,1.0]},{'V':[0.0,0.0,-1.0]}]
assert before['Bezier']['Axes']!=after['Bezier']['Axes']
assert before['Bezier']['Normals']!=after['Bezier']['Normals']
for k in ('Starts','Positions','Material','Girder','Primary'):assert before['Bezier'][k]==after['Bezier'][k]
for k in ('HHi','HLo'):assert before[k]==after[k]
for key,peer in [('paxAnchorA',B),('paxAnchorB',A)]:
    other=lambda row:[c for c in row[key]['AnchorPeerCurves'] if c['Peer']!=peer]
    assert all(other(r)==other(geo[0]) for r in geo)
assert rows[5]['savedRails']==rows[6]['savedRails']==rows[7]['savedRails']
for pre,post in zip(pax(rows[5]),pax(rows[6])):
    for key in ('revision','fault','alignment_required','restraints','boundFrom','boundTo'):assert pre[key]==post[key]
stopped=read('stopped-foundation.json')['data']
vs=[v for v in stopped['vehicles'] if v['family']=='pax_shuttle']
assert identity(vs)==baseline and mapping(vs)==expected
assert all(v['alignment_required'] and v['restraints'] and v['fault']=='Matching track support lost' for v in vs)
assert rail_signatures(stopped['rails'])==rail_signatures(rows[0]['savedRails'])
shutdown=read('shutdown-state.json')
for role in ('server','dev','observer'):
    assert shutdown[role]['exitCode']==0
    log=gzip.decompress((root/(role+'.log.gz')).read_bytes()).decode()
    assert 'BUILD SUCCESSFUL' in log
    assert ('All dimensions are saved' if role=='server' else 'Stopping!') in log
assert shutdown['xvfb']['zeroChildWindowsBeforeStop']
manifest=read('manifest.json')
assert set(manifest)=={p.name for p in root.iterdir() if p.is_file() and p.name!='manifest.json'}
for name,sha in manifest.items():assert hashlib.sha256((root/name).read_bytes()).hexdigest()==sha,name
print(f'PASS evidence verification: {len(rows)} observations, original PAX identities retained, changed geometry rejected, {len(manifest)} hashes. Runtime acceptance remains FAILED; lifecycle not reached.')
