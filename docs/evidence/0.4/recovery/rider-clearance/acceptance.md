# FD402 sampled mounted native-rider clearance

Dev.24 extends the source-pinned banked recovery trace with the live mounted
passengers already used by the native fixtures. It is a bounded FD402 checkpoint
in the active 0.4 stage, not complete player or rendered-rack clearance.

## Method

Every twenty ticks, the trace resolves each native Create seat in each carrier,
records every mounted passenger's eye point and native dimensions, and associates
it with the carrier's measured logical pose. The independent verifier rejects a
missing rider on any original car, duplicate rider IDs, unexpected passenger
dimensions, missing carriers, or invalid carrier axes.

Sable renders passengers around their eye point using the containing carrier
orientation. The verifier uses that recorded point, dimensions and pose to build
an oriented passenger volume. It checks each volume against the reconstructed
Original running, control and spine tubes, outboard support, underbrace and
Arrow control mounts. It also includes it in the existing different-original-car
and ordinary-bearing neighbor-volume check. No running rail is excluded for a
rider.

## Local result

The fresh combined Track Styles/Engineered run passed all 37 required GameTests.
The two independent audits passed with the required 0.02-block margin:

| Route set | Sampled rider envelopes | Certified lower bound |
| --- | ---: | ---: |
| Five single-car routes | 9,328 | at least 0.08 blocks |
| Original Arrow/PAX trains | 8,184 | at least 0.08 blocks |

The verifier caps separated track results at 0.08 blocks. `0.08` therefore
proves at least that gap; it is not an exact closest-surface measurement. The
dev.24 verifier SHA-256 is
`a26a912567e57246de89c04088936e7ac7f8ff3c4310a99ea4093b21ddc4421e`.
Both clean GitHub matrices for this exact source revision passed all required
checks; their immutable workflow and job records are in
[ci-dev24.json](ci-dev24.json).

## Limits

The fixtures use mounted Pigs, so this does not establish a player model,
player cosmetics, animation, client rendering or camera behavior. Same-car
volumes are intentionally excluded from the neighbor check because their seat
and hinge contacts are intended; rider clearance to its own rack remains open.
Samples are twenty ticks apart and do not prove continuous curve/bank sweeping,
linkage clearance, decoration/resource overrides, real-client operator behavior,
general strength or the recovery lifecycle matrix. FD402, FD404 and the 0.9
release gate remain open.
