# FD901 original-family eligibility matrix

This dev.27 checkpoint verifies the matching-track boundary for all five
original Fourth Dimension families: Arrow, legacy ZacSpin, Free Spin, revised
ZacSpin, and PAX.

## Method

The native GameTest places one original functional rail for each family. For
each cart design, it attempts construction on every other rail, then creates
the matching car and locks its restraints. The locked chassis is evaluated
against every rail through `EligibilityHooks`, Coasters Simulated's native
bearing snap filter, and the public native initial-snap path.

The matching hit must be the sole accepted hit. Every rejected hit must leave
the existing persisted `boundFrom` and `boundTo` rail binding unchanged; an
initial snap on a wrong-family hit must leave the bearing disengaged. A matching
initial snap must engage the bearing after the rejected attempts.

## Result

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer --no-daemon -PfdIntegrations=styles,engineered -PfdPerformanceTargets
```

The combined target-gated profile passed all **41 required GameTests** in
**4.113 minutes** and emitted:

```text
FD_FAMILY_MATRIX decisions=25 factoryRejects=20 lockedNativeInitialSnapRejects=20 bindingPreserved=true
```

The 25 decisions are the complete five-cart by five-original-track table. The
20 off-diagonal cases rejected in the factory and native initial-snap paths;
the five diagonal matching cases attached successfully.

The same run's native survival dispatcher fixture independently placed all five
matching cars through their held-item interaction path and rejected all 20
wrong-family placements without consuming an item or allocating a vehicle:

```text
FD_SURVIVAL ... trackFamilies=5 wrongFamilyRejects=20 placedCars=5 ...
```

The additional 41st test applies each inspected Track Styles cosmetic ID to an
Arrow rail, rejects a Legacy car through `VehicleFactory`, accepts an Arrow car,
and removes the temporary vehicle state. It directly proves that a decorative
Arrow appearance cannot grant a different family mechanical eligibility.

## Limits

This is an original functional-track, server-side regression. It does not
certify cosmetic-only styles as functional tracks, external functional-track
providers, modified assemblies, a real connected player, every connection path,
or future addon versions. Those cases remain in FD901 and its FD404 prerequisite.
