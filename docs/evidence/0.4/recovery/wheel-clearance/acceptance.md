# FD402 sampled banked native-wheel clearance

Dev.23 extends the recovery clearance verifier to the pinned Coasters native
wheel model. It is an incremental FD402 result in the 0.4 stage, not complete
rendered vehicle/rider clearance.

## Method

The verifier reads the ten wheel-model cuboids from the separately installed,
hash-pinned Coasters JAR. Every cuboid is enclosed in a full-spin volume around
its native local-X axle. It places front/back and main/upstop rows at the
inspected renderer offsets for each sampled chassis and ordinary-bearing frame
on the occupied banked recovery routes.

Those volumes are conservatively bounded by oriented boxes and tested against
the reconstructed original track mesh and volumes belonging to different
original cars. Only `rail0` and `rail1` are excluded from wheel checks: they are
the intended running-rail contacts for both wheel rows. Control rails, spine,
supports, brackets, neighbor cars and all lower-bearing/default-block checks
remain included. The verifier rejects unexpected wheel geometry/rotations and
requires at least one wheel-envelope check per route.

## Local result

The fresh combined Track Styles/Engineered dev.23 audit passed every sampled
route with a required gap of 0.02 blocks:

| Route set | Smallest sampled conservative wheel-envelope bound |
| --- | ---: |
| Five single-car routes | 0.04602209 blocks |
| Original Arrow/PAX train routes | 0.04608466 blocks |

The closest checked component is the new outboard support, not an excluded
running rail. The report covers 23,120–23,520 wheel envelopes per single-car
route and 39,360/69,800 in the Arrow/PAX train routes. CI repeats the verifier
in standalone, Styles, Engineered and combined profiles. The local verifier
SHA-256 is `455ba8a7e37473dcd073db1481de3aa8260462464ac7b017e96d7d9a2e15bcb5`.

Both exact-head dev.23 workflows passed on commit `0bd40e4`: push run
36950775897 and pull-request run 36950779404. Their eight jobs passed all 37
native GameTests, native route geometry and the extended recovery audit; Styles
and combined jobs also passed the installed-decoration audit. Job identities
are recorded in [ci-dev23.json](ci-dev23.json).

## Limits

Each observation is twenty ticks apart, so this is not continuous longitudinal
or curve/bank swept clearance. It does not model rendered players, 4D carrier
or linkage motion, optional decoration/resource overrides, shader/backend
behavior, real-client operator interaction, general structural strength or the
recovery lifecycle matrix. FD402, FD404 and the 0.9 gate remain open.
