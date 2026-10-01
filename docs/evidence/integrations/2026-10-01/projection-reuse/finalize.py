from pathlib import Path
import hashlib,json,os,shutil,socket,statistics
b=Path(__file__).resolve().parent;root=b.parents[1];out=root/'docs/evidence/integrations/2026-10-01/projection-reuse'
assert (b/'finish.exit').read_text().strip()=='0'
cleanup=json.loads((b/'cleanup.json').read_text());ids=set(cleanup['tracked_pids'])
for directory in [b,b/'failed-attempt']:
 for p in directory.glob('*.pid'):ids.add(int(p.read_text()))
failed=json.loads((b/'failed-attempt/status.json').read_text());ids.update(r['pid'] for r in failed['runs'])
assert not [p for p in ids if Path('/proc',str(p)).exists()]
for port in [25585,25586]:
 with socket.socket() as s:assert s.connect_ex(('127.0.0.1',port))!=0
cleanup.update(tracked_pids=sorted(ids),closed_loopback_ports=[25585,25586]);(b/'cleanup.json').write_text(json.dumps(cleanup,indent=2)+'\n')
runtime=Path('/tmp/fourthdimension-48535694/build/runs/packaged-server')
world=runtime/failed['runs'][1]['world'];dest=b/'worlds'/world.name
if not dest.exists():shutil.copytree(world,dest)
shutil.copyfile(world/'data/fourthdimension_foundation.dat',b/'failed-attempt/base-0-new-foundation.dat')
assert (runtime/'server.properties').read_bytes()==(b/'server.properties.original').read_bytes()
v=json.loads((out/'verification.json').read_text());rows=[]
for key,pair in v['pairs'].items():
 pack,repeat,mode=key.split('-');old,new=pair['old'],pair['new']
 rows.append(f"| {pack.title()} | {int(repeat)+1} | {mode} | {old['carrier_callback_ms_per_tick']:.2f} → {new['carrier_callback_ms_per_tick']:.2f} | {pair['callback_reduction_percent']:.1f}% | {old['p95_ms']:.2f} → {new['p95_ms']:.2f} |")
loads=[float(json.loads(l)['loadavg'].split()[0]) for l in (b/'host-load.jsonl').read_text().splitlines()]
results='''## Results and decision

All 16 accepted windows pass the identity, restraint, coupling, loaded-body and
saved-data checks. All 96 original passengers remain attached to their original
native seats and carriers. Every operating window observes lifting, outbound,
returning and capturing. These short windows end during capture; they do not add
full-cycle unloading evidence.

| Pack | Round | Window | Mean carrier callback, old → new (ms/tick) | Reduction | Server-event P95, old → new (ms) |
| --- | --- | --- | --- | --- | --- |
'''+ '\n'.join(rows)+f'''

The mean carrier callback is lower in all eight comparisons, by 10.2–35.4%.
Mean server-event time improves in seven comparisons; the first base operating
window instead rises from 33.95 to 39.82 ms. Two server-event P95 comparisons
worsen. The new artifact meets the 50 ms P95 development target in seven of eight
windows, with a 54.63 ms exception. The shared host's recorded one-minute load
ranges from {min(loads):.2f} to {max(loads):.2f} across the retained trace. The code removes redundant work,
but these observations do not isolate its causal speedup from host, GC and runtime
variation, and do not establish a pack-wide performance guarantee.

Retain the narrow optimization: all seven native regression profiles pass with
19 unit tests, and both run orders show less callback time. Keep FD904 open for
consistent server targets and the unmeasured client/render/equipment scenarios.
Exact per-window means, P95/P99, maxima and over-budget counts are retained in
[verification.json](verification.json); [manifest.json](manifest.json) identifies
both immutable JARs and every retained source/evidence hash.

![Paired server and carrier callback timing](comparison.png)

All {len(ids)} tracked process handles, including the interrupted attempt and coordinators,
are terminal. The private Minecraft/RCON ports are closed and the original server
settings match their pre-run SHA-256. Full failed and accepted world copies remain
in the ignored workspace evidence directory.

'''
p=out/'README.md';s=p.read_text();assert '## Results and decision' not in s;s=s.replace('## Interrupted observation and recovery',results+'## Interrupted observation and recovery');p.write_text(s)
p=root/'ROADMAP.md';s=p.read_text();needle='**Depends on:** FD404, FD006. **Design:** D17.';assert s.count(needle)==1
s=s.replace(needle,'''The [scoped projection-reuse checkpoint](docs/evidence/integrations/2026-10-01/projection-reuse/README.md)
shares native geometry within each vehicle's physics update. Seven native profiles
and 19 unit tests pass; eight occupied old/new pairs record 10.2–35.4% lower mean
carrier-callback time. Overall timing remains variable on the shared host, including
one new-build P95 above 50 ms. The optimization is retained; FD904 stays open.

'''+needle);p.write_text(s)
p=root/'docs/INTEGRATIONS.md';s=p.read_text();needle='## Combined-pack multiplayer evidence';assert s.count(needle)==1
s=s.replace(needle,'''A [paired optimization checkpoint](evidence/integrations/2026-10-01/projection-reuse/README.md)
reuses each vehicle's native center/edge/projection within a single physics update.
It preserves boundary and dispatch checks and passes all seven native profiles.
Across 9,600 accepted occupied-scene ticks, mean carrier-callback time is lower in
every old/new comparison, while total server-event timing remains variable. One
new-build P95 exceeds the 50 ms development target. The retained failed harness
attempt, raw windows and limits are documented; FD904 remains open.

'''+needle);p.write_text(s)
print('Final results written; process/settings cleanup and failed-world preservation confirmed.')
