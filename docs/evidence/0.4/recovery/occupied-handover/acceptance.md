# FD402 occupied physical PAX handover and recovery

The dev.17 checkpoint advances FD402 in the **0.4 stage toward 0.9.0**. One
native test now uses the same occupied four-car PAX shuttle from its actual
powered winch through interruption, handover, ordinary-bearing recovery and
unloading. Earlier separate empty handover and occupied flat towing tests did
not establish that sequence.

## Fixture and operations

`RecoveryHandoverGameTests.occupiedPaxHandsOverItsWinchThenTowsAndUnloads`
constructs the actual compact shuttle example, assembles its four original cars
and seats sixteen native passengers in its eight original rotary carriers.
Normal restraint closure precedes starting its 128-RPM powered physical winch.
Unretained and active handover requests reject. The actual server editor handler
captures and retains the interrupted train, then removes the loaded physical
carriage/catch/guide and winch assignment without replacing train components.

After handover, the test initially places a certified bare ordinary bearing six
blocks beyond the end car on the same PAX route. It installs a native Coasters
chain span through the station and outbound spike with a Create motor, clutch
and vanilla stop lever. The eight-RPM motor supplies only the ordinary bearing;
the 4D cars have no separate propulsion. The initial ON lever cuts chain power.
Normal bearing selection and production editor attachment establish one recovery
group. Winch rebinding rejects while recovery is attached.

Explicit Start tow and ordinary vanilla lever interaction supply chain power.
During twelve seconds of towing, the test samples original train/recovery axle
spacing, every retained carrier angle and native car speed. All sixteen riders
remain on their original native seat entity UUIDs. Moving disconnection rejects;
Brake tow and normal lever power cut settle the group with its motor still
installed at eight RPM. Stationary disconnection preserves the original four-car
links, all eight carrier bodies and maintenance retention. Restraints open through
the normal editor action before every native passenger dismounts.

Only the permitted FakePlayer operator is repositioned to access controls. The
train is never teleported, given commanded velocity or driven by an artificial
actuator. This is actual native physics and server-handler acceptance; it does
not establish rendered player input or network-client behavior.

## Recorded results

`handover-standalone-results.json` and `handover-combined-results.json` pin the
source manifest, reproducible dev.17 JAR digest, unit counts and sampled metrics.
Their accompanying native excerpts retain the handover markers and complete
required-test/build outcomes. Each profile passes **34 required native tests**
and **25 unit tests**. The combined profile installs pinned Track Styles 1.2.0
and Engineered 1.1.1; the addon JAR bundles neither dependency.

The required towing checks are travel over three blocks, maximum axle-spacing
error below 0.15 blocks, retained carrier angle below 0.04 radians and sampled
speed at most the production 0.75-block/s cap plus 0.01 tolerance. All four cars
must have no faults at the end of powered towing. The JSON records measured
values rather than substituting these thresholds for measurements.

| Profile | Travel (blocks) | Max spacing error (blocks) | Max retained angle (radians) | Max sampled speed (blocks/s) |
| --- | ---: | ---: | ---: | ---: |
| standalone | 4.674103 | 0.00009073 | 0.00006371 | 0.453602 |
| combined | 4.675049 | 0.00012087 | 0.00006661 | 0.453750 |

Both builds produce JAR SHA-256 `25ae73967f43c0a5cd866e8cc0f122981a7681464b365d9bc66af17dca6f3ac0`.

Reproduce with Java 21:

```sh
./gradlew build runGameTestServer
./gradlew build runGameTestServer -PfdIntegrations=styles,engineered
```

GameTests recreate only their disposable world. Avoid `clean` while valuable
packaged-client worlds remain under the build directory.

The preceding generated-translation ordering correction, commit `4a42099`,
passed all eight push/PR CI jobs; its two workflow records are archived here as
`preceding-correction-*-ci.json`. Those results validate that preceding revision,
not the new occupied handover test. Current-revision CI is recorded separately.

## Remaining FD402 and release work

This closes the bounded **native occupied handover-to-recovery sequence**. It
does not close FD402 or FD404. Short travel on the actual shuttle route does not
prove that the whole four-car train negotiates the spike, nor complete curved or
banked axle/vehicle/rider clearance. Rendered occupied handover and all-family
operator acceptance, nonzero Arrow profiles, broader coupler strength/propulsion,
The later [isolated Engineered recovery/FE checkpoint](../engineered-fe/acceptance.md)
closes the bounded provider-power item for one occupied Arrow recovery group.
Rendered manual provider controls, all-family Engineered coverage and the full
recovery lifecycle matrix remain.
Prior movement-packet warnings and the freed bitmap-glyph client crash retain
their FD902/FD904 investigations; no client fix is claimed by this test.
