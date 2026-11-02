# Client support-repair observation order

**Scope:** FD902 packaged-client validation correctness. A healthy occupied
vehicle can initially be stopped, restrained, fault-free and on a valid matching
rail. Those fields also describe the post-repair state, so the client checker
must not count that initial state as evidence that a repair happened.

`CarrierTransformValidation` now accepts the repaired state only if the client
had already observed `Matching track support lost` on an earlier tick. It
accepts realignment only after a repaired state was observed on another earlier
tick; the initial healthy car could otherwise pass the bearing check before
support loss. The existing checks still require a mounted rider, the
carrier-seat assignment, the stopped and restrained vehicle, an empty fault, a
valid matching rail, the physical bearing bound, and motion before support
loss. This changes only the opt-in validation path.

## Packaged replay

On 2026-11-02, an isolated packaged `0.9.0-dev.27` client ran from a fresh,
empty Arrow fixture world using the occupied moving support-loss, repair and
realignment fixture/validation flags. It used
`-PfdCarrierValidationSamples=40` and exited through the fixture's normal
self-termination path.

```text
FD_CARRIER_FIXTURE support-loss settled ... retained=true fault=Matching track support lost
FD_CARRIER_FIXTURE rebuilt exact ARROW support ...
FD_CARRIER_FIXTURE support repair settled ... stopped=true restraints=true ridersRetained=1
FD_CARRIER_FIXTURE physically realigned ARROW support ...
FD_CARRIER_VALIDATION passed samples=40 ... riderMounted=true supportLossObserved=true supportLossAfterMotion=true supportRepairObserved=true supportRealignmentObserved=true ... motionDistances={...=2.148903735773872}
BUILD SUCCESSFUL in 49s
```

The full positive log SHA-256 is
`cf3e02639b5f2785e09aa2602ee6ee451112d8e42103ffd0c6c79186370fb97a`.
The [selected positive lines](client-repair-order-positive.txt) show the state
sequence; the full log records normal Minecraft shutdown and saved chunks.
`./gradlew build` passed before the packaged replay; its test task was up to
date from the prior 26-test run.

A fresh negative replay enabled client realignment validation but deliberately
omitted the server realignment fixture action. It observed motion, support loss
and explicit repair, then timed out at 1,000 ticks with
`supportRealignmentObserved=false`. This expected failure proves the initial
healthy car and the repaired but unaligned car no longer satisfy the final
client gate. Its full log SHA-256 is
`9a5a1d4cbee5942016ff7d008335d77f60fdff67243eb9568f304ee74d5d873c`.
The [selected negative lines](client-repair-order-negative.txt) retain the
timeout reason and expected failed build result.

## Limit

This validates observation order for one occupied Arrow client. The five-family
packaged repair matrix and two-client physical realignment have separate
checkpoints. This validator fix adds no recovery behavior and does not close
multi-client support loss or post-repair lifecycle gates.
