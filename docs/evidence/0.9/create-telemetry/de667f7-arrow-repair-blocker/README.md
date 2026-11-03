# Occupied Arrow repair-to-realignment blocker

**Failed acceptance, not a release-gate pass.** On source
`de667f7bd7deef808d6229df9e2d386e87dd598a`, normal matching-track repair succeeded,
but an end-of-track fault prevented the subsequent normal realignment. A normal
Capture retry did not resolve it. The observer received the retained fault and
dispatch denial; the rider stayed assigned. Fault clearing and normal unloading
were not achieved.

Artifact: `coasters-fourth-dimension-0.9.0-dev.27.jar`, SHA-256
`6231a0241bef082df3f3edff689e74ae2604465aa095c13656136f13e10de3c5`.
The standalone dependency set is in `manifest.json`. Schema 6 / protocol 7.
No source change or rebuild occurred during this case. The original build reused
cached compile/test outputs; this is a two-client operator observation, not a
new native test-suite result. Xvfb :121 / llvmpipe / 854×480; no hardware performance
or complete rendered-clearance claim.

## Reproduction and observations

The dedicated server and Dev/Observer clients continued from the preceding
controller-lifecycle case. RCON prepared fixture blocks, supplied creative items,
positioned operators, and read/saved state. Boarding, restraint controls, endpoint
removal, track connection, Display Link configuration, repair and realignment used
normal client interaction. No validation boarding/repair command or direct
vehicle-state write was used.

1. Build two normal Arrow sections A `(32,83,20)` → M `(32,83,38)` → B
   `(32,83,56)`, each 18 blocks. Normally place the Arrow car at M. Its initial
   saved binding is A→M. Place/bind a station `(26,83,42)`, link `(26,83,41)`
   and sign `(26,84,48)`; select **Ride fault** through the Display Link GUI.
   This same source/target remains configured throughout the case.
2. Dev boards normally from `(27.5,84,38.5)` and closes restraints through the
   Rotation Tool. Observer goggles show **1/4**, aligned, restraints closed,
   dispatch ready. The fault sign is blank.
3. Observer breaks A through normal creative input. The car changes its binding
   to the still-connected matching M→B section and remains fault-free. This is
   recorded separately; it is not evidence of complete support loss.
4. Observer breaks B. With both approaches absent, the car enters **Matching
   track support lost**, requires realignment, and retains its rider. Goggles
   show **1/4**, dispatch denied and the fault. The sign displays the cropped
   text “Matching track”; its NBT stores the full fault.
5. Normally replace B on its stone support and reconnect B→M with the Arrow
   track item. To reach M below the occupied car, Observer stands at
   `(34.5,80,38.5)`, yaw 90, pitch -42. The restored section retains the previous
   geometry fingerprint and rotation profile and remains invalid/inert pending
   repair. A→M is still absent. A repeated held use after successful connection
   is rejected as an occupied edit; it does not establish a second connection.
6. Dev selects **Repair matching track** in the normal editor. M→B becomes valid
   and keeps its fingerprint/profile. The support fault is replaced by
   **End of compatible track; capture and inspect the route** while
   `alignment_required` remains true. The editor now offers **Realign matching
   track**, not **Repair matching track**. Observer receives the changed fault,
   alignment denied and dispatch denied; the fault sign updates to the new text.
7. Dev selects **Realign matching track**. The action rejects with **Repair
   matching track before realignment**. Dev then invokes normal **Capture** once,
   reopens the editor and retries realignment. The same rejection remains.
   Original chassis, both carrier identities and the rider/carrier/seat assignment
   remain intact in every occupied observation.
8. Save/flush the retained blocked state. Stop the server with RCON `stop`; both
   clients receive **Server closed**, then exit through **Quit Game**. Server and
   both client process sessions finish with exit code 0. Stop the empty owned
   Xvfb display. Preserve the full stopped world locally at
   `build/telemetry-local-acceptance/de667f7/post-arrow-repair-blocker-world`.
   `stopped-foundation.dat` retains the shutdown saved state in this bundle.

## Evidence interpretation

`observations.jsonl` preserves all 13 attempted stages rather than removing failed
interactions. Labels are action intentions, not assertions:

- `repair-occupied-support-lost` and `repair-occupied-support-lost-confirmed`
  preceded successful anchor removal; their recorded anchor NBT and empty fault
  show that support still existed.
- `repair-occupied-support-absent` records successful A removal and same-family
  rebinding to M→B, not a support-loss fault.
- `repair-both-approaches-absent` was an unsuccessful B-removal attempt;
  `repair-no-support-verified` is the actual support-loss observation.
- `repair-section-restored-inert` preceded a successful connection;
  `repair-restored-verified` is the actual inert restoration.
- `repair-explicit-repair-before-realign`, `repair-explicit-realign`,
  `repair-capture-retry-blocked` and `repair-final-retained-blocker` establish the
  persistent repair/realignment conflict.

Some short clicks did not register. Held input completed removal/connection;
server block NBT and saved state establish which attempt changed the world.
One initial B-anchor placement hit a side face, creating an unused neighboring
anchor before the correct B anchor was placed. A few temporary stone blocks are
fixture scaffolding. No failed attempt is presented as a successful operation.
The initial raw `boarding_ready` field can lag the fault transition; the actual
shared telemetry correctly denied alignment/dispatch while realignment was required.

The screenshots are original client captures. Sign text is limited by its visible
width; raw sign NBT preserves full text. The logs are complete through shutdown,
including earlier cases in this runtime, rather than isolated per-case logs.
No claim is made that logs are warning-free. The local full world is not bundled;
the preserved foundation data and observations support identity/state assertions.

## Code path to investigate

On the pinned source, `BoundaryGuard.check` can assign the end-of-compatible-track
fault after repair makes the rail valid. `VehicleOperations.realignMatchingSupport`
rejects any nonempty fault before snapping. Normal Capture does not clear faults,
and `RotationEditorScreen` presents Repair only for the matching-support-lost fault.
This explains the observed blocked UI sequence; it is not a tested fix or a claim
that every support-loss layout fails. A release-branch change needs a fresh exact-
artifact operator retest, including retained identity, fault clearing, readiness,
and normal unloading. FD401/FD402/FD902 and the complete 0.9 gate remain open.

Run `python3 verify.py` from this directory to check the recorded state assertions
and manifest hashes. The verifier does not launch Minecraft or prove unrecorded
motion, optional profiles, cold restart of this failed case, or an eventual repair.
