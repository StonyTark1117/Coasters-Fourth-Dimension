#!/usr/bin/env python3
"""Original Flying meshes. Every face has explicit UVs; cuboids are split to <=16 units."""
import json,re,itertools
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'src/main/resources'
def write(p,d):
 f=OUT/p;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(json.dumps(d,indent=2)+'\n')
def box(a,b,t):
 steps=[[a[i]+16*k for k in range(int((b[i]-a[i]-1e-7)//16)+1)]+[b[i]]for i in range(3)]
 return [{'from':[round(v,6)for v in lo],'to':[round(v,6)for v in hi], 'faces':{f:{'texture':'#'+t,'uv':[0,0,16,16]}for f in ('north','south','east','west','up','down')}} for spans in itertools.product(*[list(zip(s,s[1:]))for s in steps]) for lo,hi in [(list(zip(*spans))[0],list(zip(*spans))[1])]]
def model(parts):return {'parent':'minecraft:block/block','textures':{'seat':'minecraft:block/cyan_concrete','metal':'minecraft:block/iron_block','harness':'minecraft:block/black_concrete','latch':'minecraft:block/orange_concrete','locked':'minecraft:block/lime_concrete','particle':'minecraft:block/cyan_concrete'},'elements':parts}
for body,legs in itertools.product((False,True),repeat=2):
 parts=[]
 for a,b,t in [([1,5,1],[15,8,15],'seat'),([1,8,14],[15,29,17],'seat'),([2,-11,-1],[14,-8,6],'metal'),([6,-8,4],[10,5,7],'metal')]:parts+=box(a,b,t)
 for x in (3,13):
  if body:
   parts+=box([x-1,11,1],[x+1,27,3],'harness')+box([x-1,25,3],[x+1,28,16],'harness')
  else:parts+=box([x-1,11,16],[x+1,30,18],'harness')
 if body:parts+=box([2,10,1],[14,13,3],'latch')
 else:parts+=box([2,28,16],[14,30,18],'harness')
 if legs:parts+=box([2,-6,-2],[14,-3,0],'latch')+box([1,-6,-1],[3,4,1],'harness')+box([13,-6,-1],[15,4,1],'harness')
 else:parts+=box([0,-6,5],[2,4,7],'harness')+box([14,-6,5],[16,4,7],'harness')
 write(f'assets/fourthdimension/models/block/flying_seat_{int(body)}{int(legs)}.json',model(parts))
write('assets/fourthdimension/blockstates/flying_seat.json',{'variants':{f'facing={d},restrained={str(b).lower()},legs={str(g).lower()}':{'model':f'fourthdimension:block/flying_seat_{int(b)}{int(g)}'}for d in ('north','south','east','west')for b,g in itertools.product((False,True),repeat=2)}})
source=(ROOT/'src/main/java/dev/fourthdimension/vehicle/FlyingFrameBlock.java').read_text().split('BOXES={',1)[1].split('};',1)[0]
boxes=[list(map(float,s.split(',')))for s in re.findall(r'\{([^{}]+)\}',source)]
frames=[]
for x,y,z in itertools.product(range(-2,3),range(-3,0),range(-2,3)):
 origin=(x-.5,y-.1875,z-.5);parts=[]
 for b in boxes:
  lo=[max(origin[i],b[i])for i in range(3)];hi=[min(origin[i]+1,b[i+3])for i in range(3)]
  if all(lo[i]<hi[i]for i in range(3)):
   parts+=box([(lo[i]-origin[i])*16 for i in range(3)],[(hi[i]-origin[i])*16 for i in range(3)],'metal')
 if parts:
  name='flying_controller'if(x,y,z)==(0,-1,0)else'flying_frame_'+str(len(frames))
  if(x,y,z)!=(0,-1,0):frames.append((x,y,z))
  write(f'assets/fourthdimension/models/block/{name}.json',model(parts))
write('assets/fourthdimension/blockstates/flying_frame.json',{'variants':{f'part={i}':{'model':f'fourthdimension:block/flying_frame_{i}'}for i in range(len(frames))}})
write('assets/fourthdimension/blockstates/flying_controller.json',{'variants':{'':{'model':'fourthdimension:block/flying_controller'}}})
for locked in(False,True):write(f'assets/fourthdimension/models/block/flying_lock_{str(locked).lower()}.json',model(box([-8,20,16],[8,24,18],'locked'if locked else'latch')))
write('assets/fourthdimension/blockstates/flying_lock.json',{'variants':{f'locked={str(v).lower()}':{'model':f'fourthdimension:block/flying_lock_{str(v).lower()}'}for v in(False,True)}})
parts=box([0,22,6],[16,24,10],'metal')
for x in(1,5,9,13):parts+=box([x-1,7,5],[x+2,9,11],'seat')+box([x-1,9,10],[x+2,17,12],'seat')+box([x-1,3,2],[x+2,5,6],'latch')
m=model(parts);m['display']={'gui':{'rotation':[25,225,0],'translation':[0,-1,0],'scale':[.7,.7,.7]},'ground':{'scale':[.35,.35,.35]}}
write('assets/fourthdimension/models/item/flying_car.json',m)
write('assets/fourthdimension/models/item/flying_track.json',model(box([0,8,1],[16,10,3],'metal')+box([0,8,13],[16,10,15],'metal')+box([0,17,6],[16,21,10],'seat')+box([2,10,2],[4,18,14],'metal')+box([12,10,2],[14,18,14],'metal')))
write('data/fourthdimension/recipe/flying_track.json',{'type':'minecraft:crafting_shaped','pattern':['III','CDC','III'],'key':{'I':{'tag':'c:ingots/iron'},'C':{'item':'minecraft:copper_ingot'},'D':{'item':'minecraft:light_blue_dye'}},'result':{'id':'fourthdimension:flying_track','count':16}})
write('data/fourthdimension/recipe/flying_car.json',{'type':'minecraft:crafting_shaped','pattern':['SIS','SCS','ITI'],'key':{'S':{'item':'create:red_seat'},'I':{'tag':'c:ingots/iron'},'C':{'item':'simulatedcoasters:coaster_cart'},'T':{'item':'fourthdimension:flying_track'}},'result':{'id':'fourthdimension:flying_car','count':1}})
for p in(OUT/'assets/fourthdimension/models').rglob('flying_*.json'):
 for e in json.loads(p.read_text()).get('elements',[]):
  assert all(-16<=a<b<=32 and b-a<=16.00001 for a,b in zip(e['from'],e['to'])),(p,e)
print('Flying models generated:',len(frames),'connected fixed frame cells; bounds and UVs verified')
