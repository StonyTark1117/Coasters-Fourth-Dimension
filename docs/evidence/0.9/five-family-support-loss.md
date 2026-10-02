# FD902 five-family support loss

Dev.27 adds native support-loss coverage for the five original functional
families: Arrow, legacy ZacSpin, Free Spin, revised ZacSpin, and PAX.

## Method

The GameTest creates one short matching connection and one native vehicle for
each family. It mounts one native Pig in each vehicle, closes the restraints,
and records the bound anchor pair. It then removes every vehicle's sole
matching connection.

On the following physics ticks, the test requires all five riders to remain
mounted; every vehicle must remain bound to its original anchors and retain its
captured carrier bodies. Each vehicle must be stopping with restraints closed
and the exact fault `Matching track support lost`. It then serializes and loads
the `FoundationData`, requiring the same fault, restraints, anchor binding and
captured carriers for every chassis.

The runtime now records that specific support fault before the generic boundary
guard runs. The guard is skipped for an already-invalid support so it cannot
replace the actionable fault with `Engaged track unavailable`.

## Result

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer --no-daemon -PfdIntegrations=styles,engineered
```

The combined profile passed all **39 required GameTests** in 4.275 minutes. The
new test emitted:

```text
FD_SUPPORT_LOSS families=[ARROW, ZACSPIN, FREE_SPIN, ZACSPIN_REVISED, PAX] riders=5 bindingsPreserved=true savedStatePreserved=true failClosed=true
```

## Limits

This is server-side native-Pig coverage on a short, loaded route plus an
in-memory FoundationData round trip. It does not exercise a player client,
two-client or late-join state, disconnect, fresh-process recovery, chunk
unload/reload during support loss, repair/rebinding, or full repeated shuttle
lifecycle coverage. Those remain FD902 acceptance work.
