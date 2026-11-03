# Integration and teaching checkpoints

These are partial FD401 and FD403 records, not the 0.4 or 0.9 release gate.

## Crafting discovery and item guidance

The [dev.9 teaching checkpoint](teaching-acceptance.md) covers all fifteen native
crafting outputs, missing-ingredient rejections, inventory-triggered recipe
discovery and packaged item guidance. All eight dev.9 CI jobs passed; the compact
[push](teaching-ci-push.json) and [PR](teaching-ci-pr.json) records identify the head.

The [dev.10 Ponder checkpoint](ponder-acceptance.md) adds nine original scenes
for all fifteen items, native schematic validation, packaged playback/replay,
resource reload and rendered review. All eight exact-head dev.10 CI jobs passed;
see the [push](ponder-ci-push.json) and [PR](ponder-ci-pr.json) records.

The [dev.11 survival checkpoint](survival-acceptance.md) covers native result-slot
crafting and world right-click placement for all five families, twenty wrong-family
rejects and the ordinary station PAX binding regression. A separate [packaged
Arrow survival-client checkpoint](survival-client-arrow.md) covers actual client
crafting, anchor/track/car placement, boarding, controls, dismount and normal
save. The [Legacy and Free Spin client checkpoint](survival-client-legacy-free-spin.md)
extends that connected exercise to those two families. The
[Revised client checkpoint](survival-client-revised/acceptance.md) adds
normal-range boarding and operator controls. The
[PAX construction checkpoint](survival-client-pax/acceptance.md) adds four
captured cars and a single-car winch binding. The separate
[shipped PAX demo client cycle](survival-client-pax/demo/acceptance.md) covers
three native links, occupied powered travel, final catch and unloading. Player
Chain coupling, occupied powered travel and unloading on a separately
[command-authored manual demo](survival-client-pax/manual-links/acceptance.md)
are also recorded. A later [survival-built PAX cycle](survival-client-pax/survival-cycle/acceptance.md)
adds normal player Chain coupling, occupied powered travel, recatch, unloading
and reopen on a normally crafted route, cars and winch with a disclosed upstream
motor fixture. The other families' powered client rides and the full teaching
gate remain unfinished.

## Resource reload investigation

The [dev.12 scheduler checkpoint](scheduler-acceptance.md) fixes initialization
ordering in the pinned Veil client scheduler and validates native completion,
including empty schedules. This is separate from the original freed-bitmap
crash; font state is unchanged.

The [font investigation](font-reload-investigation.md) distinguishes incomplete
dependency-only stress requests from three completed reloads in an observation-only
diagnostic build. No production cache change was made; the original crash remains unresolved.
Dev.12 separately records the revised double-spine level circuit through repeated
reloads and a normal occupied ride; complete rendering/clearance scope remains open.

## Native-wheel decoration revision

The [dev.8 wheel-clearance checkpoint](wheel-placement-acceptance.md) records a
native lower-wheel envelope conflict missed by the earlier control-only audit,
the revised installed-model placement, retained mechanical supports and an
independent continuous-spin/longitudinal-travel check on level straight track.
Complete banked wheel, chassis, carrier, rider and linked-car clearance remains
open. Earlier banked screenshots describe the earlier adapter placement.

## Native banked circuit checkpoint

The dev.6 [bank acceptance](bank-acceptance.md) adds a native continuous 30°
banked circuit, loaded full turns on banked and level straights, and station
capture/unloading. The independent installed-default-mesh audit covers every
spine/tie against all rendered square control segments over continuous stroke.
The newer [banked GUI acceptance](bank-gui-acceptance.md) adds actual styling, resource reload, cold reopen, occupied circuit, station capture and clean normal unload/save/exit. Full vehicle/rider clearance and other render modes remain open. Run `python3 scripts/verify-banked-styled-ride.py` to check its saved observations.

## PAX post-capture readiness regression

The [dev.7 investigation](shuttle-capture-investigation.md) preserves the failed dev.6 PR job, corrects a cycle-counter readiness assumption, and adds a bounded real native-motion rejection/recovery check before sixteen-rider unloading. Production thresholds and guards remain unchanged. Final profile results are recorded separately in `shuttle-capture-native-*.txt`.

## FE-enabled operating checkpoint

The dev.5 [FE acceptance](fe-acceptance.md) exercises installed native anchor
capabilities with energy required: empty-buffer starvation, supplied launch,
sampled regenerative braking, PAX ownership, normal unloading and powered
maintenance holds without FE loss. The isolated batch restores its original
configuration. Real-player controller/FE-network workflows remain separate.

## Packaged styled ride checkpoint

The dev.4 [styled acceptance](styled-acceptance.md) records actual editor selection
of double spine, +1080° Original fallback, cold reopen, +360° restoration,
resource reload, a flat single-spine quarter curve, and a real occupied circuit
with station recapture and verified final dismount. Run
`python scripts/verify-styled-ride.py` to check the committed save observations.
Banking now has the separate dev.6 checkpoint above; complete clearance and rendering modes remain open. Earlier sections
below describe historical artifacts and their then-pending work.

## Powered train and control-stroke checkpoint

The dev.4 [powered-train acceptance](powered-train-acceptance.md) extends the
operating evidence with a loaded coupled Arrow full turn and powered rollback,
and a reproducible default-model stroke audit. A known upper-stroke tie conflict
now has selection rejection and Original fallback while profile/intent persist.
The dev.3 evidence below remains a separate historical artifact and test count.
Neither checkpoint closes FD401.

## Operating integration checkpoint

The newer dev.3 [operating acceptance](operating-acceptance.md) records real
Engineered 1.1.1 controller/force behavior, rotary assembly mass, station/maintenance
holds, ordinary-bearing operation and full PAX cycles with opposing motor spans.
All four optional profiles have final native results; 23 unit tests pass. Both
Arrow decorative choices are now restricted to controlled Arrow functional track.
The evidence below remains historical and does not imply that dev.3 has a separate
GUI/rendering observation. FD401 remains open.

## Appearance editing and provider lifecycle checkpoint

The newer checkpoint implements independent appearance identity/color intent,
guarded editor action 5, schema/network protocol 5, and separate installed model
placement. `appearance-native.txt` records all 20 required GameTests passing;
`appearance-unit.json` records all 22 standalone unit tests passing, including
model handedness and bounded appearance parsing. The first model-frame unit run
failed because the standalone classpath lacked JOML; the explicit test dependency
fixed that failure before this checkpoint was committed.

The packaged client used the development JAR and exact hash listed in
`appearance-artifacts.sha256` as `client-observed-dev.1`. The final dev.2 build
uses the same implementation with a new version label and is listed separately;
its native/unit checks passed, but this record does not claim a separate dev.2
GUI launch. All optional JARs were separately installed, never bundled.

Normal Rotation Tool / appearance GUI actions selected Arrow single spine with
`#4A90E2` on the clear controlled straight from (-68,123,56) to (-68,123,20).
The section retains `0:0;18:360;36:0`. An earlier edit next to the parked car was
rejected by the occupied-section/approach guard. The first rendering used the
native left-handed basis and displayed inward faces; the positive-handed model
frame corrected it. Only the corrected screenshot is the rendering observation
in `appearance-arrow-straight.png`.

`appearance-resource-reloaded.png` and `appearance-client-events.txt` show a real
F3+T reload and a second successful catalog bake. Normal Save and Quit and Quit
Game completed successfully. The same world folder was then opened without the
Styles development profile: `appearance-provider-removed.png` shows original red
spine/ties. After another real save/exit, reinstalling exactly Styles 1.2.0 restored
the blue appearance automatically (`appearance-provider-restored.png`) and again
saved/exited successfully. These runs do not manufacture appearance state in NBT.

The world is a disposable copy of the previous real schema-4 five-family fixture.
The four JSON files are read-only inspections after real world saves. Run
`python scripts/verify-appearance-lifecycle.py` to reproduce
`appearance-acceptance.txt`: all 35 rail IDs/profiles/mechanisms, eight vehicle
IDs/bindings, twenty carrier definitions/capture/occupancy records, and the chosen
style/tint survive. Only that GUI edit increments one rail revision. Captured
carriers continue tiny native angle/velocity corrections while the world runs;
this is not a frozen-pose comparison or a moving styled ride. Native synthetic
schema-1–4 format tests are separate from this actual schema-4 world migration.

Temporary operator platforms at (-96,122,20), (-98,122,17), and (-66,122,23)
remain in this disposable rendering fixture. Remove them before using it for ride
acceptance. No passengers boarded or dispatch occurred during this checkpoint.
Sable's transient non-existent-sublevel movement messages recur after one reload;
the existing FD902/FD904 investigation remains open. No new crash/fix claim is made.

Double spine, curved/banked alignment, complete clearance and styled rides,
Flywheel/render/shader modes, and Engineered operating acceptance remain pending.
One straight's exterior rendering cannot certify every style/family combination.

## Earlier catalog-only checkpoint

- `provider-native.txt`: default standalone profile passed all 19 native GameTests;
  no optional API/client class broke dedicated startup or original-family checks.
- `provider-unit.txt`: all 19 JUnit tests passed, including exact-version access
  guards and rejection of cosmetic ownership of mechanical components.
- `provider-artifact.sha256`: the development JAR used for the catalog check.
- `styles-catalog-client.txt`: packaged client, separate Track Styles 1.2.0 JAR,
  `-PfdPackaged -PfdIntegrations=styles -PfdWorld=FD04StylesCatalog`, Java 21 and a
  2-GiB heap cap. The client-only baking listener successfully read 55 public
  decorative model entries. `/fd integrations` reported Styles inspected and
  Engineered absent. A copied five-family world loaded its original native links.
- `styles-installed-original-arrow.png`: original functional Arrow rails and
  original captured vehicle remain visible with Track Styles installed.

Geometry certification and style selection/rendering remain pending. No external
functional Arrow track, operating-addon combination, shader setup, or model
resource reload/removal cycle is certified by this checkpoint. Attempts at the
F3+T shortcut produced no confirmed resource reload in the log. The optional JAR
also reports an unavailable `block/track/empty` model, preserved in the log.

During the subsequent menu/exit attempt, both the isolated Xvfb and client exec
sessions returned exit code 143 and the display socket disappeared. The exact
termination source is unproven. This is not recorded as a clean application exit,
an addon crash diagnosis, or persistence acceptance. The prior separate FD305
five-family run did complete Save and Quit, reopen, and Quit Game successfully.
