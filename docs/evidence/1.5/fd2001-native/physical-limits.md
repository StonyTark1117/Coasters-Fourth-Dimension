# Additional physical derivative and stopping observations

Owner `/root/fd13_body_design`, 2026-11-22, primary `main` over `f85f4a27`.
These records supplement the accepted distinct-mechanism endpoints; they do not
reopen them or equate command-polynomial limits with measured physical loads.

## 90° tilt

`/mnt/NVME4/fourthdimension-runs/fd15-moving-native-20261122/tilt90-1.log`
passed 1/1 with clean save/shutdown. `world-tilt90-1` and
`tilt90-1-source.sha256` preserve the world and source boundary.
`tilt90-physics.csv` records 696 native pre-physics samples, each 0.025 seconds.
The actual native Wing/bed reached the 90° socket with position error 0.000838
blocks, retained identity and normal production coordinator state. Mid-travel
operator interruption required measured stopping and explicit Resume.

| Quantity | Normal motion including Resume | Braking interval |
| --- | ---: | ---: |
| COM speed (blocks/s) | 0.1941814 | 0.1425997 |
| Angular speed (rad/s) | 0.2128654 | 0.1552349 |
| COM acceleration (blocks/s²) | 0.6677081 | 6.4447309 |
| Angular acceleration (rad/s²) | 0.0741544 | 6.9196372 |
| COM jerk (blocks/s³) | 26.7083236 | 260.4104755 |
| Angular jerk (rad/s³) | 0.2788902 | 289.9392381 |

Maximum physical bed-origin excursion after interruption was 0.000807267 blocks;
orientation excursion was 0.000713373 radians. Both speed thresholds were first
met 0.15 seconds after interruption and remained met through the observed hold.
The first resumed wake contributes the normal-motion COM jerk peak; before
interruption the observed peak was 5.9400 blocks/s³.

Velocities come from the actual native solver. Acceleration and jerk are finite
differences between successive samples, not continuous-time analytic bounds.
The linear velocity is center-of-mass velocity, whereas the saved pose and
stopping excursion describe the authored bed origin. A pitched rigid body can
move its COM while that origin stays nearly fixed. The angular velocity is a
spatial vector, not separate Euler-coordinate derivatives. Consequently these
numbers must not be compared as if identical to program-origin and Euler limits.
They include native braking/wake transients and are not rider comfort claims.

The source's finite actuator force limits remain unchanged. Full-show turn
measurements and the PAX physical handoff are tracked separately until completed.

## Remaining preset measurements, 2026-11-26

Root's `fd15-root-20261122/limits-1.log` passed all six required measurement cases
on Java 21, with clean all-dimensions save/shutdown and Gradle exit 0. The retained
world is `world-limits-1`; `limits-1-runtime-output.zip` preserves the actual
compiled classes/resources. `limits-1-manifest.json` identifies the six immutable
25ms native-step CSVs under the native run root. This fills missing derivative and
braking observations; the previously accepted endpoint cases were not reopened.
One original Wing, a 16-block bed and the largest listed preset were used per
case. Fake operators and helper construction remain disclosed.

Numbers below are maxima of actual native readback/finite differences, not
analytic command derivatives. Normal includes the first resumed wake. Linear
readback describes COM; angular derivatives describe spatial vectors. Units are
blocks, seconds and radians. Each row's native total retained mass was 61, with
linear motor cap 1464 per axis and angular motor cap 14640 per axis.

| Preset | Normal linear speed / acceleration / jerk | Normal angular speed / acceleration / jerk | Braking linear acceleration / jerk | Stop position / angle excursion | First sampled stopped seconds |
| --- | --- | --- | --- | --- | --- |
| Drop 12 | 3.3898 / 2.0108 / 51.89 | 3.0079e-7 / 6.2444e-6 / 0.00036674 | 35.081 / 1040 | 0.289871 / 0 | 0.6 |
| Side-drop 8+8 | 3.2193 / 4.0414 / 189.57 | 0.0055358 / 0.14006 / 9.996 | 27.49 / 1541.3 | 0.138738 / 0.000311556 | See note |
| Transfer 16 | 1.8541 / 1.6488 / 65.952 | 0.0059294 / 0.1407 / 8.4557 | 24.095 / 980.7 | 0.0368893 / 0.000289083 | 2.2 |
| Compound 180°/15° | 0.082562 / 2.8279 / 113.12 | 0.21564 / 0.4872 / 19.488 | 3.0682 / 122.92 | 0.0000121467 / 0.000403366 | 0.15 |
| Teeter three cycles | 0.15176 / 0.72311 / 28.971 | 0.16675 / 0.10779 / 0.46798 | 6.6996 / 268.3 | 0.000797459 / 0.000820762 | 0.15 |
| Elevator 32 | 1.8805 / 0.2661 / 9.0887 | 4.8623e-7 / 1.3279e-5 / 0.00061758 | 35.046 / 1453.7 | 0.0241394 / 0 | 0.225 |

Side-drop's collector reported firstStoppedSeconds=-1: its pre-physics samples
did not catch the sleeping transition before the server-tick guarded Resume.
The final braking sample at 4.700s still measured COM speed about 0.0826 blocks/s;
Resume independently required `MovingNativeAdapter.stopped` and passed. Therefore
this record supplies excursion and the observed 2.4s braking interval, but does
not invent a sampled first-stopped timestamp. All six reached measured stopped,
locked endpoints after explicit Resume with original retained identities.

These observations establish the finite solver behavior of the selected loads,
including emergency and wake transients; they are not continuous peak bounds,
universal maximum-mass performance, or passenger comfort limits. The separate
maximum admitted four-car/full-show check supplies the declared workload case.
The command caps stay 2/1/2 for level linear speed/acceleration/jerk, 4/2/4 for
drop/side-drop, and 15°/s, 7.5°/s², 15°/s³ per angular coordinate. Native motor
caps remain 24×mass linear and 240×mass angular per axis; gravity and COM rotation
explain why native derivatives must not be advertised as those command caps.
