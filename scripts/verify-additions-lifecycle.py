#!/usr/bin/env python3
"""Audit occupied Additions station restarts and real wireless receiver delivery."""
import copy,gzip,hashlib,json,re,subprocess,sys,tempfile,uuid,zipfile
import xml.etree.ElementTree as ET
from pathlib import Path
root=Path(sys.argv[1])
def data(n):return json.loads((root/n).read_text())
def log(n):return gzip.decompress((root/n).read_bytes()).decode()
def uid(w):return str(uuid.UUID(int=sum((n&0xffffffff)<<(96-32*i) for i,n in enumerate(w))))
manifest=data('manifest.json')
for name,expected in manifest['files'].items():assert hashlib.sha256((root/name).read_bytes()).hexdigest()==expected,name
baseline=data('seed/occupied.json')['foundation']
players={uid(p['player']) for v in baseline['vehicles'] for c in v['carriers'] for p in c['rider_seats']}
assert len(players)==2 and baseline['schema']==5 and len(baseline['rails'])==len(baseline['vehicles'])==5
assert {v['family'] for v in baseline['vehicles']}=={'arrow_controlled','zacspin_legacy','sns_free_spin','zacspin_revised','pax_shuttle'}
assert sum(len(v['carriers']) for v in baseline['vehicles'])==14
assert sum(len(r['magnets']) for r in baseline['rails'])==1
mounts={uid(c['body']):sorted(uid(i) for i in c['occupants']) for v in baseline['vehicles'] for c in v['carriers']}
pigs=data('seed/native-pigs.json');seats={p['id']:p['parent_id'] for p in pigs};assert len(seats)==5
settings=['Dispatch','DispatchMin','DispatchDelay','DispatchCooldown','Span','PreSpan','Ch0','Ch1','Ch2','Ch3']
seed_cars={v['id']:v for v in data('seed/occupied.json')['cars']}
def stable(f,offline=False,unloaded=False):
 f=copy.deepcopy(f);f['vehicles'].sort(key=lambda v:str(v['chassis']))
 for v in f['vehicles']:
  if unloaded:
   assert v['restraints']==0 and v['revision']==(2 if v['family']=='pax_shuttle' else 4)
   v['restraints']=1;v['revision']=1
  for c in v['carriers']:
   c.pop('angle');c.pop('omega')
   old=next(b for a in baseline['vehicles'] for b in a['carriers'] if b['body']==c['body'])
   if unloaded:
    assert not c['occupants'] and not c['rider_seats'];c['occupants']=old['occupants'];c['rider_seats']=old['rider_seats']
   if offline:c['occupants']=[i for i in c['occupants'] if uid(i) not in players]
   c['occupants'].sort();c['rider_seats'].sort(key=str)
 return f

def occupied(s):
 assert s['additions'] and stable(s['foundation'])==stable(baseline)
 found={}
 for v in s['cars']:
  assert v['loaded'] and v['ready'] and v['stopping'] and v['restraints'] and not v['fault'] and not v['driveAllowed']
  assert v['speed']<.15 and v['channels']==v['received']
  t=v['telemetry'];assert t['available'] and t['boarding'] and t['captured'] and t['restraints'] and not t['fault']
  count=sum(len(c['riders']) for c in v['carriers']);assert t['occupied']==count
  assert v['channels']==[15,1+int(14*count/t['capacity']+.5),15,0]
  if v['family']=='pax_shuttle':assert not t['dispatch'] and 'winch' in t['denial']
  for c in v['carriers']:
   assert c['loaded'];found[c['id']]=sorted(r['id'] for r in c['riders'])
   for r in c['riders']:
    assert r['alive'] and r['carrier']==c['id'] and r['seat']
    if r['id'] in seats:assert r['seat']==seats[r['id']]
  for k in settings:assert v['stationNbt'][k]==seed_cars[v['id']]['stationNbt'][k],(v['family'],k)
  assert v['finNbt']==seed_cars[v['id']]['finNbt']
 assert found==mounts

def native_decode(case,kind):
 with tempfile.TemporaryDirectory(prefix='fd-additions-audit-') as tmp:
  folder=Path(tmp)/('entities' if kind=='pigs' else 'region');folder.mkdir()
  with zipfile.ZipFile(root/case/('entity-regions.zip' if kind=='pigs' else 'block-regions.zip')) as z:
   for name in z.namelist():
    assert Path(name).name==name and name.endswith(('.mca','.mcc'));(folder/name).write_bytes(z.read(name))
  actual=json.loads(subprocess.check_output([sys.executable,str(root/'drivers'/('inspect-entities.py' if kind=='pigs' else 'inspect-blocks.py')),tmp],text=True))
  assert actual==data(case+'/native-'+kind+'.json')
 return actual

results={}
for case in ['seed','restored','combined']:
 raw=log(case+'/server.log.gz');reports=[json.loads(l.split('FD_ADDITIONS_LIFECYCLE ',1)[1]) for l in raw.splitlines() if 'FD_ADDITIONS_LIFECYCLE {' in l]
 s=data(case+'/occupied.json');assert s in reports;occupied(s)
 assert 'Coasters Simulated Additions 0.2.0' in raw
 commands=(root/case/'commands.txt').read_text()
 if case!='seed':assert 'board_player' not in commands and 'additions_restart pigs' not in commands
 hashes=set(re.findall(r'FD_SNAPSHOT side=server.*?tick=(\d+) sha256=([a-f0-9]+) railsSha256=([a-f0-9]+)',raw));matches={}
 for name in ['dev','observer']:
  text=log(case+'/'+name+'.log.gz');assert 'BUILD SUCCESSFUL' in text and (root/case/(name+'.exit')).read_text().strip()=='0'
  matches[name]=len(hashes&set(re.findall(r'FD_SNAPSHOT side=client.*?tick=(\d+) sha256=([a-f0-9]+) railsSha256=([a-f0-9]+)',text)));assert matches[name]>=10
  for family in [v['family'] for v in baseline['vehicles']]:assert re.search(r'FD_STATION .*family='+family+r',.*occupied='+('1' if case=='seed' else '0')+r',.*restraintsClosed=false',text)
 assert 'BUILD SUCCESSFUL' in raw and (root/case/'server.exit').read_text().strip()=='0'
 if case!='seed':
  snaps={n:data(case+'/'+n+'.json') for n in ['before-release','ready-delay','departed','recaptured','unloaded']}
  assert all(x in reports for x in snaps.values())
  occupied(snaps['before-release']);assert snaps['ready-delay']['tick']-snaps['before-release']['tick']==20
  assert snaps['departed']['tick']-snaps['before-release']['tick']==50
  assert all(v['stopping'] for v in snaps['ready-delay']['cars'])
  for v in snaps['departed']['cars']:
   assert v['channels']==v['received'] and v['restraints'] and not v['fault']
   if v['family']=='pax_shuttle':assert v['stopping'] and v['speed']<.15 and v['channels'][0]==15 and 'winch' in v['telemetry']['denial']
   else:assert not v['stopping'] and v['speed']>1 and v['channels'][0]==0
   assert all(r['alive'] and r['carrier']==c['id'] for c in v['carriers'] for r in c['riders'])
  assert all(v['ready'] and v['stopping'] and not v['fault'] for v in snaps['recaptured']['cars'])
  assert all(not v['restraints'] and v['ready'] and not v['fault'] for v in snaps['unloaded']['cars'])
  assert not any(c['riders'] for v in snaps['unloaded']['cars'] for c in v['carriers'])
 saved=data(case+'/foundation.json')['data'];assert stable(saved,offline=True,unloaded=case!='seed')==stable(baseline,offline=True)
 actual=json.loads(subprocess.check_output([sys.executable,'scripts/inspect-foundation-save.py',str(root/case/'world/data/fourthdimension_foundation.dat')],text=True));assert actual['data']==saved
 if case!='seed':
  for player in players:
   file=root/case/'unloaded-playerdata'/(player+'.dat')
   p=json.loads(subprocess.check_output([sys.executable,'scripts/inspect-foundation-save.py',str(file)],text=True))
   assert 'RootVehicle' not in p and not p.get('NoGravity',0) and p['OnGround'] and p['Health']==20 and p['Pos'][1]==116 and not p['abilities']['flying']
 native=native_decode(case,'pigs');assert {p['id'] for p in native}==set(seats) and all(p['health']==10 for p in native)
 if case=='seed':assert all(p['parent_id']==seats[p['id']] and p['parent_type']=='create:seat' for p in native)
 else:assert all(p['parent_id'] is None for p in native)
 blocks=native_decode(case,'blocks');assert len(blocks)==30
 assert sum(v['id']=='create:redstone_link' for v in blocks)==20
 for v in blocks:
  if v['id']=='coasterfins:station':
   original=next(c['stationNbt'] for c in seed_cars.values() if c['stationNbt']['x']==v['x'])
   for k in settings:assert v[k]==original[k]
   assert 'fourthdimension_ride' not in v
  if v['id']=='coasterfins:coaster_fin':assert v['ScrollValue']==10 and v['SpanMode']=='boost' and v['IsPowered']==(1 if case=='seed' else 0)
 results[case]={'occupied':7,'carriers':14,'wireless_receivers':20,'client_snapshot_matches':matches,'operated_and_unloaded':case!='seed'}
# Keep failed attempts distinct and verify their actual reasons and recovery.
invalid=data('invalid-frequency/pigs-ready.json')
assert all('A' not in v['stationNbt']['Ch'+str(i)] for v in invalid['cars'] for i in range(4))
failed=log('uninitialized-link/native-additions.log.gz')
assert 'Native receiving link has not initialized' in failed and '36 GAME TESTS COMPLETE' in failed
assert (root/'uninitialized-link/native-additions.exit').read_text().strip()!='0'
occupied(data('boundary-observation/occupied.json'))
guarded=data('boundary-observation/guarded-capture.json')
assert all(v['ready'] and v['stopping'] and v['speed']<.15 for v in guarded['cars'])
assert all(('End of compatible track' in v['fault'])==(v['family']!='pax_shuttle') for v in guarded['cars'])
assert {c['id']:sorted(r['id'] for r in c['riders']) for v in guarded['cars'] for c in v['carriers']}==mounts
assert all(r['alive'] and r['carrier']==c['id'] for v in guarded['cars'] for c in v['carriers'] for r in c['riders'])
assert not any(c['riders'] for v in data('boundary-observation/unloaded-recovery.json')['cars'] for c in v['carriers'])
recovered=native_decode('boundary-observation','pigs')
assert {p['id'] for p in recovered}==set(seats) and all(p['health']==10 and p['parent_id'] is None for p in recovered)
for case in ['invalid-frequency','uninitialized-link','boundary-observation']:
 assert (root/case/'server.exit').read_text().strip()=='0'
regressions={}
for profile,skips in [('additions',5),('base',7),('combined',0)]:
 raw=log('native-'+profile+'.log.gz');assert '36 GAME TESTS COMPLETE' in raw and 'All 36 required tests passed' in raw and 'BUILD SUCCESSFUL' in raw
 assert (root/('native-'+profile+'.exit')).read_text().strip()=='0'
 assert len(re.findall(r'^FD_\w+_SKIPPED:',raw,re.M))==skips
 if profile!='base':
  checks=re.findall(r'FD_ADDITIONS_WIRELESS phase=(\S+) station=(.*?) received=\[(.*?)\]',raw)
  assert len(checks)==20 and {phase for phase,_,_ in checks}=={'open','occupied-held','dispatch','cleared'}
  assert sum(signal=='0, 0, 0, 0' for _,_,signal in checks)==5
 regressions[profile]={'registered':36,'absent_addon_skips':skips}
suites=[ET.parse(p).getroot() for p in (root/'unit-tests').glob('TEST-*.xml')];assert sum(int(s.attrib['tests']) for s in suites)==19
assert all(int(s.attrib[k])==0 for s in suites for k in ['failures','errors','skipped'])
c=data('cleanup.json');assert c['all_gone'] and c['settings_before']==c['settings_after'] and c['ports_closed']==[25585,25586]
print(json.dumps({'cases':results,'native':regressions,'unit_tests':19,'cleanup':True},indent=2))
