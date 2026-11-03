# Occupied rider coverage checkpoint

The independent one-tick recovery mesh verifier now checks original rider
identities and carrier mounts on every occupied single-car sample. It requires
four Arrow/PAX seats or eight Legacy/Free Spin/Revised seats, evenly distributed
across the original carriers. Missing riders, uneven carrier occupancy, and
swapped carrier mounts each fail the [negative controls](negative-controls.txt).

The [recorded results](single-car-results.json) pass all five native banked
single-car routes. Each route visits all five sections, with 5,770–5,873
one-tick samples. The smallest model-to-track separating bound is 0.04387
blocks, above the required 0.02. The rider-to-track bound is at least 0.08
blocks, and the rider-to-rack bound is at least 0.0625 blocks. The JSON records
source, resource, trace, frame, and verifier hashes for reproduction. This run
used the baseline GameTest trace generated before source commit
`7d438710cfc0644d46119c49a650f026f9124900`.

This is default-model and native pose evidence. It does not cover client player
meshes, optional decorations, continuous motion between ticks, or the linked
train routes. The linked-train audit did not produce a completed report in this
run. FD402 remains in progress.
