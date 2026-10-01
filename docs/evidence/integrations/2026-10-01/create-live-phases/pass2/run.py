from pathlib import Path
import ast,json,os,subprocess,time
from rcon import rcon
base=Path(__file__).resolve().parent;links=json.loads((base/'links.json').read_text());clients=json.loads((base.parent/'clients.json').read_text());rows=[];captures=[];serial=0
initial=None
run_id="live"+str(time.time_ns())

def report(label):
 global serial
 serial+=1;label=f'{run_id}_{label}_{serial}'
 assert 'snapshot recorded' in rcon('fd validation pax_energy_restart report '+label)
 until=time.monotonic()+10
 while time.monotonic()<until:
  for line in reversed((base.parent/'server.log').read_text().splitlines()):
   if 'FD_PAX_ENERGY_RESTART {' in line:
    d=json.loads(line.split('FD_PAX_ENERGY_RESTART ',1)[1])
    if d['label']==label:return d
  time.sleep(.03)
 raise AssertionError(label)

def expected(t,field,station):
 denial='Vehicle outside station range' if station else t['dispatchDenial'];ready=False if station else t['dispatchReady']
 return {'status':t['fault'] or ('Ready to dispatch' if ready else t['phase'].capitalize()),'occupancy':f"{t['occupied']} / {t['capacity']}",'restraints':'Restraints closed: '+('Yes' if t['restraintsClosed'] else 'No'),'dispatch':'Ready to dispatch' if ready else denial,'phase':t['phase'].capitalize(),'fault':t['fault']}[field]

def sample(label,fault_source=False):
 before=report(label+'_before');texts={}
 for link in links:
  coords=' '.join(map(str,link['target']));response=rcon('data get block '+coords+' front_text.messages[0]')
  field='fault' if fault_source and link['source']=='winch' and link['field']=='status' else link['field']
  texts[link['source']+'_'+field]=json.loads(ast.literal_eval(response.split('block data: ',1)[1].strip()))
 after=report(label+'_after');t=after['telemetry']
 same=before['telemetry']==t
 expected_text={l['source']+'_'+('fault' if fault_source and l['source']=='winch' and l['field']=='status' else l['field']):expected(t,'fault' if fault_source and l['source']=='winch' and l['field']=='status' else l['field'],l['source']=='station') for l in links}
 row={'label':label,'before':before,'after':after,'signs':texts,'expected':expected_text,'same_telemetry':same,'converged':same and texts==expected_text}
 rows.append(row);(base/'samples.json').write_text(json.dumps(rows,indent=2)+'\n')
 assert t['available'] and t['occupied']==16 and t['capacity']==16 and t['cars']==4 and t['restraintsClosed']
 assert after['energy']==12345 and not after['externalDriveAllowed'] and after['carriageLoaded']
 riders=[r for c in after['cars'] for b in c['carriers'] for r in b['riders']]
 assert len(riders)==16 and all(r['alive'] and r['mounted'] for r in riders)
 print(label,after['tick'],after['phase'],'converged',row['converged'],flush=True)
 return row

def stable(label,phase,fault_source=False,seconds=10):
 until=time.monotonic()+seconds
 while time.monotonic()<until:
  r=sample(label,fault_source)
  if r['converged'] and r['after']['phase']==phase:return r
  time.sleep(.2)
 raise AssertionError((label,phase))

def capture(label,row):
 for c in clients:
  filename=label+'-'+c['label']+'.png'
  subprocess.run(['import','-window','root',str(base/filename)],env=dict(os.environ,DISPLAY=c['display']),check=True)
  captures.append({'label':label,'client':c['label'],'image':filename,'sample_label':row['label'],'server_tick':row['after']['tick'],'phase':row['after']['phase']})
 (base/'captures.json').write_text(json.dumps(captures,indent=2)+'\n')

r=stable('ready','BOARDING');assert r['after']['telemetry']['dispatchReady'];initial=r['after'];capture('ready',r)
assert 'fixture: dispatch' in rcon('fd validation pax_energy_restart dispatch')
r=stable('first_lift','LIFTING');capture('first_lift',r)
rcon('setblock -607 180 608 minecraft:air')
r=stable('power_fault','FAULT');assert 'Winch power lost' in r['after']['fault'];capture('power_fault',r)
# Fault stays latched while native power is restored; never edit the winch phase or fault.
assert 'fixture: dispatch' not in rcon('fd validation pax_energy_restart dispatch')
assert 'fixture: unload' not in rcon('fd validation pax_energy_restart unload')
r=stable('native_fault_source','FAULT',True);capture('native_fault_source',r)
rcon('setblock -607 180 608 create:creative_motor[facing=east]{ScrollValue:128}')
time.sleep(1)
r=stable('power_restored_latched','FAULT',True);capture('power_restored_latched',r)
assert 'fixture: resume' in rcon('fd validation pax_energy_restart resume')
r=stable('resumed_lift','LIFTING');capture('resumed_lift',r)
seen={'LIFTING'};until=time.monotonic()+180
while time.monotonic()<until:
 r=sample('cycle_'+str(serial));phase=r['after']['phase'];assert phase!='FAULT',r['after']['fault']
 if r['converged'] and phase not in seen:
  seen.add(phase);capture(phase.lower(),r)
 if phase=='BOARDING' and r['converged']:
  assert r['after']['cycles']==initial['cycles']+1
  assert r['after']['telemetry']['dispatchReady']
  break
 time.sleep(.25)
else:raise AssertionError('No completed cycle')
assert {'LIFTING','OUTBOUND','RETURNING','CAPTURING','BOARDING'}<=seen,seen
rcon('fd validation pax_energy_restart unload');time.sleep(1)
d=report('unloaded');assert d['telemetry']['occupied']==0 and not d['telemetry']['restraintsClosed'] and d['cycles']==initial['cycles']+1
(base/'unloaded.json').write_text(json.dumps(d,indent=2)+'\n');(base/'run.complete').write_text('complete\n')
