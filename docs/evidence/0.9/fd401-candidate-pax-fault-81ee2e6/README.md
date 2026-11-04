# FD401 isolated-candidate PAX fault clear and phase output

On 2026-11-04, the dedicated server and two packaged clients continued the
isolated `81ee2e6` [candidate build](../fd401-candidate-81ee2e6/README.md)
with the combined Styles/Engineered profile, `-PfdValidation`, and
`-PfdFeNetwork`. The executable JAR in the isolated build tree had SHA-256
`de64a6833fd0a4e5f7bdbc36ad40b89ff396d48941c7e6ae0ae2e52ca6ca3a70`.
This session started from the stopped family telemetry world. Both clients
exited through Minecraft's menus and the server stopped through `stop`; all
three Gradle tasks ended `BUILD SUCCESSFUL`, exit code 0. Complete
[server](fault-server.log.gz), [Dev](fault-dev.log.gz), and
[Observer](fault-observer.log.gz) logs, the [stopped world](world.tar.gz),
[phase poll](phase-poll.log), and [SHA-256 inventory](SHA256SUMS) are retained.

An old PAX demo at x700 was already faulted from failed native coupling; an
attempt to create a new demo there correctly rejected the occupied corridor.
The actual test used a **fresh** `/fd demo pax` fixture at origin
`(900,85,65)`, with a four-car, 16-seat train and a powered winch at
`(894,85,73)`. The demo authored tracks, cars, and a creative Create motor;
platforms and an oak sign were command fixtures. Dev boarded a real seat on
the fourth car normally and closed restraints in the in-ride editor. Observer
received [1/16, Boarding, closed, ready](fault-pax-ready-goggles.png).

Observer selected a normal Create Display Link target sign `(894,86,80)`,
placed the link on the winch, and selected **Ride fault** through the Create
[source GUI](fault-pax-link-fault-source.png). The sign was initially
[blank](fault-pax-sign-blank.png). Using the motor's normal held-value
control, Observer lowered it from 128 to [16 RPM](fault-pax-motor-16.png).
Dev's normal editor Dispatch started Lifting. During that occupied lift,
Observer changed the same motor to [1 RPM](fault-pax-motor-one-aim.png).
The winch entered Fault with `Winch power lost: retain the train; inspect and
resume or recover`; Dev remained mounted, the [goggles](fault-pax-fault-goggles.png)
showed 1/16 and denied dispatch, and the same source displayed the
[fault on the sign](fault-pax-sign-fault.png). The short sign visually clips
the message; its saved text held the full string.

Observer restored the motor through the same held-value control to **120 RPM**.
The winch remained faulted and the sign still held the fault. A separate
normal empty-hand click on the winch explicitly resumed the train; the fault
cleared and the **unchanged Ride fault source** cleared the
[sign](fault-pax-sign-cleared.png). The train finished its cycle, returned to
Boarding with `shuttle_cycles=1`, and Dev opened restraints and normally
dismounted. No NBT write repaired or cleared the fault.

Observer then changed that same link to **Ride phase** through the GUI. A
second occupied editor dispatch started from [Boarding](fault-pax-phase-boarding.png).
The [Lifting](fault-pax-phase-lifting.png) and
[Outbound](fault-pax-phase-outbound.png) screenshots show the live sign.
The 240-sample [poll](phase-poll.log) records all five sign texts in order:
Boarding → Lifting → Outbound → Returning → Capturing → Boarding. The phase
source updates after the winch's state change, so individual first samples
of a phase sometimes contain the prior sign text; subsequent samples contain
the matching text. The captured Returning and Capturing images likewise
landed one sign update behind and are retained as timing evidence, not
presented as visible proof of those words. The winch ended Boarding with
`shuttle_cycles=2`; Dev opened restraints and normally unloaded again. An
intervening dispatch was denied because maintenance retention was active;
Dev used **Release retention** in the editor before the second dispatch.

This checkpoint demonstrates a candidate-matched occupied PAX power fault,
source-preserving clear after explicit resume, and all current PAX phase
strings reaching the real Create sign. It does not cover PAX support-loss
repair, repair-before-realignment ordering, cold restart during this fault,
individual optional profiles, or a noncreative power source.
