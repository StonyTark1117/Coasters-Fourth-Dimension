#!/usr/bin/env python3
"""Generate original Suspended-only block models, items and recipes."""
import json
from fd917_materials import box as production_box, model as production_model, item_display
from pathlib import Path
ROOT=Path(__file__).resolve().parent
OUT=ROOT.parent/'src/main/resources'

OWNED=set((Path(__file__).resolve().parents[1]/'docs/evidence/fd917/art/requested-output-paths.txt').read_text().splitlines())
def write(path,data):
 target=OUT/path
 rel=target.relative_to(Path(__file__).resolve().parents[1]).as_posix()
 if rel not in OWNED and not path.startswith(('assets/fourthdimension/models/block/fd917/','assets/fourthdimension/fd917_articulation/')):return
 target.parent.mkdir(parents=True,exist_ok=True);target.write_text(json.dumps(data,indent=2)+'\n')
def box(a,b,t):return production_box(a,b,t)
def model(parts):return production_model(parts,'cyan',padding='orange')
for closed in (False,True):
 parts=[box([1,2,2],[15,5,14],'cushion'),box([2,5,12],[14,22,14],'cushion'),
        box([0,2,14],[16,22,16],'body'),box([3,22,12],[13,26,16],'body'),
        box([4,22,11.75],[12,25,14],'cushion'),
        box([0,1,3],[2,9,14],'metal'),box([14,1,3],[16,9,14],'metal')]
 for x in (1,13):parts += [box([x,20,13],[x+2,24,16],'metal'),box([x,21,12.75],[x+2,23,13],'contact')]
 write(f'assets/fourthdimension/models/block/suspended_seat_{"closed" if closed else "open"}.json',model(parts))
variants={}
for direction,angle in [('north',0),('east',90),('south',180),('west',270)]:
 for closed in (False,True):
  variants[f'facing={direction},restrained={str(closed).lower()}']={'model':f'fourthdimension:block/suspended_seat_{"closed" if closed else "open"}','y':angle}
write('assets/fourthdimension/blockstates/suspended_seat.json',{'variants':variants})
restraint=[]
for x in (2,12):restraint += [box([x,9,3],[x+2,22,5],'restraint'),box([x,20,4],[x+2,22,14],'metal')]
restraint += [box([3,8,3],[13,10,5],'restraint'),box([6,8,2.75],[10,9.5,3],'latch')]
write('assets/fourthdimension/models/block/fd917/suspended_restraint.json',model(restraint))
write('assets/fourthdimension/fd917_articulation/suspended_seat.json',{'parts':[{'model':'fourthdimension:block/fd917/suspended_restraint','property':'restrained','pivot':[8,22,14],'axis':'x','open_degrees':55,'rotate_facing':True,'when':{}}]})
# Carrier block (0,-3,0), native child pivot (0,.25,.5). World graph y origin -4.75;
# y8..10 => -4.25..-4.125. Door openings on both sides remain at z0..16.
parts=[box([-16,8,-12],[16,10,28],'#metal'),box([-16,10,-12],[16,15,-10],'#body'),
       box([-16,10,26],[16,25,28],'#body')]
parts += [box([-13,15,-12],[13,19,-10],'body'),box([-10,19,-12],[10,21,-10],'body'),
          box([-14,11,-12.1],[14,12,-12],'contact'),box([-12,23,26],[12,26,28],'body')]
for x0,x1 in [(-16,-14),(14,16)]:
 for z0,z1 in [(-12,0),(16,28)]:
  parts += [box([x0,10,z0],[x1,30,z1],'#body'),box([x0,28,z0],[x1,30,z1],'#accent')]
# Join the cabin shell to the journal's fore/aft suspension rods.
parts += [box([-1,10,-12],[1,32,-10],'#metal'),box([-1,10,26],[1,32,28],'#metal')]
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
m=model(parts);m['display']=item_display(.65)
write('assets/fourthdimension/models/item/suspended_car.json',m)
track=model([box([0,8,1],[16,10,3],'#metal'),box([0,8,13],[16,10,15],'#metal'),box([0,17,6],[16,21,10],'#body'),box([2,10,2],[4,18,14],'#metal'),box([12,10,2],[14,18,14],'#metal')])
write('assets/fourthdimension/models/item/suspended_track.json',track)
write('data/fourthdimension/recipe/suspended_track.json',{'type':'minecraft:crafting_shaped','pattern':['III','CDC','III'],'key':{'I':{'tag':'c:ingots/iron'},'C':{'item':'minecraft:copper_ingot'},'D':{'item':'minecraft:orange_dye'}},'result':{'id':'fourthdimension:suspended_track','count':16}})
write('data/fourthdimension/recipe/suspended_car.json',{'type':'minecraft:crafting_shaped','pattern':['SAS','ITI','ICI'],'key':{'S':{'item':'create:red_seat'},'A':{'item':'fourthdimension:suspended_latch'},'I':{'tag':'c:ingots/iron'},'T':{'item':'fourthdimension:suspended_track'},'C':{'item':'simulatedcoasters:coaster_cart'}},'result':{'id':'fourthdimension:suspended_car','count':1}})

# Native child journal: pivot x=0,y=.25,z=.5. Fore/aft fork inner faces are ±1.5.
for locked in (False,True):
 parts=[box([-3,0,0],[3,8,16],'#metal'),box([-4,2,2],[4,6,14],'#restraint'),box([-3,3,-.1],[3,7,0],'#lamp'),
        box([-1,3,-16],[1,5,32],'#metal'),box([-1,-16,-12],[1,4,-10],'#metal'),box([-1,-16,26],[1,4,28],'#metal')]
 # Journal block is at y=-1 for native connectivity; retain the same physical pivot.
 for element in parts:
  element['from'][1]+=16; element['to'][1]+=16
 m=model(parts);m['textures']['lamp']='minecraft:block/lime_concrete' if locked else 'minecraft:block/orange_concrete'
 write(f'assets/fourthdimension/models/block/suspended_lock_{"locked" if locked else "free"}.json',m)
for front in (False,True):
 parts=[box([7,10,-2 if front else 16],[9,32,0 if front else 18],'#metal'),
        box([7,30,-1 if front else -8],[9,32,24 if front else 17],'#metal')]
 write(f'assets/fourthdimension/models/block/suspended_hanger_{"front" if front else "rear"}.json',model(parts))
write('assets/fourthdimension/blockstates/suspended_hanger.json',{'variants':{f'front={str(f).lower()}':{'model':f'fourthdimension:block/suspended_hanger_{"front" if f else "rear"}'} for f in (False,True)}})
write('assets/fourthdimension/models/block/suspended_controller.json',model([box([4,3,4],[12,13,12],'#metal'),box([3,7,3],[13,10,13],'#accent')]))
write('assets/fourthdimension/blockstates/suspended_controller.json',{'variants':{'':{'model':'fourthdimension:block/suspended_controller'}}})
write('assets/fourthdimension/models/block/suspended_keel.json',model([]))
write('assets/fourthdimension/blockstates/suspended_keel.json',{'variants':{'':{'model':'fourthdimension:block/suspended_keel'}}})
write('assets/fourthdimension/models/item/suspended_latch.json',model([box([3,3,2],[13,6,14],'#metal'),box([4,6,3],[6,14,13],'#metal'),box([10,6,3],[12,14,13],'#metal'),box([5,9,5],[11,12,11],'#accent')]))
write('data/fourthdimension/recipe/suspended_latch.json',{'type':'minecraft:crafting_shaped','pattern':[' I ','CRC',' I '],'key':{'I':{'tag':'c:ingots/iron'},'C':{'item':'minecraft:copper_ingot'},'R':{'item':'minecraft:redstone'}},'result':{'id':'fourthdimension:suspended_latch','count':1}})

for path in (OUT/'assets/fourthdimension/models').rglob('suspended_*.json'):
 data=json.loads(path.read_text())
 for element in data.get('elements',[]):
  assert all(-16<=a<b<=32 for a,b in zip(element['from'],element['to'])),path
  assert all(all(0<=value<=16 for value in face['uv']) for face in element['faces'].values()),path
print('Generated and checked Suspended resources')
