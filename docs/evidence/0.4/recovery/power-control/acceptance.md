# FD402 switchable recovery power and operator guidance

The 0.9.0-dev.16 checkpoint replaces the flat examples’ direct motor input with
an ordinary Create clutch and a vanilla lever. The motor begins at zero RPM;
the lever begins ON, cutting transmission. Set eight RPM in the bearing’s
direction, then turn the lever OFF to supply chain power. Brake the recovery
consist, turn the lever ON to stop external power, and disconnect only after
alignment. The motor stays installed at eight RPM. Existing saved examples
are not rebuilt or edited automatically.

Recovery mode displays its own operation sequence and live braked/towing state.
Action tooltips explain inspection, retention, propulsion and disconnection;
ride dispatch, restraint opening and retention release are disabled while
attached. The server retains its existing guards. Hovering over the status shows
the complete fault with explicit wrapping; the visible line is shortened to fit.
An initial client check exposed an overflowing single-line fault tooltip. That
rendering problem was repaired before the final artifact checks below.

## Final native artifact checks

`power-standalone-results.json` and `power-combined-results.json` identify the
same source manifest and JAR digest. Each profile passes 33 required native
GameTests and 25 unit tests; the combined profile uses the pinned Track Styles
1.2.0 and Engineered 1.1.1 versions. No optional JAR is bundled.

The five-family occupied recovery test uses the production example fixtures.
It sets their motors to eight RPM while the initial stop levers remain ON and
asserts zero anchor speed. Normal vanilla lever interaction supplies power,
then cuts power after normal recovery braking. Every anchor returns to zero
while its motor remains at eight RPM and installed. Native passengers, original
couplings and retained carriers survive. The same test still exercises native
holding/restoration, wrong-family rejection, lost couplings, permanent ordinary
bearing removal and normal disconnect/unloading. Occupied original Arrow/PAX
train checks also use the updated clutch fixtures.

Reproduce with Java 21, without deleting packaged-client worlds:

```sh
./gradlew build runGameTestServer
./gradlew build runGameTestServer -PfdIntegrations=styles,engineered
```

## Packaged client scope

The final JAR is tested standalone on an owned Xvfb :117 display at 1280×720,
GUI scale 3. The Agent Workspace MCP tools were unavailable. No host desktop
input, scripted vehicle motion, invented propulsion or SavedData editing is used.
Only the operator is teleported; ordinary creative walkway placement and demo
commands prepare access. All four validation flags remain false.

The world is a copy of the previous dev.15 finished-world backup. A new dev.16
example at (80,80,24) demonstrates normal attachment, clutch power and wrapped
boundary-fault guidance. During that repeat the operator misses reopening the
moving car’s editor, so it reaches its guarded route end; that attempt is not
counted as independent manual braking.

A fresh example at (160,80,24) supplies the independent empty Arrow manual-brake
case. Normal tool sneak-use selects the bare bearing; close restraints, retention
and Attach recovery produce the actual six-block native link. Create’s normal
motor interface selects eight RPM while the lever cuts transmission. Normal
empty-hand lever use supplies power; Start tow enables travel. The operator
reopens the moving car’s editor promptly, observes “Recovery towing”, and uses
Brake tow before the route end. The reopened editor shows “Recovery braked” with
no fault. Normal lever use cuts chain power while the motor remains installed
at eight RPM. Read-only block queries verify the lever/clutch powered state,
zero chain-anchor speed and unchanged motor speed.

A normal save/quit and fresh JVM restore both dev.16 recovery links under their
original native IDs. Towing remains braked and the clutch stop levers remain ON;
read-only queries confirm zero chain speed with the motors still at eight RPM.
Transient restoration unavailability leaves an inspection fault. After bodies and
links return, the primary car accepts explicit Start tow, clearing that fault;
with chain power still cut it remains in place, then accepts normal Brake tow,
Detach recovery and restraint opening. The secondary route-end case also detaches
and opens restraints, retaining its restoration diagnostic for inspection.

Both final-artifact clients exit normally. The attached/final SavedData snapshots
preserve all three original vehicles, every carrier ID and non-angular carrier
configuration, and all three rail/profile records. Both recovery identities are
cleared; cars remain stopped, captured and maintenance-retained with restraints
open and empty native seat assignments. The primary manual-brake car has no
final fault. The inherited dev.15 boundary car and secondary dev.16 case retain
their recorded inspection faults. The finished world is backed up to the ignored
`runs/recovery-dev16/finished-world` directory in the workspace.

Both startup logs contain four native missing-sublevel movement-packet warnings.
These are recorded in the event excerpts and remain an FD902 investigation;
subsequent native links/components restore and normal inspection/resume succeeds.
This check does not establish a packet-ordering fix. Complete fault text is
readable in the wrapped status tooltip; the vanilla movement tutorial is dismissed
through normal jumping/mouse input for unobscured final screenshots.

No quantitative rendered speed, braking-distance or complete clearance claim
is made. The other four families, occupied rendered towing, curved/banked
recovery, occupied PAX handover through towing, broader propulsion/strength and
the full multiplayer/lifecycle matrix remain open under FD402/FD902. The
previous bitmap-glyph crash is not resolved by this check.
