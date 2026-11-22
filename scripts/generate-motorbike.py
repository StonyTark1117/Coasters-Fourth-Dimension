#!/usr/bin/env python3
"""Original FD2202 straddle geometry. Writes only motorbike-prefixed exports."""
import copy, json
from pathlib import Path
from fd917_materials import box, model, item_display, COLORS
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
ASSET=RES/'assets/fourthdimension'
paths=[]
def write(path,data):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(data,indent=2)+'\n');paths.append(path)
def mesh(name,parts,color='orange',display=False):
    data=model(parts,'orange',color)
    if display:data['display']=item_display(.6)
    write(ASSET/'models'/f'{name}.json',data)
def b(lo,hi,mat,name):return box(lo,hi,mat,name)
# All load-bearing segments overlap the crossbeam / pan / hinge; no floating shells.
base=[b([6,1,5],[10,5,12],'metal','saddle_riser'),
      b([5.5,4,1.5],[10.5,5.5,14.5],'metal','saddle_pan'),
      b([6,5.5,2],[10,7.8,14],'seat','narrow_straddle_cushion'),
      b([6,2,-3],[10,5,5],'metal','tank_frame'),
      b([5.6,5,-4],[10.4,12.5,3],'paint','front_tank'),
      b([7,3,-4],[9,16,-2],'metal','handlebar_stem'),
      b([2,15.5,-4],[14,17,-2],'metal','handlebar'),
      b([1.5,15.3,-4.2],[4,17.2,-1.8],'rubber','left_grip'),
      b([12,15.3,-4.2],[14.5,17.2,-1.8],'rubber','right_grip'),
      b([3,-2,3],[13,1,6],'metal','step_crossbar'),
      b([6,-2,4],[10,3,7],'metal','step_riser'),
      b([0,-3,-2],[4,-1,4],'contact','left_footrest'),
      b([12,-3,-2],[16,-1,4],'contact','right_footrest'),
      b([5,2,12],[11,5,17],'metal','rear_mount'),
      b([2,2,15],[4,13,17],'metal','left_hold_post'),
      b([12,2,15],[14,13,17],'metal','right_hold_post'),
      b([2,2,12],[14,4,17],'metal','rear_cross_mount'),
      b([2,12,15],[14,14,17],'contact','hold_hinge_axle')]
restraint=[b([3,10,12],[4.5,13,16],'metal','left_yoke_arm'),
           b([11.5,10,12],[13,13,16],'metal','right_yoke_arm'),
           b([4,10,11],[12,13,14],'harness','rear_hip_hold_down'),
           b([4,10,14],[12,12,15],'latch','hold_down_latch')]
frame=[b([0,14,5],[16,18,11],'metal','continuous_native_crossbeam'),
       b([3,13,4],[13,15,12],'paint','crossbeam_cap')]
controller=[b([-8,0,5],[24,2,11],'metal','native_top_crossbeam'),b([5,0,5],[11,8,11],'metal','controller_pedestal'),
            b([3,7,3],[13,12,13],'paint','motorbike_control_box'),
            b([4,12,4],[12,13,12],'contact','control_face'),
            b([5,13,5],[7,14,7],'latch','stop_control'),
            b([9,13,8],[11,14,10],'rubber','dispatch_control')]
for color in COLORS:
    for end in ['open','closed']:mesh(f'block/motorbike_seat_{color}_{end}',base,color)
mesh('block/fd917/motorbike_restraint',restraint)
write(ASSET/'fd917_articulation/motorbike_seat.json',{'parts':[{'model':'fourthdimension:block/fd917/motorbike_restraint','property':'restrained','pivot':[8,13,16],'axis':'x','open_degrees':110,'rotate_facing':True,'when':{}}]})
variants={}
for color in COLORS:
    for facing,y in [('north',0),('east',90),('south',180),('west',270)]:
        for state,end in [('false','open'),('true','closed')]:
            variants[f'color={color},facing={facing},restrained={state}']={'model':f'fourthdimension:block/motorbike_seat_{color}_{end}','y':y}
write(ASSET/'blockstates/motorbike_seat.json',{'variants':variants})
for name,parts in [('frame',frame),('controller',controller)]:
    mesh(f'block/motorbike_{name}',parts)
    write(ASSET/f'blockstates/motorbike_{name}.json',{'variants':{'':{'model':f'fourthdimension:block/motorbike_{name}'}}})
    mesh(f'item/motorbike_{name}',parts,display=True)
mesh('item/motorbike_seat',base+restraint,display=True)
# Complete two-saddle miniature, normalized into a block item without dropping the native beam.
whole=[]
for offset in [-16,16]:
    for part in base+restraint:
        item=copy.deepcopy(part)
        for key in ['from','to']:item[key]=[(part[key][i]+([offset,16,0][i])-8)*.28+8 for i in range(3)]
        whole.append(item)
for off in [-16,16]:
    for part in frame:
        item=copy.deepcopy(part)
        for key in ['from','to']:item[key]=[(part[key][i]+([off,0,0][i])-8)*.28+8 for i in range(3)]
        whole.append(item)
for part in controller:
    item=copy.deepcopy(part)
    for key in ['from','to']:item[key]=[(part[key][i]+([0,16,0][i])-8)*.28+8 for i in range(3)]
    whole.append(item)
mesh('item/motorbike_car',whole,display=True)
track=[b([2,4,0],[4,6,16],'contact','left_N_rail'),b([12,4,0],[14,6,16],'contact','right_N_rail'),
       b([3,1,2],[13,4,4],'metal','cross_tie_A'),b([3,1,12],[13,4,14],'metal','cross_tie_B'),
       b([6,0,0],[10,2,16],'paint','Motorbike_spine')]
mesh('item/motorbike_track',track,display=True)
write(RES/'data/fourthdimension/recipe/motorbike_car.json',{'type':'minecraft:crafting_shaped','pattern':['SIS','ITI','ICI'],'key':{'S':{'item':'minecraft:saddle'},'I':{'tag':'c:ingots/iron'},'T':{'item':'simulatedcoasters:coaster_track'},'C':{'item':'simulatedcoasters:coaster_cart'}},'result':{'id':'fourthdimension:motorbike_car','count':1}})
write(RES/'data/fourthdimension/recipe/motorbike_track.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':'simulatedcoasters:coaster_track'},{'item':'minecraft:orange_dye'}],'result':{'id':'fourthdimension:motorbike_track','count':1}})
evidence=ROOT/'docs/evidence/1.4/fd2202';evidence.mkdir(parents=True,exist_ok=True)
(evidence/'generated-files.txt').write_text('\n'.join(str(p.relative_to(ROOT)) for p in sorted(paths))+'\n')
(evidence/'localization.json').write_text(json.dumps({'item.fourthdimension.motorbike_car':'Motorbike Car','item.fourthdimension.motorbike_track':'Motorbike Track','block.fourthdimension.motorbike_seat':'Motorbike Saddle','block.fourthdimension.motorbike_frame':'Motorbike Crossbeam','block.fourthdimension.motorbike_controller':'Motorbike Controller'},indent=2)+'\n')
print(f'Exported {len(paths)} owned Motorbike files')
