# FD904 integrated runtime baseline

This is a host-specific server-side baseline for the addon's carrier runtime.
It is an input to FD904 target selection, not an FPS claim or release
acceptance result.

## Profile

- Run date: 2026-10-01 (America/Phoenix).
- Java: Arch Linux OpenJDK 21.0.12.1+1; Linux amd64, kernel 7.2.7-zen1-1.
- Minecraft 1.21.1; NeoForge 21.1.248; Create 6.0.10; Coasters Simulated
  0.1.5; Sable 2.0.3; Sable Companion 1.6.0; Veil 4.1.4; Track Styles 1.2.0;
  Coasters: Engineered 1.1.1.
- Command:

  ```sh
  JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer --no-daemon -PfdIntegrations=styles,engineered
  ```

`CarrierRuntime` begins sampling after each scene has assembled and occupants
are seated. Its counter is runtime invocations, which can exceed Minecraft game
ticks when the physics integration invokes the addon more than once per game
tick. The timing excludes Sable's physics, whole-server time, rendering,
shaders, networking and client FPS.

## Result

The integrated profile passed all **39 required GameTests** in **4.032
minutes**. The game log recorded:

| Workload | Occupancy and scene | Runtime invocations | Runtime total | Average per invocation |
| --- | --- | ---: | ---: | ---: |
| Controlled | Arrow and legacy ZacSpin circuit | 11,520 | 567,769,363 ns | 49,285 ns |
| PAX shuttle | 16 riders; lift, gravity return and capture | 4,638 | 469,019,101 ns | 101,125 ns |
| Free Spin | 8 riders; magnetic circuit and capture | 14,220 | 1,510,229,959 ns | 106,204 ns |

The exact diagnostic lines and test summary are retained in the local GameTest
log at `/tmp/fourthdimension-7af457c2/build/runs/gametest/logs/latest.log`.

## Limits and next work

The values are one integrated GameTest run on the stated host. They have not
been repeated for variance, normalized by carrier count, compared across
hardware, or converted into targets. They also omit all client render modes and
shader stacks. FD904 remains open until it records reproducible target hardware,
chooses quantitative limits from repeated baselines, measures the required
render modes, and verifies that advertised scene sizes meet those limits.
