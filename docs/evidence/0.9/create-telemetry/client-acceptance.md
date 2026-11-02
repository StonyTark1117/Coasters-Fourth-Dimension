# Create telemetry client acceptance protocol

**Status: partially exercised in standalone.** The
[046ead9 checkpoint](local-046ead9/README.md) records the specific completed client
cases and remaining gaps. The earlier native checkpoint in [README.md](README.md)
proves server sampling and source output formatting. Run remaining cases after
explicit handoff of the shared runtime, using the actual integrated candidate
JAR on the server and both clients.

Record source revision, JAR/dependency hashes, profile, world copy, client roles,
commands, timestamps and screenshots alongside server/client logs. Keep fixture
creation distinct from player construction and operation. Do not use direct NBT
edits or injected display text as evidence of normal controls or packet delivery.

For packet-application evidence, start the packaged client with `-PfdValidation`
and look at the station or winch. `FD_CLIENT_TELEMETRY` logs the actual client
block entity's received snapshot when the targeted controller or snapshot changes
(checked every ten client ticks). It neither queries server state nor changes the
tooltip/display. Pair this log with the server observation and visible goggles;
it cannot by itself prove rendering or visible Display Link target output. The
trace is disabled without the existing validation flag and contains no rider IDs.

## Setup and observations

Use separate copies for the standalone and combined Styles/Engineered profiles.
Keep the other two profiles' native results attached to their original artifact;
a successful combined client run does not certify every individual profile.
Create all five family examples using the documented `/fd demo` commands, spaced
apart. Record each consist's actual number of seats rather than assuming every
example contains the same car count as the native telemetry fixture.

Wear Engineer's Goggles and inspect each bound station and the bound PAX winch.
Using normal Create controls, select a supported display target, attach a Display
Link to the controller, and choose each of the six ride sources. Record the GUI
source list and the visible target output; calling `provideLine` directly is not
this acceptance. Allow for the normal telemetry and Display Link refresh cadence.

| Exercise | Required observation |
| --- | --- |
| Initial binding / empty ride | Goggles and display agree on the actual consist capacity, empty occupancy, restraints, phase and dispatch denial. No untranslated keys appear. |
| Boarding and restraint changes | A real rider boards; the observer sees occupancy and restraint changes without reopening the world or manually refreshing block data. Repeat with a rider in a trailing Arrow/PAX car. |
| Dispatch and capture | Use the normal station/winch controls. Readiness becomes denied while unavailable for departure, operating phase changes, and capture settles before boarding readiness returns. |
| PAX authority | An ordinary station reports that the bound winch must operate PAX. The winch reports BOARDING, LIFTING, release/return/capture phases actually reached, and power-loss/interruption state without a station dispatch bypass. |
| Retention / repair denial | Through supported operator actions, retain a ride or exercise physical support loss and repair. Both displays report the current denial/fault. Repair alone does not show ready while physical realignment remains required. |
| Fault clearing | After documented recovery, the fault source clears on the actual display target; old text must not remain because the new output is blank. |
| Unavailable binding | Unbound or genuinely unloaded cars/carriers show unavailable state instead of the previous ready/occupied snapshot. Restoring availability refreshes it normally. |
| Late join / chunk re-entry | A second real client joins after the ride has changed state, then leaves and returns to the controller's tracking range. Its initial goggles snapshot matches the server and the first client. |
| Resource reload / reconnect | Reload client resources and reconnect, preserving the binding and source choice. The six source names, tooltip and visible target recover without duplicate transforms or stale text. |
| Cold restart | Stop normally, restart the same world with the same JARs, reconnect both clients, and verify saved bindings/source choices plus fresh occupancy, phase, fault and readiness. |

For every change, retain a server-side state observation and the corresponding
visible client result. A screenshot alone cannot establish which server state it
represents; a server log alone cannot establish that the client applied or drew it.
Test fresh dispatch validation independently of the displayed snapshot: an older
ready indication must never authorize a request after a restraint, support or
controller-authority change.

## Native and clearance evidence handling

The `runGameTestServer` task deletes its previous world and five native frame/trace
files before starting. Copy logs, reports and complete traces to a profile-specific
evidence directory before running another profile. Record the effective Gradle
build directory; paths containing a colon redirect this checkout's build to `/tmp`.
Run the unchanged route and dense single-car/train audits against those copied
files and pinned dependency JARs. Do not substitute older traces for a new artifact
or infer clearance success from the native GameTest count.

These results close only the exercised telemetry/client cases. Full FD401,
FD402–FD404 and FD901–FD905 still require their own remaining release evidence.
