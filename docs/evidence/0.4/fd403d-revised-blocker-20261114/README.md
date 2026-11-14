# FD403D Revised blocker record

Date: 2026-11-14 UTC

**Diagnosis corrected:** The [subsequent read-only review](review.md) found the
accepted Revised chassis, four carriers and valid rail binding in this exact
stopped save. The observed player position was beside an Arrow plot. Preserve
the observations below, but do not use their missing-binding inference as a
confirmed cause or a requirement to rebuild the world.

This record preserves the targeted FD403D execution after the assignment was
reassigned. The owned runtime was `/home/braydon/.local/state/fourthdimension-runs/fd403d-revised-20261113`, private Xvfb `:299`, server port `25484`, and Java 21. The client and server were stopped after the attempt; the world remains in `server-new/world`.

## World and connection

The live server was confirmed as the owned `server-new` process and the client
joined twice at `(253.5, -60, 74.5)`. A retained save copied from
`fd403-current-20261112/server/FD403ArrowPoweredRetry` rendered a track and
car-like assembly at that position. The earlier teleport to `(272, -42, 252)`
was empty and was a world-selection diagnostic, not an acceptance observation.

The player inventory contained a Rotation Tool in slot 8 and the selected item
was verified as `fourthdimension:rotation_tool`. F3 identified the crosshair
target as a simulated-coaster anchor with `snap_engaged: true`, but the
Rotation Tool right-click produced no editor, state message, or server-side
vehicle transition, including with the previously accepted temporary
interaction-range diagnostic (`minecraft:player.block_interaction_range = 10`,
restored to `4.5` before shutdown). This indicates that the retained save has
rendered/native route content without a matching current FD vehicle binding;
it cannot establish player-built Revised operation.

## Invalidated attempts and server health

The first client interaction window ended with a timeout while the dedicated
server was saving Sable sublevels. The server recorded `Can't keep up` warnings
up to 33,273 ms / 665 ticks behind and the client disconnected. After a clean
client restart and confirmed reconnect, the same targeted Rotation Tool action
again produced no editor or state transition. No powered travel, lower-hinge
motion, capture/unload cycle, or reopen claim is made from this run.

Relevant raw evidence is retained in the run root, including
`evidence/20261114T025350.938890Z-state.png` (route at the retained spawn),
`evidence/20261114T025438.521382Z-state.png` (F3 target),
`evidence/20261114T025745.152199Z-state.png` (range-diagnostic click),
`evidence/20261114T025831.191327Z-state.png` (post-reconnect targeted click),
`server-new/logs/latest.log`, `client-new/logs/latest.log`, and
`evidence/input.jsonl`.

## Remaining action

FD403D still needs a verified retained or rebuilt **player-built Revised** save
whose current FoundationData binds the Revised car/rail, followed by one normal
powered occupied cycle with independent lower-hinge observation, capture,
unload, and final reopen state. No source or shared teaching correction was
identified in this attempt.

## Superseding correct-plot trace

The follow-up trace in [review.md](review.md) supersedes the earlier binding
inference. The retained Revised save does contain the current vehicle binding.
With validation tracing enabled, a correctly targeted click reached
`RotationTool.useOn`, resolved the Revised chassis, dispatched `EditorNetwork`,
and opened the client editor. The earlier no-editor result was therefore a
target/setup failure; it is not evidence of a Rotation Tool or connector
defect. The powered occupied cycle and teaching claims remain open.
