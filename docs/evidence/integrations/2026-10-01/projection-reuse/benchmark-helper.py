from pathlib import Path
import hashlib,json,os,secrets,shutil,socket,struct,subprocess,time
ROOT=Path(__file__).resolve().parents[2];OUT=Path(__file__).resolve().parent
RUNTIME=Path('/tmp/fourthdimension-48535694/build/runs/packaged-server')
JAR=Path('/home/braydon/.cache/fourthdimension-48535694/server-projection-reuse.jar')
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
 with socket.create_connection(('127.0.0.1',int(props['rcon.port'])),timeout=60) as s:
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
 v=rcon('fd validation performance '+a);assert 'Performance fixture: '+a in v,v

def step(n):
 assert 'Stepping' in rcon('tick step '+str(n));time.sleep(n/20+.5)
 end=time.monotonic()+60
 while 'The game is frozen' not in rcon('tick query'):
  assert time.monotonic()<end;time.sleep(.5)

def sample(label):
 assert 'Sampling 600 ordinary' in rcon('fd validation performance sample '+label)
 end=time.monotonic()+150
 while time.monotonic()<end:
  assert server.poll() is None,'Server ended during sample'
  for d in complete_samples(log.read_text()):
    if d['label']==label:
     (OUT/(label+'.json')).write_text(json.dumps(d,indent=2)+'\n')
     print(label,'mean_ms',sum(d['tickNanos'])/len(d['tickNanos'])/1e6,'max_ms',max(d['tickNanos'])/1e6,'phases',d['phases'],flush=True)
     return d
  time.sleep(1)
 raise AssertionError('Sample observation expired')

def launch(case,profile,seed=None):
 global server,log,cmdlog,stream
 assert server is None or server.poll() is not None
 world=RUNTIME/('FDProjectionReuse'+case.replace('-','').title()+('Retry' if case=='base-0-new' else ''));assert not world.exists(),world
 if seed:shutil.copytree(seed,world)
 (RUNTIME/'server.properties').write_text('level-name='+world.name+'\nserver-ip=127.0.0.1\nserver-port=25585\nrcon.port=25586\nrcon.password='+secrets.token_hex(24)+'\nenable-rcon=true\nonline-mode=false\nspawn-protection=0\nview-distance=4\nsimulation-distance=4\nlevel-type=minecraft:flat\ngenerate-structures=false\nallow-flight=true\n')
 log=OUT/(case+'.log');cmdlog=OUT/(case+'-commands.txt');assert not log.exists()
 cmd=['./gradlew','-Dorg.gradle.jvmargs=-Xmx1G','--max-workers=2','runServer','-PfdPackaged','-PfdPackagedJar='+str(JAR),'-PfdValidation','-PfdFrozen']
 if profile:cmd.append('-PfdIntegrations='+profile)
 stream=log.open('w');server=subprocess.Popen(cmd,cwd=ROOT,stdin=subprocess.DEVNULL,stdout=stream,stderr=subprocess.STDOUT,start_new_session=True,env={**os.environ,'JAVA_HOME':'/usr/lib/jvm/java-21-openjdk','JAVA_TOOL_OPTIONS':'-Xmx2G'})
 STATE['runs'].append({'case':case,'profile':profile,'world':world.name,'pid':server.pid,'command':cmd,'status':'running','artifact_sha256':hashlib.sha256(JAR.read_bytes()).hexdigest()});state()
 end=time.monotonic()+180
 while 'RCON running on' not in log.read_text():
  assert server.poll() is None,'Server ended';assert time.monotonic()<end,'Startup observation expired';time.sleep(1)
 return world

def stop(world):
 rcon('save-all flush');rcon('stop');code=server.wait(timeout=120);stream.close();assert code==0
 case=STATE['runs'][-1]['case'];(OUT/(case+'.exit')).write_text(str(code)+'\n');STATE['runs'][-1]['status']='complete';state()
 src=world/'data/fourthdimension_foundation.dat';shutil.copyfile(src,OUT/(case+'-foundation.dat'))
 dest=OUT/'worlds'/world.name;dest.parent.mkdir(exist_ok=True);shutil.copytree(world,dest);return dest

def check(d,occupied):
 for s in (d['before'],d['after']):
  assert len(s['cars'])==(21 if occupied else 0),s
  if occupied:
   assert sum(len(c['carriers']) for c in s['cars'])==48
   assert sum(len(b['riders']) for c in s['cars'] for b in c['carriers'])==96
   for c in s['cars']:
    assert c['loaded'] and not c['fault'] and c['restraints'],c
    assert c['linked']==(7 if c['family']=='ARROW' else 4 if c['family']=='PAX' else 1),c
    for b in c['carriers']:
     assert b['loaded'] and len(b['riders'])==2,b
     assert all(r['alive'] and r['mounted'] and r['carrier']==b['id'] for r in b['riders']),b
 print('integrity',d['label'],'ok',flush=True)

def complete_samples(text):
 for line in reversed(text.splitlines(keepends=True)):
  if line.endswith('\n') and 'FD_PERFORMANCE_SAMPLE {' in line:
   yield json.loads(line.split('FD_PERFORMANCE_SAMPLE ',1)[1])
