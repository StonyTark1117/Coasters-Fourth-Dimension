# Shared propulsion availability guard — passing bounded replay

Source `ec731dbdefe7b3adb6c4f21395993ce66a72872c` passes **all 54 required
GameTests**. Compile/JAR, Gradle runtime and post-exit native export capture each
exit **0**. The server saves and stops normally; the runtime build completes in
4m 45s. This closes the direct-callback gaps measured in the
[unchanged-test failing baseline](../native-chain-baseline-0fc8a76/README.md),
not the complete 0.9 release gate.

## Before/after measurements

| Callback and state | Actual calls | Failing baseline | Fixed replay |
| --- | --- | --- | --- |
| Chain, ordinary bearing after component unload before recovery tick | 3 | 0.5 linear velocity delta | **0.0** |
| Chain, surviving 4D chassis after a consist component unload | 10 | 0.5 linear velocity delta | **0.0** |
| Chain, healthy/restored positive controls | 23 | 0.5 linear velocity delta | **0.5** |
| Chain, braked/held controls | 15 | 0.0 | **0.0** |
| Chain, ordinary bearing after recovery tick faults/stops group | 3 | 0.0 | **0.0** |
| Engineered, ordinary bearing after component unload before recovery tick | 3 | 0.08305553527832525 queued force/torque delta | **0.0** |
| Engineered, healthy ordinary towing | 3 | 0.08305553527832525 queued force/torque delta | **0.08305553527832525** |
| Engineered, braked and after recovery tick | 6 | 0.0 | **0.0** |

Every chain angular velocity delta is zero. The real chain target is -0.5 for
every callback. There are exactly **54 chain probes**, across three ordinary
recovery-bearing unload cases and six two-car consist unload cases. That probe
count happens to equal the suite test count. All missing native bodies are
serialized, actually removed through `UNLOADED`, and restored with the same UUID.
Logical identity/healthy flags and surviving source containment are checked before
the unavailable callback. Carrier cases have positive post-restoration probes;
chassis cases do not claim automatic native relinking.

The chain probe logs original native linear/angular velocities, verifies a temporary
zero-velocity baseline, invokes the actual public chain-drive callback, reads native
pipeline velocity, then restores and verifies each original velocity in `finally`.
Power, logical interlocks and native membership are unchanged by normalization.
Engineered probes separately measure and restore queued force/torque; those numbers
are not velocity. The pre-existing Engineered 4D-source availability test also
completes six unloads, ten blocked calls and twenty positive controls with no failures.

## Production change and preserved boundaries

Only `PropulsionInterlock.class` differs from the failing `0fc8a76` JAR. Test
classes and `EngineeredInterlock.class` are byte-identical. Both native chain drive
and Engineered consult the shared guard. A nonprototype 4D source now requires
the same live contained source plus available chassis/carriers throughout its
resolved consist; unresolved consist identity fails closed. An ordinary source
with attached recovery records requires all matching cars' native components,
no removed-partner marker, and each chassis still linked to that source.

Healthy maintenance-retained ordinary towing remains permitted, as measured above.
Unrelated ordinary and prototype branches retain their existing policy. The new
removed-partner and detached-link checks were source-reviewed; this replay does
not separately inject those two negative conditions or certify relink recovery.
Engineered's additional whole-consist logical holds remain unchanged.

## Provenance and reproduction

The matching addon JAR (`baseline.jar`, named by the common runner) has SHA-256
`fbf96bbe8704c7d9ce8246f5611eae73ede1c0342240991196bfdcd0e76304e9`.
All **233 packaged classes** match the compiled runtime classes before and after
the run. Source was clean throughout. Execution is the isolated Engineered-only
development loose-class profile, not packaged-JAR or merged release validation.
The newer release branch has additional work; derive its own suite count and
revalidate the integrated artifact rather than transferring this 54-test claim.

The single outer nonblocking lock covers compile, parity checks, runtime and
export capture. An earlier attempt aborted before compilation when another Java
process existed; `launch-attempts.json` records that refusal. The successful attempt
started after coordinator handoff and no non-daemon Java process was present.
Start/end process observations and **108 live open-JAR hashes** are retained;
the end observation contains no JVM. These observations are not continuous process
tracing or a performance result. No second runtime was launched by this task.

`python3 review-run.py` reproduces all twelve chain completeness checks and all
eight chain safety checks from compressed stdout. `python3 review-engineered.py`
reproduces the six ordinary-bearing evidence checks and four safety checks.
All pass. `acceptance.json` additionally records finite Engineered measurements,
the exact suite result and artifact identity. The runners, source preflight,
compile metadata/log, full Gradle/server logs, matching JAR and hash index remain
in this archive. Tests and acceptance thresholds were not relaxed.

All five native exports were captured after runtime exit. Large payloads remain
local at `/tmp/fd-native-chain-availability-ec731db/native-exports/`; the retained
capture manifest does not substitute for those payloads or establish new geometry
acceptance.

## Remaining limits

This is direct-callback evidence on native physics, not proof that natural chunk
scheduling always enters the tested window. It does not certify occupied recovery,
automatic relinking, every family/addon profile, GUI/FE-network operation or the
complete release. The prior failing evidence remains intact. Integration into the
current release requires preserving newer Arrow/FD402 work and its own candidate
validation.
