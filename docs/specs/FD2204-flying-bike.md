# FD2204 — Original suspended flying bike

Specification frozen 2026-11-22 for implementation on primary `main` from
`59c40d11`. Source feasibility established below; runtime acceptance remains open.
Owner: `fd13_spinning_check`; root owns shared integration files.

## Reference and distinct identity

The [Six Flags announcement](https://www.sixflags.com/magicmountain/new-in-2027)
identifies a suspended multi-launch ride and lists two launches. The
[issuer press release, 10 September 2026](https://www.prnewswire.com/news-releases/six-flags-magic-mountain-announces-thrill-glider-the-worlds-first-thrillglider-roller-coaster-opening-in-2027-302875547.html)
describes prone, face-forward flying-bike riders below the track. These support
this concept only. Our dimensions, capacity, structure, restraints, controls and
propulsion are original game design; commercial actuator details are not inferred.

One rigid native chassis, two parallel prone riders, one train/car per controller.
Persist family `flying_bike` and mechanical variant `prone_multilaunch_v1`.
No independent passenger rotor, seated-to-prone changeover, yaw deck, suspension
swing, Surf travel, or inherited Flying state. No car coupling or multi-train
scheduling. Body/palette selectors from 1.3 do not become mechanics selectors.

## Dimensions and rider contract

Dimensions are in blocks. The native track design frame has origin O at graph
sample, right R, up U and forward F; native N-contact wheel geometry and bearing
anchor remain upstream. Two seat blocks occupy native plot offsets (-1,-3,0)
and (1,-3,0). Geometry and rider transforms must use these same attachment frames.

| Component / reservation | Finite contract |
| --- | --- |
| Native wheels | Existing N contacts, half gauge .772, wheel radius .2, rows .55 apart; no rescaling |
| Complete fit | `NativeTrackFit(N_CONTACTS, OVERHEAD, Envelope(2.25,1.10,4.50,2.50))` |
| Car / occupied reservation | Width 4.50, fore/aft 5.00, above O 1.10 and below O 4.50; rider, open restraint and all support must fit |
| Seats | Two individual fixed bike cradles separated by two blocks; no fake extra capacity |
| Load path | Native bearing → outboard overhead crosshead → two rigid descending spars → each belly/chest pan; supported handlebars and rear calf stops |
| Restraint | One linked body-and-calf hold-down per rider, common closed bit; hinge at rear spar, unobstructed side access when open; all geometry remains attached in both endpoints |
| Rider | Fixed prone torso along native forward (-Z in authored plot); shoulders, pelvis and lower legs supported; hands toward front grips, no free torso yaw |
| Camera | Eye origin matches the authored prone head, but neutral camera points along track forward; normal mouse look is composed once with native chassis bank/roll |
| Board/unload | Level, captured declared dock only, speed <=.05 and five stopped ticks; ordinary empty-hand seat use, close restraints then dispatch; open only after final stop/dock alignment |
| Dock access | Side platforms outside occupied reservation with solid two-block headroom at actual dismount; no automatic drop below an overhead car |

The exact rendered body/eye offsets are implementation outputs and must be
recorded by the measured fit audit before occupied acceptance. The reservation
above is frozen; offsets may be calibrated within it without changing mechanics.
Use one seat ownership and native MoveFunction attachment, including server
loading contribution and restored players. A visual pose alone is not accepted
as a camera or mount proof. No open-restraint flight or automatic mid-route exit.

## Track feasibility and supported route

`NativeTrackFit` already describes the exact overhead N contact orientation and
complete reservation. Fixed Inverted demonstrates a rigid carrier on the same
native chassis; Flying demonstrates continuous fixed-world N-contact roll.
Existing `SuspendedTrack` and `FlyingTrack` correctly reject other variants, so
this vehicle receives its own `TrackDesign.FLYING_BIKE`, original track tool,
metadata validator and exact family/variant check. It is not authorized on
ordinary, conventional Suspended, either Flying, or provider-authored track.

Fixed world track only. Radius >=16 blocks; local roll change <=5 degrees/block;
full track roll is permitted because the rigid rider follows the chassis.
Continuous route must be unambiguous, loaded and within the complete envelope;
no switches, moving track, transfer track, gaps, unsupported segments or native
links. Maximum speed 12 blocks/s. The finite example has a closed route with one
level dock, two distinct ordered launch spans and coasting/banked sections.
No requirement for a commercial-scale inversion count or terrain layout.

Exactly two nonoverlapping committed native Chain spans, each >=32 blocks,
level and upright. Each begins after at least eight unpowered upright, level, straight blocks. The drive anchor must not share a curve joint: the native upper chassis collision half-box can clip an adjacent shaft while residual curve yaw settles. The original example now uses ten edges and two 56-block launches with eight-block run-ins; this is a geometric placement constraint, not a propulsion exemption. Initial launch targets 8 blocks/s; second targets 12 blocks/s.
Each span has a real rear-powered Create transmission and uninterrupted output
shaft to its Chain anchor (<=16 blocks). Target acceleration <=3 blocks/s².
The 8→12 boost needs 13.34 blocks under that cap; initial 0→8 needs 10.67,
leaving explicit runout inside each 32-block span. Final station approach must
provide >=16 blocks of clear braking reservation at 12 blocks/s (6 blocks/s²
brake: 12 blocks stopping distance plus four blocks margin). Exact example
geometry and measured physical speed/stop remain acceptance evidence, not this
source calculation. Launch spans contain no bank, slope, inversions or platforms
inside their reservation; coast sections may bank/roll within the fit above.

## Actual propulsion and authority

Source evidence: `S2SpinningGovernorEntity` is a real Create SplitShaftBlockEntity
whose output derives from rear input RPM and available stress. Native
`CoasterCartChainLiftDrive.applySpeedMatch` applies the Chain's travel to native
cars; `ChainDriveGuardMixin` consults `PropulsionInterlock` and `RideAuthority` at
each callback. Neither native callback nor native-chain authority excludes a
new non-recovery rigid family. `FlyingClearance` already distinguishes actual
upright native drive-port contacts from obstacles. These make the selected
provider feasible without a force/teleport propulsion substitute.

Use a dedicated two-node flying-bike governor. One coordinator is the sole
`RideAuthority.Controller`; its second transmission is a subordinate physical
output, never a second owner. Binding records controller/node UUIDs, exact
anchor endpoints and geometry, selected vehicle UUID and finite route. Both
nodes require actual kinetic supply; 192 RPM supports 12 blocks/s under the
existing 16 RPM per blocks/s relationship. No Creative Motor energy is
synthesized; examples can disclose a creative power source for setup while
normal construction requires a player-supplied kinetic source.

The coordinator renews each output's lease every server tick only while the
same owner/epoch/provider remains valid, route and native bodies are present,
restraints are closed, no hold/fault exists and observed travel belongs to the
expected route phase. Output ramps from measured current state; reversal is not
allowed in ordinary riding. An unoccupied span pre-spins at the car’s measured approach speed, under the same
3 blocks/s² transmission ramp. The native matcher’s otherwise 64/32 blocks/s²
constants are lowered to 3 only for this family by `FlyingBikeChainRampMixin`;
this retains native powered propulsion while bounding physical entry, so matching cannot abruptly brake
or accelerate a car entering its second launch. Only the occupied launch ramps
toward its declared 8/12 target. Both outputs are zero on hold. This explicitly
revises the initial nonactive-zero draft after native entry evidence exposed
an abrupt 7.8→0 speed match; no extra energy source or authority is introduced. A lease older than
one tick, missing coordinator, changed identity, power/stress loss, stale token,
missing track or obstruction cuts output and retains braking. Native hold takes
precedence even before a governor tick. Do not transfer authority while moving.

Finite phases: DOCK → LAUNCH_1 → COAST_1 → LAUNCH_2 → COAST_2 → BRAKING → DOCK.
A repeat cycle requires normal explicit dispatch at the dock. Two physical span
entries and actual acceleration must be measured; a phase counter is not proof.
Manual capture always zeros both outputs and retains the car/riders. Recovery
may move a stopped restrained car toward the declared dock only under an
explicit revalidated authority; it must not replay launch two from a stale phase.

Optional Engineered/Magnetized/CTNB propulsion is not part of this profile.
Shared native-chain ownership fences competing providers; adding support for
another provider would require a named later contract, not a silent fallback.

## Failure, evacuation and persistence

Save rider identities, native chassis UUID, seat map, fixed geometry, dock,
route fingerprint, owner/node identities and progress needed to diagnose an
interruption. Never restore drive output or a live lease. Load always holds,
marks explicit resume required and closes restraints if an occupied/in-flight
state was interrupted. Revalidate current physical support and power before
explicit resume; no auto-launch after power returns or world reload.

Interrupted launch/power loss: zero both transmissions, retain riders and apply
bounded brake. Missing track/body/controller: fault, retain surviving identity,
reject dispatch/unload until matching repair and explicit inspection. Unknown
phase or malformed state is conservative hold, not default running. Permanent
removal releases this controller's binding without affecting another ride.

Normal unload is at the level dock after capture. Revision after native entry
and failure-path diagnosis: a held car on an unpowered coast cannot propel
itself to the original dock; no recovery coupling or invented force is added.
Instead, explicit binding of a second station selects persistent evacuation-only
intent while preserving original dock and launch phase. For every supported
roll/grade, actual transformed seat/head coordinates select a world-horizontal,
dry sturdy standing exit within four blocks horizontally and -1.25 to +.6
blocks vertically, with 1.9 blocks clear headroom. Both seats require exits.
Matching native support and body must survive, restraints stay retained until
five stopped ticks and exit checks pass, then normal open/dismount uses those
exact exits. No new boarding, dispatch or resume is allowed in evacuation mode.
On reload, stationary support and exits must pass again; missing exits retain
riders. Build the necessary safe platform beside the stopped pose and restore
matching support if necessary. After evacuation the empty car may be removed
and rebuilt at its original dock; this is not a claim of occupied powered
recovery from a coast. Disconnect/death, seat occupancy and rejoin follow the
accepted shared ownership contract, with the new fixed prone attachment tested.

## Shared ownership and implementation surface

This owner may add only new FlyingBike* classes, original `flying_bike_*` assets,
new generator/audit/tests/examples and this specification until root's shared
integration is ready. Root owns edits to all existing shared files:

| Shared file / area | Required integration |
| --- | --- |
| Family, TrackDesign, TrackContract | Nonrotary FLYING_BIKE identity, exact fit, overhead/capture/longitudinal capability |
| FoundationData | `FlyingBikeState flyingBike`; stable save/read; fixed carrier geometry, N/overhead validation; never instantiate FlyingState |
| FourthDimension, content tabs | FlyingBikeContent.register; track/car/governor/item registration |
| VehicleItem / track placement and render | Route new exact design to FlyingBikeVehicle.create; original overlay and fit |
| CarrierRuntime, native loading | Rigid tick path; one parent carrier, two rider attachment offsets, zero rotary inertia |
| Boarding / VehicleOperations | ready, dock binding, dispatch/start, acknowledgement/resume, matching support failure, safe unload |
| ConsistRules / link guards | Single-car-only; no mixed family or native links |
| Editor/diagnostics/examples | Existing tool capture/close/dispatch, clear fault/provider/phase readback, example command |
| mixins JSON | New flying-bike seat/body/model mixins only; existing Flying compensation stays unchanged |

New APIs: FlyingBikeVehicle.is/validate/create/FIT/SEATS/VARIANT;
FlyingBikeTrack.rejection/obstruction; FlyingBikeRuntime.ready/tick/safety/
rebindDock/resumeToDock/acknowledge/rememberConsist/validateDispatch/start/fault;
FlyingBikeState.read/save; FlyingBikeContent.register/SEAT/FRAME/GOVERNOR/TRACK/CAR.

## Finite acceptance

1. Audit original support connections, pan/rider/head/hand fit, open/closed
   restraints, track contacts and full declared occupied reservation.
2. Native actual car: two supplied Chain launches, ownership/lease/ramp/phase
   bounds, open-restraint denial, power/controller/track interruption, safe stop,
   load hold and explicit recovery. No fake velocity as launch evidence.
3. Early real occupied client: normal mount/close/dispatch, first launch and
   stop, prone torso/head/camera and native identity. Reuse this through final
   acceptance; do not repeat it for packaging alone.
4. Finish normal construction, complete two-launch occupied circuit, final
   dock/unload, distinct interrupted launch and cold process restart, original
   item/guide/example, exact evidence and clean owned-runtime shutdown.

No all-family replay. Prior 1.3 bodies and all unaffected accepted mechanics
remain closed. Source feasibility is not marked as runtime acceptance.
