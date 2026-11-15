# FD811 CTNB — complete

Owner: lead `01a1dd1d-1468-7f81-8f31-5532ad7a178d`, primary checkout `main`.
Qualified 2026-11-15. Native mechanics and joins combine with normal connected
construction and occupied operation below. The installed combined conflict case
is recorded under [FD813](../operations/README.md).

Selected artifact: CTNB1.2 file8718286,
SHA256 `e989c33e305779e37d4cd61bd4a7a13700faa45a604bd4aa92eacad738145115`.
Core versions unchanged. [Guide](../../../CTNB.md) enumerates seven materials,
Wing/Arrow scope and manual validated station/reverse release semantics.

`geometry-audit.json` compares 28 selected rail, beam and tie OBJ vertex lists
against native Coasters0.1.5. Every ordered vertex matches. Runtime fingerprinting
also includes their model JSON and the relevant addon control/render/alias classes.
This geometry comparison does not claim a player or native-physics run.

Native runs use Java21, `-PfdIntegrations=ctnb`, one named namespace at a time,
under `/mnt/NVME4/fourthdimension-runs/fd08-operations-20261115/ctnb-build`.
Saved worlds are retained outside that build directory with their run names.

- `ctnb-native-01.log` rejected the adapter's unsafe-material test. Diagnostic02
  established the mixin was installed and the direct guard itself accepted the
  material. CTNB aliases **all** `*_track` resource IDs to native Coasters in
  `ResourceLocation.equals`. Exact string IDs fix this without removing the addon.
- `ctnb-native-03.log`: 1/1 passed. Actual Chain entry and optional callback fence,
  bounded launch impulse, explicit reverse, station dwell/redstone hold, sensor
  reporting, signal-loss latch, restored-power denial, occupied repaint rejection,
  exact Arrow profile preservation and unknown/angular material rejection.
  Fixture material and velocity changes are disclosed invariant inputs.
- `ctnb-joins-01.log` exposed native graph edges retaining their previous material
  after repaint. The car had released authority but stayed on the initial span.
  Eligibility/drive now read live anchor material without changing route geometry.
- `ctnb-joins-02.log`: 1/1 passed. An actual native Wing body travelled from CTNB
  Boost through a native segment to CTNB Sensor, reaching approximately
  `(1800,100.625,677.5)` and activating its native sensor. An unconnected crossing
  was rejected. No direct velocity write drives this transition check.

`ctnb-native-04.log` repeats only the affected control case after live-material
lookup changed: 1/1 passed. All native server processes exited cleanly.

## Normal connected construction and operation

The empty `/fd demo ctnb_track` fixture supplied native loop geometry, seven
selected section roles, redstone, motors and a Boarding Station. Commands supplied
materials, car, Rotation Tool, platforms and initial viewpoints. They did not
repaint, place the car, bind its station, select its provider, board or operate it.
Camera turning and reachable vanilla placement were verified in this same private
client with default bindings and raw input on.

The first two repaint clicks exposed a real input-path mismatch: Coasters0.1.5
clears Create's old `TrackBlockOutline.result`, which CTNB1.2 reads, and supplies
`AnchorPeerCurveHit` instead. The client bridge now sends the native CTNB packet
from that current hit. The server checks the selected held material, build and
anchor permissions, loaded chunks and six-block curve reach before retaining the
native repaint guard. Pinned fingerprints include the packet and handler classes.
No geometry, physics or normal mouse settings changed for this repair.

After one process restart, [normal sneak-use](normal-repaint.png) changed the saved
first curve from `simulatedcoasters:coaster_track` to `coasters_extras:station_track`.
[Normal car placement](normal-car-placement.png) created Wing chassis
`d24af5c8-1109-4111-9b16-1cf29d79250b`; [station use](normal-station-binding.png)
bound it, and [Claim CTNB](normal-claim.png) selected held forward authority.
[Seat use](normal-board.png), [Close](normal-close.png), and
[Dispatch](normal-dispatch.png) succeeded through normal input.

The same occupied car travelled through the functional loop,
[captured on Reverse Track](reverse-capture.png), accepted normal
[Drive reverse](normal-reverse.png) and [Dispatch](normal-reverse-dispatch.png),
and returned to Station Track. [Station capture](station-return.png) and
[read-only save](returned-station.json) show the same rider/car held after dwell.
A further explicit Dispatch continued along the station section. The manual Stop
arrived beyond the supplied platform, at the adjoining Boost span near z32;
this is not evidence of stopping on the platform. It did capture safely and
became Ready. Normal [Open](normal-open.png) and [Shift](normal-unload.png)
unloaded to ground `(16.5,-60,32.5)`, and read-only `RootVehicle` was absent.
The [final save](final-empty.json) has the same chassis, no riders/fault,
restraints open, stopping true, held CTNB authority and saved reverse direction.

[Operator events](operator-events.txt), [input](input.jsonl) and
[console](console.jsonl) distinguish normal actions from disclosed setup and
readback. This claims representative selected CTNB input/control and occupied
boundaries, not Survival crafting of upstream materials or a second family sweep.
Native Arrow profile preservation and all seven control behaviors carry forward.

## Provenance and cleanup

Launch01 JAR SHA256 `fc849069ad5f4c928a2e649f3486eb249fdb01f7b9ea0584e9a10ea20936b432`
exposed the repaint input mismatch. Launch02
`8ad7b8929e0fce2b6688f14ded8d6630241faf227c3ac62ac32860f124587e65`
contains the input/packet repair and proves the workflow above. These new hooks
are limited to selected CTNB material use; accepted native force and joins do not
need repeating. The later FD813 test runs the current source with all providers.

Persistent root `/mnt/NVME4/fourthdimension-runs/fd08-ctnb-20261115`, owner above,
display316, game/RCON25650/25651, prefix`fd08-ctnb-20261115`. Java21 actual client
heap1536MiB/6GiB scope; server2048MiB/4GiB. [Measured memory](final-memory.txt)
remained within both limits. Full logs/screens and the same saved world remain
under `server/CtnbWorkshop` and `completed-empty-world`, outside build.
Client exited, server saved all dimensions and exited, then Xvfb stopped at
14:07UTC. [Cleanup](cleanup.txt) reports all three inactive/MainPID0/success.
No live runtime or required FD811 case remains.
