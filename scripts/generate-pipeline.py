#!/usr/bin/env python3
"""Original seated P-gauge cabin, paired seats, restraint states and recipes."""
import json
from pathlib import Path
OUT=Path(__file__).resolve().parents[1]/'src/main/resources'
def write(p,data):
 f=OUT/p;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(json.dumps(data,indent=2)+'\n')
def box(a,b,t):return {'from':a,'to':b,'faces':{f:{'texture':'#'+t,'uv':[0,0,16,16]}for f in ('north','south','east','west','up','down')}}
def model(parts):return {'parent':'minecraft:block/block','textures':{'seat':'minecraft:block/blue_concrete','metal':'minecraft:block/iron_block','harness':'minecraft:block/black_concrete','latch':'minecraft:block/orange_concrete','particle':'minecraft:block/blue_concrete'},'elements':parts}
def worldbox(lo,hi,pos,t):
 origin=[pos[0]-.5,pos[1]-.1875,pos[2]-.5]
 return box([round(16*(lo[i]-origin[i]),5)for i in range(3)],[round(16*(hi[i]-origin[i]),5)for i in range(3)],t)
for right in (False,True):
 for rear in (False,True):
  x=(.1 if right else .9)*16;z=(.75 if rear else .25)*16
  for closed in (False,True):
   parts=[box([x-6,5,z-6],[x+6,7.8,z+6],'seat'),box([x-6,7.8,z+5],[x+6,25,z+7],'seat'),box([x-5,0,z-8],[x+5,1.5,z-3],'metal')]
   for side in (-1,1):
    h=x+side*4
    parts += [box([h-1,10,z-4],[h+1,24,z-2],'harness'),box([h-1,22,z-3],[h+1,25,z+6],'harness')] if closed else [box([h-1,17,z+7],[h+1,29,z+9],'harness')]
   parts.append(box([x-5,10,z-4],[x+5,12,z-2],'latch')if closed else box([x-5,27,z+7],[x+5,29,z+9],'harness'))
   write(f'assets/fourthdimension/models/block/pipeline_seat_{int(right)}_{int(rear)}_{"closed"if closed else "open"}.json',model(parts))
write('assets/fourthdimension/blockstates/pipeline_seat.json',{'variants':{f'facing={d},restrained={str(c).lower()},right={str(r).lower()},rear={str(z).lower()}':{'model':f'fourthdimension:block/pipeline_seat_{int(r)}_{int(z)}_{"closed"if c else "open"}'}for d in ('north','south','east','west')for c in (False,True)for r in (False,True)for z in (False,True)}})
frame=[(x,-3,z)for z in range(-2,3)for x in range(-1,2)]+[(x,0,z)for z in (-2,2)for x in range(-1,2)]+[(x,y,z)for z in (-2,2)for x in (-1,1)for y in (-2,-1)]+[(0,-1,0)]
for i,pos in enumerate(frame):
 x,y,z=pos
 if y==-3:parts=[worldbox([x-.5,-2.40,z-.5],[x+.5,-2.25,z+.5],pos,'metal')]
 elif y==0:
  zz=2.3*(1 if z>0 else -1)
  parts=[worldbox([x-.5,.48,zz-.08],[x+.5,.62,zz+.08],pos,'metal')]
 elif z!=0:
  xx=1.4*(1 if x>0 else -1);zz=2.3*(1 if z>0 else -1)
  parts=[worldbox([xx-.08,y-.1875,zz-.08],[xx+.08,y+.8125,zz+.08],pos,'metal')]
 else:parts=[worldbox([-.18,-1.3,-.18],[.18,.28,.18],pos,'metal'),worldbox([-.45,.15,-.45],[.45,.3,.45],pos,'seat')]
 write(f'assets/fourthdimension/models/block/pipeline_frame_{i}.json',model(parts))
write('assets/fourthdimension/blockstates/pipeline_frame.json',{'variants':{f'part={i}':{'model':f'fourthdimension:block/pipeline_frame_{i}'}for i in range(len(frame))}})
write('assets/fourthdimension/models/block/pipeline_controller.json',model([worldbox([-.22,-2.25,-.22],[.22,-1.18,.22],(0,-2,0),'metal'),box([1,6,1],[15,12,15],'seat')]))
write('assets/fourthdimension/blockstates/pipeline_controller.json',{'variants':{'':{'model':'fourthdimension:block/pipeline_controller'}}})
parts=[box([-2,1,0],[18,3,16],'metal')]
for x in (4,12):
 for z in (4,12):parts += [box([x-3,3,z-3],[x+3,5,z+2],'seat'),box([x-3,5,z+1],[x+3,12,z+3],'seat'),box([x-2,6,z-2],[x-1,12,z-1],'harness'),box([x+1,6,z-2],[x+2,12,z-1],'harness')]
parts += [box([-4,11,0],[-1,14,16],'metal'),box([17,11,0],[20,14,16],'metal')]
m=model(parts);m['display']={'gui':{'rotation':[25,225,0],'translation':[0,0,0],'scale':[.6,.6,.6]},'ground':{'scale':[.35,.35,.35]}}
write('assets/fourthdimension/models/item/pipeline_car.json',m)
write('assets/fourthdimension/models/item/pipeline_track.json',model([box([0,14,0],[16,16,2],'metal'),box([0,14,14],[16,16,16],'metal'),box([2,0,0],[4,14,2],'seat'),box([2,0,14],[4,14,16],'seat'),box([2,0,0],[4,2,16],'seat')]))
write('data/fourthdimension/recipe/pipeline_track.json',{'type':'minecraft:crafting_shaped','pattern':['ICI','IDI','ICI'],'key':{'I':{'tag':'c:ingots/iron'},'C':{'item':'minecraft:copper_ingot'},'D':{'item':'minecraft:blue_dye'}},'result':{'id':'fourthdimension:pipeline_track','count':16}})
write('data/fourthdimension/recipe/pipeline_car.json',{'type':'minecraft:crafting_shaped','pattern':['SIS','SCS','ITI'],'key':{'S':{'item':'create:red_seat'},'I':{'tag':'c:ingots/iron'},'T':{'item':'fourthdimension:pipeline_track'},'C':{'item':'simulatedcoasters:coaster_cart'}},'result':{'id':'fourthdimension:pipeline_car','count':1}})
for f in (OUT/'assets/fourthdimension/models').rglob('pipeline_*.json'):
 for e in json.loads(f.read_text()).get('elements',[]):
  assert all(-16<=a<b<=32 for a,b in zip(e['from'],e['to'])),(f,e)
print('Pipeline assets generated; element bounds and explicit UVs verified')
