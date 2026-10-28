# Shared operations telemetry on the release baseline

This checkpoint brings shared telemetry and Create diagnostics forward from
`codex/coaster-integrations` (`8f97fc2`) onto release parent
`b0d894b68a312bfc0883d3e1f6b097fcb826a3c4`, on `codex/integration-reconcile`.
The branches have substantial independent vehicle, recovery, rendering and
persistence changes. Only the shared operations and Create source files were
adapted here; this is not a merge of the older integration branch.

The release's recovery dispatch guard, schema 6, protocol 7, saved-passenger
restoration, native Engineered interlocks and current appearance renderer remain
in place. The broader provider registry and Additions/Magnetized/Immersion
adapters still require reconciliation. FD401 and the full compatibility goal
remain open.

## Behavior

Stations and shuttle winches publish immutable whole-consist snapshots with car
and seat counts, occupancy, carrier readiness, restraints, maintenance retention,
dispatch readiness/denial, faults and shuttle phase. Snapshots contain no rider
identities and are sent only when changed. Station sampling runs every five
ticks; winch sampling and Display Link refresh run every ten ticks. Snapshot
data is included in client update tags, not mechanical disk persistence.

Create goggles use the shared fields. Winches retain Create's native kinetic
overlay. Six registered Display Link sources expose status, occupancy,
restraints, dispatch eligibility, phase and faults on both controller types.

Dispatch telemetry uses the same read-only preflight as actual dispatch, plus
the requesting controller's ownership/range checks. PAX stations cannot report
dispatch ready; the bound winch owns its shuttle state machine. Requests always
validate current server state again. A previously ready snapshot grants no
authority. Station redstone requests use the same operations entry point.

## Native scenario

The isolated telemetry batch creates an occupied, natively linked two-car Arrow
train, one occupied car in each of the three other non-PAX families, and an
occupied four-car PAX train with a powered bound winch. It checks:

- All five family snapshots, whole-consist occupancy/capacity and readiness.
- Six actual registered Create sources discovered from each controller, with
  `provideText` reading through a real adjacent Display Link block entity.
- Station update-tag application and winch snapshot packet serialization.
- One hundred samples leaving saved vehicle and winch state unchanged.
- Open peer restraints, retained recovery reference, maintenance, wrong binding,
  distance, PAX station ownership and stale snapshot rejection.
- Valid non-PAX dispatch and PAX winch dispatch to LIFTING; shared PAX capture
  follows its existing emergency FAULT transition, which telemetry exposes.

The recovery-reference negative control is synthetic. It proves preservation of
the existing preflight guard, not a newly exercised native recovery attachment.
Other native recovery cases remain in the complete regression suite.

## Reproduction and evidence

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export JAVA_TOOL_OPTIONS=-Xmx2G
./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 build
./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 runGameTestServer -PfdPackaged
./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 runGameTestServer -PfdPackaged -PfdIntegrations=styles,engineered
python3 scripts/verify-reconciled-telemetry.py
```

Run the two GameTest profiles sequentially: their task recreates the same test
world. The new case is `sharedtelemetryrespectsconsistrecoveryandcontrollerauthority`.
Exact JAR/source hashes, process exits, unit results and compressed logs are
recorded in `manifest.json`. The pins remain Minecraft 1.21.1, NeoForge 21.1.248,
Create 6.0.10, Sable 2.0.3, Coasters 0.1.5, Engineered 1.1.1 and Track Styles
1.2.0, using Java 21.0.13.0.2+1.

All 26 unit tests passed. Base and combined packaged runs each passed all 45
registered native tests. Both included the five-family telemetry fixture; the
combined run also retained all 18 blocked/four valid motor probes and the native
five-family controller sequence checks. The evidence verifier passes. Changed
Java files pass google-java-format 1.24.0 under Java 21, and the diff has no
whitespace errors.

Tested JAR SHA-256:
`43482c6c421f588c889826f6b2740c96cbf57a78f556c30a06ff6389d4e305f3`.

## Limits and retained diagnostic

The first targeted run passed the five station/source checks, then called a
native Create client-only path on the dedicated server. That failed log is
retained. The corrected headless fixture checks shared winch tooltip formatting
and packet serialization; it does not invoke the native kinetic overlay or
client packet application. Production retains both client paths.

This is evidence for source registration/output and server operations, not for
physical display rendering, Display Link configuration GUI, client goggles
layout, or two-client/late-join synchronization. Those need live-client checks.
The ordinary suites explicitly skip the separate cold-restart scenario; this
checkpoint does not replace the earlier occupied restart evidence or certify a
full external-control PAX cycle, all addons, or the complete FD901–FD904 matrix.
