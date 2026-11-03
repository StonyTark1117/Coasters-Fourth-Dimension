# Integrated candidate: occupied PAX power interruption and cold restart

Tested on 2026-11-02 from exact release source
`de667f7bd7deef808d6229df9e2d386e87dd598a`, packaged standalone JAR SHA-256
`6231a0241bef082df3f3edff689e74ae2604465aa095c13656136f13e10de3c5`.
This continues the [trailing-car checkpoint](../de667f7-trailing-pax/README.md)
in its dedicated world, with the same two packaged clients and validation logging.
No rebuild, mechanical-state edits or injected display text occurred.

## Observed sequence

1. Dev normally boarded the fourth PAX car and closed restraints through the
   Rotation Tool editor. Observer used Create's normal motor value control to
   select **16 RPM**, then normally used the bound winch to start Lifting.
2. During the occupied lift, Observer reduced the motor to **1 RPM** through
   the same control. This is insufficient drive below the required 16–128 RPM,
   not a zero-RPM or disconnected-shaft test. The winch entered **Fault** with
   `Winch power lost: retain the train; inspect and resume or recover`.
   The rider remained assigned; goggles showed 1/16, closed restraints and denied
   dispatch. The carriage retained route distance **49.17979295672618**.
3. Observer changed the existing Display Link to **Ride fault** using its normal
   GUI. The actual sign showed `Winch power lost:`; its saved text contained the
   full fault. The sign's normal width clips the longer message visually.
4. The dedicated server saved and stopped normally (Gradle exit 0). An offline
   world copy was preserved, then a fresh server JVM opened the same world/JAR.
   Both clients reconnected through the normal server-list/direct-connect UI.
   Observer first received/drew 0/16 while Dev was offline, with the saved rider
   reservation still present. After Dev rejoined, it received/drew 1/16 again.
5. The original four chassis, eight carriers, trailing-car seat assignment,
   interrupted Lifting phase, fault, and exact carriage distance survived.
   The Display Link remained configured to Ride fault and retained its target.
   This does not claim preservation of the native Create seat-entity UUID.
6. Observer restored **128 RPM** through the motor control. The winch remained
   faulted at the same distance; power restoration did not resume motion.
   A separate normal winch interaction explicitly resumed Lifting and cleared
   the fault. The existing fault source cleared the actual sign to blank without
   changing sources, exercising real fault clearing rather than source switching.
7. The original train returned to Boarding with `shuttle_cycles=1`, the rider
   still assigned, and no fault. The observer received/drew that final state.
   Dev normally opened restraints in the editor and used Shift to unload.
   Saved assignments and visible occupancy both returned to zero.

The retained server observations verify exact carriage-coordinate persistence,
original chassis/carrier topology, matching rider/seat assignment before and after
restart, explicit resume, cycle completion and final unloading. Client snapshots
and PNGs establish visible received states. Intermediate Outbound/Returning/
Capturing screenshots were not captured for this resumed cycle; the final phase
and cycle counter do not substitute for separate per-phase visual acceptance.

## Evidence and limits

[manifest.json](manifest.json) pins artifact, dependencies and every file.
`server.log.gz` covers the first server through its clean stop. The restart-server
and client logs are prefixes captured while subsequent acceptance remains active;
no final shutdown is claimed for those processes. A complete cold-restart world
backup is retained locally, not bundled into this evidence directory.

This is one rider in the fourth car, one interruption during active Lifting,
one occupied dedicated-server cold restart and one explicit resume to completion.
It does not certify interruptions during other phases, zero drive or removed
hardware, full occupancy, all profiles, or support-loss repair telemetry.
Fixture creation, inventory and positioning used commands; operating controls and
reconnects used normal clients. Hidden Xvfb/llvmpipe rendering is functional GUI
evidence, not the hardware performance or complete rendered-clearance gate.
FD401 and the full 0.9 release gate remain open.
