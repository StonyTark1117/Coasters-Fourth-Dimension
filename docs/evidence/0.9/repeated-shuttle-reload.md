# FD902 repeated shuttle station reload

This checkpoint advances repeated PAX shuttle operation toward **FD902**. It
does not close the complete lifecycle gate.

## Method

The native tall PAX shuttle fixture assembles its four-car, matching-track
train, closes its restraints, dispatches it, and samples the outbound pass.
The outbound pass must reject an attempt to open the restraints.

At the first normal station return, when the winch is in `BOARDING` with cycle
counter one, the test saves the actual winch block entity with full metadata,
removes it, loads it from that NBT, installs the restored block entity, and
registers it with `ShuttleRuntime`. Before the next dispatch, it requires the
restored state to retain `BOARDING`, cycle one, the physical carriage UUID, and
the ordered four-car consist.

The test then dispatches the restored controller and requires a second complete
cycle. At the final station return, all four cars must remain captured and the
native consist must still contain four links.

## Result

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer --no-daemon -PfdIntegrations=styles,engineered -PfdPerformanceTargets
```

The combined target-gated profile passed all **41 required GameTests** in
**4.125 minutes**. The test emitted:

```text
FD_SHUTTLE_TALL cycles=2 stationReloaded=true maximum=142.94313148634603 station=82.92133833155415
```

## Limits

This is a server-side native fixture on a loaded route. It validates the winch
block entity persistence boundary between station cycles; it does not replace a
real dedicated-server restart, player-client, two-client, disconnect,
chunk-unload, or all-family repeated-shuttle acceptance run. Those remain
FD902 work.
