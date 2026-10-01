# Empty Arrow recovery through a packaged client

The unchanged dev.15 JAR from source checkpoint
`5130179841c7f988ad206c246f47c44593fbb499` now has a bounded real-client recovery
check in addition to the native server-handler tests. Its SHA-256 is
`6b31a038e9a8fe8b636b7e485bc8640c03e9592a03c6eab49ea3bec2dccf7668`.
This is one empty, standalone Arrow example, not occupied or all-family rendered
recovery acceptance.

## Actual workflow

Both fresh Java 21 JVMs load the packaged JAR with all four validation flags false.
The first creates a superflat creative world named `FD15Recovery`, seed 40220, and
uses `/execute positioned 0 77 12 run fd demo recovery_arrow`. The registered
command builds its actual original matching track, car, ordinary bearing and
unpowered chain/motor. A Rotation Tool and Create wrench are obtained through
normal give commands. Additional stone walkways let the operator reach the bearing
and motor without altering either native cart. Teleports move only the operator;
no body motion, velocity, coupling or recovery state is set through commands.

Normal right-click on the car opens its editor at GUI scale 3 in a 1280×720 window.
The controls are visible and clickable. Close restraints and Retain go through the
real client/server payload path. Normal sneak-use selects the bare bearing;
Attach recovery produces the actual native six-block coupling. The editor shows
Recovery braked / carriers retained and its Start tow, Brake tow and Detach
recovery buttons. The native linked IDs are:

- Chassis: `daaaf95d-f248-439b-abf0-cc97b582f493`.
- Ordinary bearing: `be856020-4f1c-43ed-ba97-66e405721514`.

The operator uses Create's normal wrench/value-setting interface to select eight
RPM. Read-only block data confirms motor Speed 8 / ScrollValue -8. Power is
supplied while the recovery group remains braked. Normal Save and Quit saves the
attached state; normal Quit Game exits the first JVM with code zero.

A second fresh JVM opens the same world with `-PfdWorld=FD15Recovery`. The native
log restores that exact coupling and the car renders normally. Its editor still
shows recovery braked and retained; it does not automatically resume. Start tow
through the normal button moves the car from its original station toward the far
end. The route-end guard stops it before the open end. The operator then uses
Brake tow, stops chain power by normal creative removal of the demo motor, and
checks the anchor's Speed 0 through read-only block data. Detach recovery removes
that exact native link. Normal Open restraints returns the editor to Ready /
restraints open. Save and Quit and Quit Game complete normally, with exit zero.

The explicit Brake tow action occurs **after** the automatic route-end stop. This
is not a rendered measurement of independently braking a moving car; the native
occupied tests cover that separately. No exact rendered travel-distance claim is
made.

## State and evidence

`arrow-attached-state.json` is inspected from SavedData after the first normal
save/quit. `arrow-detached-state.json` is inspected after the second normal
save/quit. The schema remains 6; one 4D vehicle and one rail remain. Chassis,
carrier IDs, variant/family, actual boundFrom/boundTo, damping/ballast, full
non-angular carrier configuration, and every rail/profile field are unchanged.
The ordinary bearing has no 4D vehicle record. Final state removes recovery
identity, retains maintenance/capture/stopping, and opens restraints. No occupants
or rider assignments exist in this empty case.

The expected route-end fault remains `End of compatible track; capture and inspect
the route`; disconnect does not silently clear it. The native event excerpt
records original linkage, same-ID fresh-JVM restoration and normal unlinking.
`arrow-gui-results.json` records verified findings, scope and hashes of snapshots,
events and reviewed screenshots. The two clients and owned Xvfb display are
stopped. The complete final world is preserved under the ignored workspace path
`runs/recovery-dev15/finished-world` for later checks, outside the temporary build
folder.

## Follow-up work

The recovery editor still displays its generic Capture/close/dispatch hint during
recovery, even though dispatch is correctly rejected. Recovery-specific teaching
and visible diagnostic guidance need improvement. This demo also needs clearer
switchable propulsion control: the tested power-stop operation removes the motor.
Stop propulsion before disconnecting, so the newly independent ordinary bearing
is not immediately driven again.

All-family rendered/occupied recovery, an independent rendered moving-brake case,
occupied PAX handover-to-towing, complete curved/banked offsets and clearance,
nonzero Arrow profiles, broader strength/propulsion/FE operation and the full
recovery-mode multiplayer/lifecycle matrix remain open. This two-JVM check does
not resolve the separate bitmap-glyph crash investigation or close FD402/FD404.
