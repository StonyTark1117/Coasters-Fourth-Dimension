# Wing and baseline comfort — current handoff

- **Owner/packages:** `01a1dd1d-1468-7f81-8f31-5532ad7a178d`; FD1101,
  FD501A, FD1101A–C, FD904C1 and FD503. Primary checkout
  `/home/braydon/Documents/Coasters: Fourth Dimension`, branch `main`;
  starting HEAD `a4ef96d775b389ae0a5e608ac80a37533c2592ac`.
- **Verified:** ownership published; ready earlier work committed as `f782540`.
  FD501 core decision reviewed and FD501A closed by no-change disposition.
  Wing fixed-seat family, chassis topology, eligibility, restraint/stop, dye,
  persistence and coupling implementation is in progress. No Wing runtime claim
  is accepted yet. Java and Gradle both use Java 21.
- **Exact next action:** prepare the owned packaged client/server for Wing's
  normal occupied handoff. Native Wing mechanics passed once; do not repeat it.
  Finish collector registration when its owner declares the file ready.
- **Failed condition / changed precondition:** qualified GameTest template was
  corrected to namespace-local `empty`. Corrected run passed all 1 required
  tests (fixed group, two-car native link/motion, stop, schema6, obstruction).
  Shutdown is still saving; thread inspection shows active I/O, not a test hang.
- **Dependency:** optional FD501 discovery no longer blocks Wing. Review the ready
  full catalog and Pipeline specification for canonical reconciliation.
- **FD502 bounded file handoff — granted:** dependency/tooling owner
  `01a1dd1d-c6a6-7be0-9cb5-2b033ac550da` exclusively owns creation/editing of
  `src/main/java/dev/fourthdimension/runtime/RuntimePerformanceRecorder.java`
  plus its dedicated scripts/evidence. Implement the proposed opt-in buffered
  frame/tick and heap/GC collector there. Lead owns registration in
  `FourthDimension.java`; no other shared file is transferred. Coordinator
  should relay this explicit handoff. Verify event delivery during the first
  useful Wing run; no separate park campaign.
- **Runtime:** native test PID `575691` is in clean save/shutdown, launched by
  Gradle session `24325`; log `native/template-corrected.log`, default persistent
  build `/home/braydon/.local/state/fourthdimension-7af457c2/build`. No client live.
  Reserved next run: `/home/braydon/.local/state/fourthdimension-runs/fd1101-wing-20261114`,
  display `:310`, game/RCON `25630/25631`, units `fd1101-wing-20261114-*`;
  client heap1536M/scope6G, server heap2048M/scope4G. Host available32GiB;
  parent memory limit unlimited, ~20GiB used; persistent XFS has263GiB free.
- **Evidence:** [prior-change inventory](prior-change-inventory.md);
  [Wing specification](../../../design/0.5/FD1101-wing.md).

## Acceptance boundaries

FD1101A supplies the occupied normal boarding, motion, station stop and unload
proof reused by C. B supplies the original assets, craftable item, controls and
playable example. C adds only missing construction, coupling, clearance or
restoration claims. Comfort C1 follows the integrated fixed-seat proof and
changes client view only. FD503 reconciles its eleven specifications, FD501
decisions, FD501A/FD502, Wing A–C and C1; it adds no runtime sweep.
