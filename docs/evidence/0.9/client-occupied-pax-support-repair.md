# FD902 packaged occupied PAX support-repair checkpoint

This bounded FD902 checkpoint applies the exact support-repair client flow to a
fresh packaged PAX shuttle world on Xvfb. The fixture assembles the real
four-chassis native train, boards the connected player in a `create:seat`,
dispatches the train, removes the lead's bound Coasters curve, restores the
exact original curve, and invokes the explicit repair operation.

The packaged `0.9.0-dev.27` JAR SHA-256 was
`5149fe8800a12c3197d571818d1aadd3531dfa35a4104de4f5c6fd050a5f3eb7`.

## Result

The client passed after 611 ticks. It observed all four PAX chassis move before
support loss; the selected lead moved `3.3563421611633584` blocks. It then
observed the retained local rider, the support-loss fault, and the repaired,
stopped, restrained state with `supportRepairObserved=true`. The server recorded
one rider retained after repair and normal shutdown followed.

The selected runtime record is in
[client-occupied-pax-support-repair.txt](client-occupied-pax-support-repair.txt).

## Limits

This validates the PAX train's exact support repair with one client. It does not
test physical realignment and a later dispatch, multi-client repair, post-repair
save/reload, or the Legacy, Free Spin, and Revised client repair paths.
