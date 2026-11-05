import json, struct, sys, zlib
from pathlib import Path
region=Path(sys.argv[1]); cx,cz,bx,by,bz=map(int,sys.argv[2:])
with region.open('rb') as f:
 h=f.read(4096); idx=(cx%32)+(cz%32)*32; loc=int.from_bytes(h[idx*4:idx*4+4],'big')
 if not loc: raise SystemExit('chunk absent')
 f.seek((loc>>8)*4096); length=int.from_bytes(f.read(4),'big'); compression=f.read(1)[0]; raw=f.read(length-1)
if compression==2: raw=zlib.decompress(raw)
elif compression==1:
 import gzip; raw=gzip.decompress(raw)
elif compression!=3: raise ValueError(compression)
i=0
def take(n):
 global i
 b=raw[i:i+n]; i+=n
 if len(b)!=n: raise EOFError()
 return b
def num(fmt): return struct.unpack('>'+fmt,take(struct.calcsize('>'+fmt)))[0]
def string(): return take(num('H')).decode('utf8')
def value(t):
 if t in {1: 'b',2:'h',3:'i',4:'q',5:'f',6:'d'}: return num(value_fmt[t])
 if t==7: return list(take(num('i')))
 if t==8: return string()
 if t==9:
  typ=num('B'); n=num('i'); return [value(typ) for _ in range(n)]
 if t==10:
  d={}
  while True:
   typ=num('B')
   if typ==0: return d
   name=string(); d[name]=value(typ)
 if t==11:return [num('i') for _ in range(num('i'))]
 if t==12:return [num('q') for _ in range(num('i'))]
 raise ValueError(t)
value_fmt={1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}
root_type=num('B'); root_name=string(); root=value(root_type)
def find(v):
 if isinstance(v,dict):
  if v.get('id')=='fourthdimension:shuttle_winch' and (v.get('x'),v.get('y'),v.get('z'))==(bx,by,bz): return v
  for a in v.values():
   q=find(a)
   if q is not None:return q
 elif isinstance(v,list):
  for a in v:
   q=find(a)
   if q is not None:return q
returnval=find(root)
if returnval is None: raise SystemExit('winch BE not found')
print(json.dumps(returnval,indent=2))
