"""Read-only reconciliation of the preserved candidate17 regression sample."""
from pathlib import Path
import json,math
repo=Path('/home/braydon/Documents/Coasters: Fourth Dimension')
root=Path('/mnt/NVME4/fourthdimension-runs/fd10-lead-20261115/park-combined01');e=root/'evidence'
summary=json.loads((e/'comparison-combined17-summary.json').read_text());original=json.loads((e/'comparison-combined17-verdict.json').read_text())
rows=[json.loads(x) for x in (e/'comparison-combined17-windows.jsonl').read_text().splitlines()]
start=next(x for x in rows if x['kind']=='window_start');end=next(x for x in rows if x['kind']=='comparison_window_end');lo=start['unix_ns'];hi=lo+300_000_000_000
assert lo==summary['window']['start_unix_ns'] and hi==summary['window']['end_unix_ns']
assert hi<=end['unix_ns']<hi+1_000_000_000
names=[x['name'] for x in json.loads((root/'park.json').read_text())]
assert [x['style'] for x in rows if x['kind']=='view_start']==names
assert [x['style'] for x in rows if x['kind']=='view_end']==names
assert any(x['kind']=='spectator_verified' and x['unix_ns']<lo for x in rows)
for name in names:
 a=next(x for x in rows if x['kind']=='view_start' and x['style']==name);b=next(x for x in rows if x['kind']=='view_end' and x['style']==name)
 assert lo<=a['unix_ns']<b['unix_ns']<=hi and b['unix_ns']-a['unix_ns']>=9_900_000_000
 assert Path(b['screenshot']).is_file()
occupied=next(x for x in rows if x['kind']=='occupied_view_start');motion=next(x for x in rows if x['kind']=='occupied_motion_proof')
mount=next(x for x in rows if x['kind']=='command' and x['command']=='execute as Dev run fd904 mount arrow')
mounted=json.loads(mount['answer'].strip().removeprefix('FD904_STATE '))
def arrow(d):return next(g for g in d['groups'] if g['name']=='arrow')
assert occupied['style']==motion['style']=='arrow' and occupied['unix_ns']<motion['unix_ns']<hi
assert motion['state']['tick']-mounted['tick']>=100
movement=math.dist(arrow(motion['state'])['center'],arrow(mounted)['center']);assert movement>=2
states=[x['state'] for x in rows if 'state' in x and lo<=x['unix_ns']<=end['unix_ns']]
for x in rows:
 if x['kind']=='command' and lo<=x['unix_ns']<=end['unix_ns'] and x['command']=='fd904 snapshot':states.append(json.loads(x['answer'].strip().removeprefix('FD904_STATE ')))
for d in states:
 assert d['running'] and not d['draining'] and (d['rails'],d['vehicles'],d['carriers'],d['forcedChunks'])==(198,21,34,1680)
 assert sum(g['riders'] for g in d['groups'])==90 and all(not g['fault'] for g in d['groups'])
counts={};footers=[]
for side,kind in [('client','frame'),('server','tick')]:
 samples=[]
 for line in (e/(side+'-combined17.jsonl')).open():
  x=json.loads(line)
  if x['kind']==kind and lo<=x['unix_ns']<hi:samples.append(x)
 footer=x;assert footer['kind']=='complete' and footer['lossless'] and footer['dropped_events']==0;footers.append(footer)
 assert samples[0]['unix_ns']<lo+100_000_000 and samples[-1]['unix_ns']>hi-100_000_000
 if side=='client':assert all(x['screen']=='none' and not x['paused'] and x['dimension']=='minecraft:overworld' for x in samples)
 counts[kind]=len(samples);assert len(samples)==summary['sources'][kind+':'+side]['count']
resources=[json.loads(x) for x in (e/'comparison-combined17-resources.jsonl').read_text().splitlines()];assert resources[-1]['kind']=='complete' and resources[-1]['samples']==300
samples=[x for x in resources if x['kind']=='resource'];assert len(samples)==300
for x in samples:
 for side,cap in [('client',6*1024**3),('server',4*1024**3)]:
  proc=x['processes'][side];g=proc['groups'][0];assert int(g['memory.max'])==cap and int(g['memory.current'])<cap
  events=dict(line.split() for line in g['memory.events'].splitlines());assert all(events[k]=='0' for k in ['max','oom','oom_kill'])
assert not summary['collector_errors'] and not summary['missing']
baseline=json.loads((repo/'docs/evidence/1.0/lead/park/combined13r2-first300-summary.json').read_text());checks=[]
for source in ['frame:client','tick:server']:
 for metric in ['p95','p99']:
  value=summary['sources'][source][metric];limit=baseline['sources'][source][metric]*1.1;checks.append({'source':source,'metric':metric,'actual':value,'frozen_limit':limit,'pass':value<=limit})
checks.extend(x for x in original['checks'] if x['source'].startswith(('resource:','post_gc:')))
assert all(x['pass'] for x in checks)
result={'status':'pass: later regression comparison, not a new initial-baseline claim','policy':'PERFORMANCE.md Measurements and frozen budgets items3/5 and later-change paragraph; initial13r2 and sustained14 retain their accepted <=50ms results','original_guard_failure_preserved':'comparison-combined17-verdict.json and comparison-combined17-windows.jsonl','classification_error':'The initial50ms clamp and return-code completeness test were incorrectly reused for a later regression comparison. No baseline or frozen ceiling is changed.','window':summary['window'],'coverage':{'view_order':names,'occupied_arrow_motion_blocks':movement,'state_observations':len(states),'end_tick_advance':end['state']['tick']-start['state']['tick'],'counts':counts,'all_90_riders_no_faults':True,'all_frames_in_world_no_screen':True,'resource_samples':len(samples),'scope_limits_and_zero_oom_max_events':True},'checks':checks,'footers':footers}
(e/'comparison-combined17-reconciled.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps({'status':result['status'],'coverage':result['coverage'],'checks':checks},indent=2))
