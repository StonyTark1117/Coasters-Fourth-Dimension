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
are seated. Runtime invocations can exceed Minecraft game ticks when the
physics integration invokes the addon more than once per game tick. The
instrumentation also counts each valid live-carrier update and times its
individual update body, allowing shared runtime work to be distinguished from
carrier work. Constraint rebuilds are counted separately. The timing excludes
Sable's physics, whole-server time, rendering, shaders, networking and client
FPS.

## Results

Two consecutive integrated profiles passed all **39 required GameTests**: run
one in **3.969 minutes** and run two in **4.123 minutes**. Their logs recorded:

| Workload | Run | Runtime invocations | Average / invocation | Carrier updates | Carrier total | Average / carrier | Rebuilds |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Controlled — Arrow and legacy ZacSpin circuit | 1 | 11,520 | 50,057 ns | 15,360 | 59,925,361 ns | 3,901 ns | 0 |
| Controlled — Arrow and legacy ZacSpin circuit | 2 | 11,520 | 43,509 ns | 15,360 | 47,576,533 ns | 3,097 ns | 0 |
| PAX shuttle — 16 riders; lift, gravity return and capture | 1 | 4,638 | 94,323 ns | 12,368 | 31,170,666 ns | 2,520 ns | 0 |
| PAX shuttle — 16 riders; lift, gravity return and capture | 2 | 4,638 | 102,080 ns | 12,368 | 30,155,175 ns | 2,438 ns | 0 |
| Free Spin — 8 riders; magnetic circuit and capture | 1 | 14,220 | 101,318 ns | 18,960 | 71,962,359 ns | 3,795 ns | 0 |
| Free Spin — 8 riders; magnetic circuit and capture | 2 | 14,220 | 89,377 ns | 18,960 | 50,629,872 ns | 2,670 ns | 0 |

The exact run-two diagnostic lines and test summary are retained in the local
GameTest log at `/tmp/fourthdimension-7af457c2/build/runs/gametest/logs/latest.log`.

## Limits and next work

The values are two integrated GameTest runs on the stated host. The carrier
figures normalize the direct carrier body but do not allocate shared runtime
work among carriers. Their visible variation confirms that this shared-host
pair is a range, not a release threshold. They have not been repeated enough
for variance analysis, compared across hardware, or converted into targets.
They also omit all client render modes and shader stacks. FD904 remains open
until it records reproducible target hardware, chooses quantitative limits from
repeated baselines, measures the required render modes, and verifies that
advertised scene sizes meet those limits.
