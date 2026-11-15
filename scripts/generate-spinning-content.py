#!/usr/bin/env python3
"""Original S1 four-seat turntable; all centers match the native fractional seating definition."""
import json
import sys
from pathlib import Path
root=Path(__file__).resolve().parents[1]/'src/main/resources'
def write(path,value):
 p=root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(value,indent=2)+'\n')
def box(a,b,t):
 # Extended elements must not derive UVs from their out-of-block coordinates.
 # Bound each face to its own atlas sprite; geometry and texture identities stay fixed.
 x,y,z=[min(16,hi-lo) for lo,hi in zip(a,b)]
 faces={f:{'texture':t,'uv':[0,0,*uv]} for f,uv in
        [('north',(x,y)),('south',(x,y)),('east',(z,y)),('west',(z,y)),('up',(x,z)),('down',(x,z))]}
 return {'from':a,'to':b,'faces':faces}
tex={'cushion':'minecraft:block/orange_concrete','metal':'minecraft:block/iron_block','bar':'minecraft:block/black_concrete','particle':'minecraft:block/orange_concrete'}
variants={}
for outer in [False,True]:
 for facing in ['north','south','east','west']:
  for closed in [False,True]:
   cx=9.6 if outer else 6.4;cz=12 if facing=='south' else 4
   # Model both outward rows explicitly; rotation does not reverse the fractional side offset.
   front=1 if facing=='south' else -1
   def part(x0,y0,z0,x1,y1,z1,t):
    a=[cx+x0,y0,cz+front*z0];b=[cx+x1,y1,cz+front*z1]
    return box([min(a[i],b[i]) for i in range(3)],[max(a[i],b[i]) for i in range(3)],t)
   parts=[part(-5,10,-5,5,12,5,'#cushion'),part(-5,12,-6,5,29,-4,'#cushion'),part(-6,3,-5,-4,12,5,'#metal'),part(4,3,-5,6,12,5,'#metal')]
   if closed:parts +=[part(-4,18,2,-2,29,4,'#bar'),part(2,18,2,4,29,4,'#bar'),part(-4,17,2,4,19,4,'#bar')]
   else:parts +=[part(-4,28,-5,-2,32,-3,'#bar'),part(2,28,-5,4,32,-3,'#bar'),part(-4,30,-5,4,32,-3,'#bar')]
   name=f'spinning_seat_{int(outer)}_{facing}_{int(closed)}'
   write(f'assets/fourthdimension/models/block/{name}.json',{'textures':tex,'elements':parts})
   variants[f'outer={str(outer).lower()},facing={facing},restrained={str(closed).lower()}']={'model':f'fourthdimension:block/{name}'}
write('assets/fourthdimension/blockstates/spinning_seat.json',{'variants':variants})
for name,parts in [('spin_bearing',[box([0,-8,0],[16,-6,16],'#metal')]),('spinning_controller',[box([2,0,2],[14,5,14],'#metal'),box([5,5,5],[11,10,11],'#cushion')])]:
 write(f'assets/fourthdimension/models/block/{name}.json',{'textures':tex,'elements':parts})
 write(f'assets/fourthdimension/blockstates/{name}.json',{'variants':{'':{'model':f'fourthdimension:block/{name}'}}})
parts=[box([3,2,3],[13,5,13],'#metal'),box([0,5,0],[16,7,16],'#metal')]
for x in [1,9]:
 for z in [0,10]:parts += [box([x,7,z],[x+6,9,z+6],'#cushion'),box([x,9,z+2],[x+6,15,z+4],'#cushion')]
write('assets/fourthdimension/models/item/spinning_car.json',{'textures':tex,'elements':parts,'display':{'gui':{'rotation':[30,225,0],'scale':[.8,.8,.8]}}})
write('assets/fourthdimension/models/item/spinning_track.json',{'parent':'fourthdimension:item/legacy_zacspin_track','textures':{'spine':'minecraft:block/orange_concrete','particle':'minecraft:block/orange_concrete'}})
write('data/fourthdimension/recipe/spinning_track.json',{'type':'minecraft:crafting_shaped','pattern':['III','CDC','III'],'key':{'I':{'tag':'c:ingots/iron'},'C':{'item':'minecraft:copper_ingot'},'D':{'item':'minecraft:orange_dye'}},'result':{'id':'fourthdimension:spinning_track','count':16}})
write('data/fourthdimension/recipe/spinning_car.json',{'type':'minecraft:crafting_shaped','pattern':['SSS','BTB','ICI'],'key':{'S':{'item':'create:orange_seat'},'B':{'item':'create:mechanical_bearing'},'I':{'tag':'c:ingots/iron'},'T':{'item':'fourthdimension:spinning_track'},'C':{'item':'simulatedcoasters:coaster_cart'}},'result':{'id':'fourthdimension:spinning_car','count':1}})
if '--skip-language' not in sys.argv:
 p=root/'assets/fourthdimension/lang/en_us.json';lang=json.loads(p.read_text());lang.update({'item.fourthdimension.spinning_car':'S1 Spinning Car','item.fourthdimension.spinning_track':'S1 Spinning Track','block.fourthdimension.spinning_seat':'Spinning Seat','block.fourthdimension.spin_bearing':'Vertical Spin Bearing','block.fourthdimension.spinning_controller':'S1 Spinning Chassis'});p.write_text(json.dumps(lang,indent=2)+'\n')
