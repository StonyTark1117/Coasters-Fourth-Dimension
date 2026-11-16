import importlib.util,json,time,sys,subprocess,math
from pathlib import Path
repo=Path('/home/braydon/Documents/Coasters: Fourth Dimension')
spec=importlib.util.spec_from_file_location('m',repo/'scripts/fd-private-client.py');m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
r=m.PrivateRun(sys.argv[1],'01a1dd1d-1468-7f81-8f31-5532ad7a178d');r.state('server');r.state('client');r.window()
label=sys.argv[2];seconds=int(sys.argv[3]);assert seconds in (300,1800)
recorder_stem=sys.argv[4] if len(sys.argv)>4 else label.split('-')[0]+'11'
tick_limit=float(sys.argv[5]) if len(sys.argv)>5 else 50.0
stream=(r.evidence/(label+'-windows.jsonl')).open('x');names=[g['name'] for g in json.loads((r.root/'park.json').read_text())]
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
motion_watch={}
def check():
 d=json.loads(command('fd904 snapshot').strip().removeprefix('FD904_STATE '))
 assert d['running'] and not d['draining'],d
 assert sum(g['riders'] for g in d['groups'])==90,d
 assert all(not g['fault'] for g in d['groups']),d
 assert 'Dev' in command('list')
 now=time.monotonic()
 for g in d['groups']:
  n=g['name'];old=motion_watch.get(n)
  if g['ready'] or g['stopping'] or old is None or math.dist(old[1],g['center'])>=.05:
   motion_watch[n]=(now,g['center'])
  elif now-old[0]>=60:
   diagnostic=command('fd904 inspect '+n)
   record('stationary_running_group',style=n,seconds=now-old[0],state=g,diagnostic=diagnostic)
   raise RuntimeError(n+' remained stationary >=60seconds without ready/stopping state')
 return d
sampler=None
resource_stop=r.evidence/(label+'-resources.stop')
assert not resource_stop.exists()
try:
 spectator()
 d=check();arrow=next(g for g in d['groups'] if g['name']=='arrow');assert arrow['ready'] and arrow['held'],arrow
 for part in ['client','server']:
  data=r.evidence/(part+'-'+recorder_stem+'.jsonl')
  assert data.exists(),data
  assert '"kind":"complete"' not in data.read_text()[-500:],str(data)+' already stopped'
 sampler=subprocess.Popen([sys.executable,str(repo/'scripts/fd-resource-sample.py'),sys.argv[1],'--owner',r.c['owner'],'--seconds',str(seconds),'--output',str(r.evidence/(label+'-resources.jsonl')),'--stop-file',str(resource_stop)])
 start=time.monotonic();start_ns=time.time_ns();record('window_start',duration_seconds=seconds,state=d)
 for name in names:
  command('execute as Dev run fd904 view '+name);record('view_start',style=name,view='park')
  time.sleep(10);check();record('view_end',style=name,screenshot=r.snapshot("park-"+str(time.time_ns())));print('Measured park view',name,flush=True)
 command('gamemode creative Dev');command('fd904 open arrow');command('execute as Dev run fd904 mount arrow');command('fd904 play arrow');command('fd904 dispatch arrow')
 record('occupied_view_start',style='arrow',screenshot=r.snapshot("park-"+str(time.time_ns())));check()
 comparison=False;occupied_proof=False
 while time.monotonic()-start<seconds:
  elapsed=time.monotonic()-start
  time.sleep(min(10,seconds-elapsed));d=check();elapsed=time.monotonic()-start
  if not occupied_proof:record('occupied_motion_proof',style='arrow',screenshot=r.snapshot("park-"+str(time.time_ns())),state=d);occupied_proof=True
  if elapsed>=300 and not comparison:
   record('comparison_window_end',state=d);comparison=True
   ticks=[]
   for line in (r.evidence/('server-'+recorder_stem+'.jsonl')).read_text().splitlines():
    try: row=json.loads(line)
    except json.JSONDecodeError: continue
    if row.get('kind')=='tick' and start_ns<=row.get('unix_ns',0)<start_ns+300_000_000_000: ticks.append(row['duration_ns']/1e6)
   ticks.sort();p95=ticks[math.ceil(.95*len(ticks))-1]
   record('first300_budget',tick_count=len(ticks),tick_p95_ms=p95,threshold_ms=tick_limit)
   print('First300 tick p95',p95,'ms; ticks',len(ticks),flush=True)
   if p95>tick_limit:raise RuntimeError('First300 p95 server tick budget failed: '+str(p95)+'ms')
  if int(elapsed)//60 != int(elapsed-10)//60:print('Measured seconds',round(elapsed),flush=True)
 record('window_end',state=d,screenshot=r.snapshot("park-"+str(time.time_ns())),elapsed_seconds=time.monotonic()-start)
 sampler.wait(timeout=15);assert sampler.returncode==0,sampler.returncode
 record('complete');print('Measurement complete',flush=True)
except Exception as e:
 record('failure',error=str(e));command('fd904 drain');raise
finally:
 if sampler is not None and sampler.poll() is None:
  resource_stop.touch();sampler.wait(timeout=15)
 stream.close()
