# FD502 executable input and retained circuit repair — 2026-11-13

Owner: **Audit and improve roadmap**, thread
`01a1a961-64bb-7701-9a19-2acee5ff23a3`, primary `main` at `1c2564a8` plus
this task's tooling/planning edits and the previously present teaching edits.
The [current queue](../../../CURRENT-QUALIFICATION.md#fd403-construction-and-teaching-cases)
owns assignments. This record supplies evidence and a handoff, not a second tracker.

## Input repair verified

The checked-in [helper](../../../../scripts/fd-private-client.py) generated a
reproducible input record on owned private Xvfb `:252`, an 854×480 window,
Raw Input On, default left/right bindings and sensitivity 0.5:

- A core `XTestFakeMotionEvent` to `(477,240)` moved the pointer but left yaw
  at 90°. This is retained as the failed method; it is not the camera procedure.
- `input turn 50 0` enumerated this display's **Virtual core XTEST pointer**
  (device 4) and called `XTestFakeDeviceMotionEvent` with relative axes `(50,0)`.
  Server `data get entity Dev Rotation` changed from `[90.0f,29.3f]` to
  `[97.5f,29.3f]`; [before](camera-before.png) and [after](camera-after.png)
  show the same turn. Device IDs are discovered, not hard-coded.
- In that same live client and unchanged controls, setup moved the player to
  `(280.5,-60,30.5)`, yaw 0°, pitch 60°, and supplied one upstream Stone in
  hotbar slot 9. F3 identified reachable grass at `(280,-61,31)`. A single
  `input click 3`, without a pointer warp, placed Stone at `(280,-60,31)`.
  [The screenshot](normal-stone-placement.png), empty selected item and the
  server's `execute if block 280 -60 31 minecraft:stone` → `Test passed` agree.

These observations prove camera movement and ordinary item use for the retained
procedure. They do not prove a full Arrow ride, new player construction, or
that every future target is reachable. Every action is tied to the run owner and records its window,
geometry, expected result, actual arguments and before/after screenshots.
Connection is checked against the owned server's live player list before
in-world input. A process/window alone is insufficient.

## Runtime and source world

Persistent task directory:
`/home/braydon/.local/state/fourthdimension-runs/fd502-execution-repair-20261113`.
Its `run.json` records this owner, `:252`, and the `fd502-execution-20261113-*`
server/client/Xvfb units. Server/RCON use loopback ports `25602/25603`.
RCON's local password stays in the private server properties, not this repository.
The packaged Java 21 preparation succeeded; client/server processes use separate
6 GiB / 4 GiB limits and 1536 MiB / 2048 MiB Java heaps. Gradle ran outside them.

The source was the stopped `fd403a-root-parallel-20261113/server-game/FD403ArrowRootParallel`
world, copied to `server/FD403ArrowRetained`. Its 22 rails and 15 vehicle records
were decoded before changes. The original source is untouched. FD403's active
`:248` and `:250` runtimes are not owned by this task.

The working copy's two obstructing return-side chassis were identified by exact
UUID and live position, named locally, and removed through Sable's removal path:
`7a1295c5-2e6e-48e5-9925-8e4068d1de76` and
`b83842c8-ef61-4b5f-b732-884c2ea322e2`. Their original identities and bodies
remain preserved in the untouched source. This disclosed setup cleanup earns
no player removal/recovery acceptance. The intended normally placed car is
`819ce77e-8cd5-4eaf-831a-c45de09908cb` on the 50 m profiled leg.

## Circuit preparation completed

The [circuit record](../../../FD403A-CIRCUIT.md) gives exact geometry, working
power, intended car, saved boarding pose, control sequence and remaining claims.
The preparation traversals identified and corrected an unpowered G–F leg and
an incomplete/opposing C–D drive. F's new Chain used normal client input;
C's extension and the creative-motor changes were disclosed setup commands,
not placement acceptance. After the C source correction, the occupied car
traversed C–B and returned to A–B at approximately 1 m/s.

Normal client Capture, Open restraints and Shift left the car at
`(230.5,-58.88,34.52)`, Ready, restraints open and unloaded.
[Captured Ready](return-captured-ready.png) and [reachable open seat](final-open-seat.png)
show the result. The player is saved at `(237.5,-60,34.209)`, yaw 90°, pitch −6.63°,
with empty hand selected. [Saved state](preparation-state.json) preserves the
actual JAR identity, original bound profile/carrier identities, no fault,
boarding readiness and empty occupants. The saved state was decoded after the
clean server exit; this is not a new cold-reopen pass.

The route legs were verified cumulatively, with targeted repairs and captures.
This is a usable preparation handoff, not one uninterrupted FD403A acceptance
circuit. FD403's named owners retain their assignments and use the remaining
claim table. In particular, the command-extended C–D span needs normal client
placement evidence if this copy is used for that claim; unaffected spans do not.

All three owned services are stopped. The client exited through Quit Game;
the server logged all dimensions saved and exited successfully. Sable's final
save took about 42 seconds: a slow save is not a reason to kill/relaunch it.
The private display was stopped only after the client exited. To consume the
world, obtain an explicit runtime handoff or copy this stopped save into an
independently owned run; do not change another owner's live run record.

## Verification and process findings

- Live Java 21 inspection confirmed client heap 1536 MiB / cap 6 GiB and server
  heap 2048 MiB / cap 4 GiB. Measured service peaks were approximately 2.36 GiB
  and 1.36 GiB respectively. Generated direct launchers needed an explicit
  `JAVA_TOOL_OPTIONS` heap setting; the Gradle JavaExec setting alone was absent.
- `python3 -m unittest discover -s scripts/tests -p test_private_client.py -v`
  passed all six focused checks: fragmented/multipart RCON, disconnect handling,
  wrong-owner/cgroup-prefix refusal, disconnected-player input refusal, and failed-input
  recording with modifier release. No general GameTest/profile suite was run.
- This was primarily an execution-process and fixture-readiness problem.
  Camera input lacked a retained executable procedure; the nominal loop had
  obstructing cars, missing drive coverage and opposing power; the old runbook
  applied a historical two-client/fresh-world setup too broadly. FD502 existed
  but had no explicit owner repairing these blockers for FD403.
- The planning correction is a named FD502 repair/handoff and a precise FD403A
  remaining-claim table. Keep FD403A's complete outcome and permanent ID. Do not
  turn these diagnostics into new gates or require every future agent to repeat
  this preparation.

## Retained diagnostics

Committed [input](input.jsonl), [console](console.jsonl) and motion records retain
the relevant actions; screenshot paths inside those logs refer to the persistent
run. The task directory's `evidence/` contains `prepare.log`, server/client logs,
`input.jsonl`, `console.jsonl`, exact before/after images,
`initial-foundation.json`, `initial-chain-anchors.json`,
`copy-obstruction-removal.jsonl`, and `prior-fd403-queue.md`.
The latter preserves the old long queue narrative; it is historical context.
Earlier detailed failures remain in the November 12–13 run directories.
Successful input calls above supersede ambiguous “absolute pointer” advice
for this helper; no earlier acceptance is reset.
