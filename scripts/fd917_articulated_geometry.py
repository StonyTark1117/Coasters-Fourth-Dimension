#!/usr/bin/env python3
"""Geometry-only production exports for FD917 articulated families.

No recipes, language, loot or shared material writes. Native aligned structure
coordinates have one editable source; new shells and hinges are authored below.
"""
import copy
import json
from pathlib import Path
from fd917_materials import box, model, texture, item_display

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/fourthdimension'
STRUCTURES=json.loads((Path(__file__).with_name('fd917_mechanism_structures.json')).read_text())['models']
COLORS={'axis':'cyan','roller_ball':'lime','flying':'light_blue','new_generation_flying':'purple',
        'spinning':'orange','s2_spinning':'red','pipeline':'blue','powered_inverted_yaw':'cyan'}
PREFIX={'spinning':('spinning_','spin_bearing'), 'new_generation_flying':('new_generation_flying_','yaw_marker')}
WRITTEN=[]

def write(path,value):
    path=ASSETS/path
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(value,indent=2)+'\n')
    WRITTEN.append(str(path.relative_to(ROOT)))

def export_model(name,parts,family,item=False):
    value=model(parts,COLORS[family],padding='black')
    if item:value['display']=item_display(.65)
    write(('models/item/' if item else 'models/block/')+name+'.json',value)

def b(a,c,t,name):return box(a,c,t,name)

def transformed(parts,cx=8,cz=8,south=False):
    result=copy.deepcopy(parts)
    for e in result:
        lo,hi=e['from'],e['to']
        if south:
            lo,hi=[16-hi[0],lo[1],16-hi[2]],[16-lo[0],hi[1],16-lo[2]]
        e['from']=[lo[0]+cx-8,lo[1],lo[2]+cz-8]
        e['to']=[hi[0]+cx-8,hi[1],hi[2]+cz-8]
    return result

def shell(family):
    low=10 if family in ('spinning','s2_spinning') else 4 if family=='powered_inverted_yaw' else 5
    seat_top=low+2.8
    # Each shell has paint outside and padded contact faces, not a solid recolored chair.
    parts=[b([1,low-1,1],[15,low+1,15],'body','pressed seat pan'),
           b([2,low+1,2],[14,seat_top,13],'seat','seat cushion'),
           b([1,seat_top,13],[15,27,16],'body','structural seat back'),
           b([2.5,seat_top,11.8],[13.5,25,13],'seat','back padding'),
           b([4,25,12],[12,29,15],'seat','head pad')]
    if family=='axis':
        parts += [b([0,low-1,2],[2,14,13],'body','left swept cheek'),b([14,low-1,2],[16,14,13],'body','right swept cheek'),
                  b([3,0,5],[5,low,13],'metal','journal seat spar'),b([11,0,5],[13,low,13],'metal','journal seat spar'),
                  b([2,-2,-5],[14,0,2],'rubber','open foot ledge'),b([3,0,0],[5,6,3],'metal','left foot support'),b([11,0,0],[13,6,3],'metal','right foot support')]
    elif family=='roller_ball':
        parts += [b([0,low-1,2],[2,24,15],'body','enclosing left bucket wall'),b([14,low-1,2],[16,24,15],'body','enclosing right bucket wall'),
                  b([2,24,14],[14,29,18],'body','rear crown'),b([2,-10,0],[14,-7,9],'metal','suspended foot tray'),
                  b([6,-7,5],[10,5,8],'metal','foot tray support'),b([6,8,15],[10,24,18],'metal','load spine'),
                  b([6,22,16],[10,26,24],'contact','transverse axle boss')]
    elif family in ('flying','new_generation_flying'):
        parts += [b([0,low,10],[2,25,17],'body','left posture cradle'),b([14,low,10],[16,25,17],'body','right posture cradle'),
                  b([5,2,15],[11,24,18],'metal','posture load spine'),b([2,-11,-1],[14,-8,6],'rubber','ankle support'),
                  b([6,-8,4],[10,5,7],'metal','leg restraint spine')]
        if family=='new_generation_flying':
            parts += [b([0,6,14],[16,10,19],'body','compact deck back sill'),b([1,10,17],[3,22,19],'contact','posture bearing cheek'),b([13,10,17],[15,22,19],'contact','posture bearing cheek')]
        else:
            parts += [b([1,7,16],[15,10,18],'contact','four abreast hip beam')]
    elif family in ('spinning','s2_spinning'):
        parts += [b([1,2,1],[15,4,17],'metal','footwell base'),b([0,4,1],[2,14,15],'body','left car sill'),b([14,4,1],[16,14,15],'body','right car sill'),
                  b([2,4,1],[14,5,8],'rubber','footwell tread'),b([2,4,10],[4,low,14],'metal','seat riser'),b([12,4,10],[14,low,14],'metal','seat riser')]
        if family=='spinning':
            parts += [b([0,4,-2],[16,9,1],'body','roundabout end panel'),b([2,5,-3],[14,7,-2],'contact','end bumper')]
        else:
            parts += [b([0,3,-3],[16,7,1],'body','low inverting nose'),b([1,7,-1],[3,11,3],'body','separate left pod cheek'),b([13,7,-1],[15,11,3],'body','separate right pod cheek')]
    elif family=='pipeline':
        parts += [b([0,low-1,-1],[2,14,15],'body','left narrow cockpit coaming'),b([14,low-1,-1],[16,14,15],'body','right narrow cockpit coaming'),
                  b([1,0,-1],[15,2,14],'metal','side rail cabin pan'),b([2,2,-1],[14,3,4],'rubber','seated footwell'),
                  b([2,low-1,-2],[14,low+2,0],'body','nose sill')]
    else:
        parts += [b([0,-12,-1],[16,-10,15],'body','upright pod floor'),b([1,-10,11],[3,7,15],'metal','rear pod support'),b([13,-10,11],[15,7,15],'metal','rear pod support'),
                  b([0,5,8],[2,22,16],'body','left cabin shoulder'),b([14,5,8],[16,22,16],'body','right cabin shoulder'),
                  b([3,-10,0],[13,-9,9],'rubber','pod footwell'),b([5,27,14],[11,30,16],'body','upright pod crown')]
    return parts

def harness(family):
    lap=family in ('spinning','s2_spinning','powered_inverted_yaw')
    hinge=27 if lap else 23
    front=1.5
    parts=[]
    # Outside cheek arms support the lap pad without an over-shoulder front strap.
    if lap:
        for x in (1,13):
            parts += [b([x,13,front],[x+2,16,14],'contact','lap arm'),b([x,15,12],[x+2,hinge+1,15],'metal','back hinge arm')]
        parts += [b([2,12,0],[14,16,4],'restraint','lap cushion'),b([4,13,-1],[12,15,0],'rubber','hand grip')]
    else:
        for x in (2,12):
            parts += [b([x,11,front],[x+2,24,front+2],'restraint','body restraint pad'),
                      b([x,22,front+1],[x+2,25,15],'metal','shoulder hinge arm')]
        parts += [b([2,10,front],[14,13,front+2],'restraint','lower crossbar')]
    return parts,[8,hinge,14],55 if lap else 50

def fixed_hinges(pivot):
    _,y,z=pivot
    return [b([0,y-2,z-2],[2,y+2,z+2],'contact','left fixed hinge socket'),b([14,y-2,z-2],[16,y+2,z+2],'contact','right fixed hinge socket')]

def seat_variants(family):
    if family in ('spinning','s2_spinning'):
        for outer in (False,True):
            for face in ('north','south','east','west'):
                yield {'outer':str(outer).lower(),'facing':face},9.6 if outer else 6.4,12 if face=='south' else 4,face=='south',False
    elif family=='roller_ball':
        for left in (False,True):
            for face in ('north','south','east','west'):
                yield {'left':str(left).lower(),'facing':face},20 if left else 12,16,face=='south',False
    elif family=='pipeline':
        for right in (False,True):
            for rear in (False,True):
                yield {'right':str(right).lower(),'rear':str(rear).lower()},1.6 if right else 14.4,12 if rear else 4,False,False
    else:yield {},8,8,False,family not in ('flying','new_generation_flying')

def seats(family):
    name=family+'_seat'; descriptors=[]
    state=json.loads((ASSETS/'blockstates'/f'{name}.json').read_text())
    # Existing identities and all supported state combinations stay stable.
    for when,cx,cz,south,rotate in seat_variants(family):
        moving,pivot,angle=harness(family)
        fixed=shell(family)+fixed_hinges(pivot)
        if family in ('spinning','s2_spinning'):
            # Native fractional seats are ten units wide; squeeze only the X shell envelope.
            for e in fixed+moving:
                for key in ('from','to'):e[key][0]=8+(e[key][0]-8)*.75
        elif family=='pipeline':
            for e in fixed+moving:
                for key in ('from','to'):e[key][0]=8+(e[key][0]-8)*.75
        fixed=transformed(fixed,cx,cz,south); moving=transformed(moving,cx,cz,south)
        pivot=[cx,pivot[1],cz+(8-pivot[2] if south else pivot[2]-8)]
        suffix='_'.join(f'{k}_{v}' for k,v in when.items()) or 'standard'
        moving_name=f'fd917/{family}_restraint_{suffix}'
        export_model(moving_name,moving,family)
        descriptors.append({'model':'fourthdimension:block/'+moving_name,'property':'restrained','pivot':pivot,'axis':'x','open_degrees':-angle if south else angle,'rotate_facing':rotate,'when':when})
        if family in ('flying','new_generation_flying'):
            # Separate ankle U-frame rotates about its own side hinges, not the body harness.
            leg=[b([1,-6,-1],[3,4,1],'metal','left ankle arm'),b([13,-6,-1],[15,4,1],'metal','right ankle arm'),b([2,-6,-2],[14,-3,0],'restraint','ankle pad')]
            export_model(f'fd917/{family}_leg_restraint',leg,family)
            descriptors.append({'model':f'fourthdimension:block/fd917/{family}_leg_restraint','property':'legs','pivot':[8,4,6],'axis':'x','open_degrees':-75,'rotate_facing':False,'when':{}})
            fixed += [b([0,2,4],[3,6,8],'contact','left ankle socket'),b([13,2,4],[16,6,8],'contact','right ankle socket')]
        matched=set()
        for conditions,variant in state['variants'].items():
            props=dict(pair.split('=') for pair in conditions.split(','))
            if all(props.get(k)==v for k,v in when.items()):matched.add(variant['model'].split('block/')[1])
        for original in matched:export_model(original,fixed,family)
    write(f'fd917_articulation/{name}.json',{'parts':descriptors})


def structures(family):
    for name,source in STRUCTURES.items():
        if not name.startswith(PREFIX.get(family,(family+'_',))):continue
        parts=copy.deepcopy(source.get('elements',[]))
        for e in parts:
            for face in e['faces'].values():
                old=face['texture'].lstrip('#')
                face['texture']='#'+{'pad':'restraint','bar':'restraint','cue':'latch','locked':'contact','signal':'latch'}.get(old,old)
                face.pop('uv',None)
        # Exposed bearing collars and fork gussets belong to the actual existing structure cells.
        if name.startswith('flying_lock_'):
            parts=[b([-1.25,22.75,6.75],[1.25,25.25,9.25],'contact','posture axle core')]
        elif name=='axis_journal':
            parts += [b([-4,10,10],[4,18,15],'contact','longitudinal journal collar'),b([-3,11,-6],[3,17,-3],'contact','front journal collar')]
        elif name=='roller_ball_axle':
            parts += [b([0,4,4],[3,12,12],'contact','shared axle collar'),b([29,4,4],[32,12,12],'contact','shared axle collar')]
        elif name in ('spin_bearing','s2_spinning_bearing'):
            parts=[b([1,-9,1],[15,-5,15],'metal','turntable drum'),b([0,-8,0],[16,-6,16],'contact','turntable race'),b([2,-5,2],[14,-3,14],'body','upper shell mounting plate')]
        elif name=='powered_inverted_yaw_bearing':
            parts += [b([-4,6,3],[4,14,13],'contact','yaw race collar')]
        elif name=='powered_inverted_yaw_spindle':
            parts += [b([5,0,5],[11,3,11],'contact','spindle flange'),b([5,13,5],[11,16,11],'contact','spindle flange')]
        export_model(name,parts,family)


def item(family):
    # Miniatures deliberately reproduce the family arrangement, not one oversized chair.
    if family=='axis':
        parts=[b([0,10,1],[16,12,3],'body','long fork'),b([0,10,13],[16,12,15],'body','long fork'),b([1,5,6],[4,12,10],'contact','roll journal')]
        coords=[(3,6),(9,6)]
    elif family=='roller_ball':
        parts=[b([0,11,6],[16,13,10],'contact','transverse axle'),b([6,3,6],[10,11,10],'metal','axle support')];coords=[(0,2),(10,2),(0,10),(10,10)]
    elif family in ('flying','new_generation_flying','powered_inverted_yaw'):
        parts=[b([0,21,5],[16,23,11],'metal','overhead support')]
        coords=[(0,6),(4,6),(8,6),(12,6)] if family=='flying' else [(2,6),(10,6)]
        parts += [b([0,6,8],[2,21,10],'metal','outer hanger'),b([14,6,8],[16,21,10],'metal','outer hanger')]
        if family!='flying':parts += [b([6,23,6],[10,26,10],'contact','yaw bearing')]
    elif family=='pipeline':
        parts=[b([0,1,0],[16,3,16],'body','narrow floor'),b([-2,12,0],[0,15,16],'metal','left side rail truck'),b([16,12,0],[18,15,16],'metal','right side rail truck')];coords=[(2,2),(10,2),(2,10),(10,10)]
    else:
        parts=[b([2,1,2],[14,4,14],'contact','turntable'),b([0,4,0],[16,6,16],'body','car shell')];coords=[(1,1),(9,1),(1,9),(9,9)]
    for x,z in coords:
        w=3 if family=='flying' else 5
        back=z if family in ('spinning','s2_spinning','roller_ball') and z>=9 else z+4
        front=z+5 if back==z else z
        parts += [b([x,7,z],[x+w,9,z+5],'seat','seat cushion'),b([x,9,back],[x+w,17,back+2],'body','seat back')]
        if family in ('spinning','s2_spinning','powered_inverted_yaw'):
            parts += [b([x,10,front],[x+w,11.5,front+1],'restraint','lap bar')]
        else:
            parts += [b([x,10,front],[x+1,16,front+1],'restraint','left harness'),b([x+w-1,10,front],[x+w,16,front+1],'restraint','right harness')]
    export_model(family+'_car',parts,family,item=True)
    track=[b([0,9,1],[16,11,3],'contact','running rail'),b([0,9,13],[16,11,15],'contact','running rail'),b([0,3,6],[16,6,10],'body','spine'),b([3,6,2],[5,10,14],'metal','cross tie'),b([11,6,2],[13,10,14],'metal','cross tie')]
    if family=='pipeline':track=[b([0,13,0],[16,15,2],'contact','side rail'),b([0,13,14],[16,15,16],'contact','side rail'),b([3,0,0],[5,13,2],'body','side support'),b([3,0,14],[5,13,16],'body','side support'),b([3,0,0],[5,2,16],'metal','underbrace')]
    export_model(family+'_track',track,family,item=True)


def generate(family):
    WRITTEN.clear()
    seats(family);structures(family);item(family)
    path=ROOT/'docs/evidence/fd917/mechanisms'/f'{family}-outputs.json'
    path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(sorted(set(WRITTEN)),indent=2)+'\n')
    print(f'{family}: {len(set(WRITTEN))} geometry/hinge outputs; no metadata or texture writes')
