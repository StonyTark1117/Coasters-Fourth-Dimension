# Native FE-enabled operating checkpoint

This advances FD401 in dev.5. It adds one isolated GameTest batch against the
separately installed Engineered **1.1.1** release and retains the earlier dev.4
GUI observations as a distinct artifact. No optional implementation or asset is
bundled. Schema/network protocol remains 5.

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer \
  -PfdIntegrations=engineered
```

`EngineeredEnergyGameTests` enables the installed release's `REQUIRE_ENERGY`
configuration for its own sequential batch and restores the previous value with
the batch completion/stop hook. The native runner completes one batch before
starting another; the existing default-energy tests do not share this temporary
configuration. Absent/uninspected releases explicitly report that FE was not
exercised. The test uses the public NeoForge FE capability on the installed
motor-span anchor, not a substitute battery or custom motor implementation.

## Real native fixtures

Six independent 48-block matching flat sections carry the five 4D families and
one ordinary native bearing. Each has an installed controller with LAUNCH target
10. Every 4D car boards one native pig passenger, checks assembly mass, and closes
restraints normally. Four families dispatch; PAX remains retained because its
winch owns propulsion. The ordinary bearing has no addon vehicle record.

After initial native settling, 100 ticks with empty FE buffers do not launch any
fixture. Legacy drift is **0.07516 blocks**; the other five record zero
longitudinal drift. Filling each anchor's real capability accepts **100,000 FE**.
Another 100 ticks launches the four dispatched families and ordinary bearing;
PAX remains stationary with its entire buffer intact.

The initial successful Engineered-only observation measured:

| Fixture | Supplied distance (blocks) | Speed (blocks/s) | FE remaining before STOP | Peak FE recovered during STOP |
| --- | ---: | ---: | ---: | ---: |
| Controlled Arrow | 16.1713 | 6.2574 | 96,265 | 892 |
| Legacy ZacSpin | 16.2602 | 6.1738 | 95,277 | 924 |
| S&S Free Spin | 16.5383 | 6.4313 | 91,025 | 2,007 |
| Revised ZacSpin | 16.5507 | 6.4395 | 90,561 | 2,232 |
| PAX retained | 0 | 0.00056 | 100,000 | 0 |
| Ordinary bearing | 17.4545 | 6.9347 | 99,649 | 68 |

Actual native STOP reduces each moving speed below **0.016 blocks/s**. The test
samples the buffer throughout braking: positive recovery is required before
later positional holding can spend energy. The ordinary bearing has a net
**-68 FE** change by the final STOP observation despite its earlier **+68 FE**
peak. Four 4D families also spend some recovered energy while settling at their
hold position. This is not a claim that the whole STOP period always increases
stored FE or that every braking impulse is fully recovered.

Normal carrier capture then permits restraint opening and unloading. Supplying
full buffers, retaining all five 4D vehicles, and setting LAUNCH target 20 for
100 more ticks produces **zero measured longitudinal drift and zero FE loss**.
Passenger mass appears once, ordinary behavior remains ordinary, and no forced
velocity/angle writes drive these phases. Fixtures are removed at completion.

## Initial failure and scope

The first test incorrectly required a net energy increase after the entire STOP
period. It failed on the light ordinary bearing after the other five results.
The configuration restoration hook ran even on that failure. Sampling the real
buffer during braking established both its regeneration and later holding cost;
the corrected assertion requires actual braking recovery and preserves the
final net measurement. The passing run also checks final stop, capture,
unloading and supplied retention, rather than stopping at launch evidence.

The committed `fe-native-*.txt` logs and `fe-unit.json` identify the final dev.5
results and artifact. Four profiles retain all registered tests; the standalone
and Styles-only FE availability guards are explicitly not FE operation evidence.
All four final profiles pass **24 registered native GameTests**; all **24 JUnit
tests** pass. Without Engineered, its four operation-specific scenarios report
not exercised and pass availability guards. With Engineered, all four execute
their installed operating scenarios. `fe-artifact.sha256` records the dev.5 JAR;
the earlier styled GUI evidence retains its separate dev.4 hash.
This bounded flat/native-capability test does not certify a particular generator,
cable mod, real-player controller GUI sequence, FE network reload, every layout,
graphics backend or a functional external track provider. Those remaining
operator/lifecycle/rendering checks retain their own acceptance gates.
