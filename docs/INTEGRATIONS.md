# Optional integration development

FD401 is in progress. The addon remains standalone: no optional JAR is required or
bundled, and no external functional track is certified yet. FD801 owns a future
Track Styles Arrow 4D mechanical adapter; a decorative model or equal main-rail
gauge cannot satisfy that gate.

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
Only separately installed Track Styles 1.2.0 enables its choices, and both choices are permitted only on controlled Arrow functional track. The server rejects selection on all four other families; renderer fallback also enforces this boundary. A saved decorative choice is intent, not a renderer guarantee: if either separately installed baked spine/tie model is missing, cannot create a cached buffer, or fails during drawing, the original spine/tie renderer remains active for that rail. This addon neither copies nor substitutes the provider's assets.

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

## Remaining FD401 acceptance

Complete style geometry and clearance checks and record supported styles/render
modes and styled ride behavior. Resource reload and a real removal/reinstallation
cycle now pass for one single-spine controlled Arrow section. Engineered operating
spans and a native coupled Arrow full-turn/STOP/powered-rollback fixture now have
the bounded results below. The packaged styled Arrow circuit covers real-player
restraint actions and rear station input dispatch/capture. Native FE-enabled
starvation/supply/braking/retention now passes in an isolated batch. The complete
Engineered operator/FE-network workflow and broader rendering/vehicle clearance
still need evidence; the native banked circuit now has packaged GUI evidence. Publish supported
combinations only after the runtime matrix; inspection profiles alone are not
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
exterior alignment and occupied circuit evidence. The banked circuit now has a separate packaged checkpoint; complete clearance
still needs evidence before broad style compatibility is advertised.

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
unloads or aligns rotating seats. Ordinary bearings bypass these 4D policies.

The [consist interlock regression](evidence/integrations/2026-10-02/engineered-interlock/README.md)
adds callback-time checks for open restraints, faults and held linked cars, and
rejects missing peer registrations. It preserves the ordinary recovery-cart
policy and verifies native queued impulses with valid-launch controls.

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
Without Engineered, its three dedicated operating tests explicitly report that they
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

The [scheduler checkpoint](evidence/0.4/scheduler-acceptance.md) records a client-only initialization fix for pinned Veil 4.1.4, 512 native schedule checks, 64 completed reload/chat/query cycles and a normal occupied revised double-spine level circuit with recapture, dismount and clean exit. It preserves profiles and saved mechanical identities. The original freed-bitmap crash remains unresolved; complete banked vehicle/rider clearance, render modes and the Engineered operator/FE-network workflow retain FD401.
