# FD901 ordinary-cart native snap matrix

This dev.27 checkpoint extends the original regular-cart test from direct
eligibility alone to Coasters Simulated's native snap-filter entry path.

## Method

The native GameTest places one matching track for each original family: Arrow,
legacy ZacSpin, Free Spin, revised ZacSpin, and PAX. On each track it creates a
bare Coasters bearing and requires both `EligibilityHooks.allows` and
`CoasterCartRailSnapEligibility.filterHitForBearing` to accept it.

The test then adds an iron block outside the certificate's permitted bare
bearing envelope. Both paths must reject the widened assembly. After removing
that block, the original bearing traverses natively. It must acquire neither a
Fourth Dimension vehicle record nor carrier rotation or magnetic impulses.

## Result

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer --no-daemon -PfdIntegrations=styles,engineered
```

The combined Track Styles/Engineered profile passed all **39 required
GameTests** in 4.198 minutes and emitted:

```text
FD_ORDINARY_MATRIX families=5 directFits=5 nativeFits=5 wideRejects=5 nativeWideRejects=5 no4D=true
```

## Limits

This covers the certified bare ordinary bearing on original tracks only. It does
not certify modified ordinary assemblies, recovery locomotives, external
functional track providers, mixed consists, every placement/connection entry
path, client operation, or the full optional-addon matrix. Those remain FD901
and its FD404 prerequisite.
