# FD402 Engineered recovery/FE checkpoint

This checkpoint closes the bounded native Engineered 1.1.1 recovery-power item. It
uses the installed provider's native linear-motor controller and block FE
capability on the certified bare ordinary bearing in a production Arrow
recovery group. It is server-side integration evidence, not a rendered manual
operator or all-family Engineered claim.

## Isolated gate

The recovery FE GameTest now owns the `fourthdimension_fd402` template
namespace. Normal GameTest runs include that namespace, while the
`fdGameTestNamespaces` Gradle property can select it alone. This preserves the
full suite and prevents unrelated long-running fixtures or global vehicle
capacity from contaminating the recovery-power result.

The evidence run exported commit `3d56da7c651fd31ad5c1e73d4620cb76f059eec5`
and used a private build/game directory with Java 21:

```sh
./gradlew --offline --no-daemon runGameTestServer --console=plain \
  -PfdIntegrations=engineered -PfdPerformanceTargets \
  -PfdGameTestNamespaces=fourthdimension_fd402 \
  -PfdBuildDirectory=<private-output>
```

Exactly one required GameTest ran and passed in 2.186 seconds. The log loaded
Coasters: Engineered 1.1.1 and proves:

- zero movement while the supplied motor was recovery-braked;
- zero movement and zero invented FE after explicit start with an empty buffer;
- no FE consumption while supplied but still braked;
- 0.340515 blocks of occupied native-link towing after explicit start;
- FE fell from 100,000 to 99,680 during that tow;
- measured speed was 0.040958 blocks/s, below the 0.75-block/s recovery cap;
- the original rider remained mounted;
- braking retained 99,680 FE without further depletion; and
- teardown removed the vehicle, native bodies, rider and rail.

[`verify.py`](verify.py) checks the compressed log, exact committed sources,
provider/version markers, single-test boundary and all recorded metrics against
[`metadata.json`](metadata.json).

## Boundary

This does not certify a rendered Engineered controller or wire, manual player
input, provider power on all five recovery families, a stronger locomotive,
general grade/coupler ratings, or the remaining rendered/lifecycle matrix. The
separate FD401 Engineered evidence owns normal ride-controller and physical-wire
operation; this checkpoint is specifically the guarded recovery bearing.

## Rendered/manual Engineered-only closure

The [2026-11-11 connected-client record](../engineered-fe-manual-20261111/README.md)
combines this native result with rendered manual provider construction, physical
wire supply/starvation, occupied bearing selection and recovery controls, fresh
process reopen, explicit capped towing with measured FE draw, normal Brake tow,
detach, restraint opening and dismount. It closes FD402F1. The combined Track
Styles + Engineered profile remains the distinct open FD402F2 case.
