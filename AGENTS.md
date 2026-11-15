# Branch and worktree policy

`main` is the authoritative integration branch for this project. Continue work in the
worktree and branch already assigned to the task. A new session or context compaction
does not call for a new branch.

Before editing, run `git status --short --branch` and `git worktree list`. If the
current worktree already has a branch, use it, including when its name belongs to an
earlier phase of the same task. Do not create another branch merely to get a clean
tree, change task names, or avoid reconciling existing work. Preserve and inspect
uncommitted changes before integrating anything.

The detached worktrees left from the 2026-11-04 archive are historical snapshots
with ignored run data. Start new work in the primary `main` checkout; do not
create a branch from a detached archive worktree.

For work done directly in the primary checkout, use `main`. For genuinely parallel
work that needs a separate worktree, create one stable task branch only if an existing
task worktree cannot be reused. Record its branch and absolute worktree path in every
handoff and compaction summary, along with its HEAD and uncommitted files. Resume that
same worktree after compaction. When the task is ready, integrate its commits into
`main` and report any remaining uncommitted work. Do not treat historical `codex/*`
branches as newer than `main` based on branch name or commit date alone; compare their
merge base, commits, and working tree first.

The former `codex/*` branch tips were reviewed and preserved in
`archive/branch-consolidation-2026-11-04`; see
`docs/BRANCH-CONSOLIDATION.md`. That archive is historical evidence, not a
starting point for a new task. Do not delete a branch or worktree with unique
commits or uncommitted changes without preserving its state first.

# Test scope and stopping

Follow [docs/VALIDATION-POLICY.md](docs/VALIDATION-POLICY.md) before scheduling
tests. Reuse accepted evidence unless a relevant change or failure invalidates
it; a new JAR hash, documentation edit, or test-only fixture does not reset the
roadmap. Run the smallest affected check once, then stop when it passes. Full
profile sweeps belong to release qualification, not every task. Do not turn
historical evidence limitations into new acceptance requirements. After two
inconclusive attempts with the same approach, diagnose before another launch.

Closed roadmap cases stay closed across builds. Reopen only the specific case
whose accepted behavior a named code/input change or new defect invalidates;
do not reset its siblings or parent gate. A passing targeted fix closes that
case again without restarting qualification. Combine compatible evidence to
finish whole tasks; do not keep them open merely because individual records
describe bounded “slices.”

Start from the current remaining-case queue in
[docs/CURRENT-QUALIFICATION.md](docs/CURRENT-QUALIFICATION.md#current-work-queue),
confirm ownership, and update it as cases close. Historical “still open” notes
are not assignments. Name the violated required criterion before treating a
finding as a gate blocker; nonblocking polish stays outside acceptance. New
shared-contract mechanics include the roadmap's early occupied-client proof,
which later feature acceptance reuses rather than repeats.

Use the package's linked [milestone execution guide](docs/roadmap/README.md)
for ordered steps, inputs and handoffs. `ROADMAP.md` remains authoritative for
scope, prerequisites, status and acceptance; the current queue owns assignments.
Guide steps are procedures within a complete outcome, not new acceptance gates,
mandatory runs or a reason to stop after an arbitrary slice.

Keep a named-package assignment within that package and its required children.
In particular, completing FD402 means ordinary-bearing recovery; FD403 owns
player construction and teaching, and FD404 aggregates both. Neither sibling
is a prerequisite for the other. Use the [0.4 scope boundary](docs/roadmap/0.4-core.md#choose-the-assigned-package)
before claiming work; a busy owner or blocked test does not expand the assignment.

# Java runtime

Use **Java 21** for every Gradle command, build, GameTest, server, and client run
in this project. On this development host, set
`JAVA_HOME=/usr/lib/jvm/java-21-openjdk` and put `$JAVA_HOME/bin` first in `PATH`
before invoking `./gradlew` or `java`. Check `"$JAVA_HOME/bin/java" -version`
and `./gradlew --version` when starting a run or troubleshooting a launch; both
must report Java 21. The Java toolchain in `build.gradle` does not by itself
select the JVM that starts Gradle.

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
"$JAVA_HOME/bin/java" -version
./gradlew --version
```

Do not use the shell's default `java` without checking it: it may resolve to
Java 27 on this host and crash the Minecraft tooling. If Java 21 is missing or
Gradle still starts on another JVM, fix `JAVA_HOME` and `PATH` before retrying
the project command. Carry this Java selection into private Xvfb runs,
`systemd-run` scopes, scripts, and every child process that launches Java.

# Minecraft testing displays

Run builds, GameTests, servers, and Minecraft clients from this project checkout
on the development host. `runGameTestServer` and other command-line checks do not
need a display. For any graphical client test, start an agent-owned private Xvfb
display on this host and set `DISPLAY` explicitly for the client and every input
or screenshot command. `xvfb-run -a` is suitable for a self-contained automated
client run; interactive runs need a persistent, uniquely reserved Xvfb display.
Confirm that the display belongs to this task before sending input, and never
reuse another agent's display.

Agents may run graphical clients and send live input concurrently when each
task owns its own private display, client game directory, world/server port,
and process scope, with enough host memory for both. Another agent using input
is not a global input lock and is not a reason to defer an independent assigned
case. Coordinate a handoff to reuse or change the same client, display, or
server/world. If the user explicitly assigns parallel work on the same
acceptance case, the agents may run separate copies concurrently: record each
run's owner, scope, paths, display, port, and evidence, then reconcile the
results before closing the shared case. Case coordination avoids duplicate or
conflicting claims; it does not forbid an independent private run. Verify
resource ownership before acting; do not send input to or stop another task's
runtime. A display handed to another task stays theirs until handed back, but
that does not prevent starting a different owned private display.

Do not launch Minecraft on the user's desktop or control the user's mouse,
keyboard, windows, or browser for project testing. Do not move this Minecraft
workflow into ChatGPT's built-in Agent Workspace: it does not provide this
project's Gradle, mod, and runtime setup. If a graphical test cannot start,
inspect and repair the private Xvfb setup on the development host; report the
specific blocker if it remains.

Give concurrent clients distinct `-PfdClientGameDirectory=/absolute/path`
values, and reserve separate server ports and worlds. Keep the owned display
alive until its clients exit, then stop only the processes and display started
for this task. Preserve requested logs, screenshots, and worlds before cleanup.
See `docs/FD403-RUN-PREPARATION.md` for the packaged client and server procedure.

Before reporting a goal or case complete, stop its owned client, let its server
save and exit cleanly, then stop its owned Xvfb display and any run services or
scopes. Verify their PIDs or units are inactive and report any process that
could not be stopped. Preserve the saved world, logs, and evidence needed for
reopen or handoff. An idle live runtime is not a handoff: keep it running after
completion only when a named continuing task has explicitly accepted ownership
of the exact client, server/world, display, and units. Apply the same cleanup
when abandoning or blocking a run with no active owner. Do not stop another
task's active processes; ask its owner to clean up a completed or abandoned run.
If the owner is unavailable and a completed case left an orphaned unit, a
coordinating agent may stop that unit after confirming the case is closed, no
client or other task is attached, the world and logs are saved, and the exact
unit belongs to that completed run. Record the verification and what was
stopped; do not use this exception on a live or ambiguous run.

Before diagnosing a failed in-world input, confirm the client is still in the
world and the dedicated server log shows that player connected. A live Java
process or visible Minecraft window does not prove an active connection: the
client can sit on “Connection Lost — Timed out” indefinitely. When it has
disconnected, check the server process, listening port, and latest server log,
then reconnect to the same world before resuming the interaction. Direct
Connection does not save an entry in Minecraft's server list; use the recorded
endpoint or read `server-ip` and `server-port` from that run's `server.properties`
instead of expecting a saved entry. Confirm the new join in both client view
and server log. If it times out again, diagnose that connection separately from
mouse input and preserve the world.

For interactive input on a private Xvfb display, identify the Minecraft window
on that exact `DISPLAY` and check its current geometry after every process
restart, resource reload, or resize. Recalibrate screen and window-relative
click coordinates from a fresh screenshot before clicking a control. These
displays may have no window manager: `xdotool windowactivate` can fail with
`_NET_ACTIVE_WINDOW` unsupported. Use the owned window ID with `windowfocus`
and window-targeted key/type events instead. Confirm focus and the resulting
screen or log after a command before chaining more input. On this setup, an
unqualified `T` has opened Social Interactions instead of chat; the window-
targeted `/` command key avoids that mapping. Do not infer display ownership
from an old display number or service name; verify its live PID and cgroup
before treating it as another agent's active client or stopping it.

For automated Xvfb mouse input (`xdotool`/XTest), check the private client's
`options.txt` before launch and after startup. Keep
`key_key.use:key.mouse.right` and `key_key.attack:key.mouse.left`; do not
assume copied or restored options retained those bindings. A diagnostic remap
must be recorded, reverted, and verified before interpreting normal-use results.
Restart after changing mouse settings or bindings. Test pointer movement and
button clicks **separately in the same live client**: confirm a visible camera
turn, then a reachable vanilla in-world right-click or item placement, before
testing mod interactions. Menu clicks or a changed cursor alone do not prove
in-world use. F3 naming a targeted block does not prove it is within interaction
reach: check the player-to-target distance and reposition before clicking. If
placement has no visible effect, check for obstructing track or blocks before
changing mouse settings again.

The later 2026-11-12 FD403 run on private Xvfb `:298` proved both camera turning
with **absolute XTest pointer motion** and normal in-world right-click with
`rawMouseInput:true` and default bindings in the same live client. It also
placed a Creative Motor by right-click. This supersedes earlier inconclusive
claims that Raw Input On prevented item use. Resume that run with those proven
settings; do not toggle Raw Input, remap controls, or repeat a generic mouse
diagnostic after an interaction miss. A failed click now calls for checking
focus, F3 hit identity, reach, obstruction, selected item, and relevant game
state (such as seated status, restraints, or an existing Chain span).
For each Wrench, Chain, station, seat, or other state-changing click, name the
expected visible/log/world-state result first. Send one input, read that result,
and stop if it did not change. Do not batch clicks or repeat a decompiled
interaction sequence as a substitute for proving that its event or packet
arrived; inspect the handler guards and current state before any retry. Read the
newest run evidence before reviving an older control explanation that later
observations superseded.

For a new run, establish one configuration that passes both camera turning and
reachable in-world use, then keep it fixed. If one F3 aim attempt misses, stop
repeating pointer warps or `/tp` angle guesses: inspect player position, target
geometry, and intervening blocks, then make one calculated correction and
confirm its F3 hit before clicking. If that still fails, preserve the state and
diagnose the target or game interaction before repeating that approach. Once a
specific cause is found and the next setup changes the failed condition, a new
targeted client attempt is allowed; this is not a permanent launch ban. Record
the corrected preconditions and expected readback before that attempt. A disclosed
aim/position setup command is allowed only when the assigned acceptance permits
it; it does not prove normal player camera control.

Do not use the host `ydotool` daemon or `/dev/uinput` as a shortcut for Xvfb
mouse control. `ydotool` emits kernel device events; setting `DISPLAY` does not
route them to the private Xvfb server, and they may reach the user's desktop.
The presence of `ydotool` or a writable `/dev/uinput` is not proof of isolated
input. Any replacement input method must be demonstrably bound to the owned
private display before it sends a button or motion event.

The current executable procedure is [scripts/fd-private-client.py](scripts/fd-private-client.py),
verified in the [FD502 handoff](docs/evidence/0.4/fd502-execution-repair-20261113/README.md).
Its `turn` uses relative XTest **device** motion on the owned display's discovered
XTEST pointer; its `move` is menu-pointer positioning. Keep those calls distinct
from older shorthand descriptions of “absolute pointer” input. Reuse the proven
procedure and check the actual target/state; do not repeat generic input discovery.

Set server-side mod configuration before launching the client/server process;
changing it in a running integrated server may require a clean process restart
to take effect. In singleplayer, the integrated server can pause while an
editor is open, delaying scheduled fixture commands until the screen closes.
For a timed GUI check, use a dedicated server or a private LAN-published world
and confirm server ticks advance while the editor remains open. For the FD402
manual Engineered recovery fixture, a stationary motor needs a fixed `FORWARD`
LAUNCH heading; a `ROLLING` heading has no initial motion direction. Check the
saved controller sequence and actual FE draw before attributing a zero-motion
result to recovery logic.

# Build and run storage

On this host `/tmp` is `tmpfs`: files there consume RAM and can disappear after
a reboot. Do not put Gradle builds, Minecraft game directories/worlds, long run
logs, screenshots, extracted runtime JARs, or evidence that must survive the
task in `/tmp`. The default build for this colon-containing checkout is a
persistent, colon-free path under
`/home/braydon/.local/state/fourthdimension-<path-hash>/build`; read the actual
`buildDir` from Gradle rather than guessing the hash. For isolated runs, pass
`-PfdBuildDirectory`, `-PfdClientGameDirectory`,
`-PfdObserverGameDirectory`, and `-PfdServerGameDirectory` with distinct
absolute paths on persistent storage. Keep valuable worlds outside build
directories because `clean` removes build outputs and default run directories.
Before large runs, check the target mount with `findmnt -T /absolute/path` and
available disk space with `df -h /absolute/path`; also account for existing
`/tmp` use in host memory checks. Tiny disposable scratch files may use `/tmp`.
Preserve any still-needed data from old `/tmp` runs before reboot or cleanup;
never delete another agent's run directory.

If disk space is low, inspect the project-owned build and run directories with
`df -h` and `du -x -h --max-depth=2` before starting another large build or
client. Reclaim space from completed runs you own: first preserve requested
evidence and any world needed for continuation, confirm no process still uses
the directory, then remove disposable game copies, logs, extracted artifacts,
and generated build outputs. `./gradlew clean` is appropriate only when the
selected build directory is idle and its default run worlds are no longer
needed. Recheck free space afterward and record what was removed. Do not delete
tracked source/evidence, uncommitted work, unique Git history, shared Gradle
caches, archival worktrees, or another agent's data merely to free space.

# Current client memory policy

**The 3 GiB client process cap is retired.** For new runs, use a **6 GiB
Minecraft-only scope by default**. Older chat instructions, compaction notes,
launch scripts, and evidence describing a 3 GiB cap are historical run records;
they do not set the current limit. Do not rerun a fixture at 3 GiB merely to
make it "policy-conforming." If a task's measured client memory requires more
than 6 GiB, raise the scope to the measured need with reasonable headroom after
checking available host and cgroup memory. Agents may make that adjustment
without waiting for separate approval; record the chosen limit and measurement.

Before each client run, check current host memory (`free -h`), the active cgroup
limit and usage (`memory.max` and `memory.current` when present), and other
Minecraft, Gradle, and agent processes. Budget for every simultaneous client,
server, and the user's work. If the available headroom is insufficient, wait or
reduce concurrency rather than starting another client.

Use **1.5 GiB as the default game JVM heap** for `runClient` and `runObserver`
with `-PfdClientMaxHeap=1536M`. Put the **Minecraft client process and its
children** in an owned cgroup or systemd scope with the 6 GiB default limit.
Do not put the Gradle launcher or daemon in that scope: a bounded launch was
killed at 3 GiB before the fixture command ran because Gradle and Minecraft shared the
cap. A later live client exceeded 3 GiB RSS while its 4 GiB scope approached
the ceiling. Run the Java 21 Gradle preparation outside the client scope, using
`createClientLaunchScript` or `createObserverLaunchScript` with
`-PfdClientMaxHeap=1536M` and the intended game directory property
(`-PfdClientGameDirectory=/absolute/path` for the client or
`-PfdObserverGameDirectory=/absolute/path` for the observer), then run the
generated direct Java launch script inside the scope. Confirm that the script
uses the current build, the intended game directory, and a Java 21 executable.
After reserving display `:N` and an isolated game directory, the scoped client
step has this form:

```sh
systemd-run --user --scope -p MemoryMax=6G -- \
  env DISPLAY=:N ALSOFT_DRIVERS=null JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  PATH=/usr/lib/jvm/java-21-openjdk/bin:$PATH \
  /absolute/path/to/generated-direct-client-launch.sh
```

Replace the display and script placeholders, and use the corresponding observer
script where needed. The generated Java command must use the selected heap
limit (1536M by default); verify its JVM arguments before launch. Budget and
monitor the separate Gradle
preparation against current host/cgroup headroom too. For a memory-sensitive
world replay, lower render and simulation distance before entering the copied
test world, and record those settings with the run evidence.
A per-process heap flag or `ulimit` alone does not enforce the process-group cap.
Verify that the scope is active before relying on it; if no equivalent cap can
be established, do not run the graphical client unbounded.

Increase the game heap if Java heap pressure or an out-of-memory error shows
that 1.5 GiB is insufficient; increasing the process scope alone does not fix
Java heap exhaustion. Likewise, increase the process scope when measured RSS or
cgroup usage approaches its limit, without waiting for an OOM kill. Check
host/cgroup headroom, reduce concurrent runs first, and leave memory for the
user's desktop. Record the reason, chosen limits, and observed peak, then use
the defaults again for unrelated runs.
