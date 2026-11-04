# Current-release Engineered regression — one FE recovery failure

Source **d3a51aa4b822d73777c713b9c5ec2b1859d36bfa**, Engineered-only profile,
2026-11-03. **58 GameTests completed; one required test failed.** This is a
failed release regression, not a completed FD401/FD402/FD905 gate. Compile/JAR
exit 0, runtime exit 1, normal server shutdown, native export capture exit 0.
The runtime lasted 13:35:04–13:40:04 UTC; reported GameTest duration 4.791 minutes.

## Source and fixture repair

The isolated branch starts at release source `e8c05ae`. Commit `cb7e548` changes
only EngineeredEnergyGameTests: board before closing restraints; remove the exact
recovery vehicle, native bodies, rider, rail and fixture blocks; release only
chunks newly forced by this fixture. AfterBatch cleanup also handles failed or
timed-out tests and restores the energy requirement in finally. The success path
checks body, rider, rail and vehicle removal after three ticks. Production limits,
guards, healthy towing assertions and dependency pins were not changed.

The release branch lacked four previously reviewed probe sources. Exact original
commits `836e088`, `270697b` and `0fc8a76` were restored as `aa6086e`, `454ba88`
and `d3a51aa`. The resulting source contains 58 GameTest declarations; this run
must not be described as the earlier 54-test isolated replay or the discarded
55-test blanket-hold diagnostic. The net change from `e8c05ae` is five GameTest
source files only. Shared-root experiments were not used.

## Failure and isolation evidence

`nativeEnergyRespectsRecoveryBrakeAndExplicitTow` reaches tick 400 and fails:

> Supplied FE did not tow the recovery group: 0.0

Boarding, native attachment, empty-buffer starvation, supplied braked retention
and the second explicit Start execute before this assertion. The assertion occurs
before checking post-tow FE consumption, mount retention, fault state or speed;
those later checks and the successful-path three-tick cleanup assertions were
**not reached**. Zero displacement alone does not establish overspeed, controller
phase, a guard rejection, power starvation or a physical stall as the cause.

Failure-path AfterBatch cleanup logs `vehiclePresent=false remainingVehicles=0`
before the next batch. The last controlled example then reaches
`FD_PERFORMANCE_CONTROLLED families=[ARROW, ZACSPIN]`; all 58 tests finish with
only the FE recovery failure. The prior final-example capacity failure is absent.
This proves the new fixture no longer exhausts the shared vehicle slots in this
failed-test path; production capacity remains unchanged.

The same FE batch's unmodified ordinary-cart control moves 17.45452880859375
blocks with remaining energy 99649, establishing that the installed FE provider
operates in this run. The separate energy-disabled explicit-start recovery test
records zero braked impulse, positive active impulse 0.08305536554931911,
0.03314208984375 blocks of movement and zero rebraked impulse. That short positive
control does not certify sustained healthy FE recovery.

The fixture uses LAUNCH target 1 block/s, above the 0.75-block/s recovery cap.
The source comment now states this accurately; the healthy/no-fault checks were
not relaxed. Next investigation should log controller mode/value/heading and
returning-to-start state, FE, stopping/fault and native velocities before the
second Start and before the first tick-400 assertion. Configured STOP/creep 0.5
is a separate candidate, not a demonstrated solution. No further diagnostic was
launched as part of this archive.

## Availability checks and identity

All twelve chain evidence checks and eight safety checks pass. The recovery
chain probe completes three native unload/restore cases; the chassis probe
completes six, with ten unavailable and twenty positive callbacks. All six
Engineered recovery evidence checks and four safety checks pass. Its three
unavailable callbacks are held while all healthy controls remain positive.
The original Engineered chassis availability test also completes six unloads,
ten held callbacks and twenty positive controls. See the retained parser outputs
for the exact measurements and restoration assertions.

All **234 packaged classes** match the development runtime before and after the
run. The shared guards and restored focused probes are byte-identical to the
reviewed `ec731db` JAR. EngineeredInterlockGameTests legitimately adds the release's
explicit-start positive control; its source is pinned byte-for-byte to `e8c05ae`
in preflight instead of substituting the older class. Other changed release
classes are enumerated in source-preflight, not represented as identical.

The matching addon JAR SHA-256 is
`5481a97004c7ea6db93e59d4410f3086764141a296de3f6bdc60178d0cd18ade`.
Source was clean and fixed through compile, execution and capture. A single
nonblocking runtime lock covered this sequence; 108 live open-JAR hashes were
captured. The final process observation contains no JVM. Gradle used Java 27;
the actual GameTest server used Java 21.

## Reproduction and limits

`python3 review-release.py` reproduces the chain/Engineered parsers and overall
failed acceptance from compressed stdout. `acceptance.json` deliberately records
`bounded_regression_passed=false`. Full compressed compile, Gradle and server logs,
matching JAR, run scripts, preflight, metadata, parser results and hash index are
retained here. Verify payloads with `sha256sum -c evidence.sha256`.

All five native exports were captured after shutdown. Large export payloads stay
local at `/tmp/fd-isolated-release-availability-d3a51aa/native-exports/`; the retained
capture manifest is provenance, not a substitute for those payloads or new
geometry acceptance. Those external payloads are temporary and may be removed;
this repository archive does not guarantee future independent frame inspection. This is an isolated current-release loose-class regression,
not a shared-root, packaged-JAR, Styles+Engineered, physical FE-network or complete
release test. Direct callback probes do not establish natural chunk scheduling,
automatic relinking or every family/profile lifecycle behavior.
