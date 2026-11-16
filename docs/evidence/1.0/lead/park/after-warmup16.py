from pathlib import Path
import json,time,subprocess,sys,math
base=Path('/mnt/NVME4/fourthdimension-runs/fd10-lead-20261115');root=base/'park-combined01';e=root/'evidence'
for _ in range(110):
 p=e/'warmup-combined16.jsonl';rows=[json.loads(x) for x in p.read_text().splitlines()] if p.exists() else []
 if any(x['kind']=='failure' for x in rows):raise RuntimeError('Warmup16 failed')
 if rows and rows[-1]['kind']=='complete':break
 time.sleep(5)
else:raise TimeoutError('Warmup16 did not complete')
subprocess.run([sys.executable,str(base/'profile-client16.py')],check=True)
left=[]
for side in ['client','server']:
 manifest=json.loads((e/(side+'-combined16.jsonl')).open().readline());left.append((manifest['unix_ns']+manifest['seconds']*1e9-time.time_ns())/1e9)
budget=min(2800,math.floor(min(left))-45);assert budget>=2000,budget
styles=['roller_ball','flying','new_generation_flying','pipeline','dive','s2_spinning','powered_inverted_yaw']
subprocess.run([sys.executable,str(base/'park-occupied.py'),str(root/'run.json'),'occupied16','--budget',str(budget),'--recorder-stem','combined16',*styles],check=True)
print('Remaining mounted batch finished; inspect completion and drain normally.',flush=True)
