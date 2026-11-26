#!/usr/bin/env python3
"""Original modular moving-track machinery; writes only moving_* resources and a language fragment."""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/fourthdimension'
D=ROOT/'src/main/resources/data/fourthdimension'
LANG={}
MECHANISMS={'transfer':'Transfer platform','turntable':'Turntable','drop':'Drop track','tilt':'Tilt track','elevator':'Elevator','teeter':'Teeter-totter','side_drop':'Side-drop','compound':'Compound table'}
PARTS={'bed':'Moving bed beam','overhead_gantry':'Moving overhead gantry','pipeline_bed':'Moving Pipeline bed','pipeline_brace':'Moving Pipeline portal brace','guide':'Moving guide rail','pivot':'Moving pivot bearing','lock':'Moving end lock','socket':'Moving docking socket'}
def write(path,value):
 path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(value,indent=2)+'\n')
def box(name,a,b,texture='steel'):
 return {'name':name,'from':a,'to':b,'faces':{f:{'texture':'#'+texture,'uv':[0,0,16,16]} for f in ('north','south','east','west','up','down')}}
def model(elements):
 return {'parent':'minecraft:block/block','textures':{'steel':'fourthdimension:block/fd917/structural','bearing':'fourthdimension:block/fd917/machined','paint':'fourthdimension:block/fd917/paint_cyan','signal':'fourthdimension:block/fd917/paint_orange','dark':'fourthdimension:block/fd917/rubber','particle':'fourthdimension:block/fd917/structural'},'elements':elements}
def export(name,elements,locked=False):
 ident='moving_'+name
 write(A/'models/block'/f'{ident}.json',model(elements))
 write(A/'models/item'/f'{ident}.json',{'parent':f'fourthdimension:block/{ident}'})
 variants={}
 for face,rot in [('north',{}),('east',{'y':90}),('south',{'y':180}),('west',{'y':270}),('up',{'x':270}),('down',{'x':90})]:
  for latch in ([False,True] if locked else [None]):
   key='facing='+face+(f',locked={str(latch).lower()}' if latch is not None else '')
   variants[key]={'model':f'fourthdimension:block/{ident}'+('_locked' if latch else ''),**rot}
 write(A/'blockstates'/f'{ident}.json',{'variants':variants})
 write(D/'loot_table/blocks'/f'{ident}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'fourthdimension:{ident}'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
 return ident
# Every module has an unbroken mechanical load path to the next module.
bed=[box('left longitudinal flange',[0,3,0],[4,8,16]),box('right longitudinal flange',[12,3,0],[16,8,16]),box('front tie',[0,3,0],[16,6,3]),box('rear tie',[0,3,13],[16,6,16]),box('rail saddle',[5,5,5],[11,20.25,11],'bearing'),box('saddle root',[3,4,6],[13,6,10])]
export('bed',bed)
overhead=[box('left gantry upright',[0,0,5],[3,16,11]),box('right gantry upright',[13,0,5],[16,16,11]),box('overhead tie',[0,12,4],[16,16,12]),box('underslung rail saddle',[5,5,5],[11,14,11],'bearing'),box('left bolted foot',[0,0,0],[5,3,16]),box('right bolted foot',[11,0,0],[16,3,16])]
export('overhead_gantry',overhead)
pipeline=[box('side-contact left bed',[0,3,0],[3,10,16]),box('side-contact right bed',[13,3,0],[16,10,16]),box('low cross tie',[0,0,6],[16,4,10]),box('left mounting shoe',[0,8,5],[5,12,11],'bearing'),box('right mounting shoe',[11,8,5],[16,12,11],'bearing'),box('deep side-rail spine saddle',[5,3,5],[11,22,11],'bearing')]
export('pipeline_bed',pipeline)
portal=[box('continuous portal beam',[6,4,0],[10,8,16])]
export('pipeline_brace',portal,True)
write(A/'models/block/moving_pipeline_brace_locked.json',model(portal+[box('connected corner upright',[6,0,6],[10,16,10])]))
guide=[box('guide web',[5,0,6],[11,16,10]),box('rear guide flange',[2,0,10],[14,16,13]),box('front machined running face',[3,0,3],[13,16,6],'bearing')]
export('guide',guide)
pivot=[box('bolted bearing pedestal',[0,0,2],[16,4,14]),box('bearing web',[3,3,3],[13,12,13]),box('fixed outer race',[1,6,1],[15,14,15],'bearing'),box('retained axle',[5,8,0],[11,12,16]),box('upper keeper',[3,13,4],[13,15,12],'signal')]
export('pivot',pivot)
lock=[box('lock base',[1,0,2],[15,4,15]),box('lock cylinder',[3,3,6],[13,11,15]),box('lock throat',[5,5,1],[11,9,8],'bearing'),box('retained indicator',[6,10,7],[10,12,13],'signal')]
export('lock',lock,True)
write(A/'models/block/moving_lock_locked.json',model(lock+[box('extended locking bolt',[6,6,0],[10,8,8],'signal')]))
socket=[box('socket flange',[0,0,3],[16,4,16]),box('socket cheeks left',[0,3,1],[4,12,16]),box('socket cheeks right',[12,3,1],[16,12,16]),box('alignment receiver',[4,3,7],[12,6,16],'bearing'),box('contact stop',[4,4,13],[12,11,16],'signal')]
export('socket',socket)
# Eight housings share a stationary rear input bearing; raised badges expose the motion in shape.
for i,(key,title) in enumerate(MECHANISMS.items()):
 es=[box('stationary bolted base',[0,0,1],[16,3,15]),box('drive housing',[2,2,3],[14,12,14],'paint'),box('rear kinetic input collar',[5,5,12],[11,11,16],'bearing'),box('protected front panel',[3,6,1],[13,12,4],'dark'),box('operator button',[10,7,0],[12,9,2],'signal'),box('status bar',[4,10,0],[9,11,2],'bearing')]
 if key in ('transfer','side_drop'):
  es += [box('translation badge rail',[3,12,6],[13,14,8],'bearing'),box('translation arrow tip',[11,12,4],[14,14,10],'signal')]
 if key in ('drop','elevator'):
  es += [box('vertical badge stem',[7,12,5],[9,16,7],'bearing'),box('vertical arrow',[4,12 if key=='drop' else 14,4],[12,14 if key=='drop' else 16,8],'signal')]
 if key in ('turntable','compound'):
  es += [box('yaw badge front',[4,12,5],[12,14,7],'bearing'),box('yaw badge side',[10,12,5],[12,14,12],'bearing'),box('yaw arrow',[7,12,10],[12,14,13],'signal')]
 if key in ('tilt','teeter','compound','side_drop'):
  es += [box('pitch badge low',[3,12,8],[6,14,11],'bearing'),box('pitch badge high',[5,13,8],[8,16,11],'signal')]
 export(key,es)
 LANG[f'block.fourthdimension.moving_{key}']=title+' controller'
for key,title in PARTS.items():LANG[f'block.fourthdimension.moving_{key}']=title

def recipe(name,pattern,keys,count=1):
 write(D/'recipe'/f'moving_{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':{k:{'item':v} for k,v in keys.items()},'result':{'id':f'fourthdimension:moving_{name}','count':count}})
recipe('bed',['III','S S','III'],{'I':'minecraft:iron_ingot','S':'create:shaft'},4)
recipe('overhead_gantry',['BBB','I I','I I'],{'B':'fourthdimension:moving_bed','I':'minecraft:iron_ingot'},4)
recipe('pipeline_bed',['I I','BBB','I I'],{'B':'fourthdimension:moving_bed','I':'minecraft:iron_ingot'},4)
recipe('pipeline_brace',['I I','III','I I'],{'I':'minecraft:iron_ingot'},8)
recipe('guide',['ISI','ISI','ISI'],{'I':'minecraft:iron_ingot','S':'create:shaft'},4)
recipe('pivot',['III','SBS','III'],{'I':'minecraft:iron_ingot','S':'create:shaft','B':'create:mechanical_bearing'},2)
recipe('lock',['IRI','PSP','III'],{'I':'minecraft:iron_ingot','R':'minecraft:redstone','P':'create:brass_ingot','S':'create:shaft'},2)
recipe('socket',['I I','L L','BBB'],{'I':'minecraft:iron_ingot','L':'fourthdimension:moving_lock','B':'fourthdimension:moving_bed'},2)
components={'transfer':'create:linear_chassis','turntable':'create:mechanical_bearing','drop':'create:clutch','tilt':'create:gearbox','elevator':'create:rope_pulley','teeter':'create:gearshift','side_drop':'create:gantry_carriage','compound':'create:rotation_speed_controller'}
for name,part in components.items():recipe(name,['ICI','RPR','BBB'],{'I':'minecraft:iron_ingot','C':part,'R':'minecraft:redstone','P':'create:precision_mechanism','B':'fourthdimension:moving_bed'})
LANG.update({'moving.fourthdimension.title':'Moving track','moving.fourthdimension.compatibility':'Compatibility','moving.fourthdimension.guide':'Construction guide','moving.fourthdimension.preview':'Preview assembly','moving.fourthdimension.validate':'Validate','moving.fourthdimension.configure':'Build validated assembly','moving.fourthdimension.capture':'Capture whole train','moving.fourthdimension.move':'Move to destination','moving.fourthdimension.resume':'Resume after inspection','moving.fourthdimension.return':'Return to a valid dock','moving.fourthdimension.release':'Release to fixed track','moving.fourthdimension.evacuate':'Stationary evacuation','moving.fourthdimension.waiting':'Waiting for the server…','moving.fourthdimension.unavailable':'Moving assembly unavailable. Inspect its controller and loaded supports.','moving.fourthdimension.power':'Stationary rear shaft: real Create power is required.','moving.fourthdimension.part':'Place this component where the validated assembly preview shows it.','moving.fourthdimension.empty_service':'Empty service transfer only','moving.fourthdimension.no_spend':'Preview and validation spend nothing.'})
# Functional insert modules are distinct items; no skin/model choice can substitute for one.
INSERTS={'wing':('Wing','simulatedcoasters:coaster_track'),'arrow':('Arrow controlled 4D','fourthdimension:arrow_track'),'legacy':('Legacy ZacSpin','fourthdimension:legacy_zacspin_track'),'free_spin':('Free Spin','fourthdimension:free_spin_track'),'revised':('Revised ZacSpin','fourthdimension:revised_zacspin_track'),'pax':('PAX','fourthdimension:pax_track'),'spinning':('Classic Spinning S1','fourthdimension:spinning_track'),'s2_spinning':('Launched Spinning S2','fourthdimension:s2_spinning_track'),'pipeline':('Pipeline','fourthdimension:pipeline_track'),'dive':('Dive','fourthdimension:dive_track'),'suspended':('Suspended','fourthdimension:suspended_track'),'fixed_inverted':('Fixed Inverted','fourthdimension:fixed_inverted_track'),'axis':('Axis','fourthdimension:axis_track'),'flying':('Flying','fourthdimension:flying_track'),'new_generation_flying':('New-generation Flying','fourthdimension:new_generation_flying_track'),'powered_inverted_yaw':('Powered Inverted Yaw','fourthdimension:powered_inverted_yaw_track'),'roller_ball':('Roller Ball','fourthdimension:roller_ball_track'),'standing_standard':('Standard Standing','fourthdimension:standing_track'),'standing_surf':('Surf Standing','fourthdimension:surf_track'),'motorbike':('Motorbike','fourthdimension:motorbike_track'),'flying_bike':('Flying Bike','fourthdimension:flying_bike_track'),'native_service':('Empty native service','simulatedcoasters:coaster_track')}
for index,(key,(title,track)) in enumerate(INSERTS.items()):
 name='insert_'+key
 es=[box('functional mounting plate',[1,3,1],[15,5,15]),box('left rail contact',[2,5,1],[5,8,15],'bearing'),box('right rail contact',[11,5,1],[14,8,15],'bearing'),box('certified interface cross tie',[4,5,6],[12,7,10],'paint')]
 # Raised binary key teeth are an original physical identity motif, not permission data.
 for bit in range(5):
  if (index+1)&(1<<bit):es.append(box('key tooth '+str(bit),[3+bit*2,5,2],[4+bit*2,7,5],'signal'))
 write(A/'models/item'/f'moving_{name}.json',model(es))
 recipe(name,['III','ITI','III'],{'I':'minecraft:iron_ingot','T':track},8)
 LANG[f'item.fourthdimension.moving_{name}']=title+' moving-track insert'
 LANG[f'moving.fourthdimension.configuration.{key}']=title
LANG['moving.fourthdimension.compatibility_hint']='Use the controller panel to inspect all compatible car configurations.'
LANG['moving.fourthdimension.insert']='Functional insert: %s. One module per block of bed length; appearance does not grant compatibility.'
LANG['moving.fourthdimension.insert_pairing']='Requires %s contact and %s mounting. Server inspection verifies the actual rail.'
LANG.update({'moving.fourthdimension.configuration':'Car configuration','moving.fourthdimension.configuration_value':'Car: %s','moving.fourthdimension.length':'Bed length','moving.fourthdimension.length_value':'Length: %s','moving.fourthdimension.facing':'Direction','moving.fourthdimension.facing_value':'Facing: %s','moving.fourthdimension.preset':'Finite program','moving.fourthdimension.preset_value':'Amount %s | %s° → %s° | show %s','moving.fourthdimension.origin':'World bed origin X / Y / Z','moving.fourthdimension.coordinates_invalid':'Enter whole-number world coordinates for X, Y and Z.','moving.fourthdimension.destination':'Destination','moving.fourthdimension.destination_value':'Destination: %s','moving.fourthdimension.refresh':'Refresh','moving.fourthdimension.close':'Close','moving.fourthdimension.previous':'Previous','moving.fourthdimension.next':'Next','moving.fourthdimension.back':'Back','moving.fourthdimension.power_value':'Stationary input: %s RPM','moving.fourthdimension.phase_value':'Phase: %s','moving.fourthdimension.locks_value':'Measured locks: %s','moving.fourthdimension.updated':'Server state updated.','moving.fourthdimension.valid':'Construction clear. Build spends the exact listed materials.','moving.fourthdimension.invalid':'Construction refused. Correct the highlighted issue and validate again.','moving.fourthdimension.compatibility_details':'Contact: %s; mount: %s; up to %s cars; pitch limit %s°.','moving.fourthdimension.dismantle':'Dismantle empty assembly'})
DECISIONS={'allowed':'Compatible pairing; complete-train retention, restraints and measured dock locks are still required.','unknown_configuration':'Choose one of the original supported car configurations.','unknown_provider_version':'Use the pinned native track provider and a recognized functional insert.','unsupported_motion':'This configuration supports level transfer, turntable and elevator only. Choose a supported controller.','shallow_tilt_only':'Classic Spinning supports at most 30° pitch. Select the shallow tilt preset.','wrong_insert':'Install the functional insert for this exact car family and variant.','contact_mismatch':'The rail contact geometry does not match the car. Replace the insert.','mount_mismatch':'Use the matching above-rail, overhead or Pipeline side-contact bed.','missing_interface':'Restore the required control, magnetic, yaw or posture interface.','non_native_material':'Use original native Coasters track material. External track does not qualify.','invalid_deck':'Choose a straight 16, 32 or 48 block bed.','partial_train':'Bring the complete homogeneous train onto the bed with both end margins.','mixed_train':'Separate mixed families; capture one homogeneous train.','recovery_cart':'Remove recovery carts before capture.','too_many_cars':'Use at most four cars and obey this family’s lower limit.','oversize_train':'Use a longer bed; reserve two blocks beyond each end of the complete train.','empty_service_only':'The ordinary service configuration permits empty native carts only.','custom_native_cart':'Custom native bodies are unsupported. Use an original supported car.','carrier_not_held':'Stop and retain every chassis and moving carrier before moving the bed.','restraints_open':'Close every occupied restraint before capture or movement.','still_rolling':'Stop every car and carrier before capturing the train.','invalid_route':'Restore the family’s required route and safe posture before capture.','destination_unavailable':'Clear and restore the exact receiving span, then reserve it again.','obstructed_sweep':'Clear the complete occupied sweep, including seats, carriers and guides.','propulsion_owner':'Release competing propulsion ownership before moving-track capture.','managed_circuit':'Move this train outside the managed 1.2 circuit before capture.','launch_systems':'Launch Systems handoff is outside 1.5. Use native fixed approaches.'}
for key,value in DECISIONS.items():LANG['moving.fourthdimension.decision.'+key]=value
PAGES=[
 'Construction: place the matching controller and connect real Create power to its rear shaft. Open the front panel. Choose the exact car configuration, a 16/32/48 block bed, its world origin and cardinal direction. Preview spends nothing. Cyan outlines show the full moving structure sweep; gold outlines show fixed machinery; green boxes identify native dock anchors. Socket positions lists exact block coordinates. Close the panel to place native anchors while nearby preview ghosts remain visible; Clear preview removes them. Clear the sweep and validate. Build then consumes the server-listed parts and functional insert modules. After Build, use the normal matching track tool to connect fixed approach rails to the exact sockets; Capture refuses until they are valid. Existing curves on a shifted or angled socket must be removed and rejoined after Build. Keep the controller outside the sweep and within eight blocks when operating.',
 'Parts and survival: craft bed beams, overhead gantries or Pipeline side-contact beds, guides, pivots, end locks and docking sockets. Each has its own recipe. The matching controller uses a precision mechanism and the relevant Create drive component. Craft the exact family insert with its normal track item plus eight iron ingots: eight modules per recipe, one module per bed-length block. Skins, colors and car body variants never grant mechanical compatibility. The panel shows the exact structural bill before Build.',
 'Operating and recovery: board at a fixed station, close restraints and stop the complete train on the bed. Capture retains all chassis and carriers. Choose a destination, then Move. Release requires the real bed to stop and align, locks to engage and the exact receiving rail to be reserved and reconnected. Power loss, obstruction, broken support, unload and restart hold the train. Inspect the cause and use explicit Resume or Return; no saved release flag grants permission. Evacuation requires stationary retained bodies and safe measured exits. Never board, couple or repaint while retained.',
 'Wing transfer example: choose Wing, length 16 and translation 4. With origin (0,80,0) facing south, the starting rail anchors are (0,80,-8) and (0,80,8); the second span is four blocks east. Place matching original native approach rails. Preview and build the visible bed, outboard guides and locks. Board one Wing car at a fixed station, stop it wholly within the bed, close restraints, Capture, Move to end, then Release only after measured locking.',
 'Wing turntable example: choose Wing, length 16, ports 0° to 90°. Preview marks the two perpendicular fixed spans around the same center. Build each approach with the normal track tool; keep the full circular sweep clear. The full-show option performs an extra bounded turn before the same named exit. Never use a visual heading alone as docking evidence: the panel’s measured locks and release guards remain authoritative.',
 'Dive drop example: choose Dive, length 16 and drop 4. Install Dive inserts and matching native Dive approach rails at the upper and lower sockets. Clear the vertical sweep below all six seats. The downward stroke is bounded, not free fall; the reset is powered. Capture the complete stopped car, Move to the lower dock, Release after locking, then return the empty bed explicitly.',
 'Wing tilt example: choose Wing and 30° or 90° tilt. The bed pivots at its center. Exact angled socket positions are computed from the finite program; never round the rail endpoint geometry to a block grid. The visible native anchor identifies the socket while the native rail attaches at its exact endpoint. Keep the full pitched train and rider sweep clear. Classic Spinning may use only the 30° option.',
 'Classic Spinning elevator example: choose Classic Spinning S1, length 16 and lift 4, 8, 16 or 32. Use S1 inserts, a real elevated receiving span and full-height guides. Close restraints and latch the local spinning carrier before capture. This level lift does not grant S1 inverted/90° tilt capability. A stopped lift with lost power remains held until an explicit recovery request passes inspection.',
 'Wing teeter example: choose Wing, length 16 and one to three cycles. Build one starting dock and clear the complete ±15° pitch sweep. Capture only a stopped complete train. The finite show returns to its original level pose; Release still waits for measured locks. Do not place approach scenery, riders or machinery inside the teeter envelope.',
 'S2 side-drop example: choose S2, length 16 and side-drop 4 or 8. The bed stays level while translating diagonally down and sideways by the selected amount. Preview locates the second fixed span and the combined sweep. Retain the spinning carrier before capture. Build the second real receiving track before movement; this is not a teleport between disconnected anchors.',
 'S2 compound and alternate mount examples: compound combines the shortest yaw turn (at most 180°) with +15° or -15° pitch and finishes level at the selected port. Use the full simultaneous sweep. For suspended families choose an overhead gantry, leaving the under-rail rider space open. For Pipeline choose its distinct low side-contact bed. Compatibility lists all 22 configurations and limits; only original supported pairings can be built.'
]
for i,text in enumerate(PAGES):LANG[f'moving.fourthdimension.guide.page.{i}']=text
LANG.update({'moving.fourthdimension.contact.coasters_n_contacts_v1':'native N rail','moving.fourthdimension.contact.pipeline_p_contacts_v1':'Pipeline P side rail','moving.fourthdimension.mount.overhead_n_v1':'overhead gantry','moving.fourthdimension.mount.above_rail_v1':'above-rail bed','moving.fourthdimension.mount.between_side_rails_v1':'low side-contact bed with end portals'})
LANG['moving.fourthdimension.details']='Server message'
LANG['moving.fourthdimension.materials']='Materials'
LANG['moving.fourthdimension.materials_preview']='Request a preview to see the exact material list.'
LANG.update({'moving.fourthdimension.sockets':'Socket positions','moving.fourthdimension.sockets_preview':'Request a preview to list exact native anchor blocks.','moving.fourthdimension.socket_name':'Named dock: %s','moving.fourthdimension.socket_position':'%s: X %s / Y %s / Z %s','moving.fourthdimension.sockets_teaching':'Place native anchor blocks at these positions. Build the assembly before joining matching approach rails to its exact sockets. Nearby preview ghosts remain visible after closing the panel; Clear preview removes them.','moving.fourthdimension.clear_preview':'Clear preview','moving.fourthdimension.dispatch_service':'Dispatch empty service train'})
write(ROOT/'scripts/generated/moving-track-en_us.json',LANG)
print(f'Generated {len(MECHANISMS)} original controllers, {len(PARTS)} structural modules, recipes and language fragment')
