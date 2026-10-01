from pathlib import Path
import json,os,time,shutil,subprocess
b=Path(__file__).resolve().parent
old=int((b/'native.pid').read_text())
while Path('/proc',str(old)).exists(): time.sleep(2)
state=json.loads((b/'native-status.json').read_text());assert state['status']=='failed' and (b/'native-additions.exit').read_text().strip()!='0'
for name in ['native-status.json','native-additions.log','native-additions.exit','native.exit','native-wrapper.log','native.pid']:
 shutil.move(b/name,b/'uninitialized-link'/name)
with (b/'native-wrapper.log').open('w') as f:
 p=subprocess.Popen(['python3',str(b/'native.py')],stdin=subprocess.DEVNULL,stdout=f,stderr=subprocess.STDOUT,start_new_session=True)
(b/'native.pid').write_text(str(p.pid)+'\n')
print('new native coordinator',p.pid,flush=True)
