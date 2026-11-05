# FD401C four-car PAX motor guard

The Engineered-only named GameTest
`nativemotorrejectseachoccupiedpaxcar` was expanded from two to four native-linked
PAX cars and run on the project development host with Java 21. The real
`coastersengineered:linear_motor_controller` was configured for forward drive
on the 48-block native PAX rail. A real Pig occupied the rear carrier seat;
all four cars had closed restraints and no stop, maintenance or fault flag.
The test called the optional provider's native `MotorDrive.apply` once for
each chassis. It asserted that each callback was cancelled by the addon guard,
left zero queued propulsion impulse and kept the rider mounted.

The launch used an isolated build and server directory, Engineered-only profile,
`-PfdValidation -PfdNamedGameTestServer -PfdServerMaxHeap=1536M`, and an owned
systemd scope with `MemoryMax=4G`. The server listened on `127.0.0.1:25579`
with local RCON on `25580`. After startup, RCON ran exactly
`test run nativemotorrejectseachoccupiedpaxcar`. The test formed three native
links at distance 6.000 and printed:

```text
FD_ENGINEERED_INTERLOCK source=0 state=occupied-pax-car blocked=true maxImpulse=0.0
FD_ENGINEERED_INTERLOCK source=1 state=occupied-pax-car blocked=true maxImpulse=0.0
FD_ENGINEERED_INTERLOCK source=2 state=occupied-pax-car blocked=true maxImpulse=0.0
FD_ENGINEERED_INTERLOCK source=3 state=occupied-pax-car blocked=true maxImpulse=0.0
FD_ENGINEERED_PAX_INTERLOCK complete cars=4 blocked=4 traced=4 nativeCalls=4
```

The completion line precedes `helper.succeed()` in the fixture. The server
stopped through RCON and Gradle exited zero. The owned scope peaked below its
cap during observation. [Gradle console](gradle-console.log.gz),
[server log](server.log.gz), [stopped world](world.tar.gz), and
[hashes](SHA256SUMS) retain the run. The development classpath contained the
shared checkout's separate untracked `ShuttleBoardingHoldGameTests.java`.
The tested fixture class SHA-256 was
`c4aa752f2d8a77589301d9c65d085c5c48102842626787697348c7aaec1656c2`;
the runtime `EngineeredInterlock` and `EngineeredMotorGuardMixin` class hashes
were `bbdf31d93e8581c5eced971d33c46285119ee0e7d9358e25e687f7f2e9bbdfb9`
and `85e0572875e1b4daffb2d0b986195e9d44b7aa13e3ce6c7b63f3ce9f481451a6`.

This closes the **four-chassis native callback cancellation** observation in
FD401C. It does not prove that all four callbacks occurred naturally during
a moving winch cycle, or complete the Engineered-only and combined operator,
FE starvation/restoration and cold-reopen matrix. The earlier
[two-car callback run](../fd401-engineered-pax-callback-663aa12/README.md)
remains at its original JAR identity.
