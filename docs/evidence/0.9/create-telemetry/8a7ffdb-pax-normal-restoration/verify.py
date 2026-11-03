#!/usr/bin/env python3
"""Verify fresh normal-restoration evidence, not legacy migration or all release gates."""
import gzip,hashlib,json,re
from pathlib import Path
root=Path(__file__).resolve().parent
read=lambda n:json.loads((root/n).read_text())
rows=[json.loads(x) for x in (root/'observations.jsonl').read_text().splitlines()]
assert len(rows)==19
by={r['case']:r for r in rows};decoded=read('decoded-observations.json');geo={r['case']:r for r in decoded}
assert len(decoded)==len(rows)
identity=lambda vs:[(v['chassis'],[(c['body'],c['seats']) for c in v['carriers']]) for v in vs]
mapping=lambda vs:[(v['chassis'],c['body'],s) for v in vs for c in v['carriers'] for s in c['rider_seats']]
base=identity(rows[0]['savedVehicles']);assert len(base)==4 and sum(len(v[1]) for v in base)==8
expected=mapping(by['normal-board-lower-attempt']['savedVehicles']);assert len(expected)==1
assert expected[0][0]==[571577530,167201992,-1309861536,82117678]
assert expected[0][1]==[-393715833,-233291156,-1410570422,-390831594]
assert expected[0][2]=={'seat':274877911039,'player':[940439953,-167562164,-1601161573,-1389718966]}
A=140737488621653;B=140737488752725
station=lambda rails:next(r for r in rails if {r['from'],r['to']}=={A,B})
rail_signature=lambda rails:sorted((r['from'],r['to'],r['geometry_hash'],json.dumps(r['profile'],sort_keys=True),json.dumps(r['original_curve'],sort_keys=True)) for r in rails)
connection=lambda d,key,peer:next((c for c in d[key]['AnchorPeerCurves'] if c['Peer']==peer),None)
keys=('shuttle_vehicle','shuttle_cars','shuttle_start','shuttle_end','shuttle_station')
for i,(r,d) in enumerate(zip(rows,decoded)):
 assert r['case']==d['case']
 assert identity(r['savedVehicles'])==base
 assert all(v['family']=='pax_shuttle' for v in r['savedVehicles'])
 assert mapping(r['savedVehicles'])==([] if i in (0,1,18) else expected)
 assert r['availability']==f'Validation availability chassis=4/4 carriers=8/8 assignments={0 if i in (0,1,18) else 1}'
 assert rail_signature(r['savedRails'])==rail_signature(rows[0]['savedRails'])
 assert len(r['savedRails'])==3
 assert d['motor']['Speed']==128
 for k in keys:assert d['winch'][k]==decoded[0]['winch'][k]
 # Only the station connection is removed/replaced; both spike connections are preserved.
 for key,peer in [('paxAnchorA',B),('paxAnchorB',A)]:
  other=lambda row:[c for c in row[key]['AnchorPeerCurves'] if c['Peer']!=peer]
  assert other(d)==other(decoded[0])
  c=connection(d,key,peer)
  if i in (4,5,6):assert c is None
  else:assert c==connection(decoded[0],key,peer)
 if 4<=i<=7:
  assert all(v['fault']=='Matching track support lost' and v['alignment_required'] and v['stopping'] and v['restraints'] and all(c['capture'] for c in v['carriers']) for v in r['savedVehicles'])
 if i in (8,9):
  assert all(not v['fault'] and v['alignment_required'] and v['stopping'] and v['restraints'] and all(c['capture'] for c in v['carriers']) for v in r['savedVehicles'])
 if i>=10:assert all(not v['fault'] and not v['alignment_required'] for v in r['savedVehicles'])
assert station(rows[0]['savedRails'])['id']!=station(by['normal-track-item-side-restored']['savedRails'])['id']
assert not station(by['normal-track-item-side-restored']['savedRails'])['valid']
assert station(by['normal-repair-after-restoration']['savedRails'])['valid']
assert geo['winch-start-before-realignment-rejected']['winch']['shuttle_phase']=='BOARDING'
assert geo['normal-realignment']['winch']['shuttle_phase']=='BOARDING'
assert by['repaired-restart-observer-only']['devMotion']=='No player was found'
assert 'Dev' not in by['repaired-restart-observer-only']['playersOnline']
assert 'Validation motion chassis=' in by['repaired-restart-dev-prompt']['devMotion']
assert 'RootVehicle' in by['repaired-restart-dev-prompt']['players']['Dev']
assert by['aligned-occupied-before-restart']['players']['Dev']['RootVehicle']['Attach']!=by['repaired-restart-dev-prompt']['players']['Dev']['RootVehicle']['Attach']
for name in ('aligned-occupied-before-restart','repaired-restart-observer-only','repaired-restart-dev-prompt','repaired-restart-ready-before-start'):
 w=geo[name]['winch'];assert w['shuttle_phase']=='BOARDING' and w['shuttle_cycles']==0 and 'shuttle_carriage' not in w
 assert all(v['stopping'] and v['restraints'] and all(c['capture'] for c in v['carriers']) for v in by[name]['savedVehicles'])
assert geo['repaired-normal-winch-start']['winch']['shuttle_phase']=='LIFTING'
assert geo['repaired-cycle-followup']['winch']['shuttle_phase']=='CAPTURING'
assert geo['repaired-cycle-boarding']['winch']['shuttle_phase']=='BOARDING'
assert geo['repaired-cycle-boarding']['winch']['shuttle_cycles']==1
assert 'RootVehicle' not in by['repaired-normal-unloaded']['players']['Dev']
for alias,rowname in [('pre-restart','aligned-occupied-before-restart'),('final','repaired-normal-unloaded')]:
 data=read(alias+'-foundation.json')['data'];w=read(alias+'-winch.json')
 assert data['schema']==6 and identity(data['vehicles'])==base
 assert rail_signature(data['rails'])==rail_signature(rows[0]['savedRails'])
 assert mapping(data['vehicles'])==([] if alias=='final' else expected)
 assert all(not v['fault'] and not v['alignment_required'] for v in data['vehicles'])
 for k in keys:assert w[k]==geo[rowname]['winch'][k]
 assert w['shuttle_phase']=='BOARDING' and not w['shuttle_fault']
 if alias=='final':
  assert w['shuttle_cycles']==1 and all(not v['restraints'] for v in data['vehicles'])
  assert w['shuttle_carriage']==geo['repaired-normal-winch-start']['winch']['shuttle_carriage']
  assert all('RootVehicle' not in d for d in read('final-players.json').values())
 else:assert w['shuttle_cycles']==0 and 'shuttle_carriage' not in w
artifact=read('artifact.json')
assert artifact['sourceRevision']=='8a7ffdbfa7b77c58230d997da47d9cc0e1ee75f4'
assert artifact['jarSha256']=='bb694007d157faa53eb7376de9d2558280239557e87081021cea579bf2dc0cba'
assert artifact['profile']=='standalone' and artifact['schema']==6 and artifact['protocol']==7
shutdown=read('shutdown.json')
for name in ('server1','server2','dev','observer'):
 assert shutdown[name+'Exit']==0
 log=gzip.decompress((root/(name+'.log.gz')).read_bytes()).decode()
 assert 'BUILD SUCCESSFUL' in log
 assert ('All dimensions are saved' if name.startswith('server') else 'Stopping!') in log
log=gzip.decompress((root/'server2.log.gz').read_bytes()).decode()
m=re.search(r'FD_BOARDING_RESTORE mounted player=Dev gameTick=(\d+) deadlineTick=(\d+)',log)
assert m and tuple(map(int,m.groups()))==(5438,5637)
assert shutdown['xvfbChildrenBeforeStop']==0
manifest=read('manifest.json')
assert set(manifest)=={p.name for p in root.iterdir() if p.is_file() and p.name!='manifest.json'}
for name,sha in manifest.items():assert hashlib.sha256((root/name).read_bytes()).hexdigest()==sha,name
print(f'PASS bounded fresh PAX normal restoration: {len(rows)} observations; exact native geometry/provenance, all4/all8 identities, repair/realign ordering, cold restart/rider restore, normal cycle/unload; {len(manifest)} hashes. Legacy migration and broader release gates remain open.')
