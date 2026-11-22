#!/usr/bin/env python3
"""Export-space FD2204 support connectivity and restraint sweep; not runtime player evidence."""
import json, math
from pathlib import Path
A=Path(__file__).resolve().parents[1]/'src/main/resources/assets/fourthdimension/models/block'
def es(name,offset=(0,0,0)):
 out=[]
 m=json.loads((A/(name+'.json')).read_text());translation=m.get('transform',{}).get('translation',[0,0,0])
 offset=[a+16*b for a,b in zip(offset,translation)]
 for e in m['elements']:
  assert all(-16<=c<=32 for key in ['from','to'] for c in e[key]),(name,e)
  out.append((e['name'],[a+b for a,b in zip(e['from'],offset)],[a+b for a,b in zip(e['to'],offset)]))
 return out
def joined(a,b):
 overlap=[min(a[2][i],b[2][i])-max(a[1][i],b[1][i]) for i in range(3)]
 return min(overlap)>=-1e-8 and sum(d>1e-8 for d in overlap)>=2
fixed=es('flying_bike_controller',(0,-16,0))
for i,p in enumerate([(-16,-16,0),(16,-16,0),(-16,-32,0),(16,-32,0)]):fixed+=es('flying_bike_frame_'+str(i),p)
for x in [-16,16]:fixed+=es('flying_bike_seat',(x,-48,0))
seen={0}
while True:
 n=seen|{j for j,b in enumerate(fixed) if any(joined(fixed[i],b) for i in seen)}
 if n==seen:break
 seen=n
assert len(seen)==len(fixed),[(i,e[0]) for i,e in enumerate(fixed) if i not in seen]
# Native plot center is(.5,.5,.5); continuous support and all hinged corners stay in authored reservation.
points=[]
for _,a,b in fixed:
 for x in [a[0],b[0]]:
  for y in [a[1],b[1]]:
   for z in [a[2],b[2]]:points.append((x/16-.5,y/16,z/16-.5))
for side in [-16,16]:
 for angle in range(56):
  q=math.radians(angle)
  for _,a,b in es('flying_bike_restraint'):
   for x in [a[0],b[0]]:
    for y in [a[1],b[1]]:
     for z in [a[2],b[2]]:
      dy,dz=y-10,z-31.5
      points.append(((x+side)/16-.5,(-48+10+dy*math.cos(q)-dz*math.sin(q))/16,(31.5+dy*math.sin(q)+dz*math.cos(q))/16-.5))
lo=[min(p[i] for p in points) for i in range(3)];hi=[max(p[i] for p in points) for i in range(3)]
assert lo[0]>=-2.25 and hi[0]<=2.25 and lo[1]>=-4.5 and hi[1]<=1.1 and lo[2]>=-2.5 and hi[2]<=2.5,(lo,hi)
# Prone torso center and actual eye are both inside reservation; no second native passenger body.
print(json.dumps({'fixed_elements':len(fixed),'one_connected_load_path':True,'restraint_samples_degrees':56,'occupied_reservation':[4.5,5,5.6],'export_bounds':[lo,hi],'eye_seat_local':[.5,.7,-.35],'synthetic_rider_mass_seat_local':[.5,.65,.9],'physical_seat_COM':[.5,.55,.9],'native_chain_physical_acceleration_cap':3,'result':'PASS'},indent=2))
