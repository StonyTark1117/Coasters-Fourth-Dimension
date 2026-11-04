# FD402 occupied PAX native physics-substep clearance

This checkpoint extends the recorded native-physics-substep gate from the
two-car Arrow recovery train to the occupied four-car PAX recovery train. Every
paired Rapier half/full step inside the one-tick sampled interval preserves at
least `0.02` blocks of conservative clearance for native body/linkage, wheel,
rider, vanilla-player, carrier-rack, neighboring-car, and ordinary-bearing
geometry.

It does not interpolate through the unexposed trajectory inside a `0.025 s`
Rapier step, and it excludes unpaired startup/teardown steps. The later
[remaining-family checkpoint](../remaining-family-native-physics-substeps/acceptance.md)
closes the corresponding Legacy ZacSpin, Free Spin, and Revised ZacSpin gate.

## Production fixture change

`RecoverySubstepTrace` now supports one active recorder per family instead of a
single global Arrow recorder. `BankedRecoveryTrainGameTests` starts independent
Arrow and PAX traces while the two occupied native trains traverse together,
then closes each trace only after that train completes normal brake, power cut,
recovery detach, restraint opening, and unload. Trace headers pin family,
variant, original-car count, and the exact native body IDs.

The banked-train test uses the existing `fourthdimension_fd402` template
namespace, so the bounded FD402 run does not require the broad GameTest matrix.

## Focused physical run

The run used an isolated source snapshot, private Gradle state/output, and a
fresh Xvfb display so concurrent client sessions in the primary checkout could
not remove or replace its compiled classes:

```sh
xvfb-run -a -s '-screen 0 1280x720x24' \
  env JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  ./gradlew --offline --no-daemon runGameTestServer --console=plain \
  -PfdBuildDirectory=/tmp/fd402-pax-source-build \
  -PfdGameTestNamespaces=fourthdimension_fd402
```

All three required FD402 tests passed in 1 minute 45 seconds. The banked-train
case completed the two-car Arrow train with eight riders and the four-car PAX
train with sixteen riders. Both visited all five native route sections and
retained original seats, carriers, profiles, and original train links through
normal brake, power cut, recovery detach, and unload. PAX recorded a maximum
spacing error of `0.00027038`, carrier angle of `0.00070712` radians, speed of
`0.48951523` blocks/s, anchor offset of `0.00030238`, and frame error of
`0.04562214` degrees.

## Independent geometry and substep result

The current native mesh verifier passed all 6,951 PAX tick samples. The
family-general substep verifier then matched all 6,951 full steps bit-for-bit
and audited the 6,950 intervening half steps. The complete trace contains
14,222 sequential steps; 321 unpaired startup/teardown steps remain explicitly
outside the claim.

| Check | Tick-sampled bound | PAX substep lower bound | Required |
| --- | ---: | ---: | ---: |
| Body/linkage to native track | 0.04397748 | 0.03152361 | 0.02 |
| Full-spin wheel to non-contact track | 0.04397748 | 0.03159818 | 0.02 |
| Native rider to track | 0.08000000 | 0.06756206 | 0.02 |
| Vanilla player mesh to track | 0.08000000 | 0.06752334 | 0.02 |
| Native rider to carrier rack | 0.08000857 | 0.08000857 | 0.02 |
| Vanilla player parts to carrier rack | 0.06532928 | 0.06532928 | 0.02 |
| Other cars / ordinary recovery body | 1.27619986 | 1.25124655 | 0.02 |

The largest conservative motion from a PAX half-step to its nearest tick pose
was `0.01247666` blocks. [`verify.py`](verify.py) checks the preserved trace and
report hash chain, source/auditor pins, counters, family/variant/body header,
physical-run completion, and every acceptance threshold without launching
Minecraft.

This remains native/default-model evidence. It does not certify equipped or
cosmetic player layers, observed rendering, unanchored startup/teardown, or
continuous within-step motion. The later remaining-family checkpoint covers the
three single-car families that were still open when this PAX evidence landed.
