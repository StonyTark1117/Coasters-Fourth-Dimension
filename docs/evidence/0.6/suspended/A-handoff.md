# FD1301A qualified handoff — 2026-11-15

Owner `01a1dd1d-6e99-70d2-aef3-6dcebac10d72`, primary checkout
`/home/braydon/Documents/Coasters: Fourth Dimension`, branch `main`, current
integration HEAD `0d7f6eda`. Changes remain uncommitted for lead review/integration.
Consumes integrated S1 `d5565a09ac0d6230b66c9dd332c7b35fb3840f2a`.

## Targeted correction qualified before integration

Occupied source exposed an isolated journal block removed by native structure splitting.
Its connected replacement at child offset(0,-1,0) keeps the same world pivot/model
geometry but adds one native unit of pivot mass. Only loading/bounds/capture is
qualified for that changed input; other evidence below remains accepted.
The replacement remained attached at55ticks, measured empty mass5 and
pivot inertia21.0171354166665, exact .8/rider delta, both loaded physical stops
(peaks .78579622/.78570787rad), capture, open and exact pose/seat save all passed.
See [journal raw pass](native/journal-correction.log), [build](native/journal-build.log),
[identity](native/journal-build.sha256) and `visual/journal-connected-*.png`.
The isolated old journal is not a supported persistent topology; prequalification
worlds are retained as evidence, while new player construction uses the connected part.

## Accepted evidence

- Native empty mass4, .8 per equal rider, asymmetric gravitational equilibrium:
  [native raw record](native/qualification-through-capture.log). Actual Generic
  constraint locks translation and X/Y rotation, bounds Z at ±π/4. The two-direction
  sustained outward-force trace reaches 45.03°/45.22° without exceeding tolerance;
  [force-budget interpretation](native/limit-interpretation.md) distinguishes the
  undocumented aggregate JNI reaction from the actual boundary proof.
- Four native cars at coupling distance8 pass powered lift retention, power-loss
  retained locks, persisted lock state, explicit Resume, whole-rear-clear release,
  and dock recapture. `FD_SUSPENDED_TRAIN` at05:42 UTC. Pending entry at30° remains
  stopped/restrained/pending/unlocked, preserves pose in save: `FD_SUSPENDED_PENDING`.
- Retained bodies pass capture, Ready, open restraints and exact pose/seat save:
  `FD_SUSPENDED_RETAINED_CAPTURE` at05:49. The old failed arbitrary reaction
  threshold is not an outstanding behavioral failure; capture has separate proof.
- Stale native-consist snapshot recovery is deliberate at a stopped level dock;
  moving rebind rejects without mutation. `FD_SUSPENDED_REBIND` at05:55. Ordinary
  bound-dock editor access remains available without invoking rebind.
- [Occupied trace](occupied/occupied-trace.jsonl), 180 samples: normal seat
  right-click, native rider UUID and seat assignment, tool controls, Close
  restraints, Dispatch, retained station-to-lift approach, physical locked lift,
  release after all rear cars, bounded free swing (sampled peak .49484rad), both
  native and world-stable rider views, normal Capture/Ready/Open/Shift unload.
  No fixture command boarded, dispatched, restrained, captured or unloaded the
  player. Route creation, initial player positioning and tool grant are disclosed
  setup, and earn no construction credit. Camera motion and vanilla lever click
  were separately proved with raw input and default bindings in this same client.
- Final rider readback: [unloaded](occupied/unloaded.json), passenger=false.
  Representative images in `occupied/` show each normal interaction state.
- Java21 packaged build passes (`rebind-build.log` in owned run evidence).
  Existing Original/Wing/S1 acceptance carries forward; no profile replay required.

## Consumer interfaces and remaining content

Shared native rail fit is `overhead_n_v1` with N contacts, graph-centered reservation
(half width3, above1.10, below4.70, fore/aft1.75). Suspended pivot is graphY=-1.5,
its single child Z hinge is family-specific. Fixed Inverted can reuse overhead
renderer/fit without creating that hinge. `TrackDesign.overhead()` currently
returns true for Suspended and is the intended Fixed extension point. Existing
families retain Rotary constraints, their loading branches and render paths.

A's dynamics and normal occupied operation are qualified. B remains open for
remaining inventory/recipe/placed-state review; the suspension-art gap is corrected;
C remains open for normal construction and any distinct interruption reconciliation.
The art gap does not detach the native child or rider; no A replay is assigned for
an asset-only correction. If diagnosis changes mechanics, reopen only that named
changed criterion. No claim that FD1301B/C is finished.

## File and runtime handoff

Integration set: all modified `src/main/java/dev/fourthdimension/` listed by current
`git diff --name-only` (no Spinning*.java or SpinningGameTests changes), new
`vehicle/Suspended*.java`, `gametest/SuspendedGameTests.java`, shared en_us,
Suspended-only assets/recipes/template, `scripts/generate-suspended.py`,
`docs/SUSPENDED.md`, and `docs/evidence/0.6/suspended/`. These include stable content
needed to run A; B corrections can follow separately. Exclude lead's central docs,
`docs/evidence/0.6/acceptance.md`, Fixed evidence, and untracked `logs/`.

**Named continuing owner:** this same Suspended chat explicitly retains the exact
runtime for FD1301B/C immediately after A handoff: root
`/home/braydon/.local/state/fourthdimension-runs/fd1301-suspended-20261115`, world
`server/SuspendedWorkshop2`, display`:312`, game/RCON25642/25643, units
`fd1301-suspended-20261115-{client,server,xvfb}.service`, run.json owner above.
Client1536M heap/6G cap, server2G/4G cap, Java21. No runtime ownership is transferred
to Fixed. Final Suspended completion still requires clean save and inactive units.
