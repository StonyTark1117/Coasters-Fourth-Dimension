# Integrated candidate: trailing PAX car telemetry

On 2026-11-02, exact release source `de667f7bd7deef808d6229df9e2d386e87dd598a`
produced packaged JAR SHA-256
`6231a0241bef082df3f3edff689e74ae2604465aa095c13656136f13e10de3c5`.
The standalone dedicated server and both packaged clients used this artifact with
`-PfdValidation`; build succeeded with compile/test outputs reused from Gradle cache.
This is a client checkpoint, not a fresh native matrix or release-gate closure.

## Observed case

A fresh creative flat world used `/execute positioned 512 80 0 run fd demo pax`.
The fixture supplied the four-car train, bound powered winch and boarding platform;
commands supplied positioning, goggles, Display Link item and a blank sign.
The rider boarded and dismounted with normal client interactions. No rider state,
telemetry packet or displayed text was injected.

1. Observer's received winch snapshot and visible goggles showed four cars and
   **0/16** riders at `(506,85,73)`.
2. Dev right-clicked a seat in the fourth car near `(512,85,91)`. The first click
   after window focus did not board; the subsequent normal click did. The retained
   server observations distinguish that attempt from the successful boarding.
3. The saved rider assignment belongs to chassis
   `[859674760,2032226056,-1909381182,299761874]`, the fourth entry in the winch's
   ordered `shuttle_cars` list. The lead car remained empty. Observer's received
   snapshot and visible goggles changed to **1/16** without a reconnect.
4. Observer used the normal Display Link item to select the blank oak sign at
   `(506,86,79)`, attached the link to the winch at `(506,85,72)`, and selected
   **Ride occupancy** in its normal GUI. The actual sign displayed **1 / 16**.
5. Dev used Shift to dismount with open restraints. Saved rider assignments became
   zero; the same sign refreshed to **0 / 16**. Observer then viewed the winch from
   above the motor and received/drew **0/16** again.

The six PNGs retain those visible states. `server-observations.jsonl` preserves
winch car order, native availability, saved carrier/seat ownership and player data.
Client log prefixes include `FD_CLIENT_TELEMETRY` snapshots at 17:10:59 (empty),
17:11:45 (occupied), and 17:14:07 (empty after unloading), local America/Phoenix time.
All logs are prefixes captured while the continuing acceptance session was active;
no clean shutdown is claimed by this case. [manifest.json](manifest.json) records
source, artifact, dependency and evidence hashes.

## Limits

This proves one real rider in the fourth car of the standalone PAX example,
whole-consist occupancy in goggles and one normally configured Display Link, and
normal unloading. It does not prove trailing Arrow occupancy, every seat, other
profiles, survival acquisition/building, moving occupancy, interruption/restart,
controller-unavailability or support-repair telemetry. Those checks remain separate.
The clients use hidden Xvfb 854×480 with llvmpipe software rendering; no hardware
performance or full rendered-clearance claim is made. FD401 and the 0.9 gate stay open.
