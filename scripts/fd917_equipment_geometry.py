#!/usr/bin/env python3
"""Bounded FD917 equipment exports; independent of shared family generators."""
import json
from pathlib import Path
from fd917_materials import box,model,item_display,texture
ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/'src/main/resources/assets/fourthdimension'
written=[]
def write(name,parts,color='gray',item=False,overrides=None):
    m=model(parts,color,overrides=overrides)
    if item:m['display']=item_display(.70)
    path=BASE/('models/item'if item else'models/block')/(name+'.json')
    path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(m,indent=2)+'\n');written.append(str(path.relative_to(ROOT)))
def b(a,c,t,n):return box(a,c,t,n)
frame=[b([0,3,3],[3,13,13],'metal','input shaft bearing'),b([13,3,3],[16,13,13],'metal','output clutch bearing'),b([0,1,1],[16,3,15],'body','remote kinetic drive foot'),b([1,13,3],[15,15,13],'body','controller cover'),b([3,3,11],[13,13,14],'body','generator rear casing'),b([1,6,2],[3,10,3],'bolt','input bearing flange'),b([13,6,2],[15,10,3],'bolt','output bearing flange')]
for axis in ('x','y','z'):
    parts=json.loads(json.dumps(frame))
    if axis!='x':
        target=1 if axis=='y' else 2
        for part in parts:
            for key in ('from','to'):part[key][0],part[key][target]=part[key][target],part[key][0]
    write('shuttle_winch'+(''if axis=='x'else'_'+axis),parts,'cyan')
# This is a small structural core; the actual native carriage's extended yoke/drive is dynamic.
write('shuttle_carriage',[b([3,5,3],[13,11,13],'body','native carriage core')],'cyan')
mini=frame+[b([4,4,4],[12,12,12],'contact','drive drum'),b([-1,7,7],[17,9,9],'contact','through shaft')]
write('shuttle_winch',mini,'cyan',True)
for ready in (0,1):
    for locked in (0,1):
        parts=[b([1,0,1],[15,3,15],'metal','station plinth'),b([3,3,4],[13,13,13],'body','operator cabinet'),b([2,13,3],[14,15,13],'metal','console lip'),b([4,7,3],[12,12,4],'rubber','state panel'),b([4,9,2.8],[7,11,3],'signal','boarding indicator'),b([9,9,2.8],[12,11,3],'lock','restraint indicator'),b([4,5,2],[6,7,4],'contact','capture button'),b([10,5,2],[12,7,4],'latch','restraint button')]
        write(f'boarding_station_{ready}{locked}',parts,overrides={'signal':texture('indicator_ready'if ready else'indicator_off'),'lock':texture('indicator_hold'if locked else'indicator_off')})
        if not ready and not locked:
            write('boarding_station',parts,overrides={'signal':texture('indicator_off'),'lock':texture('indicator_off')})
            write('boarding_station',parts,item=True,overrides={'signal':texture('indicator_off'),'lock':texture('indicator_off')})
console=[b([1,0,1],[15,3,15],'metal','controller base'),b([2,3,3],[14,13,14],'body','remote hold electronics'),b([3,12,2],[13,15,13],'metal','control panel lip'),b([4,7,2],[12,11,3],'rubber','hold status window'),b([6,8,1.8],[10,10,2],'off','unavailable default lens'),b([5,4,1],[7,6,3],'latch','hold control'),b([9,4,1],[11,6,3],'contact','release control')]
for item in (False,True):write('dive_hold',console,'orange',item,{'off':texture('indicator_off')})
for name in ('suspended_lock_free','suspended_lock_locked'):
    write(name,[b([-1.25,18.75,6.75],[1.25,21.25,9.25],'contact','swing journal core'),
        b([-1,19,-16],[1,21,32],'contact','fore aft pivot shaft'),
        b([-1,0,-12],[1,20,-10],'metal','front cabin suspension rod'),
        b([-1,0,26],[1,20,28],'metal','rear cabin suspension rod')],'orange')
for name,color in [('magnetic_inducer','orange'),('magnetic_damper','cyan')]:
    parts=[b([0,1,3],[16,3,13],'metal','span mounting foot'),b([2,3,5],[4,8,11],'metal','left equipment mount'),b([12,3,5],[14,8,11],'metal','right equipment mount'),b([0,8,4],[16,12,12],'body','magnetic housing'),b([0,12,5],[16,13,11],'contact','noncontact pole face')]
    if name.endswith('inducer'):parts +=[b([3,9,3],[5,11,4],'contact','induction pole'),b([7,9,3],[9,11,4],'contact','induction pole'),b([11,9,3],[13,11,4],'contact','induction pole')]
    else:parts +=[b([1,9,3],[15,10,4],'rubber','damping lamination'),b([1,11,3],[15,12,4],'rubber','damping lamination')]
    write(name,parts,color,True)
p=ROOT/'docs/evidence/fd917/mechanisms/equipment-outputs.json';p.write_text(json.dumps(written,indent=2)+'\n')
print('Equipment geometry exports:',len(written))
