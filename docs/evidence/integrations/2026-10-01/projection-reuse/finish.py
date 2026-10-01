from pathlib import Path
import os,time,json,subprocess,hashlib
b=Path(__file__).resolve().parent;root=b.parents[1]
pid=int((b/'benchmark.pid').read_text())
while not (b/'benchmark.exit').exists():
 os.kill(pid,0);time.sleep(5)
assert (b/'benchmark.exit').read_text().strip()=='0'
# Exit markers precede finally blocks: wait for terminal process handles too.
while Path('/proc',str(pid)).exists():time.sleep(1)
time.sleep(6)
ids=set()
for name in ['regressions-status.json','status.json']:
 ids.update(r['pid'] for r in json.loads((b/name).read_text())['runs'])
for name in ['regressions.pid','benchmark.pid','monitor.pid']:ids.add(int((b/name).read_text()))
live=[p for p in sorted(ids) if Path('/proc',str(p)).exists()];assert not live,live
runtime=Path('/tmp/fourthdimension-48535694/build/runs/packaged-server')
original=(b/'server.properties.original').read_bytes();actual=(runtime/'server.properties').read_bytes();assert original==actual
(b/'cleanup.json').write_text(json.dumps({'tracked_pids':sorted(ids),'live':live,'settings_sha256':hashlib.sha256(actual).hexdigest()},indent=2)+'\n')
subprocess.run(['python3',str(b/'plot.py')],cwd=root,check=True)
subprocess.run(['python3',str(b/'collect.py')],cwd=root,check=True)
out=root/'docs/evidence/integrations/2026-10-01/projection-reuse'
with (out/'verification.json').open('w') as f:subprocess.run(['python3','scripts/verify-projection-reuse.py',str(out)],cwd=root,stdout=f,check=True)
(b/'finish.exit').write_text('0\n')
print('Evidence collected and audited; visual review and commit still pending',flush=True)
