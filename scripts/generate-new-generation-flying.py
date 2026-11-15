#!/usr/bin/env python3
"""Deterministic original compound Flying meshes, bounded UVs and small authored material tiles."""
import json,re,itertools,struct,zlib
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'src/main/resources'
def write(p,d):
 f=OUT/p;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(json.dumps(d,indent=2)+'\n')
def box(a,b,t):
 steps=[[a[i]+16*k for k in range(int((b[i]-a[i]-1e-7)//16)+1)]+[b[i]]for i in range(3)]
 return [{'from':[round(v,6)for v in lo],'to':[round(v,6)for v in hi],'faces':{f:{'texture':'#'+t,'uv':[0,0,16,16]}for f in ('north','south','east','west','up','down')}}for spans in itertools.product(*[list(zip(s,s[1:]))for s in steps])for lo,hi in[(list(zip(*spans))[0],list(zip(*spans))[1])]]
def tile(name,color):
 raw=b''
 for y in range(16):
  row=bytearray()
  for x in range(16):
   shade=16 if x in(1,14)and y in(1,14)else -14 if x in(0,15)or y in(0,15)else ((x*3+y*5)%5)-2
   row.extend(max(0,min(255,c+shade))for c in color);row.append(255)
  raw+=b'\0'+row
 def chunk(k,d):return struct.pack('>I',len(d))+k+d+struct.pack('>I',zlib.crc32(k+d))
 p=OUT/f'assets/fourthdimension/textures/block/new_generation_flying_{name}.png';p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',16,16,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b''))
for name,color in {'seat':(89,65,151),'metal':(147,165,176),'harness':(29,36,46),'latch':(238,170,53),'locked':(91,195,143)}.items():tile(name,color)
def model(parts):return {'parent':'minecraft:block/block','textures':{**{t:f'fourthdimension:block/new_generation_flying_{t}'for t in('seat','metal','harness','latch','locked')},'particle':'fourthdimension:block/new_generation_flying_seat'},'elements':parts}
for body,legs in itertools.product((False,True),repeat=2):
 parts=[]
 for a,b,t in [([1,5,1],[15,8,15],'seat'),([1,8,14],[15,29,17],'seat'),([2,-11,-1],[14,-8,6],'metal'),([6,-8,4],[10,5,7],'metal')]:parts+=box(a,b,t)
 for x in(3,13):parts+=box([x-1,11,1],[x+1,27,3],'harness')+box([x-1,25,3],[x+1,28,16],'harness')if body else box([x-1,11,16],[x+1,30,18],'harness')
 parts+=box([2,10,1],[14,13,3],'latch')if body else box([2,28,16],[14,30,18],'harness')
 parts+=box([2,-6,-2],[14,-3,0],'latch')+box([1,-6,-1],[3,4,1],'harness')+box([13,-6,-1],[15,4,1],'harness')if legs else box([0,-6,5],[2,4,7],'harness')+box([14,-6,5],[16,4,7],'harness')
 write(f'assets/fourthdimension/models/block/new_generation_flying_seat_{int(body)}{int(legs)}.json',model(parts))
write('assets/fourthdimension/blockstates/new_generation_flying_seat.json',{'variants':{f'facing={d},restrained={str(b).lower()},legs={str(g).lower()}':{'model':f'fourthdimension:block/new_generation_flying_seat_{int(b)}{int(g)}'}for d in('north','south','east','west')for b,g in itertools.product((False,True),repeat=2)}})
source=(ROOT/'src/main/java/dev/fourthdimension/vehicle/NewGenerationFlyingDeckBlock.java').read_text().split('BOXES={',1)[1].split('};',1)[0];boxes=[list(map(float,s.split(',')))for s in re.findall(r'\{([^{}]+)\}',source)];frames=[]
for x,y,z in itertools.product(range(-2,3),range(-1,4),range(-1,2)):
 origin=(x-.5,y-.5,z-.5);parts=[]
 for b in boxes:
  lo=[max(origin[i],b[i])for i in range(3)];hi=[min(origin[i]+1,b[i+3])for i in range(3)]
  if all(lo[i]<hi[i]for i in range(3)):parts+=box([(lo[i]-origin[i])*16 for i in range(3)],[(hi[i]-origin[i])*16 for i in range(3)],'metal'if y<2 else'seat')
 if parts:write(f'assets/fourthdimension/models/block/new_generation_flying_deck_{len(frames)}.json',model(parts));frames.append((x,y,z))
write('assets/fourthdimension/blockstates/new_generation_flying_deck.json',{'variants':{f'part={i}':{'model':f'fourthdimension:block/new_generation_flying_deck_{i}'}for i in range(len(frames))}})
parts=box([0,22,6],[16,24,10],'metal')+box([7,24,7],[9,29,9],'latch')
for x in(3,10):parts+=box([x,7,5],[x+4,9,11],'seat')+box([x,9,10],[x+4,18,12],'seat')+box([x,3,2],[x+4,5,6],'latch')
m=model(parts);m['display']={'gui':{'rotation':[25,225,0],'translation':[0,-1,0],'scale':[.7,.7,.7]},'ground':{'scale':[.35,.35,.35]}};write('assets/fourthdimension/models/item/new_generation_flying_car.json',m)
write('assets/fourthdimension/models/item/new_generation_flying_track.json',model(box([0,8,1],[16,10,3],'metal')+box([0,8,13],[16,10,15],'metal')+box([0,17,6],[16,21,10],'seat')+box([2,10,2],[4,18,14],'latch')+box([12,10,2],[14,18,14],'metal')))
write('assets/fourthdimension/models/block/yaw_marker.json',model(box([1,0,1],[15,3,15],'metal')+box([6,3,6],[10,12,10],'seat')+box([3,12,3],[13,15,13],'latch')+box([7,15,7],[9,16,9],'locked')))
write('assets/fourthdimension/blockstates/yaw_marker.json',{'variants':{'':{'model':'fourthdimension:block/yaw_marker'}}});write('assets/fourthdimension/models/item/yaw_marker.json',{'parent':'fourthdimension:block/yaw_marker'})
recipes={'new_generation_flying_track':(['III','CDC','III'],{'I':{'tag':'c:ingots/iron'},'C':{'item':'minecraft:copper_ingot'},'D':{'item':'minecraft:magenta_dye'}},16),'new_generation_flying_car':(['ISI','SCS','ITI'],{'I':{'tag':'c:ingots/iron'},'S':{'item':'create:red_seat'},'C':{'item':'simulatedcoasters:coaster_cart'},'T':{'item':'fourthdimension:new_generation_flying_track'}},1),'yaw_marker':([' G ',' C ','III'],{'G':{'item':'minecraft:glowstone_dust'},'C':{'item':'minecraft:compass'},'I':{'tag':'c:ingots/iron'}},1)}
for name,(pattern,key,count)in recipes.items():write(f'data/fourthdimension/recipe/{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':key,'result':{'id':'fourthdimension:'+name,'count':count}})
count=0
for p in(OUT/'assets/fourthdimension/models').rglob('*.json'):
 if not(p.name.startswith('new_generation_flying_')or p.name=='yaw_marker.json'):continue
 for e in json.loads(p.read_text()).get('elements',[]):
  assert all(-16<=a<b<=32 and b-a<=16.00001 for a,b in zip(e['from'],e['to'])),(p,e)
  assert all(f['uv']==[0,0,16,16]for f in e['faces'].values())
 count+=1
print(f'Generated {count} bounded models, {len(frames)} deck cells and five authored material tiles')
