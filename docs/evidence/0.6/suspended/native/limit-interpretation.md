# Native bounded-swing probe interpretation

The retained server log records the actual native generic-joint test, with two
cabins carrying two equal riders and one rider respectively. Each began at a
disclosed ±40° pose, then received signed angular impulse `20 I × .05` each of
40 server ticks. Peak angles were 0.7859817197 and 0.7892226532 radians (45.03°
and 45.22°), within the specified ±45° stop plus the .025-radian solver tolerance.
This deliberately forces both sides outward; gravity return alone is not proof.

The empty body's measured mass was 4 and pivot inertia 20.85046875. The native
loading delta was exactly .8 per rider. After reload (without the synthetic pig
riders), retained capture readback measured COM radius 2.203125. Its applied
outward torque budget is 417.009375; conservative opposing bounds are gravity
86.450625, native linear brake 52.875, and motor 208.5046875. Their sum is below
applied torque. The free-state motor is capped at 10 I and linear braking at
6 blocks/s². These bounds intentionally overestimate the opposing forces.

For the loaded probe, the empty first moment is 8.8125. The two riders' torso
centers are ±.5 laterally and -.75 below the pivot, yielding balanced first moment
10.0125 and asymmetric magnitude sqrt(9.4125² + .4²). Their additional point-mass
inertia is nonnegative. Thus even using only the measured empty inertia,
10 I > (9.81 + 6) × first moment for both loads: outward drive exceeds every
non-limit opposing torque. Remaining within the stop while loaded outward is
the boundary evidence, alongside the production native joint's ±π/4 limits.

The JNI aggregate angular-impulse norm was finite (.00107549/.00163178), but its
components do not expose documented separate limit impulses. Do not interpret
that norm alone as the hard-stop reaction or impose an arbitrary minimum on it.
The first run failed only such an arbitrary .01 assertion after recording the
successful outward-force boundary trace. That assertion was removed. Its later
capture requirement was checked separately on the retained bodies and passed;
no full fixture rerun is needed to combine these records.

## Connected journal correction

The final connected journal adds one unit of mass exactly at the unchanged pivot.
Empty native mass is5, pivot inertia21.0171354166665. Gravitational first moment is
unchanged; the conservative loaded torque argument above therefore still holds
with more inertia margin. The targeted repaired-journal native test separately
passed exact .8/rider loading, both stops (peaks .7857962204/.7857078706 radians),
capture/open and pose/seat persistence. This supersedes old mass4 measurements
for current production while retaining their useful diagnostic history.
