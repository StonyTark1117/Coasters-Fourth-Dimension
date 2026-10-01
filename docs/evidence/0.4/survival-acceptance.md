# FD403 native survival checkpoint — 0.9.0-dev.11

This advances crafting and construction validation toward FD403. It does not
close the complete survival teaching exercises, FD404 or the 0.9 release gate.

## Native crafting and world use

`SurvivalBuildingGameTests.nativeSurvivalCraftingAndWorldPlacement` uses a
survival-mode native ServerPlayer. The fixture supplies raw materials, upstream
Create/Coasters components, a crafting table, stone supports and loaded chunks.
It does not supply any Fourth Dimension output item. It populates crafting grids
from the player's inventory and takes results through Minecraft's actual
`CraftingMenu` result slot. All fifteen recipe types consume their grids and
award recipe discovery; sixteen crafting operations make a second boarding
station for the negative station test.

The resulting inventory is then used through `ServerPlayerGameMode.useItemOn`,
including NeoForge's right-click event and native block/item dispatch:

- All ten native anchors consume their items on placement.
- Each of the five family tracks rejects insufficient inventory without adding
  an addon rail or native anchor peer. Sneak-click clears selection. Successful
  two-anchor placement consumes eight items for a 32-block straight.
- All twenty ordered wrong-family car/track combinations reject without
  consuming a car or allocating a vehicle. Five matching cars consume their
  crafted items, preserve their carrier topology and settle into native capture.
- Crafted Free Spin inducer/damper items each consume one item and add one span.
  The Rotation Tool's server interaction accepts all five section anchors.
- Crafted boarding stations and a shuttle winch consume their block items.
  Empty-hand station use binds the captured Arrow car. An ordinary station
  rejects PAX, including an invalid PAX binding written by an earlier build.
- A sneak-use of the winch rejects the flat PAX section without binding a train
  or allocating a carriage. This section lacks a valid shuttle station and spikes.

The detached player uses NeoForge's fake outbound connection and is not inserted
into the negotiated client list. Outgoing menu/editor packets are discarded.
This is server crafting and interaction evidence, not proof of a real client GUI,
manual shaping, mining/upstream component acquisition, boarding or a powered ride.
No full PAX route is built by this test.

## Demonstrated station defect and fix

The [before probe](survival-station-before.txt) restores the station source from
`64836ce4058ae8d6894700854fbf6b492e7b4803` while running the new test. Native use
returned `SUCCESS` and stored the PAX chassis UUID. Ordinary station redstone
operation excludes PAX, so this created a misleading, unusable binding.

The final station search excludes PAX and clears an old PAX binding when used.
The regression returns native `PASS` with a null binding; no interaction action
is consumed. Vanilla's server dispatcher falls through an empty-hand block
rejection to `PASS`, so the assertion checks that it does not consume the action
rather than requiring an item-use `FAIL` result. Ordinary Arrow binding still
passes. The final build's metadata now accurately lists all five shipped families.

The first test attempts also exposed fixture mistakes: wrong-family checks ran
after consuming needed matching cars, and station binding was attempted in the
same tick as native body allocation. Rejection checks now run before matching
placement, and binding waits for the native bodies to settle. These were not
production fixes or release acceptance results.

## Final validation

- [Standalone](survival-native-standalone.txt): all 28 required native tests pass.
- [Track Styles + Engineered](survival-native-combined.txt): all 28 required native
  tests pass, including the installed optional operating scenarios.
- [JUnit](survival-unit.json): 25 tests, no failures, errors or skips.
- Teaching/Ponder generation remains byte-identical to the committed assets.
- The [artifact digest](survival-artifact.sha256) is unchanged after the combined
  build. Packaged metadata identifies dev.11 and all five families; the JAR
  contains no nested optional dependency JARs.

The [validation record](survival-validation.json) preserves full-log hashes,
source identity for the failing probe, coverage and fixture limits. The guide
now specifies survival track cost, station binding/operating distances and the
PAX route requirements enforced by the winch.

The dev.10 Ponder checkpoint separately passed all eight exact-head CI jobs:
[push](ponder-ci-push.json) and [PR](ponder-ci-pr.json). This does not substitute
for validation of the dev.11 revision.

## Remaining gates

Complete survival building and operating exercises through a real client remain
open. This run does not test resource reload, occupied rides, rider interaction,
full banked vehicle clearance, recovery towing, the complete Engineered operator
workflow, migration or measured release performance. The earlier bitmap-glyph
resource-reload crash remains unresolved. FD401, FD402, FD403, FD404 and
FD901–FD905 retain their existing acceptance criteria.
