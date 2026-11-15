import json, math, pathlib, itertools, gzip
r=pathlib.Path(__file__).parent
ids=['1f890217-ddbe-4328-8cef-973459a8e49a','2805d60c-d2f5-4180-a5a7-2e1f41f9e4aa','0996199f-9602-4fa1-8630-9d1f1169c902','3eec2d1d-9e2e-40d1-9f4f-ea5b19bbabc6']
def dot(a,b):return sum(x*y for x,y in zip(a,b))
def sub(a,b):return [x-y for x,y in zip(a,b)]
def cross(a,b):return [a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]]
def basis(q):
 x,y,z,w=q
 return [[1-2*(y*y+z*z),2*(x*y+z*w),2*(x*z-y*w)], [2*(x*y-z*w),1-2*(x*x+z*z),2*(y*z+x*w)], [2*(x*z+y*w),2*(y*z-x*w),1-2*(x*x+y*y)]]
def separation(a,b):
 A=basis(a['q']);B=basis(b['q']);ac=[p-2.0125*u for p,u in zip(a['center'],A[1])];bc=[p-2.0125*u for p,u in zip(b['center'],B[1])];delta=sub(bc,ac);ext=[2.25,2.8,1.75];gaps=[]
 for v in A+B+[cross(x,y) for x in A for y in B]:
  length=math.sqrt(dot(v,v))
  if length<1e-7:continue
  v=[x/length for x in v]
  gaps.append(abs(dot(delta,v))-sum(e*abs(dot(v,x)) for e,x in zip(ext,A))-sum(e*abs(dot(v,x)) for e,x in zip(ext,B)))
 return max(gaps)
stats={i:{'minUpY':1.,'maxSeatSpacingError':0.,'samples':0} for i in ids};minimum=math.inf;full=0;owner=True;min_record=None
for line in gzip.open(r/'native-trace.jsonl.gz','rt'):
 d=json.loads(line);cars=[c for c in d['cars'] if c['id'] in ids]
 for c in cars:
  s=stats[c['id']];s['samples']+=1;s['minUpY']=min(s['minUpY'],c['upY']);s['maxSeatSpacingError']=max(s['maxSeatSpacingError'],max(abs(math.dist(a,b)-1) for a,b in zip(c['seats'],c['seats'][1:])));owner &= c['sameSeatOwner']
 if len(cars)!=4:continue
 full+=1
 for a,b in itertools.combinations(cars,2):
  gap=separation(a,b)
  if gap<minimum:minimum=gap;min_record={'tick':d['tick'],'ids':[a['id'],b['id']]}
result={'cars':stats,'sameNativeSeatOwnerEverySample':owner,'fourCarSamples':full,'minimumEnvelopeSeparatingAxisGap':minimum,'minimumRecord':min_record,'method':'15-axis OBB SAT from actual native logical quaternion and bearing center. Full reserved envelope x±2.25, graph y[-4.5,1.1], z±1.75; its center is bearing minus2.0125 nativeUp. Temporal samples20Hz while moving, not a swept continuous-time proof. Native velocity/curvature limits and live forward obstruction checks complement samples.'}
(r/'roll-native-summary.json').write_text(json.dumps(result,indent=2));print(json.dumps(result,indent=2))
