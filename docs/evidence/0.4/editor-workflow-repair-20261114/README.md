# Editor workflow and held-use repair — 2026-11-14

Owner: **Audit and improve roadmap**, `01a1a961-64bb-7701-9a19-2acee5ff23a3`,
primary `main` at `549b130b` plus the recorded working-tree changes. The human
requested repairs to both the editor's repeated reopening and the test setup.
This is a focused interaction check, not a new family acceptance campaign.

## Implementation

Routine editor actions now remain on the same screen. An outstanding request
disables further edits until the server sends its result after the current
snapshot. The screen then refreshes its revision and conditional controls.
Rejected actions show the reason, retain the draft and allow review/retry;
accepted Dispatch closes the editor. Late results cannot reopen a closed screen.
The existing authority, reach, restraint and stale-revision guards remain.
Editor payload version 8 requires matching updated client and server artifacts.

The diagnostic names distinguish `FD_EDITOR_OPEN` from
`FD_EDITOR_RESULT action=13 success=…`. The old `FD_EDITOR_DISPATCH` merely
meant opening the screen; it never proved a ride dispatch.

The private-client helper adds `input hold-use`: inspect the Create value screen
while holding right/Use, then Escape-cancel and release. With two menu coordinates
it selects while held and releases to submit. Before/held/selection/after images
are retained. Failed captures cancel before release; all button paths release
in cleanup. Owner, display, window, binding and connection checks remain active.

## Verification

Java 21 compiled the packaged JAR and generated direct launchers successfully.
All 12 focused Python checks passed, including held selection, cancel-before-release,
release after capture failure, malformed selections, wrong-owner and disconnected
input refusal. No broad GameTest or profile sweep was required.

The connected client used root's private `:252`, loopback `25602/25603`, and a
copy named `FD403ArrowEditorReview` of root's stopped `FD403ArrowRetained` world.
The source world and C/D runtimes were untouched. [Run metadata](run.json)
records the exact JAR SHA-256, paths, Java and memory limits. This unoccupied
Arrow check verifies shared editor behavior; it does not replace D's occupied
Revised outcome.

One editor opening produced the following sequence through normal mouse input:

1. At 07:08:55 UTC, Dispatch with open restraints was rejected. The
   [editor stayed open with the guard reason](reject.png), and the server logged
   action 13, `success=false`.
2. At 07:09:02, Close restraints succeeded. The [same screen showed restraints
   closed and enabled controls](restraints.png), with action 11, `success=true`.
3. At 07:09:16, Dispatch succeeded without reopening or a stale-revision failure.
   The [screen closed and the car moved](dispatch.png), with action 13,
   `success=true`. A disclosed fixture capture followed for cleanup.

See the [server results](editor-results.log) and [input record](input.jsonl).

For the held-use check, a disclosed console fixture placed one west-facing
Creative Motor at `(282,-60,30)` and supplied a Wrench. An anchored-eyes setup
aim targeted its top control. `input hold-use` [opened the value board](inspect.png)
and canceled without changing the original UI +16 / actual -16 RPM.
The first coordinate selected -31 rather than the intended -32; authoritative
readback exposed that one-pixel/milestone targeting error. Inspection of Create's
`getCoordinateOfValue` established the four-pixel milestone width. The corrected
`input hold-use 330 233` [selected -32](selected.png), and the server read back
`ScrollValue:-32`, `Speed:32`, with its network present. Coordinates are specific
to this 854×480 window and GUI scale; inspect each current screen rather than
copying them into D's differently scaled UI. [Console readbacks](console.jsonl)
retain the exact outcomes.

After checks passed, `save-all` exceeded RCON's 30-second response timeout while
Sable saved; the server log subsequently confirmed saving continued and processed
the requested stop. This cleanup timeout is separate from the completed UI checks.
The copied world and evidence remain on persistent storage.

## Handoff

D received the helper and verified editor behavior. It already restored its
motor through normal held Use and obtained occupied powered translation with
its retained artifact; those observations remain valid. Adopt matching updated
client/server JARs at the next coordinated change, without replaying closed
construction, lower-hinge or persistence evidence. Follow the current queue
for the remaining family/teaching work.

## Runtime cleanup

Cleanup completed after the checks: the client exited through **Quit Game** at
07:14:19 UTC; the server exited with status 0 after saving all dimensions; then
the owned Xvfb stopped. The server, client and Xvfb services for private display
`:252` are all inactive. The copied world and evidence remain preserved.
