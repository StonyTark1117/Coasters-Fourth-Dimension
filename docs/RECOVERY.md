# Ordinary-bearing recovery

Development build 0.9.0-dev.19 includes explicit recovery coupling for the five
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
   In the dev.16 example, set motor speed while its clutch lever is ON (power
   disconnected), then turn the lever OFF to supply chain power.
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

A commanded native chain can stall under the load of an occupied consist. Dev.18
monitors actual bearing displacement while native chain speed is commanded above
0.1 blocks/s: remaining within 0.08 blocks for four physics seconds brakes the
whole group and records “Native chain drive stalled”. Zero chain input is not a
stall. Inspect propulsion and cut external power; the fault does not clear and
towing does not resume automatically. Start/brake/disconnect, lost availability
and power cut reset the transient progress window. It is not saved as ride state.

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
creative motor initially at zero RPM, a clutch with its stop lever ON, and side platforms. Coupling and recovery
controls are left to the operator. Set eight RPM in the direction of the ordinary
bearing after attachment; **Start tow** enables travel. Turn the clutch lever
OFF to supply power. Choose **Brake tow** before the end, then turn the lever
ON to disconnect chain power before detaching the now-independent ordinary
bearing. The motor can remain installed at eight RPM. The earlier dev.15
client check removed its directly connected motor; dev.16 replaces that setup
with the switchable clutch. Recovery buttons explain the sequence, ride-only
controls are disabled while attached, and hovering over the live status shows
the complete fault text when it exceeds the visible line. The [dev.16 checkpoint](evidence/0.4/recovery/power-control/acceptance.md) records native five-family lever checks and an empty Arrow client manual-brake, power-stop and fresh-process restoration exercise.
The PAX example is an unassigned recovery car; a bound four-car shuttle uses the
separate handover workflow above. Commands require operator permission and a
17 by 14 by 57 clear corridor inside the supported track/build bounds.

## Curved and banked native recovery examples

Dev.19 adds `/fd demo recovery_bank_arrow`, `recovery_bank_legacy`,
`recovery_bank_free_spin`, `recovery_bank_revised` and `recovery_bank_pax`
under the same `/fd demo` prefix. Each requires a clear, loaded 129 by 14 by
77 corridor inside the supported bounds. Its 162.53-block native route has a
32-block level approach, a quarter bend, a 32-block sustained 30-degree bank,
a 24-block bank-down transition and a 24-block level runout. The initial car
and certified bare bearing have their normal family-specific axle spacing.
The motor starts at zero RPM and the clutch lever starts ON (power cut).

Use the same retained recovery workflow as the flat examples. Brake after the
car returns to the final level runout, turn the lever ON to cut chain power,
wait for readiness, detach recovery, open restraints and unload. Keep the
motor installed at eight RPM. Arrow track contains a full 360-degree profile
on the banked straight; recovery keeps carriers retained without erasing that
profile. An unassigned PAX recovery car does not replace physical handover of
an assigned four-car shuttle.

All five **single-car** routes have occupied native traversal and original seat
identity evidence, with normal brake/power/disconnect/unload controls and measured
body/track anchor and orientation offsets. These are headless native checks;
complete rendered vehicle/rider and control-rack clearance, linked-train recovery
on these curves/banks and the full operator/lifecycle matrix remain open. See the
[banked towing checkpoint](evidence/0.4/recovery/banked-tow/acceptance.md).

**Measured incline limit:** the certified bare bearing cannot pull a fully
occupied four-car PAX train far enough for every car to enter the compact
example's outbound curved spike under the tested eight-RPM chain input. Its
chain remains powered and an unlinked bearing ascends the same spike, but the
loaded group stalls. This does not certify whole-spike recovery or a maximum
permissible grade. Stronger propulsion or added ordinary assemblies need their
own physical fit, force and clearance checks. See the
[failed climb and stall guard](evidence/0.4/recovery/chain-stall/acceptance.md).

The production native test constructs these actual example fixtures and exercises
normal tool selection, editor request guards, native coupling, fully occupied
retained towing by native chains/Create motors, braking, broken links, permanent
bearing removal, retained rail support, save identity, occupied native
holding/restoration, stationary disconnect and normal unloading. A separate loaded train test tows a two-car Arrow train with eight native
passengers and a four-car PAX train with sixteen; stationary recovery disconnect
preserves all original train links and maintenance retention before normal
unloading. The dev.17 test fills an actual four-car powered shuttle with sixteen native
passengers, interrupts its winch, retains it and hands over its physical carriage.
The same original train then tows under native chain power, brakes, cuts power
with its eight-RPM motor retained, disconnects and opens restraints for unloading.
Every passenger keeps its original seat mount until unloading; see the
[occupied handover evidence](evidence/0.4/recovery/occupied-handover/acceptance.md). These headless checks do not establish rendered
operator acceptance, complete vehicle/rider/rack mesh
clearance, linked-train curved/banked recovery, Engineered recovery propulsion/FE,
coupler strength across those scenes,
or the complete recovery lifecycle matrix. FD402 and the 0.4 gate remain open.
