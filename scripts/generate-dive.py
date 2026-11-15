#!/usr/bin/env python3
"""Original six-abreast fixed Dive row; content dimensions from adopted FD1702S."""
import json
from pathlib import Path
OUT=Path(__file__).resolve().parents[1]/'src/main/resources'
def write(p,data):
 f=OUT/p;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(json.dumps(data,indent=2)+'\n')
def box(a,b,t):return {'from':a,'to':b,'faces':{f:{'texture':'#'+t,'uv':[0,0,16,16]}for f in ('north','south','east','west','up','down')}}
def model(parts):return {'parent':'minecraft:block/block','textures':{'seat':'minecraft:block/white_concrete','metal':'minecraft:block/iron_block','harness':'minecraft:block/black_concrete','latch':'minecraft:block/yellow_concrete','particle':'minecraft:block/white_concrete'},'elements':parts}
for closed in (False,True):
 parts=[box([-2,-4,-8],[2,-2,24],'metal'),box([-2,-4,6],[2,8,10],'metal'),box([-7,8,2],[7,11,14],'seat'),box([-7,11,13],[7,29,16],'seat'),box([-6,4,-6],[6,6,2],'metal')]
 for x in (-5,5):
  parts += [box([x-1,13,2],[x+1,28,4],'harness'),box([x-1,26,3],[x+1,29,15],'harness')] if closed else [box([x-1,17,16],[x+1,31,18],'harness')]
 parts.append(box([-6,12,2],[6,15,4],'latch')if closed else box([-6,29,16],[6,31,18],'harness'))
 write(f'assets/fourthdimension/models/block/dive_seat_{"closed"if closed else "open"}.json',model(parts))
write('assets/fourthdimension/blockstates/dive_seat.json',{'variants':{f'facing={d},restrained={str(c).lower()}':{'model':f'fourthdimension:block/dive_seat_{"closed"if c else "open"}'}for d in ('north','south','east','west')for c in (False,True)}})
write('assets/fourthdimension/models/block/dive_frame.json',model([box([-8,12,3],[8,16,13],'metal'),box([0,8,7],[8,12,10],'latch')]))
write('assets/fourthdimension/blockstates/dive_frame.json',{'variants':{'':{'model':'fourthdimension:block/dive_frame'}}})
parts=[box([-4,4,6],[20,6,12],'metal')]
for x in (-2,2,6,10,14,18):parts += [box([x-1,6,6],[x+2,8,11],'seat'),box([x-1,8,10],[x+2,16,12],'seat'),box([x-1,9,6],[x,16,7],'harness'),box([x+1,9,6],[x+2,16,7],'harness'),box([x-1,2,3],[x+2,3,7],'metal')]
m=model(parts);m['display']={'gui':{'rotation':[25,225,0],'scale':[.6,.6,.6]},'ground':{'scale':[.3,.3,.3]}}
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
