# FD901 optional-addon profile matrix

This checkpoint reruns the current branch's full native matrix against each
audited optional-addon profile. It records compatibility of those profiles; it
does not certify future addon versions, external functional tracks, or client
rendering.

## Profiles

All commands used Java 21, Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10,
Coasters Simulated 0.1.5, Sable 2.0.3, Sable Companion 1.6.0, Veil 4.1.4, and
Fourth Dimension 0.9.0-dev.27.

| Profile | Command options | Result |
| --- | --- | --- |
| Track Styles 1.2.0 only | `-PfdIntegrations=styles` | 39 required GameTests passed in 4.154 minutes. |
| Engineered 1.1.1 only | `-PfdIntegrations=engineered -PfdPerformanceTargets` | 39 required GameTests passed in 4.200 minutes; all three host-scoped timing checks passed. |
| Both audited addons | `-PfdIntegrations=styles,engineered -PfdPerformanceTargets` | 39 required GameTests passed in 4.158 minutes; all three host-scoped timing checks passed. |

The Engineered-only run exercised its motor/assembly cases. The Styles-only run
exercised the native matrix with the decorative provider installed. The combined
run is separately recorded in the [runtime baseline](runtime-baseline.md) and
the packaged lifecycle checks.

## Limits

The GameTests cover native eligibility, vehicle, recovery, migration and
operating behavior. They do not prove a real-player client rendering mode,
multi-client lifecycle behavior, all external track types, or an uninspected
optional-addon version. The full FD901 compatibility matrix remains open.
