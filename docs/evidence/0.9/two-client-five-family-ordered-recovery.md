# FD902 ordered two-client five-family recovery

This replay closes the earlier observation-order gap in the two-client recovery
matrix. Fresh dedicated servers admitted **Dev** and **Observer** to distinct
native `create:seat` seats on the same carrier for Arrow, Legacy ZacSpin, Free
Spin, Revised ZacSpin, and PAX. Each run dispatched the occupied vehicle,
removed its real matching Coasters connection while moving, rebuilt the exact
support, applied repair, then physically realigned the carrier.

The packaged client validator now requires an observation on an earlier tick at
each boundary: support loss before repair, and repair before realignment. Both
clients passed that ordered sequence in every run with `riderAssignments=2`,
`riderMounted=true`, and all three observation flags true. PAX also reports
motion for all four chassis before its lead support was removed.

The captured server and client markers are retained in
[two-client-five-family-ordered-recovery.txt](two-client-five-family-ordered-recovery.txt).
This proves the bounded concurrent recovery sequence and its client replication
for the five current families. PAX powered-lift dispatch after recovery and
post-repair reconnect or cold-server restart remain FD902 work.
