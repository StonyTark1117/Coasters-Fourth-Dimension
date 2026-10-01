from pathlib import Path
import gzip,hashlib,json,os,secrets,shutil,socket,struct,subprocess,time
ROOT=Path(__file__).resolve().parents[2];OUT=Path(__file__).resolve().parent
RUNTIME=Path('/tmp/fourthdimension-48535694/build/runs/packaged-server')
JAR=Path('/home/braydon/.cache/fourthdimension-48535694/magnetized-lifecycle.jar')
source=Path('/tmp/fourthdimension-48535694/build/libs/coasters-fourth-dimension-0.9.0-dev.1.jar')
if JAR.exists():assert JAR.read_bytes()==source.read_bytes()
else:shutil.copyfile(source,JAR)
original=(RUNTIME/'server.properties').read_bytes();(OUT/'server.properties.original').write_bytes(original)
STATE={'status':'starting','artifact_sha256':hashlib.sha256(JAR.read_bytes()).hexdigest(),'runs':[]}
server=None;log=None;cmdlog=None;stream=None

def state(): (OUT/'status.json').write_text(json.dumps(STATE,indent=2)+'\n')
def rcon(cmd):
 props=dict(l.split('=',1) for l in (RUNTIME/'server.properties').read_text().splitlines() if '=' in l and not l.startswith('#'))
 def packet(n,t,text):
  b=struct.pack('<ii',n,t)+text.encode()+b'\0\0';return struct.pack('<i',len(b))+b
 with socket.create_connection(('127.0.0.1',int(props['rcon.port'])),timeout=10) as s:
  def read():
   def exact(n):
    b=b''
    while len(b)<n:
     v=s.recv(n-len(b))
     if not v:raise ConnectionError('RCON closed')
     b+=v
    return b
   size=struct.unpack('<i',exact(4))[0];b=exact(size);return struct.unpack('<ii',b[:8]),b[8:-2].decode()
  s.sendall(packet(11,3,props['rcon.password']));h,_=read();assert h[0]==11
  s.sendall(packet(12,2,cmd));h,out=read();assert h[0]==12
 with cmdlog.open('a') as f:f.write(cmd+': '+out+'\n\n')
 assert 'Unknown or incomplete' not in out and 'unexpected error' not in out,out
 return out

def action(a):
 v=rcon('fd validation magnetized_restart '+a);assert 'Equipment lifecycle fixture: '+a in v,v

def step(n):
 assert 'Stepping' in rcon('tick step '+str(n));time.sleep(n/20+.5)
 end=time.monotonic()+30
 while 'The game is frozen' not in rcon('tick query'):
  assert time.monotonic()<end;time.sleep(.5)

def report(label):
 assert 'snapshot recorded' in rcon('fd validation magnetized_restart report '+label)
 end=time.monotonic()+10
 while time.monotonic()<end:
  for l in reversed(log.read_text().splitlines()):
   if 'FD_EQUIPMENT_LIFECYCLE {' in l:
    d=json.loads(l.split('FD_EQUIPMENT_LIFECYCLE ',1)[1])
    if d['label']==label:
     (OUT/(STATE['runs'][-1]['case']+'-'+label+'.json')).write_text(json.dumps(d,indent=2)+'\n');return d
  time.sleep(.05)
 raise AssertionError('Report missing')

def check(d,occupied=True):
 assert len(d['anchors'])==10 and all(a['powered'] and a['nbt'] for a in d['anchors'])
 assert len(d['cars'])==5
 for c in d['cars']:
  assert c['ready'] and c['stopping'] and not c['fault'] and c['restraints']==occupied and not c['externalDriveAllowed'],c
  assert all(b['loaded'] and b['capture'] for b in c['carriers'])
  riders=[r for b in c['carriers'] for r in b['riders']]
  assert len(riders)==int(occupied),c
  assert all(r['alive'] and r['mounted'] and r['carrier']==b['body'] for b in c['carriers'] for r in b['riders'])
 print(STATE['runs'][-1]['case'],d['label'],'occupied',occupied,'addon',d['magnetized'],flush=True)

def launch(case,profile,seed=None):
 global server,log,cmdlog,stream
 assert server is None or server.poll() is not None
 world=RUNTIME/('FDMagnetizedLifecycle'+case.replace('-','').title());assert not world.exists(),world
 if seed:shutil.copytree(seed,world)
 (RUNTIME/'server.properties').write_text('level-name='+world.name+'\nserver-ip=127.0.0.1\nserver-port=25585\nrcon.port=25586\nrcon.password='+secrets.token_hex(24)+'\nenable-rcon=true\nonline-mode=false\nspawn-protection=0\nview-distance=4\nsimulation-distance=4\nlevel-type=minecraft:flat\ngenerate-structures=false\nallow-flight=true\n')
 log=OUT/(case+'.log');cmdlog=OUT/(case+'-commands.txt');assert not log.exists()
 cmd=['./gradlew','-Dorg.gradle.jvmargs=-Xmx1G','--max-workers=2','runServer','-PfdPackaged','-PfdPackagedJar='+str(JAR),'-PfdValidation','-PfdFrozen']
 if profile:cmd.append('-PfdIntegrations='+profile)
 stream=log.open('w');server=subprocess.Popen(cmd,cwd=ROOT,stdin=subprocess.DEVNULL,stdout=stream,stderr=subprocess.STDOUT,start_new_session=True,env={**os.environ,'JAVA_HOME':'/usr/lib/jvm/java-21-openjdk','JAVA_TOOL_OPTIONS':'-Xmx2G'})
 STATE['runs'].append({'case':case,'profile':profile,'world':world.name,'pid':server.pid,'command':cmd,'status':'running'});state()
 end=time.monotonic()+180
 while 'RCON running on' not in log.read_text():
  assert server.poll() is None,'Server ended';assert time.monotonic()<end,'Startup observation expired';time.sleep(1)
 return world

def stop(world):
 rcon('save-all flush');rcon('stop');code=server.wait(timeout=120);stream.close();assert code==0
 case=STATE['runs'][-1]['case'];(OUT/(case+'.exit')).write_text(str(code)+'\n');STATE['runs'][-1]['status']='complete';state()
 for name in ('fourthdimension_foundation','coastersmagnetized_magnetized_anchors'):
  src=world/'data'/(name+'.dat')
  shutil.copyfile(src,OUT/(case+'-'+name+'.dat'))
  with (OUT/(case+'-'+name+'.json')).open('w') as f:subprocess.run(['python3','scripts/inspect-foundation-save.py',str(src)],cwd=ROOT,stdout=f,check=True)
 with (OUT/(case+'-entities.json')).open('w') as f:subprocess.run(['python3',str(OUT/'inspect-entities.py'),str(world)],cwd=ROOT,stdout=f,check=True)
 dest=OUT/'worlds'/world.name;dest.parent.mkdir(exist_ok=True);shutil.copytree(world,dest);return dest

try:
 with socket.socket() as probe:assert probe.connect_ex(('127.0.0.1',25585))!=0,'Private port already active'
 world=launch('seed','magnetized');action('create');step(100);action('cars');step(100);action('board');step(5);baseline=report('occupied');check(baseline);seed=stop(world)
 combined='engineered,styles,additions,magnetized,immersion'
 cases=[('control','magnetized','seed'),('removed',None,'seed'),('reinstalled','magnetized','removed'),('combined',combined,'seed'),('combined-removed','engineered,styles,additions,immersion','combined'),('combined-reinstalled',combined,'combined-removed')]
 worlds={'seed':seed}
 for case,profile,parent in cases:
  world=launch(case,profile,worlds[parent]);step(200);d=report('restored');check(d)
  if 'reinstalled' in case:action('unload');step(10);check(report('unloaded'),False)
  worlds[case]=stop(world)
 STATE['status']='complete';state();(OUT/'run.exit').write_text('0\n')
except BaseException as e:
 STATE['status']='failed';STATE['error']=repr(e);state();(OUT/'run.exit').write_text('1\n')
 if server is not None and server.poll() is None:
  try:rcon('stop');server.wait(timeout=120)
  except BaseException as cleanup:STATE['cleanup_error']=repr(cleanup);state()
 raise
finally:
 if server is None or server.poll() is not None:
  (RUNTIME/'server.properties').write_bytes(original)
  (OUT/'settings-restored.json').write_text(json.dumps({'before':hashlib.sha256(original).hexdigest(),'after':hashlib.sha256((RUNTIME/'server.properties').read_bytes()).hexdigest()},indent=2)+'\n')
