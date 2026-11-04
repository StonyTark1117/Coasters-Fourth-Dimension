# FD402 remaining-family native physics-substep clearance

This checkpoint closes the recorded native-physics-substep gate for occupied
Legacy ZacSpin, S&S Free Spin, and Revised ZacSpin recovery vehicles. Together
with the earlier Arrow and PAX checkpoints, all five supported functional
families now have paired Rapier half/full-step evidence through a banked native
route and normal release.

Every paired substep inside each one-tick sampled interval preserves at least
`0.02` blocks of conservative clearance for native body/linkage, wheel, rider,
vanilla-player, carrier-rack, neighboring-body, and ordinary-bearing geometry.
The claim excludes unpaired startup/teardown steps and the unexposed continuous
trajectory inside one discrete `0.025 s` physics step.

## Focused isolated run

`RecoveryFamilySubstepGameTests` reuses the production banked recovery examples
but owns a dedicated `fourthdimension_fd402` GameTest. It starts one trace per
family, keeps all original native riders and seats occupied, traverses the five
route sections, and closes the trace only after normal brake, power cut,
recovery detach, restraint opening, and unload. `RecoverySubstepTrace` now takes
an output prefix so this focused test cannot replace the Arrow/PAX artifacts.

The run used a clean source snapshot, private build output, and a fresh Xvfb
display. It did not share a Minecraft display, Gradle project state, or run
directory with another session:

```sh
xvfb-run -a -s '-screen 0 1280x720x24' \
  env JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  ./gradlew --offline --no-daemon runGameTestServer --console=plain \
  -PfdBuildDirectory=/tmp/fd402-family-substeps-build \
  -PfdGameTestNamespaces=fourthdimension_fd402
```

All four required FD402 tests passed in 2 minutes 33 seconds. Each of the three
new cases carried eight riders through all five sections and retained the
original profile, seats, carriers, support, and release sequence. Maximum
spacing error was `0.00021312` blocks, maximum carrier angle was `0.00104147`
radians, maximum speed was `0.49784278` blocks/s, maximum anchor offset was
`0.00032562` blocks, and maximum frame error was `0.04752355` degrees.

## Independent geometry and substep result

The current mesh verifier passed 5,842 Legacy, 5,872 Free Spin, and 5,869
Revised one-tick samples. The family-general substep verifier then matched every
full step bit-for-bit and audited each intervening half step.

| Family | Paired steps | Body/wheel lower bound | Player/track lower bound | Neighbor lower bound | Required |
| --- | ---: | ---: | ---: | ---: | ---: |
| Legacy ZacSpin | 11,683 | 0.03122760 | 0.06723692 | 1.70064708 | 0.02 |
| S&S Free Spin | 11,743 | 0.03084601 | 0.06691886 | 4.99551311 | 0.02 |
| Revised ZacSpin | 11,737 | 0.03084677 | 0.06685394 | 6.87633185 | 0.02 |

The smallest carrier-local rider/player-to-rack bound is `0.06532928` blocks.
The largest conservative motion from a recorded half step to its nearest tick
pose is `0.01314606` blocks. [`verify.py`](verify.py) checks the preserved trace
and report hash chain, source and auditor pins, exact counters, design-to-runtime
family mapping, trace completion, physical-run completion, and every clearance
threshold without launching Minecraft.

This remains native/default-model evidence. It does not certify armor, held
items, capes, elytra, modded cosmetics, observed client rendering, unanchored
startup/teardown, continuous within-step motion, or the broader rendered
operator lifecycle.
