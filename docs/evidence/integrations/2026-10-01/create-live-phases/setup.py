from pathlib import Path
import json
from rcon import rcon
base=Path(__file__).resolve().parent
links=[('status',[-606,181,608],'up'),('occupancy',[-606,180,607],'north'),('restraints',[-606,180,609],'south'),('dispatch',[-605,180,608],'east'),('phase',[-606,179,608],'down')]
fixtures=[]
for i,(field,pos,facing) in enumerate(links):
 target=[-611,181,604+i];support=[target[0],target[1]-1,target[2]]
 coords=lambda v:' '.join(map(str,v))
 rcon('setblock '+coords(support)+' minecraft:stone')
 rcon('setblock '+coords(target)+' minecraft:oak_sign[rotation=4]')
 offset=','.join(str(t-p) for t,p in zip(target,pos))
 rcon('setblock '+coords(pos)+' create:display_link[facing='+facing+']{TargetOffset:[I;'+offset+'],TargetLine:0,Source:{Id:"fourthdimension:ride_'+field+'"}}')
 fixtures.append({'field':field,'link':pos,'target':target,'facing':facing})
(base/'links.json').write_text(json.dumps(fixtures,indent=2)+'\n')
for player,x,yaw in [('Dev',-605.5,180),('Observer',-606.5,-161.565)]:
 rcon('gamemode creative '+player)
 rcon('item replace entity '+player+' armor.head with create:goggles')
 rcon(f'tp {player} {x} 180 611.5 {yaw} 20.5')
rcon('time set day')
print(rcon('execute if block -607 180 608 create:creative_motor[facing=east] run say FD_CREATIVE_MOTOR_FACING_EAST'))
