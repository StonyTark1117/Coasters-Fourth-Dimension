from pathlib import Path
import json,time
from PIL import Image
from rcon import rcon
from ui import capture
b=Path(__file__).resolve().parent;root=b.parents[1]
items=sorted(p.stem for p in (root/'src/main/resources/data/fourthdimension/recipe').glob('*.json'))
for name in items:
 rcon('item replace entity Dev hotbar.0 with fourthdimension:'+name)
 time.sleep(.5);capture('tooltip-'+name)
for group in range(2):
 subset=items[group*8:(group+1)*8];img=Image.new('RGB',(602,121*len(subset)))
 for i,name in enumerate(subset):img.paste(Image.open(b/('tooltip-'+name+'.png')).crop((465,449,1067,570)),(0,i*121))
 img.save(b/('tooltip-review-'+str(group)+'.png'))
(b/'tooltip-captures.json').write_text(json.dumps({'items':items,'window':[854,480],'gui_scale':2,'client':'Dev','artifact':'/home/braydon/.cache/fourthdimension-48535694/acquisition.jar'},indent=2)+'\n')
