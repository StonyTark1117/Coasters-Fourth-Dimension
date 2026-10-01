from pathlib import Path
import socket,struct,sys,fcntl
RUNTIME=Path("/tmp/fourthdimension-48535694/build/runs/packaged-server")
cmdlog=Path(__file__).resolve().parent/"commands.txt"
def _rcon(cmd):
 props=dict(line.split('=',1) for line in (RUNTIME/'server.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
 def packet(n,t,text):
  body=struct.pack('<ii',n,t)+text.encode()+b'\0\0'
  return struct.pack('<i',len(body))+body
 with socket.create_connection(('127.0.0.1',int(props['rcon.port'])),timeout=10) as sock:
  def read():
   def exact(n):
    b=b''
    while len(b)<n:
     x=sock.recv(n-len(b))
     if not x: raise ConnectionError('RCON closed')
     b+=x
    return b
   n=struct.unpack('<i',exact(4))[0];body=exact(n)
   return struct.unpack('<ii',body[:8]),body[8:-2].decode()
  sock.sendall(packet(11,3,props['rcon.password']));header,_=read()
  assert header[0]==11,'RCON authentication failed'
  sock.sendall(packet(12,2,cmd));header,result=read();assert header[0]==12
 with cmdlog.open('a') as f:f.write(cmd+': '+result+'\n\n')
 assert 'Unknown or incomplete command' not in result and 'unexpected error' not in result,result
 return result
def rcon(cmd):
 with (Path(__file__).resolve().parent/'rcon.lock').open('w') as lock:
  fcntl.flock(lock,fcntl.LOCK_EX)
  return _rcon(cmd)
if __name__=="__main__": print(rcon(sys.argv[1]))
