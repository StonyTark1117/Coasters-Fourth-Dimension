# Engineered consist interlock regression

Release baseline: `8da29836e7c16da3036a01b95af08a1172273c62` (`0.9.0-dev.27`).
This checkpoint concerns Engineered 1.1.1 only. It does not close FD401 or the
full integration certification gates.

## Change

Engineered's native `MotorDrive.apply` callback previously checked capture,
maintenance and PAX ownership only on the body receiving power. An open restraint
or a fault did not independently reject propulsion, and a held linked car could
be pulled by a powered peer. The optional adapter now checks the registered
consist and denies the callback for any captured, retained, open-restraint,
faulted or PAX car. Missing/incompatible peer registrations fail closed.

The check still delegates ordinary recovery-cart decisions to the existing
`PropulsionInterlock`; native chain-drive and recovery logic are unchanged.
Only exact inspected Engineered versions activate the adapter. Ordinary and
prototype bodies retain their prior behavior.

## Native regression

`EngineeredInterlockGameTests` assembles two Arrow cars on an original 48-block
functional rail, waits for boarding readiness, couples them through native
Coasters links, then closes restraints and dispatches through `VehicleOperations`.
It directly invokes the installed Engineered callback on both ends of the train.
For each powered end it injects capture, maintenance, open restraints and a fault
on each car, then removes/restores the peer registration. Valid-launch controls
surround those nine rejection cases: 18 rejected calls and four positive calls.

The probe measures changes to Sable's queued propulsion impulse on both chassis
and every carrier. It restores the saved force/torque totals after each call so
one probe cannot propel or brake the next one. This tests callback authority
between physics steps, not physical travel under each injected fault. Existing
native operating, PAX, ordinary-bearing and recovery tests remain in the suite.

The first test draft looked for an immediate velocity delta. It failed its valid
launch control because the native motor queues an impulse instead. That run is
retained as `velocity-probe-attempt.log.gz`; it is not a compatibility result.
`compile-attempt.log.gz` preserves an earlier incorrect cleanup enum name.

With the corrected probe and the original guard, native launch produced a
1.3338716785601952 impulse both in the valid control and with the powered car's
restraints open. Capture and maintenance produced zero. The restraint assertion
failed as intended; the other 41 registered tests passed (including optional-absence skips). `before.log.gz`
and `before-source.json` retain that regression evidence. To reproduce the
failure, apply the new GameTest to the baseline commit without the guard change.

## Validation

All 26 unit tests and all 42 registered native tests in each of the four profiles passed.
Results and exact artifact/source hashes are recorded in `manifest.json`.
The final matrix builds once and loads the resulting JAR with `-PfdPackaged` in
standalone, Styles-only, Engineered-only and Styles+Engineered profiles. Optional
Engineered scenarios explicitly skip when its JAR is absent. A registered test
count is not proof that an absent addon's behavior ran.

Reproduce with Java 21 from the repository root:

```sh
./gradlew build
./gradlew -PfdPackaged -PfdIntegrations=engineered runGameTestServer
./gradlew -PfdPackaged -PfdIntegrations=styles,engineered runGameTestServer
./gradlew -PfdPackaged runGameTestServer
./gradlew -PfdPackaged -PfdIntegrations=styles runGameTestServer
python3 scripts/verify-engineered-interlock.py
```

Run profiles sequentially: each run recreates the same disposable GameTest world.
The native runs use the base stack in the mod-list sections of their raw logs.
The manifest pins optional artifact coordinates and SHA-256 hashes.

This evidence does not certify the full controller UI/redstone/FE-network
workflow, occupied multiplayer restart, every missing-component failure mode,
or arbitrary Engineered versions. The new fixture is an empty two-car Arrow
consist; existing occupied family tests provide their own bounded coverage.

The long-running tool shell later reported exit 143. The runner recorded exit 0
separately for the build and each of the four Gradle profiles; each raw log
contains its successful completion. No owned game server remained.
