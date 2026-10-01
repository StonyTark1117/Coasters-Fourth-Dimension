# Ordinary-bearing recovery

Development build 0.9.0-dev.15 adds explicit recovery coupling for the five
original families. A 4D car stays on its matching functional track throughout.
Only the pinned, unmodified bare Coasters bearing is certified as the ordinary
recovery cart. It acquires no carriers, profile, magnetic assistance or 4D data.
Added blocks or controllers invalidate that certification. While recovery remains
attached, the ordinary bearing also stays on the consist’s functional family;
disconnecting restores its independent ordinary-cart access.

## Operator workflow

1. Stop and inspect the complete 4D consist on matching track. Let riders board
   while restraints are open, then use the Rotation Tool to **Capture**, **Close
   restraints**, and **Retain** every car. Recovery does not release carrier locks.
2. Put the ordinary bearing on the same functional family route, stopped at
   the end car. Axle spacing is six blocks for Arrow, legacy ZacSpin and PAX,
   twelve for Free Spin, and fourteen for revised ZacSpin. Placement must be
   within 0.25 blocks of that spacing before the native coupler settles.
3. Sneak-use the Rotation Tool on the bare bearing to select it. Open the end
   car's tool editor and choose **Attach recovery**. Selection belongs to that
   operator and dimension. Missing equipment, stale edits, extra linked carts,
   incomplete components, wrong track and unretained cars reject coupling.
4. Supply slow ordinary longitudinal propulsion. **Start tow** releases the
   common translational hold while keeping carriers retained and restraints
   closed. The native chain demonstration uses eight RPM, targeting 0.5 blocks/s.
   The recovery speed limit is 0.75 blocks/s. Ride dispatch and releasing
   restraints or retention are prohibited while recovery remains attached.
5. Choose **Brake tow**, then stop the propulsion input. Wait for the ordinary
   bearing to stop and every carrier to align, then choose **Detach recovery**. Only the recovery links are removed;
   original Arrow/PAX links and maintenance retention remain. Open restraints
   and unload normally. Restore ordinary ride operation separately after inspection.

For occupied recovery, use an operator to handle selection, the propulsion
controls and the editor while riders remain restrained. Selecting the bearing
before boarding retains the operator's selection; no rider is automatically
unloaded or repositioned. A normal single-player empty-car exercise is possible.
A survival locomotive or added ordinary assembly needs its own fit certification;
this release does not silently approve one because its bearing has the right gauge.

An assigned PAX train must first hand over its winch using **Detach shuttle**.
The nearby winch and every car must be loaded and stationary, with retained
carriers and closed restraints; only BOARDING or a stopped FAULT permits handover.
An assigned carriage must be present. Handover removes the physical catch/guide
and its actual carriage, clears the winch assignment, and preserves the native
four-car couplings and all chassis/carrier bodies. Moving shuttles reject handover.
Normal winch binding remains unavailable until recovery is disconnected.

## Interruption and save behavior

Lost or altered gear, a broken coupler, missing native components, incompatible
track, an open/wrong-family route boundary or overspeed requests braking and
retains the recovery identity. Restoring availability never resumes towing
automatically. Inspect and use **Start tow** explicitly after every component,
matching track and native link returns. The ordinary leading axle and every
4D car independently guard upcoming compatible-track ends.

A broken recovery coupling does not revoke the disabled 4D car's own matching
rail support. It must remain on those rails while braking. A temporarily missing
bearing must restore before disconnecting, so a dormant native saved link cannot
return after the operator has forgotten it. Actual permanent removal records
a durable tombstone and permits stationary disconnect without recreating that
bearing. Removal never converts a 4D car into an ordinary cart.

Save schema and network protocol are **6**. Existing schemas 1–5 load without
inventing recovery state. Upgrade clients and servers together, back up worlds,
and do not downgrade a recovery-mode world to a version that cannot read schema 6.
A packaged empty Arrow example also preserves its actual coupling through a
fresh-JVM reopening, remains braked, and completes normal start/disconnect and
restraint opening; see the [bounded client check](evidence/0.4/recovery/gui/arrow-gui-acceptance.md).
The native checks include serialization, legacy-schema loading, and actual Sable
holding/restoration of all five occupied recovery groups: bearing, chassis,
carriers, native couplings and 32 passengers. They stay braked after restoration
and resume only through the normal explicit operator action. A complete
fresh-process recovery-mode restart/unload matrix remains part of FD902.

## Flat examples and validated scope

In a clear, loaded corridor, operators can create these standalone examples:

- `/fd demo recovery_arrow`
- `/fd demo recovery_legacy`
- `/fd demo recovery_free_spin`
- `/fd demo recovery_revised`
- `/fd demo recovery_pax`

Each example supplies 48 blocks of matching original track, the correct car,
a correctly spaced ordinary bearing, a native chain drive with its Create
creative motor initially at zero RPM, and side platforms. Coupling and recovery
controls are left to the operator. Set eight RPM in the direction of the ordinary
bearing after attachment; **Start tow** enables travel. Brake before the end and
stop chain power before disconnecting the now-independent ordinary bearing. The
current example starts unpowered; the bounded client check stops power by normal
creative removal of its motor. A clearer switchable example remains needed.
The PAX example is an unassigned recovery car; a bound four-car shuttle uses the
separate handover workflow above. Commands require operator permission and a
17 by 14 by 57 clear corridor inside the supported track/build bounds.

The production native test constructs these actual example fixtures and exercises
normal tool selection, editor request guards, native coupling, fully occupied
retained towing by native chains/Create motors, braking, broken links, permanent
bearing removal, retained rail support, save identity, occupied native
holding/restoration, stationary disconnect and normal unloading. A separate loaded train test tows a two-car Arrow train with eight native
passengers and a four-car PAX train with sixteen; stationary recovery disconnect
preserves all original train links and maintenance retention before normal
unloading. Another test hands over an actual powered PAX carriage without
removing its original train. These headless checks do not establish rendered
operator acceptance, complete banked/curved offsets, nonzero Arrow rack/profile
clearance, Engineered recovery propulsion/FE, coupler strength across those scenes,
or the complete recovery lifecycle matrix. FD402 and the 0.4 gate remain open.
