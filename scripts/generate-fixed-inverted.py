#!/usr/bin/env python3
"""Original Fixed Inverted models in the native chassis frame, plus usable recipes."""
import json
from pathlib import Path
OUT=Path(__file__).resolve().parents[1]/'src/main/resources'
def write(p,data):
 f=OUT/p;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(json.dumps(data,indent=2)+'\n')
def box(a,b,t):return {'from':a,'to':b,'faces':{f:{'texture':'#'+t,'uv':[0,0,16,16]}for f in ('north','south','east','west','up','down')}}
def model(parts):return {'parent':'minecraft:block/block','textures':{'seat':'minecraft:block/purple_concrete','metal':'minecraft:block/iron_block','harness':'minecraft:block/black_concrete','latch':'minecraft:block/orange_concrete','particle':'minecraft:block/purple_concrete'},'elements':parts}
def worldbox(lo,hi,pos,t):
 # input native X/Z, graph Y. Block origin relative to graph is (x-.5,y-.1875,z-.5).
 origin=[pos[0]-.5,pos[1]-.1875,pos[2]-.5]
 return box([round(16*(lo[i]-origin[i]),5)for i in range(3)],[round(16*(hi[i]-origin[i]),5)for i in range(3)],t)
for closed in (False,True):
 # X=0 and Y=.9375 are exact fractional seat centers in the containing block.
 parts=[box([-7,12,2],[7,15,14],'seat'),box([-7,15,13],[7,32,16],'seat'),
        box([-2,7,8],[2,11,30],'metal'),box([-8,7,28],[8,11,32],'metal'),box([-6,1,-6],[6,3,2],'metal')]
 for x in (-5,5):
  parts += [box([x-1,2,0],[x+1,13,2],'metal')]
  if closed:
   parts += [box([x-1,17,3],[x+1,31,5],'harness'),box([x-1,29,4],[x+1,31,15],'harness')]
  else:parts += [box([x-1,17,16],[x+1,32,18],'harness')]
 parts += [box([-6,16,3],[6,18,5],'latch')] if closed else [box([-6,30,16],[6,32,18],'harness')]
 write(f'assets/fourthdimension/models/block/fixed_inverted_seat_{"closed"if closed else "open"}.json',model(parts))
 write(f'assets/fourthdimension/models/block/fixed_inverted_seat_{"closed"if closed else "open"}_outer.json',model(parts+[box([8,7,28],[12,16,32],'metal')]))
write('assets/fourthdimension/blockstates/fixed_inverted_seat.json',{'variants':{f'facing={d},restrained={str(c).lower()},outer={str(o).lower()}':{'model':f'fourthdimension:block/fixed_inverted_seat_{"closed"if c else "open"}{"_outer"if o else ""}'}for d in ('north','south','east','west')for c in (False,True)for o in (False,True)}})
frame=[(-2,-1,0),(-1,-1,0),(1,-1,0),(2,-1,0),(-2,-2,0),(2,-2,0),(-2,-3,0),(2,-3,0),(-2,-4,0)]
for i,pos in enumerate(frame):
 x,y,z=pos;parts=[]
 if y==-1:
  lo=max(-2.25,x-.5);hi=min(2.25,x+.5)
  parts.append(worldbox([lo,-.9,1.25],[hi,-.75,1.5],pos,'metal'))
 if abs(x)==2:
  lo,hi=(-2.25,-2)if x<0 else(2,2.25)
  ylo=max(-3.75,y-.1875);yhi=min(-.75,y+.8125)
  parts.append(worldbox([lo,ylo,1.25],[hi,yhi,1.5],pos,'metal'))
 write(f'assets/fourthdimension/models/block/fixed_inverted_frame_{i}.json',model(parts))
write('assets/fourthdimension/blockstates/fixed_inverted_frame.json',{'variants':{f'part={i}':{'model':f'fourthdimension:block/fixed_inverted_frame_{min(i,8)}'}for i in range(10)}})
# Connect the visible upper rear crossbar to the native bearing. No decorative spine through heads.
parts=[worldbox([-.5,-.9,1.25],[.5,-.75,1.5],(0,-1,0),'metal'),worldbox([-.125,-.85,0],[.125,-.7,1.375],(0,-1,0),'metal'),worldbox([-.125,-.8,-.125],[.125,0,.125],(0,-1,0),'metal')]
write('assets/fourthdimension/models/block/fixed_inverted_controller.json',model(parts))
write('assets/fourthdimension/blockstates/fixed_inverted_controller.json',{'variants':{'':{'model':'fourthdimension:block/fixed_inverted_controller'}}})
# Four distinct cushions identify this fixed row in inventory.
parts=[box([0,23,7],[16,25,10],'metal'),box([0,5,12],[2,24,14],'metal'),box([14,5,12],[16,24,14],'metal')]
for x in (1,5,9,13):
 parts += [box([x-1,5,6],[x+2,7,11],'seat'),box([x-1,7,10],[x+2,14,12],'seat'),box([x-1,8,6],[x,14,7],'harness'),box([x+1,8,6],[x+2,14,7],'harness'),box([x-1,1,3],[x+2,2,7],'metal')]
m=model(parts);m['display']={'gui':{'rotation':[25,225,0],'translation':[0,-1,0],'scale':[.65,.65,.65]},'ground':{'scale':[.35,.35,.35]}}
write('assets/fourthdimension/models/item/fixed_inverted_car.json',m)
write('assets/fourthdimension/models/item/fixed_inverted_track.json',model([box([0,8,1],[16,10,3],'metal'),box([0,8,13],[16,10,15],'metal'),box([0,17,6],[16,21,10],'seat'),box([2,10,2],[4,18,14],'metal'),box([12,10,2],[14,18,14],'metal')]))
write('data/fourthdimension/recipe/fixed_inverted_track.json',{'type':'minecraft:crafting_shaped','pattern':['III','CDC','III'],'key':{'I':{'tag':'c:ingots/iron'},'C':{'item':'minecraft:copper_ingot'},'D':{'item':'minecraft:purple_dye'}},'result':{'id':'fourthdimension:fixed_inverted_track','count':16}})
write('data/fourthdimension/recipe/fixed_inverted_car.json',{'type':'minecraft:crafting_shaped','pattern':['SIS','SCS','ITI'],'key':{'S':{'item':'create:red_seat'},'I':{'tag':'c:ingots/iron'},'T':{'item':'fourthdimension:fixed_inverted_track'},'C':{'item':'simulatedcoasters:coaster_cart'}},'result':{'id':'fourthdimension:fixed_inverted_car','count':1}})
for f in (OUT/'assets/fourthdimension/models').rglob('fixed_inverted_*.json'):
 for e in json.loads(f.read_text()).get('elements',[]):
  assert all(-16<=a<b<=32 for a,b in zip(e['from'],e['to'])),(f,e)
print('Generated Fixed Inverted resources; element bounds pass')
