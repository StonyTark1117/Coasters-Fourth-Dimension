# Engineered equipment around an occupied PAX shuttle

This native fixture exercises Engineered 1.1.1 around the existing compact PAX
catch/lift/gravity-return test. Four physically coupled cars carry sixteen native
pig passengers. One native Engineered motor span covers all three route segments;
a controller and observer stand outside the moving train's clearance envelope.
Production integration behavior is unchanged by this test addition.

The fixture configures real upstream commands as the winch changes phase:

| Winch phase | Engineered command |
| --- | --- |
| Lifting | Backward LAUNCH, target 100 blocks/s |
| Power-loss fault | Forward LAUNCH, target 100 blocks/s |
| Outbound gravity run | STOP at span position 0 |
| Returning | TRIM to 1 block/s |
| Capturing | Backward LAUNCH, target 100 blocks/s |
| Final boarding | Forward LAUNCH, target 100 blocks/s |

These are competing command requests, not permissions to propel PAX. The existing
adapter preserves winch ownership and native presence reports. Each moving/fault/boarding
phase must have more than two ticks in which the native controller reports driving,
which excludes merely configuring equipment that never sees the train. The real
observer must detect a pass. The upstream default `requireEnergy=false` is asserted,
so a depleted FE buffer cannot explain the result. No artificial chassis velocity
or physics bypass is applied. Phase changes configure command modes through the
upstream API; this does not test a user-wired automatic controller sequence.

The parent test requires the real carriage, occupied lift above y=195, intentional
winch-power failure, less than 0.3 blocks of rollback during the hold, refusal to
open restraints in the interrupted state, explicit resume, gravity travel through
the station, all four movement phases, final capture/boarding, intact four-car
coupling and all sixteen passengers still mounted. It then opens restraints and
unloads normally after a further twenty ticks of occupied boarding hold with
external equipment active. Shared telemetry must retain four cars, sixteen seats and the
current winch phase throughout the cycle.

The first fixture placed solid equipment two blocks from the rail centerline.
A coupler snapped under tension during lifting and the test failed; the retained
`attempt1.txt` records this rejected setup. The corrected fixture places its
controller twelve blocks from the centerline, outside the moving carriers. This is
a fixture clearance correction, not a production motor-guard change.

Run with Java 21:

```sh
./gradlew build runGameTestServer -PfdIntegrations=engineered
./gradlew runGameTestServer -PfdIntegrations=engineered,styles,additions,magnetized,immersion
./gradlew runGameTestServer
python3 scripts/verify-engineered-pax.py docs/evidence/integrations/2026-09-30/engineered-pax
```

The check is attached only for exact Engineered 1.1.1 inside
`sixteenRidersPhysicalCarriagePowerHoldGravityReturn`; it does not add a registered
test. The combined profile also runs the existing powered Additions fin and
Magnetized 100% equipment on this same occupied route. Base-profile execution
proves optional-class isolation, not Engineered compatibility. Per-profile mod
lists, filtered results, warning excerpts and full-log hashes document the runs.
Native Gradle tests execute compiled classes; the artifact/source hashes identify
the accompanying built JAR and final fixture sources.

Final results:

| Profile | Registered result | Explicit optional skips |
| --- | --- | --- |
| Engineered only | All 25 passed | 3 (Additions single/linked and Magnetized) |
| All five addons | All 25 passed | 0 |
| Base | All 25 passed | 4 |

The build retained 19 passing unit tests. `verification.json` records the parsed
command and presence evidence. Both final optional profiles report twenty native
controller-presence ticks at final boarding. Earlier corrected runs before that
extra hold are identified separately in `runs.json`. Known dependency warnings and
startup lag messages remain in the warning excerpts; these results do not establish
warning-free startup or performance acceptance.

This evidence is limited to the compact fixed route and default energy mode. It
does not certify FE-on behavior, other configuration extremes, every shuttle
layout, player clients, occupied restart with this equipment, chunk interruption,
addon removal, rendering, or performance. PAX remains driven by its winch and
mechanical catch; external motors are intentionally excluded in every phase.
