from pathlib import Path
import os,sys,subprocess,time,json
case=sys.argv[1];os.environ['FD_ADDITIONS_CASE']=case
from rcon import rcon
b=Path(__file__).resolve().parent;d=b/case
def ui(*args):subprocess.run(['python3',str(b/'ui.py'),case,*args],check=True)
def capture(tag):subprocess.run(['python3',str(b/'capture.py'),case,tag],check=True)
ui('key','Escape');time.sleep(.5);capture('pause')
ui('mousemove','639','505','click','1');time.sleep(1)
assert 'There are 0 of' in rcon('list')
capture('disconnected');ui('mousemove','871','560','click','1');time.sleep(.5);capture('title')
ui('mousemove','740','536','click','1')
rcon('save-all flush');rcon('stop')
limit=time.monotonic()+90
for name in ['dev','observer','server']:
 while not (d/(name+'.exit')).exists():
  assert time.monotonic()<limit,'Exit observation expired; inspect original handles';time.sleep(.5)
 assert (d/(name+'.exit')).read_text().strip()=='0'
subprocess.run(['python3',str(b/'retain-world.py'),case],check=True)
print('clean stop and world retained',flush=True)
