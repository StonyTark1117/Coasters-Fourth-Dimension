# FD402 Engineered recovery motor interlock

This checkpoint adds one bounded provider-integration regression for the pinned
Coasters: Engineered 1.1.1 release. It is not a claim of full Engineered or FE
recovery acceptance.

`EngineeredInterlockGameTests.engineeredMotorRespectsRecoveryBrakeAndExplicitTow`
creates the production Arrow recovery example with a normal bare bearing. It
requires the six-block certified spacing, closes restraints, retains the car,
uses the production select/attach action, and verifies the native linked group.
It then lets the new native link settle, configures an installed Engineered
linear-motor controller, and gives the provider twenty normal server ticks to
register it while the recovery brake remains active. The braked bearing must not
move over those controller ticks; that avoids confusing initial native-link
settling with controller motion.

The test directly invokes the installed `MotorDrive` callback on the ordinary
bearing and restores the queued force accumulator after each probe. It requires:

- zero added propulsion force while recovery is braked;
- a nonzero force only after the production `Start tow` action; and
- measurable live bearing displacement over the following controller ticks,
  greater than the braked baseline; and
- zero added force again after the production `Brake tow` action.

This proves that the installed Engineered controller does not move a settled
braked recovery group, and that an explicitly started, retained native group
receives direct motor force and initiates live movement before the production
brake blocks force again. It also guards against configuring the controller early
enough to move a free bearing out of the certified coupling position.

## Reproduction

From the repository root, with Java 21:

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  ./gradlew --offline --no-daemon runGameTestServer --console=plain \
  -PfdIntegrations=engineered -PfdPerformanceTargets
```

The current run completed **51 required GameTests**, all passing. The new test
runs in the `fourthdimension_engineered_interlock` batch alongside the existing
linked-car motor guard.

## Boundary

The test is server-side. Its callback probes isolate policy checks; the separate
live displacement assertion establishes only initiation, not sustained towing.
It does not establish a rendered operator workflow, a complete live
controller/network or FE acceptance, braking distance, coupler strength,
all-family recovery, mesh clearance, or fresh-process lifecycle behavior. Those
FD402 acceptance areas remain open.
