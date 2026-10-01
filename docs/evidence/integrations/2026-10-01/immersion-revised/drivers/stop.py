from pathlib import Path
import os,sys,subprocess,time,json,signal,shutil
case=sys.argv[1];os.environ['FD_CAMERA_CASE']=case
from rcon import rcon
b=Path(__file__).resolve().parent;d=b/case
clients=json.loads((d/'clients.json').read_text())
def ui(*args):subprocess.run(['python3',str(b/'ui.py'),case,*args],check=True)
def cap(tag):subprocess.run(['python3',str(b/'capture.py'),case,tag],check=True)
assert json.loads((d/'accept-events.json').read_text())[-1]['kind']=='complete'
ui('key','Escape');time.sleep(.5);cap('pause')
ui('mousemove','639','505','click','1');time.sleep(1)
assert 'There are 0 of' in rcon('list')
cap('disconnected');ui('mousemove','871','560','click','1');time.sleep(.5);cap('title')
ui('mousemove','740','536','click','1')
rcon('save-all flush');rcon('stop')
limit=time.monotonic()+90
for name in ['server']+[c['label'] for c in clients]:
 while not (d/(name+'.exit')).exists():
  assert time.monotonic()<limit,'Exit observation expired; inspect original handles';time.sleep(.5)
 assert (d/(name+'.exit')).read_text().strip()=='0'
m=json.loads((d/'server.json').read_text());assert not Path('/proc',str(m['pid'])).exists()
for c in clients:
 assert not Path('/proc',str(c['wrapper_pid'])).exists()
 assert Path('/proc',str(c['display_pid']),'comm').read_text().strip()=='Xvfb'
 os.kill(c['display_pid'],signal.SIGTERM)
shutil.copytree(Path('/tmp/fourthdimension-48535694/build/runs/packaged-server')/m['world'],d/'world')
print('clean stop and world retained',flush=True)
