# PAX normal restoration blocks post-repair lifecycle acceptance

**Failed/limited acceptance.** Normal mining removed the occupied station span;
normal PAX track-item restoration rebuilt it with different tangents/normals.
The normal **Repair matching track** action correctly rejected changed geometry.
Realignment, post-repair reconnect/restart and normal unloading were **not reached**.
The original four-car train and its one rider remain retained in the stopped world.
This is not a PAX lifecycle pass or evidence that the geometry guard should weaken.

## Artifact and fixture

- Exact source `311078e8229e54514b1e3e0cbb4837bf87066ce5`, standalone packaged
  `coasters-fourth-dimension-0.9.0-dev.27.jar`, SHA-256
  `dc60cbdefe35cf2737573e57b1509f790cf82342bea6a81764a49066b2b72fd6`.
- Schema 6 / protocol 7, validation observation logging enabled. Build succeeded
  in three seconds with all six tasks up-to-date. No new native-suite result.
- Full copy of the preceding clean-stopped historical Arrow world. Its separate
  four-car PAX example had completed an earlier cycle and was empty at Boarding.
  `input-world.json` preserves both paths and matching input foundation hashes.
  Other Arrow cars are outside this case; availability totals include them.
- PAX station anchors A `(512,85,65)` and B `(512,85,97)`, winch `(506,85,73)`;
  two existing spikes remained connected throughout. The winch remained powered
  at 128 RPM. This was stopped occupied support loss, not an active-lift case.
- Dev is the rider; Observer operates track/restoration and observes goggles.
  RCON supplied creative items/platforms/positioning and read/saved state. No
  vehicle-state writes, curve injection, validation boarding/repair, rebind or
  relaxed guard was used. No source or planning file changed during the case.

## Actual sequence and failed-attempt labels

1. `pax-initial`: four original cars/eight carriers, empty, aligned, open restraints,
   no faults, winch Boarding. The saved station curve is straight with up normals.
2. `pax-board-attempt`: a normal empty-hand click from `(505.5,85,91.5)` with pitch
   -25 misses the seat; assignments remain zero. `pax-board-lower-attempt` with
   pitch -5 **successfully boards** Dev in the fourth car's second carrier, seat
   `274877911039`. No direct mount command was used.
3. `pax-occupied-closed`: Dev normally opens the Rotation Tool editor and selects
   Close restraints. Observer's goggles show 1/16, four cars, aligned and ready.
4. `pax-support-removal-attempt` records **successful removal**, despite its generic
   attempt label. Observer mines the station curve normally from
   `(509.5,85,80.5)`, yaw -90/pitch 12, holding attack about 0.8 seconds. Both
   anchors survive; their peer lists lose the station connection. All four cars
   acquire Matching track support lost, alignment-required, closed restraints,
   stopping and captured carriers. The rider assignment remains intact.
5. Observer uses the normal PAX track item on A, then B. The item reports placement.
   `pax-restored-inert` records the rebuilt span. The saved original profile and
   fingerprint remain unchanged and the rail remains invalid pending repair;
   its rail ID/revision can change through normal restoration. Native geometry
   differs from the original (table below).
6. Dev normally opens the Rotation Tool editor and selects Repair matching track.
   `repair-result.png` shows **Restored geometry changed; unload riders and
   explicitly rebind the profile before operating**. `pax-after-repair` is a
   **rejected action**, not a repair pass. All four faults/alignment flags remain.
7. `pax-repair-geometry-blocked-final`: Observer goggles show 1/16, closed restraints,
   alignment No, dispatch No, support-loss warning and pending-realignment guidance.
   The winch's own phase remains Boarding and its own fault field remains blank;
   whole-consist telemetry correctly exposes the vehicle support faults. No
   dispatch, handle edit, realignment, post-repair reconnect or unload is attempted.

## Geometry mismatch

`observations.jsonl` retains full raw block NBT; `decoded-controller-geometry.json`
is its parsed representation for inspection. At A, the station connection to B has:

| Field | Original | Normal rebuilt span |
| --- | --- | --- |
| First axis | `[0,0,1]` | `[0,-0.21646844758242295,0.9762896144081714]` |
| Second axis | `[0,0,-1]` | `[0,-0.33589067474790874,-0.9419009791997218]` |
| First normal | `[0,1,0]` | `[0,0.9762896144081712,0.21646844758242295]` |
| Second normal | `[0,1,0]` | `[0,0.9419009791997216,-0.33589067474790874]` |
| Each handle length | `10.666666666666666` | `10.666666666666666` |
| Stored profile fingerprint | `-5318209662748205439` | Same, intentionally retained for repair validation |

Endpoints, starts, material and handle lengths are preserved. The spike curves
are unchanged in the observed anchor data. This makes the rejection consistent
with actual changed geometry rather than an unexplained false failure. The
preserved stored fingerprint does not prove the rebuilt physical curve matches.
A source review found native handle controls, but no documented exact normal
restoration procedure was established or tested. This record does not claim that
all possible native handle operations are incapable of restoring the curve.

## Identity, shutdown and limits

Across all eight observations, all four original chassis IDs, eight carrier IDs,
seat layouts and winch vehicle/carriage/car IDs persist. From successful boarding
through the final live state, the original player-to-carrier-to-seat mapping and
native RootVehicle remain present. The stopped foundation file retains that
occupied mapping and all four support faults/alignment flags. Original PAX rail
profiles and stored fingerprints survive; rail identity is not asserted unchanged
across item replacement.

Server save/flush and stop completed; session 57503 exited 0 with all dimensions
saved. Both clients showed Server closed, returned through their menus and used
Quit Game; Dev97820 and Observer76359 exited 0. DISPLAY=:121 had zero child windows
before owned Xvfb1544742 was terminated; wrapper91742 exited 0. The separate
DISPLAY=:99 client was untouched. Full logs include shutdown; screenshots are raw.
The full blocked world is retained locally under
`build/telemetry-local-acceptance/pax-repair-311078e/after-clean-stop-world`.
Committed stopped foundation NBT/JSON preserve the inspected state.

Run `python3 verify.py` to check the recorded identity/interlock/geometry assertions
and bundle hashes. The verifier supports a **failure record**, not acceptance of
repair. Post-repair lifecycle, the 200-tick reconnect mount deadline, winch owner
registration after restart, normal unload, moving support loss, other profiles,
rendered clearance and the complete FD902/0.9 gates remain unverified here.
Xvfb/llvmpipe observations provide functional GUI evidence, not hardware timing.

## Read-only restoration follow-up

[Static inspection](static-restoration-analysis.json) records pinned upstream class
hashes and the placement/handle call paths. Placement runs a curve optimizer;
normal tangent controls have guide snapping but derive length from the dragged
offset and can modify adjoining curves. Our fingerprint checks 17 sampled
positions/up vectors, so visually straight axes alone are insufficient. This is
a reproducible investigation lead, not a proven GUI restoration recipe or an
additional runtime result. No decompiled third-party implementation is bundled.
