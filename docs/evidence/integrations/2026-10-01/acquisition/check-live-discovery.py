from pathlib import Path
import json,time
from rcon import rcon
b=Path(__file__).resolve().parent;root=b.parents[1];rows=[]
for p in sorted((root/'src/main/resources/data/fourthdimension/advancement/recipes').glob('*.json')):
 d=json.loads(p.read_text());id=d['rewards']['recipes'][0];trigger=d['criteria']['has_ingredient']['conditions']['items'][0]['items'];trigger='minecraft:iron_ingot' if trigger=='#c:ingots/iron' else trigger
 rcon('clear Dev');rcon('advancement revoke Dev only fourthdimension:recipes/'+p.stem);rcon('recipe take Dev '+id)
 before=rcon('data get entity Dev recipeBook.recipes');assert '"'+id+'"' not in before
 rcon('give Dev minecraft:dirt');time.sleep(.15)
 dirt=rcon('data get entity Dev recipeBook.recipes');assert '"'+id+'"' not in dirt
 rcon('give Dev '+trigger);time.sleep(.15)
 after=rcon('data get entity Dev recipeBook.recipes');assert '"'+id+'"' in after,id
 rows.append({'recipe':id,'trigger':trigger,'initiallyKnown':False,'dirtUnlocked':False,'ingredientUnlocked':True})
(b/'live-discovery.json').write_text(json.dumps(rows,indent=2)+'\n');print('Live survival discovery passed:',len(rows))
rcon('clear Dev');rcon('give Dev minecraft:iron_ingot 6');rcon('give Dev minecraft:copper_ingot 2');rcon('give Dev minecraft:red_dye 1')
rcon('tp Dev 0.5 -60 0.5 0 25');rcon('setblock 0 -60 2 minecraft:crafting_table')
