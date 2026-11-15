#!/usr/bin/env python3
"""Original S2 inversion harness, turntable and stress-supplied governor assets only."""
import itertools,json,struct,zlib
from pathlib import Path
root=Path(__file__).resolve().parents[1]/'src/main/resources'
def write(path,value):
 p=root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(value,indent=2)+'\n')
def box(a,b,t):
 steps=[[a[i]+16*k for k in range(int((b[i]-a[i]-1e-7)//16)+1)]+[b[i]] for i in range(3)]
 return [{'from':list(lo),'to':list(hi),'faces':{f:{'texture':'#'+t,'uv':[0,0,16,16]} for f in ('north','south','east','west','up','down')}} for spans in itertools.product(*[list(zip(s,s[1:])) for s in steps]) for lo,hi in [list(zip(*spans))]]
def tile(name,color):
 raw=b''
 for y in range(16):
  row=bytearray()
  for x in range(16):
   shade=-12 if x in(0,15) or y in(0,15) else 10 if x in(2,13) and y in(2,13) else ((x+y)%3)-1
   row.extend(max(0,min(255,c+shade)) for c in color);row.append(255)
  raw+=b'\0'+row
 def chunk(k,d):return struct.pack('>I',len(d))+k+d+struct.pack('>I',zlib.crc32(k+d))
 p=root/f'assets/fourthdimension/textures/block/s2_spinning_{name}.png';p.parent.mkdir(parents=True,exist_ok=True)
 p.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',16,16,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b''))
for name,color in {'seat':(151,37,66),'metal':(152,167,178),'harness':(24,30,36),'signal':(233,175,46)}.items():tile(name,color)
tex={t:f'fourthdimension:block/s2_spinning_{t}' for t in ('seat','metal','harness','signal')};tex['particle']=tex['seat']
def model(parts):return {'parent':'minecraft:block/block','textures':tex,'elements':parts}
variants={}
for outer,facing,closed in itertools.product((False,True),('north','south','east','west'),(False,True)):
 cx=9.6 if outer else 6.4;cz=12 if facing=='south' else 4;front=1 if facing=='south' else -1
 def part(x0,y0,z0,x1,y1,z1,t):
  a=[cx+x0,y0,cz+front*z0];b=[cx+x1,y1,cz+front*z1]
  return box([min(a[i],b[i]) for i in range(3)],[max(a[i],b[i]) for i in range(3)],t)
 parts=part(-5,10,-5,5,12,5,'seat')+part(-5,12,-6,5,29,-4,'seat')
 # Original continuous shell, supported footboard and deep over-shoulder harness.
 parts+=part(-6,3,-5,-4,12,5,'metal')+part(4,3,-5,6,12,5,'metal')+part(-6,2,-5,6,4,11,'metal')
 if closed:parts+=part(-4,12,2,-2,29,4,'harness')+part(2,12,2,4,29,4,'harness')+part(-4,12,2,4,15,4,'signal')+part(-4,27,-4,-2,29,4,'harness')+part(2,27,-4,4,29,4,'harness')
 else:parts+=part(-4,28,-5,-2,32,-3,'harness')+part(2,28,-5,4,32,-3,'harness')+part(-4,30,-5,4,32,-3,'harness')
 name=f's2_spinning_seat_{int(outer)}_{facing}_{int(closed)}';write(f'assets/fourthdimension/models/block/{name}.json',model(parts))
 variants[f'outer={str(outer).lower()},facing={facing},restrained={str(closed).lower()}']={'model':f'fourthdimension:block/{name}'}
write('assets/fourthdimension/blockstates/s2_spinning_seat.json',{'variants':variants})
write('assets/fourthdimension/models/block/s2_spinning_bearing.json',model(box([0,-8,0],[16,-6,16],'metal')))
write('assets/fourthdimension/blockstates/s2_spinning_bearing.json',{'variants':{'':{'model':'fourthdimension:block/s2_spinning_bearing'}}})
parts=box([2,2,2],[14,14,14],'metal')+box([4,4,0],[12,12,2],'harness')+box([6,6,-1],[10,10,0],'signal')+box([6,6,14],[10,10,17],'metal')
write('assets/fourthdimension/models/block/s2_spinning_governor.json',model(parts))
write('assets/fourthdimension/blockstates/s2_spinning_governor.json',{'variants':{f'facing={f}':{'model':'fourthdimension:block/s2_spinning_governor',**rotation} for f,rotation in {'north':{},'south':{'y':180},'east':{'y':90},'west':{'y':270},'up':{'x':270},'down':{'x':90}}.items()}})
write('assets/fourthdimension/models/item/s2_spinning_governor.json',{'parent':'fourthdimension:block/s2_spinning_governor'})
parts=box([2,2,2],[14,6,14],'metal')
for x,z in itertools.product((1,9),(0,10)):
 parts+=box([x,6,z],[x+6,8,z+6],'seat')+box([x,8,z+2],[x+6,15,z+4],'seat')+box([x+1,8,z],[x+2,14,z+2],'harness')+box([x+4,8,z],[x+5,14,z+2],'harness')+box([x,3,z],[x+6,5,z+6],'metal')
m=model(parts);m['display']={'gui':{'rotation':[30,225,0],'scale':[.8,.8,.8]},'firstperson_righthand':{'rotation':[0,135,0],'scale':[.4,.4,.4]},'firstperson_lefthand':{'rotation':[0,225,0],'scale':[.4,.4,.4]}}
write('assets/fourthdimension/models/item/s2_spinning_car.json',m)
write('assets/fourthdimension/models/item/s2_spinning_track.json',model(box([0,8,1],[16,10,3],'metal')+box([0,8,13],[16,10,15],'metal')+box([0,2,6],[16,5,10],'seat')+box([3,5,2],[5,9,14],'harness')+box([11,5,2],[13,9,14],'harness')))
recipes={
 's2_spinning_track':(['III','RDR','III'],{'I':{'tag':'c:ingots/iron'},'R':{'item':'minecraft:redstone'},'D':{'item':'minecraft:red_dye'}},16),
 's2_spinning_car':(['SBS','SCS','ITI'],{'S':{'item':'create:red_seat'},'B':{'item':'create:mechanical_bearing'},'C':{'item':'simulatedcoasters:coaster_cart'},'I':{'tag':'c:ingots/iron'},'T':{'item':'fourthdimension:s2_spinning_track'}},1),
 's2_spinning_governor':(['IRI','SCS','IRI'],{'I':{'tag':'c:ingots/iron'},'R':{'item':'minecraft:redstone'},'S':{'item':'create:shaft'},'C':{'item':'create:clutch'}},1)}
for name,(pattern,key,count) in recipes.items():write(f'data/fourthdimension/recipe/{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':key,'result':{'id':'fourthdimension:'+name,'count':count}})
for p in (root/'assets/fourthdimension/models').rglob('s2_spinning_*.json'):
 for e in json.loads(p.read_text()).get('elements',[]):
  assert all(-16<=a<b<=32 and b-a<=16.00001 for a,b in zip(e['from'],e['to'])),(p,e)
print('Generated S2-only bounded models, four original material tiles and three recipes; shared language untouched.')
