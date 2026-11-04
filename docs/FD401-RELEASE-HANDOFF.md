# FD401 evidence handoff for the 0.9 candidate

Initial read-only audit of release `783d4ba`, updated through `ff7c831` on
2026-11-03 for the corrected missing-font source and retest. This checklist requests evidence
using existing behavior; it authorizes no runtime, production edit or new addon.
The coordinator owns runtime scheduling and implementation. Keep base, Styles
1.2.0, Engineered 1.1.1 and their combined profile as the declared candidate set.
FD801 and the [expansion backlog](INTEGRATION-BACKLOG.md) remain separate.

## Evidence already available

- [Historical occupied-reload fixture](evidence/0.9/fd401-occupied-reload-fixture-20261103/README.md)
  preserves its earlier uncommitted-source client JAR and raw logs. The programmatic
  reload completed before automatic fixture dispatch; the gate records 493 mounted
  ticks, 890 global geometry stages and 1.082 blocks of chassis motion, with zero
  Styles decoration pieces. Normal shutdown is recorded. These are not 493 ticks
  of moving ride or evidence for the later `1a91416` through-reload identity gate.
  [Independent review](evidence/0.9/occupied-reload-historical-review-2026-11-03.json)
  verifies tracked payload hashes and preserves source/exit provenance limits.
  Its separate combined-profile 50-test development run does not replace the
  four-profile matrix or manual operator/render acceptance.
- [Isolated native capture](evidence/0.9/fd402-isolated-base-ff7c831/README.md):
  base-profile source `ff7c831`, 50 required tests passed, normal server shutdown,
  exact same-route decoration transforms and matching one-tick body traces.
  This run did not enable target-host performance limits and is not the optional
  four-profile matrix or a rendered-client result.
- [Prospective Styles recovery geometry](evidence/0.9/fd401-prospective-styles-ff7c831/README.md):
  both single-car Arrow styles pass 5,768 sampled poses across five sections with
  minimum separating bound 0.04891048 blocks against 0.02 required and no
  uncertified body/track pairs. Both two-car Arrow configurations also pass 6,548 poses with a minimum
  bound of 0.04878235 blocks against the same requirement. These
  default-model proposals are not observed Styles rendering, player meshes,
  track-to-decoration self-clearance, continuous sweeps or full-turn operation.
- [Prospective whole-section appearance transitions](evidence/0.9/fd401-styles-transitions-ff7c831/README.md)
  also pass 6,548 two-car Arrow poses with minimum bound 0.04878235 blocks against
  0.02 required. The plan is Original → single → double → Original → single.
  This adds sampled body clearance at those section transitions, not UI edits
  within a curve, decoration self-clearance, player meshes or visible rendering.
- [Native middle-of-section Styles edit reach](evidence/0.9/fd401-styles-midsection-reach-b2403d3/README.md):
  one Styles-only packaged GameTest selected both inspected appearances on a
  straight 32-block Arrow section from its middle, beyond both endpoint reach
  limits, and rejected a lateral offset. This checks the edit transaction and
  reach rule; it does not show an edit partway along one section or client UI.
- [Occupied banked and flat styled full turns](evidence/0.9/fd401-styles-banked-fullturn-9ab54cf/README.md):
  a Styles-only packaged native circuit assigned single spine to its 30-degree
  banked full turn and double spine to its flat full turn. A mounted rider stayed
  aboard through both; peak carrier angles were 6.2719 and 6.2811 radians,
  followed by station capture and unload. The style assignment was a fixture
  and the remote circuit was not in the client's view during the ride.
- [Styles renderer class parity](evidence/0.9/fd401-styles-renderer-parity-9ab54cf/README.md):
  all 18 selected renderer, catalog, ownership and placement class files are
  byte-identical between the retained earlier FAST/FABULOUS/Solas JAR and the
  current full-turn JAR. This maps the earlier stationary visible Styles record
  to the current renderer code without claiming a current rendered full turn.
- [Current-JAR stationary Styles rendering](evidence/0.9/fd401-styles-current-visible-9ab54cf/README.md):
  the same JAR as the new full-turn fixture visibly rendered the saved
  double-spine Styles circuit in a Styles-only FANCY client, with nonzero
  decoration pieces and a clean exit. The circuit was stationary and its
  appearance was selected in the earlier saved-world run.
- [Current-JAR visible banked player ride](evidence/0.9/fd401-styles-visible-bank-player-9ab54cf/README.md):
  a Styles-only FANCY client rendered the player occupying a physical seat on
  the new banked Arrow car through banked and flat full turns. The same carrier
  reached 6.27 and 6.16 radians, returned to powered station capture, and
  unloaded after restraints opened. Fixture commands selected one Styles
  candidate on each turn; the operator used the car UI to close restraints and
  dispatch. This single-car pass does not cover a coupled train, equipped
  player or component-by-component render ownership.
- [Coupled native styled full turns](evidence/0.9/fd401-styles-coupled-fullturn-a23ae2d/README.md):
  a Styles-only packaged named GameTest linked two physical Arrow cars with a
  rider in each and assigned the single- and double-spine styles to banked and
  flat full-turn sections. Both cars reached about 6.27 and 6.28 radians,
  stayed linked and occupied, then captured and unloaded. The client did not
  view this remote circuit during motion.
- [Normal UI banked appearance change](evidence/0.9/fd401-styles-banked-ui-a23ae2d/README.md):
  on the saved 30-degree banked full-turn section, the player used the rotation
  tool and appearance screen to switch Arrow spine to Arrow double spine. The
  same rail reopened at revision 2 with its 0→360→0 profile intact and the
  new choice selected. This run did not repeat the ride after the edit.
- [Visible coupled Styles full turns](evidence/0.9/fd401-styles-coupled-visible-2c5d9a7/README.md):
  the frozen candidate JAR rendered both native linked car assemblies through
  the banked and flat styled full turns in FANCY. One player occupied the near
  car; the route returned to station capture and normal unload. The second
  lap's contraption-camera frame series shows the moving train and styled
  rails. Component-by-component ownership and equipped-player clearance are
  still separate checks.
- [Styles component ownership review](evidence/0.9/fd401-styles-component-ownership-2c5d9a7/README.md):
  maps each candidate draw path to the coupled inversion frames. It establishes
  source-level exclusive ownership and visible combined running/control and
  decorative geometry on Arrow, but the retained frames cannot pixel-isolate
  every provider tie and native support. Arrow has no magnetic spans.
- [Banked vehicle and vanilla-player clearance](evidence/0.9/fd401-banked-player-clearance-a5df9b8/README.md)
  extends both uniform Styles configurations with a conservative 1.21.1 seated
  player skin envelope at every occupied fixture seat. Single-car runs check
  23,072 player poses and coupled runs check 52,384; every player/track bound is
  at least 0.08 blocks against 0.02 required. The reports retain wheel, chassis,
  carrier, linkage, native-rider and neighboring-car measurements. The fifth
  report applies the same player envelope to the retained Original → single →
  double → Original → single transition plan. This is
  one-tick sampled geometry, not a between-tick sweep, equipped/cosmetic player,
  resource replacement or observed client render.
- Draw-fallback corrections `21bfd1a` / `d5132a3` catch provider draw failures,
  restore the suppressed Original spine, and quarantine that rail's styled mesh
  until rail/model revision. The failure frame may retain partial provider output;
  the next frame uses full Original geometry. Source/helper tests do not prove a
  rendered injected-failure/reload recovery, which remains acceptance work.
- [Exact native matrix](evidence/0.9/profile-matrix-abc4a9a/README.md): 49 required
  tests pass in each profile, with exact dependency hashes and class/JAR parity.
  Engineered-dependent scenarios run only in Engineered/combined. This is native
  development evidence, not four packaged-client certifications.
- [Isolated `81ee2e6` artifact](evidence/0.9/fd401-candidate-81ee2e6/README.md):
  a committed source snapshot rebuilt offline to a byte-identical JAR as the
  combined telemetry and Styles render-mode records, with 111 resolved packaged
  classpath file hashes. It is an audit baseline; shared-output runtime identity
  and later source changes still require release review.
- [Final `2c5d9a7` candidate identity](evidence/0.9/fd401-final-candidate-2c5d9a7/README.md):
  the committed source was exported and rebuilt offline outside every shared
  output/game directory. Its retained JAR is byte-identical to the `a23ae2d`
  coupled/UI artifact and its 111-file combined validation classpath is hashed.
  The artifact map separates exact whole-JAR evidence, production/resource
  parity, scoped renderer parity, non-equivalent predecessors and sessions whose
  runtime JAR was never established.
- [Standalone telemetry](evidence/0.9/create-telemetry/local-046ead9/README.md):
  five-family empty goggles, Arrow occupancy/readiness/retention, normal Display
  Link configuration, late join, reload and occupied restart; PAX phase views.
- Later standalone records add [trailing PAX occupancy](evidence/0.9/create-telemetry/de667f7-trailing-pax/README.md),
  [power-fault display, restart and actual fault clearing](evidence/0.9/create-telemetry/de667f7-pax-power-restart/README.md),
  [empty unload/tracking re-entry and unbound station](evidence/0.9/create-telemetry/de667f7-controller-lifecycle/README.md),
  [fresh Arrow repair/realignment display ordering](evidence/0.9/create-telemetry/effa1c6-realignment-retest/README.md),
  and [PAX normal repair/restart/cycle](evidence/0.9/create-telemetry/8a7ffdb-pax-normal-restoration/README.md).
  These cases must not be listed as wholly untested simply because the earlier
  telemetry README retains its historical remaining-work list.
- [Loaded controller during native body gaps](evidence/0.9/fd401-controller-native-availability-2ba168d/README.md):
  a focused isolated combined packaged GameTest kept an Arrow station loaded
  while its real carrier, then chassis, was unloaded and restored. The station
  tick, all six Create source fields, goggle formatter and update tag published
  the appropriate unavailable reason before returning to available. It did not
  capture an actual remote goggle overlay or a physical sign target in the gap.
- [Visible remote-controller native unload and cold reopen](evidence/0.9/fd401-controller-live-unavailable-2ba168d/README.md):
  a fixture-bound remote Arrow station stayed loaded while the saved normal
  car's native chassis and carriers unloaded. The client's goggles displayed
  the full unavailable reason, the physical Display Link sign showed its
  width-limited prefix, both recovered when the route was force loaded and
  reverted when those tickets were removed. F3+T and a clean cold reopen kept
  the unavailable presentation. This was one integrated client, not late join.
- [Dedicated remote-controller late join and tracking re-entry](evidence/0.9/fd401-controller-dedicated-reentry-2ba168d/README.md):
  Dev held the controller loaded while Observer joined, later reconnected and
  received unavailable snapshots. Observer moved away while the original car
  reloaded; on return, goggles showed the live one-car state. Unloading the
  car again restored the unavailable goggles and sign text. All three processes
  used byte-identical packaged JARs and exited cleanly. The first Observer
  spawn over water required a platform and respawn before its snapshot capture.
- [Optional-profile visible controller availability](evidence/0.9/fd401-controller-profile-matrix-2ba168d/README.md):
  base, Styles-only and Engineered-only packaged clients each showed the same
  unavailable → BOARDING → unavailable telemetry and goggles as the saved Arrow
  car unloaded, reloaded and unloaded again. Their JARs matched the combined
  artifact byte for byte. An Engineered cold reopen exited cleanly. The base
  and Styles world copies substituted missing Engineered and Crafts & Additions
  blocks, so these records are controller/client checks, not clean-world
  optional-profile operator acceptance.
- [Combined-profile packaged Create telemetry](evidence/0.9/fd401-create-telemetry-combined/README.md):
  two clients, late observer join, normal selection of all six Display Link
  sources, occupied Arrow occupancy/restraint/dispatch/phase changes on a
  powered board, an actual route fault, and cold persistence of that fault and
  link. This single-car Arrow run does not cover trailing-car or other-family
  transitions, fault clear, or individual optional profiles; Dev's X display
  closed after disconnect, so its Gradle task failed.
- [Isolated-candidate family telemetry and trailing Arrow occupancy](evidence/0.9/fd401-candidate-family-telemetry-81ee2e6/README.md):
  both clients and the dedicated server ran the frozen `81ee2e6` combined
  output to clean exits. Legacy, Free Spin, and Revised each showed occupied
  sign/goggles output, restraint/readiness change, Boarding → Running, station
  capture, and normal unload. A client-placed second Arrow car was native-chain
  linked; boarding its real seat changed a normally linked sign and goggles
  from `0 / 8` to `1 / 8`, then normal dismount restored `0 / 8`. The earlier
  broken Arrow mount required a disclosed fixture reset before these loops.
- [Isolated-candidate PAX fault and phase telemetry](evidence/0.9/fd401-candidate-pax-fault-81ee2e6/README.md):
  an occupied four-car PAX train faulted during Lifting when Observer normally
  reduced the Create motor to 1 RPM. Restored power alone retained the fault;
  explicit normal winch resume cleared the same Ride fault sign. A second
  occupied cycle put Boarding, Lifting, Outbound, Returning, and Capturing on
  the Ride phase sign, then returned to Boarding and normal unload.
- [Candidate PAX realignment blocker](evidence/0.9/fd401-candidate-pax-realignment-blocker-81ee2e6/README.md):
  normal station-curve loss/restoration and Repair preserved the occupied
  four-car train, cleared the same Ride fault sign, and kept dispatch denied
  pending Realign. Immediately after the Realign action, native couplers
  snapped under tension; telemetry fell to `0/4 (1 car)` while Dev remained
  mounted and the winch still saved four car IDs. This is a failed acceptance
  case requiring a focused mechanics/telemetry fix, not a completed recovery.
- [Candidate PAX realignment retest](evidence/0.9/fd401-candidate-pax-realignment-2e12e9d/README.md):
  committed `2e12e9d` preserves native six-block station spacing during
  captured-train Realign and makes saved/native consist mismatches unavailable
  to winch telemetry. On its isolated combined packaged JAR, a four-car
  occupied PAX train survived normal support removal, restoration, Repair and
  Realign. Goggles stayed at `1/16 (4 cars)`, boarding aligned and dispatch
  ready after about 29 seconds without a native link snap; Dev opened restraints
  and normally unloaded to `0/16 (4 cars)`. This is a short-hold retest;
  prolonged holds are not runtime-proven.
- [Broken-consist telemetry replay](evidence/0.9/fd401-candidate-broken-consist-2e12e9d/README.md):
  the saved failed `81ee2e6` world reopened on `2e12e9d` with one native
  linked lead car and four saved winch car IDs. Goggles and the existing Ride
  fault Display Link reported “Shuttle consist changed; recover and rebind
  before operating” with controller telemetry unavailable and dispatch denied.
  Normal operator recovery from this broken state is still unproven.
- [Engineered network loop](evidence/0.9/fd401-engineered-network-loop.md): normal
  seat boarding, editor dispatch/capture/unload, wired FE delivery and a bounded
  occupied circuit return/STOP. The car was fixture-created, the generator was
  creative, the speed target was not attained, and final cold reopen was absent.
- [Bare-loop ordinary car placement](evidence/0.9/fd401-bare-item-placement/README.md):
  packaged client on `61a9884` aimed the Arrow car item at the open interior
  rail, placed it by normal right click, then bound the station by empty-hand
  click. A focused native survival item-use case consumed one car item. This
  new world had no motor, FE network or occupied ride; it does not convert the
  earlier fixture-created circuit into an ordinary-placement ride.
- [Normal Engineered loop with noncreative FE](evidence/0.9/fd401-engineered-normal-loop/README.md):
  the same item-placed car was boarded normally, driven by a client-fitted motor
  and linked controller with a three-water-wheel alternator, and captured by
  station redstone after an occupied circuit. Native state recorded 19.385 of
  20 blocks/s, FE consumption, and a starved pulse after power interruption.
  The restored low-buffer pass did not complete STOP; the saved car is safely
  unloaded away from the station. There was no FE wire or cold repeat operation.
- [Normal STOP and cold repeat](evidence/0.9/fd401-engineered-stop-cold/README.md):
  the same item-placed car, normally fitted motor, linked controller, and original
  station completed `Launch 20 → Stop at 12 → End`, held at STOP inside station
  range, was captured by station redstone, and unloaded. A cold reopen preserved
  the ride and repeated the operator cycle. Both passes remained just short of
  the 20 blocks/s target. This run used adjacent noncreative water-wheel FE.
- [Isolated-candidate Launch 20 provider pass](evidence/0.9/fd401-engineered-launch20-2e12e9d/README.md):
  the saved normal Arrow STOP world ran on the `2e12e9d` combined packaged JAR.
  A disclosed command removed its old 20-block motor-span NBT; the normal
  motor item then placed a 28-block span on the same anchor. The occupied
  operator loop returned `PassReached=19.50049/20`, `PassShort=0`, no
  starvation, programmed STOP, station redstone capture and normal unload.
  This is the provider's accepted pass, not literal 20.0 speed attainment.
- [Wired water-wheel FE and cold repeat](evidence/0.9/fd401-engineered-wired-waterwheel/README.md):
  a separated three-water-wheel alternator supplied the motor through a copper
  spool link made in the client. Reciprocal connector nodes and FE rise from zero
  survived cold reopen. Source interruption stopped transfer; restoration and
  recharge supported another occupied STOP, station capture, and unload. Exact
  runtime-JAR identity still needs a controlled freeze because concurrent builds
  shared the output directory.
- [Legacy, Free Spin and Revised operator loops](evidence/0.9/fd401-engineered-family-operator/README.md):
  validation-only bare circuits allowed normal motor fitting, matching car-item
  placement and client-linked controller programs. Each family completed an
  occupied circuit and STOP, then station redstone capture and normal unload.
  All three `Launch 8` passes reported `PassShort=0` without starvation. Their
  FE buffers were fixture-supplied, while physical FE delivery is evidenced
  separately above. A controlled artifact freeze remains necessary.
- [Occupied PAX per-car native motor callback](evidence/0.9/fd401-engineered-pax-callback-663aa12/README.md):
  an isolated combined packaged `663aa12` JAR ran one targeted GameTest with
  two native-linked PAX cars and a real rear-carrier rider. Both direct native
  `MotorDrive.apply` calls reached the guard, were cancelled, and queued zero
  impulse. The client exited cleanly. This fixture does not prove that each car
  received a motor callback during the earlier occupied four-car winch cycle.
- Styles has historical selection/fallback/removal/reinstall and banked GUI
  evidence. Later geometry changes and the [font failure](evidence/0.4/wheel-placement-acceptance.md)
  limit reuse; automated clean reloads do not erase that failure.
- The [isolated FANCY post-reload ride](evidence/0.9/fd401-fancy-styles-reload-ride/README.md)
  applies double-spine styling, performs a real F3+T reload, then normally boards,
  dispatches, completes the styled circuit, station-captures and unloads. The
  [render-mode record](evidence/0.9/fd401-styles-render-modes/README.md) adds strict
  FAST and FABULOUS mode-guard passes plus a visible Iris/Solas V3.7b run. All use
  isolated displays/game directories and clean exits. The mode-only runs are
  stationary and the Solas run records Flywheel's expected `off` fallback.
- The [final-source interactive font retest](evidence/0.9/fd401-final-source-retest-1a91c94/README.md)
  records three actual F3+T reloads on source `1a91c94`, combined profile,
  llvmpipe, with post-reload chat, inventory, pause menu and normal exit 0.
  Independent review at `f47f342` verified all 13 manifest hashes, apply generations
  1–4, one default-font redirect in generation 3 and the rendered generation-4
  pause menu. This replaces “no interactive final-source retest” with a targeted
  pass; it does not prove a post-reload occupied ride or other graphics modes.
- The [removed-custom-font retest](evidence/0.9/fd401-missing-font-retest-05ad4a6/README.md)
  runs the corrected source `05ad4a6` in the combined profile. The file-pack font
  loaded, supplied a custom-styled chat message, and disappeared after F3+T when
  its JSON was removed. Earlier styled chat rendered missing-glyph boxes; inventory,
  pause, save and title completed with client exit 0. The run did not naturally
  use the stale-removed-font `missingFallback=true` redirect, so that branch
  remains untested by runtime evidence.

## Remaining evidence, in execution order

| Priority / handoff | Concrete observation still required | Pass condition and boundary |
| --- | --- | --- |
| 2. Engineered normal operator loop | Review the completed STOP/capture/unload and cold repeat against the exact frozen candidate, including the [isolated longer-span pass](evidence/0.9/fd401-engineered-launch20-2e12e9d/README.md). | The normally placed car completed occupied circuits, a programmed STOP hold inside station range, station redstone capture, and unload in initial and cold runs. An unready station release was rejected before launch. The `2e12e9d` 28-block span run achieved the provider's `PassShort=0` at `PassReached=19.50049/20` and repeated normal capture/unload. Retuning required a disclosed span-NBT removal fixture before normal item replacement. Literal 20.0 speed attainment remains unproven. |
| 2. Physical FE workflow | Freeze and review the wired evidence on the exact candidate. | With `requireEnergy=true`, the noncreative water-wheel alternator delivered through a normal copper-spool wire, stopped on source interruption, resumed on restoration, persisted through cold reopen, and powered another occupied STOP/capture/unload. Crafts & Additions remains a disclosed validation-only dependency (`-PfdFeNetwork`), not a new advertised integration. |
| 2. Engineered family/authority coverage | Freeze/review the coupled Arrow and PAX authority evidence against an exact candidate. | Legacy, Free Spin and Revised have normal car-item placement, client-fitted motor, linked GUI controller, occupied full circuit/STOP, station redstone capture, and unload. A [normally item-placed, chain-linked two-car Arrow train](evidence/0.9/fd401-engineered-coupled-arrow/README.md) completed an occupied circuit, programmed STOP, station capture and unload. An [occupied PAX winch cycle](evidence/0.9/fd401-engineered-pax-authority/README.md) completed against a powered `Launch 20` Engineered controller, with pass zero, FE unchanged, native capture and unload. A separate [two-car occupied PAX callback fixture](evidence/0.9/fd401-engineered-pax-callback-663aa12/README.md) reached and cancelled the native motor callback on each chassis with zero queued impulse. The four-car cycle did not trace callbacks per car, and earlier shared-build JAR identity remains an evidence limit. |
| 3. Dynamic Create output | Review the [combined-profile six-source run](evidence/0.9/fd401-create-telemetry-combined/README.md), [isolated family/trailing run](evidence/0.9/fd401-candidate-family-telemetry-81ee2e6/README.md), [PAX fault/phase run](evidence/0.9/fd401-candidate-pax-fault-81ee2e6/README.md), [failed long-hold support-repair run](evidence/0.9/fd401-candidate-pax-realignment-blocker-81ee2e6/README.md), [successful short-hold retest](evidence/0.9/fd401-candidate-pax-realignment-2e12e9d/README.md), and [broken-consist telemetry replay](evidence/0.9/fd401-candidate-broken-consist-2e12e9d/README.md) together. | Arrow's six sources and the Legacy/Free Spin/Revised occupancy/control transitions are observed. Native-chain-linked trailing Arrow occupancy changed `0 / 8 → 1 / 8 → 0 / 8`. The same PAX fault source cleared after explicit power-fault resume, and the phase source received all five PAX phase strings. The new `2e12e9d` PAX retest kept all four native linked cars and the mounted rider through normal Repair/Realign and unload after a short unsupported interval. Reopening the earlier broken world on that JAR correctly marked winch telemetry unavailable and put its recovery message on goggles and the linked fault sign. Normal recovery from a broken consist and long unsupported holds remain open. |
| 3. Controller availability / optional clients | Review whether occupied unloading needs a separate release claim; complete optional-profile operator/render acceptance on clean worlds. | The [focused native gap fixture](evidence/0.9/fd401-controller-native-availability-2ba168d/README.md) proves server snapshots, all six Create source values, goggle formatting and packet data for missing carriers and chassis. The [live remote-controller run](evidence/0.9/fd401-controller-live-unavailable-2ba168d/README.md) shows actual goggles and sign switch between unavailable and available as the saved car unloads/reloads, then retain unavailable text through F3+T and cold reopen. The [dedicated late-join/reconnect run](evidence/0.9/fd401-controller-dedicated-reentry-2ba168d/README.md) adds a second client's unavailable packets and visible reconnect, available tracking re-entry, and return to unavailable. The [optional-profile client runs](evidence/0.9/fd401-controller-profile-matrix-2ba168d/README.md) show the same unavailable → BOARDING → unavailable controller behavior in base, Styles-only and Engineered-only classpaths. The command-bound remote controller remains a fixture, and base/Styles copies substituted absent optional blocks. |
| 4. Styles geometry and visible ownership | Freeze/review the new sampled banked vehicle/player reports against the exact candidate; finish style changes partway along a curve if required, and visible ownership acceptance with occupied single/coupled cars. | The five sampled reports cover both uniform styles and the retained whole-section Original/single/double plan, including wheels, chassis, carriers, explicit linkage, conservative vanilla player skins and neighboring cars. All 19 pinned source files in the transition/player report still match the current checkout at `9ab54cf`; this preserves its original input and scope, not a new runtime result. A [Styles-only edit test](evidence/0.9/fd401-styles-midsection-reach-b2403d3/README.md) proves native middle-of-section reach. The [occupied native full-turn test](evidence/0.9/fd401-styles-banked-fullturn-9ab54cf/README.md) proves both styles can accompany full rotations and capture/unload on one circuit. [Renderer class parity](evidence/0.9/fd401-styles-renderer-parity-9ab54cf/README.md) connects prior render-mode runs to current renderer bytes; the [current-JAR visible replay](evidence/0.9/fd401-styles-current-visible-9ab54cf/README.md) shows the saved styled circuit stationary. The [current-JAR visible banked ride](evidence/0.9/fd401-styles-visible-bank-player-9ab54cf/README.md) adds a physically boarded vanilla player, both full turns, and station capture/unload in FANCY. The [coupled named fixture](evidence/0.9/fd401-styles-coupled-fullturn-a23ae2d/README.md) proves both styled full turns for two linked occupied cars. The [normal UI edit](evidence/0.9/fd401-styles-banked-ui-a23ae2d/README.md) changes and reads back the banked full-turn style without altering its profile. The [visible coupled ride](evidence/0.9/fd401-styles-coupled-visible-2c5d9a7/README.md) shows the frozen JAR rendering both linked cars, an occupied near car, both styled rotations, and normal capture/unload. A continuous between-tick sweep, equipped/cosmetic players, a visible ride after the separate UI edit, and component-by-component render ownership through inversion remain open. Preserve profiles/family/eligibility through changes, unsupported stroke fallback, missing models, removal/reinstall and reopen. |
| 4. Styles rendering / reload | Freeze and review the new occupied FANCY reload ride and FAST/FABULOUS/Solas records against the chosen candidate; add tracing-disabled and repeated-reliability acceptance if they remain release claims. | The bounded runs now retain actual F3+T/catalog evidence, post-reload styled operation, capture/unload, all three strict graphics-mode markers and the pinned shader pack. Solas correctly records Flywheel `off` and is not accelerated-rendering evidence. The mode-only replays are stationary, all observations use llvmpipe, and a tracing-enabled replay does not itself prove tracing-disabled operation. |
| 5. Release evidence review | Verify payload/world hashes, source/JAR correspondence and claims against raw records; update FD401 and support matrix only for proven combinations. | All advertised acceptance satisfied or an explicit coordinator/user scope decision, never silently waived criteria. Full FD402/FD403 and FD901–FD905 remain separate gates. |

### Font recovery source boundary

Read-only inspection at `f47f342` confirms that live-set publication in
`FontReloadTrace.applyEnd` occurs before its diagnostic guard, and all four
lookup redirects in `FontSetReloadTraceMixin` run without `fdFontValidation`.
The flag controls tracing, not the recovery path. This is source evidence;
both retained interactive replays enabled tracing.

Source `05ad4a6` changes `replacementForClosedSet` to return Minecraft's actual
`missingFontSet` when a removed custom ID is absent from the new map. This fixes
the source gap found at `f47f342`; it does not itself prove the branch at runtime.
The new removed-font run shows normal fallback rendering after a resource-pack
font disappears, but no stale custom `FontSet` lookup or `missingFallback=true`
redirect occurred. Keep direct stale-reference and tracing-disabled acceptance
open before claiming general recovery. Do not relabel the observed default-font
redirect (`missingFallback=false`) as a test of the removed-font branch.

## Work that can be done while runtimes are occupied

Review committed logs/screenshots/manifests, compare source and artifact identities,
decode copies of stopped saves, and rerun existing Python geometry/verifier tools
on retained inputs. These activities cannot prove fresh UI, packet delivery,
hardware performance or normal operation. The [retained geometry audit](evidence/0.9/fd401-retained-geometry/README.md)
records one such offline result and its frame-provenance limitation.

Use the [client telemetry protocol](evidence/0.9/create-telemetry/client-acceptance.md)
for the scheduled runs. It requires two real clients and normal Create controls.
For packaged profiles use `-PfdPackaged -PfdValidation`, adding only the applicable
`-PfdIntegrations=styles`, `engineered`, or `styles,engineered`. Do not launch from
this checklist until the coordinator hands over the runtime. Existing native
performance targets and geometry margins remain unchanged.

For future native profile runs, use the [post-exit export capture helper](NATIVE-EVIDENCE-CAPTURE.md)
before another run deletes the five generated frame/trace files. Capture failures
remain evidence gaps; archiving cannot turn a failed runtime into a passing one.

### Same-route decoration inputs and remaining Styles clearance

The [historical offline sampling inventory](evidence/0.9/fd401-decoration-frame-gap.json)
found **151 missing spine and 37 missing tie frames** on each retained Arrow
recovery route. Those older records remain unchanged; interpolated frames still
cannot stand in for measured native placement inputs.

The [isolated `ff7c831` capture](evidence/0.9/fd402-isolated-base-ff7c831/README.md)
now supplies exact native spine/tie inputs on the same single-car and train routes
as their one-tick body traces. Both archived input-verifier reports pass, retaining
source/input hashes and chassis identity pairing. The observation-only exporter
preserves the original rail grid and adds native frames, renderer-oriented profile
values and exact model transforms. The export-input gap is therefore resolved for
these frozen fixtures; it is not a general vehicle-clearance pass.

The [prospective Styles mesh audit](NATIVE-EVIDENCE-CAPTURE.md#prospective-styles-recovery-geometry-audit)
includes altered control mounts, crossbraces and endpoint bridges alongside the
provider OBJ geometry. Both single-car configurations have
[bounded passing reports](evidence/0.9/fd401-prospective-styles-ff7c831/README.md);
both two-car configurations now have bounded passing reports as well. Proposed appearance is explicitly separate
from saved/observed appearance. Preserve each result, including any uncertified
pairs. Recovery towing does not substitute for a full-turn operating ride, player
meshes, continuous swept clearance, style transitions or visible render ownership.

The [post-fallback source review](evidence/0.9/post-fallback-geometry-pin-review-2026-11-03.json)
finds the normal geometry constructors unchanged by the draw-fallback correction.
That permits reviewed future audit pins, not relabeling frozen-source calculations
as post-fix runtime evidence. New source must still pass the applicable checks.

For each future run retain exact commands/configuration, normal item/GUI actions,
server/client observations, failed attempts, screenshots, complete logs, actual
process exit codes, and pre/post-restart whole-world copies/hashes. An RCON write,
fixture-created car, forced mount, direct FE fill or generated display text must
be disclosed and cannot stand in for the corresponding operator criterion.
If a required step exposes a mechanics defect, preserve it and return the narrow
reproduction to the implementation owner; this stream does not patch around it.
