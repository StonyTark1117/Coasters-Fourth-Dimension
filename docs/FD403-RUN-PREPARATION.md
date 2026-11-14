# Scoped FD403 client preparation

Start from the [current assignment](CURRENT-QUALIFICATION.md#fd403-construction-and-teaching-cases)
and [acceptance procedure](FD403-ACCEPTANCE.md). Apply [VALIDATION-POLICY.md](VALIDATION-POLICY.md)
and [AGENTS.md](../AGENTS.md). This runbook prepares a named missing observation;
it does not require a fresh world, full build, all-family replay or second client.

The former Revised/PAX preparation was a historical scenario. Its blanket fresh-world,
two-client and whole-world-hash instructions are superseded here. PAX acceptance is
closed; consult its retained evidence instead of executing that old preparation.

## Choose the observation and reuse its inputs

| Missing claim | Use | Retain as evidence |
| --- | --- | --- |
| Normal crafting, paid connection or control interaction | One connected client; existing survival inventory/world; native checks support mechanics | Before/after inventory or state, visible interaction and relevant log excerpt. Reuse previously accepted recipes and unchanged input paths. |
| Arrow full circuit / family operating cycle | One connected rider and a prepared route; [Arrow handoff](FD403A-CIRCUIT.md) | Intended chassis identity, normal board/restraint/dispatch, route progress, family motion and final capture/unload. A normal Chain check on one span does not require reconstructing every accepted span. |
| Changed instructions or captions | Existing operating evidence plus affected UI/content | Changed wording and a readable affected view. No second ride for FD403F. |
| Uncovered final state after circuit/reopen | The completed route's saved world and one reconnect | Relevant rail/profile/vehicle/carrier identity and usable final state before/after. Preserve the world once; do not collect every subsystem at every intermediate click. |
| Specifically unresolved two-player interaction or synchronization | Two real clients with separate displays, game directories and memory limits | That interaction only. A second observer is not the default for Arrow. Closed FD402/PAX multiplayer cases remain closed. |

A fixture may supply upstream ingredients and power as already declared by the
acceptance scope. Command-created Fourth Dimension outputs or injected ride
operations do not prove normal construction/operation. Tooling checks may use
clearly disclosed setup poses or blocks; keep their credit separate from player
acceptance. Whole-world copies preserve recoverability; hash them only when the
identity/integrity claim needs it, not as a condition for every observation.

## Retained Arrow run

Use the [FD502 handoff](evidence/0.4/fd502-execution-repair-20261113/README.md)
for exact current paths, owner, input proof and circuit readiness. The helper's
local `run.json` describes resources, not package status. A named owner remains
required before starting or controlling a run. Do not adopt another task's live
runtime by changing its JSON owner. An explicit runtime handoff must precede
such a change; otherwise make an independently owned copy of a stopped save.

The helper is [`scripts/fd-private-client.py`](../scripts/fd-private-client.py).
It uses systemd services to keep the owned display and direct Java processes
alive across shell calls, and checks PID/cgroup, working directory and window
ownership before sending input. It never uses host `ydotool` or `/dev/uinput`.
It refuses an already active unit rather than restarting it after a timeout.

A run JSON contains the absolute persistent `root`, owner thread ID, reserved
`display`, unique `unit_prefix`, player name, and `client_script` / `server_script`
paths. The root contains `client/`, `server/`, `build/` and `evidence/` directories;
the saved world is under `server/`, outside `build/`. Keep the private server on
`127.0.0.1` with a unique game port and RCON port. Its random RCON password belongs
only in its local `server.properties` (mode 0600); never include it in evidence.

## Prepare a missing launcher once

First check live processes, host/cgroup headroom and disk/mount as required by
AGENTS. Reuse a matching retained launcher/artifact. Only regenerate when it is
missing or its actual game directory, relevant code or inputs have changed.

For a new owned run, set `FD_RUN` to its persistent absolute directory, `FD_PORT`
to its reserved game port, and `FD_OWNER` to the assigned thread ID. Java selection:

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
"$JAVA_HOME/bin/java" -version
./gradlew --version
./gradlew createClientLaunchScript createServerLaunchScript \
  -PfdPackaged -PfdValidation -PfdClientMaxHeap=1536M \
  "-PfdConnect=127.0.0.1:$FD_PORT" \
  "-PfdBuildDirectory=$FD_RUN/build" \
  "-PfdClientGameDirectory=$FD_RUN/client" \
  "-PfdServerGameDirectory=$FD_RUN/server" --offline --no-daemon --console=plain
```

This prepares the packaged artifact and launchers; it does not run a test suite.
Inspect generated paths and the actual runtime mod list. ModDev's generated shell
scripts on this project do not inherit the `JavaExec.jvmArgs` heap setting. The
helper therefore applies `JAVA_TOOL_OPTIONS=-Xmx1536M` to its direct client Java
process (2048M for its server); verify the running JVM with Java 21 `jcmd <pid> VM.flags`.
Default client/server service limits are 6G/4G. Follow AGENTS for measured adjustments.
Keep Raw Input On, Use=right mouse and Attack=left mouse in the saved client options.

Launch each component once, inspecting readiness between commands:

```sh
python3 scripts/fd-private-client.py "$FD_RUN/run.json" --owner "$FD_OWNER" start server
# Read evidence/server.log for Done and the loopback RCON listener.
python3 scripts/fd-private-client.py "$FD_RUN/run.json" --owner "$FD_OWNER" start xvfb
python3 scripts/fd-private-client.py "$FD_RUN/run.json" --owner "$FD_OWNER" start client
python3 scripts/fd-private-client.py "$FD_RUN/run.json" --owner "$FD_OWNER" status
```

The status command requires the configured player in the live server player list,
checks loaded controls and reports current window geometry. Also inspect the
screenshot: a connected player list alone does not prove the desired screen or
interaction. If startup is still running, inspect the same service/log; don't
launch another copy. A disconnect calls for checking the server and reconnecting
to the recorded endpoint, not changing mouse settings.

## One action, then its readback

```sh
python3 scripts/fd-private-client.py "$FD_RUN/run.json" --owner "$FD_OWNER" snapshot
python3 scripts/fd-private-client.py "$FD_RUN/run.json" --owner "$FD_OWNER" \
  input turn 50 0 --expect 'Camera yaw changes by about 7.5 degrees at sensitivity 0.5'
```

`turn` uses **relative XTest device motion** on the owned display's discovered
XTEST pointer. Its exact successful call is retained in the handoff. `move X Y`
is absolute pointer positioning for menus; it is not the proven camera method.
Before a world click, verify current target, reach, unobstructed face, held item
and required vehicle state. The helper cannot infer those from a live process.

```sh
python3 scripts/fd-private-client.py "$FD_RUN/run.json" --owner "$FD_OWNER" \
  input use --expect 'The specific visible message or state change for this target'
```

`use` sends right-click (X11 button **3**) for boarding, placement and tool use.
`attack` sends left-click (button **1**) for deliberate breaking/attacking; it can
instantly remove the target in Creative. Both take no value. The older
`click 3` / `click 1` syntax remains available, including left-click menu
selection with `--menu`. Input records name the resolved button and binding.
Neither command repositions the pointer. Inspect the returned before/after
screenshots and the stated state/log result before another action. `key` sends
window-targeted 180 ms key presses; `--modifier Shift_L` holds Shift for one
action and releases it afterward. `type` types into an already open field. For chat, send
`key slash`, inspect the opened field, then type the command **without another
leading slash** and press Return. `--menu` is only for an explicitly identified
menu/connection action; it does not certify in-world interaction.

The vehicle editor now waits for a server result before allowing another edit.
Routine operations (including Close restraints) refresh the same screen; accepted
Dispatch returns to gameplay. Rejected actions keep the editor open with the
reason and refreshed state. Inspect that result before the next operation.
Older retained JARs close the screen after every operation: reopen on those
artifacts, without mistaking their behavior for the updated editor.

`--menu` and `--expect` describe intent; neither detects an open screen. Never
click a saved menu coordinate after returning to gameplay. The old diagnostic
`FD_EDITOR_DISPATCH` meant **editor opening**, not ride Dispatch. Updated builds
use `FD_EDITOR_OPEN` and `FD_EDITOR_RESULT` (ride Dispatch is edit action 13).

Change a Create Creative Motor's speed through its normal value control. Live
`data merge` of `Speed` or `ScrollValue` does not execute the normal speed-change
callback and can leave the kinetic network inconsistent. Read back both the
motor and driven anchor after a change; a stored UI value alone is insufficient.
Determine direction through the complete pinned drive path, including the cart
velocity calculation, rather than from the motor's facing or chain animation
alone. The [Revised diagnosis](evidence/0.4/fd403d-revised-blocker-20261114/review.md#motor-and-editor-lifecycle-diagnosis--2026-11-14)
records the retained route's specific sign calculation and corrected next action.

Create's “Click and hold to edit” requires **right/Use held**, not Attack or a
short Use tap. With the reachable value control confirmed, inspect it safely:

```sh
python3 scripts/fd-private-client.py "$FD_RUN/run.json" --owner "$FD_OWNER" \
  input hold-use --expect 'Create value screen, then canceled without changing the setting'
```

This holds right for 750 ms, saves `during` (the open value screen), sends Escape
and releases in cleanup. Inspect `during` and the current window geometry. To
select, repeat `input hold-use X Y --expect 'Named setting and driven state'`,
using the menu coordinates established by that inspection. It records the held
screen and selection, then releases right to submit. It does not infer a valid
control from `--expect`; verify the authoritative motor/anchor readback. The
helper cancels and releases if a capture fails; it never uses Attack for this
flow. Create's kinetic value board uses its first row for negative values and
second row for positive values. The motor's displayed sign may differ from its
world-axis speed depending on facing.

Read-only server queries use `console 'data get ...'` or another named query;
setup mutations through console must be disclosed. Input/console records are
appended to `evidence/input.jsonl` and `console.jsonl`. No screenshot alone
proves a mutation reached its handler. After two inconclusive attempts, diagnose
the named failed condition; do not relaunch or experiment with bindings again.
A verified correction permits a targeted retry, not a fresh campaign.

## Recover a moving-body target without angle guessing

Use this when a retained vehicle has moved since its last successful interaction.
These are setup/diagnostic procedures, not extra acceptance gates. Keep accepted
boarding and camera-control evidence; a disclosed setup teleport earns neither.

1. Identify the intended chassis/carrier UUID from the current case. Query the
   live world from the player context, for example
   `execute as Dev at @s run sable info @e[distance=0..40,limit=20,sort=nearest]`.
   Match the UUID, not merely the nearest body or a similar-looking assembly.
   Console-origin `@n` and `tp Dev ~ ~ ~` do not use Dev's position. Old saved
   poses and screenshots are historical after movement; a Sable body position
   is not necessarily the seat's selectable point.
2. Recover the seat's **sublevel-local block coordinate** from its existing
   boarding record or current block data. The inherited seat selection shape
   occupies the lower half-block, despite its taller visible back. For a seat
   at `(LX, LY, LZ)`, use `(LX+0.5, LY+0.25, LZ+0.5)` as a candidate point.
   Verify its body with `execute positioned <local point> run sable info @i`.
   Stop a mismatched UUID here; do not try another arbitrary camera angle.
   If recent diagnostic inputs may have broken a block, verify the seat itself
   still exists as well: containing-body identity does not establish that.
3. Choose a clear, supported world standing position within normal reach of
   that seat, accounting for eye height. If the assigned scope permits a setup
   teleport, the pinned Sable 2.0.3 command path can aim at the **live** target:
   `execute positioned <local point> out_sub_level @i anchored eyes run tp Dev <world feet X Y Z> facing ~ ~ ~`.
   `out_sub_level` applies the containing body's current `logicalPose` to the
   local point. No manually copied quaternion or old world target is needed.
   This command moves only the player; do not substitute `sable tp`, which
   moves a body. Select/record any allowed temporary support before teleporting;
   falling or levitation changes the eye position and invalidates the aim.
4. Read the resulting F3 hit, actual player position and reach before one
   normal input. An accepted teleport response alone does not prove a seat
   hit, clear ray or valid reach. If it misses, inspect the named blocking
   geometry, pose synchronization or standing position and correct that cause.
   Keep the proven mouse settings. After the expected interaction succeeds,
   proceed directly to the unresolved route/action observation.

The transform command is established by the pinned dependency's
`SubLevelArgumentType`, `SubLevelSelector` and `ExecuteCommandMixin`; each run
still records its actual target/readback. Save the successful local target,
body UUID, supported approach and timestamp in that run's existing handoff so
compaction does not restart targeting discovery. A helper-mounted ride or
injected operation does not replace a still-open normal-input claim.
The [Free Spin recovery record](evidence/0.4/fd403c-context-recovery-20261114.md#target-recovered-and-normal-boarding-verified--0642-utc)
records a live F3 seat hit and successful normal mount using this transform path.

## Finish and hand off

Stop the observation when its named result passes. Record the smallest evidence
that establishes that claim, with actual artifact/world identity and any setup
limitations, then update the existing queue. Link accepted observations instead
of recopying their entire bundles. Current runtime state and next action belong
in the queue; detailed failures belong in the linked run record.

For an uncovered reopen claim, save/stop the server cleanly and wait for the
actual process to exit before copying its world. Reopen that retained world,
inspect the required identities/state and reconnect normally. Do not demand a
new cold reopen for an unchanged claim already accepted elsewhere.

Quit the owned client normally and stop the owned server cleanly. Confirm the
server's final save and terminal process state. Stop only this run's Xvfb and
units after the clients exit, preserve the world and relevant evidence, and mark
the runtime stopped in its handoff. No other agent's process or world is cleanup.
