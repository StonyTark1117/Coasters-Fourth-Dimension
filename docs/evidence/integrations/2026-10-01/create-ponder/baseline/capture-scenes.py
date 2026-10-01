from pathlib import Path
import json,os,re,subprocess,time
from rcon import rcon
base=Path(__file__).resolve().parent
clients=json.loads((base/'clients.json').read_text())
pattern=re.compile(r'FD_PONDER player=(\w+) scene=fourthdimension:(\w+) time=(\d+) total=(\d+) keyframes=([\d,]*) finished=(true|false) width=(\d+) height=(\d+)')
scenes=[('arrow_car','family_arrow'),('legacy_zacspin_car','family_legacy'),('free_spin_car','family_free_spin'),('revised_zacspin_car','family_revised'),('pax_car','family_pax'),('boarding_station','station_operations')]
results=[]
for item,scene in scenes:
 handles={c['label']:(base/(c['label']+'.log')).open() for c in clients}
 for handle in handles.values():handle.seek(0,2)
 states={c['label']:{'targets':None,'captured':[]} for c in clients}
 print(rcon('ponder fourthdimension:'+item+' @a'),flush=True)
 started=time.monotonic()
 while not all(s.get('complete') for s in states.values()):
  assert time.monotonic()-started<120,(scene,states)
  for c in clients:
   label=c['label'];state=states[label]
   matches=[m for line in handles[label].readlines() if (m:=pattern.search(line)) and m[2]==scene]
   if not matches:continue
   m=matches[-1];current,total=int(m[3]),int(m[4]); keys=[int(x) for x in m[5].split(',')]
   if state['targets'] is None:
    targets={f'caption_{i+1}':k+35 for i,k in enumerate(keys)}
    if scene!='station_operations':
     targets['appearance_warning']=total-65
     targets['rotation_end']=keys[4 if scene=='family_pax' else 2]+85
     if scene=='family_pax':targets['lift_end']=keys[3]+65
    state['targets']=dict(sorted(targets.items(),key=lambda x:x[1]))
   for name,target in state['targets'].items():
    if name in state['captured'] or current<target:continue
    assert current-target<=20,(scene,label,name,current,target)
    filename=f'{scene}-{label}-{name}.png'
    env=dict(os.environ,DISPLAY=c['display'])
    windows=subprocess.check_output(['xdotool','search','--name','Minecraft'],env=env,text=True).splitlines()
    assert len(windows)==1,windows
    subprocess.run(['import','-window',windows[0],str(base/filename)],env=env,check=True)
    result={'scene':scene,'item':item,'client':label,'phase':name,'target':target,'time':current,'total':total,'keyframes':keys,'finished':m[6]=='true','width':int(m[7]),'height':int(m[8]),'image':filename,'sample':m[0]}
    results.append(result);state['captured'].append(name)
    (base/'captures.json').write_text(json.dumps(results,indent=2)+'\n')
    print(scene,label,name,current,flush=True)
   if m[6]=='true' and len(state['captured'])==len(state['targets']):state['complete']=True
  time.sleep(.12)
 for h in handles.values():h.close()
 for c in clients:subprocess.run(['xdotool','key','Escape'],env=dict(os.environ,DISPLAY=c['display']),check=True)
 time.sleep(.5)
 print('Completed',scene,flush=True)
(base/'capture-scenes.complete').write_text('complete\n')
