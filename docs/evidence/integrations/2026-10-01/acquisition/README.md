# FD403 standalone acquisition and teaching acceptance

All fifteen acquisition items now have ingredient-triggered vanilla recipe-book
discovery and two translated inventory hints beside their existing Ponder prompt.
The recipes themselves are unchanged. Discovery grants knowledge, not materials;
it uses vanilla advancements and the common iron-ingot tag. No optional addon is
needed for the recipes, descriptions or family demonstrations.

## Recorded acceptance

| Requirement | Evidence and scope |
| --- | --- |
| Every family has matching track and car acquisition | Native recipe-manager checks resolve all ten family recipes, the station, tool, winch and both magnetic items; outputs/counts match the material grids and missing ingredients are rejected. Base and combined packs each pass all fifteen cases. |
| Recipe discovery works for players | Each native case rejects dirt before accepting its trigger ingredient. A separate connected survival player repeats all fifteen cases through actual inventory changes. The saved player recipe book contains all fifteen recipes and completed advancement rewards. |
| Crafting consumes materials | The real survival client searches the crafting-table book for controlled track, fills the grid and crafts 16 items from exactly six iron ingots, two copper ingots and one red dye. The final saved inventory contains only the 16 track items. Other fourteen outputs are recipe-manager checks, not fourteen manual GUI crafts. |
| Items explain their role | All fifteen tooltips were visually reviewed in the standalone packaged client at 854×480, GUI scale 2. Both description lines and Create's Ponder prompt are readable. This does not assert every display size or resource-pack font. |
| Every family has a tutorial | The current Ponder classes and all five schematic structures are byte-identical to the [six-scene, two-client playback checkpoint](../create-ponder/README.md); all earlier language entries are preserved. Tracks/cars and equipment retain those entry points. The sixth scene teaches station interlocks. |
| Shipped demonstrations exercise their mechanics standalone | The current base native suite calls the same `ExampleLayouts.create` used by `/fd demo arrow`, `legacy`, `free_spin`, and `revised`. Arrow completes a full turn; legacy swings; Free Spin applies passive magnetic spans to independent carriers; revised runs its offset pivots. They traverse their circuits and capture passengers. `ShuttleExamples.create`, shared with `/fd demo pax` and `pax_tall`, demonstrates physical catch/lift/release, gravity return, power interruption and capture, including two tall-route cycles. |
| Consistent operating guidance | README now includes material counts, discovery ingredients, all five family matching rules and the difference between demonstration creative motors and survival power. Existing Ponder and station/winch telemetry guidance remains in place, including [occupied phase/fault evidence](../create-live-phases/README.md). Optional Engineered demonstrations remain separately labeled in the integration guide. |

This closes FD403 for the documented five-family, near-origin development scope.
It does not close FD404 or the FD901–FD904 release gates, extend the ±1024 world
coordinate bound, certify arbitrary ordinary carts, or expand optional-adapter
support. Ordinary-cart exceptions and appearance-versus-functional-family rules
remain explicit in README. Other translations and user font/GUI settings are not
claimed as tested.

## Native and packaged runs

The build passes 19 unit tests. Base and combined runs each pass 36 registered
native GameTests, including the new acquisition check. Base logs seven explicit
absent-addon skips; combined logs none and enables the required-energy PAX reload
checks. A skip is not compatibility evidence. Exact loaded versions are in the
manifest; the underlying pins remain Minecraft 1.21.1, NeoForge 21.1.248, Create
6.0.10, Sable 2.0.3 and Coasters Simulated 0.1.5, with the established combined
addon pins.

The packaged UI/server used `acquisition.jar`. The final build/native runs use
`acquisition-v2.jar`: their only differing entries belong to the acquisition
GameTest, including its anonymous no-network handler. Every production class and
resource is identical. Both immutable artifact hashes and that complete archive
comparison are retained. The test uses a real `ServerPlayer` with a no-network
handler because NeoForge deliberately disables advancement loading/rewards for
`FakePlayer`; it does not modify production advancement handling.

The initial FakePlayer attempt failed discovery. A subsequent test-only compile
attempt exposed the private visibility of NeoForge's dummy network handler; the
final test uses the public vanilla handler API and suppresses outgoing packets.
Both attempts are retained. A first tooltip capture batch was invalidated when an
already spawned slime killed the survival player. Those review strips remain under
`slime-interruption/`; the accepted fifteen captures were repeated after selecting
peaceful difficulty in the disposable world. Neither interruption was a recipe or
ride failure, and neither is counted as passing evidence.

The client/server exited zero. All tracked coordinators, wrappers and the owned
Xvfb display are gone; private ports are closed. Server properties match the
pre-session hash. The full disposable world remains under ignored
`build/acquisition/world`; final player data and advancement state are committed.

## Review and reproduction

- [All fifteen tooltips, first group](tooltip-review-0.png) and [second group](tooltip-review-1.png).
- [Discovered recipes](recipe-search.png), [filled crafting grid](recipe-grid.png), [consumed materials and crafted output](crafted-track.png).
- [Native/live verification](verification.json), [exact hashes and loaded versions](manifest.json).

From the checkout root:

```sh
python3 scripts/generate-recipe-unlocks.py
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer -PfdIntegrations=engineered,styles,additions,magnetized,immersion -PfdPaxEnergyReload
python3 scripts/verify-acquisition.py docs/evidence/integrations/2026-10-01/acquisition
```

The verifier checks recorded native assertions, exact recipe coverage, actual
saved survival inventory/knowledge, screenshot inventory, unit counts, process
cleanup and manifest hashes. Screenshot readability was reviewed separately;
file existence alone is not a visual check. The source generator only writes
recipe discovery advancements and does not change recipe ingredients or results.
