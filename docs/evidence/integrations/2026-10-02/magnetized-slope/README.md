# Occupied Magnetized inclines and gravity rollback

Magnetized 1.1.0 accelerates occupied cars uphill and amplifies gravity-driven
rollback on the tested original inclines. Its 100% brake removes more than 90%
of approach speed in these cases, but it does **not** provide a rigid station hold.
The observed cars continue creeping downhill. Normal server capture restores
carrier alignment and boarding readiness, but also does not retain an exact world
position on this incline. This checkpoint records both capabilities and limits;
it makes no inclined-station holding claim and changes no gameplay adapter.

## Native experiment

Eight independent lanes pair powered and unpowered cars for Arrow, legacy ZacSpin,
Free Spin and revised ZacSpin. Each lane is original matching functional track,
rising 12 blocks over a 60-block horizontal run (approximately 11.31 degrees).
Every car carries one native seated pig at full health, with closed restraints.
The native chassis and all its original carrier bodies remain active. PAX is
excluded from this external-drive experiment; its existing powered winch/catch
interlock and interruption tests remain in the suite.

The test uses these phases:

| Test tick | Action or measurement |
| ---: | --- |
| 20 | Create the cars halfway along their inclines; configure paired MAGNET anchors at 10%, powered only on one lane per family. |
| 100 | Require normal boarding readiness, board one rider per car, close restraints and dispatch through the server operation. Set each chassis/carrier's linear velocity to zero once. Record release. |
| 130 | Measure gravity rollback without a backward impulse. Both lanes must descend; powered lanes must accelerate downhill more than their unpowered controls. Turn on 100% BRAKE for every lane. |
| 180 | Measure braking; remaining speed must be below 10% of that lane's approach speed. Configure 30% MAGNET, powered only on the designated lanes, and supply one 2-block/s uphill velocity to each chassis/carrier. |
| 220 | Measure uphill travel. Powered cars must advance uphill and accelerate beyond their initial speed and their unpowered controls. Turn on 100% BRAKE for all lanes. |
| 270 | Require another greater-than-90% speed reduction. Record remaining motion. |
| 280, 290, 300, 310 | Observe the powered brakes for another 40 ticks without capture or velocity forcing. After the last observation, request normal server capture. |
| 490 | Record position and speed; require carrier alignment/boarding readiness, original seat identity, full rider health, restraints and matching support. Open restraints and verify the pig can leave the native seat, then remove the fixture. |

The two velocity assignments are setup inputs, not per-tick control. Rollback starts
from rest and receives no backward seed. The incline's actual tangent is checked
from native geometry. Each measurement requires the chassis to remain more than
four blocks from either end, with its original matching track binding and no fault.
The evidence audit also checks that each measured chassis center follows its initial
12:60 incline line within 0.005 blocks in the recorded vertical plane. Thus normal
end-of-track braking or free fall does not supply the measured Magnetized result.

Both Free Spin lanes also have original passive INDUCE equipment along the incline,
targeting carrier group 0 only. Its recorded impulse count must be positive only
for that group, and its cumulative audited energy change must be non-positive.
Groups 1–3 receive no seat-magnet impulses. This checks the separation between
Magnetized rail propulsion and our passive carrier equipment.

## Results and limitations

| Profile | Powered uphill speed | Worst remaining speed after braking | Downhill brake drift, 40 ticks | Downhill capture drift, 180 ticks |
| --- | ---: | ---: | ---: | ---: |
| magnetized | 3.465–8.453 blocks/s | 6.28% | 0.126–0.242 blocks | 0.241–0.320 blocks |
| combined | 3.465–8.453 blocks/s | 6.28% | 0.126–0.242 blocks | 0.241–0.320 blocks |

There are ten recorded phases for eight cars per optional profile: 160 phase
samples across Magnetized-only and combined. Every sample retains the original
native rider seat, full pig health, closed restraints, matching support and no
fault. The final capture check measures carrier readiness and restraint release;
it is **not** evidence of a rigid hold, a safe dismount platform, or human unloading.
The fixture discards the pigs after checking native seat release.

The readiness threshold allows small residual velocity. It must not be described
as an exact position lock. The observed creep is present after capture, when the
external-drive policy blocks Magnetized's callback. This result therefore does not
justify changing optional drive ownership or bypassing the existing interlocks.
Use the separately verified level-station workflows; inclined station holding is
not certified by this checkpoint.

The tested settings are Magnetized acceleration 32 blocks/s² and speed cap 128
blocks/s, with 10% rollback acceleration, 30% uphill acceleration and 100% brakes.
They are fixed observed configuration values, not guarantees for other settings,
loads, grades or speeds. This is single-car, one-pig-per-car, straight fixed-world
track evidence. Longer or linked trains on inclines, curves, moving track, chunk
loss, multiplayer equipment rendering and arbitrary loading remain separate gates.
No optional provider becomes a functional Arrow 4D provider through this work.

## Preserved failures and reporting correction

The initial test expected less than 0.15 blocks/s after braking, adapting the prior
flat-track check. Its first run failed on powered legacy ZacSpin after uphill
braking, at approximately 0.2173 blocks/s. The other observed brake samples also
showed continuing downhill velocity. The native failure log and initial fixture
are retained as `first-*`; that failed expectation is not presented as fixed.

The final experiment distinguishes substantial deceleration from position holding.
It requires greater than 90% speed reduction, records four additional creep
observations and measures the result of normal capture. This is an explicit
qualification of the observed capability, not a fix for the failed station-hold
assumption. The retained logs make the residual movement auditable.

The first observation run passed all 38 native tests and recorded all 80 samples,
but its completion marker still reported six main phases, omitting the four added
observation phases. The runner rejected that metadata. `observation-magnetized-*`
retains the successful native log; `observation-audit.json` records the failed
runner audit. The marker was corrected to ten and the final native profiles rerun.
No existing gameplay class was changed in either follow-up.

The next Magnetized-only run passed, but the combined run exhausted the existing
64-vehicle dimension limit while the slope cars shared the default concurrent
batch with the other optional fixtures. `prebatch-magnetized-*` and
`capacity-combined-*` retain both results. The slope test now has its own sequential
`fourthdimension_magnetized_slopes` batch; the vehicle limit and gameplay policy
remain unchanged. Final profiles were rerun with that registration.

## Verification

Final Magnetized-only, combined and base runs each passed all 38 registered tests.
There are five absent-addon skips in Magnetized-only, none in combined, and eight
in base. The base profile skips the new slope test and verifies that the optional
helper remains isolated when the addon is absent. Nineteen JUnit tests also pass.
Combined enables FE-required PAX recovery. Existing ordinary-cart access, occupied
PAX interruptions and the other integration regressions remain in the suite.

Full loaded mod lists and unmodified compressed logs are retained. The common
runtime is Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10, Coasters Simulated
0.1.5 and Sable 2.0.3. Combined also loads Engineered 1.1.1, Track Styles 1.2.0,
Additions 0.2.0 and Immersion 0.2.3 with their listed companions.

Artifact SHA-256: `1842fda2f44f7ee9b83d5ef56939057d4f3a6f00d918b1b372871fbd4481d0e8`.
Local immutable copy: `/home/braydon/.cache/fourthdimension-48535694/magnetized-slope.jar`.
Compared with the ordinary-access artifact, only the GameTest registration class
changes, and the new slope helper and its Lane class are added. All existing
gameplay, resource and adapter entries remain byte-identical. All three profile
launchers exited and were gone before packaging.

Audit from the repository root:

```sh
python3 scripts/verify-magnetized-slope.py docs/evidence/integrations/2026-10-02/magnetized-slope
```

Repeat the experiment with Java 21 and `build runGameTestServer
-PfdIntegrations=magnetized`, then the combined comma-separated profile with
`-PfdPaxEnergyReload`, and finally the base profile. The retained `matrix.py`
records the exact commands and ran from ignored `build/magnetized-slope`.
Profiles must run sequentially because they share a GameTest world/output directory.
