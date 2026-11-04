# FD403 survival-built PAX occupied cycle

On 2026-11-02, a packaged standalone Minecraft 1.21.1 / NeoForge 21.1.248
client opened `FD403PaxSurvivalRetry`, copied from the earlier PAX survival
checkpoint. The seed is `1933455356643639169`; game type is survival (`0`),
with commands enabled for test fixtures. Source commit
`a6e18c906a8e1f9f33880030dccd7b0be1b53b16` produced the JAR with SHA-256
`34c2d6ce14fecaf9941303ff950ec9f8ef586a61456669a0d404b0be43672db1`.
The client used `JAVA_TOOL_OPTIONS=-Xmx4G`. Its [complete log](client.log.zst)
has compressed SHA-256
`42360662409e09f6de13bbbe68529b76910368d871133615ee1b65c07f9e6c62`
and ends with `BUILD SUCCESSFUL in 26m 52s` after normal Save and Quit and
Quit Game. [Selected raw events](events.txt) retain the commands, native link
events, winch queries, shutdown and reopen observations.

Commands supplied **upstream ingredients and fixtures only**: 26 additional
iron ingots, six copper ingots, four green dye, eight Create red seats, four
iron bars, four native Coasters carts, one redstone, one Create shaft, six
native anchors and a crafting table. The copied player's inventory already
contained 18 iron, four copper and six PAX track. One seat was accidentally
placed during table selection, then one replacement upstream red seat was
granted. Four station-placement stones and the six anchor supports were
command fixtures. The old Chain stack did not produce a native selection;
it was cleared and replaced with three upstream Minecraft Chains. No Fourth
Dimension output item was command-granted. A Create creative motor was
command-placed solely as a power fixture; this run does not prove survival
acquisition of its power source or unassisted gathering of ingredients.

Through the normal crafting-table recipe book and result slots, the player
made 64 [PAX track items](track-crafted.png), four [PAX cars](four-cars-crafted.png)
and one winch. Native anchors were placed by item use at x=850 with
`(y,z)` pairs `(-31,100)`, `(-55,150)`, `(-55,175)`, `(-55,225)`,
`(-55,250)` and `(-23,300)`. Five matching rails were connected by ordinary
PAX track clicks in the order center, level approaches, then spikes. The
route consumed 58 track items, leaving seven including the six carried into
the run. The [saved native curve record](route-geometry.json) has all five
edges; the station from `(850,-55,175)` to `(850,-55,225)` has exactly zero
Y component at both endpoint tangents after both spikes were added. All five
new matching rails are valid in the final save.

Four cars were placed through normal item clicks at six-block station
intervals near z=183, 189, 195 and 201. Temporary placement stones were
removed. All four cars then reported boarding readiness with captured
carriers. The player used normal Chain clicks to make [three native links](events.txt),
each at `6.000` blocks, and the links restored on two later world opens.
The crafted [winch was placed](winch-placed2.png) at `(844,-55,183)` with X
axis and bound by empty-hand sneak-use to the four original chassis; the
[binding message](winch-bind.png) and raw query list all four IDs.

The first motor fixture used an NBT `ScrollValue` merge that left the actual
winch at zero RPM; the editor correctly rejected that dispatch with `Power the
shuttle winch at 16..128 RPM`. Replacing the upstream motor fixture in its
default state supplied a stable **16 RPM**. The player [boarded a native
seat](boarded.png), [closed restraints](editor-ready-closed.png) through the
mounted Rotation Tool editor, removed the boarding foothold and dispatched
through that same editor. [Lift motion](lifting-f3.png) and [outbound motion](outbound-f3.png)
show the mounted player's changing world position. Winch queries record
`LIFTING` at route distances 89.27, 79.52, 61.07, 45.82 and 22.52;
`OUTBOUND` at 14.70; then a final maximum distance of 170.97 beyond the
89.79 station, **one completed cycle**, `BOARDING` and an empty fault. The
[post-cycle editor](postcycle-editor.png) again showed Ready with restraints
closed. The player opened restraints and [dismounted](unloaded.png) normally;
`RootVehicle` was absent afterward.

After normal save and title exit, the same JAR reopened the world. All three
native links restored at 6.0 blocks. The winch still reported 16 RPM, the
same four chassis IDs, one cycle, `BOARDING` and no fault. The final
[schema-6 foundation save](foundation-save.json) has four new PAX cars bound
to the level station, all `stopping=true`, `boarding_ready=true`, captured,
unrestrained, fault-free and unoccupied. Its decoded JSON SHA-256 is
`ca381e749205af8350f76dfc87f55103bf2442d11a75be1d06701175f3f01d3f`.
The [complete stopped world](world.tar.zst) SHA-256 is
`44c6f5c0666d4100cfdae4413109b38a47402242956609ea41b6810356428166`.
Run [the verifier](verify.py) to check the [artifact hashes](artifact.sha256),
saved route, cars, native events and final winch state.

This closes the previously open **survival-built PAX Chain, occupied powered
cycle, unloading and reopen** checkpoint under a disclosed upstream power
fixture. It does not close FD403: the other families' complete powered
connected-client exercises and the fifteen-item/nine-scene teaching review
remain open. FD401, FD402 and FD404 also remain open.
