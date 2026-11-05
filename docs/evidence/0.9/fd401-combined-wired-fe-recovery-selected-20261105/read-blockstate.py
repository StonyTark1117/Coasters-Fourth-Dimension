import sys,struct,zlib,math
from pathlib import Path
raw=Path(sys.argv[1]).read_bytes()
x,y,z=map(int,sys.argv[2:5])
cx,cz=x//16,z//16
index=(cz%32)*32+(cx%32)
loc=int.from_bytes(raw[index*4:index*4+3],'big')*4096
n=int.from_bytes(raw[loc:loc+4],'big')
kind=raw[loc+4]
assert kind==2,kind
data=zlib.decompress(raw[loc+5:loc+4+n]);i=0
def take(n):
 global i
 b=data[i:i+n]; i+=n; return b
def num(fmt):return struct.unpack('>'+fmt,take(struct.calcsize(fmt)))[0]
def string():return take(num('H')).decode()
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
  typ=num('B');return [value(typ) for _ in range(num('i'))]
 if t==10:
  d={}
  while (typ:=num('B'))!=0:
   name=string();d[name]=value(typ)
  return d
 if t==11:return [num('i') for _ in range(num('i'))]
 if t==12:return [num('q') for _ in range(num('i'))]
 raise ValueError(t)
t=num('B');string();root=value(t)
sections=root.get('sections',root.get('Level',{}).get('Sections'))
s=next(s for s in sections if s['Y']==y//16)
bs=s['block_states'];pal=bs['palette'];arr=bs.get('data',[])
idx=((y&15)*16+(z&15))*16+(x&15)
if len(pal)==1: entry=0
else:
 bits=max(4,(len(pal)-1).bit_length());per=64//bits
 entry=(arr[idx//per] & ((1<<64)-1)) >> ((idx%per)*bits) & ((1<<bits)-1)
print({'chunk':(cx,cz),'index':idx,'entry':entry,'palette_size':len(pal),'state':pal[entry]})
