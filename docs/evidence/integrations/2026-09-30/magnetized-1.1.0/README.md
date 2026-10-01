# Magnetized 1.1.0 native development evidence

Baseline: Minecraft 1.21.1, Java 21, NeoForge 21.1.248, Create 6.0.10-280,
Ponder 1.0.82+mc1.21.1, Coasters 0.1.5 and Sable 2.0.3. `dependency.json`
records the separately installed Magnetized artifact, embedded requirements and
SHA-256. The combined profile also installs Engineered 1.1.1, Track Styles 1.2.0,
Additions 0.2.0, Aeronautics bundled 1.3.2 and Interiors 0.6.1 v3, as pinned in
the sibling evidence and `research/dependency-audit.json`.

## Reproduction

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk JAVA_TOOL_OPTIONS=-Xmx2G ./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 build runGameTestServer -PfdIntegrations=magnetized
JAVA_HOME=/usr/lib/jvm/java-21-openjdk JAVA_TOOL_OPTIONS=-Xmx2G ./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 build runGameTestServer -PfdIntegrations=engineered,styles,additions,magnetized
JAVA_HOME=/usr/lib/jvm/java-21-openjdk JAVA_TOOL_OPTIONS=-Xmx2G ./gradlew -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 build runGameTestServer
```

Each native run uses a fresh disposable world. Selected logs retain the full-log
hash and command. Optional fixtures log explicit skips, which count as registered
GameTest successes but are not executed compatibility checks. These tests run
production classes in the development harness, not packaged multiplayer clients.

## Recorded results

| Profile | Native result |
| --- | --- |
| Magnetized alone | All 24 registered tests passed; Engineered and Additions explicitly skipped. |
| Engineered + Styles + Additions + Magnetized | All 24 tests executed and passed. |
| Standalone | All 24 registered tests passed; the three addon fixtures explicitly skipped. |

All 17 unit tests passed. `source-files.sha256` records the tested source inputs;
`artifact.sha256` identifies the resulting JAR and immutable local copy. The JAR
contains our adapter but no optional addon archive or copied addon implementation.
Standalone absence is clean-world evidence, not addon removal from an occupied save.

## Exercised behavior

Five matching straight 60-block tracks carry one car and one native seated pig each.
The real powered Magnetized anchor equipment uses MAGNET at 10% and BRAKE at 100%,
with the artifact's default acceleration/speed configuration. The four non-PAX
cars receive an explicit initial speed of +1 or -1 blocks/second: the addon needs
existing motion to choose direction and does not launch from rest.

The whole-method propulsion guard rejects each held, open-restraint, maintenance
retention and fault state before Magnetized's injected callback can change velocity.
A positive valid-drive probe confirms the velocity readback can detect its increment.
Forward speed after acceleration exceeds 1.1 blocks/second; reverse speed is below
-1.1. Both braking phases reduce speed below 0.15 blocks/second. All passengers
remain seated, no ride fault occurs, and normal capture restores boarding readiness.
The held PAX single-car fixture never receives external propulsion.

Measured combined-run chassis speeds (blocks/second):

| Family | Forward | After forward brake | Reverse |
| --- | ---: | ---: | ---: |
| Arrow | 3.6023 | 0.00090 | -3.6036 |
| Legacy ZacSpin | 2.7158 | 0.07833 | -2.4504 |
| Free Spin | 1.9776 | 0.01013 | -2.0402 |
| Revised ZacSpin | 4.4784 | 0.00503 | -4.5381 |

These are fixture results, not layout speed guarantees. Reverse braking is also
asserted below 0.15 before capture. The selected Free Spin carrier recorded 343
seat-magnet impulses and about -310.23 energy change; the other three recorded zero.

The occupied four-car PAX fixture additionally powers MAGNET at 100% across all
three shuttle route segments and checks continued power each tick. The winch must
complete lift, a deliberate power-loss hold, explicit resume, release, outbound,
return and capture with its sixteen riders. In the combined profile, powered
Additions BOOST fins and a zero-delay station also remain active. This tests that
external equipment cannot take control; PAX external propulsion stays unsupported.

Free Spin's selected carrier receives independent seat-magnet impulses throughout
the forward/reverse fixture. Its three unselected carriers receive none; recorded
seat-magnet energy change remains nonpositive. The addon leaves the functional
provider, family, profile and our seat-equipment ownership unchanged.

The combined run also exercises the three occupied Engineered circuits with real
observers/controllers, Additions stations and fins, Styles identity/persistence,
and all 25 family-pair eligibility decisions.

## Limits

This is straight-track native interoperability evidence. No linked non-PAX train,
curved/uphill braking distance, maximum-speed configuration, external equipment
client rendering, real-player camera, multiplayer/late join, occupied restart,
chunk interruption or optional-mod removal acceptance is claimed here.

Our renderer retains the family rails, ties, profile-driven controls and magnetic
seat equipment. Magnetized's base-Coasters replacement rail models are not imported.
Equipment visuals and combined appearance transitions require client inspection.
Dedicated addon stacks still emit upstream client-class mixin warnings; passing
startup is not a warning-free certification. Full FD901–FD904 gates remain open.
