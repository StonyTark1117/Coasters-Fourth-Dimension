# FD401 Sable client pointer and snapshot boundary

This is a source-boundary review of pinned Sable 2.0.3 bytecode, not a new
runtime ride or a clearance certificate. The [exact-candidate Styles UI ride](../fd401-styles-ui-reload-ride-2c5d9a7/README.md)
used the archived [`sable-client.toml`](exact-candidate-sable-client.toml),
whose `sub_level_snapshot_interpolation_delay_ticks` is the default `1.5`.
The inspected Sable JAR SHA-256 is
`da6c3b66238586603d1dcaa2afb012d36815fbce0a2d5938fbb2936701d42279`.

The [client clock bytecode](ClientSableInterpolationState.javap.txt) caps
`interpolationTick` at `mostRecentTick + 1.5` and returns
`getTickPointer() = interpolationTick - getInterpolationDelay()`. At the
recorded default delay, the pointer therefore cannot exceed
`mostRecentTick`, the newest **information-packet tick**. The
[config bytecode](SableClientConfig.javap.txt) declares `1.5` as the default
and allows values from `0` to `100`; a reduced delay can move the pointer
farther ahead of the information tick.

The [sublevel bytecode](ClientSubLevel.javap.txt) passes that pointer to the
[snapshot interpolator](SubLevelSnapshotInterpolator.javap.txt). When it has
two snapshots bracketing the pointer, it interpolates. When the pointer is
past the latest **pose snapshot** and an older snapshot exists, its active
path extrapolates from that pair, capped at one further snapshot interval.
The [information packet](ClientboundSableSnapshotInfoDualPacket.javap.txt)
and [pose packet](ClientboundSableSnapshotDualPacket.javap.txt) update those
two quantities separately. The [server tracking bytecode](SubLevelTrackingSystem.javap.txt)
sends information before batches of pose snapshots: as separate reliable UDP
packets when UDP is connected, or in a TCP bundle otherwise. This preserves
the question of how much newer the information tick was than each body's
received pose buffer at a render step. Thus the default `1.5` setting alone
does not prove that every body in the visible ride had a pose snapshot as
recent as the information tick.

The precise safe condition for the existing between-snapshot clearance
certificate is that each rendered body's latest pose snapshot tick is at
least its client tick pointer. The saved client evidence did not record that
per-body relation. A focused client trace of tick pointer, information tick,
and latest two pose snapshot ticks during the occupied ride is the remaining
way to decide whether an extrapolation clearance check is needed for that
ride. The [physics-substep certificate](../fd401-styles-physics-substeps-2c5d9a7/README.md)
concerns server native poses and cannot establish packet arrival order.

`SHA256SUMS` hashes the bytecode disassemblies and archived exact-candidate
config. Each disassembly was generated with `javap -c -p` from the pinned
Sable JAR, not from a rebuilt library.
