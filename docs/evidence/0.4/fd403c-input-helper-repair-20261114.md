# FD403C input helper diagnosis — 2026-11-14

Root owns the bounded helper diagnosis; FD403C and its private runtime remain
with `01a1d49d-7763-76b2-8b0c-6f5b337f0ea2`. Work is on primary `main`.
Accepted construction, family mechanics and prior input evidence remain closed.

C's 13:46 UTC input record shows a real current failure: relative-device
`turn -78 136` moved the pointer from `(427,240)` to `(349,376)`, while
F3/server rotation remained approximately `-14.9834/-2.18245`. Pause/resume
had already restored the centered pointer. Repeating that setup does not
establish camera movement. Normal Capture, restraint opening and dismount
completed; the failure is not evidence that every helper action is broken.

Root reproduced the exact XTest call on a separately owned private Xvfb and
observed the XI raw event with the requested `(50,0)` delta. A window using
the exact GLFW library from the pinned LWJGL 3.3.3 native JAR (GLFW 3.4.0)
then received the helper's cursor callback with that delta. Including the
same screenshot commands did not suppress callback delivery. These tests
isolate the transport; they do not prove the Minecraft instance consumed it.
The tiny probe processes and private display have exited. Their source/logs
remain under `/home/braydon/.local/state/fourthdimension-runs/input-helper-repair-20261114`.
An initial probe selected host Wayland despite its private DISPLAY and exited;
subsequent probes explicitly forced X11 and removed WAYLAND_DISPLAY.

The opt-in Chain diagnostic now also reports Minecraft's screen, active
window, mouse-grab state, ignored-first-move flag, callback coordinates and
deltas, plus actual GLFW focus/cursor/raw-input modes and player rotation.
Java 21 `compileJava -PfdPackaged` passed in root's isolated build. C owns
adoption of the single diagnostic class and the targeted before/after trace.
No production input behavior or runtime API was changed by this instrumentation.

## Proven cause and repair

C's [13:59:14 input snapshot](fd403c-input-helper-repair-20261114/before-first-turn.json) has `screen=null`, active/focused window,
`GLFW_CURSOR_DISABLED`, raw input enabled, `mouseGrabbed=true` and
`ignoreFirstMove=true`. The [first 20-pixel event](fd403c-input-helper-repair-20261114/after-discarded-turn.json) changed the callback position
from 427 to 447 and cleared `ignoreFirstMove`, but left yaw unchanged.
Without another capture reset, the [second event](fd403c-input-helper-repair-20261114/after-next-turn.json) advanced the callback to 467
and changed client/server yaw from -14.983383 to -11.983383 (exactly +3°).
This is vanilla `MouseHandler.onMove` initialization, not missing XTest input.
`grabMouse` sets `ignoreFirstMove` again, explaining why pause/resume retries
repeated the failure.

The helper now sends a tiny opposing one-pixel lead-in and the compensating
motion in one `turn` gesture. With established capture their sum equals the
requested deltas. With fresh capture only the lead-in is discarded, adding at
most one pixel in the requested direction (0.15° at sensitivity 0.5). This
preserves normal XTest input and makes even a short turn effective; it does
not set player rotation directly or modify Minecraft's input flags. The record
includes both motion steps and the tolerance. Use the actual rotation/hit for
targeting, as before. Invalid/zero turn arguments fail before runtime access.

`python3 -m unittest discover -s scripts/tests -p test_private_client.py`
passes all 14 checks, including the discarded-first-callback and active-capture
paths, direction preservation, small turns and pre-input argument refusal.
The repaired native GLFW probe delivered both callbacks from one helper call.

## Connected-client repair verified

C used the repair during normal navigation, after the Create value board closed,
instead of adding a separate test run. The [before-state recovered from C's command output](fd403c-input-helper-repair-20261114/before-repaired-turn.json)
has fresh capture (`ignoreFirstMove=true`), yaw `-26.833385` and pitch `18.217554`.
At 14:10:57 UTC, one [recorded `turn -247 -122`](fd403c-input-helper-repair-20261114/repaired-turn-input.json)
sent `(1,0)` then `(-248,-122)`. The [14:11:07 snapshot](fd403c-input-helper-repair-20261114/after-repaired-turn.json)
reports yaw `-64.03339`, pitch `-0.08244705` and `ignoreFirstMove=false`.
Server Rotation independently reported the same values. The yaw/pitch changes
are `-37.2°/-18.3°`, exactly the expected effective `(-248,-122)` motion at
0.15° per pixel after the tiny lead-in was discarded. C then walked normally
toward i3 and verified the resulting position. This closes the camera-helper
repair; no further generic input test is required.

The existing client/server/world remained owned by C. The client diagnostic
overlay SHA-256 was `40faa0172fe08ed5074fe784c276826018b7f5aa5b546e66120c57f87d48e984`;
only `ChainLiftDiagnostics.class` differs from its preceding diagnostic overlay.
Raw input and normal mouse bindings stayed unchanged. Normal held-use had
already changed i1 to motor and anchor `Speed=-16`, verified at 14:08 UTC.
Remaining motor corrections, occupied DAMP proof and walkthrough reconciliation
remain within FD403C; they do not reopen accepted input or family evidence.
