# FD402 certified rider-equipment interlock

**Result:** Moving recovery now fails closed around the already measured vanilla
player/wearable envelope. A mounted player must have empty hands and either empty
equipment slots, built-in vanilla armor, or elytra. Start rejects anything else.
If equipment becomes unsafe after Start, the normal recovery tick retains and
brakes the complete consist with a rider-clearance fault. Correcting the rider
does not resume motion; the operator must explicitly choose Start tow again.

This resolves held items and non-armor head-slot items as an operational safety
boundary rather than making an unsupported geometry claim. Non-player native
passengers keep their existing behavior.

## Runtime and unit checks

`RecoveryRiderClearanceTest` passes the pure fail-closed policy cases: all six
inputs must be certified, either occupied hand rejects, and each equipment slot
rejects independently.

`riderEquipmentInterlocksRecoveryMotion` uses a live mounted `ServerPlayer` in
an actual native Arrow seat and production recovery coupling. It proves that:

- a held trident rejects Start with the rider-clearance reason;
- a vanilla diamond helmet is admitted;
- changing to a dragon head during active recovery retains and brakes with the
  same fault; and
- removing the head item still requires an explicit successful Start.

The runtime check passed in the headless `fourthdimension_fd402` GameTest
namespace on 2026-11-04. NeoForge groups five tests under that shared structure
namespace; after the clearance test advanced the server progress from two to
three completed tests with no failure report, the owned process was stopped
while an unrelated long-running substep test continued. This was intentional to
honor the host RAM limit and is not represented as a full namespace pass.

The checks used an isolated `/tmp/fd402-rider-clearance-build` output tree, one
Gradle worker, a 512 MiB Gradle heap and a 1.5 GiB GameTest-server heap. No
Minecraft client, X display, desktop input or other agent session was used.

## Remaining scope

The server cannot identify arbitrary client-only cosmetic layers or resource-pack
mesh overrides. Observed rendering, modded cosmetics, resource overrides,
unexposed motion inside a physics step, and the broader operator/lifecycle matrix
remain outside this checkpoint. FD402 therefore remains in progress.
