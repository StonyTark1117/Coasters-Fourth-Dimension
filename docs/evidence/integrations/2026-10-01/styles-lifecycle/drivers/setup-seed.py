from pathlib import Path
import os,time,json
os.environ['FD_STYLES_CASE']='seed'
from rcon import rcon
b=Path(__file__).resolve().parent/'seed';pid=int((b/'server.pid').read_text());until=time.monotonic()+180
while not (b/'server.log').exists() or 'RCON running on' not in (b/'server.log').read_text():
 os.kill(pid,0);assert time.monotonic()<until;time.sleep(1)
for cmd in ['difficulty peaceful','gamerule doMobSpawning false','gamerule doDaylightCycle false','time set noon','weather clear']:
 rcon(cmd)
def action(name):assert 'fixture: '+name in rcon('fd validation styles_restart '+name)
def step(n):
 assert 'Stepping' in rcon('tick step '+str(n));time.sleep(n/20+.5)
 until=time.monotonic()+60
 while 'The game is frozen' not in rcon('tick query'):os.kill(pid,0);assert time.monotonic()<until;time.sleep(.5)
action('create');step(100);action('cars');step(100);action('pigs');step(40);rcon('tick unfreeze')
(b/'setup.exit').write_text('0\n');print('seed ready for clients',flush=True)
