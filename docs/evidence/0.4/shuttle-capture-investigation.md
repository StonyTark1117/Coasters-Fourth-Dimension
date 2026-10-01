# Current readiness after physical PAX station capture

The dev.6 PR CI run [36819333291](https://github.com/StonyTark1117/Coasters-Fourth-Dimension/actions/runs/36819333291)
failed its Engineered-only job while the other seven push/PR profile jobs passed
at head `0a75881dcc66f7a7d1444cdfa7e1b241c651511e`. The original failure is retained
below; the green local runs did not resolve it.

At 05:23:36 the sixteen-rider physical carriage test attempted restraint opening
and received `Stop and align every carrier before operating restraints`, shortly
after its CAPTURING records. The test opened immediately when `cycles == 1`.
`ShuttleRuntime` records completed capture in pre-physics, while the operator
checks current chassis speed and carrier alignment. A cycle count is historical
state, not an unconditional current boarding certificate. Production rejection
prevented unsafe opening. The old CI output lacks per-car readiness readings,
so the exact failed car/threshold cannot be reconstructed.

A diagnostic-only native Engineered run kept the original immediate operation
and logged all four cars. It passed, but chassis speeds at that first check were
0.07015–0.07350 blocks/s, close to the unchanged 0.08 boarding limit. This supports
post-capture settling as an explanation; it is not a recreation of the old CI
physics state. No failed run was discarded or rerun simply to obtain green CI.

The dev.7 test now uses the operator's current readiness, requires ten consecutive
ready ticks, and fails if station settling/recovery exceeds eighty ticks (four
seconds). When unready, it explicitly tries the normal opening operation and
checks that rejection preserves every car's closed restraints and revision.
After initial settling it introduces 0.2 blocks/s of real native translation to
the lead chassis and its carrier bodies through Sable's pipeline, verifies the
same rejection deterministically, then requires ten consecutive ready ticks
again before normal restraint opening and actual passenger unloading. Passenger
retention, four-car coupling, native engagement, lift, power interruption/hold,
gravity reversal and station traversal assertions remain in place.

Production boarding thresholds, carrier capture, restraint operations and shuttle
physics are unchanged. This is a corrected test assumption plus a stronger native
interlock regression, not a relaxation or bypass of readiness. The diagnostic
run and final per-profile observations are separate files. Full lifecycle and
operator UI coverage retain FD902/FD401 gates.

## Final dev.7 local validation

The final artifact hash is in `shuttle-capture-artifact.sha256`. All four profiles
(standalone, Styles-only, Engineered-only and combined) pass all 25 registered
native tests. In each run the updated sixteen-rider test settled/recovered in
20 ticks, recorded ten consecutive ready ticks after the disturbance, rejected
opening with no mutation, and unloaded normally. `shuttle-capture-unit.json`
records all 24 JUnit tests passing with no errors, failures or skips. Missing
Engineered profiles still exercise availability guards in its four specific
scenarios; they are not installed operating evidence.

Both Styles and combined reran the independent installed-default-mesh audit
successfully, with the same 0.0433135950-block minimum conservative gap. The native
frame export hash is unchanged from the committed dev.6 frame dataset, because
this checkpoint changes only the regression harness and evidence. These checks
retain the audit's recorded-layout/control-rail scope and do not certify full
vehicle clearance. GitHub results for the new head are recorded separately from
the preserved failed dev.6 run.
