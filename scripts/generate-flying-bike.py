#!/usr/bin/env python3
"""FD2204 original rigid prone cradles, linked hold-downs and real-drive governor housing."""
import json, copy, itertools
from pathlib import Path
A=Path(__file__).resolve().parents[1]/'src/main/resources/assets/fourthdimension'
def write(p,v):
 p=A/p;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,indent=2)+'\n')
def box(name,a,b,t='metal'):
 return dict(name=name,**{'from':list(a),'to':list(b)},faces={f:dict(texture='#'+t,uv=[0,0,16,16]) for f in ['north','south','east','west','up','down']})
def model(es):
 return dict(parent='minecraft:block/block',textures={k:'fourthdimension:block/fd917/'+v for k,v in dict(metal='structural',contact='machined',paint='paint_cyan',body='paint_cyan',seat='upholstery_black',cushion='upholstery_black',restraint='restraint',rubber='rubber',latch='paint_orange',particle='paint_cyan').items()},elements=es)
def shift(es,d):
 es=copy.deepcopy(es)
 for e in es:
  for k in ['from','to']:e[k]=[a+b for a,b in zip(e[k],d)]
 return es
seat=[box('rigid cradle pan',(2,5,-1),(14,7,30)),box('prone chest cushion',(3,7,0),(13,8.5,12),'cushion'),box('pelvis cushion',(3,7,14),(13,8.5,21),'cushion'),box('rear load spine',(6,5,14),(10,32,16)),box('rear calf support',(3,7,25),(13,9,30),'cushion'),box('hinge crossmember',(1,9,30),(15,11,33))]
for x in [2,14]:
 seat += [box('rear hinge load leg',(x-.8,5,29),(x+.8,10,33)),box('handlebar root',(x-.6,5,-1),(x+.6,12,1)),box('forward grip',(x-.8,11,-5),(x+.8,12.5,1),'rubber'),box('fixed restraint hinge',(x-.8,8.5,30),(x+.8,11.5,33),'contact')]
restraint=[box('linked calf hold',(2,11,25),(14,13,28),'restraint'),box('body hold crossbar',(2,13,4),(14,15,7),'restraint'),box('body latch',(7,12.7,3.5),(9,15.2,4),'latch')]
for x in [2,14]:restraint += [box('one-piece restraint side beam',(x-.7,10,4),(x+.7,12,32)),box('body pad riser',(x-.7,10,4),(x+.7,15,7),'restraint')]
# Vanilla elements must remain in [-16,32]; NeoForge root translation restores exact authored coordinates.
seat_export=model(shift(seat,(0,0,-1)));seat_export['transform']={'translation':[0,0,1/16],'origin':'corner'}
write('models/block/flying_bike_seat.json',seat_export);write('models/block/flying_bike_restraint.json',model(restraint))
write('blockstates/flying_bike_seat.json',{'variants':{f'facing={f},restrained={str(c).lower()},load_marker={str(m).lower()}':{'model':'fourthdimension:block/flying_bike_seat'} for f,c,m in itertools.product(['north','south','east','west'],[False,True],[False,True])}})
write('fd917_articulation/flying_bike_seat.json',{'parts':[{'model':'fourthdimension:block/flying_bike_restraint','property':'restrained','pivot':[8,10,31.5],'axis':'x','open_degrees':55,'rotate_facing':False}]})
frames=[]
for i in range(4):
 es=[box('continuous descending spar',(6,0,14),(10,12 if i<2 else 16,16))]
 if i<2:es += [box('overhead crosshead',(6 if i==0 else -10,8,4),(26 if i==0 else 10,12,16)),box('crosshead rear knee',(6,6,12),(10,12,16))]
 frames.append(es);write(f'models/block/flying_bike_frame_{i}.json',model(es))
write('blockstates/flying_bike_frame.json',{'variants':{f'part={i}':{'model':f'fourthdimension:block/flying_bike_frame_{i}'} for i in range(4)}})
controller=[box('native bearing saddle',(2,5,2),(14,12,14)),box('native bearing neck',(6,12,6),(10,16,10),'contact'),box('original controller cover',(3,11,3),(13,12,13),'paint')]
write('models/block/flying_bike_controller.json',model(controller));write('blockstates/flying_bike_controller.json',{'variants':{'':{'model':'fourthdimension:block/flying_bike_controller'}}});write('models/item/flying_bike_controller.json',{'parent':'fourthdimension:block/flying_bike_controller'})
gov=[box('governor casing',(1,1,2),(15,15,14),'paint'),box('output bearing',(5,5,0),(11,11,3),'contact'),box('rear input bearing',(5,5,13),(11,11,16),'contact'),box('output direction arrow',(6,15,2),(10,16,8),'latch')]
write('models/block/flying_bike_governor.json',model(gov));write('models/item/flying_bike_governor.json',{'parent':'fourthdimension:block/flying_bike_governor'})
vs={}
for f,rot in [('north',{}),('east',{'y':90}),('south',{'y':180}),('west',{'y':270}),('up',{'x':270}),('down',{'x':90})]:vs['facing='+f]={'model':'fourthdimension:block/flying_bike_governor',**rot}
write('blockstates/flying_bike_governor.json',{'variants':vs})
whole=shift(controller,(0,32,0))
for i,(x,y) in enumerate([(-16,32),(16,32),(-16,16),(16,16)]):whole+=shift(frames[i],(x,y,0))
for x in [-16,16]:whole+=shift(seat+restraint,(x,0,0))
for e in whole:
 for k in ['from','to']:e[k]=[round((x-(8,20,12)[i])*.25+(8,8,8)[i],6) for i,x in enumerate(e[k])]
item=model(whole);item['display']={'gui':{'rotation':[25,140,0],'scale':[1.15,1.15,1.15]},'ground':{'scale':[.6,.6,.6]}}
write('models/item/flying_bike_car.json',item);write('models/item/flying_bike_track.json',{'parent':'fourthdimension:item/arrow_track'})
print('Flying Bike: original cradles, linked restraints, continuous supports, miniature and governor generated')
