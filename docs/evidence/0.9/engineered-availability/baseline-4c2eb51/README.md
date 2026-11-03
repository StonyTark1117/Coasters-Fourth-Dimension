# Engineered physical availability: failing baseline

Source `4c2eb51dc010cb095b3cbb22ac4826683394cc87`; Engineered-only development
profile on the unchanged production guard. All 51 tests completed; the availability
test failed, the server saved/stopped normally, and Gradle returned exit 1.

Two Arrow cars were coupled and dispatched. Native `UNLOADED` callbacks removed
one physical body while retaining the logical vehicle and its healthy interlock
flags. The test confirmed a two-car logical consist and unavailable telemetry
before every measured call. Direct `MotorDrive.apply` nevertheless queued nonzero
force/torque in five calls: either source with car 0's first carrier missing,
either source with its second carrier missing, and source 1 with car 0's chassis
missing. Maximum queued deltas range from 0.978382 to 1.100356; required is <1e-9.
Eight healthy/restored positive controls also produced nonzero deltas.

Cases 3–5 are **incomplete**. After the partial chassis serializer restore and
three ticks, native code unlinked the cars. The case-3 consist assertion failed
before a motor call. The test does not prove complete train-link restoration.
The next fixture must use fresh native trains so one restore cannot contaminate
a later availability case. Preserve this limitation independently of the guard fix.

The complete profile run was **not isolated**: another owner's shared-root
Engineered process started during its final batches. The five direct calls were
recorded at 10:56:04 UTC, before that process's approximately 11:00:00 UTC start;
the process observation and final metadata retain this distinction. Do not use
this run as isolated performance or complete profile certification.

`run.py` records the exact command, start/end and exit state and captures exports
after process completion. Raw logs, matching JAR, preflight class parity, metadata,
overlap observation and parsed review are retained here. All five large native
exports were captured successfully under
`/tmp/fd-engineered-availability-4c2eb51/native-exports/`; only their capture manifest
is included in Git. These local auxiliary files are not separate acceptance claims.

This direct callback fixture is not a natural chunk-unload/reload cycle or an
occupied-player/operator test. The base stack, permanent removal behavior, PAX
ownership and ordinary recovery propulsion policy were unchanged. A fix and its
fresh-fixture native result remain required; FD401 and the release gates stay open.
