"""Run occupied PAX cold-restart fixtures sequentially on private loopback ports."""
from pathlib import Path
import hashlib,json,os,shutil,socket,struct,subprocess,sys,time,secrets
REPO=Path(__file__).resolve().parents[2]
OUT=Path(__file__).resolve().parent
RUNTIME=Path('/tmp/fourthdimension-48535694/build/runs/packaged-server')
ARTIFACT=Path('/home/braydon/.cache/fourthdimension-48535694/passenger-load-fixed.jar')
assert ARTIFACT.read_bytes()==(OUT/'fixed.jar').read_bytes()
STATE={'status':'starting','artifact_sha256':hashlib.sha256(ARTIFACT.read_bytes()).hexdigest(),'runs':[]}
server=None; stream=None; log=None; cmdlog=None; baseline=None

def status(): (OUT/'packaged-status.json').write_text(json.dumps(STATE,indent=2)+'\n')
def rcon(cmd):
 props=dict(line.split('=',1) for line in (RUNTIME/'server.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
 def packet(n,t,text):
  body=struct.pack('<ii',n,t)+text.encode()+b'\0\0'
  return struct.pack('<i',len(body))+body
 with socket.create_connection(('127.0.0.1',int(props['rcon.port'])),timeout=10) as sock:
  def read():
   def exact(n):
    b=b''
    while len(b)<n:
     x=sock.recv(n-len(b))
     if not x: raise ConnectionError('RCON closed')
     b+=x
    return b
   n=struct.unpack('<i',exact(4))[0];body=exact(n)
   return struct.unpack('<ii',body[:8]),body[8:-2].decode()
  sock.sendall(packet(11,3,props['rcon.password']));header,_=read()
  assert header[0]==11,'RCON authentication failed'
  sock.sendall(packet(12,2,cmd));header,result=read();assert header[0]==12
 with cmdlog.open('a') as f:f.write(cmd+': '+result+'\n\n')
 assert 'Unknown or incomplete command' not in result and 'unexpected error' not in result,result
 return result

def action(name):
 result=rcon('fd validation pax_energy_restart '+name)
 assert 'PAX energy restart fixture: '+name in result,result

def rows():
 return {d['label']:d for line in log.read_text().splitlines() if 'FD_PAX_ENERGY_RESTART {' in line for d in [json.loads(line.split('FD_PAX_ENERGY_RESTART ',1)[1])]}

def report(label):
 assert label not in rows(),'Duplicate report label'
 assert 'snapshot recorded' in rcon('fd validation pax_energy_restart report '+label)
 until=time.monotonic()+10
 while label not in rows():
  assert time.monotonic()<until,'Report did not flush';time.sleep(.1)
 d=rows()[label];print(STATE['runs'][-1]['case'],label,d['tick'],d['phase'],d['telemetry']['occupied'],flush=True)
 return d

def occupied(d):
 assert d['required'] and d['energy']==12345 and not d['externalDriveAllowed']
 assert len(d['cars'])==4
 if d['phase']!='BOARDING':assert d['carriageLoaded']
 assert all(c['consist']==4 and c['restraints'] and len(c['carriers'])==2 for c in d['cars'])
 assert all(b['loaded'] and len(b['riders'])==2 and all(r['alive'] and r['mounted'] and r['carrier']==b['body'] for r in b['riders']) for c in d['cars'] for b in c['carriers']),d
 t=d['telemetry'];assert t['available'] and t['cars']==4 and t['occupied']==16 and t['capacity']==16 and t['restraintsClosed'] and t['phase']==d['phase']
 if d['phase']=='FAULT':assert all(c['stopping'] and all(b['capture'] for b in c['carriers']) for c in d['cars'])
 return d

def mounts(d):return {c['chassis']:{b['body']:sorted(r['id'] for r in b['riders']) for b in c['carriers']} for c in d['cars']}

def step(n):
 assert 'Stepping' in rcon('tick step '+str(n));time.sleep(n/20+1)
 until=time.monotonic()+30
 while 'The game is frozen' not in rcon('tick query'):
  assert time.monotonic()<until;time.sleep(1)

def launch(case,world,profile,seed=None):
 global server,stream,log,cmdlog
 assert server is None or server.poll() is not None
 path=RUNTIME/world;assert not path.exists(),world
 if seed:shutil.copytree(seed,path)
 props=RUNTIME/'server.properties'
 props.write_text('\n'.join('level-name='+world if l.startswith('level-name=') else l for l in props.read_text().splitlines())+'\n')
 log=OUT/(case+'.log');cmdlog=OUT/(case+'-commands.txt');assert not log.exists()
 cmd=['./gradlew','-Dorg.gradle.jvmargs=-Xmx1G','--max-workers=2','runServer','-PfdPackaged','-PfdPackagedJar='+str(ARTIFACT),'-PfdFrozen','-PfdValidation','-PfdIntegrations='+profile]
 stream=log.open('w');server=subprocess.Popen(cmd,cwd=REPO,stdin=subprocess.DEVNULL,stdout=stream,stderr=subprocess.STDOUT,start_new_session=True,env={**os.environ,'JAVA_HOME':'/usr/lib/jvm/java-21-openjdk','JAVA_TOOL_OPTIONS':'-Xmx2G'})
 STATE['runs'].append({'case':case,'world':world,'profile':profile,'pid':server.pid,'command':cmd,'status':'running'});status()
 until=time.monotonic()+180
 while 'RCON running on' not in log.read_text():
  assert server.poll() is None,'Server ended before startup'
  assert time.monotonic()<until,'Startup timeout with server still live';time.sleep(1)
 return path

def stop(world):
 rcon('save-all flush');rcon('stop');code=server.wait(timeout=120);stream.close()
 STATE['runs'][-1].update(status='complete',exit=code,log_sha256=hashlib.sha256(log.read_bytes()).hexdigest());status();assert code==0
 return archive(world)

def archive(world):
 case=STATE['runs'][-1]['case']
 with (OUT/(case+'-save.json')).open('w') as f:subprocess.run(['python3','scripts/inspect-foundation-save.py',str(world/'data/fourthdimension_foundation.dat')],cwd=REPO,stdout=f,check=True)
 with (OUT/(case+'-entities.json')).open('w') as f:subprocess.run(['python3','docs/evidence/integrations/2026-10-01/pax-returning-passengers/inspect-entities.py',str(world)],cwd=REPO,stdout=f,check=True)
 dest=OUT/'worlds'/world.name;dest.parent.mkdir(exist_ok=True);shutil.copytree(world,dest)
 return dest

def prepare(phase):
 world=launch(phase.lower()+'-before','FDPassenger2'+phase.title()+'Before','engineered')
 action('create');step(100)
 for a in ('assemble','equip','board','charge'):action(a)
 step(2);occupied(report('ready'))
 rcon('tick unfreeze');action('dispatch');selected=None
 for i in range(200):
  time.sleep(1);d=occupied(report('prepare_'+str(i)))
  assert d['phase']!='FAULT',d['fault']
  if d['phase']==phase and max(c['speed'] for c in d['cars'])>.2:
   action('trim' if phase=='RETURNING' else 'reverse');rcon('tick freeze');d=occupied(report('before'))
   assert d['phase']==phase;return stop(world),d
  choice={'OUTBOUND':'stop_motor','RETURNING':'trim','CAPTURING':'reverse'}.get(d['phase'])
  if choice and choice!=selected:action(choice);selected=choice
 raise AssertionError('Target phase not observed')

def restore(phase,profile,seed,before):
 case=phase.lower()+'-'+('combined' if ',' in profile else profile)
 world=launch(case,'FDPassenger2'+phase.title()+('Combined' if ',' in profile else 'Engineered'),profile,seed)
 step(100);d=occupied(report('restored'))
 assert d['phase']=='FAULT' and d['interrupted']==phase and d['cycles']==before['cycles']
 assert mounts(d)==mounts(before)
 for key in ('span','controller','observer','carriage'):assert d[key]==before[key],key
 for name in ('dispatch','unload'):
  reply=rcon('fd validation pax_energy_restart '+name);assert 'PAX energy restart fixture:' not in reply
 action('forward');rcon('tick unfreeze')
 if phase=='RETURNING':
  for name in ('resume','recover'):
   reply=rcon('fd validation pax_energy_restart '+name);assert 'PAX energy restart fixture:' not in reply,reply
  for i in range(220):
   time.sleep(1.5);d=occupied(report('brake_'+str(i)));assert d['phase']=='FAULT' and mounts(d)==mounts(before)
   if all(c['ready'] for c in d['cars']):break
  else:raise AssertionError('Gravity fault did not reach readiness')
  action('recover')
 else:
  time.sleep(3);d=occupied(report('held'));assert d['phase']=='FAULT'
  action('resume')
 selected=None
 for i in range(220):
  time.sleep(1);d=occupied(report('cycle_'+str(i)));assert mounts(d)==mounts(before)
  assert d['phase']!='FAULT',d['fault']
  if d['phase']=='BOARDING':
   assert d['cycles']==before['cycles']+1 and all(c['ready'] for c in d['cars']);break
  if d['phase']=='CAPTURING' and selected!='reverse':action('reverse');selected='reverse'
 else:raise AssertionError('Station recovery did not finish')
 action('forward');time.sleep(1);rcon('tick freeze');occupied(report('station'))
 action('unload');step(10);d=report('unloaded');assert d['telemetry']['occupied']==0 and all(not c['restraints'] for c in d['cars'])
 rcon('execute as @e[type=minecraft:pig,tag=fd_pax_energy_restart] run data get entity @s Health')
 stop(world)

RUNTIME.mkdir(parents=True,exist_ok=True)
if (RUNTIME/'server.properties').exists():
 assert 'level-name=FDPassengerUnused' in (RUNTIME/'server.properties').read_text(),'Use only the private validation runtime'
(RUNTIME/'config').mkdir(exist_ok=True)
(RUNTIME/'eula.txt').write_text('eula=true\n')
(RUNTIME/'server.properties').write_text('level-name=FDPassengerUnused\nserver-ip=127.0.0.1\nserver-port=25585\nrcon.port=25586\nrcon.password='+secrets.token_hex(24)+'\nenable-rcon=true\nonline-mode=false\nspawn-protection=0\nview-distance=4\nsimulation-distance=4\nlevel-type=minecraft:flat\ngenerate-structures=false\nallow-flight=true\n')
source=Path('/tmp/fourthdimension-48535694/build/runs/gametest/config/coastersengineered-server.toml')
config=source.read_text();assert 'requireEnergy = false' in config
original_props=(RUNTIME/'server.properties').read_bytes()
(RUNTIME/'config/coastersengineered-server.toml').write_text(config.replace('requireEnergy = false','requireEnergy = true'))
try:
 for phase in ('RETURNING','CAPTURING'):
  seed,before=prepare(phase)
  for profile in ('engineered','engineered,styles,additions,magnetized,immersion'):restore(phase,profile,seed,before)
 STATE['status']='complete';status();(OUT/'packaged.exit').write_text('0\n')
except BaseException as ex:
 STATE['status']='failed';STATE['error']=repr(ex);status()
 if server is not None and server.poll() is None:
  try:
   rcon('tick freeze');rcon('save-all flush');rcon('stop');code=server.wait(timeout=120)
   STATE['runs'][-1]['cleanup_exit']=code;status()
   archive(RUNTIME/STATE['runs'][-1]['world'])
  except BaseException as cleanup:STATE['cleanup_error']=repr(cleanup);status()
 (OUT/'packaged.exit').write_text('1\n');raise

finally:
 if server is None or server.poll() is not None:
  (RUNTIME/'server.properties').write_bytes(original_props)
  (RUNTIME/'config/coastersengineered-server.toml').write_text(config)
  result={}
  for name,data in [('server.properties',original_props),('config/coastersengineered-server.toml',config.encode())]:
   result[name]={'original_sha256':hashlib.sha256(data).hexdigest(),'restored_sha256':hashlib.sha256((RUNTIME/name).read_bytes()).hexdigest()}
  (OUT/'settings-restored.json').write_text(json.dumps(result,indent=2)+'\n')
