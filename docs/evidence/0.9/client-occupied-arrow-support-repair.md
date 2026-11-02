# FD902 packaged occupied Arrow support-repair checkpoint

This bounded FD902 checkpoint extends the moving occupied Arrow support-loss
fixture through exact support restoration and the explicit repair operation. It
uses a fresh packaged single-player world on Xvfb with one connected player in a
real `create:seat` carrier seat.

The packaged `0.9.0-dev.27` JAR SHA-256 was
`5149fe8800a12c3197d571818d1aadd3531dfa35a4104de4f5c6fd050a5f3eb7`.

## Method

After 40 static client samples, production dispatch moves the occupied Arrow
chassis. The fixture removes its currently bound Coasters curve after native
movement, waits for the server fail-closed state, reinstates the exact original
Bezier curve, and invokes `VehicleOperations.repairMatchingSupport`.

The client must observe all of these before normal shutdown:

- at least one block of Arrow chassis movement before support loss;
- the mounted player and carrier-seat assignment through the fault and repair;
- the `Matching track support lost` state before repair; and
- a repaired matching rail with an empty fault while the vehicle remains stopped,
  restrained, and carrier-captured.

The generated circuit has authored curve tangents. The fixture deliberately
restores that exact native curve; generic straight placement produces different
geometry and is correctly rejected by the fingerprint interlock.

## Result

The run passed after 648 client ticks. The Arrow chassis moved
`30.940545161255425` blocks before the client observed support loss and the
subsequent repaired state. The server recorded one retained rider after repair;
the client reported `supportLossObserved=true`,
`supportLossAfterMotion=true`, and `supportRepairObserved=true` before normal
shutdown. The selected runtime record is in
[client-occupied-arrow-support-repair.txt](client-occupied-arrow-support-repair.txt).

## Limits

This is a single occupied Arrow client and validates exact restoration only. It
does not test user-operated track construction, physical chassis realignment and
later dispatch, the remaining four family clients, concurrent clients, or a
post-repair unload/reload lifecycle. Those remain FD902 work.
