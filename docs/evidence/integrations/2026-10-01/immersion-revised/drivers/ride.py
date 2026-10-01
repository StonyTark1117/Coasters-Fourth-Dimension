from pathlib import Path
import os,sys,subprocess,time,json,re
case=sys.argv[1];os.environ['FD_CAMERA_CASE']=case
from rcon import rcon
b=Path(__file__).resolve().parent;d=b/case
config=Path('/tmp/fourthdimension-48535694/build/runs/packaged-client/config/coasters_immersion-client.toml')
original=(b/'original-coasters_immersion-client.toml').read_text()
def cfg(enabled=True,inertia=40,maximum=False):
 s=original.replace('[camera]\n','[camera]\n')
 start=s.index('[camera]');end=s.index('[telemetry]')
 s=s[:start]+s[start:end].replace('enabled = true','enabled = '+str(enabled).lower(),1)+s[end:]
 s=re.sub(r'head_inertia_ms = \d+',f'head_inertia_ms = {inertia}',s)
 if maximum:
  for key in ['roll_stabilization','pitch_stabilization']:
   s=re.sub(key+r' = \d+',key+' = 100',s)
 config.write_text(s)
 (d/('config-'+str(enabled)+'-'+str(inertia)+'-'+str(maximum)+'.toml')).write_text(s)
def ui(*args):subprocess.run(['python3',str(b/'ui.py'),case,*args],check=True,stdout=subprocess.DEVNULL)
def report(label):
 subprocess.run(['python3',str(b/'report.py'),case,label],check=True)
 return json.loads((d/(label+'.json')).read_text())
def capture(tag):subprocess.run(['python3',str(b/'capture.py'),case,tag],check=True)
def wait(seconds):
 limit=time.monotonic()+seconds
 while time.monotonic()<limit:time.sleep(min(1,limit-time.monotonic()))
def phase(name,seconds):
 print('PHASE',name,flush=True);report(name+'-begin');wait(seconds/2);capture(name);wait(seconds/2);report(name+'-end')
def cmd(text,expected):
 result=rcon(text);assert expected in result,result
cmd('fd validation camera_ride dispatch','Camera ride: dispatch')
phase('default-first',12)
cmd('setblock 444 140 501 minecraft:redstone_block','Changed')
ui('mousemove_relative','--','160','-45');wait(1);ui('mousemove_relative','--','-50','70')
ui('key','F5');phase('default-rear',15)
ui('key','F5');phase('default-front',15)
ui('key','F5');cfg(inertia=0);phase('zero-first',15)
cfg(enabled=False,inertia=0);phase('disabled-first',15)
cfg();phase('restored-first',12)
for i in range(30):
 state=report('return-'+str(i))
 if state['ready'] and state['stopping']:break
 wait(5)
else:raise AssertionError('Return observation expired; inspect live server')
assert not state['fault'],state
capture('captured');cmd('fd validation camera_ride open','Camera ride: open')
ui('keydown','Shift_L');wait(2);ui('keyup','Shift_L');wait(3)
report('first-unloaded');capture('first-unloaded')
cmd('fd validation camera_ride board 3 Dev','Camera ride: boarded');wait(3)
report('second-boarded');capture('second-boarded')
cmd('fd validation camera_ride close','Camera ride: close');cfg(inertia=150,maximum=True)
cmd('fd validation camera_ride dispatch','Camera ride: dispatch')
phase('maximum-first',12);cmd('setblock 444 140 501 minecraft:redstone_block','Changed')
ui('mousemove_relative','--','100','-35');phase('maximum-first-moving',20)
ui('key','F5');phase('maximum-rear',15)
ui('key','F5');phase('maximum-front',15)
ui('key','F5');cfg();phase('restored-second',15)
for i in range(35):
 state=report('second-return-'+str(i))
 if state['ready'] and state['stopping']:break
 wait(5)
else:raise AssertionError('Second return observation expired; inspect live server')
assert not state['fault'],state
capture('second-captured');cmd('fd validation camera_ride open','Camera ride: open')
ui('keydown','Shift_L');wait(2);ui('keyup','Shift_L');wait(3)
report('final-unloaded');capture('final-unloaded')
(d/'player-final.txt').write_text(rcon('data get entity Dev'))
config.write_text(original)
print('COMPLETE',flush=True)
