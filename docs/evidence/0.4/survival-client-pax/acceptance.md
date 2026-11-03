# FD403 PAX connected survival construction checkpoint

On 2026-11-02, the packaged standalone client opened `FD403PaxClient`, a copy
of the existing survival family world, on a separate x=400 PAX plot. The world
uses seed `1933455356643639169`, game type 0, and commands enabled for the
fixture. Commands supplied upstream iron, copper, dye, Create seats and shaft,
redstone, Minecraft Chains, native Coasters carts and anchors, stone supports,
and player positioning. No Fourth Dimension output item was command-granted.
The client ran Minecraft 1.21.1 and NeoForge 21.1.248 with `-Xmx4G`.

Source commit `4950fea30ace94e4fe87093778564d9e59566016` produced the packaged
JAR with SHA-256 `2a52a641f13ef78fa3c23c2fe466ff22bd359d3af33c4fb26b0f3773379285a2`.
The player used the ordinary crafting result slots for three batches of
[PAX track](track-recipe.png), one [Shuttle Winch](winch-recipe.png), and four
[PAX cars](car-recipe.png); the [four-car inventory](four-cars-crafted.png)
precedes placement. The three route sections consumed 14, 8, and 15 of the
48 crafted track items, leaving six in the final [saved inventory](saved-inventory.json).
Native anchors were placed by ordinary item use at `(400,-19,202)`,
`(400,-43,250)`, `(400,-43,282)`, and `(400,-11,330)`.

The middle rail required a Rotation Tool geometry rebind after adjacent curve
construction. The final [schema-6 save](foundation-save.json) contains three
valid matching PAX rails, with middle revision 2, and four original PAX
vehicles on the station section. Each has two captured empty carrier bodies,
no fault, and boarding readiness after the temporary supports were removed.
The [fourth placement](fourth-car-placed.png) consumed the last crafted car.
Some approach stones under the cars initially caused persistent carrier motion;
the winch rejected binding while three cars were unready. Removing those stones
allowed all eight carriers to settle, and ordinary right-click placement plus
sneak-use [placed](winch-placed.png) and [bound](winch-bound.png) the crafted
winch in `BOARDING` phase.

The winch's block data at `(394,-43,258)` reports `shuttle_cars` with **one**
chassis UUID and `shuttle_cycles: 0`. Native Chain clicks selected two distinct
cart UUIDs, but no `linked carts` event was logged, and no three-link consist
was established. The winch therefore proves a valid single-car route binding,
not the required four-car shuttle. No motor was placed, no rider boarded, and
no powered cycle or final catch is claimed. The saved Chain stack retains a
selection target; it can be replaced with a fresh upstream Chain before retry.

Post-save inspection of the original Coasters path record found that the
station curve between `(400,-43,250)` and `(400,-43,282)` has endpoint axis
Y components `-0.215512` and `-0.163029`, although its anchors share Y=-43.
Those are measured nonlevel station tangents, consistent with the observed
unlinked cars rolling together. A source check found that the Fourth Dimension
family track item calls its own placement path and does not read Coasters'
Left Alt smoothing control. A retry should inspect and level the station
with native curve handles, rebind its profile, and verify six-block car
spacing before Chain clicks. This is a route diagnosis, not a passing retry.
The raw
saved Coasters path record SHA-256 is
`25fbd5c1b379522a4a5f7482430ba660b96c1a33671b326adf6dab644b0b8a7f`.

All three client sessions exited through normal save and title controls, with
`BUILD SUCCESSFUL` in their logs. Log SHA-256 values, in construction, rebind,
and winch order, are
`4dba326d527e3f8a53e31c3d0db1aa0b47bf684989887f60c68c741fe3e78caa`,
`29f7c63eb7440579a6c7c38d157fa09e6795115cb3834b36ccda36231e6c07db`,
and `e2e56607c7b1d5a5cd271dab64f1206f7440b25a3c99bd4f7f350e8c5f00acd2`.
The final foundation save SHA-256 is
`43b94cfcb327326940f448d3200dffb0035432473e8ee83d2ed6cf35e4aee419`.
The complete [saved survival world](world.tar.zst) is a 1.1 MiB zstd archive
for continuing the chain and winch check; its SHA-256 is
`debe2a1136a94291017551c769bd33a78e847393ddb53857b79e22f928d7cc6a`.

This is a connected survival construction checkpoint only. The separate
[shipped-demo client cycle](demo/acceptance.md) proves the original four-car
sample can carry a rider through powered lift, return, catch and unloading.
A later [survival-built cycle](survival-cycle/acceptance.md) closes this
checkpoint's Chain, occupied cycle and unload gap on a newly constructed
route. Powered exercises for the other families and the complete teaching
review remain open before FD404 can close.
