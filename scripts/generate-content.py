#!/usr/bin/env python3
"""FD917 original-family production geometry. No recipes/language/equipment writes."""
import json
from pathlib import Path
from fd917_materials import box,model,texture,item_display,COLORS
R=Path(__file__).resolve().parents[1];OUT=R/'src/main/resources/assets/fourthdimension'
OWNED=set((R/'docs/evidence/fd917/art/requested-output-paths.txt').read_text().splitlines())
def write(path,value):
 p=OUT/path;rel=p.relative_to(R).as_posix()
 assert rel in OWNED or path.startswith(('models/block/fd917/','fd917_articulation/')) or path in {
  'models/block/paired_spine.json','models/block/paired_spine_cross.json',
  'blockstates/paired_spine.json','blockstates/paired_spine_cross.json'},rel
 p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(value,indent=2)+'\n')
def shoulder():
 p=[]
 for x in (2,12):p += [box([x,9,3],[x+2,22,5],'restraint','shoulder padding'),box([x,20,4],[x+2,22,14],'metal','hinge arm')]
 return p+[box([3,8,3],[13,10,5],'restraint','lap crossbar'),box([6,8,2.75],[10,9.5,3],'latch','center buckle')]
def seat(f):
 p=[box([1,2,2],[15,5,14],'cushion','seat cushion'),box([2,5,12],[14,21,14.5],'cushion','back cushion'),box([1,0,3],[15,2,14],'metal','underseat support')]
 if f=='arrow':
  p += [box([0,3,13],[16,21,16],'paint','mechanical bucket shell'),box([3,21,12.5],[13,25,16],'paint','tapered crown'),box([4,21,12],[12,24,14],'cushion','head cushion')]
  for x in (0,14):p.append(box([x,2,4],[x+2,10,13],'paint','short bucket cheek'))
 elif f=='legacy':
  p += [box([0,3,13],[16,21,16],'paint','paired rotor back shell'),box([2,21,12],[14,24,16],'paint','continuous rotor crown'),box([4,20,11.5],[12,23,13],'cushion','head pad')]
  for x in (0,14):p.append(box([x,3,2],[x+2,15,13],'paint','compact capsule cheek'))
 elif f=='free_spin':
  p += [box([2,3,13],[14,22,16],'paint','narrow independent pod'),box([4,22,12],[12,26,16],'paint','pod head shell'),box([5,22,11.5],[11,25,13],'cushion','head pad')]
  for x in (0,13):p += [box([x,3,5],[x+3,11,14],'paint','flared pod cheek'),box([x,12,13],[x+3,21,16],'metal','rotor support cheek')]
 elif f=='revised':
  p += [box([2,3,13],[14,21,16],'paint','split pair bucket'),box([4,21,12],[12,25,16],'paint','inset crown')]
  for x in (0,14):p += [box([x,1,3],[x+2,9,14],'metal','separate pair cradle'),box([x,7,3],[x+2,10,11],'paint','short arm pod'),box([x,11,14],[x+2,24,16],'metal','bearing spine'),
                           box([x,8,11],[x+2,22.5,16],'metal','cradle to bearing web')]
  p.append(box([0,8,13],[16,11,16],'metal','rear cradle crossmember'))
 elif f=='pax':
  p += [box([1,3,13],[15,21,16],'paint','compact shuttle shell'),box([3,21,12],[13,25,16],'paint','shuttle head fairing'),box([2,0,0],[14,2,4],'paint','short car nose')]
  for x in (0,14):p += [box([x,2,1],[x+2,10,14],'paint','boxed shuttle side'),box([x,0,3],[x+2,2,15],'contact','lower mounting rail')]
 elif f=='wing':
  # The fixed beam cells are replaced by seat blocks at the outer stations.
  # A full-width mounting shoe continues the beam through each seat; all of
  # this assembly is rigid on the native chassis, with no invented rotary hub.
  p += [box([0,0,4],[16,3.5,12],'metal','continuous fixed mounting shoe'),
        box([1,4,14.5],[15,16,16],'paint','lower wing shell'),
        box([2,16,14.5],[14,21,16],'paint','tapered wing shoulders'),
        box([4,21,13],[12,24,16],'paint','inset wing crown'),
        box([5,24,13],[11,26,16],'paint','compact head fairing'),
        box([5,22,11.5],[11,25,13],'cushion','head pad')]
  for x in (0,14):p += [box([x,1,12],[x+2,12,14],'metal','rigid wing upright'),
                        box([x,5,7],[x+2,8,12],'paint','tapered arm fairing')]
 else:raise ValueError(f)
 # The axle sits ahead of these child-local shells. Rails carry its load to the
 # rear face; exposed posts then bring it to the pans without crossing the
 # rotating shoulder restraint at x=2..4/12..14. The paired Free Spin/Revised
 # seat models rotate 180 degrees on the rear row, but the axle block does not:
 # their rails reach Z=21 so both orientations overlap the fixed axle position.
 if f in ('arrow','free_spin','pax'):
  front=-5 if f=='arrow' else -6
  rear=21 if f=='free_spin' else 17
  for x in (0,14.5):
   p += [box([x,23,front],[x+1.5,24.5,rear],'metal','axle to rear post rail'),
         box([x,1,15.5],[x+1.5,24.5,17],'metal','exposed rear load post')]
  p.append(box([1,1,13],[15,2.5,17],'metal','rear underseat crossmember'))
 elif f=='revised':
  # Keep the paired rail low enough to sit below the restraint hinge tips.
  for x in (0,14.5):
   p.append(box([x,21,-6],[x+1.5,22.5,21],'metal','axle to bearing spine rail'))
 for x in (1,13):p += [box([x,20,13],[x+2,24,16],'metal','fixed restraint hinge'),box([x-.1,21,14],[x+.1,23,16],'contact','hinge pin')]
 return p
families={'arrow':'red','legacy':'yellow','free_spin':'blue','revised':'purple','pax':'green'}
for name,color in [('arrow_controlled_controller','red'),('zacspin_legacy_controller','yellow'),('pax_shuttle_controller','green'),('sns_free_spin_controller','blue'),('zacspin_revised_controller','purple')]:
 # Retain the controller envelope; distinguish the enamel casing from its
 # structural crossbeam and access plates instead of leaving concrete placeholders.
 p=[box([2,0,2],[14,8,14],'paint','controller casing'),
    box([0,8,5],[16,10,11],'metal','beam lower flange'),
    box([0,14,5],[16,16,11],'metal','beam upper flange'),
    box([0,10,7],[16,14,9],'metal','beam web')]
 for z in (1,14):p += [box([4,2,z],[12,7,z+1],'metal','access plate'),box([5,3,z-.05],[6,4,z],'bolt','plate screw')]
 # All five original families place a stationary transverse wing strut in the
 # block above this controller. Two stanchions and a saddle carry its middle
 # down to the native cart while leaving the controller visible between them.
 p += [box([2,9,5],[5,25,11],'metal','left chassis to crossbeam stanchion'),
       box([11,9,5],[14,25,11],'metal','right chassis to crossbeam stanchion'),
       box([1,20,5],[15,27,12],'metal','crossbeam support saddle')]
 write(f'models/block/{name}.json',model(p,color))
for f,c in families.items():
 for state in ('open','closed'):write(f'models/block/{f}_seat_{state}.json',model(seat(f),c))
 write(f'models/block/fd917/{f}_restraint.json',model(shoulder(),c))
 write(f'fd917_articulation/{f}_seat.json',{'parts':[{'model':f'fourthdimension:block/fd917/{f}_restraint','property':'restrained','pivot':[8,22,14],'axis':'x','open_degrees':55,'rotate_facing':True,'when':{}}]})
 write(f'blockstates/{f}_seat.json',{'variants':{f'facing={d},restrained={str(cl).lower()}':{'model':f'fourthdimension:block/{f}_seat_{"closed"if cl else "open"}','y':a}for d,a in [('north',0),('east',90),('south',180),('west',270)]for cl in (False,True)}})
for name,z0,z1 in [('carrier_axle',-7,-4),('free_spin_axle',-8,-4),('legacy_axle',11,12.5),('revised_axle',-8,-4)]:
 p=[box([0,6,z0],[16,10,z1],'contact','rotating axle'),box([0,5,z0-.25],[2,11,z1+.25],'metal','journal collar'),box([14,5,z0-.25],[16,11,z1+.25],'metal','journal collar')]
 if name=='legacy_axle':
  # The Legacy carrier has two back-to-back seat rows. These outboard rails
  # belong to the rotating child axle, so both rows meet the common rotor even
  # though the rear seat models turn 180 degrees without rotating the axle.
  for x in (0,14):
   p.append(box([x,6,-16],[x+2,10,32],'metal','common rotor longitudinal rail'))
 write(f'models/block/{name}.json',model(p,'gray'))
for name,y0,y1 in [('wing_strut',4,12),('revised_strut',12,20)]:
 # The lower Revised row beams otherwise only face-touch the center spine at
 # world Y=2.25. One model unit of overlap joins the real row to that spine
 # without a visible riser above the beam.
 if name=='revised_strut':y1=21
 p=[box([0,y0,4],[16,y0+1.5,12],'metal','lower flange'),box([0,y1-1.5,4],[16,y1,12],'metal','upper flange'),box([0,y0+1.5,7],[16,y1-1.5,9],'metal','central web')]
 write(f'models/block/{name}.json',model(p,'gray'))
# Paired cars need a continuous fore-aft connection through the center of the
# transverse row beams. The old seven wing_strut cells made their isolated
# upper flanges look like a row of raised spines from the rider's sightline.
# Keep one real crossbeam at each row, and make the intervening support a
# low continuous box beam below the transverse top flange.
spine=[box([4,1,0],[12,5.5,16],'metal','continuous low spine beam')]
write('models/block/paired_spine.json',model(spine,'gray'))
cross=[box([0,4,4],[16,5.5,12],'metal','row lower flange'),
       box([0,10.5,4],[16,12,12],'metal','row upper flange'),
       box([0,5.5,7],[16,10.5,9],'metal','row web')]
write('models/block/paired_spine_cross.json',model(spine+cross,'gray'))
for name in ('paired_spine','paired_spine_cross'):
 write(f'blockstates/{name}.json',{'variants':{'':{'model':f'fourthdimension:block/{name}'}}})
p=[box([0,2,4],[16,3.5,12],'metal','fixed wing lower flange'),box([0,6.5,4],[16,8,12],'paint','wing top cap'),box([0,3.5,7],[16,6.5,9],'metal','fixed wing web')]
write('models/block/fixed_wing_beam.json',model(p,'cyan'))
write('models/block/wing_fixed_controller.json',{'parent':'fourthdimension:block/fixed_wing_beam'})
for c in COLORS:
 for state in ('open','closed'):write(f'models/block/wing_seat_{c}_{state}.json',model(seat('wing'),'gray',padding=c))
 write('models/block/fd917/wing_restraint.json',model(shoulder(),'cyan'))
write('fd917_articulation/wing_seat.json',{'parts':[{'model':'fourthdimension:block/fd917/wing_restraint','property':'restrained','pivot':[8,22,14],'axis':'x','open_degrees':55,'rotate_facing':True,'when':{}}]})
def miniature(f,c):
 # Compact actual shell geometry with the beam at axle/head height, not below feet.
 paired=f in ('free_spin','revised');legacy=f=='legacy'
 zs=(-4,20)if paired else(4,12)if legacy else(8,)
 xs=(-8,-4,20,24)
 p=[box([5,3,4],[11,7,12],'metal','native chassis silhouette'),box([7,7,7],[9,13,9],'metal','chassis tower')]
 for z in (3,11):
  p += [box([3,2,z],[6,5,z+2],'rubber','native wheel silhouette'),box([10,2,z],[13,5,z+2],'rubber','native wheel silhouette'),box([4,3,z-.2],[12,4,z],'contact','wheel axle')]
 beams=zs if paired else(8,)
 for z in beams:
  p += [box([-7,12,z+1],[27,13,z+3],'metal','transverse axle beam')]
 if paired:p += [box([7,12,-3],[9,13,23],'metal','longitudinal carrier spine')]
 if legacy:p += [box([-7,11.5,4],[0,13,16],'metal','common rotor'),box([20,11.5,4],[27,13,16],'metal','common rotor')]
 if f=='revised':
  for z in zs:p += [box([-3,10,z+1],[-1,13,z+3],'paint','low independent pair fork'),box([17,10,z+1],[19,13,z+3],'paint','low independent pair fork')]
 if f=='pax':p += [box([6,13,8],[10,19,10],'metal','reserved lead catch spine'),box([5,18,7],[11,20,11],'contact','catch receiver silhouette')]
 if f=='wing':p += [box([-7,11,9],[27,12,11],'paint','fixed wing saddle')]
 for z in zs:
  for x in xs:
   for part in seat(f)+shoulder():
    q=json.loads(json.dumps(part));a=part['from'];b=part['to']
    # Back-to-back rows deliberately face away from the centre on both paired styles.
    if len(zs)>1 and z==zs[-1]:a,b=[16-b[0],a[1],16-b[2]],[16-a[0],b[1],16-a[2]]
    q['from']=[a[0]*.25+x,a[1]*.25+7,a[2]*.25+z]
    q['to']=[b[0]*.25+x,b[1]*.25+7,b[2]*.25+z]
    p.append(q)
 m=model(p,'gray' if f=='wing' else c,padding=c if f=='wing' else None);m['display']=item_display(.48 if paired else .52)
 # Whole winged cars span 36 model units; normalize their held silhouette so
 # the outer pair does not disappear beyond the first-person viewport.
 for hand,x,angle in [('right',-2,135),('left',2,225)]:
  m['display'][f'firstperson_{hand}hand']={'rotation':[0,angle,0],'translation':[x,3,0],'scale':[.18]*3}
 return m
for name,f in [('arrow_car','arrow'),('legacy_zacspin_car','legacy'),('free_spin_car','free_spin'),('revised_zacspin_car','revised'),('pax_car','pax'),('wing_car','wing')]:write(f'models/item/{name}.json',miniature(f,families.get(f,'cyan')))
for name,f in [('arrow_track','arrow'),('legacy_zacspin_track','legacy'),('free_spin_track','free_spin'),('revised_zacspin_track','revised'),('pax_track','pax')]:
 p=[box([0,9,2],[16,11,4],'contact','running rail'),box([0,9,12],[16,11,14],'contact','running rail'),box([0,2,6],[16,5,10],'paint','track spine')]
 for x in (2,12):p.append(box([x,5,3],[x+2,9,13],'metal','cross tie'))
 if f=='arrow':p += [box([0,6,-2],[16,7,0],'contact','control rail'),box([0,6,16],[16,7,18],'contact','control rail')]
 m=model(p,families[f]);m['display']=item_display(.7);write(f'models/item/{name}.json',m)
print('Exported original-family shells, articulation parts and whole-car items; equipment untouched')
