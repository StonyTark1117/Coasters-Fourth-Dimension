# FD402 rider-to-rack coverage correction

The earlier `audit-recovery-mesh-trace.py` skipped a rider's carrier-rack check
when its track search found no nearby triangle. That track-only fast path was
inside the rider loop, before the independent rack check in the dev.25 source
(`3144a3f`) and remained there through dev.29. The dev.29
[one-tick results](one-tick-single-results.json) therefore report 23,052 Arrow
rider envelopes but only 5,244 rider-to-rack volume checks; the corresponding
[train results](one-tick-train-results.json) report 111,584 PAX rider envelopes
but only 28,126 rack checks. Their 0.0625-block rack minimum applies to the
checked subset, not every sampled rider. The earlier reports remain as
historical records.

Commit `33fbd483a6171ba426e48358b000ea1f8dcc3bef` moves rack checking before
the track-only fast path. It now rejects any rider with no auditable non-seat
rack geometry and rejects a run whose rack-check count is less than its rider
count. The corrected verifier SHA-256 is
`6da0e684f424875f394b3322e2e57d95ed0fb4de37dbe8f13ef3e7e87a40d87d`.

## Archived one-tick cross-check

The corrected verifier ran on an archived, different one-tick trace. It used
the pinned base Coasters JAR `f80b4c3cdf9fa6188804967147761b0373e150a6eadbdc53626e5ac9700b2049`
and Minecraft client JAR `499f6897d1837516680f3114072d8106e11c9adcd933fe5cf051b551089b0c99`.
The source-pinned verifier checked the same route, renderer and model sources
as the earlier report. The [corrected single-car report](rider-rack-corrected-single-results.json)
and [corrected train report](rider-rack-corrected-train-results.json) record
input and verifier hashes, all route metrics and no failures.
The single-car frames/trace SHA-256 pair is
`24687a320e8a8b19c1d1d29d868aefc51b64d828fe27b7ffb4bb9131debe0555` /
`9c5808175d202a418fc5e292f9e4c0cc002c104b69672bf077bb444f432fefb5`;
the train pair is
`eb331c5efeead09c3304353d537022bc63a2cda0675f7323641c88937226f426` /
`42c3de46541b5206d7944724df0bec5239db5949bed0c3ffa7a054dc17925562`.

| Route | Pose samples | Rider envelopes | Rider-to-rack volume checks | Minimum rack gap | Result |
| --- | ---: | ---: | ---: | ---: | --- |
| Arrow | 5,769 | 23,076 | 46,152 | 0.06250 | Pass |
| Legacy ZacSpin | 5,844 | 46,752 | 93,504 | 0.06250 | Pass |
| Free Spin | 5,872 | 46,976 | 93,952 | 0.06250 | Pass |
| Revised ZacSpin | 5,865 | 46,920 | 93,840 | 0.06250 | Pass |
| PAX shuttle | 5,767 | 23,068 | 46,136 | 0.06250 | Pass |
| Arrow train | 6,547 | 52,376 | 104,752 | 0.06250 | Pass |
| PAX train | 6,972 | 111,552 | 223,104 | 0.06250 | Pass |

The archived raw traces are local inputs under `/tmp/fd-ci-standalone` and are
not included in this commit; the reports preserve their hashes. This is a
cross-check, not a rerun of the exact dev.29 inputs or a rendered-client
clearance result. The exact 0.9 telemetry traces need their own corrected
audit. This result remains limited to sampled native Pig/default-model geometry
and does not cover continuous swept motion, player
meshes, client rendering, optional resource overrides or the full FD402 gate.
