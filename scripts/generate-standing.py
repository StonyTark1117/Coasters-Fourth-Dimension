#!/usr/bin/env python3
"""Original FD2201 rigid standing and native sliding Surf body assets only."""
from pathlib import Path
import json,copy,itertools
ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/fourthdimension'
def write(path,value):
 value=copy.deepcopy(value)
 # Vanilla parses elements before NeoForge applies its corner-origin root transform.
 # Preserve the exact physical geometry while encoding tall supports in legal coordinates.
 if path.startswith('models/block/') and any(v < -16 or v > 32 for e in value.get('elements',[]) for k in ('from','to') for v in e[k]):
  for e in value['elements']:
   for k in ('from','to'):e[k]=[v/2 for v in e[k]]
  value['transform']={'scale':[2,2,2],'origin':'corner'}
 p=A/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(value,indent=2)+'\n')
def box(name,lo,hi,tex='metal'):
 return {'name':name,'from':list(lo),'to':list(hi),'faces':{s:{'texture':'#'+tex,'uv':[0,0,16,16]} for s in ('north','south','east','west','up','down')}}
def model(es):
 return {'parent':'minecraft:block/block','ambientocclusion':True,'textures':{k:'fourthdimension:block/fd917/'+v for k,v in dict(metal='structural',contact='machined',paint='paint_cyan',body='paint_cyan',seat='upholstery_black',cushion='upholstery_black',restraint='restraint',rubber='rubber',latch='paint_orange',particle='paint_cyan').items()},'elements':es}
def seat(right,surf):
 x=2.4 if right else 13.6
 es=[box('standing footplate',(x-6.8,5.4,.8),(x+6.8,7,15.2)),box('non-slip standing tread',(x-6.5,7,.9),(x+6.5,7.08,15),'rubber'),box('rear rigid spine',(x-1.4,5.4,14),(x+1.4,34,16)),box('lower rear brace',(x-5,9,14),(x+5,11,16)),box('original slim rear fairing',(x-4.5,12,15.8),(x+4.5,32,16.8),'paint'),box('standing rear pad',(x-3.5,18,11.8),(x+3.5,27,14),'cushion'),box('upper hinge crossmember',(x-5.8,30,14),(x+5.8,32,16))]
 for sign in (-1,1):
  h=x+sign*4.8;es.append(box('fixed shoulder hinge',(h-.8,29.7,13),(h+.8,32.3,16),'contact'))
 if surf:
  # Centre of fixed guide is local z=18.4. A .01-block radial gap to the .10-square rail.
  for y in (10,27):
   es += [box('guide shoe front',(x-1.8,y,16.6),(x+1.8,y+3,17.44),'contact'),box('guide shoe rear',(x-1.8,y,19.36),(x+1.8,y+3,20.2),'contact'),box('guide shoe left',(x-1.8,y,17.44),(x-.96,y+3,19.36),'contact'),box('guide shoe right',(x+.96,y,17.44),(x+1.8,y+3,19.36),'contact'),box('shoe structural bridge',(x-1.4,y,14),(x+1.4,y+2,16.6))]
 else:
  es.append(box('rigid post connector',(x-1.4,8,14),(x+1.4,11,19.2)))
 return es

def restraint(right):
 x=2.4 if right else 13.6;es=[]
 for sign in(-1,1):
  h=x+sign*4.8
  es += [box('shoulder restraint arm',(h-.65,30,3),(h+.65,32,15),'metal'),box('shoulder retention pad',(h-1,20,2),(h+1,31,4.5),'restraint')]
 es += [box('retention crossbar',(x-5.8,19,2),(x+5.8,21,4.5),'restraint'),box('visible latch',(x-1.5,19,1.7),(x+1.5,20.5,2),'latch')]
 return es

def frame(surf):
 es=[box('native chassis saddle',(3,4,-28),(13,14,-20)),box('longitudinal support beam',(5,10,-24),(11,14,4)),box('crossmember',(-14,10,0),(30,14,4))]
 for x in(-2.4,18.4):
  es += [box('guide root bracket',(x-2,12,0),(x+2,17,4)),box('fixed guide rail' if surf else 'fixed standing post',(x-.8,14,1.6),(x+.8,60 if surf else 43,3.2),'contact' if surf else 'metal')]
 return es

def translated(es,delta):
 result=copy.deepcopy(es)
 for e in result:
  for k in('from','to'):e[k]=[round(a+b,8) for a,b in zip(e[k],delta)]
 return result

carriage=[box("paired stance carriage crossmember",(-3.8,9,14),(19.8,11,16))]
write("models/block/surf_carriage.json",model(carriage))
write("blockstates/surf_carriage.json",{"variants":{"":{"model":"fourthdimension:block/surf_carriage"}}})
variants={};parts=[]
for surf,right in itertools.product((False,True),repeat=2):
 name=('surf' if surf else 'standing')+'_seat_'+('right' if right else 'left')
 write('models/block/'+name+'.json',model(seat(right,surf)))
 for facing,closed in itertools.product(('north','south','east','west'),(False,True)):
  key=f'facing={facing},restrained={str(closed).lower()},right={str(right).lower()},surf={str(surf).lower()}'
  variants[key]={'model':'fourthdimension:block/'+name}
for right in(False,True):
 name='standing_restraint_'+('right' if right else 'left')
 write('models/block/'+name+'.json',model(restraint(right)))
 parts.append({'model':'fourthdimension:block/'+name,'property':'restrained','pivot':[2.4 if right else 13.6,31,14],'axis':'x','open_degrees':55,'rotate_facing':False,'when':{'right':str(right).lower()}})
write('blockstates/standing_seat.json',{'variants':variants})
write('fd917_articulation/standing_seat.json',{'parts':parts})
write('blockstates/standing_frame.json',{'variants':{'surf='+str(s).lower():{'model':'fourthdimension:block/'+('surf' if s else 'standing')+'_frame'} for s in(False,True)}})
for surf in(False,True):
 name='surf' if surf else 'standing';write('models/block/'+name+'_frame.json',model(frame(surf)))
 es=translated(frame(surf),(0,0,16))
 if surf:es+=translated(carriage,(0,16,0))
 for right in(False,True):es+=translated(seat(right,surf)+restraint(right),(16 if right else -16,16,0))
 # A true miniature of this arrangement, not an unrelated seated car icon.
 miniature=copy.deepcopy(es)
 for e in miniature:
  for k in('from','to'):e[k]=[round((x-(8,22,8)[i])*.23+(8,7,8)[i],8) for i,x in enumerate(e[k])]
 item=model(miniature);item['display']={'gui':{'rotation':[25,140,0],'translation':[0,0,0],'scale':[1.15,1.15,1.15]},'ground':{'scale':[.7,.7,.7]},'fixed':{'rotation':[0,180,0],'scale':[1,1,1]}}
 write('models/item/'+name+'_car.json',item)
 write('models/item/'+name+'_track.json',{'parent':'fourthdimension:item/arrow_track'})
write('models/block/standing_controller.json',model([box('standing controller saddle',(2,4,2),(14,16,14)),box('standing control cover',(3,16,3),(13,17,13),'paint')]))
# FamilyControllers owns the actual controller and its entity; only original assets are generated here.
write('blockstates/standing_controller.json',{'variants':{'':{'model':'fourthdimension:block/standing_controller'}}})
write('models/item/standing_controller.json',{'parent':'fourthdimension:block/standing_controller'})
print('Standing/Surf original models, item miniatures and articulation generated')
