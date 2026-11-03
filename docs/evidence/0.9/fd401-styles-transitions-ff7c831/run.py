from pathlib import Path
import ast,datetime,hashlib,json,subprocess
out=Path(__file__).resolve().parent
c=json.loads((out/'config.json').read_text());root=Path(c['root']);inputs=Path(c['inputs'])
def digest(p):
 with p.open('rb') as f:return hashlib.file_digest(f,'sha256').hexdigest()
def now():return datetime.datetime.now(datetime.timezone.utc).isoformat()
pins={}
for name in ['audit-recovery-mesh-trace.py','audit-recovery-styles.py']:
 for node in ast.parse((root/'scripts'/name).read_text()).body:
  if isinstance(node,ast.Assign) and any(isinstance(t,ast.Name) and t.id=='SOURCE_PINS' for t in node.targets):pins.update(ast.literal_eval(node.value))
for name,sha in pins.items():assert digest(root/name)==sha,name
frames=inputs/'recovery-bank-train-native-frames.json';trace=inputs/'recovery-bank-train-mesh-trace.jsonl';plan=out/'style-plan.json'
paths={'frames':frames,'trace':trace,'style_plan':plan,'tool':root/'scripts/audit-recovery-styles.py',**{'source:'+n:root/n for n in pins},**{'jar:'+str(i):Path(p) for i,p in enumerate(c['jars'])}}
before={name:digest(p) for name,p in paths.items()}
command=[c['python'],str(root/'scripts/audit-recovery-styles.py'),*c['jars'],str(frames),str(trace),'--style-plan',str(plan)]
m={'source':c['source'],'scope':c['scope'],'command':command,'cwd':str(root),'started_utc':now(),'startup_hashes':before,'numpy':c['numpy']}
print('START train-mixed-style',flush=True)
with (out/'train-mixed-style.json').open('w') as stdout,(out/'train-mixed-style.stderr').open('w') as stderr:
 p=subprocess.Popen(command,cwd=root,stdout=stdout,stderr=stderr);m['pid']=p.pid
 (out/'metadata.json').write_text(json.dumps(m,indent=2)+'\n')
 code=p.wait()
after={name:digest(p) for name,p in paths.items()}
m.update({'exit_code':code,'ended_utc':now(),'end_hashes':after,'hashes_unchanged':before==after,'report_sha256':digest(out/'train-mixed-style.json'),'stderr_sha256':digest(out/'train-mixed-style.stderr')})
(out/'metadata.json').write_text(json.dumps(m,indent=2)+'\n')
print('END train-mixed-style exit',code,'hashes_unchanged',before==after,flush=True)
raise SystemExit(code if before==after else 2)
