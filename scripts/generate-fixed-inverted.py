#!/usr/bin/env python3
"""Original Fixed Inverted models in the native chassis frame, plus usable recipes."""
import json
from fd917_materials import box as production_box, model as production_model, item_display
from pathlib import Path
OUT=Path(__file__).resolve().parents[1]/'src/main/resources'
OWNED=set((Path(__file__).resolve().parents[1]/'docs/evidence/fd917/art/requested-output-paths.txt').read_text().splitlines())
def write(path,data):
 target=OUT/path
 rel=target.relative_to(Path(__file__).resolve().parents[1]).as_posix()
 if rel not in OWNED and not path.startswith(('assets/fourthdimension/models/block/fd917/','assets/fourthdimension/fd917_articulation/')):return
 target.parent.mkdir(parents=True,exist_ok=True);target.write_text(json.dumps(data,indent=2)+'\n')
def box(a,b,t):return production_box(a,b,t)
def model(parts):return production_model(parts,'purple')
def worldbox(lo,hi,pos,t):
 # input native X/Z, graph Y. Block origin relative to graph is (x-.5,y-.1875,z-.5).
 origin=[pos[0]-.5,pos[1]-.1875,pos[2]-.5]
 return box([round(16*(lo[i]-origin[i]),5)for i in range(3)],[round(16*(hi[i]-origin[i]),5)for i in range(3)],t)
for closed in (False,True):
 # X=0 and Y=.9375 are exact fractional seat centers in the containing block.
 parts=[box([-7,12,2],[7,15,14],'seat'),box([-6,15,12],[6,28,14],'seat'),
        box([-8,12,14],[8,28,17],'paint'),box([-5,28,13],[5,32,17],'paint'),
        box([-4,28,12],[4,31,14],'seat'),box([-2,7,8],[2,13,30],'metal'),
        box([-8,7,20],[8,11,24],'metal'),box([-8,7,28],[8,11,32],'metal')]
 # No front foot platform or shin rods: four seats visibly hang below the track.
 for x in (-7,5):parts += [box([x,26,13],[x+2,31,16.75],'metal'),box([x,28,12.75],[x+2,30,13],'contact')]
 write(f'assets/fourthdimension/models/block/fixed_inverted_seat_{"closed"if closed else "open"}.json',model(parts))
 write(f'assets/fourthdimension/models/block/fixed_inverted_seat_{"closed"if closed else "open"}_outer.json',model(parts+[box([8,7,28],[12,16,32],'metal')]))
write('assets/fourthdimension/blockstates/fixed_inverted_seat.json',{'variants':{f'facing={d},restrained={str(c).lower()},outer={str(o).lower()}':{'model':f'fourthdimension:block/fixed_inverted_seat_{"closed"if c else "open"}{"_outer"if o else ""}'}for d in ('north','south','east','west')for c in (False,True)for o in (False,True)}})
restraint=[]
for x in (-6,4):restraint += [box([x,17,2],[x+2,29,4],'harness'),box([x,27,3],[x+2,29,15],'metal')]
restraint += [box([-5,16,2],[5,18,4],'harness'),box([-2,16,1.75],[2,17.5,2],'latch')]
write('assets/fourthdimension/models/block/fd917/fixed_inverted_restraint.json',model(restraint))
write('assets/fourthdimension/fd917_articulation/fixed_inverted_seat.json',{'parts':[{'model':'fourthdimension:block/fd917/fixed_inverted_restraint','property':'restrained','pivot':[0,29,15],'axis':'x','open_degrees':55,'rotate_facing':False,'when':{}}]})
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
 if pos==(-2,-4,0):
  # The four seat-block rear crossbars start at x=-1.5. Bridge the end post to
  # the first crossbar in the existing bottom frame cell, without adding a ride block.
  parts.append(worldbox([-2.25,-3.55,1.25],[-1.5,-3.30,1.5],pos,'metal'))
 write(f'assets/fourthdimension/models/block/fixed_inverted_frame_{i}.json',model(parts))
write('assets/fourthdimension/blockstates/fixed_inverted_frame.json',{'variants':{f'part={i}':{'model':f'fourthdimension:block/fixed_inverted_frame_{min(i,8)}'}for i in range(10)}})
# Connect the visible upper rear crossbar to the native bearing. No decorative spine through heads.
parts=[worldbox([-.5,-.9,1.25],[.5,-.75,1.5],(0,-1,0),'metal'),worldbox([-.125,-.85,0],[.125,-.7,1.375],(0,-1,0),'metal'),worldbox([-.125,-.8,-.125],[.125,0,.125],(0,-1,0),'metal')]
write('assets/fourthdimension/models/block/fixed_inverted_controller.json',model(parts))
write('assets/fourthdimension/blockstates/fixed_inverted_controller.json',{'variants':{'':{'model':'fourthdimension:block/fixed_inverted_controller'}}})
# Four distinct cushions identify this fixed row in inventory.
parts=[box([0,23,7],[16,25,10],'metal'),box([0,5,12],[2,24,14],'metal'),box([14,5,12],[16,24,14],'metal')]
for x in (1,5,9,13):
 parts += [box([x-1,5,6],[x+2,7,11],'seat'),box([x-1,7,10],[x+2,15,12],'paint'),box([x-1,8,6],[x,14,7],'harness'),box([x+1,8,6],[x+2,14,7],'harness')]
m=model(parts);m['display']=item_display(.65)
write('assets/fourthdimension/models/item/fixed_inverted_car.json',m)
write('assets/fourthdimension/models/item/fixed_inverted_track.json',model([box([0,8,1],[16,10,3],'metal'),box([0,8,13],[16,10,15],'metal'),box([0,17,6],[16,21,10],'seat'),box([2,10,2],[4,18,14],'metal'),box([12,10,2],[14,18,14],'metal')]))
write('data/fourthdimension/recipe/fixed_inverted_track.json',{'type':'minecraft:crafting_shaped','pattern':['ICI','IDI','ICI'],'key':{'I':{'tag':'c:ingots/iron'},'C':{'item':'minecraft:copper_ingot'},'D':{'item':'minecraft:purple_dye'}},'result':{'id':'fourthdimension:fixed_inverted_track','count':16}})
write('data/fourthdimension/recipe/fixed_inverted_car.json',{'type':'minecraft:crafting_shaped','pattern':['SIS','SCS','ITI'],'key':{'S':{'item':'create:red_seat'},'I':{'tag':'c:ingots/iron'},'T':{'item':'fourthdimension:fixed_inverted_track'},'C':{'item':'simulatedcoasters:coaster_cart'}},'result':{'id':'fourthdimension:fixed_inverted_car','count':1}})
for f in (OUT/'assets/fourthdimension/models').rglob('fixed_inverted_*.json'):
 for e in json.loads(f.read_text()).get('elements',[]):
  assert all(-16<=a<b<=32 for a,b in zip(e['from'],e['to'])),(f,e)
print('Generated Fixed Inverted resources; element bounds pass')
