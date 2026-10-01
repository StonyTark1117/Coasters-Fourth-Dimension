# Veil client scheduler checkpoint — 0.9.0-dev.12

This checkpoint addresses a shader-preparation scheduler defect in pinned Veil
4.1.4. It does not establish the cause or resolution of the earlier dev.8 freed
bitmap-glyph crash, close FD401/FD902/FD904, or qualify the 0.9 candidate.

## Inspected defect and fix

The exact published [Veil 4.1.4 sources archive](https://maven.blamejared.com/foundry/veil/veil-neoforge-1.21.1/4.1.4/veil-neoforge-1.21.1-4.1.4-sources.jar)
shows `ThreadTaskScheduler` starting workers inside its constructor loop, then
assigning the final worker count after that loop. A worker that drains the
supplier before that assignment can release zero exit permits. Its completion
accounting can also observe the unpublished count. A schedule with no initial
work creates no workers and never completes its native future.

`VeilSchedulerStartupMixin` independently redirects only the constructor's
worker starts, collects the native threads, and starts them at constructor return
after the count is assigned. It completes the native future immediately when
there are zero workers. Queueing, native tasks, worker execution, cancellation,
error handling and positive-work completion remain native. The mixin is listed
only under the client section; metadata requires exactly Veil 4.1.4 on clients.
There is no copied third-party implementation, source archive or nested JAR in
the addon.

The native binary SHA-256 is
`9c2899086a96339245b5abde82879011753871f25443df9581881f29596a7180`;
the published source archive SHA-256 is
`90ba7dd4a57f17b070bb28b80e4ab081ea76169e4f149588441c44fa65f6462d`.
The separate Java control loads the unmodified binary, creates an empty schedule
and confirms `completedFutureDone=false`. Its original source and result are
`scheduler-control-{source,result}.txt`; no control class is packaged.

## Native verification

Run `python3 scripts/verify-scheduler-checkpoint.py` to check the recorded reload,
ride and saved-state observations. CI runs the same read-only verifier. This
checks this checkpoint's evidence, including the four recorded cold PAX faults;
it does not replay the client or accept the remaining lifecycle gate.

The Java 21 build passes 25 unit tests without failures, errors or skips.
Standalone and combined Track Styles 1.2.0/Engineered 1.1.1 dedicated GameTest
runs each pass all 28 required tests and exit zero. Selected native logs, full-log
hashes, unit totals and artifact digest accompany this document. The client
mixin does not run on the dedicated distribution.

The opt-in `-PfdSchedulerValidation` client checks the installed, transformed
native scheduler after the world and loading overlay are ready. It runs 128
iterations at each task count 0, 1, 6 and 17 with six requested workers. It waits
for the real native future, then verifies each task ran exactly once. The packaged
client reports 512 completed schedules, 128 empty schedules and 3072 tasks.
The ordinary client does not run this validator.

## Reload replay

The prior turn's temporary client and forensic files were unavailable on this
continuation. No earlier temporary exit or thread observation is counted as
current acceptance. This run builds a fresh packaged artifact and a new creative
superflat `FD12Reload` world on an owned Xvfb `:198`; the user's desktop and other
projects' running processes are not operated.

Normal demonstration commands create all five families plus a second, banked
Arrow circuit. The Rotation Tool opens a normal track editor. The captured-car
approach rejects an appearance edit; a midpoint attempt also rejects because
editing requires proximity to an anchor. Styling succeeds near the clear
controlled straight's endpoint: **(-68,83,-44) → (-68,83,-80)**, double spine,
`#4A90E2`, retaining `0:0;18:360;36:0`. Temporary editing stones are removed before
replay. The sole view platform stands well outside that track at (-56,89,-49).
These are creative fixtures, not survival construction acceptance.

The replay sends one F3+T request at a time, waits for one new ResourceManager
start and one 55-entry catalog bake, checks the loading overlay, renders native
`/da` suggestions, then waits for a new native SelectedItem query acknowledgement
before continuing. The selected item is one Minecraft red dye. It never mutates
a font cache, native image, shader future, render return or exception. The client
uses `-PfdSchedulerValidation -PfdValidation`; Ponder validation is false. These
flags perform startup checks and pose observations, rather than substituting
reload behavior. The 2-GiB heap cap and game directory override are recorded in
`scheduler-client-init.txt`.

The initial screen classifier counted any red pixel in the upper viewport.
After six complete cycles, the seventh reload and catalog bake completed, but
the sunset caused a false 45-second timeout. The driver stopped without sending
another request. The same live JVM was inspected using a read-only thread dump;
normal world rendering, suggestions and a successful query then completed the
seventh cycle. No restart, cache change, bytecode transformer or future repair
was used. The failed classifier, its screenshot, trace and manual seventh-cycle
record are preserved. The resumed driver recognizes the loading overlay's
specific red color and requires a majority of those pixels before calling the
screen blocked. Native starts/bakes and query acknowledgements still gate each
request. The replay totals and selected rendered frames are recorded separately.

## Occupied ride after reload

All 64 in-world reloads completed their 55-entry bake and native query. The six
initial automated cycles, manually verified seventh cycle and 57 resumed cycles
are distinguished in `scheduler-reload-cycles.json`; cycle seven has no invented
completion time. Representative suggestion/query screenshots accompany the trace.

Normal empty-hand interaction with the Arrow seat mounted the real player after
several unsuccessful aim attempts; the native RootVehicle query identifies
Create's seat entity. The mounted Rotation Tool screen reports Ready, closes
restraints, then dispatches. The original example's native chain drives propel
the car. A normal redstone-block command powers the station rear input at
(-106,83,-67) after departure, requesting capture on return. No forced mount,
travel command, velocity write or angle write drives this ride.

The same player remains seated through the styled straight's full turn and
returns to station capture. At 04:39:50 MST, the actual occupied carrier
`f08f774e-36f4-4f5e-86fe-450fef402d06` reports snapshot/native angles
6.1423429764 / 6.2607767524 radians. The diagnostic interval is five seconds;
these are measured samples, not an exact peak or camera-lag bound. The screenshots
show the occupied car, revised blue decoration and rotating camera. The returning
mounted editor reports Ready with closed restraints. Normal opening and a held
Shift dismount succeed. Native queries and the saved player have no RootVehicle
or NoGravity override.

Normal Save and Quit, then Quit Game, ends the client/Gradle process with exit 0.
The final saved rails match the autosaved before-ride snapshot exactly, preserving
the styled section's complete profile, fingerprint, ID and appearance. All nine
chassis and 22 carrier identities remain. All vehicles are captured, ready,
unrestrained and fault-free; all carriers have empty occupant/seat assignments.
Only the operated Arrow car changes revision (0 to 4). No addon NBT is edited.
The selected events include the passenger samples, normal seat query, station
input, dismount checks and clean lifecycle; the full source log hash is recorded.

The debug view records Java 21.0.12, AMD Ryzen 9 7900X3D, an 854×480 viewport,
Mesa 26.3.3-arch1.1 and llvmpipe LLVM 22.1.8. This software-rendered observation
is not a performance benchmark or coverage of every Flywheel/shader mode.

## Remaining acceptance

Successful reloads are bounded negative observations of the original bitmap
crash, not proof of its cause or a universal font fix. Complete banked vehicle,
rider and linked-car clearance, supported render/shader modes, the Engineered
operator/FE-network workflow, physical recovery evaluation, real-client survival
building/operating exercises, migration and the remaining lifecycle/performance
matrix retain their roadmap gates. Save schema and network protocol remain 5.

## Default client reopen and newly exposed PAX fault

A fresh client opens the same closed world with all three validation flags false.
The revised double spine renders with its saved profile and four more sequential
reload/suggestions/native-query cycles complete. Normal Save and Quit and Quit
Game again finish with exit zero; the owned display is then stopped. No bitmap
error is observed in either client. Both artifacts are the same recorded JAR.

The resaved rails match exactly, and all chassis/carrier IDs and static carrier
fields persist. Arrow, legacy, Free Spin and revised vehicle records also retain
their non-angular fields. However, **all four PAX cars acquire `Shuttle lead car
unavailable` and closed restraints** after reopening. This is recorded in the
cold snapshot and field-difference report; it is not a fault-free five-family
cold-reopen acceptance. The saved player remains unmounted without a gravity
override.

`ShuttleRuntime.tick` throws this message when the winch's lead UUID does not yet
resolve to a native sublevel, including during BOARDING. Native PAX links later
restore in the client log. A transient restoration gap is a source-supported
hypothesis, not yet a proven cause or a shipped PAX fix. Determining the missing
lead's lifecycle and validating the supported recovery are further FD902 work.
The successful Arrow ride/reload and shader scheduler checks remain distinct
from this new PAX failure.
