from pathlib import Path
import os,sys,json,time,subprocess
case=sys.argv[1];os.environ['FD_CAMERA_CASE']=case
from rcon import rcon
b=Path(__file__).resolve().parent;d=b/case;c=json.loads((d/'clients.json').read_text())[0];events=[]
def event(kind,**fields):
 events.append({'kind':kind,'time':time.time(),'offset':(d/'dev.log').stat().st_size,**fields});(d/'group-events.json').write_text(json.dumps(events,indent=2)+'\n')
def ui(*args):
 assert Path('/proc',str(c['display_pid']),'comm').read_text().strip()=='Xvfb'
 subprocess.run(['xdotool',*args],env={**os.environ,'DISPLAY':c['display']},check=True)
def sample():
 for line in reversed((d/'dev.log').read_text(errors='replace').splitlines()):
  if 'FD_CAMERA_GROUP {' in line:return json.loads(line.split('FD_CAMERA_GROUP ',1)[1])
 raise AssertionError('No camera group sample')
def mode(name):
 for i in range(7):
  if sample()['mode']==name:break
  ui('key','F5');time.sleep(.7)
 else:raise AssertionError('Camera mode not observed')
 event('mode',mode=name,sample=sample())
def capture(name):subprocess.run(['python3',str(b/'capture.py'),case,name],check=True)
def phase(name):
 event('phase-start',name=name);time.sleep(3);capture(name);time.sleep(3);event('phase-end',name=name,sample=sample())
def cmd(s,expected):
 answer=rcon(s);assert expected in answer,answer
cmd('fd validation camera_ride board 0 Dev','boarded');time.sleep(3)
cmd('fd validation camera_ride close','close');mode('FIRST_PERSON');phase('first')
for name in ['SUB_LEVEL_VIEW','THIRD_PERSON_BACK','SUB_LEVEL_VIEW_UNLOCKED','THIRD_PERSON_FRONT']:
 mode(name);phase(name)
mode('SUB_LEVEL_VIEW');ui('click','--repeat','20','--delay','30','5');time.sleep(2);phase('orbit-zoom')
# Live collider control is only meaningful once the camera can leave the car.
if case!='before':
 row=sample();assert row['zoom']>3.9 and row['distance']>15,row
 import math
 pos=[row['camera'+axis] for axis in ['X','Y','Z']];center=[row['center'+axis] for axis in ['X','Y','Z']]
 delta=[(a-b)/row['distance'] for a,b in zip(pos,center)];wall=[math.floor(a+12*v) for a,v in zip(center,delta)]
 axis=0 if abs(delta[0])>abs(delta[2]) else 2
 lo=[wall[i]-(0 if i==axis else 4) for i in range(3)];hi=[wall[i]+(0 if i==axis else 4) for i in range(3)]
 bounds=' '.join(map(str,lo+hi));event('wall-create',bounds=bounds,sample=row)
 cmd('fill '+bounds+' minecraft:stone','filled');phase('wall')
 assert sample()['distance']<row['distance']-4,sample()
 cmd('fill '+bounds+' minecraft:air','filled');phase('wall-removed')
 assert sample()['distance']>15,sample()
ui('click','--repeat','20','--delay','30','4');time.sleep(2);mode('FIRST_PERSON')
cmd('fd validation camera_ride open','open');ui('keydown','Shift_L');time.sleep(2);ui('keyup','Shift_L');time.sleep(3)
subprocess.run(['python3',str(b/'report.py'),case,'unloaded'],check=True);capture('unloaded')
(d/'player-final.txt').write_text(rcon('data get entity Dev'))
event('complete');print('COMPLETE',flush=True)
