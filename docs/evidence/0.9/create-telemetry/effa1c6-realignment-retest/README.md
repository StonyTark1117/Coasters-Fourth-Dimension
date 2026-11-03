# Realignment retest: fresh cycle passes; historical recovery remains blocked

This bundle contains **two distinct outcomes** on exact source
`effa1c63a668bf1856c567fc702105c01e9accd4`:

- **Fresh stopped occupied Arrow cycle: bounded pass.** Support loss, normal
  restoration, Repair, delayed Realign, readiness, normal restraint opening and
  unloading succeed while retaining the original chassis/carriers/rider seat.
- **Pristine historical failed world: unresolved failure.** Normal Capture and
  Realign still reject because native chassis motion exceeds the physical-stop
  threshold. Its retained rider is not unloaded or declared recovered.

Packaged JAR SHA-256:
`9014f8f27724536e088e3a3e546c20930dae45c0fa0b7e363fdb8e62c772de88`.
This matches the core chat's candidate artifact. Local `build` passed with compile/
unit-test outputs reused from cache; `build.log.gz` records this limit. These are
packaged client observations, not a fresh native-suite run from this worktree.
Dependencies are hashed in `manifest.json`; schema 6 / protocol 7. Standalone,
Xvfb :121, llvmpipe, 854×480; no hardware performance claim.

## Historical world: retained failure

Copy the untouched stopped-world backup from the
[de667f7 failure](../de667f7-arrow-repair-blocker/README.md). Original and replay
foundation SHA-256 match in `input-world.json`:
`7b30ddbf3abf72f1002a3935992d6331d1b89ee2beed15f7ccc100d9098ca821`.
No replay NBT is edited. The server uses the `telemetry-effa1c6` directory; its
embedded level name can still appear as `telemetry-de667f7` in logs.

Dev and Observer join normally. The original rider restores to its original
carrier/seat; both carriers and chassis identities remain intact. Dev uses normal
**Capture**, then **Realign matching track**. The latter rejects with **Wait for
every linked chassis to stop physically before realignment**. Five subsequent
read-only motion samples show speed magnitudes of approximately **0.111–0.265**,
all above the 0.1 precondition. The car remains below the original rail and near
the old restoration scaffold. No scaffold, native velocity, threshold, fault or
vehicle state is changed to force a pass.

All four historical observations retain the boundary fault, pending realignment,
closed restraints and one rider assignment. The screenshot records the actual
normal action rejection. The native hard-stop assertion from the core suite does
not prove this post-physics scene settles; this historical result remains open.
The server is saved/flushed and stopped normally, exit 0, with the failed world
backed up under `build/telemetry-local-acceptance/effa1c6/historical-after-clean-stop`.

An initial window capture occurred before focus repaint and a right-click opened
Observer's sign editor; it was closed without editing its text. Window PIDs were
then checked: Dev was 0x200008 and Observer 0x400008. Only the verified Dev editor
and realignment-result captures are used for the historical action claim.

## Fresh world: bounded operator pass

Use a separate fresh world `telemetry-effa1c6-fresh` on the **same JAR**, reconnecting
both clients through normal Direct Connection. `fresh-setup.txt` records the creative
fixture preparation: native anchors A `(32,83,20)`, M `(32,83,38)`, B `(32,83,56)`,
stone platforms, supplied items, goggles, and operator positioning. These initial
anchor blocks are command fixtures, not survival-building evidence. The two 18-block
Arrow rails, car, station binding and Display Link configuration use normal items.
The lower restoration access uses a stone at `(34,79,38)` rather than the historical
scene's larger scaffold; this is not an identical physical-scene comparison.

1. Normally connect A→M and M→B, place the Arrow car at M, place/bind station
   `(26,83,42)`, attach Display Link `(26,83,41)` to sign `(26,84,48)` and select
   **Ride fault**. This source/target stays unchanged through the cycle.
2. Dev normally boards with an empty hand from `(27.5,84,38.5)`, yaw -90,
   pitch 20, then closes restraints using the Rotation Tool. Observer receives
   **1/4**, aligned, restraints closed and dispatch ready, with no fault.
   The earlier raw stage `fresh-occupied-ready` is a failed boarding attempt
   with **zero** assignments; only `fresh-occupied-ready-confirmed` starts the
   occupied acceptance sequence. The failed attempt remains in the raw record.
3. Observer normally breaks A; the car rebinds to remaining matching M→B without
   fault. Observer then breaks B; actual support loss sets the retained fault and
   realignment requirement. Goggles show the fault and deny dispatch; saved sign
   NBT also contains **Matching track support lost**. The rider remains mounted.
4. Normally replace B and reconnect B→M using the Arrow track item, reaching M
   from `(34.5,80,38.5)`, yaw 90, pitch -42. The original profile and geometry
   fingerprint are retained, but the restored rail remains invalid/inert. A repeated
   attempt after the successful held use is not a second restoration. The stone
   pedestal already existed, so the setup `setblock` response “Could not set the
   block” is not a failed track restoration.
5. Dev invokes normal **Repair matching track**. The rail becomes valid, the
   fault clears, restraints remain closed and pending realignment still denies
   dispatch. Observer goggles show the required follow-up and the same fault
   Display Link clears its sign. Recorded game times **3691 → 3931** span
   **240 ticks** with fault still empty and pending alignment retained at both
   observations. Five read-only velocity samples in that delay are approximately
   **0.00068–0.00266**. The samples/endpoints are not a continuous per-tick trace.
6. Dev invokes normal **Realign matching track** after the delay. It succeeds;
   pending alignment clears, fault remains empty, and the original identities and
   rider/carrier/seat assignment survive. Observer receives aligned and dispatch
   ready; the same fault sign remains blank. No dispatch is requested.
7. Dev normally opens restraints and uses Shift to dismount. The observer receives
   **0/4**, restraints open and dispatch denied for the open-restraint condition.
   Saved occupants and seat assignments are both empty. The final stopped-world
   NBT also contains no stale rider/seat entries; no cold restart of that fresh
   success is claimed.

The successful car is chassis `[-1474168113,-778088683,-1679358650,-462846203]`.
The historical chassis is `[543374797,-1922610306,-1252424642,897751176]`.
Do not combine their identity assertions or use the fresh result to close the
historical failure. Normal boarding, support removal/restoration, repair, realignment
and unload were client interactions; RCON setup/positioning and read-only diagnostics
never wrote vehicle flags or reset native motion directly.

## Evidence and cleanup

All 13 observations are retained: four historical and nine fresh, including the
failed initial boarding stage. Both motion sample sets, original client PNGs,
complete logs, hashed input metadata and the two stopped foundation files are
included. `shutdown-state.json` was extracted from those NBT files using nbtlib;
the historical state retains one rider, while the fresh state has zero assignments.
The ordinary verifier checks that extracted summary's hashes, not an independent
NBT parser. Sign screenshots are cropped by Minecraft sign width; raw NBT carries
full text. Full worlds remain local rather than being bundled.

Historical and fresh server sessions both stopped normally with exit 0. After the
fresh server closed, both clients used **Quit Game** and exited 0. The empty owned
Xvfb :121 was terminated and its PID confirmed absent. The fresh world backup is
`build/telemetry-local-acceptance/effa1c6/fresh-after-clean-stop`. No source changes
or rebuilds occurred between these two cases.

Run `python3 verify.py` here to check retained state/identity/profile assertions,
recorded delay, sampled motion and file hashes. This covers one stopped occupied
standalone Arrow car, not a dispatched moving loss, every family/optional profile,
rendered clearance, hardware performance or the complete FD401/FD402/FD902/0.9 gate.
Historical recovery remains unresolved and must be addressed with its own evidence.
