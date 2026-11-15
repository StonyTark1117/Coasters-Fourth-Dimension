# FD810 Additions station operations

Complete, 2026-11-15. [Operator guide](../../../ADDITIONS-STATIONS.md).
Selected profile: `additions` (file9243346/runtime coasterfins0.2.2,
Aeronautics bundle1.3.2 and Interiors0.6.1v3), current core unchanged. Exact
four-class runtime fingerprint is enforced. FD501B dependency loading is closed;
Additions rotating seats remain FD911.

## Native contract

[Native06](native-06.log): 1/1 required test passed. Actual Additions station and
native two-car Wing consist validate exact loading/fingerprint, single whole-train
span authorization, actual rider count once, occupied and bars-up Link polarity,
open-restraint denial, automatic validated Dispatch, partial-span rejection,
stale sample rejection and conflicting ordinary Boarding Station ownership.
Read-only telemetry retains the real rider/restraint state under a foreign owner;
control is denied. Managed-station identity persists after reload but automatic
dispatch stays disarmed until explicit user configuration.

Native02 corrected fixture coupling before physics initialization; Native03 found
empty Link frequencies force zero power. Native04 passed after configuring distinct
frequency items. Follow-up06 changes only foreign-owner telemetry and persisted
managed protection; all previous behavior remains accepted. Native05 did not launch:
concurrent new Dive scaffold was briefly incomplete. The combined source compiled
successfully before06, and the test server saved every dimension and exited0.
No unchanged family sweep was scheduled.

## Normal operator input

The disclosed `/fd demo additions_station` fixture prepared native loop, car,
platforms and station span. Commands supplied ingredients/tool and initial aiming
positions; they did not board, close restraints, configure auto dispatch or launch.
The real Dev client first proved camera motion and reachable vanilla stone use.
A first stone click intersected the player's own feet; one calculated correction
proved placement with default bindings. Seat aiming reused the recorded same-geometry
lower native seat hit; no input-setting changes were made.

Normal empty-hand station use opened the upstream menu. Normal inventory/ghost-slot
input selected stone as the Occupied frequency, and Auto Dispatch was enabled with
threshold1, delay3seconds, cooldown2seconds. Upstream saves frequency changes on
menu closure; authoritative NBT confirmed both frequency and enabled flag.
Normal empty-hand boarding assigned Dev to seat(4,1,0). The station reported
occupied1/restraintsopen and denied dispatch. Normal Rotation Tool→Close restraints
was accepted at12:52:46UTC; by12:52:55, native motion was4blocks/s and capturefalse,
with the same rider and closed restraints. No manual Dispatch/action13 was sent.
See [normal menu](normal-auto-on.png), [motion](automatic-occupied-motion.png),
[input log](input.jsonl), [readbacks](console.jsonl), [server events](occupied-events.txt).

Auto dispatch changed the vehicle revision while the editor remained open. The
first Stop request was correctly rejected as stale with refreshed feedback; the
second, fresh normal Stop succeeded. Native motion then read essentially zero and
capturetrue. This stop occurred on the banked portion, where Wing correctly keeps
restraints closed. This run does not claim normal unloading; accepted native Wing
unload/authority evidence is [reused](../authority/README.md). The stopped banked
save is retained as a fixture state, not a new 0.8 recovery assignment.

## Scope and preservation

The adapter reads shared telemetry and sends existing validated Capture/Dispatch
requests. It never writes restraint, angular or vehicle phase state. Native
pre-station output remains advisory and never authorizes a request. Unmanaged
ordinary-cart stations retain upstream behavior. Unknown Additions artifacts
cannot authorize Fourth Dimension operations. Native actual-controller conflict
is covered; additional installed drive conflicts remain the named FD813 combined
profile work, not an assertion that metadata loading certifies them.

Packaged operator JAR SHA256 `c5c0992e52cec2583c3a7e78b61f7035433b219e5807244035547509583f6f6e`. Java21, client1536M/6GiB,
server2048M/4GiB; private display316, ports25650/25651. Owner
`01a1dd1d-1468-7f81-8f31-5532ad7a178d`. Root
`/mnt/NVME4/fourthdimension-runs/fd08-additions-20261115`, saved world
`server/AdditionsWorkshop`; full logs/input/screenshots retained. A usage-limit
interruption left the menu open; recovery verified the same owned PID/cgroup,
connected Dev and current menu before continuing. At completion, client exited,
server saved all dimensions and exited, then Xvfb stopped. All three units inactive,
MainPID0, success: [cleanup](cleanup.txt). No live runtime handoff.
