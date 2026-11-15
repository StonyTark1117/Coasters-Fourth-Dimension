#!/usr/bin/env python3
"""Prepare original Suspended resources without touching production or shared generators."""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parent
OUT=ROOT/'resources'

def write(path,value):
 p=OUT/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(value,indent=2)+'\n')
def box(a,b,t):
 return {'from':a,'to':b,'faces':{f:{'texture':t} for f in ['north','south','east','west','up','down']}}
def model(parts):
 return {'parent':'minecraft:block/block','textures':{'cushion':'minecraft:block/orange_concrete','metal':'minecraft:block/iron_block','body':'minecraft:block/cyan_terracotta','restraint':'minecraft:block/black_concrete','accent':'minecraft:block/orange_concrete','particle':'minecraft:block/cyan_terracotta'},'elements':parts}

for closed in (False,True):
 parts=[box([1,2,2],[15,5,14],'#cushion'),box([1,4,12],[15,23,15],'#cushion'),
        box([0,1,2],[2,6,15],'#metal'),box([14,1,2],[16,6,15],'#metal')]
 if closed:
  parts += [box([3,9,3],[5,23,5],'#restraint'),box([11,9,3],[13,23,5],'#restraint'),box([3,8,3],[13,10,5],'#restraint')]
 else:
  # Open shoulders remain inside the accepted cabin's -1.75 top, using existing
  # fold-behind geometry. This is not the use/collision shape.
  parts += [box([3,22,13],[5,30,15],'#restraint'),box([11,22,13],[13,30,15],'#restraint'),box([3,28,13],[13,30,15],'#restraint')]
 write(f'assets/fourthdimension/models/block/suspended_seat_{"closed" if closed else "open"}.json',model(parts))
variants={}
for direction,angle in [('north',0),('east',90),('south',180),('west',270)]:
 for closed in (False,True):
  variants[f'facing={direction},restrained={str(closed).lower()}']={'model':f'fourthdimension:block/suspended_seat_{"closed" if closed else "open"}','y':angle}
write('assets/fourthdimension/blockstates/suspended_seat.json',{'variants':variants})
# Carrier block (0,-3,0), native child pivot (0,.25,.5). World graph y origin -4.75;
# y8..10 => -4.25..-4.125. Door openings on both sides remain at z0..16.
parts=[box([-16,8,-12],[16,10,28],'#metal'),box([-16,10,-12],[16,15,-10],'#body'),
       box([-16,10,26],[16,25,28],'#body')]
for x0,x1 in [(-16,-14),(14,16)]:
 for z0,z1 in [(-12,0),(16,28)]:
  parts += [box([x0,10,z0],[x1,30,z1],'#body'),box([x0,28,z0],[x1,30,z1],'#accent')]
write('assets/fourthdimension/models/block/suspended_cabin.json',model(parts))
write('assets/fourthdimension/blockstates/suspended_cabin.json',{'variants':{'':{'model':'fourthdimension:block/suspended_cabin'}}})
# Lock indicator on a separate visible block; never used to infer native latch state.
for locked in (False,True):
 m=model([box([5,4,0],[11,12,16],'#metal'),box([4,6,2],[12,10,14],'#restraint'),box([5,7,-.1],[11,11,0],'#lamp')])
 m['textures']['lamp']='minecraft:block/lime_concrete' if locked else 'minecraft:block/orange_concrete'
 write(f'assets/fourthdimension/models/block/suspended_lock_{"locked" if locked else "free"}.json',m)
write('assets/fourthdimension/blockstates/suspended_lock.json',{'variants':{f'locked={str(v).lower()}':{'model':f'fourthdimension:block/suspended_lock_{"locked" if v else "free"}'} for v in (False,True)}})
# Recognizable miniature with overhead bogie, longitudinal hanger and two-place cabin.
parts=[box([3,22,4],[13,25,12],'#metal'),box([7,13,2],[9,23,4],'#metal'),box([7,13,12],[9,23,14],'#metal'),
       box([6,12,2],[10,14,14],'#metal'),box([0,2,1],[16,4,15],'#body'),box([0,4,13],[16,10,15],'#body')]
for x in (1,9):
 parts += [box([x,4,4],[x+6,6,11],'#cushion'),box([x,6,10],[x+6,13,12],'#cushion'),box([x+1,7,4],[x+2,13,5],'#restraint'),box([x+4,7,4],[x+5,13,5],'#restraint')]
m=model(parts);m['display']={'gui':{'rotation':[25,225,0],'translation':[0,-1,0],'scale':[.65,.65,.65]},'ground':{'scale':[.35,.35,.35]}}
write('assets/fourthdimension/models/item/suspended_car.json',m)
track=model([box([0,8,1],[16,10,3],'#metal'),box([0,8,13],[16,10,15],'#metal'),box([0,17,6],[16,21,10],'#body'),box([2,10,2],[4,18,14],'#metal'),box([12,10,2],[14,18,14],'#metal')])
write('assets/fourthdimension/models/item/suspended_track.json',track)
write('data/fourthdimension/recipe/suspended_track.json',{'type':'minecraft:crafting_shaped','pattern':['III','CDC','III'],'key':{'I':{'tag':'c:ingots/iron'},'C':{'item':'minecraft:copper_ingot'},'D':{'item':'minecraft:orange_dye'}},'result':{'id':'fourthdimension:suspended_track','count':16}})
write('data/fourthdimension/recipe/suspended_car.json',{'type':'minecraft:crafting_shaped','pattern':['SAS','ITI','ICI'],'key':{'S':{'item':'create:red_seat'},'A':{'item':'minecraft:iron_bars'},'I':{'tag':'c:ingots/iron'},'T':{'item':'fourthdimension:suspended_track'},'C':{'item':'simulatedcoasters:coaster_cart'}},'result':{'id':'fourthdimension:suspended_car','count':1}})
# These are merge inputs, not replacement shared language files.
(ROOT/'language-input.json').write_text(json.dumps({
 'item.fourthdimension.suspended_car':'Suspended Coaster Car',
 'item.fourthdimension.suspended_track':'Suspended Coaster Track',
 'block.fourthdimension.suspended_seat':'Suspended Cabin Seat',
 'block.fourthdimension.suspended_cabin':'Suspended Cabin',
 'block.fourthdimension.suspended_lock':'Suspended Swing Lock',
 'fourthdimension.suspended.instructions':'Build matching overhead track; bind a level dock and the lift section. Close restraints before dispatch. The cabin stays locked on the lift, then swings through the curves. Return to the dock to stop, align, open and unload.',
 'fourthdimension.suspended.lock_pending':'Aligning for swing lock — travel held',
 'fourthdimension.suspended.locked':'Swing locked',
 'fourthdimension.suspended.free':'Free swing',
 'fourthdimension.suspended.limit':'Swing limit — damping and travel stop',
 'fourthdimension.suspended.not_docked':'Stop at the bound level dock before opening restraints'
},indent=2)+'\n')
# Verify emitted resource parse, bounded JSON cuboids and texture references.
count=0;elements=0
for p in OUT.rglob('*.json'):
 data=json.loads(p.read_text());count+=1
 for e in data.get('elements',[]):
  elements+=1
  assert all(-16<=lo<hi<=32 for lo,hi in zip(e['from'],e['to'])),p
  assert all(face['texture'][1:] in data['textures'] for face in e['faces'].values()),p
print(json.dumps({'resource_files':count,'model_elements':elements,'output':str(OUT),'scope':'preparation only; no registration or runtime validation'}))
