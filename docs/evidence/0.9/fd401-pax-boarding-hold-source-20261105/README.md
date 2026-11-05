# FD401 PAX cold-reload boarding-hold test

The combined Styles + Engineered GameTest run on 2026-11-05 executed the new `ShuttleBoardingHoldGameTests.reloadedBoardingHoldSettlesMovingTrain` method against the current source production classes. The test was in the `fourthdimension_engineered_availability` batch, which the retained log shows started with exactly two tests; the other method in that batch is `EngineeredAvailabilityGameTests.nativeMotorRejectsTransientlyUnavailableBodies`. The final GameTest summary lists 69 total tests and 27 named required failures. `reloadedBoardingHoldSettlesMovingTrain` is not among those failures, so this test passed.

The test reconstructs a stale persisted PAX winch, introduces non-neutral carrier angles and train motion, then verifies the restored station hold returns all four cars to boarding readiness, stops the chassis, retains all four native links and permits a trailing-car seat mount.

This is a source-run with the untracked test source included. The production source baseline is `ec2fcda` (the fix commit); subsequent commits through `9975476` changed only artifact/evidence records. The refreshed clean JAR is SHA-256 `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`. The candidate JAR built with this test class had one extra test-only entry and byte-identical production classes.

**The combined suite did not pass.** Its log records 27 required failures, including unrelated FE starvation, disturbance-guard and test-world capacity failures. This record claims only the PAX hold test's pass, not an overall green suite or profile qualification.
