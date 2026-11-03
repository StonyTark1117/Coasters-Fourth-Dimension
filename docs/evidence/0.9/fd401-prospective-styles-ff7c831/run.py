from pathlib import Path
import datetime,hashlib,json,subprocess,time
out=Path(__file__).resolve().parent
c=json.loads((out/'config.json').read_text());root=Path(c['root']);inputs=Path(c['inputs'])
def digest(p):
 with p.open('rb') as f:return hashlib.file_digest(f,'sha256').hexdigest()
def now():return datetime.datetime.now(datetime.timezone.utc).isoformat()
for route,prefix in [('single','recovery-bank'),('train','recovery-bank-train')]:
 for style in ['arrow_4d','arrow_4d_dbl_spine']:
  name=route+'-'+style
  frames=inputs/(prefix+'-native-frames.json');trace=inputs/(prefix+'-mesh-trace.jsonl')
  command=[c['python'],str(root/'scripts/audit-recovery-styles.py'),*c['jars'],str(frames),str(trace),'--style',style]
  metadata={'source':c['source'],'scope':c['scope'],'command':command,'cwd':str(root),'started_utc':now(),'input_hashes':{'frames':digest(frames),'trace':digest(trace)},'tool_sha256':digest(root/'scripts/audit-recovery-styles.py'),'numpy':c['numpy']}
  print('START',name,flush=True)
  with (out/(name+'.json')).open('w') as stdout,(out/(name+'.stderr')).open('w') as stderr:
   process=subprocess.Popen(command,cwd=root,stdout=stdout,stderr=stderr)
   metadata['pid']=process.pid
   (out/(name+'.metadata.json')).write_text(json.dumps(metadata,indent=2)+'\n')
   code=process.wait()
  metadata.update({'exit_code':code,'ended_utc':now(),'report_sha256':digest(out/(name+'.json')),'stderr_sha256':digest(out/(name+'.stderr'))})
  (out/(name+'.metadata.json')).write_text(json.dumps(metadata,indent=2)+'\n')
  print('END',name,'exit',code,flush=True)
print('ALL_ANALYSES_TERMINAL',flush=True)
