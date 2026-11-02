# Client support-repair observation order

**Scope:** FD902 packaged-client validation correctness. A healthy occupied
vehicle can initially be stopped, restrained, fault-free and on a valid matching
rail. Those fields also describe the post-repair state, so the client checker
must not count that initial state as evidence that a repair happened.

`CarrierTransformValidation` now accepts the repaired state only if the client
had already observed `Matching track support lost` on an earlier tick. The
existing checks still require a mounted rider, the carrier-seat assignment,
the stopped and restrained vehicle, an empty fault, a valid matching rail, and
motion before support loss. This changes only the opt-in validation path.

## Packaged replay

On 2026-11-02, an isolated packaged `0.9.0-dev.27` client ran from a disposable
copy of the Arrow fixture world using `-PfdCarrierFixtureSupportRepair` and
`-PfdCarrierValidationSupportRepair` alongside the existing occupied moving
support-loss flags. It used `-PfdCarrierValidationSamples=40` and exited through
the fixture's normal self-termination path.

```text
FD_CARRIER_FIXTURE support-loss settled ... retained=true fault=Matching track support lost
FD_CARRIER_FIXTURE rebuilt exact ARROW support ...
FD_CARRIER_FIXTURE support repair settled ... stopped=true restraints=true ridersRetained=1
FD_CARRIER_VALIDATION passed samples=40 ... riderMounted=true supportLossObserved=true supportLossAfterMotion=true supportRepairRequired=true supportRepairObserved=true ... motionDistances={...=30.565324462454043}
BUILD SUCCESSFUL in 54s
```

The full log sequence also records normal Minecraft shutdown and saved chunks.
`./gradlew build` passed with 26 unit tests before the packaged replay.

## Limit

This validates observation order for one occupied Arrow client. The five-family
packaged repair matrix and physical realignment have separate checkpoints.
This validator fix adds no recovery behavior and does not close multi-client
support loss or post-repair lifecycle gates.
