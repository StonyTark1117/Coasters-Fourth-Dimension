#!/usr/bin/env python3
"""Original four-seat transverse Roller Ball; legal cuboids and explicit face UVs."""
import json
from pathlib import Path
root=Path(__file__).resolve().parents[1]/'src/main/resources'
def write(path,obj):
 p=root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,indent=2)+'\n')
tex={'seat':'minecraft:block/lime_concrete','metal':'minecraft:block/iron_block','pad':'minecraft:block/black_concrete','cue':'minecraft:block/yellow_concrete','particle':'minecraft:block/lime_concrete'}
def box(a,b,t):
 assert all(-16<=v<=32 for v in a+b)
 x,y,z=[min(16,hi-lo) for lo,hi in zip(a,b)]
 return {'from':a,'to':b,'faces':{f:{'texture':t,'uv':[0,0,*uv]} for f,uv in [('north',(x,y)),('south',(x,y)),('east',(z,y)),('west',(z,y)),('up',(x,z)),('down',(x,z))]}}
def model(name,parts):write(f'assets/fourthdimension/models/block/{name}.json',{'textures':tex,'elements':parts})
def single(name,parts):
 model(name,parts);write(f'assets/fourthdimension/blockstates/{name}.json',{'variants':{'':{'model':f'fourthdimension:block/{name}'}}})
variants={}
for left in (False,True):
 for closed in (False,True):
  for facing in ('north','south','east','west'):
   # Offset is in body X, independent of the front/back seat rotation.
   x=12 if left else 4
   parts=[box([1,5,1],[15,8,15],'#seat'),box([1,8,13],[15,27,16],'#seat'),box([1,-10,0],[15,-7,9],'#metal'),box([6,-7,5],[10,5,8],'#metal'),box([6,8,10],[10,24,13],'#metal'),box([6,22,10],[10,26,24],'#metal')]
   if closed:parts += [box([2,11,1],[4,25,3],'#pad'),box([12,11,1],[14,25,3],'#pad'),box([2,24,3],[4,27,15],'#pad'),box([12,24,3],[14,27,15],'#pad'),box([2,10,1],[14,13,3],'#cue')]
   else:parts += [box([2,11,16],[4,28,18],'#pad'),box([12,11,16],[14,28,18],'#pad'),box([2,26,16],[14,28,18],'#cue')]
   # Only north/south are authored placement; rotate mesh and preserve the fixed X hip.
   for part in parts:
    if facing=='south':
     lo,hi=part['from'][:],part['to'][:];part['from']=[16-hi[0],lo[1],16-hi[2]];part['to']=[16-lo[0],hi[1],16-lo[2]]
    for k in ('from','to'):
     part[k][0]+=x;part[k][2]+=8
   name=f'roller_ball_seat_{int(left)}_{int(closed)}_{facing}';model(name,parts)
   variants[f'facing={facing},left={str(left).lower()},restrained={str(closed).lower()}']={'model':f'fourthdimension:block/{name}'}
write('assets/fourthdimension/blockstates/roller_ball_seat.json',{'variants':variants})
single('roller_ball_axle',[box([0,6,6],[32,10,10],'#metal')])
frame_variants={}
for latched in (False,True):
 for damping in (False,True):
  name=f'roller_ball_frame_{int(latched)}_{int(damping)}'
  model(name,[box([4,0,3],[12,6,13],'#metal'),box([3,1,5],[13,5,11],'#seat' if latched else '#cue' if damping else '#metal')])
  frame_variants[f'latched={str(latched).lower()},damping={str(damping).lower()}']={'model':f'fourthdimension:block/{name}'}
write('assets/fourthdimension/blockstates/roller_ball_frame.json',{'variants':frame_variants})
(root/'assets/fourthdimension/models/block/roller_ball_frame.json').unlink(missing_ok=True)
single('roller_ball_controller',[box([3,0,3],[13,16,13],'#metal'),box([2,10,2],[14,14,14],'#seat')])
single('roller_ball_ballast',[])
write('assets/fourthdimension/models/item/roller_ball_car.json',{'parent':'minecraft:block/block','textures':tex,'elements':[box([1,9,6],[15,11,10],'#metal'),box([0,3,3],[5,6,13],'#seat'),box([11,3,3],[16,6,13],'#seat'),box([0,6,11],[5,13,13],'#pad'),box([11,6,11],[16,13,13],'#pad'),box([6,3,6],[10,9,10],'#metal')],'display':{'gui':{'rotation':[30,225,0],'scale':[.8,.8,.8]}}})
write('assets/fourthdimension/models/item/roller_ball_track.json',{'parent':'fourthdimension:item/legacy_zacspin_track','textures':{'spine':'minecraft:block/lime_concrete','particle':'minecraft:block/lime_concrete'}})
write('assets/fourthdimension/models/item/roller_ball_lift_control.json',{'parent':'minecraft:block/block','textures':tex,'elements':[box([4,0,7],[9,10,10],'#metal'),box([1,9,6],[12,15,11],'#seat'),box([3,11,5],[10,13,6],'#pad'),box([5,7,4],[7,14,6],'#cue')],'display':{'gui':{'rotation':[0,0,-35],'scale':[1,1,1]}}})
write('data/fourthdimension/recipe/roller_ball_track.json',{'type':'minecraft:crafting_shaped','pattern':['III','CDC','III'],'key':{'I':{'tag':'c:ingots/iron'},'C':{'item':'minecraft:copper_ingot'},'D':{'item':'minecraft:lime_dye'}},'result':{'id':'fourthdimension:roller_ball_track','count':16}})
write('data/fourthdimension/recipe/roller_ball_car.json',{'type':'minecraft:crafting_shaped','pattern':['SBS','STS','ICI'],'key':{'S':{'item':'create:lime_seat'},'B':{'item':'create:mechanical_bearing'},'I':{'tag':'c:ingots/iron'},'T':{'item':'fourthdimension:roller_ball_track'},'C':{'item':'simulatedcoasters:coaster_cart'}},'result':{'id':'fourthdimension:roller_ball_car','count':1}})
write('data/fourthdimension/recipe/roller_ball_lift_control.json',{'type':'minecraft:crafting_shaped','pattern':[' I ','RCR',' I '],'key':{'I':{'tag':'c:ingots/iron'},'R':{'item':'minecraft:redstone'},'C':{'item':'minecraft:clock'}},'result':{'id':'fourthdimension:roller_ball_lift_control','count':1}})
print('Roller Ball assets generated')
