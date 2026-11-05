import socket, struct, pathlib, sys
props=pathlib.Path('/tmp/fd401d-occupied-source-ui-final-20261105T1505Z/server/game/server.properties').read_text().splitlines()
pw=next(x.split('=',1)[1] for x in props if x.startswith('rcon.password='))
s=socket.create_connection(('127.0.0.1',25664),timeout=3); s.settimeout(3)
def send(i,t,p):
 b=struct.pack('<ii',i,t)+p.encode()+b'\0\0'; s.sendall(struct.pack('<i',len(b))+b)
def recv():
 def exact(n):
  o=b''
  while len(o)<n:o+=s.recv(n-len(o))
  return o
 n=struct.unpack('<i',exact(4))[0]; p=exact(n); return struct.unpack('<ii',p[:8]),p[8:-2].decode(errors='replace')
send(0,3,pw)
if recv()[0][0]==-1: raise SystemExit('auth rejected')
for i,c in enumerate(sys.argv[1:],1):send(i,2,c);print('$',c,'\n',recv()[1])
s.close()
