# PAX shuttle support repair and powered relaunch

This FD902 server-side regression covers a four-car PAX shuttle while its
original matching station section disappears during an active powered lift. It
uses sixteen mounted pig riders so the complete native carrier train is checked,
rather than an empty chassis.

## Scenario

`ShuttleGameTests.paxSupportRepairRealignmentResumesPoweredWinch` starts the
bound shuttle, removes its real matching curve after the winch enters `LIFTING`,
and requires every car to enter the captured, restrained
`Matching track support lost` state. The winch faults in the same physics
callback, preserving its physical carriage and all sixteen mounts.

The fixture restores the exact original curve, then performs `Repair matching
track`. For a bound PAX winch, repair validates every rail in the stored
unbranched route against its saved geometry fingerprint before returning any
stale route leg to valid state. It does not accept altered geometry.

Before native realignment, both winch `Resume` and `Recover` must reject without
changing the fault, restraints, capture state, car revisions, or rider mounts.
After `Realign matching track` physically snaps the original chassis to the
restored support, normal winch recovery reaches the boarding station and a
second `dispatch` enters `LIFTING`. The test records:

```text
FD_PAX_SUPPORT_RECOVERY repaired=true realigned=true recovered=true relaunched=true riders=16
```

## Validation

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer \
  --no-daemon --console=plain -PfdPerformanceTargets
```

On 2026-11-02 the command passed all **46 required GameTests** in **4.748
minutes**. Its target-gated controlled, PAX shuttle, and Free Spin averages were
50,744 ns, 97,844 ns, and 103,707 ns per invocation; carrier averages were
2,748 ns, 2,242 ns, and 2,429 ns.

## Limits

This is a dedicated-server native regression. It does not replace the remaining
post-repair connected-client reconnect or cold-server-restart acceptance
coverage.
