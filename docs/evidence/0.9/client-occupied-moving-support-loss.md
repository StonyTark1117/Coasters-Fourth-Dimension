# FD902 packaged occupied moving support-loss checkpoint

This bounded FD902 checkpoint covers an occupied client while a moving original
4D vehicle loses the matching Coasters curve to which it is currently bound.
It adds active-route loss to the earlier stationary five-family support-loss
checkpoint; it does not replace lifecycle, repair, or multi-client acceptance.

## Method

The packaged `0.9.0-dev.27` JAR SHA-256 was
`7745a1c8ca725334ce38711df790608af26d76009cd53066372dcaef094a643a`.
Each fresh single-player Xvfb world contained only one selected original family.
The fixture used `SeatBlock.sitDown` to board the connected player in a real
carrier seat, collected 40 pre-dispatch client angle samples, then used normal
production restraint closing and dispatch. Only after the server measured two
blocks of native chassis travel did it remove that vehicle's currently bound
Coasters anchor-peer curve.

The client validates all of the following before it terminates normally:

- it observed at least one block of client-native chassis displacement for the
  same family;
- the local player remains mounted in `create:seat` and remains assigned to the
  carrier seat; and
- the replicated vehicle is stopped and restrained with the exact fault
  `Matching track support lost`.

The four circuit runs used `-PfdCarrierValidationMotionPaxChassis=0`; the PAX
run required one moving PAX chassis. The fixture itself assembled the real
four-car PAX train through native Coasters links before dispatch.

## Results

All five packaged runs exited normally. The measured maximum client displacement
for the selected support-loss chassis was 11.2226 blocks for Arrow, 2.2902 for
legacy ZacSpin, 2.3441 for Free Spin, 2.7271 for revised ZacSpin, and 2.2072
for every PAX car. Each client record reports `supportLossAfterMotion=true`.

| Family | Carriers | Moving chassis observed | Maximum selected displacement |
| --- | ---: | ---: | ---: |
| Arrow | 2 | 1 | 11.222608499086881 |
| Legacy ZacSpin | 2 | 1 | 2.290152066529232 |
| Free Spin | 4 | 1 | 2.3440982981822973 |
| Revised ZacSpin | 4 | 1 | 2.727140722924312 |
| PAX shuttle | 8 | 4 | 2.2073184057675546 |

The exact selected records are in
[the moving support-loss log](client-occupied-moving-support-loss.txt).

## Limits

This is one local player on one moving vehicle per fresh world. It does not
exercise a repair or rebind after loss, concurrent riders or clients during the
loss, a complete route/capture cycle after the stop, player mesh or camera
behavior, or rendered clearance. Those remain FD402, FD902, and FD904 work.
