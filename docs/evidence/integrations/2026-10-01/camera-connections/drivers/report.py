from pathlib import Path
import os,json,sys,time
case=sys.argv[1];label=sys.argv[2];os.environ['FD_CAMERA_CASE']=case
from rcon import rcon
b=Path(__file__).resolve().parent/case
offset=(b/'server.log').stat().st_size
assert 'snapshot recorded' in rcon('fd validation camera_ride report '+label)
until=time.monotonic()+15
while time.monotonic()<until:
 for line in reversed((b/'server.log').read_bytes()[offset:].decode().splitlines(keepends=True)):
  if line.endswith('\n') and 'FD_CAMERA_RIDE {' in line:
   d=json.loads(line.split('FD_CAMERA_RIDE ',1)[1])
   if d['label']==label:
    (b/(label+'.json')).write_text(json.dumps(d,indent=2)+'\n')
    print(label, 'ready',d['ready'],'speed',d['speed'],'pos',(d['x'],d['y'],d['z']));sys.exit(0)
 time.sleep(.05)
raise AssertionError('Report observation expired')
