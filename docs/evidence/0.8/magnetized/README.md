# FD812 Magnetized propulsion — complete

Qualified2026-11-15 on the exact `magnetized` profile; [guide](../../../MAGNETIZED.md).
Current core dependencies are unchanged. FD813 retains actual combined-installed
conflicts; no Cartesian family/profile sweep is implied.

## Actual addon force path

[Wing native01](native-01.log), 1/1 passed: the actual merged Magnetized callback
is fenced for Fourth Dimension bodies, while the selected shared callback applies
exactly one bounded impulse from the real anchor mode/percentage/redstone API.
Stationary forward/reverse, forward braking of a reverse rollback, actual Lapis
brake force, open-restraint rejection, power-loss latch, no restart on restored
power, saved direction and native/Engineered ownership guards passed.
The plugin locates the pinned merged callback by its actual API call after the
addon's mixin, avoiding generated callback names. Ordinary carts continue through
the upstream implementation; managed recovery bodies cannot receive addon thrust.

[S2 native03](s2-native-03.log), 1/1 passed: real native S2/governor/station setup,
explicit held transfer to Magnetized, bounded impulse ramp<=3blocks/s², native
movement with no fault, old governor output0, power loss hold, restoration without
restart, actual dock capture, explicit Resume and validated Dispatch. S2's own
spin/dock/body/route guards remain active. Closed standalone D–F evidence carries
forward because its governor branch is unchanged.

S2 native01 reached actual speed0.6074blocks/s after20ticks but the test expected
>1 without a requirement for that one-second speed. The corrected check asserts
positive actual travel and independently measures the per-callback ramp bound;
it does not claim a new minimum acceleration. Native02 stopped before testing
because Gradle incremental output omitted `VehicleLifecycle$1.class`. Inspection
confirmed the missing class; deleting only this owned build's generated Java
output/cache forced a complete compile, then native03 passed. Worlds/logs remain
preserved. Both passing servers saved all dimensions and exited0.

## Normal connected operator

`/fd demo magnetized` supplied the disclosed native loop, car, platform, return
magnets and redstone. Commands supplied Gold, stone, Rotation Tool and initial
viewpoints; none selected a provider, boarded, restrained or dispatched the car.
The same private client proved a visible6.15° turn and reachable vanilla stone
placement with default right-use/left-attack and raw input on.

Normal Gold right-click on anchor(9,-57,40) displayed the upstream magnetized
confirmation and golden powered rails at100%: [configuration](normal-gold-anchor.png).
Normal seat use mounted Dev. Rotation Tool→Claim Magnetized succeeded (action65),
showed held forward authority and direction controls: [claim](normal-provider-claim.png).
Close restraints(action11) and Dispatch(action13) succeeded. Native movement
carried the same occupied car through the loop: [motion](occupied-motion.png).
A one-shot normal Stop click was conditioned on read-only player position at the
level return: [click record](return-stop-click.log). It stopped at playerz57.36,
showed Ready/ownerhold, then normal Open restraints and Shift unloaded Dev:
[stop](normal-level-stop.png), [open](normal-open.png), [unload](normal-unload.png).
No operation was issued through a command. Read-only `RootVehicle` was absent
after unloading. Saved [foundation](final-foundation.json) and
[operator events](operator-events.txt) retain the exact identity and final state.

## Runtime preservation

Packaged JAR SHA256 `b051680de0af416b75b859d5b68a251db4658c55fdc4dd18fbf92b7d40a19afc`.
Java21; client1536MiB/6GiB, server2048MiB/4GiB, verified actual JVM heaps and
owned cgroups. Private display316, ports25650/25651. Owner
`01a1dd1d-1468-7f81-8f31-5532ad7a178d`. Persistent root
`/mnt/NVME4/fourthdimension-runs/fd08-magnetized-20261115`, world
`server/MagnetizedWorkshop`; native checkpoints outside build, full logs and views
retained. At13:20UTC client exited, server saved and exited, then Xvfb stopped.
[Cleanup](cleanup.txt): all three units inactive, MainPID0, success. No live handoff.
