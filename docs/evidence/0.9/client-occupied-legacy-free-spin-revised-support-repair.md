# FD902 packaged occupied Legacy, Free Spin, and Revised support repair

This FD902 checkpoint completes the client support-repair matrix for the three
remaining circuit-car families: legacy ZacSpin, Free Spin, and revised ZacSpin.
Each run used a fresh packaged single-player world on Xvfb, one connected player
in a real `create:seat` carrier seat, normal production dispatch, exact authored
curve restoration, and `VehicleOperations.repairMatchingSupport`.

The packaged `0.9.0-dev.27` JAR SHA-256 was
`b68235303e117d0c221b68bab973537b215a0b61d43b749f29f0c2fe97a28d91`.

## Method

The dedicated static-transform matrix already requires 40 consecutive samples
for all five families. These lifecycle-only runs enable the explicit
`fdCarrierValidationSkipStatic` fixture option so that a free carrier cannot
consume that static window while its normal dispatch is intentionally pending.
They instead require client observation of movement before loss, the replicated
support-loss fault, the retained local rider and seat assignment, and the
repaired matching support with an empty fault while the train remains stopped
and restrained. This option is test-only and does not affect production
movement, persistence, or repair behavior.

The fixture retains the original `BezierConnection` and restores that exact
curve. Its authored tangents are part of the saved geometry fingerprint, so a
generic straight replacement is deliberately rejected by the production repair
interlock.

## Results

All three client runs passed and shut down normally:

| Family | Client ticks | Selected chassis movement | Runtime record |
| --- | ---: | ---: | --- |
| Legacy ZacSpin | 608 | 5.500840796240017 blocks | [log](client-occupied-legacy-support-repair.txt) |
| Free Spin | 628 | 10.943945798510184 blocks | [log](client-occupied-free-spin-support-repair.txt) |
| Revised ZacSpin | 609 | 6.876369422804471 blocks | [log](client-occupied-revised-support-repair.txt) |

Every record reports `supportLossObserved=true`,
`supportLossAfterMotion=true`, and `supportRepairObserved=true`. The server
recorded the rider after repair, and the repaired vehicle was stopped with
restraints enabled. Together with the existing [Arrow]
(client-occupied-arrow-support-repair.md) and [PAX]
(client-occupied-pax-support-repair.md) checkpoints, this covers occupied
exact-support repair on all current native 4D families; PAX additionally
requires movement from all four chassis.

## Limits

This is one local client per family and exact support restoration only. It does
not validate user-built replacement geometry, physical chassis realignment and
later dispatch, concurrent-client repair, or post-repair unload/reload.
