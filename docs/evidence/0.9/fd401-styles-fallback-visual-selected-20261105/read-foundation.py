import gzip,struct,sys,json
from pathlib import Path
p=Path(sys.argv[1]); data=gzip.decompress(p.read_bytes()); i=0
def take(n):
 global i
 b=data[i:i+n]; i+=n; return b
def num(fmt): return struct.unpack('>'+fmt,take(struct.calcsize(fmt)))[0]
def string(): return take(num('H')).decode('utf-8')
def value(t):
 if t==1:return num('b')
 if t==2:return num('h')
 if t==3:return num('i')
 if t==4:return num('q')
 if t==5:return num('f')
 if t==6:return num('d')
 if t==7:return take(num('i'))
 if t==8:return string()
 if t==9:
  typ=num('B'); n=num('i'); return [value(typ) for _ in range(n)]
 if t==10:
  result={}
  while True:
   typ=num('B')
   if typ==0:return result
   name=string();result[name]=value(typ)
 if t==11:return [num('i') for _ in range(num('i'))]
 if t==12:return [num('q') for _ in range(num('i'))]
 raise ValueError(t)
root_type=num('B'); root_name=string(); root=value(root_type)
def blockpos(v):
 x=v>>38; y=v&4095; z=(v>>12)&67108863
 if z>=33554432:z-=67108864
 if y>=2048:y-=4096
 return [x,y,z]
rows=[]
for rail in root['data']['rails'] if 'data' in root else root['rails']:
 a=rail['appearance']; rows.append({'from':blockpos(rail['from']),'to':blockpos(rail['to']),'family':rail['family'],'variant':rail['variant'],'id':rail['id'],'revision':rail['revision'],'geometry_hash':rail['geometry_hash'],'profile':rail['profile'],'appearance':a})
print(json.dumps(rows,indent=2))
