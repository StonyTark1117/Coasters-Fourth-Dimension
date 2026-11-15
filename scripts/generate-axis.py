#!/usr/bin/env python3
"""Original Axis fork and restrained two-seat pod, in native plot coordinates."""
import json
from pathlib import Path
root=Path(__file__).resolve().parents[1]/'src/main/resources'
def write(path,value):
 p=root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(value,indent=2)+'\n')
def box(a,b,t):
 assert all(-16<=v<=32 for v in a+b),(a,b)
 x,y,z=[min(16,hi-lo) for lo,hi in zip(a,b)]
 return {'from':a,'to':b,'faces':{f:{'texture':t,'uv':[0,0,*uv]} for f,uv in [('north',(x,y)),('south',(x,y)),('east',(z,y)),('west',(z,y)),('up',(x,z)),('down',(x,z))]}}
tex={'cushion':'minecraft:block/cyan_concrete','metal':'minecraft:block/iron_block','bar':'minecraft:block/black_concrete','accent':'minecraft:block/yellow_concrete','particle':'minecraft:block/cyan_concrete'}
def model(name,parts):write(f'assets/fourthdimension/models/block/{name}.json',{'textures':tex,'elements':parts})
def single(name,parts):
 model(name,parts);write(f'assets/fourthdimension/blockstates/{name}.json',{'variants':{'':{'model':f'fourthdimension:block/{name}'}}})
variants={}
for closed in [False,True]:
 parts=[box([1,5,1],[15,8,15],'#cushion'),box([1,8,13],[15,29,16],'#cushion'),box([2,0,4],[5,5,13],'#metal'),box([11,0,4],[14,5,13],'#metal'),box([1,-2,-6],[15,0,2],'#metal'),box([3,0,-1],[5,6,3],'#metal'),box([11,0,-1],[13,6,3],'#metal')]
 if closed:parts +=[box([2,12,1],[4,27,3],'#bar'),box([12,12,1],[14,27,3],'#bar'),box([2,25,3],[4,28,15],'#bar'),box([12,25,3],[14,28,15],'#bar'),box([2,10,1],[14,13,3],'#accent')]
 else:parts +=[box([2,12,16],[4,30,18],'#bar'),box([12,12,16],[14,30,18],'#bar'),box([2,28,16],[14,30,18],'#accent')]
 name=f'axis_seat_{int(closed)}';model(name,parts)
 for facing,rotation in [('north',0),('east',90),('south',180),('west',270)]:variants[f'facing={facing},restrained={str(closed).lower()}']={'model':f'fourthdimension:block/{name}','y':rotation}
write('assets/fourthdimension/blockstates/axis_seat.json',{'variants':variants})
# Shaft along native Z. Thin side yoke connects the two seat cells below the pivot.
single('axis_journal',[box([-3,12,-12],[3,16,28],'#metal'),box([-16,0,11],[16,3,14],'#metal'),box([-2,2,11],[2,14,14],'#accent')])
single('axis_keel',[])
single('axis_controller',[box([3,0,3],[13,10,13],'#metal'),box([0,9.4,0],[16,12.6,16],'#cushion'),box([4,12.6,4],[12,15,12],'#accent')])
positions=[(x,z) for x in range(-4,1) for z in [-2,2]]+[(0,-1),(0,1)]
variants={}
for i,(x,z) in enumerate(positions):
 parts=[box([0,9.4,2 if z>0 else 10],[16,12.6,6 if z>0 else 14],'#cushion')] if abs(z)==2 else [box([6,9.4,0],[10,12.6,16],'#cushion')]
 if x==-4:parts +=[box([5,7.5,-4 if z>0 else 8],[11,14.5,8 if z>0 else 20],'#metal')]
 model(f'axis_frame_{i}',parts);variants[f'part={i}']={'model':f'fourthdimension:block/axis_frame_{i}'}
write('assets/fourthdimension/blockstates/axis_frame.json',{'variants':variants})
parts=[box([11,3,5],[16,6,11],'#metal'),box([0,9,2],[16,11,4],'#cushion'),box([0,9,12],[16,11,14],'#cushion'),box([1,3,4],[8,6,12],'#cushion'),box([1,6,10],[8,13,12],'#bar')]
write('assets/fourthdimension/models/item/axis_car.json',{'textures':tex,'elements':parts,'display':{'gui':{'rotation':[30,225,0],'scale':[.8,.8,.8]}}})
write('assets/fourthdimension/models/item/axis_track.json',{'parent':'fourthdimension:item/legacy_zacspin_track','textures':{'spine':'minecraft:block/cyan_concrete','particle':'minecraft:block/cyan_concrete'}})
write('data/fourthdimension/recipe/axis_track.json',{'type':'minecraft:crafting_shaped','pattern':['III','CDC','III'],'key':{'I':{'tag':'c:ingots/iron'},'C':{'item':'minecraft:copper_ingot'},'D':{'item':'minecraft:cyan_dye'}},'result':{'id':'fourthdimension:axis_track','count':16}})
write('data/fourthdimension/recipe/axis_car.json',{'type':'minecraft:crafting_shaped','pattern':['SBS','ITI','ICI'],'key':{'S':{'item':'create:cyan_seat'},'B':{'item':'create:mechanical_bearing'},'I':{'tag':'c:ingots/iron'},'T':{'item':'fourthdimension:axis_track'},'C':{'item':'simulatedcoasters:coaster_cart'}},'result':{'id':'fourthdimension:axis_car','count':1}})
p=root/'assets/fourthdimension/lang/en_us.json';lang=json.loads(p.read_text())
for name,title in [('axis_car','Axis Car'),('axis_track','Axis Track'),('flying_car','Conventional Flying Car'),('flying_track','Conventional Flying Track')]:lang[f'item.fourthdimension.{name}']=title
for name,title in [('axis_seat','Axis Seat'),('axis_controller','Axis Chassis'),('axis_frame','Axis Outrigger'),('axis_journal','Longitudinal Roll Bearing'),('axis_keel','Axis Ballast'),('flying_seat','Flying Seat'),('flying_controller','Flying Chassis'),('flying_frame','Flying Fork'),('flying_lock','Flying Posture Lock')]:lang[f'block.fourthdimension.{name}']=title
p.write_text(json.dumps(lang,indent=2)+'\n')
print('Axis assets generated; all elements within model bounds and all UV faces explicit.')
