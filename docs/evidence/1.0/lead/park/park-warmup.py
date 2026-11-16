import importlib.util,json,time,sys,math
from pathlib import Path
spec=importlib.util.spec_from_file_location('m',Path('/home/braydon/Documents/Coasters: Fourth Dimension/scripts/fd-private-client.py'));m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
r=m.PrivateRun(sys.argv[1],'01a1dd1d-1468-7f81-8f31-5532ad7a178d');r.state('server');r.state('client');r.window()
output=r.evidence/sys.argv[2];stream=output.open('x');names=[g['name'] for g in json.loads((r.root/'park.json').read_text())]
def record(kind,**data):stream.write(json.dumps({'kind':kind,'unix_ns':time.time_ns(),**data})+'\n');stream.flush()
def command(cmd):
 ans=m.rcon(r.c,cmd);record('command',command=cmd,answer=ans)
 if 'REJECT' in ans:raise RuntimeError(ans)
 return ans
def spectator():
 command('gamemode spectator Dev')
 answer=command('execute if entity @a[name=Dev,gamemode=spectator]')
 assert 'Test passed, count: 1' in answer,answer
 record('spectator_verified',answer=answer)
motion_watch={};last_centers={};traveled={name:0.0 for name in names}
def check():
 d=json.loads(command('fd904 snapshot').strip().removeprefix('FD904_STATE '))
 assert d['running'] and not d['draining'],d
 assert sum(g['riders'] for g in d['groups'])==90,d
 assert all(not g['fault'] for g in d['groups']),d
 assert 'Dev' in command('list')
 for g in d['groups']:
  n=g['name']
  if n in last_centers:traveled[n]+=math.dist(last_centers[n],g['center'])
  last_centers[n]=g['center']
  now=time.monotonic();old=motion_watch.get(n)
  if g['ready'] or g['stopping'] or old is None or math.dist(old[1],g['center'])>=.05:
   motion_watch[n]=(now,g['center'])
  elif now-old[0]>=60:
   diagnostic=command('fd904 inspect '+n)
   record('stationary_running_group',style=n,seconds=now-old[0],state=g,diagnostic=diagnostic)
   raise RuntimeError(n+' remained stationary >=60seconds without ready/stopping state')
 return d
try:
 spectator()
 for name in names:command('fd904 play '+name)
 command('fd904 start');start=time.monotonic();record('warmup_start')
 for name in names:
  command('execute as Dev run fd904 view '+name);record('view_start',style=name)
  time.sleep(10);check();record('view_end',style=name,screenshot=r.snapshot("park-"+str(time.time_ns())))
  print('Warmed view',name,flush=True)
 command('fd904 pause arrow')
 while time.monotonic()-start<300:
  time.sleep(min(10,300-(time.monotonic()-start)));check()
 d=check()
 for _ in range(12):
  if next(g for g in d['groups'] if g['name']=='arrow')['ready']:break
  time.sleep(10);d=check()
 else:raise RuntimeError('Arrow station wait exceeded120seconds')
 assert all(traveled[n]>=2 for n in names),traveled
 assert all(g['cycles']>0 for g in d['groups']),d
 record('complete',elapsed_seconds=time.monotonic()-start,state=d,observed_center_path=traveled)
 print('Warmup complete',flush=True)
except Exception as e:
 record('failure',error=str(e));command('fd904 drain');raise
finally:stream.close()
