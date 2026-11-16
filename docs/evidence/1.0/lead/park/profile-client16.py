"""One bounded diagnostic of the observed high-cost Pipeline view; not acceptance timing."""
from pathlib import Path
import importlib.util,json,time,subprocess,xml.etree.ElementTree as ET
base=Path('/mnt/NVME4/fourthdimension-runs/fd10-lead-20261115');root=base/'park-combined01';e=root/'evidence';repo=Path('/home/braydon/Documents/Coasters: Fourth Dimension')
spec=importlib.util.spec_from_file_location('m',repo/'scripts/fd-private-client.py');m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m);r=m.PrivateRun(root/'run.json','01a1dd1d-1468-7f81-8f31-5532ad7a178d');r.status()
spec=importlib.util.spec_from_file_location('s',repo/'scripts/fd-resource-sample.py');s=importlib.util.module_from_spec(spec);spec.loader.exec_module(s);pid=s.game_pid(r.state('client'))
f=(e/'client16-pipeline-profile.jsonl').open('x')
def record(kind,**kw):f.write(json.dumps({'kind':kind,'unix_ns':time.time_ns(),**kw})+'\n');f.flush()
def cmd(c):
 ans=m.rcon(r.c,c);record('command',command=c,answer=ans);assert 'REJECT' not in ans,ans;return ans
cmd('gamemode spectator Dev');assert 'Test passed, count: 1' in cmd('execute if entity @a[name=Dev,gamemode=spectator]')
cmd('fd904 play arrow');cmd('execute as Dev run fd904 view pipeline');time.sleep(5)
d=json.loads(cmd('fd904 snapshot').strip().removeprefix('FD904_STATE '));assert d['running'] and not d['draining'] and sum(g['riders'] for g in d['groups'])==90
profile=e/'client16-profile.jfc';tree=ET.parse('/usr/lib/jvm/java-21-openjdk/lib/jfr/profile.jfc')
for event in tree.getroot().findall('event'):
 if event.attrib.get('name') in ['jdk.ThreadDump','jdk.ThreadCPULoad']:
  for setting in event.findall('setting'):
   if setting.attrib.get('name')=='period':setting.text='1 s';setting.attrib.pop('control',None)
tree.write(profile,encoding='UTF-8',xml_declaration=True)
jfr=e/'client16-pipeline60.jfr';assert not jfr.exists()
record('diagnostic_start',pid=pid,screenshot=r.snapshot('client16-pipeline-profile-start'),settings=str(profile),meaning='Separate diagnostic, excluded from mounted segment summaries and acceptance frame comparisons')
record('jcmd_start',output=subprocess.check_output(['/usr/lib/jvm/java-21-openjdk/bin/jcmd',pid,'JFR.start','name=fd16-pipeline-diagnostic','settings='+str(profile),'duration=60s','filename='+str(jfr)],text=True))
paths=[Path('/sys/class/drm/card1/device')/n for n in ['gpu_busy_percent','mem_busy_percent','pp_dpm_sclk','pp_dpm_mclk']]
paths += list(Path('/sys/class/drm/card1/device/hwmon').glob('hwmon*/power1_average'))+list(Path('/sys/class/drm/card1/device/hwmon').glob('hwmon*/temp1_input'))
start=time.monotonic()
for i in range(61):
 fields={}
 for p in paths:
  try:fields[str(p)]=p.read_text().strip()
  except OSError as error:fields[str(p)]=str(error)
 threads=[]
 for p in Path('/proc',pid,'task').iterdir():
  try:
   name=(p/'comm').read_text().strip()
   if name=='Render thread':threads.append({'tid':p.name,'name':name,'stat':(p/'stat').read_text()})
  except OSError:pass
 record('resource',gpu=fields,render_thread=threads)
 if i in (0,30,60):
  d=json.loads(cmd('fd904 snapshot').strip().removeprefix('FD904_STATE '));assert sum(g['riders'] for g in d['groups'])==90 and all(not g['fault'] for g in d['groups'])
 if i<60:time.sleep(max(0,start+i+1-time.monotonic()))
for _ in range(15):
 if jfr.exists() and jfr.stat().st_size>0:
  check=subprocess.run(['/usr/lib/jvm/java-21-openjdk/bin/jfr','summary',str(jfr)],text=True,capture_output=True)
  if check.returncode==0:record('jfr_verified',summary=check.stdout);break
 time.sleep(1)
else:raise RuntimeError('Diagnostic JFR did not finish flushing')
record('complete',jfr=str(jfr),duration_seconds=time.monotonic()-start);f.close();print('Pipeline60 diagnostic complete',flush=True)
