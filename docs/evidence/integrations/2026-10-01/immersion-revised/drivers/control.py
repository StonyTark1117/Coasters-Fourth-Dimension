from pathlib import Path
import os,sys,subprocess,time,json,re
case=sys.argv[1];os.environ['FD_CAMERA_CASE']=case
from rcon import rcon
b=Path(__file__).resolve().parent;d=b/case
clients=json.loads((d/'clients.json').read_text())
base=Path('/tmp/fourthdimension-48535694/build/runs')
events=[]
def event(kind,**fields):
 events.append({'time':time.time(),'kind':kind,'offsets':{c['label']:(d/(c['label']+'.log')).stat().st_size for c in clients},**fields});(d/'control-events.json').write_text(json.dumps(events,indent=2)+'\n')
def sample(label):
 lines=(d/(label+'.log')).read_text(errors='replace').splitlines()
 line=next(x for x in reversed(lines) if 'FD_IMMERSION ' in x)
 return dict(re.findall(r'(\w+)=([^ ]+)',line.split(' camera=',1)[0]))
def ui(client,*args):
 assert Path('/proc',str(client['display_pid']),'comm').read_text().strip()=='Xvfb'
 assert Path('/proc',str(client['wrapper_pid'])).exists()
 subprocess.run(['xdotool',*args],env={**os.environ,'DISPLAY':client['display']},check=True)
def mode(name):
 for client in clients:
  for i in range(7):
   if sample(client['label'])['perspective']==name:break
   ui(client,'key','F5');time.sleep(.7)
  else:raise AssertionError(('Cannot select perspective',name,sample(client['label'])))
 event('mode',mode=name,samples={c['label']:sample(c['label']) for c in clients})
def cfg(enabled=True,inertia=40,maximum=False):
 for c in clients:
  original=(b/('original-'+c['label']+'-coasters_immersion-client.toml')).read_text()
  start=original.index('[camera]');end=original.index('[telemetry]')
  s=original[:start]+original[start:end].replace('enabled = true','enabled = '+str(enabled).lower(),1)+original[end:]
  s=re.sub(r'head_inertia_ms = \d+',f'head_inertia_ms = {inertia}',s)
  if maximum:
   for key in ['roll_stabilization','pitch_stabilization']:
    s=re.sub(key+r' = \d+',key+' = 100',s)
  config=base/('packaged-client' if c['label']=='dev' else 'packaged-observer')/'config/coasters_immersion-client.toml'
  config.write_text(s);(d/('accept-config-'+c['label']+'-'+str(enabled)+'-'+str(inertia)+'-'+str(maximum)+'.toml')).write_text(s)
 limit=time.monotonic()+12
 while True:
  states={c['label']:sample(c['label']) for c in clients}
  # The diagnostic reports configured inertia even when processing is disabled.
  expected=str(inertia/1000)
  if all(r['enabled']==str(enabled).lower() and r['inertia']==expected for r in states.values()):break
  assert time.monotonic()<limit,('Config not observed',states);time.sleep(.25)
 event('config',enabled=enabled,inertia=inertia,maximum=maximum,samples=states)
def report(label):
 subprocess.run(['python3',str(b/'report.py'),case,'accept-'+label],check=True)
 return json.loads((d/('accept-'+label+'.json')).read_text())
def capture(tag):subprocess.run(['python3',str(b/'capture.py'),case,'accept-'+tag],check=True)
def phase(name,seconds):
 event('phase-start',name=name);report(name+'-begin');time.sleep(seconds/2);capture(name);time.sleep(seconds/2);report(name+'-end');event('phase-end',name=name)
def cmd(text,expected):
 result=rcon(text);assert expected in result,result

def board(second=False):
 for c in clients:
  index=({'dev':3,'observer':1} if second else {'dev':0,'observer':2})[c['label']]
  cmd('fd validation camera_ride board '+str(index)+' '+c['label'].title(),'Camera ride: boarded')
 time.sleep(3);mode('FIRST_PERSON');cfg();report('second-boarded' if second else 'boarded')
 cmd('fd validation camera_ride close','Camera ride: close')
def returned(name):
 for i in range(35):
  state=report(name+'-return-'+str(i))
  if state['ready'] and state['stopping']:break
  time.sleep(4)
 else:raise AssertionError('Return observation expired; inspect original live handles')
 assert not state['fault'],state
 assert sum(len(c['riders']) for c in state['carriers'])==len(clients),state
 mode('FIRST_PERSON');capture(name+'-captured')
 cmd('fd validation camera_ride open','Camera ride: open')
 for c in clients:ui(c,'keydown','Shift_L')
 time.sleep(2)
 for c in clients:ui(c,'keyup','Shift_L')
 time.sleep(3)
 state=report(name+'-unloaded');assert not any(c['riders'] for c in state['carriers'])
 for c in clients:
  s=sample(c['label']);assert s['body']=='none' and s['riding']=='false',s
  (d/('accept-'+name+'-'+c['label']+'-player.txt')).write_text(rcon('data get entity '+c['label'].title()))
 capture(name+'-unloaded');event('unloaded',name=name,samples={c['label']:sample(c['label']) for c in clients})

cmd('tick freeze','frozen')
for enabled in [True,False]:
 cfg(enabled=enabled,inertia=40)
 for name in ['SUB_LEVEL_VIEW','THIRD_PERSON_BACK','SUB_LEVEL_VIEW_UNLOCKED','THIRD_PERSON_FRONT']:
  mode(name);time.sleep(2);capture('control-'+str(enabled)+'-'+name)
  event('capture',name=name,enabled=enabled,samples={c['label']:sample(c['label']) for c in clients})
  if name=='SUB_LEVEL_VIEW':
   for c in clients:ui(c,'click','--repeat','20','--delay','30','5')
   time.sleep(2);capture('control-'+str(enabled)+'-zoom-out')
   event('zoom-out',enabled=enabled)
   for c in clients:ui(c,'click','--repeat','20','--delay','30','4')
   time.sleep(2)
mode('FIRST_PERSON');cfg();cmd('tick unfreeze','running normally')
cmd('fd validation camera_ride open','Camera ride: open')
for c in clients:ui(c,'keydown','Shift_L')
time.sleep(2)
for c in clients:ui(c,'keyup','Shift_L')
time.sleep(3);report('control-unloaded');capture('control-unloaded')
event('complete');print('CONTROL COMPLETE',flush=True)
