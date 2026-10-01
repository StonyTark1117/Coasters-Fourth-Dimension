# Optional anchor-strength persistence boundaries

The native regression `inactiveAnchorStrengthRespectsOwnershipAndDeletion` checks
where the Magnetized removal-save fix applies and where it must defer. It calls
real Coasters anchor `loadWithComponents`, `saveWithFullMetadata` and `getUpdateTag`
methods; it does not invoke a mocked serializer or the mixin callback directly.
No production behavior changed in this checkpoint.

A normal 30-block Arrow section at (950, 140, 900) supplies two owned anchors. A third
native anchor is deliberately outside Fourth Dimension's saved rail ownership.
The fixture has no vehicles and makes no physics or riding acceptance claim.

In the base pack, where no scroll-value behaviour exists:

- Integer strengths 0, 37 and 100 survive normal server serialization, including a
  present zero rather than relying on an absent tag's default value.
- None appears in the actual client update tag.
- Loading a tag with the field explicitly removed clears the remembered setting.
- Negative and over-100 integers, a long, a string and a byte are not retained.
- An unrelated native anchor does not adopt the fallback field.
- Destroying and replacing the owned anchor creates a fresh entity without the old value.

The Additions-only and Magnetized-only runs exercise actual installed behaviours.
After loading 83, the test changes the live behaviour to 7 and requires the native
save to contain 7. This detects stale-cache overrides of the current owner.
Additions supplies Create's standard `ScrollValueBehaviour`; Magnetized supplies
its `MagnetScrollValue$1` subclass. The Additions run covers ownership while
Magnetized is absent, complementing the installed-Magnetized exclusion.

This strengthens the [occupied removal checkpoint](../magnetized-lifecycle/README.md),
which retains the original loss and fixed fourteen-process save/restart evidence.
It does not substitute for that occupied test or add claims for unknown versions,
other mods' behaviours, client rendering, automatic chunk eviction or moving removal.

## Results

All three profiles pass 35 registered native checks. Base records seven absent-addon
skips; Additions and Magnetized each record five. The new serialization test executes
in every profile. All nineteen unit tests pass, and all processes exit zero.
`verification.json` records the eleven base events and the installed-owner control
in each addon profile. These checks complement the prior occupied restart tests;
they do not claim a new occupied or combined-pack run.

## Reproduce and audit

Run these profiles sequentially, with Java 21:

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk JAVA_TOOL_OPTIONS=-Xmx2G ./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 build runGameTestServer
JAVA_HOME=/usr/lib/jvm/java-21-openjdk JAVA_TOOL_OPTIONS=-Xmx2G ./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 runGameTestServer -PfdIntegrations=additions
JAVA_HOME=/usr/lib/jvm/java-21-openjdk JAVA_TOOL_OPTIONS=-Xmx2G ./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 runGameTestServer -PfdIntegrations=magnetized
```

```sh
python3 scripts/verify-optional-strength-boundaries.py docs/evidence/integrations/2026-10-01/optional-strength-boundaries
```

The retained verifier checks the exact native event set, installed behaviour class,
positive control values, native totals and skips, unit results, exit codes and hashes.
Full logs remain compressed, including existing dependency warnings. Source and
compiled-class hashes identify the executed regression and unchanged production hook.

Versions: Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10-280,
Ponder 1.0.82+mc1.21.1, Sable 2.0.3, Coasters Simulated 0.1.5,
Fourth Dimension 0.9.0-dev.1. The individual profiles add Additions 0.2.0 with
Aeronautics 1.3.2/Interiors 0.6.1v3, or Magnetized 1.1.0. These are pinned native
development runs, not packaged multiplayer or a new full integration certification.
