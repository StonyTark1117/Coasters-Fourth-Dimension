from pathlib import Path
import os,sys,time,json,subprocess
case=sys.argv[1];os.environ['FD_ADDITIONS_CASE']=case
from rcon import rcon
b=Path(__file__).resolve().parent
for player in ['Dev','Observer']:
 rcon('gamemode creative '+player)
 rcon('item replace entity '+player+' armor.head with create:goggles')
for lane,family in enumerate(['arrow','legacy','free_spin','revised','pax']):
 x=-120+lane*32-8
 for player,offset,yaw in [('Dev',-.5,-18.435),('Observer',1.5,18.435)]:
  rcon(f'tp {player} {x+offset} 119 15.5 {yaw} 2.18')
 time.sleep(6)
 subprocess.run(['python3',str(b/'capture.py'),case,'goggles-'+family],check=True)
 print('captured',family,flush=True)
