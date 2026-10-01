from pathlib import Path
import json,os,subprocess,sys
b=Path(__file__).resolve().parent/sys.argv[1];tag=sys.argv[2]
for c in json.loads((b/'clients.json').read_text()):
 assert Path('/proc',str(c['display_pid'])).exists() and Path('/proc',str(c['wrapper_pid'])).exists()
 subprocess.run(['import','-window','root',str(b/(tag+'-'+c['label']+'.png'))],env={**os.environ,'DISPLAY':c['display']},check=True)
