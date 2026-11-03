#!/usr/bin/env python3
"""Verify captured active-LIFTING restart evidence, not the entire release gate."""
import gzip, hashlib, json, re
from pathlib import Path
root=Path(__file__).resolve().parent
read=lambda n:json.loads((root/n).read_text())
rows=[json.loads(x) for x in (root/'observations.jsonl').read_text().splitlines()]
assert len(rows)==12
by={r['case']:r for r in rows}
decoded=read('decoded-observations.json');assert len(decoded)==len(rows)
geo={r['case']:r for r in decoded}
pax=lambda r:[v for v in r['savedVehicles'] if v['family']=='pax_shuttle']
identity=lambda vs:[(v['chassis'],[(c['body'],c['seats']) for c in v['carriers']]) for v in vs]
mapping=lambda vs:[(v['chassis'],c['body'],s) for v in vs for c in v['carriers'] for s in c['rider_seats']]
base=identity(pax(rows[0]));assert len(base)==4 and sum(len(v[1]) for v in base)==8
expected=mapping(pax(by['normal-board-attempt']));assert len(expected)==1
assert expected[0][0]==[859674760,2032226056,-1909381182,299761874]
assert expected[0][1]==[-1150326884,1816938073,-1890302266,455976573]
assert expected[0][2]=={'seat':274877911039,'player':[940439953,-167562164,-1601161573,-1389718966]}
keys=('shuttle_vehicle','shuttle_carriage','shuttle_cars','shuttle_start','shuttle_end')
wbase=decoded[0]['winch']
for i,(r,d) in enumerate(zip(rows,decoded)):
 assert r['case']==d['case']
 assert identity(pax(r))==base
 assert r['savedRails']==rows[0]['savedRails']
 assert mapping(pax(r))==([] if i in (0,11) else expected)
 assert r['availability']==f'Validation availability chassis=6/6 carriers=12/12 assignments={0 if i in (0,11) else 1}'
 for k in keys:assert d['winch'][k]==wbase[k]
 for k in ('paxAnchorA','paxAnchorB'):assert d[k]==decoded[0][k]
artifact=read('artifact.json')
assert artifact['sourceRevision']=='7958cdd5ffb5456978f7f9b55e73c5ff5ded7286'
assert artifact['productionSourceRevision']=='a1aeb1a6fc1a1739efc03cede059b0c118879bf7'
assert artifact['jarSha256']=='2a52a641f13ef78fa3c23c2fe466ff22bd359d3af33c4fb26b0f3773379285a2'
assert artifact['profile']=='standalone' and artifact['schema']==6 and artifact['protocol']==7
input_world=read('input-world.json');assert input_world['sourceFoundationSha256']==input_world['copyFoundationSha256']
pre=read('pre-restart-winch.json');final=read('final-winch.json')
assert pre['shuttle_phase']=='LIFTING' and pre['shuttle_fault']=='' and pre['Speed']==16
assert pre['shuttle_pending_retention']==0
pre_cars=[v for v in read('pre-restart-foundation.json')['data']['vehicles'] if v['family']=='pax_shuttle']
assert identity(pre_cars)==base and mapping(pre_cars)==expected
assert all(not v['stopping'] and v['restraints'] and all(c['capture'] for c in v['carriers']) and not v['fault'] for v in pre_cars)
active=geo['occupied-active-lifting-before-stop']['winch'];assert active['shuttle_phase']=='LIFTING' and not active['shuttle_fault']
fault_cases=('restart-observer-only','restart-dev-reconnected-prompt','powered-fault-stable-before-resume')
for name in fault_cases:
 w=geo[name]['winch'];r=by[name]
 assert w['shuttle_phase']=='FAULT' and w['shuttle_interrupted']=='LIFTING'
 assert w['shuttle_fault']=='Shuttle reloaded: inspect and explicitly resume or recover'
 assert w['shuttle_distance']==pre['shuttle_distance'] and w['Speed']==16 and w['shuttle_pending_retention']==0
 assert all(v['stopping'] and v['restraints'] and v['fault']==w['shuttle_fault'] and all(c['capture'] for c in v['carriers']) for v in pax(r))
assert by['restart-observer-only']['devMotion']=='No player was found'
assert 'Validation motion chassis=333d9888-7921-4f08-8e31-27c211de00d2' in by['restart-dev-reconnected-prompt']['devMotion']
game_time=lambda name:int(by[name]['gameTime'].split()[-1])
assert game_time(fault_cases[-1])-game_time(fault_cases[0])==636
assert geo['normal-winch-resume']['winch']['shuttle_phase']=='LIFTING'
assert not geo['normal-winch-resume']['winch']['shuttle_fault']
assert geo['resumed-motor-control']['motor']['Speed']==127
assert geo['closed-restraint-unload-rejected']['winch']['shuttle_phase']=='RETURNING'
assert all(v['restraints'] for v in pax(by['closed-restraint-unload-rejected']))
assert 'RootVehicle' in by['closed-restraint-unload-rejected']['players']['Dev']
assert final['shuttle_phase']=='BOARDING' and final['shuttle_cycles']==pre['shuttle_cycles']+1 and not final['shuttle_fault']
for k in keys:assert pre[k]==final[k]==wbase[k]
final_cars=[v for v in read('final-foundation.json')['data']['vehicles'] if v['family']=='pax_shuttle']
assert identity(final_cars)==base and not mapping(final_cars)
assert all(not v['fault'] and not v['restraints'] for v in final_cars)
assert 'RootVehicle' not in by['normal-restraints-open-unloaded']['players']['Dev']
assert all('RootVehicle' not in d for d in read('final-players.json').values())
# Logical seat continuity does not imply native seat-entity UUID preservation.
assert by['occupied-active-lifting-before-stop']['players']['Dev']['RootVehicle']['Attach']!=by['restart-dev-reconnected-prompt']['players']['Dev']['RootVehicle']['Attach']
shutdown=read('shutdown.json')
for name in ('server1','server2','dev','observer'):
 assert shutdown[name+'Exit']==0
 log=gzip.decompress((root/(name+'.log.gz')).read_bytes()).decode()
 assert 'BUILD SUCCESSFUL' in log
 assert ('All dimensions are saved' if name.startswith('server') else 'Stopping!') in log
log=gzip.decompress((root/'server2.log.gz').read_bytes()).decode()
m=re.search(r'FD_BOARDING_RESTORE mounted player=Dev gameTick=(\d+) deadlineTick=(\d+)',log);assert m
assert tuple(map(int,m.groups()))==(61754,61953)
assert 'FD_BOARDING_ASSIGN player=Dev' in log and 'accepted=true' in log
assert shutdown['xvfbChildrenBeforeStop']==0
manifest=read('manifest.json')
assert set(manifest)=={p.name for p in root.iterdir() if p.is_file() and p.name!='manifest.json'}
for name,sha in manifest.items():assert hashlib.sha256((root/name).read_bytes()).hexdigest()==sha,name
print(f'PASS bounded active-LIFTING restart: {len(rows)} observations, 4 PAX chassis/8 carriers, logical seat restore, explicit resume/cycle/unload, {len(manifest)} hashes. Broader release gates remain open.')
