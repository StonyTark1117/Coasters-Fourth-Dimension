# Exact support-repair rejection matrix

**Scope:** FD902 native server guards for the original five functional families.
This extends `fiveFamilySupportRepairRequiresExactTrackAndRealignment` in
`OperationsGameTests`; it does not add a packaged-client or multiplayer claim.

## Checks

For each occupied Arrow, legacy ZacSpin, Free Spin, revised ZacSpin and PAX car,
the fixture removes its real Coasters matching curve while two independent
server-player riders remain in native carrier seats. It then requires:

1. explicit repair to reject while that native curve is missing, retaining the
   support-loss fault;
2. placement of a different functional track family on the occupied endpoints
   to reject without rebuilding the curve;
3. repair to reject a changed stored geometry fingerprint after the original
   matching curve is rebuilt, leaving the rail invalid and fault intact;
4. repair to reject while a carrier is uncaptured, again without clearing the
   invalid rail or fault; and
5. exact repair to succeed once, preserve the rider and closed restraints, and
   reject a repeated repair after the fault clears.

The fingerprint mismatch is induced by altering the saved hash for one test
callback, then restoring it. It exercises the production comparison but is not
a physical changed-curve placement test. Carrier capture is similarly toggled
only within the callback to exercise the interlock.

## Reproduction

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew test runGameTestServer --console=plain
```

On 2026-11-02 the isolated branch ran against the default dependency profile:

```text
FD_SUPPORT_REPAIR families=[ARROW, ZACSPIN, FREE_SPIN, ZACSPIN_REVISED, PAX] riders=10 exactGeometryOnly=true noAutoResume=true realignmentRequiredBeforeDispatch=true missingSectionRejected=true wrongFamilyRejected=true changedFingerprintRejected=true uncapturedRejected=true repeatRejected=true nativeReengagement=true normalDispatch=true postRepairHoldingReload=true
========= 44 GAME TESTS COMPLETE IN 4.783 min ======================
All 44 required tests passed :)
BUILD SUCCESSFUL in 5m 7s
```

The same command ran 26 unit tests with no failures. The five additional
rejections are inside the existing native fixture, so the GameTest count stays
at 44.

## Limits

This is server-side guard coverage. The underlying fixture also verifies
physical realignment, later dispatch, and a native holding-store reload.
A real changed-curve rebuild, simultaneous recovery replicated to two real
clients, and a post-repair client reconnect or cold server restart remain
FD902 acceptance work; packaged-client repair has separate five-family evidence.
