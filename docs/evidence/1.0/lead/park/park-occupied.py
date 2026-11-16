"""Finite actual mounted-view segments; helper boarding is disclosed setup, not input acceptance."""
import importlib.util,json,time,sys,subprocess,argparse,math
from pathlib import Path
repo=Path('/home/braydon/Documents/Coasters: Fourth Dimension')
spec=importlib.util.spec_from_file_location('m',repo/'scripts/fd-private-client.py');m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
p=argparse.ArgumentParser();p.add_argument('run');p.add_argument('label');p.add_argument('--budget',type=int,required=True);p.add_argument('--recorder-stem',required=True);p.add_argument('--resume-mounted',action='store_true');p.add_argument('styles',nargs='+');a=p.parse_args();assert 60<=a.budget<=3500
r=m.PrivateRun(a.run,'01a1dd1d-1468-7f81-8f31-5532ad7a178d');r.state('server');r.state('client');r.window()
stream=(r.evidence/(a.label+'-windows.jsonl')).open('x');stop=r.evidence/(a.label+'-resource.stop');assert not stop.exists()
def record(kind,**kw):stream.write(json.dumps({'kind':kind,'unix_ns':time.time_ns(),**kw})+'\n');stream.flush()
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
def state(expected=90):
 d=json.loads(command('fd904 snapshot').strip().removeprefix('FD904_STATE '));assert d['running'] and not d['draining'],d;assert all(not g['fault'] for g in d['groups']),d;assert sum(g['riders'] for g in d['groups'])==expected,d;assert 'Dev' in command('list')
 now=time.monotonic()
 for g in d['groups']:
  old=motion_watch.get(g['name'])
  if g['ready'] or g['stopping'] or old is None or math.dist(old[1],g['center'])>=.05:
   motion_watch[g['name']]=(now,g['center'])
  elif now-old[0]>=60:
   diagnostic=command('fd904 inspect '+g['name'])
   record('stationary_running_group',style=g['name'],seconds=now-old[0],state=g,diagnostic=diagnostic)
   raise RuntimeError(g['name']+' stationary >=60seconds without ready/stopping state')
 return d
def group(d,n):return next(g for g in d['groups'] if g['name']==n)
def snap(n):return r.snapshot(a.label+'-'+n+'-'+str(time.time_ns()))
def shift(n):
 record('unload_expected',style=n,expected='After normal Open, Shift dismounts Dev and reduces the occupied seat count by one')
 result=r.action(argparse.Namespace(action='key',values=['Shift_L'],expect='Dev dismounts the open ready '+n+' seat; occupancy decreases by one',menu=False,modifier=None));record('unload_input',style=n,result=result)
 state(89);spectator();command('execute as Dev run fd904 view '+n);command('fd904 fill '+n);state();command('fd904 play '+n)
# Bounds include headroom over the preserved cycle periods, not new acceptance targets.
return_bounds={'roller_ball':600,'new_generation_flying':600,'dive':540,'powered_inverted_yaw':330}
def return_bound(n):return return_bounds.get(n,240)
def station_ready(n,g):
 if not (g['ready'] and g['stopping']):return False
 if n=='pax':
  answer=command('data get block 544 110 -842 shuttle_phase')
  return '"BOARDING"' in answer
 return True
def wait_ready(n):
 end=time.monotonic()+return_bound(n)
 while True:
  d=state();g=group(d,n)
  if station_ready(n,g):return d
  if time.monotonic()>end:raise TimeoutError(n+' normal return exceeded recorded run bound')
  time.sleep(5)
sampler=None;done=[];start=time.monotonic()
try:
 sampler=subprocess.Popen([sys.executable,str(repo/'scripts/fd-resource-sample.py'),a.run,'--owner',r.c['owner'],'--seconds',str(a.budget),'--stop-file',str(stop),'--output',str(r.evidence/(a.label+'-resources.jsonl'))])
 if not a.resume_mounted:spectator()
 record('segments_start',styles=a.styles,budget_seconds=a.budget)
 for n in a.styles:
  if time.monotonic()-start+2*return_bound(n)+30>a.budget:record('bounded_handoff',completed=done,remaining=a.styles[len(done):]);break
  for side in ['client','server']:
   data=r.evidence/(side+'-'+a.recorder_stem+'.jsonl')
   assert data.exists(),data
   with data.open('rb') as f:
    f.seek(max(0,data.stat().st_size-1000));tail=f.read().decode()
   assert '"kind":"complete"' not in tail,str(data)+' stopped'
   manifest=json.loads(data.open().readline())
   remaining=(manifest['unix_ns']+manifest['seconds']*1_000_000_000-time.time_ns())/1e9
   assert remaining>2*return_bound(n)+30,str(data)+' lacks safe transfer/ride collector time' 
  command('fd904 pause '+n);wait_ready(n)
  if a.resume_mounted and n==a.styles[0]:
   answer=command('execute as Dev on vehicle if entity @s[type=create:seat]');assert 'Test passed, count: 1' in answer,answer
   record('resumed_existing_mount',style=n,meaning='Same owned seat after diagnosed transfer-only setup; stationary prior interval excluded')
  else:
   command('fd904 open '+n);command('gamemode creative Dev');command('execute as Dev run fd904 mount '+n)
  d=state();g=group(d,n);initial=d['tick'];last=g['center'];distance=0.;proof=False
  record('occupied_start',style=n,state=d,screenshot=snap(n+'boarded'));command('fd904 inspect '+n);command('fd904 dispatch '+n)
  if n=='dive':
   after=group(state(),n)
   if after['cycles']==g['cycles'] and after['ready'] and not after['restraints']:
    answer=command('fd904 inspect '+n);line=next(x for x in answer.splitlines() if x.startswith('FD904_DIAGNOSTIC '));diagnostic=json.loads(line.removeprefix('FD904_DIAGNOSTIC '))
    assert diagnostic['authority']['controller']==diagnostic['dock'] and diagnostic['authority']['owner'],diagnostic
    record('transfer_only_readback',style=n,expected='Verified station authority; next dispatch closes restraints and increments cycle',diagnostic=diagnostic)
    command('fd904 dispatch '+n);after=group(state(),n)
   assert after['cycles']==g['cycles']+1 and after['restraints'],after
  deadline=time.monotonic()+return_bound(n);nextshot=time.monotonic()+30
  while True:
   time.sleep(5);d=state();g=group(d,n);distance+=math.dist(last,g['center']);last=g['center'];elapsed=d['tick']-initial
   if n=='dive' and not proof and elapsed>=100 and distance<2 and g['ready']:
    command('fd904 inspect '+n);raise RuntimeError('Dive remained docked after dispatch; diagnose before retry')
   if not proof and elapsed>=100 and distance>=2:
    record('occupied_motion',style=n,game_ticks=elapsed,center_path_distance=distance,state=d,screenshot=snap(n+'moving'));command('fd904 inspect '+n);proof=True
    index=a.styles.index(n)
    if index+1<len(a.styles):
     following=a.styles[index+1];command('fd904 pause '+following);record('next_station_preparation',current_style=n,next_style=following,meaning='Next group finishes its current route and holds ready for safe transfer; other groups keep cycling')
   if time.monotonic()>=nextshot:
    record('occupied_mechanism',style=n,game_ticks=elapsed,state=d,screenshot=snap(n+'mechanism'));command('fd904 inspect '+n);nextshot=time.monotonic()+30;print(n,'occupied ticks',elapsed,flush=True)
   if proof and station_ready(n,g):
    record('occupied_end',style=n,game_ticks=elapsed,center_path_distance=distance,state=d,screenshot=snap(n+'returned'));break
   if time.monotonic()>deadline:raise TimeoutError(n+' occupied safe return exceeded recorded run bound')
  command('fd904 open '+n);shift(n);done.append(n);record('style_complete',style=n);print('Occupied view complete',n,flush=True)
 stop.touch();sampler.wait(timeout=10);assert sampler.returncode==0;record('complete',completed=done,remaining=a.styles[len(done):]);print('Completed occupied styles:',','.join(done),flush=True)
except Exception as e:
 record('failure',error=str(e));command('fd904 drain');stop.touch()
 if sampler is not None:sampler.wait(timeout=10)
 raise
finally:stream.close()
