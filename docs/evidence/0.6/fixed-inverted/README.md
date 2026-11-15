# Fixed Inverted — FD1601A–C / FD1601

## Current checkpoint

Owner: `01a1dd1d-c6a6-7be0-9cb5-2b033ac550da`. Primary checkout
`/home/braydon/Documents/Coasters: Fourth Dimension`, branch `main`, integrated
prerequisite `ec27e982ece3e906bf144b25cbb5982b8d230d63` (qualified Suspended A).
The lead transferred shared production ownership per CURRENT-QUALIFICATION:
all `src/main/java/dev/fourthdimension/**` except Spinning*/Suspended* family files
and those two GameTest classes; shared en_us, new Fixed-only assets/tests/generator/
guide, and this evidence. Lead alone stages, commits, pushes and edits central docs.
Preserve other owners' uncommitted work and pre-existing `logs/`. No new branch.

Production implementation is in progress: one rigid chassis, four exact fractional
seat centers and equal native rider loads, overhead contact/renderer reuse,
36-block designated dock, four-car eight-block linking, whole-train fault hold,
explicit resume and fixed topology persistence. Preparation art's upper crossbar
was moved below the overhead spine to remove a static clash before any live run.
All original/Wing/S1/Suspended accepted behavior remains carried forward.

Owned runtime: `/home/braydon/.local/state/fourthdimension-runs/fd1601-fixed-inverted-20261115`,
server world `server/FixedInvertedWorkshop`, reserved display `:314`, game/RCON
25646/25647, units `fd1601-fixed-inverted-20261115-{server,client,xvfb}.service`.
No runtime was inherited. Server launched Java21 with 2G heap/4G cap; client and
Xvfb not yet launched. Client will use1536M/6G. Host had30GiB available and263G disk;
the active app cgroup reports max and approximately19.2GiB current. Recheck before
client launch because lead and Suspended also own independent clients.

Current check: targeted native fixture `fixednativecontract`
for four-car fixed topology, precise .8 masses with adjacent fractional seats,
dock-only unload, saved identity/consist and frame-loss whole-train hold. Java21
compile/package passed. Two console invocations used namespace/class prefixes and launched no test.
Diagnosis: @PrefixGameTestTemplate(false) registers the bare lower-case method,
confirmed against the retained Suspended console record. `fixednativecontract`
is the corrected launch. No native
acceptance claimed yet. Next: diagnose native result, then occupied normal input
and full-roll four-car sweep, non-upright save/reopen, construction/content, cleanup.

## Prepared inputs

- [Geometry and content brief](preparation/HANDOFF.md): dimensions, original
  mesh construction, route inputs, proposed production paths and unresolved
  native adapter details.
- `preparation/design.json`: machine-readable graph-origin dimensions, seat
  centers, native bearing conversion, envelope, limits and route waypoints.
- `preparation/closed.obj` and `preparation/open.obj`: original cuboid art in
  the specified design frame; these are preparation meshes, not installed assets.
- `preparation/geometry.svg`: dimensioned front/side drawing of the prepared art.
- `preparation/route.svg`: buildable route layout proposal.
- `preparation/report.json`: reproducible static geometry findings and explicit
  limits of that evidence. `preparation/prepare.py` produces these files using
  only Python's standard library.

These inputs do not close mechanics, content rendering or occupied criteria.
Production is now authorized on the integrated prerequisite. See the current
checkpoint above; the former preparation-only wait is superseded. FD1601 closes
from its cumulative A–C evidence and does not add an aggregate run.

## Current native retry / save diagnosis

First native fixture stopped at same-tick dock binding, before any mechanics
assertion. The correction moves binding to tick55 after native pose settlement
and uses a distinct retained test area; no production behavior was changed for
that fixture failure. `native/first-fixture.log` preserves it. The server was
asked to stop once. Its JavaPID740901 (wrapper740899) is flushing chunks;
`native-save-threads.txt` in the owned run shows RegionFile.pwrite, and written
bytes continue to increase. Do not kill a progressing save or relaunch alongside
it. `sync-chunk-writes=false` is set in owned server.properties for the next
process, matching the already diagnosed S1/Suspended host save workaround.

The focused `FixedInvertedContractTest` passed (`native/unit.log`): ordinary/wrong
mount, undersized longitudinal envelope, missing overhead/capture rejection and
fixed/no-rotary eligibility. No earlier-family cases were reopened. Generated
fixed models pass bounded element checks and have explicit atlas-safe UVs.

The first save finished normally at2026-11-15 06:33:10UTC (10m48s).
The retry confirmed all four settled centers, empty faults and ready dock guards.
Coupling in the same tick as rebind then failed because native canCouple reads the
prior cached boardingReady. This second fixture-timing failure is diagnosed:
coupling moves to tick65, after the tick55 dock bindings and physics readiness
update, with an explicit cached-ready assertion. The native/player guard remains
unchanged. Preserve `native/settled-fixture.log`; next fixture uses X=-1000 so it
does not overwrite the previous retained bodies. No graphical launch yet.

## Native pass and first occupied interaction

`native/accepted-native.log` closes the focused native case: four native chassis
with four fixed seats each,8-block coupling, empty mass6, exact3.2 added for four
adjacent equal riders, dock-only guard, fixed saved identity/consist and frame-loss
whole-train hold. Earlier timing failures were fixture-only; native input and
loading pass at tick65 after the cache settles. The example route also passed
native radius/roll eligibility during creation and produced four ready cars.

Private client :314/window2097160 joined the owned server, with default bindings,
raw input,1536M heap/6G limit; host had33GiB available. Relative device turn yielded
camera yaw4.65/pitch3.0; the same client normally toggled the reachable vanilla
lever false→true (`occupied/vanilla-*.json`). Setup tp/lever creation is disclosed.
Actual seat aim then identified `fixed_inverted_frame[part=7]`, not a cushion;
no Use was sent. Source diagnosis: FrameBlock inherited an invisible full-block
selection shape despite noCollision. The specific fix supplies only the visible
post/crossbar shape, retaining physical mass/noCollision. Accepted native evidence
carries; restart only to check the corrected normal board target. Client stopped
cleanly; server save requested; owned Xvfb remains for this continuing run.
Next action: wait for server inactive, package selection-shape correction, restart
server/client on same retained world, check fresh geometry/connection and actual
cushion hit, then one normal board and readback. Do not repeat generic input discovery.

## Corrected example drive layout

The selection outline fix passed: normal cushion Use mounted Dev in first native
chassis, then normal Close/Dispatch released the four-car train. At the first
quarter-roll the safety reservation stopped the entire restrained train before
a Creative Motor beside the90° anchor. Moving that motor to the opposite side
cleared the seat reservation but physically fouled the running gear. Stop was
then applied normally. No safety envelope or collision was weakened.

The corrected example uses one continuous native Chain span over the four
32-block roll edges, powered at the upright approach. Intermediate motors and
shafts are absent. The retained world was changed with disclosed NBT/setup
commands (`drive-repair-commands.txt` in the run), then saved and restarted to
refresh native Chain caches. Source example generates this same arrangement via
normal native commitChainLift API. This corrects a required playable-example
criterion; it does not reopen accepted native mass/topology or normal boarding.
The first banked save/reopen retained exact four-car poses and player seat,
restraints and held state; full-inversion/complete return still pending.
