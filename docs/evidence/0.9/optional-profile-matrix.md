# FD901 optional-addon profile matrix

The current native matrix uses source revision `abc4a9a` and the same built JAR
(SHA-256 `74ae47b70f2e95bcd8152552f255cabba6b55ed942a1f9856fa41fef39ca2467`)
in all four profiles. These are development GameTests run from compiled classes;
the JAR identifies their corresponding build. The [full evidence bundle](profile-matrix-abc4a9a/README.md)
retains each run's logs, dependency hashes, test inventory, performance samples,
and clean-save record. The earlier [capacity failure](profile-matrix-11e1a93/README.md)
is preserved separately; `abc4a9a` isolates the five-car family matrix from
concurrent fixtures without changing the production vehicle limit.

## Profiles

All runs used Java 21, Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10,
Coasters Simulated 0.1.5, Sable 2.0.3, Sable Companion 1.6.0, Veil 4.1.4, and
Fourth Dimension 0.9.0-dev.27. Every run enabled `-PfdPerformanceTargets`
(120,000 ns per invocation and 6,000 ns per live carrier update).

| Profile | Additional command option | Native result |
| --- | --- | --- |
| Base | None | 49/49 required GameTests passed in 5.339 minutes. |
| Track Styles 1.2.0 | `-PfdIntegrations=styles` | 49/49 passed in 5.250 minutes. |
| Engineered 1.1.1 | `-PfdIntegrations=engineered` | 49/49 passed in 5.504 minutes. |
| Both audited addons | `-PfdIntegrations=styles,engineered` | 49/49 passed in 5.585 minutes. |

All four runs saved and shut down normally. Every profile reported 25 family
decisions, 20 wrong-family factory rejects, 20 native initial-snap rejects,
and all 20 directed mixed-family coupling rejects while preserving both cars'
exact bindings. Styles and combined each exercised two appearance stroke
cases without changing mechanical eligibility. Engineered and combined each
exercised the five addon-dependent operating scenarios, including FE,
interlocks, motor behavior, ordinary-cart STOP, and the powered controlled
train. Base and Styles explicitly skip those addon-dependent operations.
All three measured scenes met both target-host timing limits in every profile;
the evidence bundle records their values.

## Limits

These development GameTests cover native eligibility, vehicle, recovery,
migration, and operating behavior under the pinned addon versions. They do not
prove real-player client rendering, multi-client lifecycle behavior, all
external track types, or future addon versions. No third-party functional
track is advertised. The full FD901 compatibility matrix and 0.9 release
gate remain open.
