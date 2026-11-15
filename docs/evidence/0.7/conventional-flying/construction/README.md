# Flying content and player acceptance

FD1801B and FD1801C complete, 2026-11-15. FD1801 closes cumulatively from this
record and the accepted [mechanics/occupied handoff](../README.md). No additional
ride or restart was required. New-generation remains independently owned.

## Normal construction

The same private Java21 packaged client/server and fixed input settings were
used. The saved, recovered/unloaded workshop was resumed; the prior accepted
ride area was left intact. Supplied fixture inputs were vanilla iron/copper/dye,
Create red seats, native Coasters bearings, normal Chain and the existing Rotation
Tool. Commands placed two upstream anchors, a crafting table, stations and safe
scaffolding; setup teleports positioned the player for each normal interaction.
No Flying Track or Flying Car was supplied by command. No movement/building of
upstream scaffolding, ingredient gathering or station crafting is claimed here.

The initial setup commands encountered unloaded chunks before the platform existed;
Dev fell in the empty world and respawned. The area was force-loaded and the
platform constructed before the corrected run. This fixture error affected no
vehicle or accepted mechanics. All construction claims below use the corrected
live session and supplied replacement ingredients.

1. Normal crafting-table/recipe-book input exposed an exact recipe collision with
   Axis Track: after the first Flying batch, the remaining cyan grid recalculated
   as Axis. [Failure](recipe-collision.png) is retained. Flying now uses **light
   blue dye**, a distinct recipe, with the generator and guide updated. The exact
   production JSON was copied into a test-world override and reloaded normally;
   [identity](recipe-artifact.json) verifies byte equality. This data-only correction
   does not invalidate mechanics. [Corrected grid](track-recipe-corrected.png)
   crafted another sixteen Flying Track normally; inventory total32 confirmed.
2. [Car recipe](car-recipe.png) used four Create seats, three iron, one native
   bearing and one crafted Flying Track per car. Normal Shift-output crafted two
   distinct cars, consuming two track items; [inventory](crafted-inventory.png)
   and authoritative readbacks confirm two cars and30 track.
3. Normal Flying Track use selected anchor `(220,73,12)` then `(220,73,60)`.
   The first item component stored that endpoint; the second use cleared selection
   and consumed12 items, leaving18. [Native span](normal-track-span.png) and
   [saved rail](saved-built-consist.json) show the48-block dedicated Flying identity.
4. Normal car-item air-use on that span at Z26.5 and34.5 created chassis
   `3985a508-9555-4240-a777-ea17510e10b6` and
   `2982be57-b4af-465b-a5c9-11500f6db61c`, with bearing X220.5 and exact8 spacing.
   Both crafted car items were consumed. Each native row/chassis retained the
   authored model, seated lock and clear envelope.
5. Ordinary empty-hand station clicks bound each car. Both became ready.
   Normal Minecraft Chain use on the first bearing stored its native link target;
   [second-target view](normal-chain-eight.png) showed `8.0 blocks`. One use on the
   second bearing produced the native `linked carts ... at distance 8.000` log.
   The native Chain implementation retained the16-item stack; the input record's
   predicted one-item consumption was not observed and is not claimed.
6. Normal Shift-right-click on the first station rebound the final consist.
   [Editor](final-consist-bound.png), [native state](native-linked-rebound.json)
   and [saved identities](saved-built-consist.json) show both cars stopped,
   seated-locked, ready and fault-free with one shared dock/consist. The temporary
   upper scaffold was removed; [placed view](placed-cars.png) shows the final build.

[Normal inputs](normal-input.json), [setup/readbacks](setup-and-readback.json)
and [server events](server-events.txt) distinguish setup from actual clicks.
The construction span is unpowered; it proves crafting, normal connection,
placement, station binding and coupling. The separate occupied powered loop
already proves the complete seated→prone→ride→seated→unload operation. These
compatible records close the workflow without relabeling command-built links
from the earlier fixture as player construction.

## Finite visual review (FD916 contribution)

Reference is the accepted FD1801S four-seat station-changeover design and existing
original metal/cyan palette. The geometry intentionally approximates a Flying
train with a stationary overhead running chassis/fork and one physical row.
The inventory car shows the suspended row and lower retention; the track item
shows the overhead rail/braces. No optional skin or seat variant is advertised.

| Required criterion | Inspection/result |
| --- | --- |
| Silhouette, proportions, style | [Inventory](crafted-inventory.png), [placed pair](placed-cars.png), seated and prone views in the parent occupied record identify four abreast seats below N running gear. Dimensions match the specified hip/sweep/fork arrangement. |
| Assembly and motion alignment | Native hinge/clearance evidence carries forward. Parent seated/changeover/prone/full-roll views show rider hips, carrier seats, separate harness/leg pieces and fixed fork alignment. Original low underbrace is absent; center brace and outboard pivots retain required clearance. |
| Texture finish | Original generated meshes use intentional iron, cyan seat, black harness and orange latch surfaces; lime cues mark engaged locks. Explicit UVs, subdivided legal cuboids and matching selection boxes; no missing texture or unintended fallback in the inspected inventory, stationary or occupied views. |
| Player-facing appearance | Distinct car/track inventory silhouettes, two clear restraint controls, measured posture/lock display and fault text; normal player actions observed. Existing station ready/locked cues and Rotation Tool are reused. |
| Supported variants | One Conventional vehicle/track appearance; no family×skin×profile expansion. Accepted shared station/tool artwork carries forward unchanged. |

The finite correction list is closed: lower brace/sweep alignment and initial COM
hinge alignment were corrected and qualified in A; recipe identity and accurate
station/track instructions were corrected here. No remaining required visual
criterion failed. These views feed FD916 reconciliation without reopening A or
waiting for the aggregate presentation package.

## Final scope and cleanup

The [player guide](../../../../FLYING.md) explains exact recipes, two-anchor track
placement, station empty-hand binding/rebinding, eight-block Chain coupling,
separate restraints, normal controls, clearance/motor setbacks, interrupted recovery
and the optional powered example. The example's full roll/return behavior was
accepted in A; programmatic example links remain explicitly disclosed.

All owned units `fd1801-flying-20261115-{client,server,xvfb}.service` are inactive,
MainPID0. The server saved all dimensions. Final built world, earlier occupied
checkpoint, failure/recovery worlds, raw input/traces and JARs remain under
`/mnt/NVME4/fourthdimension-runs/fd1801-flying-20261115`, outside build outputs.
No process or display is handed off live. The runtime recipe overlay is retained
in that test world with its exact hash; the distributable uses the same production
JSON. Shared mechanics proof and unchanged0.4–0.6 evidence remain closed.
