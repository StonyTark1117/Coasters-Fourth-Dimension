# Roadmap checkpoint history through 2026-11-04

Archived during the roadmap reconciliation at source `46b9633`. The text below
preserves the previous chronological summaries and their artifact-specific
limits; statements such as “current”, “next” and “remains open” describe those
checkpoints, not today’s task status. Use [ROADMAP.md](../ROADMAP.md) and the
[current qualification record](CURRENT-QUALIFICATION.md) for active work.
No historical run is promoted to a newer artifact by this archive.

**Current stage: completing 0.4.** The five-family bounded 0.3 gate is complete.
Integrations, crafting and tutorials are implemented with partial acceptance;
complete clearance/render coverage, physical recovery and real-client survival
construction/operation remain 0.4 work. Lifecycle investigation has also begun
under FD902 toward 0.9.0; this does not close the earlier 0.4 gate.

**Current work packages: FD401–FD403, with FD902–FD904 evidence advancing in parallel.** FD402 recovery towing remains the earliest substantial 0.4 acceptance gap. Dev.29 records actual native
chassis, rider and non-seat carrier-rack poses every game tick, replacing the
previous ten-tick cadence. The five single-car families and two occupied
original train routes pass fresh one-tick audits with 5,763–6,974 samples per
route, at least 0.04394 blocks of body/track clearance, 0.08 blocks of
rider/track clearance and a 0.0625-block rider/rack lower bound on a subset of
sampled riders; see the
[dense-clearance checkpoint](evidence/0.4/recovery/dense-clearance/acceptance.md)
and [coverage correction](evidence/0.4/recovery/dense-clearance/rider-rack-coverage-correction.md).
The later [sampled player/rack checkpoint](evidence/0.4/recovery/player-rack-clearance/acceptance.md)
adds a part-level vanilla seated-player envelope, corrects family-specific axle
placement and passes the same seven routes with at least 0.06532 blocks of
player/rack separation. This is still sampled default-model evidence. Dev.29 separately
audits the exported chassis wing-strut/carrier-axle linkage blocks across these
same routes; unmodeled linkage, equipped/cosmetic player meshes and observed client rendering remain open. See the [linkage-clearance checkpoint](evidence/0.4/recovery/linkage-clearance/acceptance.md). A bounded
[native substep checkpoint](evidence/0.4/recovery/native-physics-substeps/acceptance.md)
pairs all 13,095 Rapier half/full steps inside the occupied two-car Arrow
route's 6,548-tick sampled interval and retains at least 0.03598 blocks of
body/wheel clearance. The [PAX checkpoint](evidence/0.4/recovery/pax-native-physics-substeps/acceptance.md)
and [remaining-family checkpoint](evidence/0.4/recovery/remaining-family-native-physics-substeps/acceptance.md)
extend this paired-step gate to all five supported families. Unanchored
startup/teardown steps and the trajectory inside one discrete substep remain
open. Dev.22 moves Original visual
supports out of the native lower-bearing channel and independently samples that
native lower-bearing model against occupied banked recovery routes; see the
[bearing-clearance checkpoint](evidence/0.4/recovery/bearing-clearance/acceptance.md).
Dev.24 adds sampled mounted native-rider envelopes at those frames; see the
[rider-clearance checkpoint](evidence/0.4/recovery/rider-clearance/acceptance.md).
This bounded Pig-passenger check does not certify player meshes, intentional
seat contact or client rendering. Dev.23 adds sampled full-spin native-wheel envelopes at those banked frames,
excluding only intended running-rail contact; see the
[wheel-clearance checkpoint](evidence/0.4/recovery/wheel-clearance/acceptance.md).
Dev.21 records actual native body poses and block states for
independent sampled default model/track/neighbor clearance; see the
[model clearance checkpoint](evidence/0.4/recovery/model-clearance/acceptance.md).
Dev.20 validates original occupied
two-car Arrow and four-car PAX trains across the complete bend, bank and level
runout before normal braking, power cut, disconnection and unloading. All 24
original native seat mounts, carriers and train links survive. Four-second queued
native assembly supplies the new operator train demos. See the
[banked train checkpoint](evidence/0.4/recovery/banked-trains/acceptance.md).
The next FD402 work is observed rendered operator/lifecycle acceptance and
broader lifecycle evidence. Dev.19 validates occupied single-car
native towing across all five families through a quarter bend, a 32-block
sustained 30-degree bank, a bank-to-level transition and level runout. Original
seat mounts and nonzero Arrow profiles survive normal braking, power cut,
disconnection and unloading; native body anchor/frame offsets are measured.
See the [banked recovery checkpoint](evidence/0.4/recovery/banked-tow/acceptance.md).
Observed linked-train rendering remains open. Dev.18 records a failed whole-train
compact PAX spike pull: the powered bare bearing stalls while an unlinked control
ascends. A new production guard brakes a driven stall and retains the original
occupied train; [evidence and limits](evidence/0.4/recovery/chain-stall/acceptance.md)
keep whole-spike towing unverified. Dev.17 connects occupied physical
PAX winch handover to native-chain towing, braking, stationary disconnection and
unloading in one test, preserving the original sixteen seat mounts and four-car
train. See the [checkpoint evidence](evidence/0.4/recovery/occupied-handover/acceptance.md).
FD401 and FD403 also remain in progress. The next release gate is FD404 (0.4);
the subsequent candidate gate is FD905 (0.9).


## Version 0.4 integration and complete content

### FD401 Deliver optional provider infrastructure

**Status: in progress.** Exact-version discovery, client-only catalog access, component ownership, and guarded appearance editing/persistence/rendering are implemented. The single-spine Arrow appearance has resource-reload and real provider-removal/reinstallation evidence without changing its controlled profile. Both inspected Arrow styles are restricted to controlled Arrow track. Exact-version Engineered adapters now have native evidence for four-family straight launch/STOP/capture/retention, ordinary-bearing operation, a loaded coupled Arrow full turn/STOP/rollback, and full PAX cycles with opposing motors. A measured upper-stroke tie conflict has guarded selection and Original fallback without profile changes. Packaged dev.4 now has double-spine GUI fallback/cold-reopen/restoration/reload evidence, a flat single-spine quarter curve, and an occupied styled circuit with station dispatch/capture and verified final dismount. An isolated dev.5 native FE batch now covers empty-buffer starvation, supplied launch, sampled braking regeneration, PAX ownership and supplied retention without FE consumption. A native banked circuit now checks joint continuity, loaded full turns, capture and unloading; an independent default-mesh audit covers its decoration/control-rail gap over the full supported stroke. Packaged dev.6 banked GUI/reload/reopen/occupied-circuit/unloading now has recorded evidence. The dev.7 PAX regression uses current readiness after cycle completion and verifies post-capture native-motion rejection/recovery. Dev.8 revises installed decoration placement after a native lower-wheel envelope conflict, retains native running-rail supports and adds an independent continuous-spin/longitudinal-travel wheel/support check on level straight track. The new packaged geometry rendered, but opening chat after F3+T exposed a freed bitmap-glyph image crash; that checkpoint did not diagnose its cause. Dev.12 records 64 completed resource/chat/query cycles, then a normal occupied revised double-spine level circuit, recapture, unloading and clean save/exit; this bounded replay does not establish a font fix or complete rendering coverage. Dev.12 fixes startup ordering and empty-schedule completion in the pinned Veil client scheduler, with an unmodified binary control and packaged native-scheduler validation. The original freed-bitmap crash remained unresolved at that checkpoint; scheduler completion was not a font fix. Dev.30 adds fresh packaged combined-profile Xvfb runs with 64 sequential opt-in reloads and normal client shutdown, including a world-loaded command-chat screen held for twenty ticks: all 65 FontManager apply starts/ends and 55-entry Track Styles catalog bakes complete without a closed-set lookup, unallocated-image trace or target bitmap error; see the [64-cycle record](evidence/0.4/font-reload-64-clean.txt) and [world-chat record](evidence/0.4/font-reload-64-world-chat-clean.txt). This is controlled lifecycle evidence. A later [current-artifact interactive replay](evidence/0.4/font-interactive-current/acceptance.md) completes three separate F3+T reloads, exercises chat, inventory and pause-menu rendering after each, records a stale default FontSet redirect, then saves and exits normally. A new [isolated FANCY ride](evidence/0.9/fd401-fancy-styles-reload-ride/README.md) performs F3+T before a normally boarded styled circuit, capture and unload; [separate mode runs](evidence/0.9/fd401-styles-render-modes/README.md) add guarded FAST, FABULOUS and Iris/Solas rendering with clean exits. A [banked clearance extension](evidence/0.9/fd401-banked-player-clearance-a5df9b8/README.md) now covers both uniform Styles configurations at every recorded game tick with single/coupled wheels, chassis, carriers, linkage, neighboring cars and conservative vanilla player skins. Exact-candidate review, tracing-disabled/repeated reliability claims, between-tick swept/equipped-player clearance, visible transition ownership and dynamic telemetry remain pending. See [integration scope and evidence](INTEGRATIONS.md).

**Depends on:** FD005, FD305. **Design:** D04, D05, D07, D15.

The [final `2c5d9a7` candidate identity](evidence/0.9/fd401-final-candidate-2c5d9a7/README.md)
now fixes the source, JAR and 111-file combined validation classpath. Its
artifact map distinguishes exact whole-JAR checkpoints from scoped class parity
and non-equivalent predecessors. References below to exact-candidate review now
mean the remaining runtime/operator acceptance, not candidate selection.

The later [exact-source interactive font replay](evidence/0.9/fd401-final-source-retest-1a91c94/README.md)
tests `1a91c94` with three real F3+T reloads, a stale-default-font redirect,
post-reload chat/inventory/pause menu and exit 0. Source `05ad4a6` additionally
uses Minecraft's separate missing-font fallback for a removed custom ID; its
runtime branch acceptance remains pending. Recovery runs without diagnostic
flags, but the retained replay enabled tracing. The later isolated styled ride
and mode records cover a bounded occupied post-reload circuit and declared graphics
modes. The selected candidate's artifact map retains those older runs only at
their proven scope; candidate-exact runtime operation, tracing-disabled operation
and repeated reliability remain open and FD401 is not closed by those bounded runs.

The frozen `ff7c831` [uniform Styles geometry reports](evidence/0.9/fd401-prospective-styles-ff7c831/README.md)
now pass both single- and double-spine configurations for single-car and two-car
Arrow recovery routes. A fifth [whole-section transition report](evidence/0.9/fd401-styles-transitions-ff7c831/README.md)
passes Original → single → double → Original → single. Single-car cases cover
5,768 poses; train cases cover 6,548, with all five sections visited and minimum
bounds of 0.04891048 / 0.04878235 blocks against 0.02 required. All five reports
have no uncertified body/track pairs and retain exact source/input/tool hashes.
The later [banked player-clearance extension](evidence/0.9/fd401-banked-player-clearance-a5df9b8/README.md)
adds a conservative vanilla 1.21.1 seated skin envelope to every occupied fixture
seat for both uniform styles: 23,072 player poses per single-car report and 52,384
per coupled report remain at least 0.08 blocks from the styled track. Its fifth
report applies the same 52,384 player checks to the retained whole-section
Original/single/double transition plan. This resolves
the bounded one-tick sampled default-model/player-skin geometry work only.
Continuous between-tick clearance, equipped/cosmetic players, decoration
self-clearance, visible transition ownership, within-curve UI styling,
candidate-exact runtime acceptance and dynamic telemetry remain open. Neither these prospective results nor
the later draw-fallback source correction closes FD401.

Implement isolated optional adapters, support/version checks, component render ownership, appearance fallback and shared server-validated operations telemetry. The current 0.4–1.0 scope is Create guidance/diagnostics plus existing Track Styles 1.2.0 appearance and Engineered 1.1.1 operations. Keep the current schema 6/protocol 7 baseline; the paused schema-7/protocol-8 provider registry requires a separate migration decision and is not a 0.9 prerequisite. Newly staged Additions, CTNB, Magnetized, Skin, Immersion, Waterparked and Create Coasters adapters remain in the proposed post-1.0 expansion plan, not prerequisites for FD401.

**Done when:** standalone behavior is unchanged by missing optional mods; supported integrations preserve functional family and profiles. Publish only tested combinations. Future Arrow 4D support remains FD801 and cannot block this package.

### FD402 Evaluate and document recovery towing

**Status: in progress.** Dev.15 implements guarded production selection/coupling/start/brake/disconnect and stationary PAX winch handover. Native tests tow fully occupied cars from all five actual flat examples using Coasters chains/Create motors; forty actual wrong-family initial snaps are rejected. They preserve matching rail support after broken links, handle permanent bearing destruction, persist recovery identity and load schema-5 state. Actual native holding restores all five recovery groups and 32 passengers with unchanged component IDs/couplings; towing stays braked until explicit resume. A separate check hands over a stopped physical PAX carriage while preserving its four-car train. Fully occupied two-car Arrow and four-car PAX trains also tow, brake and disconnect while preserving original links and retention. A packaged empty Arrow check also completes real tool selection/coupling/motor controls, same-ID braked fresh-JVM restoration, explicit start, guarded route-end stop, brake/power stop, disconnect and restraint opening. Both clients exit normally with validation flags false; its saved identities/bindings/profiles persist. The brake action follows the automatic route stop. Dev.16 adds recovery-specific action guidance, live fault status with wrapped hover text, disabled incompatible ride controls and a lever-controlled native Create clutch for demo power. Standalone and combined checks validate native lever start/stop for all five occupied examples with motors retained at eight RPM. The final packaged Arrow example has independent manual braking before the boundary, normal lever power stop, fresh-JVM same-ID coupling restoration, inspection/resume, disconnect and restraint opening; [dev.16 evidence](evidence/0.4/recovery/power-control/acceptance.md) records the surviving startup movement warnings and bounded scope; see [bounded client evidence](evidence/0.4/recovery/gui/arrow-gui-acceptance.md). Dev.17 adds an occupied end-to-end physical handover/recovery test: all sixteen native passengers retain their original seat mounts through towing and braking; chain power is cut with the motor retained at eight RPM, then the original four-car links survive disconnection and normal unloading. See the [occupied handover checkpoint](evidence/0.4/recovery/occupied-handover/acceptance.md) for native metrics and scope. Dev.18 attempts to extend the same fully occupied train onto the compact curved spike. The powered bare bearing stalls before whole-train entry while an unlinked control ascends; this is a measured propulsion limit, not a passing whole-spike case. Production now diagnoses insufficient displacement under active native chain input, brakes all original cars and retains the inspection fault. A separate regression rejects false stalls while power is cut and preserves sixteen original seat mounts through driven-stall braking, power cut, disconnection and unloading; see the [stall checkpoint](evidence/0.4/recovery/chain-stall/acceptance.md). Dev.19 adds a native quarter-bend/30-degree-bank/level-runout recovery example per family, with all 32 original native seat mounts preserved until unloading. Each car traverses all five sections under actual bare-bearing chain power, stays retained while Arrow track keeps its full-turn profile, then manually brakes, cuts power with the motor retained at eight RPM, disconnects and unloads. Both native bodies are measured against track anchor positions and orientations; independent exported-frame checks verify route continuity, sustained banking and the nonzero profile. See the [banked recovery checkpoint](evidence/0.4/recovery/banked-tow/acceptance.md). Dev.20 extends the native bend/bank test to original occupied two-car Arrow and four-car PAX trains. Every car crosses all five route sections and samples the sustained bank before the whole train reaches the level runout for normal braking, power cut, stationary recovery disconnection and native unloading. Original carriers, all 24 original native seat mounts and original train links are preserved. Both extended routes have independent exported-frame checks, and the test exercises the four-second queued native assembly used by the new operator demos; see the [banked train checkpoint](evidence/0.4/recovery/banked-trains/acceptance.md). Dev.21 adds read-only native pose/block-state traces and independent sampled default-model clearance against original track triangles and neighboring cars. Dev.29 strengthens this to every game tick and adds conservative full-spin wheel unions, live rider/rack bodies and exported linkage blocks across all five single-car examples and both original train examples; see the [one-tick clearance evidence](evidence/0.4/recovery/dense-clearance/acceptance.md). A follow-up verifier requires stable rider identities and original carrier mounts on every sampled occupied single-car and linked-train pose, with missing-seat and swapped-mount negative controls; see the [occupied rider checkpoint](evidence/0.4/recovery/occupied-rider-coverage/acceptance.md). A [current-artifact one-tick audit](evidence/0.4/recovery/current-tick-rider-clearance/acceptance.md) now passes all 50 required combined-profile GameTests and independently verifies every rider/rack pair on five single-car and two linked-train banked routes, with the generated raw traces preserved. This remains native/default-model evidence and excludes client meshes, decorations and continuous physics-step sweep. Schema/protocol 6 protects the new state; older schemas 1–5 remain readable. Complete rendered operator acceptance and client mesh/rider/rack clearance, broader strength/propulsion and recovery lifecycle evidence remain open. See [operator workflow](RECOVERY.md) and [operator acceptance evidence](evidence/0.4/recovery/operator-acceptance.md).

The later [sampled vanilla-player checkpoint](evidence/0.4/recovery/player-rack-clearance/acceptance.md)
supersedes only the earlier player-to-native-rack gap: it corrects
family-specific axle geometry and checks part-level seated-player envelopes at
the same one-tick poses. All seven routes retain at least 0.06532 blocks of
player/rack clearance and at least 0.08 blocks of player/track clearance.
The [native substep checkpoint](evidence/0.4/recovery/native-physics-substeps/acceptance.md)
extends the occupied Arrow train to all 13,095 Rapier half/full steps inside its
6,548-tick sampled interval, retaining at least 0.03598 blocks of body/wheel
clearance and 0.06532 blocks of carrier-local player/rack clearance.
The later [occupied PAX substep checkpoint](evidence/0.4/recovery/pax-native-physics-substeps/acceptance.md)
adds all 13,901 paired half/full steps across 6,951 sampled ticks of the real
four-car train, retaining at least 0.03152 blocks of body/wheel clearance and
0.06532 blocks of carrier-local player/rack clearance through normal release.
The [remaining-family substep checkpoint](evidence/0.4/recovery/remaining-family-native-physics-substeps/acceptance.md)
adds 35,163 paired half/full steps across 17,583 sampled ticks for occupied
Legacy ZacSpin, Free Spin, and Revised ZacSpin vehicles, completing paired-step
coverage for all five supported families. Equipped/cosmetic meshes, observed
client rendering, unanchored startup/teardown steps, the unexposed within-step
trajectory, and broader recovery lifecycle evidence remain open.

The [sampled vanilla wearable checkpoint](evidence/0.4/recovery/vanilla-wearable-clearance/acceptance.md)
adds exact Minecraft 1.21.1 armor, clamped cape and seated elytra alternatives
at every occupied one-tick pose on those same five single-car and two linked-train
routes. Corrected carrier axles retain at least 0.04811 blocks of wearable/rack
clearance and at least 0.08 blocks of rack/track clearance. This closes only the
bounded vanilla wearable layer item; held items and item-use poses, non-armor
head-slot renderers, modded cosmetic layers, resource overrides and observed
client rendering remain open.

The [certified rider-equipment interlock](evidence/0.4/recovery/certified-rider-equipment/acceptance.md)
now keeps moving recovery scenes inside that measured envelope. Start rejects a
mounted player with either hand occupied or equipment outside empty slots,
built-in vanilla armor and elytra. An unsafe equipment change during towing
retains and brakes the consist with a persistent clearance fault; correction
requires an explicit Start tow. This closes held and non-armor head items by
fail-closed production policy rather than claiming their geometry. Modded render
layers, resource overrides and observed client rendering remain open.

The [occupied strength-rating checkpoint](evidence/0.4/recovery/strength-rating/acceptance.md)
now makes the propulsion boundary explicit: production recovery accepts at most
four original cars. A five-car Arrow consist is rejected before partial state;
four Arrow cars with all sixteen native seats occupied tow 4.636 blocks with
0.000123-block maximum spacing error, then brake and detach without losing the
original links. Added ordinary assemblies, more than four cars, arbitrary drive
settings, steeper grades and the compact PAX spike remain unsupported rather
than implied by rail fit.

The [isolated Engineered recovery/FE checkpoint](evidence/0.4/recovery/engineered-fe/acceptance.md)
closes the bounded optional-provider power item for the certified ordinary
bearing: braked/starved holds, explicit occupied towing with FE consumption,
the recovery speed cap, rebraking, rider retention and cleanup pass. Rendered
manual Engineered controls, all-family provider coverage and stronger ordinary
assemblies remain outside that result.

The preceding mechanical checkpoint proved fifteen slow, flat native-coupler pulls across all five families: empty captured, fully occupied captured and fully occupied maintenance-retained, at family-specific 6/12/14-block spacing. It used an artificial actuator on the ordinary bearing and a temporary admission policy; both were removed before production recovery was implemented. It establishes feasibility, while the dev.15 operator tests use actual native chains and production guards. A policy rejection is not a demonstrated physical coupling limitation. See [mechanical evidence and reproduction](evidence/0.4/recovery/mechanical-acceptance.md).

**Depends on:** FD108, FD106, FD305. **Design:** D03.

Test couplers, strength, offsets, braking, retained carriers, and loaded towing with fitting ordinary carts. Validate every vehicle separately and keep disabled 4D units on matching track.

**Done when:** publish a proven recovery example if feasible, or document the demonstrated coupling limitation and supported rail-access-only workflow. Do not claim towing support merely because wheels fit. A purpose-built recovery vehicle is a separate scope decision if existing couplers cannot work.

### FD403 Complete crafting and teaching content

**Status: in progress.** Dev.9 adds localized guidance for all fifteen craftable items, vanilla recipe discovery and a five-family building/operating guide. A native check exercises all fifteen crafting outputs, missing-ingredient rejects and inventory-triggered recipe rewards. Dev.10 adds nine original Ponder scenes with 53 registrations across all fifteen items, client-only registration, generated translations/schematics and native resource checks. Packaged standalone playback/replay and a completed resource reload cover every registration; the rendered review uses normal inventory shortcuts. Dev.11 adds survival crafting through the native result slot and world right-click dispatch for all five track/car families, twenty wrong-family rejects, magnetic equipment and invalid PAX setups. It fixes ordinary stations accepting PAX and clears old invalid bindings on use; standalone and combined native checks pass. Complete survival building/operating exercises through a real client remain pending. See [survival checkpoint](evidence/0.4/survival-acceptance.md), [building guide](BUILDING.md), [teaching checkpoint](evidence/0.4/teaching-acceptance.md) and [Ponder checkpoint](evidence/0.4/ponder-acceptance.md).

Connected survival-client checkpoints additionally craft, place, board, control,
unload and save Arrow, Legacy and Free Spin on short unpowered sections. They do
not establish powered travel; the Legacy check records an endpoint fault.
A later [Revised survival-client checkpoint](evidence/0.4/survival-client-revised/acceptance.md)
adds normal crafting and matching track/car placement, clearance rejection,
default-range boarding, operator dispatch/capture/unloading, and a clean
save/reopen cycle; powered lower-hinge travel remains open. A later
[PAX construction checkpoint](evidence/0.4/survival-client-pax/acceptance.md)
records survival crafting, three valid connected route rails, four captured
cars, and a bound single-car winch. At that checkpoint, the native chain links
and occupied powered four-car cycle on the survival-built train were open. A separate
[shipped PAX demo checkpoint](evidence/0.4/survival-client-pax/demo/acceptance.md)
now records three original native links, normal mounted restraint/dispatch
controls, one complete powered four-car cycle, final capture and unloading in
the connected survival client. A separate
[manual-link demo checkpoint](evidence/0.4/survival-client-pax/manual-links/acceptance.md)
records three player-made Chain links, empty-hand winch binding and boarding,
mounted dispatch, a powered four-car recatch and normal dismount. Its route,
cars and winch were command-authored. A later
[survival-built PAX cycle](evidence/0.4/survival-client-pax/survival-cycle/acceptance.md)
now records normal crafting, five player-connected rails, four player-placed
cars, three native Chain links, winch binding, occupied powered travel,
recatch, unloading and cold reopen with a disclosed upstream motor fixture.
The [current-artifact teaching review](evidence/0.4/teaching-current/acceptance.md)
checks all fifteen rendered tooltips and all nine Ponder scenes and their
captions; packaged playback/replay passes all 53 registrations. A later
[bounded powered Arrow pass](evidence/0.4/survival-client-arrow-powered/acceptance.md)
records survival result-slot crafting, player-connected 50-block original
track, player-installed Chain, player-placed car, an occupied motor-powered
full controlled turn, pre-boundary capture, unload and same-identity cold
reopen. Its first short-route endpoint fault remains recorded as a negative
case. A connected-client complete Arrow circuit and full Legacy, Free Spin and Revised
family exercises remain open. A subsequent
[bounded powered Legacy pass](evidence/0.4/survival-client-legacy-powered/acceptance.md)
records survival result-slot crafting, player-connected 50-block original
track, player-installed Chain, an occupied motor-powered 36-block traverse,
pre-boundary capture, unloading and same-identity cold reopen. The flat route
did not produce meaningful free rotation. A later
[Legacy powered demo pass](evidence/0.4/survival-client-legacy-powered/demo/acceptance.md)
records one occupied shaped-circuit lap with 0.56- and 0.64-radian independent
carrier angles, capture, unloading and same-identity cold reopen. The demo
route and car were command-authored; a player-built powered route and its
teaching walkthrough remain open. A later
[bounded powered Free Spin pass](evidence/0.4/survival-client-free-spin-powered/acceptance.md)
records survival crafting, player-installed magnetic spans on a 50-block
original section, occupied powered travel with independent carrier response,
pre-boundary capture, unloading and same-identity cold reopen. It also fixes
long-section editor reach and duplicate live rider-seat assignment found in
negative retries. The split ride does not isolate damper strength from capture;
full family teaching and a connected-client complete Arrow circuit remain open. A later
[shipped Free Spin demo damper pass](evidence/0.4/survival-client-free-spin-powered/damper-demo/acceptance.md)
samples the same four released carriers before and after the DAMP span: the
occupied carrier records 127 actual impulses, negative angular-energy change
and positive instantaneous speed reduction before capture. It then captures,
unloads and cold reopens the same car. The command-authored example logged
three transient Sable sub-level packet errors at creation, and the second
client was terminated only after save and return to title to release memory.
The [exact merged-source base GameTest](evidence/0.4/survival-client-free-spin-powered/damper-native/acceptance.md)
then passes all 50 required tests and records 483 native damper impulses with
negative angular-energy change; its five recovery exports are preserved for
separate FD402 analysis.
The full player-built shaped route and family teaching walkthrough remain
open. A later
[Revised powered demo pass](evidence/0.4/survival-client-revised/powered-demo/acceptance.md)
records an occupied powered circuit, distinct lower-hinge carrier motion,
capture, unloading and same-identity cold reopen on the command-authored shipped
example. A survival-built powered Revised route and family teaching walkthrough
remain open.
The [exact-source native player-crafted Arrow loop](evidence/0.4/survival-client-arrow-loop-native/README.md)
now passes all 51 required base GameTests: five normal track crafts supply eight
player-connected sections at 43 actual item cost, a crafted car and station
complete an occupied powered circuit with a 6.2818-radian controlled turn, and
the car returns captured with its original rider retained. The upstream Chain
and creative motor are disclosed fixtures, and the synthetic ServerPlayer does
not replace the connected-client family walkthrough, save/reopen or teaching
acceptance.
A later [connected Arrow crafting checkpoint](evidence/0.4/survival-client-arrow-loop-connected-attempt1/README.md)
takes 80 Controlled 4D Track items from normal recipe result slots and saves
cleanly, but has no new car, player-installed Chain span or powered ride. The
Boarding Station tooltip now names its non-PAX binding limit, matching the
station and Ponder guidance. The
[FD403 connected-client acceptance procedure](FD403-ACCEPTANCE.md) lists
the remaining family checks without treating the earlier samples as release
acceptance.

**Depends on:** FD305. **Design:** D08.

Complete recipes, item descriptions, Ponder scenes, family demonstrations, diagnostic messages, and consistent terminology. Teach family matching, ordinary-cart exceptions, controlled versus free motion, and shuttle boarding.

**Done when:** each family has a usable acquisition/build path and tutorial; shipped sample layouts exercise the advertised mechanics without optional mods unless clearly marked.

### FD404 Version 0.4 gate

**Status: planned; awaiting FD401–FD403 acceptance.**

**Depends on:** FD401–FD403.

Produce the feature-complete candidate scope and list tested optional combinations, known limits, and deferred external support. Freeze base, Track Styles, Engineered and their combined profile for 0.9/1.0. Adding an expansion candidate before 1.0 requires an explicit scope revision and its full acceptance evidence; staging alone does not expand this gate.

**Done when:** the five-family standalone feature set and teaching content are complete, all advertised integrations have evidence, and remaining work is stabilization rather than missing core mechanics.

## Version 0.9 release candidate

### FD901 Run the full compatibility matrix

**Status: in progress, bounded pre-FD404 checkpoint.** Dev.27 verifies the
five-by-five original-family matrix through factory, locked eligibility and
native initial-snap paths: all five matching cases bind, while all 20
wrong-family cases reject without stealing the existing binding; see the
[family eligibility matrix](evidence/0.9/family-eligibility-matrix.md).
The same fixture requests all 20 directed mixed-family native six-block
couplings after matching capture; every recorded request rejects without a
link. The strengthened assertion preserves both cars' exact bindings, and the
current 49-test native rerun covers all 20 directed requests.
Its held-item survival dispatcher also accepts the five matching placements and
rejects the 20 wrong-family placements without consumption or vehicle creation.
It also verifies bare ordinary-bearing fit and deliberately widened-body
rejection across all five original tracks through both direct eligibility and
the native snap filter. The same run confirms ordinary traversal has no vehicle
record, carrier rotation or magnetic impulse, and rejects unassigned ordinary/4D
train coupling without mutating the 4D car; see the [ordinary snap matrix](evidence/0.9/ordinary-cart-snap-matrix.md).
The loop includes both directions for every mismatched pair, rather than only
the former Arrow-first endpoint order.
Each inspected Track Styles appearance also retains Arrow-only functional
eligibility in a direct regression. The refreshed exact-candidate native
[optional-profile matrix](evidence/0.9/optional-profile-matrix.md) passes
49 required GameTests with target-host performance limits in each of base,
Track Styles-only, Engineered-only, and combined audited profiles. Its
[archived four-profile record](evidence/0.9/profile-matrix-abc4a9a/README.md)
preserves the identical source and JAR identity, profile-specific dependency
hashes, full logs, and clean shutdown for every run. This advances the regular-cart
and audited optional-profile portions before FD404, but it does not
close the full compatibility matrix. The [advertised compatibility
scope](evidence/0.9/supported-compatibility-scope.md) records that no
third-party functional track is currently supported: Track Styles remains a
cosmetic Arrow appearance and Engineered remains an operating adapter. The
current standalone target-gated core suite passes 49 GameTests. The earlier
Engineered capacity failure was traced to concurrent GameTest fixtures; the
family matrix now runs in its own batch without changing the production
64-vehicle limit.

**Depends on:** FD404. **Design:** D02, D04, D17.

Exercise every family against matching, ordinary, wrong-family, cosmetic-only, and supported external tracks; test regular-cart fit/non-fit, mixed consists, and every entry path. For the current candidate, test Styles-only, Engineered-only and Styles+Engineered in addition to base. Later expansions repeat these gates for their own named profiles; they do not silently expand the 0.9 matrix.

**Done when:** each advertised combination has recorded results. Wrong-family 4D carts are rejected everywhere, and ordinary carts receive no 4D benefit.

### FD902 Validate multiplayer and lifecycle recovery

**Status: in progress.** The current packaged dedicated-server checkpoint covers
occupied client disconnect/fresh-client restoration, normal no-player
chunk/sublevel unload/reload, and full dedicated-server cold restart for each
stationary original Arrow, Legacy, Free Spin, Revised and PAX family. Every
case retains the one rider assignment, restores the original native carrier and
returns the player to a `create:seat` before the 200-tick deadline. The
combined audited integration profile now passes 49 GameTests, including a
server-side two-live-player regression that retains distinct seat entities and
UUID-to-seat assignments across a native update. Fresh-world real two-client
fixtures retain both riders in distinct seats of one carrier across all five
current families; Arrow, Legacy, Free Spin, Revised and PAX also restore one rider
after a fresh-client reconnect while the other remains mounted. The earlier
second-rider removal was a stale Sable tracking point in a removed sublevel.
Native support-loss now also permanently removes one carrier from each of the
five families and verifies rider recovery with no retained native or saved
vehicle state. Late-join, shuttle availability-gap behavior and occupied Arrow,
Legacy, Free Spin and Revised moving-route restarts, plus PAX active-lift
recovery, also have bounded evidence. Stationary concurrent
interruption coverage is complete. See
[occupied disconnect/reconnect evidence](evidence/0.9/occupied-disconnect-reconnect.md),
[support-loss evidence](evidence/0.9/five-family-support-loss.md),
[two-client late-join evidence](evidence/0.9/two-client-snapshot.md),
[caught shuttle acceptance and limits](evidence/0.9/caught-shuttle-acceptance.md)
and [occupied boarding acceptance](evidence/0.9/occupied-shuttle-acceptance.md).

The tall native PAX shuttle now also serializes and reinstalls its real winch
at the first captured station return, retains its physical carriage and ordered
four-car consist, and completes a second cycle with outbound restraints locked;
see the [repeated station reload evidence](evidence/0.9/repeated-shuttle-reload.md).

Dev.32 adds an opt-in packaged-client carrier transform gate. In the existing
Arrow-only `FDRenderMatrix` fixture, all six client-native carrier bodies stayed
within 0.02 radians of their synchronized snapshot angles for 40 consecutive
samples (observed maximum `7.88e-7` radians) before normal shutdown. The same
gate accepts a required family count from one through five for future all-family
worlds; this bounded one-family static result does not claim riders, motion,
clearance or a complete client matrix. See the [client transform checkpoint]
(docs/evidence/0.9/client-carrier-transform-sampling.md).

Dev.33 adds an opt-in empty-world packaged-client fixture for the five original
functional families. It creates eight vehicles, 20 carrier bodies and 35 rails,
then requires all 20 native carrier angles to match their snapshots for 40
consecutive samples. The fixture passed after 55 client ticks with a maximum
error of `0.005636176411582603` radians and normal shutdown. Sable emitted three
early "movement packet for a non-existent sub-level" errors before convergence;
this proves eventual static agreement, not clean startup ordering, riders,
motion, cameras or a complete lifecycle. See the [five-family transform
checkpoint](evidence/0.9/client-carrier-transform-five-family.md).

Dev.35 added an opt-in motion mode to the fixture and recorded one Arrow
controlled-car path. Dev.36 expanded it to the four circuit families. Dev.37
assembles the PAX train with its real shuttle winch and validates client-native
motion for every family. Dev.38 further requires all four PAX chassis, not just
one representative, to cross the one-block threshold. The packaged client first
completed 40 pre-dispatch static samples for all 20 carrier bodies, with a
maximum angle error of `0.008198643866018637` radians. Production restraint
closing and dispatch then moved every circuit chassis by at least 1.0288093276302406
blocks and all four PAX chassis by at least 1.709099938887872 blocks. The dynamic
phase deliberately measures chassis translation only because a live Sable body
and replicated carrier snapshot can be one simulation tick apart while moving.
This is rider-free controlled-car validation, not camera, interpolation,
multiplayer or rendered-clearance acceptance; see the [five-family motion
checkpoint](evidence/0.9/client-carrier-transform-five-family-motion.md).

**Latest FD902 checkpoint: two live riders across all five families.** Clean
dedicated-server worlds admitted Dev and Observer to distinct native seats in
the same carrier for Arrow, Legacy, Free Spin, Revised, and the four-chassis
PAX shuttle. Each retained two assignments and two live `create:seat` mounts
for fifteen seconds. Arrow, Legacy, Free Spin, Revised and PAX additionally retained
Observer's assignment during disconnect and restored a fresh Observer client to
its original seat before the deadline while Dev stayed mounted. This closes the
prior stale-tracking-point failure and the bounded initial concurrency matrix.
The existing five-family stationary normal unload/cold-restart and current
Arrow, Legacy, Free Spin and Revised moving-route checkpoints and the PAX active-lift checkpoint remain valid. Stationary concurrent
interruption and current five-family moving concurrency coverage are complete. The packaged connected-client support-loss fixture now runs separately for Arrow, legacy ZacSpin, Free Spin, revised ZacSpin and PAX. After native boarding, each run removes its vehicle's real matching Coasters graph connection and requires the server and client to retain the mounted rider while the replicated vehicle becomes stopped, restrained and faulted with `Matching track support lost`. A further five-family packaged checkpoint collects its static client samples, dispatches the occupied vehicle, measures movement, then removes its currently bound curve; PAX first assembles its real four-car train. Every client records movement before the retained rider reaches the replicated fail-closed state; see the [moving support-loss checkpoint](evidence/0.9/client-occupied-moving-support-loss.md). The native [physical support-recovery checkpoint](evidence/0.9/five-family-support-repair.md) now permits rebuilding an occupied original section only for a stopped, restrained, fully captured `Matching track support lost` car. It retains the prior geometry fingerprint and requires two explicit Rotation Tool actions: repair, then physical realignment. The latter invokes the native Coasters snap on that exact rail, preserves the original rider/carrier state, and permits normal circuit-family dispatch only once captured carriers have settled; PAX reaches its separate powered-winch gate. Packaged occupied clients now observe exact authored-curve restoration and the explicit repair state for Arrow, Legacy, Free Spin, Revised, and PAX, with the rider retained and no automatic restart. The PAX checkpoint requires all four chassis to move before its lead loses support. Arrow and PAX retain their 40-sample pre-dispatch static gate; the separate lifecycle-only Legacy, Free Spin, and Revised runs use the existing all-family static-transform matrix and validate repair ordering without delaying dispatch for another static window. See the [Arrow](evidence/0.9/client-occupied-arrow-support-repair.md), [PAX](evidence/0.9/client-occupied-pax-support-repair.md), and [remaining circuit-family](evidence/0.9/client-occupied-legacy-free-spin-revised-support-repair.md) support-repair checkpoints. The current packaged occupied [five-family realignment checkpoint](evidence/0.9/client-occupied-five-family-support-realignment.md) now carries each rider through moving support loss, exact curve restoration, explicit repair, and native physical reattachment; PAX covers all four linked chassis and resets their native velocities as one captured safety unit before reattachment. The current server regression now retains two independent player-seat assignments in each family throughout loss, exact repair, physical realignment, dispatch gating, and holding-store reload. The dedicated-server [five-family two-client server matrix](evidence/0.9/two-client-five-family-server-recovery.md) now carries Dev and Observer through the same moving support-loss, exact-repair, and physical-realignment sequence for every family, including all four PAX chassis. The corrected ordered client guard now confirms loss before repair before realignment for both riders across all five families; see the [ordered two-client recovery matrix](evidence/0.9/two-client-five-family-ordered-recovery.md). The occupied PAX lift now also rejects both winch Resume and Recover until exact repair and native physical realignment complete, then recaptures and relaunches its original four-car train with all sixteen riders retained; see the [PAX support-recovery checkpoint](evidence/0.9/pax-shuttle-support-recovery.md). Post-repair client reconnect or cold-server-restart coverage remains open. An exact standalone Arrow client sequence subsequently exposed a repair-to-realignment operator blocker: after normal repair, a route-boundary guard replaced the cleared support-loss fault while realignment remained required. Its [failed evidence](evidence/0.9/create-telemetry/de667f7-arrow-repair-blocker/README.md) is retained; the release branch now preserves pending realignment against that boundary guard, recovers the historical boundary-fault state and atomically stops captured native bodies in the explicit realignment action. The exact standalone native retest passes all 47 required GameTests. Packaged fresh and preserved-historical Arrow replays both reach normal reattachment and unload; the incomplete historical route subsequently reports its genuine end-of-compatible-track warning, so neither establishes dispatch through that endpoint or broader end-to-end operator acceptance. See [occupied disconnect/reconnect evidence](evidence/0.9/occupied-disconnect-reconnect.md).

`a1aeb1a` adds a native regression that converts a persisted active PAX winch phase into a durable, retained fail-closed state at the first physics tick, including a second save/reload before consumption. The [bounded active-LIFTING cold-restart record](evidence/0.9/create-telemetry/a1aeb1a-active-pax-restart/README.md) adds one real dedicated-server restart with two clients, one rider and an explicit normal resume/cycle/unload: all four chassis, eight carriers, the logical seat assignment, carriage and controller remain. It does not establish every active phase, repeated restarts, every seat, interruption races or a physical stopping-distance bound. The [PAX normal-restoration blocker](evidence/0.9/create-telemetry/311078e-pax-normal-restore-blocker/README.md) remains accurate historical evidence for pre-provenance ordinary placement. Current source now persists the authored curve for fresh original 4D rails and restores it only for stopped, restrained, fully captured matching-support recovery. A fresh native regression verifies exact PAX repair without weakening the geometry guard, and the [fresh packaged PAX path](evidence/0.9/create-telemetry/8a7ffdb-pax-normal-restoration/README.md) completes normal item restore, Repair, Realign, occupied cold restart, powered cycle and unload. That record is one fresh stopped-support case; historical migration and broader lifecycle coverage remain open. Its recovery-specific item confirmation now directs Repair followed by Realign.

**Depends on:** FD404. **Design:** D14, D16, D17.

Run two-client and late-join scenarios, disconnects, chunk unloads, server restart, support loss, cart disassembly, and repeated shuttle station passes across all families.

**Done when:** no passenger duplication, stale restraints, lost carrier state, or incorrect track rebinding remains in supported cases. Document supported recovery actions for interruptions.

### FD903 Validate save migration and provider changes

**Status: in progress.** Dev.27 expands the recovery loader regression from schema 5 to every supported legacy schema (1–5). It serializes the occupied five-family recovery fixture, removes the schema-6 recovery fields, loads each declared legacy schema and requires every chassis identity to survive with no invented recovery partner or removed-partner state. The existing invalid 4D-to-4D saved recovery reference remains rejected. A separate regression consumes the recorded 0.3 schema-3 world extract, writes schema 6, reopens it, and preserves all 24 original rail IDs, their complete distance/angle/slope rotation-profile keys, three original chassis IDs and their functional families. A packaged dedicated-server replay now seeds that exact schema-3 `FoundationData` record into a clean world, loads it, saves schema 6, reopens it, and verifies the identities, profile keys, families and absent recovery state from the on-disk result. The same historical fixture injects a third-party `arrow_4d` provider ID and requires the loader to reject it instead of inferring functional conversion from an appearance-like name. The existing real appearance lifecycle opens a saved world after Track Styles removal and again after reinstallation; the current schema-6 regression additionally preserves both known Track Styles appearance IDs and an unknown provider while the provider is absent. Cosmetic appearance loss therefore cannot change a functional rail's ID, family, variant or profile. The [world upgrade and recovery procedure](MIGRATION.md) gives backup, staged-upgrade and restore steps, while explicitly declining third-party functional-block conversion. A complete schema-3 world generated by the historical 0.2.0 packaged server now upgrades and reopens with all three chassis and eight native carriers available; the original 24 rail IDs and profile keys survive the schema-6 rewrite. One connected survival client now boards, rides, captures, unloads, and saves the upgraded historical Arrow car. Separate connected-client Legacy and Free Spin operator cycles now prove their migrated native seats, restraint controls, dispatch requests, capture, and unload; the sampled cycles do not measure sustained historical travel. Sustained Legacy and Free Spin rides, two-client migration coverage, broader historical worlds, and provider conversion remain open; see the [native-world replay](evidence/0.9/native-schema3-world-replay.md), [Legacy/Free Spin operator checkpoint](evidence/0.9/native-schema3-legacy-free-spin-operator.md), the [recovery migration checkpoint](evidence/0.9/recovery-schema-migration.md) and [appearance-provider boundary](evidence/0.9/appearance-provider-lifecycle.md).

The current provenance migration checkpoint additionally boots two untouched pre-provenance worlds on the packaged candidate. All five intact valid rails acquire byte-for-byte native curve snapshots with every other rail field unchanged; a physically changed, pre-invalid PAX support route remains invalid and without provenance. Original chassis/carrier/seat topology and the saved logical rider reservation persist. The only normalized legacy winch field is absent `shuttle_pending_retention`, which is saved as its documented default `false`; see the [historical backfill record](evidence/0.9/create-telemetry/307ec35-legacy-provenance/README.md). Direct native guards additionally refuse provenance for a valid foreign-material rail, a changed native-curve fingerprint, and a missing curve. This proves bounded persistence and refusal behavior, not historical repair, rider remount, or full FD903 acceptance.

**Depends on:** FD404. **Design:** D04, D09, D10, D16.

Test upgrades from released save formats, stable IDs, profile rebinding, appearance-provider removal, and explicit conversions involving external blocks.

**Done when:** supported upgrades preserve functional families and profiles; unsupported conversions are rejected clearly. Backups and recovery instructions accompany any destructive migration.

### FD904 Meet measured performance and rendering targets

**Status: in progress, target-host runtime gate available.** Dev.27
adds the resettable `/fd performance` runtime snapshot, which reports this
addon's server-side carrier-runtime invocation totals alongside live-carrier
update totals, per-carrier timing and constraint rebuild counts. Its scope and
repeatable sampling procedure are documented in [performance
sampling](PERFORMANCE.md). Automated sampling windows cover
controlled-train, free-spin and shuttle scenes, and two [integrated host
baselines](evidence/0.9/runtime-baseline.md) are recorded. The current
checkpoint adds a third combined run and host-scoped limits of 120,000 ns per invocation and
6,000 ns per live carrier update; `-PfdPerformanceTargets` verifies them in
all three sampled scenes. Dev.28 removes transient stream allocations from the
per-carrier rail and edge lookups while preserving first-match resolution; the
target-gated combined profile passed all 41 GameTests with 103,889 ns per
invocation and 2,488 ns per carrier in the 16-rider PAX shuttle scene. Packaged
real-client runs validate the opt-in original-track stage instrumentation
under llvmpipe and on the stated AMD
RX 7900 XTX host; [the hardware observation](evidence/0.9/client-render-sampling.md)
measures 3.16 ms average original-stage work and separately observes 21,476
Track Styles decoration pieces without making an FPS claim. A native Iris/Solas
shader-mode sample also records original-stage work, but Flywheel fell back to
`off`; shader and full-render acceptance remain pending.

The current exact-candidate base profile passes all **49** GameTests in 5.339
minutes with both timing limits enabled. Its controlled, PAX, and Free Spin
averages are 57,141 ns, 107,085 ns, and 109,581 ns per invocation,
respectively; their carrier averages are 3,206 ns, 2,921 ns, and 2,849 ns.
Track Styles, Engineered, and their combined profile also pass all 49 tests
under the same target-host limits; see the [four-profile record](evidence/0.9/profile-matrix-abc4a9a/README.md).
The preceding 2026-11-02 rerun exposed and repaired a PAX and Free Spin
invocation-time regression without changing either declared limit. The shuttle
cache validates route revisions,
topology, and every required physics chunk before reuse, retaining the existing
missing-chunk and changed-route fail-closed behavior; a new active-route branch
mutation regression verifies that the train faults and remains retained. See the
[runtime baseline](evidence/0.9/runtime-baseline.md).

A packaged Xvfb replay now also preserves a HUD-free framebuffer of the actual
`FD16Recovery` world after turning toward its recovery vehicle, native chassis
and original rail/support geometry; see the [rendered recovery
capture](evidence/0.9/rendered-client-fd16.md). Dev.31 adds an opt-in
self-terminating original-track render gate: the full optional profile reaches 503
live `FDRenderMatrix` stages with three mesh builds, 1,508 in-range rails and
13,391,040 procedural quads before normal client/server shutdown; see the
[reproducible matrix record](evidence/0.9/client-render-sampling.md). This
confirms cumulative original-track submission, not a rail and quad on each
required stage, decoration, full model/rider, shader, clearance or target-FPS
acceptance. The strengthened `geometryStages` gate now requires at least the
configured number of stages (500 by default) with both an original rail and a
quad; its packaged-client replay remains pending.


Dev.34 replays the same render gate in the fresh five-family carrier fixture.
The packaged llvmpipe client submits original-track geometry for 101 stages,
with 34 mesh builds, 3,422 in-range rail observations and 9,939,376 procedural
quads, then shuts down and saves normally. The fixture contains Arrow, Legacy,
Free Spin, Revised and PAX examples, but its fixed camera averages more than 33
of 35 rails in range per stage; the cumulative counters do not establish
nonempty submission on every stage, a full camera sweep, or a
vehicle/rider/linkage visual-clearance result. See
the [five-family fixture render checkpoint]
(docs/evidence/0.9/client-render-five-family-fixture.md).

Dev.39 adds an opt-in packaged-client vehicle section-render check to the same
five-family fixture. Before dispatch, all eight native chassis and all 20
carrier bodies have client Sable sections compiled and all 28 pass through the
vanilla chunked section-layer renderer while their synchronized carrier angles
still meet the 40-sample gate; the subsequent movement gate again observes all
four PAX chassis. This proves entry into Sable's vanilla vehicle section-render
path, not camera visibility or player/linkage mesh clearance. See the [vehicle
section-render checkpoint](evidence/0.9/client-carrier-render-compile.md).

Dev.40 adds a separate opt-in occupied single-Arrow packaged-client fixture so
that the whole five-family render matrix need not carry an actual rider. It
waits for a native carrier seat, boards the connected player through Create's
seat API, and requires the client to retain both the mounted player and the
replicated carrier-seat assignment through 40 static samples and normal
production dispatch. The Arrow chassis moved `1.0790031301446994` blocks; its
chassis and both carrier sections compiled and entered the section-layer path.
This is a one-rider Arrow checkpoint, not camera, mesh-clearance, multiplayer,
or whole-lifecycle acceptance. See the [occupied Arrow carrier checkpoint]
(docs/evidence/0.9/client-occupied-arrow-carrier.md).

**Depends on:** FD404, FD006. **Design:** D17.

Benchmark the declared controlled-train, free-spin, and shuttle scenes against recorded baselines. Profile per-carrier/constraint costs, caches, Flywheel modes, and the supported shader stack.

**Done when:** quantitative targets chosen from the baseline are documented and met on stated hardware; required render modes work; no unresolved performance regression prevents the advertised scene sizes. Record limitations rather than inventing universal FPS guarantees.

### FD905 Release candidate gate

**Status: planned; awaiting FD901–FD904 and all earlier release gates.**

**Depends on:** FD901–FD904.

Publish a candidate artifact for testing with a defect list, exact versions, reproducible scenarios, and explicit supported scope.

**Done when:** all core acceptance criteria pass and release-blocking crashes, save corruption, rider-state failures, and eligibility bypasses are resolved. Retest affected cases after fixes; regressions reopen the relevant package.
