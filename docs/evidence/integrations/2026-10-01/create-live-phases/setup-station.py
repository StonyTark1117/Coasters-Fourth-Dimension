from pathlib import Path
import json
from rcon import rcon
base=Path(__file__).resolve().parent
old=json.loads((base/'links.json').read_text());(base/'links-initial.json').write_text(json.dumps(old,indent=2)+'\n')
rcon('setblock -606 180 609 minecraft:air')
links=[dict(row,source='winch') for row in old if row['field']!='restraints']
rcon('setblock -616 180 608 fourthdimension:boarding_station{vehicle:[I;1626414480,722750707,-1639736133,-1336555213]}')
for i,(field,offset,facing) in enumerate([('status',(0,1,0),'up'),('occupancy',(0,0,-1),'north'),('restraints',(0,0,1),'south'),('dispatch',(1,0,0),'east'),('phase',(0,-1,0),'down'),('fault',(-1,0,0),'west')]):
 pos=[a+b for a,b in zip([-616,180,608],offset)];target=[-615,183,604+i];coords=lambda v:' '.join(map(str,v))
 rcon('setblock '+coords([target[0],target[1]-1,target[2]])+' minecraft:stone')
 rcon('setblock '+coords(target)+' minecraft:oak_sign[rotation=4]')
 delta=','.join(str(t-p) for t,p in zip(target,pos))
 rcon('setblock '+coords(pos)+' create:display_link[facing='+facing+']{TargetOffset:[I;'+delta+'],TargetLine:0,Source:{Id:"fourthdimension:ride_'+field+'"}}')
 links.append({'field':field,'source':'station','link':pos,'target':target,'facing':facing})
(base/'links.json').write_text(json.dumps(links,indent=2)+'\n')
print('Configured',len(links),'native links')
