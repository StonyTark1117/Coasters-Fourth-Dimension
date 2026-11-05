# FD401 refreshed-JAR combined Arrow fault display

On 2026-11-05, the selected clean `ec2fcda` artifact was launched with Track
Styles 1.2.0 and Engineered 1.1.1. Its SHA-256 was
`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`. The
Java 21 direct launch used `-Xmx1536M` in a private Xvfb `:249` and a
`systemd-run --user --scope -p MemoryMax=3G` client scope; the active scope
reported `Result=success`, with 2,560,385,024 bytes current near exit. The
world and client exited cleanly; the log ends with all dimensions saved and
`Stopping!`.

The copied world descends from the combined Create telemetry fixture described
in [`fd401-create-telemetry-combined`](../fd401-create-telemetry-combined/README.md).
A previous exact-artifact session opened the normal Display Link UI and
recorded the existing `Ride fault` → `Line 1` mapping in the [UI reachability
record](../fd401-current-clean-combined-display-link-ui-20261105/README.md).
This rendered-output session copied that saved world into a separate game
folder. It targeted the Arrow boarding station at `(-6,83,32)`.

The normal Create goggles tooltip rendered the live station state in
[goggles-fault-live.png](goggles-fault-live.png): zero of four riders, one car,
BOARDING, restraints open, aligned, not retained, dispatch not ready, the
fault `End of compatible track; capture and inspect the route`, and the denial
`Vehicle is outside station range`. The F3/source screenshot
[goggles-and-station-debug.png](goggles-and-station-debug.png) shows the
station target and matching fault details. With the validation client enabled,
the packaged client log recorded the received snapshot at 01:30:42:

```
FD_CLIENT_TELEMETRY dimension=minecraft:overworld position=BlockPos{x=-6, y=83, z=32} snapshot={available:1b,boarding:1b,capacity:4,captured:1b,cars:1,denial:"Vehicle is outside station range",dispatch:0b,family:"arrow_controlled",fault:"End of compatible track; capture and inspect the route",occupied:0,phase:"BOARDING",restraints:0b,retained:0b}
```

The linked Create display board rendered `END OF COMPATIBLE` in
[display-board-fault.png](display-board-fault.png), matching the start of the
same fault message. The board width truncates the remainder. Thus, one selected-
artifact combined-profile Arrow fault state has matching live goggles, board
text, and client snapshot evidence.

This is a bounded single-state result, not full FD401D acceptance. The ride was
unoccupied and already faulted; no source transition was induced. The run does
not cover the other five sources, other ride families, trailing-car occupancy,
controller unavailable/re-entry, PAX phase changes, normal sign output, or the
base/Styles-only/Engineered-only profiles. Those rows remain open.

A preliminary launch was stopped cleanly after it started before the private
Xvfb service was active and inherited the host Wayland session. Its console is
retained in [initial-display-start-failure.log.gz](initial-display-start-failure.log.gz)
and is not acceptance evidence. The successful run began only after Xvfb `:249`
was active and `WAYLAND_DISPLAY` was unset.

The [launch script](run-client.sh), classpath, program and VM arguments,
[dependency hashes](dependencies.sha256), compressed [client log](client.log.gz),
artifact hash, and [checksum manifest](SHA256SUMS) identify the successful run.
