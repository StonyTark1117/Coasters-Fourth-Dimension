# Crafting discovery and item guidance checkpoint

Dev.9 progresses FD403 toward 0.9. It does not complete Ponder, the teaching
review, FD404 or the release candidate gates. Save schema and protocol remain 5.

## Player-facing changes

All fifteen craftable items now have localized operating guidance. The client
tooltip listener reads language keys without retaining fonts or requiring a
player; native search-tree tooltip collection can use the same path. The first
packaged inspection exposed right-edge clipping for Revised Free-Rotor Track at
the 854×480 window size. Shorter anchor-selection instructions fix that observed
case; the initial screenshot and checksum are retained separately from final
acceptance. The final artifact was relaunched and all fifteen tooltips captured.

Each recipe now has a vanilla inventory-change discovery advancement and a
recipe-unlocked alternative, with its reward granting the corresponding recipe.
Family dyes discover track recipes; matching track items discover car recipes.
The equipment ingredients are listed in [the five-family building guide](../../BUILDING.md).
The recipes themselves and their quantities are unchanged. All content works
with the standalone dependency stack; optional addons supply no ingredients.

`scripts/generate-teaching-data.py` generates the language additions and fifteen
advancements from the complete recipe set. It rejects missing guidance or
discovery definitions. A repeat invocation leaves resources byte-for-byte
unchanged, and the standalone CI job checks that regenerated data matches Git.

## Native evidence

The final local standalone and combined profiles each pass **26 required native
tests**. `TeachingContentGameTests` resolves every recipe through the installed
native RecipeManager, matches the actual ingredient grid, checks the exact item
and output count, and rejects a grid missing one required ingredient. It then
uses real ServerPlayer advancements/recipe-book state and the native inventory
criterion to check all fifteen discovery rewards.

The test's detached player discards only network output through NeoForge's fake
connection. It does not simulate a connected Minecraft client. The first fixture
used the built-in connected mock helper without mod-payload negotiation, which
caused the base Coasters sync to reject outbound packets and crash that test
server. The final fixture stays outside the global connected-player list while
retaining actual advancement listeners and recipe rewards. No product packet
guard or installed-addon test was weakened to fix the harness.

`teaching-native-{standalone,combined}.txt` records fifteen crafting outputs,
fifteen missing-ingredient rejections and fifteen discoveries per profile. The
existing **25 unit tests** pass without failures, errors or skips. Unit results
and the final artifact checksum accompany these native records. CI covers all
four profiles separately after push; local results do not imply remote completion.

## Packaged client evidence

The final packaged JAR includes all fifteen advancements and 45 guidance lines.
It contains no diagnostic font mixin or external OBJ resource. The client log
identifies that archive as the code source. Separately installed Styles 1.2.0 and
Engineered 1.1.1 were present in this GUI run.

The disposable `FD04Teaching` world is a closed-world copy of the wheel-placement
fixture. All fifteen item tooltips were captured in the creative tab. A real
F3+T reload began at **01:28:24 MST, 2026-10-01**, and Styles rediscovered 55 entries
at 01:28:26. Chat command suggestions rendered after the reload. The player had
neither the Arrow recipe advancement nor its known recipe. Acquiring one red dye
then triggered discovery. A conditional native NBT query confirmed the exact
`fourthdimension:arrow_track` recipe in the player's recipe book; it did not grant
or modify that recipe. Saved player data and the completed `has_ingredient`
advancement independently confirm the result after normal Save and Quit.

`teaching-tooltip-*.png`, `teaching-reload.png`, `teaching-chat.png`, the discovery
screenshots and `teaching-client.txt` record the observations. The source log and
save hashes, selected recipe state and aggregate results are in
`teaching-validation.json` and `teaching-player-discovery.json`. Quit Game ended
with client/Gradle exit 0. No occupied ride occurred in this check.

This production replay did not reproduce the dev.8 bitmap crash. It does not
prove its cause or resolution, or replace the revised styled post-reload ride
gate. The earlier failed run and separate diagnostic observations remain in
[the font investigation](font-reload-investigation.md).

## Remaining FD403 work

Interactive Ponder scenes, the full teaching-content review and end-to-end
survival building exercises remain required. The written guide links all five
shipped standalone example families and describes their different controls,
strict matching, the certified bare-bearing exception and uncertified recovery
towing. Existing powered example tests and earlier five-family packaged rides
provide separate bounded operating evidence. This checkpoint adds acquisition
discovery and guidance; it does not substitute those for the unfinished tutorials.
