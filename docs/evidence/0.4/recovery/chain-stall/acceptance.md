# FD402 loaded spike limit and powered-chain stall handling

Dev.18 advances **FD402 in the 0.4 stage toward 0.9.0**. The attempted whole-train
compact PAX spike recovery failed. The production fix diagnoses a powered stall
and brakes the original occupied group; it does not add propulsion or turn that
failed climb into a successful curve acceptance case.

## Failed whole-train climb

The diagnostic extends dev.17's actual occupied physical winch handover. Its
four original cars and eight carriers, with sixteen native passengers, are
retained and recovered by the same certified bare ordinary bearing. A native
chain spans the level station and outbound curved spike. The eight-RPM Create
motor supplies only the ordinary bearing. No 4D car receives separate drive,
commanded velocity or motion teleport.

Acceptance requires every original car to enter the outbound curved section,
rise at least 1.25 blocks above its starting height and reach a positive track
tangent Y of at least 0.2 before normal braking/disconnection/unloading. That
condition is never reached. Most cars remain on the level station. The bearing
settles near route distance 94.984 while chain input remains available at eight
RPM, commanding a speed magnitude of 0.5 blocks/s. Its measured speed is about
0.00315 blocks/s; the original recovery hold is inactive and all four faults are
empty. Native bearing mass is 1 unit versus 72.8 for the occupied train bodies.
These are game-scale mass values, not manufacturer measurements.

A separate initially placed unlinked bare bearing on the same powered spike
travels over 27.9 blocks while the occupied recovery group stays stalled. This
control establishes that the chain reaches and propels an unloaded bearing on
that curve. Together with the loaded standstill and active input, it supports a
native loaded-propulsion limitation rather than a lost chain span or recovery
hold. It does not establish a universal maximum grade, force rating or clearance.

`whole-train-negative-native.txt` records the diagnostics and explicit failure;
`whole-train-negative-results.json` pins its source/log hashes and failed scope.
`whole-train-diagnostic.java.txt` preserves our complete candidate test source.
In a disposable checkout of base commit `c23997d`, replace only
`RecoveryHandoverGameTests.java` with that archived source, set the development
version to dev.18, and run the standalone command below. The candidate has 34
required native tests; its whole-train climb fails. It is archived outside the
shipping Java source tree. Do not substitute it in a valuable active checkout.

## Production behavior and regression

While a recovery group is active and native chain speed is commanded above
0.1 blocks/s, the server observes actual refreshed bearing displacement. Staying
within 0.08 blocks for four physics seconds faults and brakes every original
car. Sampling displacement avoids mistaking the pre-step speed-match velocity
for progress after loaded couplers solve it back to a standstill. The fault is
“Native chain drive stalled: brake and inspect propulsion before resuming”.

Cutting chain power, unavailable equipment, and normal start/brake/disconnect
reset the transient progress window. The window is not saved; existing schema
and network protocol remain 6. Faults and recovery identity retain their normal
persistence. Native chain force and the 0.75-block/s recovery speed cap do not
change. Restored input never automatically clears the inspection fault.

`occupiedPaxBrakesOnPoweredNativeChainStall` uses a separate actual four-car
shuttle fixture. After occupied physical handover and attachment it waits ten
simulated seconds with the clutch cutting power: towing remains enabled and
no false stall is reported. Ordinary vanilla lever interaction then supplies the
same eight-RPM native chain. The loaded group enters the spike approach and
stalls. Automatic diagnosis brakes all four cars without changing any of the
sixteen original native seat mounts. Normal lever use cuts external power;
after four seconds all cars remain faulted, retained and ready. The motor stays
installed at eight RPM. Stationary recovery disconnection preserves the original
four-car links and all eight carriers; restraint opening precedes native unloading.

The corrected regression fails against unchanged dev.17 production code with
exactly one failure, “Powered native chain stall was never diagnosed or braked”.
All other required tests, including the short occupied handover/tow, pass.
`stall-before-source.json` and `stall-before-native.txt` pin that comparison.
The fixed profiles use the identical test-source hash and different production
source hash. An earlier exploratory continuous passenger check ran after intended
unloading; its completion guard was corrected before this pinned comparison.

## Final artifact checks

The standalone and combined `stall-*-results.json` records pin the same final
source manifest and reproducible dev.18 JAR. Each profile passes **35 required
native tests** and **25 unit tests**. Native excerpts include both the preserved
short occupied handover case and the new driven-stall markers. The combined
profile installs Track Styles 1.2.0 and Engineered 1.1.1; neither is bundled.

| Profile | Travel before diagnosed stall (blocks) | Max spacing error (blocks) | Max retained angle (radians) | Max sampled speed (blocks/s) |
| --- | ---: | ---: | ---: | ---: |
| standalone | 12.198078 | 0.00010153 | 0.00058113 | 0.453545 |
| combined | 12.194422 | 0.00013242 | 0.00058877 | 0.453750 |

Both final builds produce JAR SHA-256 `3a2da6dbaccefcb464d7123205052aa2d7c824d880ea0343f3372315f9b090c4`.

Reproduce with Java 21:

```sh
./gradlew build runGameTestServer
./gradlew build runGameTestServer -PfdIntegrations=styles,engineered
```

GameTests recreate only their disposable world. Avoid `clean` while valuable
packaged-client worlds remain in the build directory. The native checks use
permitted FakePlayer server handlers and actual bodies/seats/couplers/drives.
They do not establish rendered player input, client packets or safe player exits
on an elevated spike.

## Remaining scope

The certified bearing still has only the proven bounded recovery workflows;
the compact whole-train spike pull remains unsupported. A stronger drive or
added ordinary assembly needs separate fit, propulsion, braking and clearance
acceptance. Full curve/bank coverage, nonzero controlled profiles, all-family
rendered occupied recovery and the multiplayer/lifecycle matrix remain open.
FD402, FD404 and the 0.9 release candidate gate are not closed by this checkpoint.
Previous startup movement warnings and the freed bitmap-glyph crash retain
their separate investigations.
