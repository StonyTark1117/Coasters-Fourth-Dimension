# Optional integration development

FD401 is in progress. The addon remains standalone: no optional JAR is required or
bundled, and no external functional track is certified yet. FD801 owns a future
Track Styles Arrow 4D mechanical adapter; a decorative model or equal main-rail
gauge cannot satisfy that gate.

## Release scope and deferred candidates

The [integration backlog](INTEGRATION-BACKLOG.md) owns the dated candidate inventory,
release allocation and acceptance plans. The 0.4–1.0 scope covers Create guidance
and diagnostics plus the existing Engineered/Track Styles profiles. Proposed 1.1
adds Additions station operations, CTNB and Magnetized; proposed 1.2 adds Coaster
Skin, rotating-seat follow-up, Immersion, and Waterparked/Create Coasters coexistence.
These new ecosystem adapters remain paused pending user approval. Staging their
JARs does not activate a runtime profile or certify compatibility.
Use the backlog's [version refresh policy](INTEGRATION-BACKLOG.md#version-refresh-before-expansion)
before implementing an approved expansion. A newer download is a new candidate,
not an automatic replacement for an inspected or certified artifact.

The release baseline remains schema 6 / protocol 7. The shared telemetry/Create
checkpoint has been reconciled with the current support-repair and PAX guards;
its acceptance limits are listed below. The separate pending schema-7/protocol-8
functional-provider registry is deferred, requires a dedicated migration decision,
and is not a prerequisite for 0.9. FD801 remains independently gated. Existing
acceptance records below keep their original artifact-specific scope.

## Version and API boundaries

`/fd integrations` reports installed metadata without loading addon API classes.
The audited inspection profiles are Track Styles **1.2.0**
(`coasterssimulatedextratypes`) and Engineered **1.1.1** (`coastersengineered`).
"INSPECTED_RELEASE" means its released signatures were inspected, not that the
complete gameplay/graphics combination is certified. An absent or different
release leaves optional access disabled. The base stack pins remain unchanged.

The client-only Track Styles catalog reads the installed release's public
`MyTrackModels.STYLES` map and the `centerBeam()` / `tie()` component accessors,
after model baking. It refreshes after each resource reload, discards partial
results on API failure, and retains the original appearance. Neither its classes
nor client rendering classes are referenced by common discovery. The catalog
provides references to separately installed models; this addon includes no copied
Track Styles resources or code.

The rendering ownership contract gives every component one owner. A cosmetic
provider can reserve spine and ties only. Original running rails and their supports, position-driven
control geometry, and magnetic equipment remain ours. Appearance identifiers do
not participate in the functional track/vehicle contract, and cannot change
family eligibility or carrier profiles. The Rotation Tool's section editor now
opens **Track appearance**, with Original and the two inspected Arrow spine
choices, plus an optional `#RRGGBB` spine/tie color. Selection uses the same
permission, distance, revision, and occupied-section guards as profile editing.
Only separately installed Track Styles 1.2.0 enables its choices, and both choices are permitted only on controlled Arrow functional track. The server rejects selection on all four other families; renderer fallback also enforces this boundary. A saved decorative choice is intent, not a renderer guarantee: if either separately installed baked spine/tie model is missing, cannot create a cached buffer, or fails during drawing, the original spine/tie renderer remains active for that rail. A provider failure after it has queued a partial cutout can affect that one frame; the renderer balances the pose, disables the style for the current model generation and rebuilds the full Original mesh on the next frame. This addon neither copies nor substitutes the provider's assets.

Appearance intent is saved separately in the current schema 6. Earlier schema 1–4 rails
default to Original; missing provider IDs and tint are retained while the original
family geometry is shown. Reinstalling the same provider restores the selection.
Network protocol 7 requires matching client and server builds. This checkpoint
does not certify all exposed development choices: single-spine provider removal
and reinstallation, double-spine fallback/resource reload, a flat single-spine
quarter curve and a real styled Arrow circuit now have bounded evidence. A separate
[packaged banked checkpoint](evidence/0.4/bank-gui-acceptance.md) covers default styling,
reload, reopen and normal occupied operation. Complete clearance and broader
graphics coverage remain to be validated.

## Reproducible development profiles

Optional development dependencies are selected explicitly:

```sh
./gradlew runClient -PfdPackaged -PfdIntegrations=styles
./gradlew runClient -PfdPackaged -PfdIntegrations=engineered
./gradlew runClient -PfdPackaged -PfdIntegrations=styles,engineered
```

The same profiles apply to development dedicated-server and GameTest runs. Unknown
profile names fail configuration rather than resolving arbitrary dependencies.
The default profile installs neither addon. Do not treat a successful boot or
catalog lookup as evidence of correct propulsion, shader rendering or restraint
interlocks; those have separate acceptance scenarios.

Exact selected releases:

| Addon | Development coordinate | SHA-256 |
| --- | --- | --- |
| Track Styles 1.2.0 | `curse.maven:coasters-simulated-track-styles-1642719:9020601` | `42535a83307f6898b33c13ca201533bb9e337418fd7c73039c867fc80bfbcb38` |
| Engineered 1.1.1 | `curse.maven:coasters-engineered-1643631:8863559` | `eea19ed769ed573727b4e15184c229f66aff85f20667464650724e1b2d657f5d` |

The Track Styles [project page](https://www.curseforge.com/minecraft/mc-mods/coasters-simulated-track-styles)
still lists Arrow 4D as future work and describes Flywheel/shader requirements.
It labels the project MIT, while the inspected 1.2.0 JAR declares All Rights
Reserved. The independent optional catalog does not redistribute that JAR, code
or assets. No resolved license for copying or bundling its content is claimed.
The audit retains both observations in `research/dependency-audit.json`.

## Shared telemetry and Create diagnostics

Stations and winches expose immutable whole-consist snapshots for occupancy,
capacity, restraints, boarding alignment, retention, dispatch readiness, faults
and operating phase. Create goggles show these fields; Display Links offer six
sources: status, occupancy, restraints, dispatch readiness, phase and fault.
Snapshots are informational. Every capture/dispatch request checks the current
server state and controller binding; PAX dispatch continues through its bound
winch state machine.

This reconciliation uses the current dispatch validation, including each linked
car's pending physical realignment and unrepaired support fault. Winch start,
resume and recovery keep their realignment guards. Telemetry adds block-entity
update data without changing FoundationData schema 6 or network protocol 7.

Compilation and 26 existing unit tests pass. The native telemetry regression
includes all five families, linked-car repair/realignment rejection, stale
snapshots, controller range, PAX ownership, Display Link source output and
serialized block updates. [Current four-profile CI evidence](evidence/0.9/create-telemetry/README.md)
records all 47 required native tests passing on source revision `177d4f6`, including
the telemetry assertions. Each job then hit its 25-minute limit during the dense
clearance audit; this is not a complete CI pass. The earlier telemetry branch's
native results are not substituted for this run. A later
[packaged standalone checkpoint](evidence/0.9/create-telemetry/local-046ead9/README.md)
on `046ead9` adds five-family goggles, normal Arrow Display Link setup/output,
two-client Arrow occupancy/readiness/retention, late join, resource reload and
occupied cold-server restart. Two occupied PAX cycles expose every phase through
the real kinetic goggles, followed by normal unloading and a winch phase display.
Its own 47 native tests pass. Optional-profile client coverage, remaining physical
repair/lifecycle cases, complete clearance and final integrated-artifact checks
remain open; the checkpoint lists the exact bounds.

## Current font recovery checkpoint

The [exact-source interactive replay](evidence/0.9/fd401-final-source-retest-1a91c94/README.md)
records a targeted pass on `1a91c94`: three real F3+T reloads, one stale default
FontSet redirected to its current replacement, rendered chat/inventory/pause
menu and normal exit 0. This is stronger than the historical scheduler and
controlled-reload evidence below, which did not establish a font fix.

Source `05ad4a6` also publishes Minecraft's actual separate `missingFontSet`
for a removed custom font ID. The map-only fallback in `159aa6c` was insufficient
and is superseded. Current source activates recovery independently of
`fdFontValidation`; the flag controls diagnostics. The retained runtime pass
used tracing, and acceptance of the removed-ID stale-reference branch remains
pending. Do not infer it from successful normal rendering of an absent font.
Styled occupied post-reload operation, repeated reliability and graphics-mode
coverage remain open; see the [current handoff](FD401-RELEASE-HANDOFF.md).

## Remaining FD401 acceptance

The [release handoff checklist](FD401-RELEASE-HANDOFF.md) reconciles later telemetry
evidence with the exact remaining operator, optional-client, rendering and
clearance checks. It separates offline audits from scheduled runtime work and
does not promote the paused expansion addons into the candidate scope.

The frozen `ff7c831` exports now have five passing prospective sampled geometry
analyses: [both Arrow styles on single cars and two-car trains](evidence/0.9/fd401-prospective-styles-ff7c831/README.md),
plus [Original/single/double whole-section transitions](evidence/0.9/fd401-styles-transitions-ff7c831/README.md).
Each single-car case checks 5,768 one-tick poses; each train case checks 6,548.
Every car visits all five sections. Minimum separating bounds are 0.04891048
and 0.04878235 blocks respectively, above the 0.02 requirement, with no
uncertified body/track pairs. The reports include default wheel, chassis,
carrier/linkage and native-fixture rider volumes, with explicit model limits.

A [banked clearance extension](evidence/0.9/fd401-banked-player-clearance-a5df9b8/README.md)
adds a conservative vanilla 1.21.1 seated-player skin envelope at every fixture
seat for both uniform styles. It checks 23,072 player poses in each single-car
run and 52,384 in each coupled run; the player/track bound is at least 0.08
blocks against 0.02 required. It retains the wheel, chassis, carrier, explicit
linkage, native rider and neighboring-car checks. A fifth current report applies
the same player envelope to the retained Original → single → double → Original →
single whole-section plan, with the same 6,548 coupled poses and lower bounds.

These remain proposed appearance configurations on recorded native motion, not
observed client rendering. They exclude continuous between-tick motion, armor,
held items, capes/elytra/modded cosmetics, decoration self-clearance and custom
resources. Whole-section transitions do not prove UI edits inside a curve. The historical transition
report excludes player meshes; the current extension supplies that bounded
player check without turning it into runtime editor evidence. The frozen-source results do
not validate later draw-fallback behavior or close telemetry, candidate-exact
runtime acceptance, FD401 or the release gate. The
[selected `2c5d9a7` identity and artifact map](evidence/0.9/fd401-final-candidate-2c5d9a7/README.md)
fix the review target without promoting these offline reports into runtime
evidence.

The [exact-candidate post-edit reload ride](evidence/0.9/fd401-styles-ui-reload-ride-2c5d9a7/README.md)
adds the corresponding runtime boundary: the UI-selected banked appearance
cold-reopened on the frozen JAR, survived a real F3+T catalog rebuild, carried
an occupied car through visible banked and flat rotations, returned to station
capture, and unloaded through the normal vehicle UI. It does not convert the
sampled clearance reports into a continuous sweep or cover equipped/cosmetic
riders.

Complete style geometry and clearance checks and record supported styles/render
modes and styled ride behavior. Resource reload and a real removal/reinstallation
cycle now pass for one single-spine controlled Arrow section. Engineered operating
spans and a native coupled Arrow full-turn/STOP/powered-rollback fixture now have
the bounded results below. The packaged styled Arrow circuit covers real-player
restraint actions and rear station input dispatch/capture. Native FE-enabled
starvation/supply/braking/retention now passes in an isolated batch. A
[network-powered Arrow loop](evidence/0.9/fd401-engineered-network-loop.md)
adds a connected-client occupied full-circuit return, a live wired FE buffer
dip, Engineered STOP at the station area, and editor-operated capture and
unloading. A later [bare-loop item-placement check](evidence/0.9/fd401-bare-item-placement/README.md)
places an Arrow car by normal client air-use on open track and binds the station
by empty-hand click. Later normal operator records use that car, physical motor,
controller and noncreative water-wheel supply. The [exact-candidate Engineered
replay](evidence/0.9/fd401-engineered-exact-fe-2c5d9a7/README.md) cold-opens the
persisted wired setup on `2c5d9a7`, consumes and replenishes FE through an
occupied STOP/capture/unload cycle, and exits cleanly. It uses validation
boarding and redstone commands; normal construction and source
interruption/restoration remain predecessor observations. The [bounded connected-client operator
check](evidence/0.9/fd401-engineered-client-operator.md) records short occupied
FE-consuming motion and safe unloading at the endpoint guard. A separate
[real FE-network check](evidence/0.9/fd401-engineered-fe-network.md) uses a
validation-only Crafts & Additions generator and wire to fill the motor anchor
across a server restart. The native banked circuit has packaged GUI evidence.
Publish supported combinations only after the runtime matrix; inspection profiles alone are not
advertised gameplay support.

The [partial integration evidence](evidence/0.4/README.md) retains the earlier
catalog-only checkpoint and the newer appearance transaction, model-frame and
provider-lifecycle checks. Neither checkpoint closes FD401 or the 0.4 gate.

## Released Arrow model finding

The 1.2.0 public catalog initialization includes `arrow_4d` and
`arrow_4d_dbl_spine`, and the JAR contains their spine, tie and rail resources,
despite the page's future-feature label. Both rail models include extra rails at
fixed offsets. This changes the appearance investigation: the released models
are available to inspect. It does not prove the functional profile interface or
variable control-rail movement needed by FD801.

The independent adapter consumes decorative components only and retains our
profile-driven control rails. Native model origins differ: the inspected spines
are centered on X/Z, while tie bounds include a local X offset. They require
separate transforms, not a shared guessed translation. Raw extrema and the
source JAR hash are recorded in `research/track-styles-1.2.0-model-bounds.json`;
reproduce them with `python scripts/inspect-track-style-models.py <installed-jar>`.
These aggregate measurements are not copied models and are not a clearance
certificate. Separate transforms center the spine and ties; a positive-handed
model frame preserves textured-model winding even though the native travel basis
has different handedness. Resource baking invalidates meshes by catalog
generation. Original running/control rails and mechanical supports remain ours;
external rail models are never rendered. The packaged flat quarter curve now has
exterior alignment and occupied circuit evidence. The banked circuit has a
separate packaged checkpoint plus bounded one-tick vehicle/player-skin clearance;
between-tick sweep, equipped/cosmetic riders and candidate-exact runtime review
remain before broad style compatibility is advertised.

## Engineered operating adapter checkpoint

The independent optional adapters activate only for the audited Engineered 1.1.1
release. They reference optional classes through target names rather than common
class imports. Missing or uninspected releases leave the adapters inactive.

Engineered's native assembly membership follows riveted bodies. Our rotating
carriers use rotary joints, so the adapter appends this cart's live carrier bodies
without duplicating native members or including other cars in the train. Its
native motor therefore receives the complete per-cart mass; its native force
application remains responsible for propulsion. Real loaded tests check that
each passenger's native mass appears once.

A direct motor-entry guard retains captured and maintenance-held vehicles, and
preserves exclusive PAX winch/gravity-return ownership. Engineered STOP brakes
translation; the operator must still request carrier capture and wait for ready
alignment before releasing restraints. A longitudinal stop never automatically
unloads or aligns rotating seats. An ordinary bearing towing a Fourth Dimension
group follows the recovery brake/fault policy; unrelated ordinary bearings retain
their upstream behavior.

The [consist interlock regression](evidence/integrations/2026-10-02/engineered-interlock/README.md)
adds callback-time checks for open restraints, faults and held linked cars, and
rejects missing peer registrations. It preserves the ordinary recovery-cart
policy and verifies native queued impulses with valid-launch controls.

The [shared availability guard replay](evidence/0.9/engineered-availability/passed-ec731db/README.md)
also checks native bodies at callback time for Engineered motors and native chain
drive. Every car in a 4D consist must have a live chassis and carriers. An ordinary
recovery source must still be live and contained, with every attached car's native
components available, its native coupling intact and no removed-partner marker.
Healthy maintenance-retained towing remains permitted. The preserved
[failing baseline](evidence/0.9/engineered-availability/native-chain-baseline-0fc8a76/README.md)
and unchanged-test replay measure thirteen chain callbacks and three Engineered
ordinary-bearing callbacks that now hold after component unload, while positive
controls still propel. This is isolated-source direct-callback evidence, not
natural chunk-lifecycle, automatic relink or merged-release acceptance; the
removed-partner and detached-link negative cases were not separately injected.

The [operating evidence](evidence/0.4/operating-acceptance.md) covers installed
controllers on all five matching straight track families: four non-PAX cars
launch and stop, captured/retained cars hold, and the PAX car rejects motor
propulsion. A fitting ordinary bearing launches and stops with no 4D state.
Existing sixteen-rider and repeated tall PAX cycle fixtures now contain opposing
motor spans and direct installed-motor call probes through powered lift, release,
gravity return and recapture. These tests use real native passengers and physics,
with default FE requirements disabled. They do not certify every controller
sequence, resource configuration, every powered layout or GUI ride,
redstone operator workflow, unload/reload of motor spans or rendering mode.

CI exercises standalone, Styles-only, Engineered-only and combined profiles.
Without Engineered, its dedicated operating tests explicitly report that they
were not exercised; passing availability checks are not motor-operation evidence.

## Decorative stroke limit and powered controlled-train checkpoint

The original dev.3–dev.7 placement of the pinned default Arrow ties clears the local straight control cylinders by at
least 0.0725 blocks throughout -1440°..+720°. Their upper outer tips intersect
those cylinders within +1080°..+1440°. The reproducible continuous-stroke triangle
projection audit and exact artifact hash are in
`research/track-styles-1.2.0-control-stroke.json`; run
`python scripts/audit-arrow-style-stroke.py <installed-jar>`. It prints aggregate
measurements and never extracts or copies models. This establishes the local
straight stroke boundary, not curved/banked, rider or resource-pack clearance.

Both decorative choices therefore require profile keys at or below +720°.
Original geometry continues to support the full -1440°..+1440° contract. A profile
edit can exceed the decorative limit without clamping angles or deleting saved
appearance intent; the renderer uses Original until the stroke is supported
again. The appearance editor reports this fallback. The server rejects a new
external selection at an unsupported stroke. Monotone profile interpolation
keeps the complete section within its key extrema.

The newer [powered-train evidence](evidence/0.4/powered-train-acceptance.md)
records two loaded six-block-coupled Arrow cars driven solely by installed
Engineered motors through full unwrapped turns at their own axles. Actual STOP
holds the full-turn plateau, reverse LAUNCH retraces the program, and normal
capture/restraint release unloads both native passengers. It adds real native
coupled/non-flat operating coverage to the earlier flat single-car fixture;
it does not establish an external functional track provider. The separate dev.4
[styled GUI checkpoint](evidence/0.4/styled-acceptance.md) used native chains,
with bounded appearance/ride evidence and its own exact artifact hash.

## Native FE-enabled checkpoint

The dev.5 [FE acceptance](evidence/0.4/fe-acceptance.md) adds an isolated batch with
Engineered's energy requirement enabled and real NeoForge anchor capabilities.
Empty buffers prevent propulsion; supplied buffers launch four loaded non-PAX
families and a fitting ordinary bearing. Native STOP regenerates energy while
braking and may spend some of it on subsequent positional holding. Capture,
unloading and supplied maintenance retention pass without energy consumption;
PAX retains exclusive winch ownership and spends no motor FE.

The batch restores the previous configuration before the existing default-energy
scenarios run. Missing or uninspected versions explicitly skip FE operation.
This does not certify a generator/cable integration, controller GUI sequence,
FE-network reload or every powered layout. The earlier operating and GUI
checkpoints retain their distinct artifacts and bounded scopes.

The later [Engineered block-entity reload checkpoint](evidence/0.4/engineered-fe-reload.md)
round-trips one installed motor anchor and controller with a supplied FE buffer,
then verifies retained rider state, controller linkage and steps, post-reload
powered dispatch, STOP, capture and unloading. It advances native serialization
coverage; an actual cold server restart, complete chunk unload and external
generator/cable network remain open.

The later [exact-candidate wired replay](evidence/0.9/fd401-engineered-exact-fe-2c5d9a7/README.md)
cold-opens the saved physical Crafts & Additions wire twice on the frozen
`2c5d9a7` artifact. With Engineered energy required, transfer raises the anchor
from 84,130 to 90,026 FE, occupied propulsion reduces it to 63,482, and the
persisted wire replenishes it to 72,396 at STOP before capture and unload. This
closes that saved Arrow candidate-replay boundary, not normal network construction,
interruption/restoration, other vehicle families or a new advertised dependency.


The dev.6 [native bank acceptance](evidence/0.4/bank-acceptance.md) adds a
continuous eight-section circuit with native 30° banking, actual loaded turns
on both banked and level straights, and station capture/unloading. Shared joint
normals and lifted centers are resolved before installing incident curves.
Banked chain motors use the high side and the corresponding rotation sign.
The native test exports only our frame positions and axes. An independent
stdlib audit reads separately installed default provider meshes in memory and
checks every placed spine/tie against all rendered control segments over the
continuous -4..+2-turn stroke, including their square cross-sections. CI repeats
this measurement for Styles and combined profiles. This certifies the recorded
circuit's decoration/control-rail gap; it excludes arbitrary layouts, resource
replacements, wheels, chassis, riders and linked-car clearance. It is not a
banked GUI or graphics-backend observation; the packaged GUI checkpoint below
is separate. Complete vehicle/rider clearance and broader rendering checks keep
FD401 and the later release gates open.

## New banked GUI and shuttle readiness observations

The [packaged dev.6 banked ride](evidence/0.4/bank-gui-acceptance.md) adds actual
editor styling, resource reload, cold reopen, occupied native motion, station
capture and verified normal unloading/save/exit on one llvmpipe/indirect rendering
configuration. The earlier first client lost its display connection; that failure
is retained and no clean-exit claim is made for it.

The [dev.7 shuttle investigation](evidence/0.4/shuttle-capture-investigation.md)
retains the dev.6 failed Engineered PR job. The sixteen-rider test now requires
current stable boarding readiness after the historical cycle count advances,
verifies deterministic native post-capture motion cannot open restraints, then
recovers and unloads normally. Production speed/alignment thresholds and
operator interlocks are unchanged. Neither checkpoint closes FD401 or FD902.

## Native-wheel decoration revision

The dev.8 adapter lowers installed Arrow spine/tie geometry by 0.6 blocks in
track-local up and scales tie width to 85% about the provider's own model origin.
Dev.22 replaces Original's former centerline crossbars and diagonal mounts with
an outboard, below-channel truss: 0.06-radius tubes at ±1.12 local right join a
local-up -0.70 underbrace. The external spine crossbrace follows the same lower
path, and end brackets still bridge transitions to Original appearance. This
avoids the native lower-bearing channel while leaving functional rails and
provider ownership unchanged. The external provider still owns only decorative
spine and ties. No external mesh or texture is copied or redistributed.

The [wheel-clearance checkpoint](evidence/0.4/wheel-placement-acceptance.md)
records why this changed: the previous control-rail audit did not include the
native main/upstop wheel meshes. Both old tie placements enter the lower-wheel
swept envelope. The new independent audit encloses every cuboid in the pinned
native wheel model over continuous spin and longitudinal travel on a level
straight. Dev.22 corrects the support-hull projection and checks the revised
truss; its bearing-specific sampled recovery evidence is separate in the
[FD402 checkpoint](evidence/0.4/recovery/bearing-clearance/acceptance.md). CI
runs this audit and the banked decoration/control-rail audit for both
installed-style profiles.

Historical dev.6/dev.7 bank geometry results are reproduced with
`audit-arrow-style-geometry.py <installed-jar> <recorded-frames> --placement legacy`.
The default audit now reads the dev.8 production placement constants. The +720°
styling guard remains pending wider profile and whole-vehicle validation; the
old tip-conflict measurement describes the old placement. Banked wheel sweeps,
chassis, carriers, riders, linked cars and resource overrides remain separate
unfinished checks. This revision does not close FD401 or the 0.9 gate.

The final dev.8 packaged client shows both styles and the end transition, but
opening chat after F3+T exposed a freed bitmap-glyph image crash. The complete
report is retained in the checkpoint above. Root cause and the new placement's
complete post-reload GUI ride remain open; previous dev.6 acceptance does not
certify this revised adapter.

## Veil scheduler and revised styled replay (dev.12)

The [scheduler checkpoint](evidence/0.4/scheduler-acceptance.md) records a client-only initialization fix for pinned Veil 4.1.4, 512 native schedule checks, 64 completed reload/chat/query cycles and a normal occupied revised double-spine level circuit with recapture, dismount and clean exit. It preserves profiles and saved mechanical identities. The original freed-bitmap crash remained unresolved at that scheduler checkpoint; complete banked vehicle/rider clearance, render modes and the Engineered operator/FE-network workflow retain FD401.
