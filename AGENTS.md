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
