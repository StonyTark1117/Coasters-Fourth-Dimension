from pathlib import Path
import os,time,json
out=Path(__file__).resolve().parent
pid=int((out/'profile.pid').read_text())
with (out/'host-load-profile.jsonl').open('a') as f:
 while True:
  try:os.kill(pid,0)
  except ProcessLookupError:break
  f.write(json.dumps({'utc':time.strftime('%Y-%m-%dT%H:%M:%SZ',time.gmtime()),'loadavg':Path('/proc/loadavg').read_text().strip(),'memory':{k:int(v.split()[0]) for l in Path('/proc/meminfo').read_text().splitlines() if (k:=l.split(':')[0]) in ['MemAvailable','MemFree'] for v in [l.split(':')[1]]}})+'\n');f.flush()
  if (out/'profile.exit').exists():break
  time.sleep(5)
