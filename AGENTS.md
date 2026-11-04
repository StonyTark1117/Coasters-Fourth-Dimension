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
