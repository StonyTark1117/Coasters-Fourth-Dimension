from pathlib import Path
import json,os,subprocess,sys
b=Path(__file__).resolve().parent/sys.argv[1]
for c in json.loads((b/'clients.json').read_text()):
 assert Path('/proc',str(c['display_pid']),'comm').read_text().strip()=='Xvfb'
 assert Path('/proc',str(c['wrapper_pid'])).exists()
 subprocess.run(['xdotool',*sys.argv[2:]],env={**os.environ,'DISPLAY':c['display']},check=True)
