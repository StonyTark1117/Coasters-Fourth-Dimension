# FD402F2 combined Styles + Engineered recovery — passed

This 2026-11-11 connected-client record closes FD402F2. Together with
[accepted F1](../engineered-fe-manual-20261111/README.md) and the
[native FE checkpoint](../engineered-fe/acceptance.md), it completes FD402F's
finite ordinary-bearing Arrow power scope. No all-family FE claim is added.

## Observed result

The same Arrow chassis `a7c7e9cc-ad72-4a20-a5b7-32cadf402cd3`, carrier seats,
real player Dev and native bearing `576e024e-6a2e-4342-a4d4-32df84aaed8e`
remained attached and retained throughout the measured checks. Track Styles
1.2.0 and Engineered 1.1.1 were loaded together. The controller used a fixed
FORWARD LAUNCH heading, with FE required and the retained isolated producer/wire
fixture from F1.

| Observation | Measured result |
| --- | --- |
| Normal rendered Attach, then Start with zero FE | Native link formed at 6.000 blocks. Ticks 109952–110052: one mounted rider, closed restraints, no position change, FE=0. Maximum residual solver speed 0.001025 blocks/s. |
| FE restored while braked | Ticks 112284–112584: z=50.901061 throughout, FE=100,000 throughout, no automatic tow. Server ticks advanced with the editor open. |
| Rendered Start with supply | Ticks 112604–112744: z=51.016044→54.428321, FE=99,970→99,690, maximum sampled speed 0.492088 blocks/s, below the 0.75 ceiling. |
| Rendered Brake while towing | Last pre-brake sample 112884 has stopping=false and speed≈0.348; sample 112904 has stopping=true. Ticks 112924–114564 hold z=57.676220 and FE=99,684 without movement or further consumption. |
| Normal rendered cleanup | Power disabled; Detach removed the recovery/native bearing link, Open restraints succeeded, and held Shift dismounted Dev. Tick 115388 records attached=null, restraints=false, riders=0, mounted=false. The saved data confirms cleanup. |

`measurements.json` contains the sampled values and bounds. `starved.log.zst`
and `powered.log.zst` retain the complete corresponding sessions. Screenshots
017–019 show attachment/starvation; 024–028 show supplied hold, motion and
rendered Start/Brake; 029–030 and 033 show cleanup. The world saved every
dimension, returned to the title screen and exited normally. The owned client
exited before its Xvfb display was stopped.

## Fixture and evidence boundaries

F1 supplies normal bearing selection, provider construction/wiring and supply
controls; those unchanged shared paths are not repeated. Here validation-only
native placement restored a correctly spaced bare bearing, and the normal
Attach button created the recovery link. Controller sequence and isolated FE
buffer changes were command-fixtured. The editor helper only opens the existing
production screen: real clicks send its unchanged Start/Brake/Detach/Open
requests. It avoids equipping a mounted rider with the Rotation Tool, which
correctly invokes the equipment safety brake before a Brake-button observation.

Early setup attempts are preserved in `setup-attempts.log.zst`, not counted as
passes. An unattached bearing drifted during a long setup interval; temporarily
setting the controller to END allowed native placement and attachment. Four
unlinked spare bearings inherited from earlier F1 setup also consumed FE and
collided with the group after reopening. Their exact IDs and removal are logged.
Only the stable interval after that cleanup is used for supplied hold, FE draw
and Brake acceptance; this is not an additional cold-restart acceptance claim.

No production source, resource, dependency or build configuration changed since
F1 commit `5fab1bf3`; `git diff 5fab1bf3 -- src/main/java src/main/resources
build.gradle` contains only the new validation-only RecoveryFixtureValidation
class. F1 and native FE/operator acceptance carry forward. Source is preserved
as `RecoveryFixtureValidation.java.txt`; the actual packaged JAR hash and launch
arguments are included. No broader suite was rerun for fixture instrumentation.

## Runtime

Source checkpoint `f01e941b`; Java 21; packaged combined profile with
`fdFeNetwork`; 1536 MiB heap; Minecraft-only 6 GiB systemd unit; private Xvfb
`:278`; LAN port 25478 kept integrated-server ticks advancing during UI checks.
The final observed cgroup peak was 2,865,377,280 bytes. Gradle preparation ran
outside the client unit. Render distance 6, simulation distance 5.

Persistent run data and worlds remain at
`/home/braydon/.local/state/fourthdimension-runs/fd402f2-finish-20261111`.
`attached-ready-pre-final` preserves the intermediate attached world (including
spares, before the final cleanup); `game/saves/FD402FCombinedFinish` is the saved
final detached/unloaded world. Neither is an implicit assignment to rerun F2.
