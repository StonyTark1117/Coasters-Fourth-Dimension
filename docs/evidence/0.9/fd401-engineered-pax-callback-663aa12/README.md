# FD401 occupied PAX per-car native motor callback

On 2026-11-04, the combined packaged validation client ran the focused
`nativemotorrejectseachoccupiedpaxcar` GameTest from source `663aa12`. The
archived [Fourth Dimension JAR](coasters-fourth-dimension-0.9.0-dev.27.jar)
has SHA-256 `3f5994739ad3d9c97628acff36378965867598488002f5ace91e65efc94f2ccc`.
The build used `-PfdPackaged -PfdValidation -PfdIntegrations=styles,engineered
-PfdFeNetwork`, an isolated Gradle build/game directory, and
`-Dneoforge.enabledGameTestNamespaces=fourthdimension
-Dneoforge.enableGameTest=true`. The [raw log](client.log.gz),
[pass screenshot](pass-chat.png), [stopped world](world.tar.gz), and
[hash inventory](SHA256SUMS) retain the run. The client saved and quit normally;
Gradle exited 0.

The test created two native-chain-linked PAX cars on a fresh 48-block rail and
mounted a real Pig in the rear carrier seat. With restraints closed and no
stop, maintenance or fault, it called Engineered's native `MotorDrive.apply`
once on each chassis. Both calls reached the actual optional motor guard's
validation trace and were cancelled; the queued impulse maximum was `0.0` for
both. The rider remained mounted. The log records:

```text
FD_ENGINEERED_INTERLOCK source=0 state=occupied-pax-car blocked=true maxImpulse=0.0
FD_ENGINEERED_INTERLOCK source=1 state=occupied-pax-car blocked=true maxImpulse=0.0
FD_ENGINEERED_PAX_INTERLOCK complete blocked=2 traced=2 nativeCalls=2
nativemotorrejectseachoccupiedpaxcar passed! (5016ms)
GameTest done! 1 tests were run
All required tests passed :)
```

This is a targeted native callback fixture, not an occupied four-car winch
cycle. The [earlier occupied PAX winch run](../fd401-engineered-pax-authority/README.md)
shows its operator outcome on a different build. The first fixture attempt
placed the track outside the native physics radius; the second used a
non-isolating positive control. Those failed attempts were corrected before
this passing run. This record makes no claim that Engineered supplied force to
the PAX train or that the prior live winch cycle invoked this callback on each
car.
