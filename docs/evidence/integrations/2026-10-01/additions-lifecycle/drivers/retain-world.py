from pathlib import Path
import json,os,signal,shutil,subprocess,sys
b=Path(__file__).resolve().parent/sys.argv[1];rt=Path('/tmp/fourthdimension-48535694/build/runs/packaged-server');m=json.loads((b/'server.json').read_text())
for label in ['server','dev','observer']:assert (b/(label+'.exit')).read_text().strip()=='0',label
assert not Path('/proc',str(m['pid'])).exists()
for c in json.loads((b/'clients.json').read_text()):
 assert not Path('/proc',str(c['wrapper_pid'])).exists()
 assert Path('/proc',str(c['display_pid']),'comm').read_text().strip()=='Xvfb'
 os.kill(c['display_pid'],signal.SIGTERM)
shutil.copytree(rt/m['world'],b/'world')
with (b/'native-pigs.json').open('w') as f:subprocess.run(['python3',str(b.parent/'inspect-entities.py'),str(b/'world')],stdout=f,check=True)
print(sys.argv[1],'world retained; processes exited')
