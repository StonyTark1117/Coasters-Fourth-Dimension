from pathlib import Path
import os,time,json
b=Path(__file__).resolve().parent;pid=int((b/'benchmark.pid').read_text())
with (b/'host-load.jsonl').open('a') as f:
 while True:
  try:os.kill(pid,0)
  except ProcessLookupError:break
  if (b/'status.json').exists():
   d=json.loads((b/'status.json').read_text());case=d['runs'][-1]['case'] if d.get('runs') else 'starting'
   f.write(json.dumps({'utc':time.strftime('%Y-%m-%dT%H:%M:%SZ',time.gmtime()),'case':case,'loadavg':Path('/proc/loadavg').read_text().strip()})+'\n');f.flush()
  if (b/'benchmark.exit').exists():break
  time.sleep(5)
