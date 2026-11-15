# FD913 / FD914 — independent coexistence

Complete 2026-11-15. Lead owner `01a1dd1d-1468-7f81-8f31-5532ad7a178d`, primary main after `025dd6d4` with the accompanying coexistence source/build changes. This closes the named coexistence capabilities, not a Waterparked gameplay qualification or a transfer bridge.

## Configuration and scope

The profile is `styles,engineered,additions,ctnb,magnetized,skin,waterparked,createcoasters` on the [qualified 0.9 baseline](../dependencies/README.md). Waterparked0.2.8-beta-hotfix1 and Create Coasters2.0 are the selected artifacts. Waterparked requires [KotlinForForge5.10.0](language-loader.json). Immersion is absent from this run; the Seats/Immersion owner's expanded combined profile supplies client-only overlap evidence separately.

Persistent root: `/mnt/NVME4/fourthdimension-runs/fd09-coexistence-20261115`. Java21; client1536M heap /6GiB scope; server2048M /4GiB; private display316, game/RCON25650/25651. Gradle preparation remained outside the client scope. Saved client world: `server/Coexistence`; native world: `native-03-world`. The fixture builds an ordinary accepted Wing loop, an actual Waterparked shared-graph slide, and an actual powered Create bearing/seat with the Create Coasters lock. Command-authored construction and disclosed teleports do not count as player construction.

## Native result

[native-03.log](native-03.log) passes the one targeted `fourthdimension_fd913` GameTest. Actual shared graph contains the Waterparked curve; native cart snap rejects it for the FD vehicle; every TrackDesign factory refuses it; core track preview refuses slide anchors. A real Create bearing contraption locks all16 colors when the utility is powered and unlocks when unpowered. The Wing's authority remains null, restraints open and fault empty, and its ordinary graph remains valid.

Attempt01 exposed the missing Kotlin language loader; this was fixed in the optional profile. Attempt02 reached the test but the unpowered fixture bearing disassembled. Adding a real Creative Motor at16rpm corrected the fixture precondition. Attempt03 passed; neither earlier failure was hidden by Gradle's exit status.

## Connected client

[input.jsonl](input.jsonl) and [console.jsonl](console.jsonl) preserve expectations, one-input/readback records and setup commands. Camera turning and reachable vanilla use passed in this same client with default mouse bindings and raw input enabled.

* Normal lever use at15:21:20 UTC powered the native Create utility; server block-state check passed. [Powered view](create-lock-powered.png), [saved FD state](utility-powered-save.json).
* Normal Wing boarding at15:24:30 and Shift unload at15:24:56 succeeded while the Create utility stayed powered. RootVehicle changed to the real seat and then disappeared on unload. FD restraints stayed open with no acquired Create authority. [Boarded](wing-boarded.png), [unloaded](wing-unloaded.png).
* Normal lever use at15:25:26 removed power; server block-state check passed.
* The [Waterparked slide](waterparked-slide.png) rendered its native gray-concrete closed tube in the same world, with two native anchors and128 curve segments. The Wing body/seat and Create contraption also rendered during the normal utility/boarding check. Existing Wing mechanics remain accepted; this package does not require a second full ride qualification.

The initial platform teleport was off its edge and was corrected. Seat aiming was resolved from the actual Create seat outline before clicking; no bindings changed. A subsequent normal boat-item click did not establish placement. Disclosed summoned native boats were diagnostic only: one traversed toward the far end, another settled near the entry. The [trace](boat-trace.json) does not prove normal placement, a watered slide ride, or amphibious FD behavior and is not used for those claims. FD913 requires representative rendered coexistence and explicit eligibility refusal, which the independent evidence above establishes.

## Ownership and cleanup

The core change refuses Waterparked at the FD factory, track-anchor and native snap boundaries (including managed ordinary recovery carts). It does not alter native Waterparked boat physics or grant Create-train control over Sable rides. Create utilities retain their native contraption scope. Player guidance: [optional coexistence](../../../OPTIONAL-COEXISTENCE.md).

Client stopped, server received `save-all flush` then `stop`, and all dimensions were saved. The display stopped afterward. [Cleanup](cleanup.txt) verifies all three exact owned units inactive with MainPID0. Saved worlds and raw diagnostics remain in the persistent root. No other owner's process was touched.
