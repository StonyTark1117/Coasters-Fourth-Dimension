"""Report recorded actual occupied segments; no universal per-family frame budget is implied."""
from pathlib import Path
import argparse,json,importlib.util
p=argparse.ArgumentParser();p.add_argument('root',type=Path);p.add_argument('label');p.add_argument('stem');p.add_argument('--output',type=Path,required=True);a=p.parse_args()
spec=importlib.util.spec_from_file_location('summary','/home/braydon/Documents/Coasters: Fourth Dimension/scripts/fd-performance-summary.py');s=importlib.util.module_from_spec(spec);spec.loader.exec_module(s)
e=a.root/'evidence';events=[json.loads(l) for l in (e/(a.label+'-windows.jsonl')).read_text().splitlines()];assert next(x for x in reversed(events) if x['kind']!='command')['kind'] in ('complete','failure')
completed=[x['style'] for x in events if x['kind']=='style_complete']
continuation=None
if a.label=='occupied15':
 continuation=[json.loads(l) for l in (e/'occupied15-pax-continuation.jsonl').read_text().splitlines()]
 assert continuation[-1]['kind']=='complete' and continuation[-1]['style']=='pax'
 completed.append('pax')
remaining=[n for n in next(x['styles'] for x in events if x['kind']=='segments_start') if n not in completed]
raw=[];sources=[]
for n in ['client-'+a.stem+'.jsonl','server-'+a.stem+'.jsonl',a.label+'-resources.jsonl']:
 rows=[json.loads(l) for l in (e/n).read_text().splitlines()];assert rows[-1]['kind']=='complete',(n,rows[-1]);assert rows[-1].get('dropped_events',0)==0;raw.extend(rows);sources.append(n)
segments=[]
for name in completed:
 start=next(r for r in events if r['kind']=='occupied_start' and r['style']==name)
 motion=next(r for r in events if r['kind']=='occupied_motion' and r['style']==name)
 end=next(r for r in events if r['kind']=='occupied_end' and r['style']==name)
 assert motion['game_ticks']>=100 and motion['center_path_distance']>=2
 assert any(r['kind']=='style_complete' and r['style']==name for r in events) or (name=='pax' and continuation)
 stats=s.summarize(raw,start['unix_ns'],end['unix_ns']);assert all(k in stats['sources'] for k in ['frame:client','tick:server','resource:client','resource:server']),stats['missing']
 states=[r['state'] for r in events if start['unix_ns']<=r['unix_ns']<=end['unix_ns'] and 'state' in r]
 assert all(sum(g['riders'] for g in d['groups'])==90 and all(not g['fault'] for g in d['groups']) for d in states)
 segments.append({'style':name,'start_unix_ns':start['unix_ns'],'end_unix_ns':end['unix_ns'],'ticks':end['game_ticks'],'center_path_distance':end['center_path_distance'],'motion_proof':motion,'statistics':stats,'normal_return_and_open_shift':True,'screenshots':[r['screenshot'] for r in events if r.get('style')==name and 'screenshot'in r]})
a.output.write_text(json.dumps({'scope':'Actual occupied supplemental views with helper boarding; not normal player-input acceptance, not spliced into sustained window, no uniform per-family frame threshold. Paused next station prepares safe transfer; other groups continue cycling.','sources':sources,'completed':completed,'remaining':remaining,'continuation':continuation,'segments':segments},indent=2)+'\n')
print('Summarized',len(segments),'occupied segments')
