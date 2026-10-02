# FD904 integrated runtime baseline

This is a host-specific server-side baseline and target check for the addon's
carrier runtime. It is not an FPS claim or a complete FD904 release-acceptance
result.

## Profile

- Run date: 2026-10-01 (America/Phoenix).
- Java: Arch Linux OpenJDK 21.0.12.1+1; Linux amd64, kernel 7.2.7-zen1-1.
- CPU: AMD Ryzen 9 7900X3D, 12 cores / 24 threads; boost enabled.
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
| Controlled — Arrow and legacy ZacSpin circuit | 3 | 11,520 | 50,387 ns | 15,360 | 59,541,303 ns | 3,876 ns | 0 |
| PAX shuttle — 16 riders; lift, gravity return and capture | 3 | 4,674 | 107,376 ns | 12,464 | 36,640,220 ns | 2,939 ns | 0 |
| Free Spin — 8 riders; magnetic circuit and capture | 3 | 14,220 | 99,894 ns | 18,960 | 59,267,247 ns | 3,125 ns | 0 |

Run three enabled the FD904 target gate with
`-PfdPerformanceTargets`. All 39 required GameTests passed in **4.158 minutes**.
Run four repeated that target-gated combined profile after the FD901 family
matrix was added: all **40 required GameTests** passed in **4.067 minutes**.
Its sampled lines were:

| Workload | Run | Runtime invocations | Average / invocation | Carrier updates | Carrier total | Average / carrier | Rebuilds |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Controlled — Arrow and legacy ZacSpin circuit | 4 | 11,520 | 46,440 ns | 15,360 | 52,511,842 ns | 3,418 ns | 0 |
| PAX shuttle — 16 riders; lift, gravity return and capture | 4 | 4,674 | 107,747 ns | 12,464 | 33,906,019 ns | 2,720 ns | 0 |
| Free Spin — 8 riders; magnetic circuit and capture | 4 | 14,220 | 98,805 ns | 18,960 | 58,414,480 ns | 3,080 ns | 0 |

The exact run-four diagnostic lines and test summary are retained in the local
GameTest log at `/tmp/fourthdimension-7af457c2/build/runs/gametest/logs/latest.log`.

## Target-host gate

The declared target-host limits are at most **120,000 ns per runtime invocation**
and **6,000 ns per live-carrier update**. They are based on a rounded margin
above the three-run observed maxima of 107,376 ns and 3,901 ns, respectively.
They are only enabled by `-PfdPerformanceTargets`, which checks each named
performance GameTest after its required sampling window. Run three meets both
limits in all three scenes. Across all four recorded target-host samples, the
highest values are 107,747 ns per invocation for the PAX shuttle and 3,901 ns
per carrier update for the controlled scene.

## Limits and next work

The values are four integrated GameTest runs on the stated host. The carrier
figures normalize the direct carrier body but do not allocate shared runtime
work among carriers. Their visible variation remains host-specific and does not
generalize to other hardware. The gate deliberately omits whole-server time,
client render modes, and shader stacks. FD904 remains open until it measures
the required render modes and shader stack, records any supported-hardware
limits, and verifies that advertised scene sizes meet their full acceptance
scope.
