# OUTBOUND native-link retention and supported recovery — PASS

Uses the [compile06 candidate](../overlay04.json), frozen production plus only
the two bounded shuttle class repairs and opt-in fixture. Same original four
chassis/eight carriers/native carriage as the [phase restart](../phase-restarts.md)
world, with actual player FD902 in the fourth car. No NBT phase or body pose edits.

## Retention and range boundary

The first observation removed the exact original flat station curve during
actual OUTBOUND at tick10788, when the tail had entered that curve and the train
still straddled the spike/station boundary. Native support detection set
`Matching track support lost`, all-car alignmentRequired, capture, stopping and
closed restraints. The [1397tick hold](hold-comparison.json) retained every
original chassis/carrier/carriage, all four native link sets and the real
player's logical seat. Maximum measured chassis displacement between initial
and final fault observations was0.0000145blocks; no automatic resume occurred.
The [historical failure](../../../0.9/fd401-pax-longhold-client-blocker-1f13a0e/README.md)
lost middle-car links after about260ticks. This closes that retained-link regression.

Exact-curve restoration and Repair succeeded. Premature Recover rejected pending
realignment. Realign then correctly rejected the existing eight-block station-slot
limit: the lead was near z53 while its station slot was near z73.5. This is a
recorded support boundary, not a request to extend repair range or waive a guard.
The rejected state/world is preserved at
`checkpoints/outbound-support-straddling/world`. No input was repeated against
that unchanged failed precondition.

## Corrected supported operator sequence

For the remaining recovery outcome, a copy of the already-preserved CAPTURING
checkpoint reopened with the same candidate and original IDs/player, then used
explicit Resume to return to station. This is fixture continuation, not a new
qualification of the already-passed restart. [Input provenance](support-replay-input.json)
records the copy. The [approach collector](../arm-station-loss.py) then required
actual OUTBOUND, **all four cars on the original station edge**, and every chassis
within seven blocks of its saved station slot, leaving margin inside the existing
eight-block physical realignment guard. It did not reposition the train.

| Operation | Actual readback |
| --- | --- |
| Remove exact original station curve | [Before loss](station-adjacent/before-loss-OUTBOUND.json), tick9871, OUTBOUND and one real rider; native fault then held all four cars. |
| Retain | [Tick10284](station-adjacent/fault.json),413ticks unsupported, all original native identities/link sets and rider retained, alignment required. |
| Restore exact curve, Repair | Original Bezier object restored through its native anchor; Repair cleared car faults while alignmentRequired stayed true. Full command stream is [preserved](../console.jsonl.gz). |
| Realign | [Tick10456](station-adjacent/realigned.json), all four alignment flags clear, same native IDs, restraints and rider retained; winch remains FAULT. |
| Explicit Recover | [Tick10641](station-adjacent/recover.json), CAPTURING. It physically positioned the empty carriage and caught the train, then reached [BOARDINGcycle3](station-adjacent/recovered-boarding.json). |
| Occupied full cycle | [Trace](../post-repair-cycle.jsonl.gz) observes LIFTING→OUTBOUND→RETURNING→CAPTURING→[BOARDINGcycle4](station-adjacent/full-cycle.json), one same real rider throughout, every original body/link retained. |
| Open and ordinary Shift | Existing Open handler accepted at settled station; one window-targeted Shift input unloaded the actual client. [Final state](../final-unloaded.json): zero riders/assignments, all four ready, open restraints, no faults or alignment requirements. |

The frozen original curve, native support-loss detection and existing operator
handlers own these transitions. Fixture construction, mounting and curve edits
are disclosed command setup; this record does not replace accepted normal
construction/click/two-client control evidence. The representative actual-player
phase path and accepted native16-rider/real two-client records combine under the
validation policy. The4800tick LIFTING proof remains separately accepted.

All runtimes stopped and final world preserved; see [cleanup](../cleanup.json)
and the [package index](../README.md). No additional support range, moving track,
ordinary-bearing contract or performance guarantee is introduced.
