# FD917 finite production briefs

All 17 styles are required. This is the finite reference/disposition list, not
visual acceptance. Art owns A/B; Mechanisms owns C/D and equipment; lead owns
Arrow's defining connection and shared track renderer. Each final row consumes
its owner's affected occupied/state views without reopening unchanged mechanics.

## References and explicit interpretations

Native scale/material reference is pinned Coasters Simulated file8655742, listed
in [the common sheet](README.md#shared-reference-and-material-contract).
Manufacturer pages establish visible principles, not hidden internal dimensions.

- [Intamin ZacSpin](https://www.intamin.com/product/zac-spin-coaster/) supports
  compact vertical-track seating, open leg space and forward/backward flipping.
  Legacy retains its adopted eight-seat back-to-back grouping. Revised is an
  original independently articulated interpretation, not a claimed second
  generation manufacturer specification.
- [B&M Wing](https://www.bolliger-mabillard.com/wing-coaster) supplies the fixed
  outboard, open-leg silhouette; retain four seats and all sixteen existing colors.
- [B&M Inverted](https://www.bolliger-mabillard.com/inverted-coaster) explicitly
  supports freely dangling feet. Remove the model-only foot shelf and front rods;
  the interaction shape is already just the cushion, so no selection/mass/pivot
  change is needed for that removal.
- [B&M Dive](https://www.bolliger-mabillard.com/dive-coaster) supports a wide
  floorless row. Retain the adopted six seats and fixed body; the hold block is a
  remote control, not an implied physical jaw at a remote location.
- [The Bat operator reference](https://www.sixflags.com/kingsisland/attractions/the-bat)
  establishes an overhead hanging cabin that swings laterally. Retain our two-seat
  cabin, actual longitudinal journal and limited sweep; do not copy themed art.
- [PAX's current site](https://www.pax.ru/) does not establish the exact rotating
  shuttle/catch dimensions needed here. The production design is explicitly an
  original compact shuttle based on the adopted functional contract. Mechanisms
  uses a remote kinetic drive unit and a route-local guided carriage, subject to
  its connection proof. No invented cable is drawn to an arbitrarily placed winch.
- S2 and Powered Inverted use the reference-consistent lap-bar direction selected
  by Mechanisms from the MACK primary pages. Fixed native anchors and capacities
  remain. Conventional Flying retains distinct body/leg restraints; New-generation
  Flying makes the mod's added yaw axis explicit rather than implying a replica.
- Arrow's compact gearbox/follower layout remains lead-owned. Measured profile
  lag and capture mismatch require an explicit connection solution before final
  housing acceptance. The red bucket shell is independent work, not proof of that
  transmission. Free Spin's magnetic reaction surface follows Mechanisms' actual
  equipment envelope, not an inferred manufacturer linkage.

## Complete 17-style disposition

| Style / owner | Retained contract and resources | Required production difference and source |
| --- | --- | --- |
| Arrow / Art + lead mechanism | Two transverse carriers, four seats, native chassis, profile programs and rail gauge | Mechanical bucket shells and tapered crowns; visible real transmission/axle; whole-car/four-rail items. `generate-content.py`; lead renderer. |
| Legacy / Art | Two common rotors, eight seats in back-to-back groups; native axle centers | Compact deep capsule cheeks and continuous paired-back crowns; eight-seat item, exposed common rotor. `generate-content.py`. |
| Free Spin / Art | Four independent paired carriers/eight seats, native magnetic runtime | Narrow tall pods, flared lower cheeks, distinct rotor supports and reaction surfaces supplied by Mechanisms; paired-row item. `generate-content.py`. |
| Revised / Art | Four low independent pair pivots/eight seats; original variant identity | Split cradle, short arm pods and separate bearing spines; open beam web exposes independent supports. `generate-content.py`. |
| PAX / Art + Mechanisms equipment | Two rotors/four seats per car, adopted linked shuttle authority, receiver above lead chassis | Compact boxed shuttle shell/short nose/lower mounting rail, exposed central receiver space; remote drive and route-local carriage. `generate-content.py`; equipment owner. |
| Wing / Art | Four rigid outboard seats, native chassis, all16 cushion colors | Fixed pedestal, taller narrow pod fairing and webbed wing bracket, no fake rotary bearing; four-seat wing item. `generate-content.py`. |
| Fixed Inverted / Art | Four rigid fractional seat centers, rear overhead frame/native wheel fit | Open dangling-leg area, compact bucket backs and rear suspension rail; no foot platforms in world/item. `generate-fixed-inverted.py`. |
| Suspended / Art + Mechanisms lock | Two-seat cabin, overhead journal/fore-aft rods, existing side doorway and swing limit | Stepped cabin nose, intentional shell panels/trim, complete hangers, paired lock/receiver supplied by Mechanisms. `generate-suspended.py`. |
| Dive / Art + Mechanisms hold | Six-abreast fixed row, two mounting rails and existing hold authority | Broad floorless bucket row/head fairings, exposed below-seat brackets, truthful remote hold control; six-seat item. `generate-dive.py`. |
| Axis / Mechanisms | Two-seat longitudinal roll, native fork/journal and prescribed envelope | Engineered fork section, readable roll hub and separate pod shell; not a transverse Free Spin clone. `generate-axis.py`. |
| Roller Ball / Mechanisms | Shared transverse rocking carrier, damping/lock authority | Enclosing pod back, coherent axle/latch and shared rocker identity. `generate-roller-ball.py`. |
| Conventional Flying / Mechanisms | Four-abreast seated/prone posture, independent body/leg authority | Purpose-built body/leg harnesses, posture hinge and paired lock hardware. `generate-flying.py`. |
| New-generation Flying / Mechanisms | Two-abreast nested posture/yaw, preserved attachment frames | Distinct yaw bearing versus posture hinge, compact deck, separate body/leg cues. `generate-new-generation-flying.py`. |
| S1 / Mechanisms | Four-seat back-to-back vertical-axis spinner | Complete car shell/footwell with readable turntable, no S2 launch hardware. `generate-spinning-content.py`. |
| S2 / Mechanisms | Four-seat inverting/launch spinner, existing seat anchors | Lap-bar shell, central bearing, separate rotation magnets versus launch interface. `generate-s2.py`. |
| Pipeline / Mechanisms | Seated four-seat TOGO-inspired body, side-rail contacts | Narrow unified body, textured/world-lit wheel mounts/tread/hubs, no stand-up Surf substitution. `generate-pipeline.py`; Pipeline wheel renderer. |
| Powered Inverted / Mechanisms | Two-seat upright hanging yaw pod, native Chain propulsion | Lap-bar pod, separate yaw housing and truthful remote drive control; no invented contact wheels. `generate-powered-inverted.py`. |

All unmodified recipes, language, native running gear, interaction targets and
saved programs are retained. Existing custom material sprites remain available
for resource compatibility; production bindings move to the new deliberate
material vocabulary. Track structural topology changes belong to lead, and
equipment bodies/state to Mechanisms. Particle textures follow the main material.
The resource inventory lists exact ownership/disposition, including items and
teaching rather than treating file counts as finished designs.

## A/B component envelopes and affected claims

Original five and Wing use the existing Create seat block origin and mount.
Original-family carrier seats remain at the factory's `y=-1` offsets; Legacy's
back-to-back `z=-1/+1` grouping and Free/Revised's `z=-3/+3` carriers are untouched.
The common shoulder hinge is `[8,22,14]` model units, X rotation0 closed/+55° open,
with the model rotated around `[8,8,8]` by block facing. Sidebars lie at x2..4 and
12..14, outside the central head span. The new open travel is a changed visual
sweep requiring occupied review; no server restraint authority changes.

Fixed Inverted seats preserve their fractional center `(0,.9375,.5)` and cushion
shape x−7..7,y12..15,z2..14. Hinge `[0,29,15]`, open+55°, uses the fixed native
orientation, not a second facing rotation. The frame coordinates are unchanged.
Raised bar travel remains below the overhead frame; the exact sweep and actual
rider must be checked together, not inferred from an endpoint picture.

Dive preserves x−7..7,y8..11,z2..14 cushion and fixed six-seat row. Hinge
`[0,27,15]`, open+55°, fixed native orientation. Remove the visual foot ledge but
retain the underseat mounting rail/bracket. Suspended retains its actual pivot
`-1.8125`, child offsets `(-.5,-.25,0)` and seat blocks(-1,-2,0)/(0,-2,0).
The common seat hinge is checked against cabin/rod geometry at adopted swing.

PAX's catch receiver reserve is chassis local `(.5,3.5,.5)`; carriage center is
the route anchor plus3 local-up. Art adds no obstruction in that receiver reserve.
Arrow final supports and actual-child crank conform to the accepted lead follower/axle envelope.

Current Art status: all nine assigned production families pass their distinct occupied fit, with shared restraint state/reload and lighting evidence reused. PAX shell/controller/receiver fit comes from Mechanisms' actual occupied run. Arrow actual-child crank and parent stays are accepted with the final interface view and lead range evidence. All17 item icons and shared transforms, changed Ponder lessons and Track Styles/Original boundary pass. See [handoff](README.md), [items/teaching](items-teaching.md), [appearances](appearances.md) and [Dive fit](dive-fit.md). Other owners retain their assigned C/D mechanism acceptance; this brief is not a claim to have run every owner fixture.
