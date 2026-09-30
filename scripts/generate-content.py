#!/usr/bin/env python3
"""Original cuboid models and vanilla crafting data. Uses only textures supplied by Minecraft."""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources'
def write(path,value):
    p=ROOT/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(value,indent=2)+'\n')
def box(a,b,texture):
    return {'from':a,'to':b,'faces':{face:{'texture':texture} for face in ('north','south','east','west','up','down')}}
for family,color in [('arrow','red_concrete'),('legacy','yellow_concrete')]:
    for closed in (False,True):
        parts=[box([1,2,2],[15,5,14],'#cushion'),box([1,4,12],[15,23,15],'#cushion'),
               box([0,1,2],[2,6,15],'#metal'),box([14,1,2],[16,6,15],'#metal')]
        # Two visible shoulder bars and a lap bar; open restraint folds behind the backrest.
        if closed:
            parts += [box([3,9,3],[5,23,5],'#restraint'),box([11,9,3],[13,23,5],'#restraint'),box([3,8,3],[13,10,5],'#restraint')]
        else:
            parts += [box([3,22,13],[5,30,15],'#restraint'),box([11,22,13],[13,30,15],'#restraint'),box([3,28,13],[13,30,15],'#restraint')]
        name=f'{family}_seat_{"closed" if closed else "open"}'
        write(f'assets/fourthdimension/models/block/{name}.json',{'parent':'minecraft:block/block',
              'textures':{'cushion':f'minecraft:block/{color}','metal':'minecraft:block/iron_block','restraint':'minecraft:block/black_concrete','particle':f'minecraft:block/{color}'},'elements':parts})
    variants={}
    for direction,angle in [('north',0),('east',90),('south',180),('west',270)]:
        for closed in (False,True):
            variants[f'facing={direction},restrained={str(closed).lower()}']={'model':f'fourthdimension:block/{family}_seat_{"closed" if closed else "open"}','y':angle}
    write(f'assets/fourthdimension/blockstates/{family}_seat.json',{'variants':variants})
for name,a,b in [('carrier_axle',[0,6,0],[16,10,3]),('wing_strut',[0,4,4],[16,12,12])]:
    write(f'assets/fourthdimension/models/block/{name}.json',{'parent':'minecraft:block/block','textures':{'metal':'minecraft:block/iron_block','particle':'minecraft:block/iron_block'},'elements':[box(a,b,'#metal')]})
    write(f'assets/fourthdimension/blockstates/{name}.json',{'variants':{'':{'model':f'fourthdimension:block/{name}'}}})
for name,family in [('arrow_car','arrow'),('legacy_zacspin_car','legacy')]:
    write(f'assets/fourthdimension/models/item/{name}.json',{'parent':f'fourthdimension:block/{family}_seat_closed'})
for name,pigment in [('arrow_track','red_dye'),('legacy_zacspin_track','yellow_dye')]:
    write(f'data/fourthdimension/recipe/{name}.json',{'type':'minecraft:crafting_shaped','pattern':['III','CDC','III'],
          'key':{'I':{'tag':'c:ingots/iron'},'C':{'item':'minecraft:copper_ingot'},'D':{'item':f'minecraft:{pigment}'}},'result':{'id':f'fourthdimension:{name}','count':16}})
for name,track in [('arrow_car','arrow_track'),('legacy_zacspin_car','legacy_zacspin_track')]:
    write(f'data/fourthdimension/recipe/{name}.json',{'type':'minecraft:crafting_shaped','pattern':['SAS','ITI','ICI'],
          'key':{'S':{'item':'create:red_seat'},'A':{'item':'minecraft:iron_bars'},'I':{'tag':'c:ingots/iron'},'T':{'item':f'fourthdimension:{track}'},'C':{'item':'simulatedcoasters:coaster_cart'}},'result':{'id':f'fourthdimension:{name}','count':1}})

# Original station panel and directional indicators.
base=[box([0,0,0],[16,3,16],'#body'),box([2,3,2],[14,11,14],'#body'),box([0,11,0],[16,16,16],'#panel'),box([6,4,14],[10,8,16],'#input')]
for ready in (False,True):
 for locked in (False,True):
  suffix=f'{int(ready)}{int(locked)}'
  model={'textures':{'particle':'minecraft:block/copper_block','body':'minecraft:block/copper_block','panel':'minecraft:block/iron_block','input':'minecraft:block/redstone_block','ready':f'minecraft:block/{"lime_concrete" if ready else "black_concrete"}','locked':f'minecraft:block/{"red_concrete" if locked else "black_concrete"}'},'elements':base+[box([3,12,-.1],[7,15,0],'#ready'),box([16,12,6],[16.1,15,10],'#locked')]}
  write(f'assets/fourthdimension/models/block/boarding_station_{suffix}.json',model)
variants={}
for facing,angle in [('north',0),('east',90),('south',180),('west',270)]:
 for ready in (False,True):
  for locked in (False,True):
   variants[f'facing={facing},ready={str(ready).lower()},locked={str(locked).lower()}']={'model':f'fourthdimension:block/boarding_station_{int(ready)}{int(locked)}','y':angle}
write('assets/fourthdimension/blockstates/boarding_station.json',{'variants':variants})
write('assets/fourthdimension/models/block/boarding_station.json',{'parent':'fourthdimension:block/boarding_station_00'})
