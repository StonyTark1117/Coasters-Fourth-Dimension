from pathlib import Path
import os,json,sys,time
case=sys.argv[1];label=sys.argv[2];os.environ['FD_STYLES_CASE']=case
from rcon import rcon
b=Path(__file__).resolve().parent/case
offset=(b/'server.log').stat().st_size
assert 'snapshot recorded' in rcon('fd validation styles_restart report '+label)
until=time.monotonic()+15
while time.monotonic()<until:
 for line in reversed((b/'server.log').read_bytes()[offset:].decode().splitlines(keepends=True)):
  if line.endswith('\n') and 'FD_STYLES_LIFECYCLE {' in line:
   d=json.loads(line.split('FD_STYLES_LIFECYCLE ',1)[1])
   if d['label']==label:
    (b/(label+'.json')).write_text(json.dumps(d,indent=2)+'\n')
    print(label,'styles',d['styles'],'cars',len(d['cars']),'riders',sum(len(c['riders']) for v in d['cars'] for c in v['carriers']));sys.exit(0)
 time.sleep(.05)
raise AssertionError('Report observation expired')
