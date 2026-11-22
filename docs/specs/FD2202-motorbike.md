# FD2202 — Motorbike straddle configuration

Implementation specification frozen 2026-11-22. Dimensions are original game design targets until geometry/native/client evidence verifies them. Primary `main` baseline `59c40d11`; accepted 1.3 behavior remains closed.

## Reference and identity

[Vekoma Motorbike Coasters](https://www.vekoma.com/motorbike-coasters), consulted 2026-11-22, establishes the motorbike train concept and discusses newer track/train designs. Its [Toverland reference](https://www.vekoma.com/references/motorbike-coaster) identifies Booster Bike, 2004. Neither page supplies numeric restraint geometry, rider joint angles or clearance limits. The following original game dimensions and hold-down mechanism are not attributed to Vekoma; no branded assets are copied.

Stable family `motorbike`, variant `straddle_v1`; one native above-track chassis, one fixed carrier sharing its UUID, two side-by-side saddle seats. One car is supported. No independent rotary child or posture changeover; no vehicle coupling/recovery towing. Native retained stop and safe return-to-platform unloading remain required. Default body/appearance are `base`/`original`; this is a mechanical family rather than a 1.3 body choice.

## Dimensions and rider

Coordinates are blocks in the graph frame: X right, Y up, authored forward −Z/NORTH. Native plot-block origin is graph Y−0.1875. Seat blocks are offsets `(−1,1,0)` and `(1,1,0)` from plot center, their centers X=−1/+1 relative to chassis center. Native gear remains `N_CONTACTS`, `ABOVE`, rail half-gauge .772, wheel radius .2.

| Contact or volume | Target |
| --- | --- |
| Frame footprint | X ±1.70, Z ±1.25; side beams at offsets (±1,0,0), native bearing (0,0,0) preserved; controller bridge Y .8125–.9375 overlaps side beams/risers |
| Saddle | Width .25, depth .75, top Y1.30; narrow enough for separated thighs |
| Hip | `(seatX,1.3125,0)`, seat-cell local `(.5,.5,.5)` |
| Native player feet reference | Hip minus .75 → graph Y .5625; standard eye offset1.62 → eye Y2.1825 |
| Tank/fairing | Width≤.30, Y≤1.60, Z−.85…−.30, outside thighs |
| Grips | X seatX±.35, Y1.80, Z−.73; visibly connected bars |
| Footrests | X seatX±.40, Y .70, Z−.48, connected brackets |
| Closed hold-down | Rear hinged padded yoke at lower back/hip, Y1.45–1.65, Z+.20…+.40; no spine through torso |
| Open hold-down | Raised behind rider, Y≤2.35/Z≤1.0; entry unobstructed |
| Reserved occupied/restraint sweep | X±1.85, Y .45…2.65, Z±1.50, plus native gear/contact space below |
| Placement separation | ≥6 blocks between native bearings; coupling rejected |
| Platforms | Outside X±2.25, floor Y .50–1.05, width≥1, free headroom≥2 |

Dedicated SeatEntity `positionRider` handling places the real hip on server and client. Create passenger identity/Sable native transform remain authoritative. Model limbs form a straddle pose with separated bent legs and arms reaching grips; torso stays bike-forward, head/mouse look independent. Clothing/armor must follow the same pose. Native body rotation is composed exactly once; no Flying prone compensation. Joint endpoints and clearance are measured against exported mesh, not presumed from a default sitting animation. Camera uses native eye origin and existing Sable/RiderComfort. Dismount clears specialty pose and restores ordinary gravity/camera.

## Track and operation

Use a player-constructible **matching functional Motorbike Track** binder: family `motorbike`, variant `straddle_v1`, native N contacts above track. It supplies no rotary mechanism; it is not a Wing ordinary-track alias. Reject wrong family/variant, moving/sublevel tracks, unsupported materials, overhead/side-rail mounts, CTNB and Waterparked. Minimum radius16; frame change≤.05rad/block; grade≤30°, bank≤45°, no inversion. Sample curves and occupied clearance before allocation and while running, including stopping distance over a connected edge. Ambiguous/incompatible/unloaded continuation retains a stop.

Propulsion: native Chain drive/gravity through existing PropulsionInterlock. Example nominal4blocks/s via64RPM native Chain; declared profile≤4blocks/s with explicit overspeed tolerance. No special launch force or optional drive adapter. Stop/maintenance/fault must block drive and brake. Dispatch/Resume records the signs of existing native Chain anchors on the matching connected route (≤64 anchors); loss, removal or reversal retains a stop. Gravity spans need no synthetic motor. Resume revalidates current power deliberately before accepting a new direction. Boarding and restraint operations require speed≤.05, upright up.y≥.999, the live bound station and original rail/fingerprint, ≥12-block level dock with two-block end margins, sturdy dry platforms with free headroom on both sides, available matching support and no unresolved fault. Open restraints deny dispatch; closed restraints deny ordinary Shift dismount. Track/support failure retains passengers, closes restraint and records fault. Repair then explicit Stop/acknowledge clears only a resolved fault; resume remains deliberate.

Normal flow: craft/place on matching track → stopped/open/captured → bind the Boarding Station beside both clear platforms → normal saddle use → close hold-down using existing tool → Dispatch → Stop → open → Shift safe exit. Off-station recovery keeps riders secured while support/drive are repaired, then explicitly resumes to the bound safe platform; there is no separate emergency-unload control. One original playable loop and track-only example teach construction and operation; fixture commands are disclosed setup, not player-construction evidence.

## Persistence, content and integration

Persist normal family/variant, fixed carrier topology, seat assignments, bound rail, restraint/stop fields. Validate exact seat list, shared chassis identity and zero rotary offsets. No automatic start after cold restart, no duplicate login seat, no reclassification of unknown variants as Wing. Chunk unload preserves UUIDs; permanent removal evacuates before native body deletion. MotorbikeState persists running intent and accepted Chain-anchor direction signs; reload always retains the car and requires deliberate Resume if it had been running. Dock identity uses the existing fixed-dock fields.

Package owns NEW MotorbikeVehicle (`is/create/validate/FIT/SEATS`), MotorbikeTrack (`rejection/support/obstruction`), MotorbikeRuntime (`ready/tick/safety/acknowledge` plus coordinated dock hooks), MotorbikeSeatBlock, MotorbikeContent with own TRACK/CAR/blocks registers, rider/model mixins, generator, motorbike-prefixed resources, examples/tests and this spec. Root owns existing shared files: Family/TrackDesign, FoundationData/CarrierRuntime, Boarding/VehicleOperations/ConsistRules, EligibilityHooks, factory, registry/event/mixin wiring, editor labels, tabs and central language/generator manifest. Package produces a localization fragment for root. Include original frame, narrow saddle/tank/grips/footrests, connected open/closed hold-down, matching item miniature, controller/track item, recipe and teaching text. Recipe uses verified registered native cart, saddles and iron; Survival item consumption occurs only after successful placement.

## Finite acceptance

1. Native geometry/contact/topology/profile and restraint/dispatch/stop guards; malformed save rejection and intact valid assignment load.
2. Early occupied normal boarding, straddle model/forward camera, powered straight/bank movement, Stop/open/unload; reuse for final acceptance. Validate new synchronization with another client only if shared sync changed.
3. Real recipe/item placement, consumption, guide/example and original item/world consistency.
4. Distinct interrupted drive/support, explicit repair, unload/reload, cold occupied restart, and safe platform exit after normal operation or repaired interruption, with camera restoration. Combine complementary evidence; no unrelated replay campaign.
5. Retain exact source/JAR/raw evidence; save/stop all owned runtimes and verify inactive units. Root reconciles FD2203; publication remains separate.
