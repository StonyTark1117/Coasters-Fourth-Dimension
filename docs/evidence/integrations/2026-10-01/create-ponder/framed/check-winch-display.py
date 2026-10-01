from pathlib import Path
import json,time,subprocess,os
from rcon import rcon
base=Path(__file__).resolve().parent
rows=[]
def report(label):
 rcon('fd validation pax_energy_restart report '+label)
 until=time.monotonic()+10
 while time.monotonic()<until:
  for line in reversed((base/'server.log').read_text().splitlines()):
   if 'FD_PAX_ENERGY_RESTART {' in line:
    d=json.loads(line.split('FD_PAX_ENERGY_RESTART ',1)[1])
    if d['label']==label:return d
  time.sleep(.05)
 raise AssertionError(label)
def displays(label):
 before=report(label+'_before')
 for field in ['status','occupancy','restraints','dispatch','phase','fault']:
  rcon('data merge block -606 181 608 {Source:{Id:"fourthdimension:ride_'+field+'"}}')
  time.sleep(.8)
  text=rcon('data get block -610 181 608 front_text.messages[0]')
  rows.append({'state':label,'field':field,'sign_response':text,'telemetry':before['telemetry']})
  (base/'winch-displays.json').write_text(json.dumps(rows,indent=2)+'\n')
  print(label,field,text.strip(),flush=True)
 after=report(label+'_after')
 assert before['telemetry']==after['telemetry'] and before['cycles']==after['cycles']==4 and before['energy']==after['energy']==12345
 for client,display in [('dev',':0'),('observer',':1')]:
  subprocess.run(['import','-window','root',str(base/(client+'-goggles-'+label+'.png'))],env=dict(os.environ,DISPLAY=display),check=True)
 return after
opened=displays('empty_open');assert opened['telemetry']['occupied']==0 and not opened['telemetry']['dispatchReady']
assert 'snapshot recorded' not in rcon('fd validation pax_energy_restart board')
time.sleep(2)
closed=displays('occupied_closed');assert closed['telemetry']['occupied']==16 and closed['telemetry']['dispatchReady']
rcon('fd validation pax_energy_restart unload');time.sleep(2)
unloaded=report('guidance_unloaded');assert unloaded['telemetry']==opened['telemetry']
rcon('data merge block -606 181 608 {Source:{Id:"fourthdimension:ride_occupancy"}}');time.sleep(.8)
assert '0 / 16' in rcon('data get block -610 181 608 front_text.messages[0]')
(base/'winch-displays.complete').write_text('complete\n')
