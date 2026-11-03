# Occupied rider coverage checkpoint

The independent one-tick recovery mesh verifier now checks original rider
identities and carrier mounts on every occupied sample. It requires four
Arrow/PAX seats or eight Legacy/Free Spin/Revised seats per car, evenly
distributed across the original carriers. Missing riders, uneven carrier
occupancy, and swapped carrier mounts each fail the
[negative controls](negative-controls.txt).

The [recorded results](single-car-results.json) pass all five native banked
single-car routes. Each route visits all five sections, with 5,770–5,873
one-tick samples. The smallest model-to-track separating bound is 0.04387
blocks, above the required 0.02. The rider-to-track bound is at least 0.08
blocks, and the rider-to-rack bound is at least 0.0625 blocks. The JSON records
source, resource, trace, frame, and verifier hashes for reproduction. This run
used the baseline GameTest trace generated before source commit
`7d438710cfc0644d46119c49a650f026f9124900`.

The verifier also passes the [retained single-car traces](retained-single-car-results.json)
and [retained linked-train traces](retained-linked-train-results.json) from the
0.9 telemetry record. The retained single-car run covers 5,764–5,873 samples
per family and matches that record's previously reported clearance bounds.
The linked Arrow train retains eight mounted riders over 6,548 samples; the
four-car PAX train retains sixteen over 6,973 samples. Their minimum
model-to-track separating bounds are 0.04878 and 0.04398 blocks, respectively,
and both match the prior independent report. All seven routes pass with no
uncertified pairs. The verifier's batched neighbor broadphase preserves the
same lower bound and oriented-box refinement for close pairs; a scalar
comparison checked pair order and AABB distances on synthetic inputs.

This is default-model and native pose evidence. It does not cover client player
meshes, optional decorations, or continuous motion between ticks. FD402 remains
in progress pending rendered operator acceptance and broader recovery coverage.
