# Coasters: Fourth Dimension

A Minecraft/Create: Coasters Simulated addon with **17 vehicle styles**, matching
functional track, native moving seats and mechanisms, station/restraint controls,
saved ride programs and optional integrations. The rides are independently authored
game interpretations inspired by coaster concepts; manufacturer names do not imply
endorsed assets, exact engineering or verified real-world coupling.

**1.0 qualification is in progress.** The expanded feature set and production art
are frozen; final release acceptance and packaging are tracked in
[ROADMAP](ROADMAP.md) and [the current qualification queue](docs/CURRENT-QUALIFICATION.md#current-work-queue).
The qualification JAR still carries the historical `0.9.0-dev.27` label. That label
is not a published 1.0 release. [Frozen inputs](docs/evidence/1.0/lead/freeze.md)
identify the actual source, JAR and dependencies. Earlier checkpoint limitations
remain historical evidence in the [changelog](CHANGELOG.md); they do not reopen
completed cases.

## Install

Install the same Fourth Dimension build on every client and dedicated server,
using Minecraft **1.21.1**, **Java 21** and these qualified dependencies:

| Component | Qualified version |
| --- | --- |
| NeoForge | 21.1.256 |
| Create | 6.0.10; development coordinate 6.0.10-280 |
| Create: Coasters Simulated | 0.1.5 |
| Sable / Sable Companion | 2.0.6 / 1.6.0 |
| Veil / Flywheel | 4.3.2 / 1.0.6 |

Use the required support dependencies supplied by those mods. The
[artifact manifest](docs/evidence/1.0/lead/freeze.json) distinguishes development
artifacts from distributable upstream metadata and records exact hashes. No
optional coaster addon is required for standalone Fourth Dimension rides or bundled
inside this addon. Keep **Immersion off dedicated servers**.

Before opening an existing world, follow [backup, copied-world upgrade and restore](docs/MIGRATION.md).
Current saves use schema8. Foundation synchronization uses protocol7, editor
messages8 and the PAX presentation snapshot1; matching client/server builds are
required. Do not downgrade an upgraded world in place.

## Vehicles and guides

| Style | Motion and operating guide |
| --- | --- |
| Arrow controlled | Programmed independent seat rotation on four-rail track; [basic construction](docs/PLAYABLE.md) and [banked operation](docs/evidence/0.4/bank-acceptance.md). |
| Legacy ZacSpin | Independent passive back-to-back seat rotation; original-family construction below. |
| S&S-inspired Free Spin | Four independently modeled seat pairs with magnetic influence; [magnetic equipment](docs/MAGNETIC.md). |
| Revised ZacSpin | Revised free-rotation topology; [Revised/Shuttle guide](docs/REVISED-SHUTTLE.md). |
| PAX-inspired shuttle | Physical catch, carriage lift and release; [shuttle operation](docs/REVISED-SHUTTLE.md) and [interruption recovery](docs/RECOVERY.md#pax-shuttle-interruption-recovery). |
| Wing | Fixed seats beside the track; [Wing](docs/WING.md). |
| S1 Spinning | Conventional native free-yaw turntable; [S1](docs/SPINNING.md). |
| S2 Spinning | Launched/inverting free-yaw configuration with magnetic influence; [S2](docs/SPINNING-S2.md). |
| Suspended | Hanging cabin with bounded passive swing; [Suspended](docs/SUSPENDED.md). |
| Fixed Inverted | Fixed row below overhead track; [Fixed Inverted](docs/FIXED-INVERTED.md). |
| Axis | Independent longitudinal pod roll; [Axis](docs/AXIS.md). |
| Roller Ball | Lift/summit/free-descent pendular cycle; [Roller Ball](docs/ROLLER-BALL.md). |
| Conventional Flying | Physical seated/prone posture; [Flying](docs/FLYING.md). |
| New-generation Flying | Seated/prone posture with independent scenery-facing yaw; [NG Flying](docs/NEW-GENERATION-FLYING.md). |
| Pipeline | Fixed-seat whole-chassis roll on dedicated track; [Pipeline](docs/PIPELINE.md). |
| Dive | Incline, held dwell and validated gravity drop; [Dive](docs/DIVE.md). |
| Powered Inverted Yaw | Upright seats below overhead support with independently powered yaw/show; [Powered Inverted](docs/POWERED-INVERTED-YAW.md). |

Each guide defines its actual seat count, variant, track reservation, drive,
coupling, station and recovery boundaries. Matching appearance alone never grants
functional eligibility. Fixed Inverted, Suspended and Powered Inverted governor
recipes retain their documented ingredients; use the recipe book for their distinct
current crafting patterns.

## Build and operate an original-family ride

1. Craft the matching track/car, Rotation Tool and Boarding Station, or use the
   Fourth Dimension creative tab. Connect two Coasters anchorpoints with matching
   functional track. A span is at most64blocks; Survival consumes one track item
   per four blocks, rounded up. Leave the family guide's full rider/mechanism
   corridor clear.
2. Configure Arrow rotation before occupying the section. Keys use
   `distance:degrees:slope`, separated by semicolons; slope is degrees per block.
   Cover the whole displayed section length. For a36-block section,
   `0:0;18:360;36:720` gives two turns. Adjacent ends must agree in angle and slope;
   a changed native curve requires explicit profile revalidation.
3. Place the correct car. Only declared same-family consists may couple: normal
   Chain use selects each exposed native bearing. Arrow/PAX use six-block spacing;
   other families have their own guides. Legacy and Free Spin do not gain ordinary
   ride coupling because an ordinary recovery bearing can fit their rails.
4. Bind a nearby Boarding Station, wait for Ready and use an empty cushion to board.
   The tall visual back is not the cushion's click target. Use the Rotation Tool
   on the car/station or while seated, Close restraints and explicitly Dispatch.
   Native Coasters/Create or the declared optional drive moves the chassis.
5. Capture/Stop on the family's permitted dock, wait for Ready, Open restraints
   and Shift to leave. Repairing power or support does not automatically resume a
   faulted ride. Use the guide's explicit recovery action after checking the cause.

Original free-spin tuning uses declared rider loading (80kg per rider), damping
and empty-car ballast. Magnetic Inducers/Dampers create bounded spans on matching
Free Spin track; orange induces and cyan damps. They exchange energy with native
motion and do not replace propulsion. See [the equipment guide](docs/MAGNETIC.md).
The separately selectable comfort option stabilizes only the local camera;
mouse look, native eye position, physical riders and remote players remain native.
Its default K binding can conflict with Immersion: assign distinct keys in Controls.

Operator-level examples use `/fd demo <family>` in a clear disposable world; the
family guides list exact layouts and footprints. Examples provide authored setup,
not a claim that commands are required for normal construction. `/fd status`
reports family, angle, capture and occupancy. `/fd clear` removes this dimension's
addon vehicles/track connections; reserve it for disposable worlds.

## Optional integrations

| Optional addon | Frozen version / file | Accepted role |
| --- | --- | --- |
| Track Styles | 1.2.0 / 9020601 | Cosmetic ownership/fallback on eligible track; no automatic external functional Arrow conversion. |
| Engineered | 1.1.1 / 8863559 | Declared FE drive/control, including the bounded ordinary-bearing recovery source. |
| Additions | runtime0.2.2 / 9243346 (distribution label0.2.5) | Accepted station and selected-seat adapters; Aeronautics1.3.2 and Interiors0.6.1 support dependencies. |
| CTNB | 1.2 / 8718286 | Specifically measured functional rails/operations; appearance is not blanket eligibility. |
| Magnetized | 1.1.0 / 8668703 | Declared longitudinal drive authority. |
| Coaster Skin | 1.1.0 / 9409938 | Accepted appearance ownership. |
| Immersion | 1.1.1 / 9542588 | Client-only effects composed with native rider pose and comfort. |
| Waterparked | 0.2.8-beta-hotfix1 / 9058331 | Slide coexistence/separation; requires KotlinForForge5.10.0. |
| Create Coasters | 2.0 / 8110328 | Accepted native utility coexistence; does not confer another seat/drive authority. |

Named profiles are base, Styles-only, Engineered-only, combined-core
(Styles+Engineered), operations (plus Additions/CTNB/Magnetized), and
combined-expanded (plus Skin/Immersion/Waterparked/Create Coasters). The
[frozen capability record](docs/evidence/1.0/lead/freeze.md) and
[0.9 acceptance](docs/evidence/0.9/acceptance.md) define coverage; every possible
addon subset/version is not promised. Create Addition is an optional validation
FE fixture, not a required release dependency. Optional upstream code/art remains
separately installed; see [attribution and terms](THIRD_PARTY.md).

## Limits and known boundaries

Build within **±1024blocks horizontally from world origin**. Per dimension,
admission/storage limits are256sections,64vehicles,8192profile keys and512magnetic
spans, with at most8spans per section. These are correctness limits, not a promise
that a maximally populated park meets measured performance budgets.

[Ordinary-bearing recovery](docs/RECOVERY.md) covers the certified unmodified
bearing and five original families, at most four original cars, at or below
0.75blocks/s, on their matching route. It retains the stated equipment, coupling,
propulsion and operator guards. It does not certify arbitrary added bodies,
locomotives, newer-family towing or every external drive.

Supported graphics are Minecraft Fast, Fancy and Fabulous with the pinned stack.
Historical shader observations do not advertise a current Sodium/shader profile.
[Performance](docs/PERFORMANCE.md) distinguishes component timing and renderer
correctness from representative-park whole-frame/tick/memory results.

Unknown schemas/providers and unsupported conversions fail closed. A future
external Track Styles Arrow4D functional adapter, standalone builder tools,
unified multi-train management and moving-track mechanisms remain outside1.0.
Four-pair Free Spin and Revised topologies, dimensions and force models are game
approximations, not manufacturer specifications.

## Development

Use Java21 for every build and launch on this host:

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
"$JAVA_HOME/bin/java" -version
./gradlew --version
./gradlew build
```

Follow [the validation policy](docs/VALIDATION-POLICY.md) and current queue before
scheduling tests; accepted cases are reused. `./gradlew properties` reports the
actual `buildDir`; colon-containing checkout paths default to persistent
`~/.local/state/fourthdimension-<path-hash>/build`. Keep valuable worlds outside
build directories and `/tmp`. Graphical project tests use an owned private Xvfb
and the [packaged run procedure](docs/FD403-RUN-PREPARATION.md).

`-PfdPackaged` selects the built JAR; isolated client/server/observer game-directory
properties prevent run-data collisions. `-PfdValidation` enables bounded state
tracing; `-PfdRenderValidation` instruments `runClient` track geometry stages.
Neither trace is a whole-frame benchmark. Use immutable runtime inputs when
preparing concurrent builds. Build metadata, release artifact identity and
clean-install evidence belong to the final packaging record.

Design and contribution references: [PLAN](PLAN.md), [ROADMAP](ROADMAP.md),
[milestone guides](docs/roadmap/README.md), [production asset sources](assets-source/fd917/README.md),
[license](LICENSE), [third-party provenance](THIRD_PARTY.md), [changelog](CHANGELOG.md).
