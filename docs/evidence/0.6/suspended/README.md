# FD1301A–C Suspended — current checkpoint

- **Owner:** `01a1dd1d-6e99-70d2-aef3-6dcebac10d72`; complete Suspended mechanics,
  original content and player acceptance. 0.4/0.5 acceptance stays closed.
- **Checkout:** `/home/braydon/Documents/Coasters: Fourth Dimension`, existing
  `main`, dispatch HEAD `d73d43b820b725fc9648c6a2daa9348dead16b22`.
- **Current authority:** transferred shared Java paths (except Spinning* and
  SpinningGameTests), shared language, new Suspended content/tests/guide. Lead owns
  central docs and all Git operations. Transfer consumed from current queue.
- **Integrated prerequisite:** FD1501A at `d5565a09ac0d6230b66c9dd332c7b35fb3840f2a`;
  native loading/axis/capture and normal occupied cycle accepted, runtimes stopped.
- **Verified state:** shared integration compiles. Generic native Z hinge, overhead
  fit/supports, two-seat original cabin, native centered keel/rider loading,
  train-wide lift/dock state and persistence, items/recipes/example/guide implemented.
  Native empty mass4 and exact .8/rider plus asymmetric gravity response measured.
  Four-car native distance8/Chain/power-loss retention/explicit resume/rear-clear
  release/recapture passed at05:42 UTC. Do not rerun this closed native case. A
  occupied handoff passed through normal Capture/Open/Shift unload at06:01 UTC. Pending-entry unsafe-pose retention
  and saved state passed at05:42:50 UTC. Retained capture/open/pose-seat save passed
  at05:49 UTC after restoring the fixture's forced chunks.
- **Exact next action:** A is qualified; see [A handoff](A-handoff.md). Lead integrates
  before transferring shared files to Fixed. This chat explicitly retains the exact
  occupied runtime below for continuing FD1301B/C: diagnose the visible suspension
  art gap, finish assets/recipes and normal construction. Preserve the accepted
  occupied loop; no duplicate loop or native suite is assigned.
- **Failed condition / changed precondition:** initial capture repeatedly rechecked
  its five-tick guard after physical engagement; qualified latches now stay held
  while valid capture continues. A velocity-only limit probe never reached the stop.
  Its replacement now applies measured outward torque20I and reads native constraint
  reaction; gravity return alone is not acceptable hard-stop proof. Four-car exit
  released at1.75 while the rear still saw the passed lift in its2.83-block lookahead;
  signed travel now distinguishes approach from departure, including rollback.
- **Runtime identity:** legacy native test PID683000 (Java21) saved all dimensions and exited
  at05:39:28 UTC (15m58s including slow I/O). Its
  isolated build/world is `/home/braydon/.local/state/fourthdimension-runs/fd1301-native-20261115/build`;
  raw log `native/first-native.log`. No display. Main owned dedicated root:
  `/home/braydon/.local/state/fourthdimension-runs/fd1301-suspended-20261115`,
  `run.json` owner is this chat, server unit `fd1301-suspended-20261115-server.service`,
  ports25642/25643. Current world `SuspendedWorkshop2`; previous failed fixtures
  preserved in `SuspendedWorkshop`. Client reached initial welcome only, then stopped for rebuild; Xvfb owned on display312,
  corresponding `-client.service` / `-xvfb.service`,1536M heap/6GiB client.
  Helper `scripts/fd-private-client.py`; generated direct scripts under `build/moddev`.
  Server script adds `-Dneoforge.enableGameTest=true` and namespace flag. Current
  packaged compile evidence `evidence/pending-build.log` under the run root.
- **Run procedure:** use the helper with `--owner 01a1dd1d-6e99-70d2-aef3-6dcebac10d72`.
  Native commands: `execute positioned 500 90 300 run test run nativelimitsandcapture`,
  `execute positioned -300 90 300 run test run nativefourcarliftandinterruption`,
  `execute positioned 700 90 300 run test run nativependingliftinterruption`.
  Each fixture has distinct global coordinates600/-400/800; preserve existing failed
  worlds rather than placing duplicate cars over them. `/test run` places the small
  test template at surface Y=-64 in the void world; physical fixtures use their
  explicit Y100 and forced chunks.
- **Consumer:** Fixed Inverted owner `01a1dd1d-c6a6-7be0-9cb5-2b033ac550da`
  needs the qualified overhead contract and A occupied evidence once integrated;
  its fixed seats must not inherit Suspended's swing joint.

## Existing acceptance inputs

- [Accepted Suspended specification](../../../design/0.5/FD1301-suspended.md):
  overhead N contacts, Z-axis pivot at graph Y=-1.5, two seats, bounded ±45° swing,
  lift/dock locks, one to four cars at distance8, radius≥16/bank≤30°/slope≤60°.
- [0.5 shared conventions](../../../design/0.5/specification-conventions.md) and
  [native interfaces](../../0.5/specifications/native-interfaces.md): convert
  graph/design/native coordinates once; generic joint supplies real angular limits.
- Integrated Wing fixed-seat and general rider/lifecycle coverage is retained.
  Add only changed-axis/overhead/swing-lock/seat-hit claims needed by this package.

## Finite completion claims

A: actual native load-sensitive Z swing and physical limits/damping, supported
contacts/clearance/consist guards, lift lock acquisition/release and safe dock
capture, distinct interrupted lock restore, plus normal occupied handoff.
B: original recognizable bogie/hanger/cabin/seat/restraint/latch/track and item/
recipe/guide/example, FD916 finite visual criteria at relevant poses.
C: reconcile A/B with missing normal player construction/control and distinct
lifecycle claims; retain occupied handoff rather than replaying it. No hash sweep.
