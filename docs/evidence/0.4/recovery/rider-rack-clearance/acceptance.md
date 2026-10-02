# FD402 sampled mounted-rider carrier-rack clearance

Dev.25 extends the sampled native-rider audit to the non-seat block-model
volumes belonging to that rider's exact carrier. It found the Revised carrier
axle intersecting the mounted rider envelope, then moves the axle's rendered
and collision box from `y=-2..2` to the paired-row axle position `y=6..10`.
The intentional mounted-seat contact is excluded; it is the attachment point,
not a mechanical clearance surface.

## Method

At every twenty-tick pose sample, the verifier reconstructs the mounted native
passenger's carrier-oriented volume from its live eye point and native
dimensions. It transforms every non-seat default block-model element from the
same carrier with that measured pose and requires a 0.02-block SAT-separated
gap. It rejects a trace that contains no rider, no rack element, a replacement
carrier, unexpected rider dimensions, or a missing route section.

## Local result

The fresh combined Track Styles/Engineered build passed all 37 required
GameTests. Every recovered family and original occupied train passed both the
route and own-rack checks:

| Route set | Sampled rider-to-rack envelopes | Certified lower bound |
| --- | ---: | ---: |
| Five single-car routes | 1,988 | 0.0625 blocks |
| Original Arrow/PAX trains | 2,068 | 0.0625 blocks |

The verifier SHA-256 is
`d617bc02d6f61f3beae259073a1fe2dba2e5a801adc42a467ea6d7f0ae0ac681`.
Both clean GitHub matrices for the exact source revision passed all required
checks; their immutable workflow and job records are in
[ci-dev25.json](ci-dev25.json).

## Limits

This is native Pig passenger and default block-model evidence. It does not
certify player meshes or cosmetics, client rendering, intentional seat contact,
carrier linkage, optional decoration/resource overrides, continuous sweep,
real-client operator behavior, general strength or the recovery lifecycle
matrix. FD402, FD404 and the 0.9 release gate remain open.
