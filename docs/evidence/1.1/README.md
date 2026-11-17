# 1.1 builder and operator tools

FD1602, FD1603 and FD1604 have passed their affected native and connected-client
checks. FD1605 is finishing its performance/resource sample and final cleanup.
Publication is separate; the public 1.0 Beta artifacts remain unchanged.

## Scope and contracts

The [player guide](../../BUILDER-TOOLS.md) declares the exact preset families and
settings, five original upholstery palettes and server-authoritative panel.
Presets invoke the existing bounded editor actions; exports omit world markers,
owners and authority. They do not rebind invalid geometry. Palette changes add
one optional saved field and replace only original cushion sprites. Every vertex,
normal, mechanical identifier, carrier and rider binding remains unchanged.
Unknown/absent palette identifiers load Original. The Coaster Skin adapter and
its previously accepted missing-provider Original fallback are unchanged.

Panel state uses `RideTelemetry` plus bound controller power/readiness. Ordinary
vehicle, station authority and four-car PAX sources are covered. Client commands
retain current revisions and existing permission/operation guards. PAX resume/
recovery additionally requires proximity and interaction permission at its winch.
There is no new automatic recovery, multi-train system or 1.2 feature.

## Targeted checks

- [Five unit tests](preset-unit-tests.xml): version/identity/UTF-8/size checks,
  exact local replacement, path-safe names, corrupt-library preservation and
  capacity/fallback.
- [Two core native tests](core-native.log.gz): all five palettes survive saved-data
  load with mechanics/seat state unchanged; stale/occupied/permission rejection;
  whole-consist occupancy and dispatch refusal; tuning round-trip and atomic
  incompatible/NaN/unconfirmed rejection.
- [Native route test](routes-native.log.gz): full profile round-trip, atomic wrong
  length refusal, invalid geometry refusal, stripped marker coordinates and
  explicit destination marker requirement. The same first run's PAX fixture
  failed before exercising panel code because its corridor chunk at z198 was
  not loaded. That setup was corrected, and only PAX was rerun.
- [PAX panel test](panel-native.log.gz): four cars, sixteen seats, winch-source
  agreement, power telemetry and stale request rejection; passed and saved cleanly.
- [Station-authority test](authority-native.log.gz): restraint readiness, bound
  controller identity and loss refusal. 100 consecutive panel samples took
  2,886,202 ns total on this host; this is a bounded method-cost diagnostic, not
  whole-tick or FPS acceptance.
- Connected production client: native Rotation Tool hit opened the editor;
  [Tools navigation](tools-menu.png), [preset save](preset-save.png),
  [clipboard import](preset-import.png), [unconfirmed refusal](preset-unbound-denial.png)
  and [server-acknowledged apply](preset-applied.png) passed. The local library
  contains the expected `sns_free_spin/sns_magnetic_v1/TUNING` entry.
- [Operator ready](operator-ready.png) and [operator denied](operator-denied.png)
  show normal close/open restraint requests changing authoritative readiness.
  Server log records successful actions11,12 and90. Native fault/stale cases cover
  rejection without bypass; no new motion campaign was needed.
- Original Ocean upholstery survived [server restart](palette-after-restart.png)
  and [resource reload](palette-after-reload.png). Sunset [local preview](palette-preview.png)
  canceled back to saved Ocean. Original chassis/structural/safety materials stayed
  intact; the previously accepted poses and provider fallback carry forward.

[Raw client](client-workflow-client.log.gz) and [server](client-workflow-server.log.gz)
logs preserve requests and observations. Fixture creation and player-position
commands were disclosed setup, not player construction proof. Camera and native
Use were verified separately in the same client before mod interactions.
The first three-button navigation overlapped the old editor at854×480; it was
replaced by one header entry and the corrected menu was inspected at that size.
A floating camera setup initially fell; a small stationary platform corrected
that observation setup before persistence and input claims.

## Runtime and preservation

Primary checkout/main began at `b4893639`. Existing 1.0 cases remain closed.
The completed concurrent rename changes Classic Spinning/Launched Inverting
Spinning labels, preserving saved IDs and old fault-prefix compatibility.
The user's unrelated 1.3/1.4 roadmap additions remain separate local work.

Owned private run: `/home/braydon/.local/state/fd11-20261117/private01`, owner
`fd11-primary-20261117`, display`:331`, units`fd11-primary-private01-*`, game
port25711/RCON25712 (loopback). Java21, client1536M heap/6GiB scope, server2048M/4GiB,
VirtualGL `/dev/dri/card1`, native Radeon RX7900XTX, Ryzen9 7900X3D. Window854×480,
GUI scale2, render8/simulation5,60FPS cap, no shader; input/default bindings verified.
This is a representative tools scene, not the fixed expanded park workload.

The first native world saved slowly on the system XFS/NVMe drive: thread dumps
showed ChunkStorage waiting for live pwrite work and write counters advancing.
It finished normally after22m39s; no forcekill or duplicate launch. Later native
runs used the separate NVME4 build, preserving prior worlds before each run.
Final runtime/artifact/performance records are added after completion.
