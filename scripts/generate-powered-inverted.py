#!/usr/bin/env python3
"""Original upright Powered Inverted Yaw seats, spindle and drive equipment."""
import itertools,json,struct,zlib
from pathlib import Path
import argparse
parser=argparse.ArgumentParser();parser.add_argument('--output',type=Path);args=parser.parse_args()
root=args.output or Path(__file__).resolve().parents[1]/'src/main/resources'
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
 p=root/f'assets/fourthdimension/textures/block/powered_inverted_yaw_{name}.png';p.parent.mkdir(parents=True,exist_ok=True)
 p.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',16,16,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b''))
for name,color in {'seat':(25,139,151),'metal':(152,167,178),'harness':(24,30,36),'signal':(233,175,46)}.items():tile(name,color)
tex={t:f'fourthdimension:block/powered_inverted_yaw_{t}' for t in ('seat','metal','harness','signal')};tex['particle']=tex['seat']
def model(parts):return {'parent':'minecraft:block/block','textures':tex,'elements':parts}
variants={}
for closed in (False,True):
 parts=box([2,4,2],[14,7,14],'seat')+box([2,7,12],[14,30,14],'seat')+box([1,-12,-1],[15,-10,14],'metal')+box([1,-10,10],[3,6,14],'metal')+box([13,-10,10],[15,6,14],'metal')
 if closed:parts+=box([3,8,1],[5,28,3],'harness')+box([11,8,1],[13,28,3],'harness')+box([3,26,2],[5,29,14],'harness')+box([11,26,2],[13,29,14],'harness')+box([3,8,1],[13,11,3],'signal')
 else:parts+=box([3,28,12],[5,32,14],'harness')+box([11,28,12],[13,32,14],'harness')+box([3,30,12],[13,32,14],'harness')
 name='powered_inverted_yaw_seat_'+('closed' if closed else 'open');write(f'assets/fourthdimension/models/block/{name}.json',model(parts))
 for face,angle in {'north':0,'east':90,'south':180,'west':270}.items():variants[f'facing={face},restrained={str(closed).lower()}']={'model':f'fourthdimension:block/{name}','y':angle}
write('assets/fourthdimension/blockstates/powered_inverted_yaw_seat.json',{'variants':variants})
for name,parts in {'spindle':box([7,0,7],[9,16,9],'metal'),'bearing':box([-14,4,0],[14,10,16],'metal')+box([-3,8,4],[3,12,12],'signal')}.items():
 write(f'assets/fourthdimension/models/block/powered_inverted_yaw_{name}.json',model(parts));write(f'assets/fourthdimension/blockstates/powered_inverted_yaw_{name}.json',{'variants':{'':{'model':f'fourthdimension:block/powered_inverted_yaw_{name}'}}})
parts=box([2,2,2],[14,14,14],'metal')+box([4,4,0],[12,12,2],'harness')+box([6,6,-1],[10,10,0],'signal')+box([6,6,14],[10,10,17],'metal')+box([4,10,2],[12,12,3],'seat')
write('assets/fourthdimension/models/block/powered_inverted_yaw_governor.json',model(parts))
write('assets/fourthdimension/blockstates/powered_inverted_yaw_governor.json',{'variants':{f'facing={f}':{'model':'fourthdimension:block/powered_inverted_yaw_governor',**r} for f,r in {'north':{},'south':{'y':180},'east':{'y':90},'west':{'y':270},'up':{'x':270},'down':{'x':90}}.items()}})
write('assets/fourthdimension/models/item/powered_inverted_yaw_governor.json',{'parent':'fourthdimension:block/powered_inverted_yaw_governor'})
parts=box([0,14,4],[16,16,12],'metal')+box([7,9,7],[9,14,9],'metal')+box([0,0,2],[16,2,14],'metal')
for x in (1,9):parts+=box([x,3,3],[x+6,5,12],'seat')+box([x,5,10],[x+6,12,12],'seat')+box([x+1,5,3],[x+2,11,5],'harness')+box([x+4,5,3],[x+5,11,5],'harness')
m=model(parts);m['display']={'gui':{'rotation':[30,225,0],'scale':[.8,.8,.8]},'firstperson_righthand':{'rotation':[0,135,0],'scale':[.4,.4,.4]}};write('assets/fourthdimension/models/item/powered_inverted_yaw_car.json',m)
write('assets/fourthdimension/models/item/powered_inverted_yaw_track.json',model(box([0,10,1],[16,12,3],'metal')+box([0,10,13],[16,12,15],'metal')+box([0,4,6],[16,7,10],'seat')+box([3,7,2],[5,11,14],'harness')+box([11,7,2],[13,11,14],'harness')))
recipes={
 'powered_inverted_yaw_track':(['III','RDR','III'],{'I':{'tag':'c:ingots/iron'},'R':{'item':'minecraft:redstone'},'D':{'item':'minecraft:cyan_dye'}},16),
 'powered_inverted_yaw_car':(['IBI','SCS','ITI'],{'S':{'item':'create:cyan_seat'},'B':{'item':'create:mechanical_bearing'},'C':{'item':'simulatedcoasters:coaster_cart'},'I':{'tag':'c:ingots/iron'},'T':{'item':'fourthdimension:powered_inverted_yaw_track'}},1),
 'powered_inverted_yaw_governor':(['IRI','SCS','IRI'],{'I':{'tag':'c:ingots/iron'},'R':{'item':'minecraft:redstone'},'S':{'item':'create:shaft'},'C':{'item':'create:clutch'}},1)}
for name,(pattern,key,count) in recipes.items():write(f'data/fourthdimension/recipe/{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':key,'result':{'id':'fourthdimension:'+name,'count':count}})
write('data/fourthdimension/loot_table/blocks/powered_inverted_yaw_governor.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'fourthdimension:powered_inverted_yaw_governor'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
for p in (root/'assets/fourthdimension/models').rglob('powered_inverted_yaw_*.json'):
 for e in json.loads(p.read_text()).get('elements',[]):assert all(-16<=a<b<=32 and b-a<=16.00001 for a,b in zip(e['from'],e['to'])),(p,e)
print('Generated bounded original upright yaw assets and recipes; shared language untouched.')
