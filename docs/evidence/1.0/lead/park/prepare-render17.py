from pathlib import Path
import argparse,shutil,json,hashlib,subprocess,re
p=argparse.ArgumentParser();p.add_argument('root',type=Path);p.add_argument('prior');p.add_argument('stem');p.add_argument('jar',type=Path);a=p.parse_args()
c=json.loads((a.root/'run.json').read_text())
for side in ['client','server','xvfb']:
 result=subprocess.run(['systemctl','--user','show',c['unit_prefix']+'-'+side+'.service','-p','ActiveState','--value'],text=True,capture_output=True)
 assert result.stdout.strip() not in ['active','activating','deactivating'],(side,result.stdout)
source=a.root/a.prior;target=a.root/'runtime17';assert not target.exists();assert a.jar.is_file()
shutil.copytree(source,target)
for f in (target/'moddev').iterdir():
 if f.is_file() and f.suffix in ['.txt','.xml','.sh']:f.write_text(f.read_text().replace(str(source),str(target)))
oldjar=next((target/'libs').glob('coasters-fourth-dimension-*.jar'));assert oldjar.name==a.jar.name
oldhash=hashlib.sha256(oldjar.read_bytes()).hexdigest();shutil.copy2(a.jar,oldjar)
# Enable observational render counters; no auto-exit or mode changes.
f=target/'moddev/clientRunVmArgs.txt';s=f.read_text();assert '-Dfourthdimension.renderValidation=' in s;s=s.replace('-Dfourthdimension.renderValidation=false','-Dfourthdimension.renderValidation=true');f.write_text(s)
for side in ['client','server']:
 f=a.root/(side+'-launch.sh');s=f.read_text().replace(str(source),str(target));s=re.sub(side+r'-(base|combined)\d+\.jsonl',side+'-'+a.stem+'.jsonl',s);assert a.stem+'.jsonl' in s and str(target) in s
 f.write_text(s)
files={str(f.relative_to(target)):hashlib.sha256(f.read_bytes()).hexdigest() for f in target.rglob('*') if f.is_file() and f.name!='snapshot.json'}
(target/'snapshot.json').write_text(json.dumps({'source_snapshot':str(source),'replaced_jar_sha256':oldhash,'jar_sha256':hashlib.sha256(oldjar.read_bytes()).hexdigest(),'source_jar':str(a.jar),'files':files},indent=2)+'\n')
print('Prepared immutable',target)
