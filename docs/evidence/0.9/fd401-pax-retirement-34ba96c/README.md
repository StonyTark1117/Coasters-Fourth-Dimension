# FD401 detached PAX car retirement source gate

On 2026-11-04, committed source `34ba96c` added a **Retire empty car** action
to the PAX editor with an explicit confirmation screen. The server accepts it
only for an original PAX car that was handed over by **Detach shuttle**, is
retained, captured and stopped, has no recorded riders or recovery cart, and
has no native link to another car. The handover marker is saved with the car
and cleared when a winch binds it again. Native body removal uses the existing
vehicle lifecycle cleanup for its carriers and FoundationData entry.

An isolated development server ran only
`emptydetachedpaxcarcanberetiredafterhandover` after `compileJava` passed.
The [server log](server.log.gz) records
`FD_PAX_RETIRE detached=true empty=true removed=true otherCarsIntact=true`.
The named test rejected retirement while the winch still owned the car,
retained and detached the four-car PAX train, removed one isolated empty car,
and checked the other three remained. The server exited 0 after `stop`.
[SHA256SUMS](SHA256SUMS) identifies the log.

This is a source lifecycle gate. It does not prove normal client use of the
confirmation screen, successful Detach in the saved displaced world, removal
of all four saved cars, construction and native chain linking of replacement
cars, winch rebind, or a fresh occupied Dispatch. Those steps remain the
saved-world recovery gate.
