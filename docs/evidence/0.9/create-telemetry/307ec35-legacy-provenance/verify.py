#!/usr/bin/env python3
"""Check recorded legacy backfill evidence; no live runtime or broad release claim."""
from pathlib import Path
import json,gzip,hashlib
root=Path(__file__).resolve().parent
read=lambda n:json.loads((root/n).read_text())
rows=[json.loads(x) for x in (root/'observations.jsonl').read_text().splitlines()]
assert len(rows)==4
geo=read('decoded-observations.json');assert len(geo)==4
artifact=read('artifact.json')
assert artifact['sourceRevision']=='307ec35ee4039c9669515cfb3ae4901868b1a000'
assert artifact['jarSha256']=='5de077e3d76e5e81184c6af316d4c7a6083f99bf5367d5319fd697a96b3fb598'
assert artifact['profile']=='standalone' and artifact['schema']==6 and artifact['protocol']==7
assert not artifact['clientsLaunched']
rail_map=lambda rs:{tuple(r['id']):r for r in rs}
vehicle_map=lambda vs:{tuple(v['chassis']):v for v in vs}
winch_state=lambda w:{'shuttle_pending_retention':0,**{k:v for k,v in w.items() if k.startswith('shuttle_')}}
reservation=lambda vs:[(v['chassis'],c['body'],s) for v in vs for c in v['carriers'] for s in c['rider_seats']]
for case,count in [('intact',5),('changed_support',2)]:
 baseline=read(case+'-baseline.json');before=read(case+'-before-foundation.json')['data'];after=read(case+'-after-foundation.json')['data']
 assert baseline['foundation']['data']==before
 assert baseline['foundationSha256']==baseline['copyFoundationSha256']==hashlib.sha256((root/(case+'-before-foundation.dat')).read_bytes()).hexdigest()
 assert before['schema']==after['schema']==6
 assert len(before['rails'])==len(after['rails'])==6
 assert len(before['vehicles'])==len(after['vehicles'])==6
 assert sum(len(v['carriers']) for v in before['vehicles'])==12
 assert all('original_curve' not in r for r in before['rails'])
 br=rail_map(before['rails']);bv=vehicle_map(before['vehicles'])
 samples=[r for r in rows if r['case']==case]
 assert [r['label'] for r in samples]==['loaded-save','stable-save']
 assert int(samples[1]['gameTime'].split()[-1])>int(samples[0]['gameTime'].split()[-1])
 expected_reservation=reservation(before['vehicles'])
 assert len(expected_reservation)==(0 if case=='intact' else 1)
 for state in [r['foundation']['data'] for r in samples]+[after]:
  ar=rail_map(state['rails']);assert ar.keys()==br.keys()
  assert sum('original_curve' in r for r in ar.values())==count
  for rid,r in ar.items():
   orig=br[rid]
   assert {k:v for k,v in r.items() if k!='original_curve'}==orig
   if orig['valid']:
    assert 'original_curve' in r
    anchor=baseline['anchors'][str(r['from'])];curve=next(c for c in anchor['AnchorPeerCurves'] if c['Peer']==r['to'])['Bezier']
    assert r['original_curve']==curve
   else:assert not r['valid'] and 'original_curve' not in r
  av=vehicle_map(state['vehicles']);assert av.keys()==bv.keys()
  assert reservation(state['vehicles'])==expected_reservation
  for vid,v in av.items():
   orig=bv[vid]
   assert {k:x for k,x in v.items() if k!='carriers'}=={k:x for k,x in orig.items() if k!='carriers'}
   assert len(v['carriers'])==len(orig['carriers'])
   for c,old in zip(v['carriers'],orig['carriers']):
    omit=('angle','omega','occupants')
    assert {k:x for k,x in c.items() if k not in omit}=={k:x for k,x in old.items() if k not in omit}
 for r in samples:
  assert r['onlinePlayers'].startswith('There are 0 of a max of ')
  assert r['availability']==f'Validation availability chassis=6/6 carriers=12/12 assignments={len(expected_reservation)}'
 for d in [g for g in geo if g['case']==case]:
  for key,anchor in baseline['anchors'].items():
   if anchor is None:assert 'unavailable' in d['anchors'][key]
   else:
    for field in ('AnchorPeerCurves','JointBank'):
     assert d['anchors'][key].get(field)==anchor.get(field)
  before_w=read(case+'-before-winch.json')
  assert winch_state(d['winch'])==winch_state(before_w)
 before_w=read(case+'-before-winch.json');after_w=read(case+'-after-winch.json')
 assert winch_state(before_w)==winch_state(after_w)
 if case=='changed_support':
  assert 'shuttle_pending_retention' not in before_w and after_w['shuttle_pending_retention']==0
 assert read(case+'-before-players.json')==read(case+'-after-players.json')
 if case=='changed_support':
  pax=[r for r in after['rails'] if r['family']=='pax_shuttle'];assert len(pax)==3
  assert all(not r['valid'] and 'original_curve' not in r for r in pax)
  assert all(v['fault']=='Matching track support lost' and v['alignment_required'] and v['restraints'] and v['stopping'] for v in after['vehicles'] if v['family']=='pax_shuttle')
  assert not any(c['occupants'] for v in after['vehicles'] for c in v['carriers'])
 shutdown=read('shutdown.json');assert shutdown[case]['exitCode']==0
 log=gzip.decompress((root/(case+'.log.gz')).read_bytes()).decode()
 assert 'BUILD SUCCESSFUL' in log and 'All dimensions are saved' in log and 'Stopping server' in log
assert not read('shutdown.json')['clientsLaunched'] and not read('shutdown.json')['displayLaunched']
manifest=read('manifest.json')
assert set(manifest)=={p.name for p in root.iterdir() if p.is_file() and p.name!='manifest.json'}
for name,sha in manifest.items():assert hashlib.sha256((root/name).read_bytes()).hexdigest()==sha,name
print(f'PASS bounded historical backfill: 2 actual old worlds, 4 live observations, 5/2 exact snapshots, invalid support unadopted, identities/profiles/reservations preserved, clean exits; {len(manifest)} hashes. No client/repair/operation acceptance claimed.')
