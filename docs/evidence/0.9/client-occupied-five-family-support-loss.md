# FD902 packaged occupied five-family support-loss checkpoint

This FD902 checkpoint validates the existing fail-closed matching-track rule on
an occupied packaged client for every original family: Arrow, legacy ZacSpin,
S&S Free Spin, revised ZacSpin, and the PAX shuttle. It extends the prior
Arrow-only client result rather than treating Arrow as evidence for the other
carrier topologies.

## Method

The packaged `0.9.0-dev.27` JAR SHA-256 was
`bbb276f82186be4545dc5547600ea4f672306dd460090a116a67f6813c1a4a8f`. The validation fixture now accepts a requested rider family. Each run
creates only that family on its original functional track (the PAX run creates
its four chassis), waits for `SeatBlock.sitDown` to board the connected player,
and removes that vehicle's actual Coasters anchor-peer curve.

For each family, the client requires 40 consecutive synchronized carrier-angle
samples, a retained mounted player and carrier-seat assignment, and the exact
replicated state `stopping=true`, `restraints=true`, with fault `Matching track
support lost`. No fake rider, synthetic track state, or generic cart is used.

The four additional client commands differed only in the family values:

```sh
xvfb-run -a ./gradlew runClient --no-daemon -PfdPackaged \
  -PfdWorld=FD<Family>OccupiedSupportLoss \
  -PfdCarrierFixture -PfdCarrierFixtureRider -PfdCarrierFixtureRiderOnly \
  -PfdCarrierFixtureRiderFamily=<ZACSPIN|FREE_SPIN|ZACSPIN_REVISED|PAX> \
  -PfdCarrierFixtureSupportLoss -PfdCarrierValidation \
  -PfdCarrierValidationRider -PfdCarrierValidationSupportLoss \
  -PfdCarrierValidationSupportLossFamily=<same-family> \
  -PfdCarrierValidationTimeout=4000 -PfdCarrierValidationExit \
  -PfdCarrierValidationSamples=40
```

## Result

All four new runs passed and shut down normally. Legacy ZacSpin retained one
rider across two carriers (maximum angle error `2.2530695589914897E-6`
radians); Free Spin and revised ZacSpin retained one rider across four carriers
(`1.2657914816239955E-5` and `5.791172961219245E-6` radians); and PAX retained
one rider across its eight carrier bodies (`3.370362156292002E-13` radians).
Arrow was re-run through the same selector path, completing the five-family
set. The selected runtime records are in [the five-family support-loss log](client-occupied-five-family-support-loss.txt).

## Limits

These are stationary, single-client support-loss stops. They do not cover
loss during a moving route, repair or rebinding, concurrent riders, player
mesh/camera behavior, permanent disassembly, or continuous visual clearance.
Those remain FD402, FD902, and FD904 acceptance work.
