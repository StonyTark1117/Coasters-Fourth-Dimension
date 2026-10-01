#!/usr/bin/env python3
"""Audit native safety regressions and balanced old/new occupied-scene measurements."""
import ast,gzip,hashlib,itertools,json,math,re,statistics,subprocess,sys,uuid
import xml.etree.ElementTree as ET
from pathlib import Path
root=Path(sys.argv[1])
def read(name):
 p=root/name
 return gzip.decompress(p.read_bytes()).decode() if p.suffix=='.gz' else p.read_text()
def data(name):return json.loads(read(name))
m=data('manifest.json')
for name,h in m['files'].items():assert hashlib.sha256((root/name).read_bytes()).hexdigest()==h,name
regressions={}
for profile,skips in {'base':7,'engineered':4,'styles':7,'additions':5,'magnetized':5,'immersion':7,'combined':0}.items():
 log=read('native-'+profile+'.log.gz')
 assert read('native-'+profile+'.exit').strip()=='0'
 assert '35 GAME TESTS COMPLETE' in log and 'All 35 required tests passed' in log and 'BUILD SUCCESSFUL' in log
 assert len(re.findall(r'^FD_\w+_SKIPPED:',log,re.M))==skips
 versions={mod:version for version,mod in re.findall(r'^\t\t.+ ([^ ]+) \(([^)]+)\)$',log,re.M)}
 assert versions==m['loaded_mod_versions'][profile]
 for key,version in {'minecraft':'1.21.1','neoforge':'21.1.248','create':'6.0.10','sable':'2.0.3'}.items():assert versions[key]==version
 for addon,key,version in [('engineered','coastersengineered','1.1.1'),('styles','coasterssimulatedextratypes','1.2.0'),('additions','coasterfins','0.2.0'),('magnetized','coastersmagnetized','1.1.0'),('immersion','coasters_immersion','0.2.3')]:
  assert (versions.get(key)==version) if profile in [addon,'combined'] else key not in versions
 assert 'FD_CONNECTED_MATRIX cases=200 matchingAccepted=40 wrongFamilyRejected=160 endpoints=2 appearances=4 bindingRestored=true' in log
 assert 'FD_BOUNDARY_MATRIX families=4 directions=2 wrongFamilyStops=8 missingProviderStops=8 matchingCrossings=8' in log
 rows=[]
 for line in log.splitlines():
  if 'FD_BOUNDARY_CASE ' not in line:continue
  fields,fault=line.split(' fault=',1);row=dict(re.findall(r'(\w+)=(\S+)',fields));row['fault']=fault;rows.append(row)
 assert len(rows)==24
 assert {(r['family'],r['mode'],r['direction']) for r in rows}==set(itertools.product(['ARROW','LEGACY','FREE_SPIN','REVISED'],['MATCHING','WRONG_FAMILY','MISSING_PROVIDER'],['FORWARD','REVERSE']))
 for r in rows:
  z=float(r['z']);speed=float(r['speed']);stopped=r['mode']!='MATCHING'
  assert math.isfinite(z) and math.isfinite(speed) and r['restraints']=='true'
  assert r['stopping']==str(stopped).lower() and r['originalBinding']==str(stopped).lower() and r['nextBinding']==str(not stopped).lower()
  if stopped:assert 301<z<340 and speed<.1 and r['fault']=='End of compatible track; capture and inspect the route'
  else:assert (z<299 if r['direction']=='REVERSE' else z>341) and speed>.1 and not r['fault']
 for i in range(2):assert f'FD_LINKED_SUPPORT damaged={i} eligible=false filtered=false drive=[false, false]' in log
 turn=re.search(r'FD_TRAIN maxTurn=([\d.]+) maxAxleDifference=([\d.]+)',log)
 assert turn and float(turn[1])>6 and float(turn[2])>1
 assert 'FD_DELAYED_PASSENGER pendingIdentity=true pendingMass=true delayedReplay=true loadedEmpty=true' in log
 for phase in ['LIFTING','OUTBOUND','RETURNING','CAPTURING']:
  assert f'FD_PAX_PHASE_COMPLETE target={phase}' in log
  assert f'FD_PAX_PHASE_ENERGY target={phase} required='+str(profile in ['engineered','combined']).lower() in log
 regressions[profile]={'registered':35,'absent_addon_skips':skips,'connected_eligibility_cases':200,'moving_boundary_cases':24,'linked_provider_loss_probes':2,'controlled_max_turn':float(turn[1]),'controlled_max_axle_difference':float(turn[2])}
assert data('regressions-status.json')['status']=='complete' and read('regressions.exit').strip()=='0'
state=data('status.json');assert state['status']=='complete' and read('benchmark.exit').strip()=='0'
# The interrupted first new-artifact attempt is diagnostic evidence, not a timing window.
failed=data('failed-attempt/status.json')
assert failed['status']=='failed' and 'JSONDecodeError' in failed['error']
assert read('failed-attempt/benchmark.exit').strip()=='1'
assert failed['runs'][0]==state['runs'][0] and failed['runs'][1]['case']=='base-0-new'
assert state['runs'][1]['world'].endswith('Retry')
assert 'BUILD SUCCESSFUL' in read('failed-attempt/base-0-new.log.gz')
assert not (root/'failed-attempt/base-0-new.exit').exists()
assert all(data('reader-check.json.gz').values())
# Exercise the retained parser itself without executing its server setup.
module=ast.parse(read('benchmark-helper.py'))
parser=next(n for n in module.body if isinstance(n,ast.FunctionDef) and n.name=='complete_samples')
namespace={'json':json};exec(compile(ast.Module(body=[parser],type_ignores=[]),'retained-parser','exec'),namespace)
native_line=next(l for l in read('failed-attempt/base-0-new.log.gz').splitlines(keepends=True) if 'FD_PERFORMANCE_SAMPLE {' in l)
assert len(list(namespace['complete_samples'](native_line)))==1
assert list(namespace['complete_samples'](native_line[:57259]))==[]
try:list(namespace['complete_samples']('FD_PERFORMANCE_SAMPLE {broken}\n'))
except json.JSONDecodeError:pass
else:raise AssertionError('Completed malformed record must fail')

order=[f'{profile}-{repeat}-{version}' for repeat in range(2) for profile in ['base','combined'] for version in (['old','new'] if repeat==0 else ['new','old'])]
assert [r['case'] for r in state['runs']]==order
samples={};identities=None
for run in state['runs']:
 case=run['case'];version=case.split('-')[-1]
 assert run['status']=='complete' and read(case+'.exit').strip()=='0'
 assert run['artifact_sha256']==state['artifacts'][version]['sha256']
 assert '-PfdPackagedJar='+state['artifacts'][version]['path'] in run['command']
 log=read(case+'.log.gz');assert 'BUILD SUCCESSFUL' in log
 assert any(state['artifacts'][version]['path'] in line for line in log.splitlines() if 'Coasters: Fourth Dimension loaded from' in line)
 found={}
 for line in log.splitlines():
  if 'FD_PERFORMANCE_SAMPLE {' in line:
   row=json.loads(line.split('FD_PERFORMANCE_SAMPLE ',1)[1]);assert row['label'] not in found;found[row['label']]=row
 assert set(found)=={case+'-parked',case+'-operating'}
 for label,d in found.items():
  assert d==data(label+'.json.gz');samples[label]=d
  for key in ['tickNanos','carrierNanos','carrierSteps']:assert len(d[key])==600 and all(isinstance(v,int) and v>=0 for v in d[key])
  assert set(d['carrierSteps'])=={6} and d['after']['tick']-d['before']['tick'] in [600,601]
  for scene in [d['before'],d['after']]:
   assert len(scene['cars'])==21 and scene['rails']==13 and scene['forcedChunks']==158 and scene['phase']!='FAULT'
   assert {f:sum(c['family']==f for c in scene['cars']) for f in ['ARROW','FREE_SPIN','PAX']}=={'ARROW':14,'FREE_SPIN':3,'PAX':4}
   bodies=[b for c in scene['cars'] for b in c['carriers']];riders=[r for b in bodies for r in b['riders']]
   assert len(bodies)==48 and len(riders)==96
   assert all(c['loaded'] and c['restraints'] and not c['fault'] and c['linked']==(7 if c['family']=='ARROW' else 4 if c['family']=='PAX' else 1) for c in scene['cars'])
   assert all(b['loaded'] and len(b['riders'])==2 and all(r['alive'] and r['mounted'] and r['carrier']==b['id'] for r in b['riders']) for b in bodies)
   ids=(frozenset(c['id'] for c in scene['cars']),frozenset(b['id'] for b in bodies),frozenset((r['id'],r['seat'],b['id']) for b in bodies for r in b['riders']))
   assert tuple(map(len,ids))==(21,48,96)
   identities=identities or ids;assert ids==identities
  assert sum(d['phases'].values())==600
  if label.endswith('-parked'):assert d['phases']=={'BOARDING':600} and all(c['held'] for c in d['after']['cars'])
  else:
   assert set(d['phases'])=={'LIFTING','OUTBOUND','RETURNING','CAPTURING'}
   before={c['id']:c for c in d['before']['cars']}
   assert all(abs(c['z']-before[c['id']]['z'])>.01 for c in d['after']['cars'] if c['family']!='PAX')
 def uid(parts):return str(uuid.UUID(int=sum((v&0xffffffff)<<(96-i*32) for i,v in enumerate(parts))))
 saved=json.loads(subprocess.check_output([sys.executable,str(Path(__file__).with_name('inspect-foundation-save.py')),str(root/(case+'-foundation.dat'))],text=True))['data']
 assert saved['schema']==5 and len(saved['rails'])==13 and len(saved['vehicles'])==21
 assert {uid(v['chassis']) for v in saved['vehicles']}==set(identities[0])
 assert {uid(b['body']) for v in saved['vehicles'] for b in v['carriers']}==set(identities[1])
 assert {(uid(r),uid(b['body'])) for v in saved['vehicles'] for b in v['carriers'] for r in b['occupants']}=={(r,b) for r,seat,b in identities[2]}
assert len(samples)==16
summary={}
def stats(values):
 v=sorted(values)
 return {'mean_ms':statistics.mean(v)/1e6,'p95_ms':v[math.ceil(.95*len(v))-1]/1e6,'p99_ms':v[math.ceil(.99*len(v))-1]/1e6,'max_ms':max(v)/1e6,'over_50ms':sum(n>50e6 for n in v)}
for label,d in samples.items():
 summary[label]=stats(d['tickNanos']);summary[label]['carrier_callback_ms_per_tick']=statistics.mean(d['carrierNanos'])/1e6
pairs={}
for profile,repeat,mode in itertools.product(['base','combined'],range(2),['parked','operating']):
 old=summary[f'{profile}-{repeat}-old-{mode}'];new=summary[f'{profile}-{repeat}-new-{mode}']
 pairs[f'{profile}-{repeat}-{mode}']={'old':old,'new':new,'callback_reduction_percent':100*(old['carrier_callback_ms_per_tick']-new['carrier_callback_ms_per_tick'])/old['carrier_callback_ms_per_tick'],'mean_tick_reduction_percent':100*(old['mean_ms']-new['mean_ms'])/old['mean_ms']}
suites=[ET.parse(p).getroot() for p in (root/'unit-tests').glob('TEST-*.xml')]
assert sum(int(s.attrib['tests']) for s in suites)==19 and all(int(s.attrib[k])==0 for s in suites for k in ['errors','failures','skipped'])
restore=data('settings-restored.json');assert restore['before']==restore['after']
assert not data('cleanup.json')['live']
diff=data('artifact-diff.json')
assert diff['added']==['dev/fourthdimension/runtime/VehicleTrackSample.class'] and not diff['removed']
assert diff['changed']==['dev/fourthdimension/runtime/CarrierRuntime.class','dev/fourthdimension/vehicle/BoundaryGuard.class']
print(json.dumps({'regressions':regressions,'unit_tests':19,'scene':{'cars':21,'carriers':48,'riders':96},'samples':summary,'pairs':pairs},indent=2))
