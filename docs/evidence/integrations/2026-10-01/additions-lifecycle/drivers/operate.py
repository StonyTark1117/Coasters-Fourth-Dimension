from pathlib import Path
import os,sys,time,json,subprocess,re
case=sys.argv[1];os.environ['FD_ADDITIONS_CASE']=case
from rcon import rcon
b=Path(__file__).resolve().parent;d=b/case;pid=int((d/'server.pid').read_text())
def report(label):
 subprocess.run(['python3',str(b/'report.py'),case,label],check=True)
 return json.loads((d/(label+'.json')).read_text())
def tick():return int(re.search(r'The time is (\d+)',rcon('time query gametime')).group(1))
def step(n):
 target=tick()+n;rcon('tick step '+str(n));limit=time.monotonic()+90
 while tick()<target:
  os.kill(pid,0);assert time.monotonic()<limit,'Step observation expired; inspect same server handle';time.sleep(.5)
def action(name):assert 'fixture: '+name in rcon('fd validation additions_restart '+name)
rcon('tick freeze');before=report('before-release')
assert sum(len(c['riders']) for v in before['cars'] for c in v['carriers'])==7
assert all(v['ready'] and v['stopping'] and v['restraints'] and v['channels']==v['received'] for v in before['cars'])
action('release');step(20);delay=report('ready-delay');assert all(v['stopping'] for v in delay['cars'])
step(30);departed=report('departed')
for v in departed['cars']:
 assert v['channels']==v['received'] and v['restraints'] and not v['fault']
 if v['family']=='pax_shuttle':assert v['stopping'] and v['speed']<.15 and 'winch' in v['telemetry']['denial'] and v['channels'][0]==15
 else:assert not v['stopping'] and v['speed']>1 and v['channels'][0]==0
 assert all(r['alive'] and r['carrier']==c['id'] for c in v['carriers'] for r in c['riders'])
action('capture');step(150);captured=report('recaptured');assert all(v['ready'] and v['stopping'] and not v['fault'] for v in captured['cars'])
action('unload');step(20);unloaded=report('unloaded');assert not any(c['riders'] for v in unloaded['cars'] for c in v['carriers'])
rcon('tick unfreeze');print('operation checks passed',flush=True)
