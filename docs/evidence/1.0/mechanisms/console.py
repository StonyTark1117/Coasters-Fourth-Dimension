"""Owned-server RCON with full immutable per-command readbacks and concise summaries."""
import importlib.util,json,sys,time
from pathlib import Path
ROOT=Path('/mnt/NVME4/fourthdimension-runs/fd10-mechanisms-20261115')
spec=importlib.util.spec_from_file_location('fd',str(Path(__file__).resolve().parents[4]/'scripts/fd-private-client.py'))
m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
r=m.PrivateRun(ROOT/'run.json','01a1dd1d-c6a6-7be0-9cb5-2b033ac550da')
r.state('server')
cmd=' '.join(sys.argv[1:]);stamp=str(time.time_ns());answer=m.rcon(r.c,cmd)
record={'time_ns':stamp,'command':cmd,'answer':answer}
(ROOT/'evidence'/('console-'+stamp+'.json')).write_text(json.dumps(record,indent=2)+'\n')
if answer.startswith('FD902_STATE '):
 state=json.loads(answer.removeprefix('FD902_STATE '));(ROOT/'evidence/state-last.json').write_text(json.dumps(state,indent=2)+'\n')
 print(json.dumps({'phase':state['phase'],'interrupted':state['interrupted'],'fault':state['fault'],'tick':state['tick'],'distance':state['distance'],'cycles':state['cycles'],'riders':len(state['riders']),'cars':len(state['cars']),'ready':[c['ready'] for c in state['cars']],'restraints':[c['restraints'] for c in state['cars']],'align':[c['alignmentRequired'] for c in state['cars']],'faults':[c['fault'] for c in state['cars']],'log':str(ROOT/'evidence'/('console-'+stamp+'.json'))}))
else:print(answer)
