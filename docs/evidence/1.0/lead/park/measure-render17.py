from pathlib import Path
import json,time,subprocess,sys,importlib.util,argparse
p=argparse.ArgumentParser();p.add_argument('root',type=Path);p.add_argument('stem');p.add_argument('baseline',type=Path);a=p.parse_args()
baseline=json.loads(a.baseline.read_text());tick_limit=baseline['sources']['tick:server']['p95']*1.1
base=a.root.parent;e=a.root/'evidence';repo=Path('/home/braydon/Documents/Coasters: Fourth Dimension');label='comparison-'+a.stem
for _ in range(100):
 rows=[json.loads(x) for x in (e/('warmup-'+a.stem+'.jsonl')).read_text().splitlines()]
 if rows and rows[-1]['kind'] in ['complete','failure']:
  assert rows[-1]['kind']=='complete',rows[-1];break
 time.sleep(5)
else:raise TimeoutError('Warmup did not finish')
other='fd10-lead-park-combined01-server.service' if a.stem.startswith('base') else 'fd10-lead-park-base02-server.service'
state=subprocess.check_output(['systemctl','--user','show',other,'-p','ActiveState','--value'],text=True).strip();assert state=='inactive',(other,state)
result=subprocess.run([sys.executable,str(base/'park-measure.py'),str(a.root/'run.json'),label,'300',a.stem,str(tick_limit)])
spec=importlib.util.spec_from_file_location('m',repo/'scripts/fd-private-client.py');m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m);r=m.PrivateRun(a.root/'run.json','01a1dd1d-1468-7f81-8f31-5532ad7a178d');r.state('server')
answer=m.rcon(r.c,'fd904 drain');(e/('drain-'+a.stem+'.json')).write_text(json.dumps({'unix_ns':time.time_ns(),'answer':answer},indent=2)+'\n')
for side in ['client','server']:(e/(side+'-'+a.stem+'.jsonl.stop')).touch()
for _ in range(18):
 footers=[]
 for side in ['client','server']:
  path=e/(side+'-'+a.stem+'.jsonl')
  with path.open('rb') as f:f.seek(max(0,path.stat().st_size-2000));lines=f.read().decode().splitlines()
  try:footers.append(json.loads(lines[-1]))
  except (ValueError,IndexError):footers.append({})
 if all(x.get('kind')=='complete' for x in footers):break
 time.sleep(5)
else:raise TimeoutError('Recorders did not finish')
assert all(x.get('lossless') and x.get('dropped_events')==0 for x in footers),footers
rows=[json.loads(x) for x in (e/(label+'-windows.jsonl')).read_text().splitlines()];start=next(x['unix_ns'] for x in rows if x['kind']=='window_start')
subprocess.run([sys.executable,str(repo/'scripts/fd-performance-summary.py'),str(e/('client-'+a.stem+'.jsonl')),str(e/('server-'+a.stem+'.jsonl')),str(e/(label+'-resources.jsonl')),'--start-unix-ns',str(start),'--end-unix-ns',str(start+300_000_000_000),'--output',str(e/(label+'-summary.json'))],check=True)
s=json.loads((e/(label+'-summary.json')).read_text());baseline=json.loads(a.baseline.read_text());checks=[]
for source in ['frame:client','tick:server']:
 for metric in ['p95','p99']:
  limit=baseline['sources'][source][metric]*1.1
  value=s['sources'][source][metric];checks.append({'source':source,'metric':metric,'actual':value,'limit':limit,'pass':value<=limit})
for side in ['client','server']:
 for source,metric in [('resource:'+side,'rss_bytes'),('post_gc:'+side,None)]:
  original=baseline['sources'][source];current=s['sources'][source]
  if metric:original=original[metric];current=current[metric]
  limit=original['median']*1.1;value=current['median'];checks.append({'source':source,'metric':('rss_bytes:' if metric else '')+'median','actual':value,'limit':limit,'pass':value<=limit})
verdict={'baseline':str(a.baseline),'complete_measurement':result.returncode==0,'checks':checks,'all_pass':result.returncode==0 and all(x['pass'] for x in checks),'footers':footers}
(e/(label+'-verdict.json')).write_text(json.dumps(verdict,indent=2)+'\n');print(json.dumps(verdict,indent=2),flush=True)
