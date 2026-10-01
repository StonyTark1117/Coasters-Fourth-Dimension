# Restore local rider look after a moving native mount

The [earlier occupied reconnect checkpoint](../occupied-pax-reconnect/README.md)
covered a RETURNING fault and a fresh powered lift. Extending its guarded artifact
to live OUTBOUND, RETURNING and CAPTURING found a view-restoration defect. Ride
ownership, seat reservations and FE survived, but the baseline outbound reconnect
changed saved local yaw by **1.13336°** and pitch by **0.21148°**. Its initial cold
restoration also turned local yaw by about 8°. These exceed the existing 0.25° view
tolerance; the baseline is retained as a failed view control, not a certified run.

## Cause and correction

The saved player angles are carrier-local. `Boarding.restore` supplies world angles
to the native mount using the server's current carrier pose. Sable 2.0.3's
`entity_rotations_and_riding/LocalPlayerMixin.sable$onStartRiding` converts that input
back to local using the client's current logical pose. During motion or initial
loading, those poses can differ. The composition then introduces a turn even when
no mouse input occurs.

`RiderLookSync` now sends the saved local yaw/pitch, exact seat entity UUID and carrier
UUID over the normal play connection after a successful server restoration.
`RestoredRiderLook` waits until that same player mounts that exact native seat and
carrier, then applies the saved angles once after the native mount hook. It updates
previous yaw/pitch for view interpolation and clears the pending request. Requests
are rejected for invalid angles and discarded when the player/connection changes,
the connection closes or 200 client ticks expire. The handler is client-only;
registration and the payload use common classes on dedicated servers. Sable remains
pinned to exactly 2.0.3 by the existing required-mod metadata.

The normal native mount, physics, mouse look, camera transforms and Immersion
carrier selection remain in use. This correction does not repeatedly constrain
look, replace transport or change dispatch/recovery validation. Both client and
server need the same tested artifact.

## Real-client procedure

Each server uses a fresh copy of the original occupied Engineered RETURNING world,
with one actual Dev rider, four coupled PAX cars, eight carriers and the original
native carriage. Engineered requires energy. Startup is frozen until Dev logs in,
then ordinary ticking restores the rider and applies the expected restart fault.
Observer joins later and stays online throughout all three reconnects. Explicit
manual recovery returns the train to BOARDING at cycle one.

The owned Xvfb client uses the ordinary Escape → Disconnect and Multiplayer → Direct
Connection screens. A normal validated dispatch begins each new cycle; the harness
waits for the selected live phase before disconnecting Dev. It does not freeze the
ride during a reconnect. Offline occupancy becomes zero while a flushed save
retains the original carrier/local-seat reservation. The train may advance to a
later phase before Dev rejoins. After restoration, each run returns to a ready
station; the three new cycles advance the counter to four. Engineered's opposing
motor sequence is selected during capture and remains denied by PAX ownership.

The final unload uses the Rotation Tool's Open restraints button and normal Shift
dismount. No mounted-player teleport, forced mount, rider NBT edit, synthetic UDP
probe or passenger fixture is used. Only the unmounted Observer is positioned by
operator command. Original mechanical fields and all three saved rails are audited.
The same scripts run with Engineered alone and with all five optional addons.

The baseline preparation retains two separate harness stops. Its first sample was
on the first unfrozen tick, before the runtime applied deserialized winch retention
to the cars. Later, a sample reported all cars just below the 0.08 m/s readiness
limit, but the next-tick recovery request was correctly denied. The completed
preparation resamples and retries only that normal denial, without bypassing the
interlock. The failed logs, command response and replacement harness are preserved
under `baseline-combined`.

## Audit and scope

Run from the repository root:

```sh
python3 scripts/verify-pax-phase-reconnect.py docs/evidence/integrations/2026-10-01/pax-phase-reconnect
```

The audit requires both pack profiles and all three live phases. It checks exact
seat/carrier ownership, one rider when connected and zero while offline, restraints,
consist membership, original carriage, native FE **12,345**, blocked external drive,
completed cycles, preserved schema-5 mechanics and normal unloading. It also matches
client snapshot hashes to server emissions, each restored-look message to its server
counterpart, and final native availability of all eight carriers. `verification.json`
contains the measured results and warning counts.

All six corrected reconnects pass with **zero saved yaw/pitch drift**:

| Pack | Disconnect phase | Phase when restored | Offline simulation ticks | Final cycle |
| --- | --- | --- | ---: | ---: |
| Combined | OUTBOUND | OUTBOUND | 60 | 2 |
| Combined | RETURNING | CAPTURING | 59 | 3 |
| Combined | CAPTURING | CAPTURING | 66 | 4 |
| Engineered | OUTBOUND | OUTBOUND | 60 | 2 |
| Engineered | RETURNING | CAPTURING | 51 | 3 |
| Engineered | CAPTURING | CAPTURING | 60 | 4 |

The audit matches 405 Dev and 387 Observer snapshots in combined, and 339 Dev and
332 Observer snapshots in Engineered. In combined, 1,382 mounted Immersion samples
have a maximum raw carrier-orientation error of 4.22 × 10⁻⁸ radians. In both packs,
the final native camera-up vector matches the unloaded player's ordinary yaw/pitch
within 0.000066 vector distance, allowing for the diagnostic quaternion's printed
precision. The first cold mount and three reconnects each have exactly one matching
server/client restored-look record per pack.

All three packaged servers and all six clients, including the failed view control,
exit with status zero. Neither corrected run records a queued UDP exception,
NullPointerException or TCP peer reset. The original server properties and Engineered
configuration are restored byte-for-byte, and all owned process/display handles are
gone after normal client exits and display cleanup.

The combined camera check compares Immersion's raw orientation with the native
rendered carrier pose and checks cleared carrier context/travel after dismount.
A subsequent mouse-input check changes yaw by 3° and holds it for another second,
showing that the restored view is not pinned continuously. Raw mouse input stays on;
the first relative movement after recapturing the mouse is ignored by the client,
while the next movement takes effect. Saved-angle comparisons for reconnects occur
before this deliberate input.

The build passes **19 unit tests**. Base, Engineered and combined dedicated GameTest
profiles each pass **33 registered tests**, with respectively six, three and zero
explicit absent-addon skips. The Engineered profiles enable the FE-required PAX
phase-recovery checks. These native tests cover retained behavior and server loading;
the separate actual-client runs supply the view and reconnect evidence.

The new artifact is `/home/braydon/.cache/fourthdimension-48535694/rider-look-local.jar`,
SHA-256 `08a1f0e6a51b8aad8ca31b65b104f3dda315c3913ee5361c28f8202fa1ad52ed`.
The failed control is the unchanged earlier `udp-lifecycle-fixed.jar`, SHA-256
`daf8e9c039240c6bffa96e49f946d396934c334808c4b2fc31b4dedc0177fa81`.
Source hashes and packaged/compiled class matches accompany the process manifests.
Full local logs, launch scripts, copied worlds and phase drivers remain in
`build/occupied-phase-reconnect` and its worktree-owned packaged runtime.

The tested stack is Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10, Coasters
Simulated 0.1.5, Sable 2.0.3 and Engineered 1.1.1. The combined pack adds Track Styles
1.2.0, Additions 0.2.0, Magnetized 1.1.0 and Immersion 0.2.3 with its dependencies.
Per-process loaded-mod lists record the exact stack.

Transient native loading/passenger warnings are retained, including thirteen Dev
missing-body messages during the combined run's first loading second and one
Observer passenger warning on its initial join. Startup addon model/texture/shader
and dedicated-side mixin warnings also remain. Engineered's Observer also records
one initial-join passenger warning. Subsequent authoritative snapshots
and native carrier presence must pass; the messages are not deleted or treated as
proof of a clean loading transport. The earlier shutdown TCP reset remains a
separate investigation even if it does not recur in a later run.

This is bounded PAX evidence for these two packs and ordinary local-network menu
reconnects. Other families, simultaneous rider reconnects, client crashes, prolonged
loading, chunk interruption while reconnecting, adverse wire conditions, every
camera/comfort mode, frame-exact interpolation and the wider FD902–FD904 matrices
remain open.
