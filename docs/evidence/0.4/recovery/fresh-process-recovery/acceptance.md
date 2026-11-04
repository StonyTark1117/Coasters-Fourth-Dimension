# FD402 fresh-process recovery lifecycle checkpoint

This bounded checkpoint proves that a normal occupied Arrow recovery group can
survive an actual client/server process shutdown and complete its normal
release in a second process. It does not certify the full multiplayer or
all-family lifecycle matrix.

The opt-in pause fixture creates the normal recovery example, boards the local
player, closes restraints, retains the car, and uses the production select and
attach operations. It then remains braked and linked; the first client exits
only after observing the rendered native link. No second example is built.

On the next process, the resume fixture rejects an empty world, a missing
ordinary bearing, a missing native link, a non-retained car, a non-braked car,
or missing original motor/lever controls. It accepts only the saved linked
chassis and bearing IDs. The normal player restore path remounts the rider;
the fixture then starts the native chain, brakes, detaches, opens restraints,
and unloads the rider through the production operations.

The development-client check was run in two separate JVMs against one world:

1. Pause stage: linked `af475614-e2a8-4295-b8e9-b3d7b382bfea` to ordinary
   bearing `00133676-a77d-413f-849c-d7d2815edc45`, and exited successfully
   after 40 rendered client samples.
2. Resume stage: restored that same native link and mounted player, observed
   more than 5.7 blocks of chassis and bearing displacement, then passed the
   normal brake/detach/restraints-open/unload lifecycle. The process exited
   successfully.

Reproduce with an empty validation world for the first process, then use the
same game directory and world for the second:

```sh
./gradlew --offline --no-daemon runClient \
  -PfdCarrierFixture -PfdCarrierFixtureRecovery \
  -PfdCarrierFixtureRecoveryPause \
  -PfdCarrierValidation -PfdCarrierValidationExit \
  -PfdCarrierValidationRecovery -PfdCarrierValidationRider \
  -PfdCarrierValidationRender

./gradlew --offline --no-daemon runClient \
  -PfdCarrierFixture -PfdCarrierFixtureRecoveryResume \
  -PfdCarrierFixtureRecoveryCycle \
  -PfdCarrierValidation -PfdCarrierValidationExit \
  -PfdCarrierValidationRecovery -PfdCarrierValidationRecoveryMotion \
  -PfdCarrierValidationRecoveryCycle -PfdCarrierValidationRider \
  -PfdCarrierValidationRender
```

This remains a fixture-operated one-player Arrow scene. It does not establish
ordinary player tools/UI, Engineered/FE operation, all recovery families,
full multiplayer behavior, or broad rendered clearance.
