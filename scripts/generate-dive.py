#!/usr/bin/env python3
"""Original six-abreast fixed Dive row; content dimensions from adopted FD1702S."""
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
def model(parts):return production_model(parts,'white')
for closed in (False,True):
 parts=[box([-2,-4,-8],[2,-2,24],'metal'),box([-2,-4,6],[2,8,10],'metal'),
        box([-7,8,2],[7,11,14],'seat'),box([-6,11,12],[6,27,14],'seat'),
        box([-8,9,14],[8,26,17],'paint'),box([-5,26,13],[5,31,17],'paint'),
        box([-4,27,12],[4,30,14],'seat')]
 for x in (-7,5):parts += [box([x,24,13],[x+2,29,16.75],'metal'),box([x,26,12.75],[x+2,28,13],'contact')]
 write(f'assets/fourthdimension/models/block/dive_seat_{"closed"if closed else "open"}.json',model(parts))
write('assets/fourthdimension/blockstates/dive_seat.json',{'variants':{f'facing={d},restrained={str(c).lower()}':{'model':f'fourthdimension:block/dive_seat_{"closed"if c else "open"}'}for d in ('north','south','east','west')for c in (False,True)}})
restraint=[]
for x in (-6,4):restraint += [box([x,13,2],[x+2,27,4],'harness'),box([x,25,3],[x+2,27,15],'metal')]
restraint += [box([-5,12,2],[5,14,4],'harness'),box([-2,12,1.75],[2,13.5,2],'latch')]
write('assets/fourthdimension/models/block/fd917/dive_restraint.json',model(restraint))
write('assets/fourthdimension/fd917_articulation/dive_seat.json',{'parts':[{'model':'fourthdimension:block/fd917/dive_restraint','property':'restrained','pivot':[0,27,15],'axis':'x','open_degrees':55,'rotate_facing':False,'when':{}}]})
write('assets/fourthdimension/models/block/dive_frame.json',model([box([-8,12,3],[8,16,13],'metal'),box([0,8,7],[8,12,10],'latch')]))
write('assets/fourthdimension/blockstates/dive_frame.json',{'variants':{'':{'model':'fourthdimension:block/dive_frame'}}})
parts=[box([-4,4,6],[20,6,12],'metal')]
for x in (-2,2,6,10,14,18):parts += [box([x-1,6,6],[x+2,8,11],'seat'),box([x-1,8,10],[x+2,17,12],'paint'),box([x-1,9,6],[x,16,7],'harness'),box([x+1,9,6],[x+2,16,7],'harness')]
m=model(parts);m['display']=item_display(.6)
write('assets/fourthdimension/models/item/dive_car.json',m)
write('assets/fourthdimension/models/item/dive_track.json',model([box([0,9,2],[16,11,4],'metal'),box([0,9,12],[16,11,14],'metal'),box([0,2,6],[16,5,10],'seat'),box([2,5,3],[4,9,13],'metal'),box([12,5,3],[14,9,13],'metal')]))
write('data/fourthdimension/recipe/dive_track.json',{'type':'minecraft:crafting_shaped','pattern':['III','CDC','III'],'key':{'I':{'tag':'c:ingots/iron'},'C':{'item':'minecraft:copper_ingot'},'D':{'item':'minecraft:white_dye'}},'result':{'id':'fourthdimension:dive_track','count':16}})
write('data/fourthdimension/recipe/dive_car.json',{'type':'minecraft:crafting_shaped','pattern':['SSS','SSS','ICT'],'key':{'S':{'item':'create:red_seat'},'I':{'tag':'c:ingots/iron'},'T':{'item':'fourthdimension:dive_track'},'C':{'item':'simulatedcoasters:coaster_cart'}},'result':{'id':'fourthdimension:dive_car','count':1}})
for f in (OUT/'assets/fourthdimension/models').rglob('dive_*.json'):
 for e in json.loads(f.read_text()).get('elements',[]):assert all(-16<=a<b<=32 for a,b in zip(e['from'],e['to'])),(f,e)
print('Dive content preparation generated; dimensions/bounds only, no mechanics acceptance')

write('assets/fourthdimension/models/block/dive_hold.json',model([box([0,0,0],[16,12,16],'metal'),box([2,12,2],[14,14,14],'harness'),box([3,14,3],[7,15,13],'latch'),box([9,14,3],[13,15,13],'seat')]))
write('assets/fourthdimension/blockstates/dive_hold.json',{'variants':{'':{'model':'fourthdimension:block/dive_hold'}}})
write('assets/fourthdimension/models/item/dive_hold.json',{'parent':'fourthdimension:block/dive_hold'})
write('data/fourthdimension/recipe/dive_hold.json',{'type':'minecraft:crafting_shaped','pattern':['IRI','CTC','IRI'],'key':{'I':{'tag':'c:ingots/iron'},'R':{'item':'minecraft:redstone'},'C':{'item':'minecraft:copper_ingot'},'T':{'item':'fourthdimension:dive_track'}},'result':{'id':'fourthdimension:dive_hold','count':1}})
write('data/fourthdimension/loot_table/blocks/dive_hold.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'fourthdimension:dive_hold'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})

write('assets/fourthdimension/models/block/dive_controller.json',model([box([2,4,2],[14,16,14],'metal'),box([3,12,3],[13,16,13],'latch')]))
write('assets/fourthdimension/blockstates/dive_controller.json',{'variants':{'':{'model':'fourthdimension:block/dive_controller'}}})
