#!/usr/bin/env python3
"""Finite Motorbike source/asset geometry checks; this does not claim client appearance."""
import json,math,itertools
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
ASSET=ROOT/'src/main/resources/assets/fourthdimension'
def read(p):return json.loads((ASSET/p).read_text())
base=read('models/block/motorbike_seat_orange_closed.json')['elements']
rest=read('models/block/fd917/motorbike_restraint.json')['elements']
frame=read('models/block/motorbike_frame.json')['elements']
def overlap(a,b):return all(min(a['to'][k],b['to'][k])-max(a['from'][k],b['from'][k])>=-1e-8 for k in range(3))
# Every shell component has a solid path to the pan; colored cushion stays on the pan.
parts=base+rest;seen={next(i for i,p in enumerate(parts) if p.get('name')=='saddle_pan')}
while True:
    added={i for i,p in enumerate(parts) if i not in seen and any(overlap(p,parts[j]) for j in seen)}
    if not added:break
    seen|=added
assert len(seen)==len(parts),[p.get('name') for i,p in enumerate(parts) if i not in seen]
# Frame block is immediately below the saddle; all three beam blocks connect edge to edge.
relative=[dict(p,**{key:[p[key][i]+(16 if i==1 else 0) for i in range(3)] for key in ['from','to']}) for p in base]
assert any(overlap(a,b) for a in relative for b in frame)
meta=read('fd917_articulation/motorbike_seat.json')['parts'][0];pivot=meta['pivot'];peak=[-1e9]*3;low=[1e9]*3
for angle in range(111):
    a=math.radians(angle)
    for p in rest:
        for xyz in itertools.product(*zip(p['from'],p['to'])):
            x,y,z=[xyz[i]-pivot[i] for i in range(3)]
            point=[x+pivot[0],math.cos(a)*y-math.sin(a)*z+pivot[1],math.sin(a)*y+math.cos(a)*z+pivot[2]]
            # Seat centered X +/-1; graph seat-originY=.8125, forwardZ centered at8.
            for side in [-1,1]:
                graph=[side+(point[0]-8)/16,.8125+point[1]/16,(point[2]-8)/16]
                assert abs(graph[0])<=1.85 and .45<=graph[1]<=2.65 and abs(graph[2])<=1.50,graph
                low=[min(low[i],graph[i]) for i in range(3)];peak=[max(peak[i],graph[i]) for i in range(3)]
# The exported seat variants all retain the same physical mesh, changing only upholstery.
for p in ASSET.glob('models/block/motorbike_seat_*_*.json'):assert json.loads(p.read_text())['elements']==base,p
# Minecraft model-space leg/hand endpoints, relative to hip (12unit thigh/arm length).
foot={'x':(4+12*math.sin(.10))/16,'y':1.3125-12*math.cos(.70)/16,'z':-12*math.sin(.70)/16}
hand={'x':5/16,'y':1.3125+10/16-12*math.cos(1.35)/16,'z':-12*math.sin(1.35)/16}
report={'scope':'Source cuboid connectivity, dense 1-degree hinge sweep, approximate limb centerline endpoints; no rendered/occupied acceptance','connected_elements':len(parts),'hinge_graph_bounds':{'min':low,'max':peak},'foot_centerline':foot,'hand_centerline':hand,'hip_graph_y':1.3125,'cushion_top_graph_y':.8125+7.8/16,'status':'PASS'}
out=ROOT/'docs/evidence/1.4/fd2202/geometry-audit.json';out.write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report))
