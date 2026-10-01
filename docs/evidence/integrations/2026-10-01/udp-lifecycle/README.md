# Sable queued UDP callbacks across disconnect and reconnect

The earlier [combined PAX player restart](../combined-pax-player-restart/README.md)
recorded `SableUDPClientboundKeepAlivePacket.handleClient` throwing a null-channel
exception on both clients at shutdown. Inspection of the pinned Sable 2.0.3 source
showed that its receiving handler queues a callback without checking the handler's
original connection when the callback executes. The keepalive then fetches the
current Minecraft connection and its UDP channel, which may already be closed or
belong to a replacement connection.

`SableClientQueueMixin` wraps that enqueue operation. On the client thread, a queued
packet executes only when its original connection is still the current Minecraft
connection, that connection is connected, and its original UDP channel is current
and active. Otherwise the callback is discarded. Healthy callbacks run unchanged;
packet formats, poses, camera calculations and the server transport are unchanged.
The optional mixin plugin enables this client-only compatibility guard for exactly
Sable **2.0.3**. This is a guard in Fourth Dimension, not an upstream Sable release.

## Deterministic native-handler probe

The opt-in `UdpLifecycleProbe` requires **both** `-PfdValidation` and `-PfdUdpProbe`
on `runClient` or `runObserver`. It deliberately disconnects that test client once.
It is inert in normal play. Use an owned test server/world; do not enable the probe
on a session whose connection should be preserved.

The probe enters Sable's actual inherited Netty `channelRead` method using its live
handler and context. It queues a counting canary and an actual native keepalive.
No packet handler is replaced in the fixture. It checks a live positive control,
queues another pair, disconnects the original connection and drains Sable's native
client queue. The client is then rejoined through the normal multiplayer screen.
After reconnect, the probe invokes the retained old handler to model delayed delivery
from the former connection, alongside the new connection's positive control.
This last step is controlled handler-level replay, not delayed traffic injected on
the wire. The probe report records channel identities and both connection states.

Two clients, Dev and Observer, ran on separate owned Xvfb displays against packaged
combined-pack servers. A baseline JAR contains the identical probe but no queue
guard. Both baseline clients recorded:

- One healthy callback before disconnect and one after reconnect.
- One stale callback executing after the original UDP channel became null, with
  the native keepalive null-channel exception preserved in the log.
- One callback from the old handler executing against the replacement connection.

Both guarded clients instead record **zero** callbacks for closed and replaced
connections, while both healthy controls still execute once. The drop diagnostics
identify the old connection/channel conditions. Neither guarded client logs a queued
packet exception or a NullPointerException during its complete run, including the
later ordinary server stop. All four client processes and both packaged server
processes exit 0. Screenshots preserve the deliberate disconnections, normal editor
controls and final server disconnections.

## Live motion after reconnect

With both guarded clients reconnected, Dev closes the empty PAX train's restraints
and dispatches through the normal Rotation Tool editor. The existing four-car train
completes LIFTING, OUTBOUND, RETURNING and CAPTURING, returns to BOARDING, and advances
its cycle count from one to two without a fault. RCON only samples winch NBT during
the cycle; it does not move the train or bypass its state machine.

Each client records 56 native carrier-angle samples over the cycle, covering all
eight original carriers with measurable angle changes. The audit also matches each
client's full Fourth Dimension snapshot hash to the server's emitted snapshot. These
checks show continued native motion and authoritative data flow after reconnect;
they do not establish frame-exact interpolation or occupied rider/camera accuracy.

The copied test world used Engineered `requireEnergy=false`. Two attempted queries
to the FE-specific diagnostic fixture therefore reported `Native PAX FE capability
unavailable`; those server errors and commands are retained. The subsequent cycle
uses the normal editor with that existing configuration, and claims no new FE
acceptance. The baseline Dev client also displayed a peer-reset disconnection on
server stop; this is preserved separately from the queued keepalive failure. Existing
startup model/texture/shader warnings remain outside this fix.

## Artifacts and verification

Baseline JAR SHA-256:
`f34d0788b144a49e6d52fce7a437a4465ebe8a09f58abcf9f3ea33a791eee46b`.
Guarded JAR SHA-256:
`daf8e9c039240c6bffa96e49f946d396934c334808c4b2fc31b4dedc0177fa81`.
Both are retained locally as `udp-lifecycle-baseline.jar` and
`udp-lifecycle-fixed.jar` under `/home/braydon/.cache/fourthdimension-48535694`.
An entry comparison proves that the probe classes are byte-identical. The guarded
artifact adds only `SableClientQueueMixin.class` and changes only
`OptionalMixinPlugin.class` and the optional mixin registration JSON relative to the
baseline. Source/build changes also expose the two opt-in client probe flags.

The build passes 19 JUnit tests. Base and combined dedicated GameTest runs each pass
33 registered checks, with six explicit absent-addon skips in base and none in
combined. The combined run enables FE-required occupied PAX phase recovery. These
server runs check client-only loading and retained native regressions, not client
behavior; the packaged two-client runs above supply that evidence. The strict audit
matches 334 Dev and 333 Observer snapshots in the guarded run to their server records,
with no missing-body or unknown-passenger warnings in these runs.

The loaded stack is Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10,
Coasters Simulated 0.1.5 and Sable 2.0.3, with Engineered 1.1.1, Track Styles 1.2.0,
Additions 0.2.0, Magnetized 1.1.0 and Immersion 0.2.3. Full loaded-mod lists are
retained, including Immersion's Aeronautics dependency. Run the strict evidence audit from the repository root:

```sh
python3 scripts/verify-udp-lifecycle.py docs/evidence/integrations/2026-10-01/udp-lifecycle
```

For another local probe, start a matching owned dedicated server, then run a client
with `-PfdValidation -PfdUdpProbe -PfdConnect=127.0.0.1:25585` and the desired integration
profile. After its intentional disconnection, reconnect through Multiplayer → Direct
Connection. Read `fd-udp-lifecycle.json` in that client's game directory. A complete
guarded run has active counts 1/1 and closed/replaced counts 0/0. The probe requires
a live Sable UDP channel; it does not synthesize a fake transport.

The exact launch scripts, loaded versions, warning traces, snapshot/native-motion
logs, probe reports, ride samples, process exits and artifact hashes are retained.
Full local logs and copied test worlds remain under ignored `build/udp-lifecycle`
and the worktree's packaged runtime. The original server properties are restored
byte-for-byte and all owned displays are stopped.

This closes the reproduced queued-disconnect/replacement failure for the pinned
stack under these checks. Occupied reconnects, adverse networks, packet loss and
reordering, other Sable versions, and the wider FD902–FD904 lifecycle/rendering/
performance matrices remain separate acceptance work.

The subsequent [occupied PAX follow-up](../occupied-pax-reconnect/README.md) uses this
unchanged guarded artifact for real rider reconnects during fault recovery and a
fresh lift. It preserves seat ownership and completes occupied recovery/cycling and
normal unloading. Its retained initial-join warning and shutdown TCP peer-reset
screen keep the wider transport and lifecycle limitations explicit.
