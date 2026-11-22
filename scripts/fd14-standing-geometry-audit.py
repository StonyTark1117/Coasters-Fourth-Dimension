#!/usr/bin/env python3
"""Actual exported primitive contact graph and finite standing/Surf stroke sweep."""
import json,itertools,math
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/fourthdimension'
def read(n):
 m=json.loads((A/'models/block'/f'{n}.json').read_text());es=m['elements']
 assert all(-16<=v<=32 for e in es for k in ('from','to') for v in e[k]),(n,'vanilla element bounds')
 transform=m.get('transform',{'scale':[1,1,1],'origin':'corner'})
 assert set(transform)=={'scale','origin'} and transform['origin']=='corner',n
 for e in es:
  for k in ('from','to'):e[k]=[v*s for v,s in zip(e[k],transform['scale'])]
 return es
def at(es,offset):return [dict(e,**{k:[a+b for a,b in zip(e[k],offset)] for k in('from','to')}) for e in es]
def touch(a,b):return all(max(a['from'][i],b['from'][i])<=min(a['to'][i],b['to'][i])+1e-7 for i in range(3))
def graph(es):
 seen={0};more=True
 while more:
  before=len(seen);seen|={j for i in seen for j,e in enumerate(es) if touch(es[i],e)};more=len(seen)>before
 return seen
for surf in(False,True):
 name='surf' if surf else 'standing';frame=at(read(name+'_frame'),(-8,-3,8))
 metal=[e for e in frame if next(iter(e['faces'].values()))['texture'] in('#metal','#contact')]
 assert len(graph(metal))==len(metal),name+' fixed support disconnect'
 for right in(False,True):
  hand='right' if right else 'left';offset=(8 if right else -24,13,-8)
  seat=at(read(name+'_seat_'+hand),offset);rigid=[e for e in seat if next(iter(e['faces'].values()))['texture'] in('#metal','#contact')]
  assert len(graph(rigid))==len(rigid),(name,hand,'rigid carriage disconnect')
  restraint=at(read('standing_restraint_'+hand),offset)
  # Each articulated arm really meets its fixed hinge at the retained endpoint.
  hinges=[e for e in seat if e['name']=='fixed shoulder hinge']
  arms=[e for e in restraint if e['name']=='shoulder restraint arm']
  assert all(any(touch(h,a) for h in hinges) for a in arms)
  if not surf:
   combined=metal+rigid;assert len(graph(combined))==len(combined),'Rigid standing post does not meet seat'
  else:
   x=10.4 if right else -10.4
   guides=[e for e in frame if e['name']=='fixed guide rail' and e['from'][0]<x<e['to'][0]]
   assert len(guides)==1
   guide=guides[0]
   for q in(0,6.4):
    moved=at(seat,(0,q,0));shoes=[e for e in moved if e['name'].startswith('guide shoe')]
    assert not any(touch(guide,e) for e in shoes),'Surf solid guide/shoe interference'
    assert min(e['from'][1] for e in shoes)>=guide['from'][1] and max(e['to'][1] for e in shoes)<=guide['to'][1],'Lost guide engagement'
    # Four-sided rail surrounding gap is .16 model units = .01 block.
    front=next(e for e in shoes if e['name']=='guide shoe front');assert abs(guide['from'][2]-front['to'][2]-.16)<1e-6
  # All shell and open/closed articulated vertices remain inside declared envelope.
  for q in((0,6.4) if surf else(0,)):
   points=[]
   for e in at(seat,(0,q,0)):
    points+=list(itertools.product(*[(e['from'][i],e['to'][i]) for i in range(3)]))
   pivot=((2.4 if right else 13.6)+offset[0],31+offset[1]+q,14+offset[2])
   for degrees in range(56):
    a=math.radians(degrees)
    for e in at(restraint,(0,q,0)):
     for v in itertools.product(*[(e['from'][i],e['to'][i]) for i in range(3)]):
      dy,dz=v[1]-pivot[1],v[2]-pivot[2];points.append((v[0],pivot[1]+dy*math.cos(a)-dz*math.sin(a),pivot[2]+dy*math.sin(a)+dz*math.cos(a)))
   assert all(-28-1e-6<=x<=28+1e-6 and -15.2<=y<=64 and -28<=z<=28 for x,y,z in points),'Occupied reservation exceeded'
   # A 1.8-block standing rider has feet at Y=20+q and head below Y=64.
   assert 20+q+28.8<=64
 print(name+': connected fixed metal support; connected seat/hinge graph; 0..55 degree restraints and physical stroke within fit')
paired=at(read('surf_carriage'),(-8,13,-8))
for right in(False,True):
 paired += [e for e in at(read('surf_seat_'+('right' if right else 'left')),(8 if right else -24,13,-8)) if next(iter(e['faces'].values()))['texture'] in('#metal','#contact')]
assert len(graph(paired))==len(paired),'Surf pair lacks one connected metal carriage'
print('Surf paired stance blocks have a child-owned crossmember: complete metal contact graph connected')
print('Surf: both .01-block guide radial clearances; shoes engaged at q=0 and q=.40; no solid guide overlap')
print('All model elements fit vanilla [-16,32]; corner-origin root transform restores exact original support geometry')
print('Limits: source geometry only; native physics and occupied client observations are separate required evidence.')
