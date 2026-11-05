# FD401 fixed-source PAX station bind and telemetry candidate

This isolated combined-profile server run tested a fix for PAX carrier readiness
on initial bind. Before the fix, a winch first registered while it had no cars,
creating runtime handles with `stationRestorePending=false`. Binding later
registered the same winch again, but `computeIfAbsent` retained those handles;
the BOARDING tick therefore skipped neutral carrier restoration and physical
station holds. The selected-JAR telemetry test then failed with all four PAX
cars captured but carrier angles near 0.42/0.46 radians and `boardingReady=false`.

`ShuttleWinchEntity.bind` now explicitly requests one station restore after
binding. This preserves ordinary per-tick registration and the new-object
restore path while restoring captured carrier poses and installing support
holds for a newly bound train.

The fixed-source candidate passed
`RideTelemetryGameTests.sharedTelemetryRespectsConsistRecoveryAndControllerAuthority`.
It emitted six-source and packet snapshots for Arrow (including a two-car
trailing consist), Legacy, Free Spin, Revised, and a four-car PAX train; it also
passed support/repair and dispatch guards plus PAX BOARDING/LIFTING/FAULT
telemetry, then printed `FD_TELEMETRY COMPLETE`. The server stopped cleanly and
Gradle exited with `BUILD SUCCESSFUL`.

This candidate is **not** the selected clean qualification artifact. Its JAR
SHA-256 is `ae6a699e818c5de54a138e57f1683d51be59aef08540a76e52f206efddf5a884`.
It was built from the working tree based on `1d5c5a3` with the production fix
listed in `run.txt`; the untracked
`ShuttleBoardingHoldGameTests.java` was also present and compiled into this JAR
(hash recorded in `run.txt`). That separate cold-hold test was not run here.

The result is a server-side family/consist/telemetry check only. It does not
qualify the new JAR across base, Styles-only, Engineered-only, and combined
profiles; rendered goggles, physical sign/source GUI transitions, or normal
client family operation remain separate acceptance items. Rebuild from a clean,
committed source tree and attach a fresh artifact identity before reusing any
baseline-wide claim.
