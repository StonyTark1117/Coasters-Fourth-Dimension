from pathlib import Path
import os,sys,time,json,re,subprocess
case=sys.argv[1];os.environ['FD_ADDITIONS_CASE']=case
from rcon import rcon
b=Path(__file__).resolve().parent;d=b/case;pid=int((d/'server.pid').read_text());limit=time.monotonic()+180
while not (d/'server.log').exists() or 'RCON running on' not in (d/'server.log').read_text():
 os.kill(pid,0);assert time.monotonic()<limit;time.sleep(1)
def tick():return int(re.search(r'The time is (\d+)',rcon('time query gametime')).group(1))
target=tick()+200;rcon('tick step 200')
while tick()<target:
 os.kill(pid,0);assert time.monotonic()<limit;time.sleep(.5)
subprocess.run(['python3',str(b/'report.py'),case,'pigs-ready'],check=True)
rcon('tick unfreeze')
subprocess.run(['python3',str(b/'start-client.py'),'dev',case],check=True)
subprocess.run(['python3',str(b/'start-client.py'),'observer',case],check=True)
print('restarted world ready, clients launched',flush=True)
