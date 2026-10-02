# Engineered held presence, sequences and observers

Parent: `11d1e216d3d7a11e40dab127ebe7e3bcc5261441` on the release-derived
Engineered worktree. This checkpoint covers pinned Engineered 1.1.1 and Coasters
0.1.5. Exact artifacts, source hashes, commands and raw logs accompany the results.

## Problem and correction

The native Engineered motor callback reports cart speed/presence before choosing
its force command. Cancelling it at entry prevented that report for captured,
retained or faulted cars. A captured Arrow car on a linked STOP span therefore
appeared absent to the controller in the native regression.

Moving only the motor cancellation was insufficient: the base chain-drive entry
guard could cancel before Engineered's own HEAD callback ran. The final change
uses two explicit boundaries with the original hold predicates:

- For exact inspected Engineered, let its native callback report presence, then
  cancel before `LinearMotorControllerBlockEntity.commandMode()` and all native
  motor acceleration, braking and parking work.
- For that same release, cancel base chain matching at its first span query,
  after addon HEAD callbacks and before any native chain speed matching.
- Keep the original chain entry guard for absent or uninspected Engineered.

There are no new optional compile dependencies, reflection adapters, registry
changes or saved-data changes. The PAX and ordinary recovery hold policies remain
in `PropulsionInterlock`; whole-consist motor authority remains in
`EngineeredInterlock`.

## Native scenario

An isolated batch builds five original 64-block straight functional tracks, one
per family. Each has a 24-block native motor span, a STOP/LAUNCH/COAST program and
an observer targeting the 18-block position. Cars begin 12 blocks along the track.
The test uses native block entities, graph spans, passenger seats and physics.

1. Wait for normal capture/boarding readiness and require native controller
   activity on STOP for all five held cars.
2. Place a redstone block adjacent to each controller, then remove it. The normal
   server tick must advance STOP to LAUNCH. After 80 ticks, all five still have
   open restraints and captured cars; drift stays below 0.1 block and the program
   must remain LAUNCH rather than treating the held car as departed.
3. Board one native pig per car, close restraints through `VehicleOperations`,
   and dispatch the four non-PAX families through that same validation path.
   PAX stays held; no external dispatch substitutes for its winch state machine.
4. Record real observer crossings and verify their world redstone signal. Each
   non-PAX controller must advance automatically to COAST only after physical
   departure; the fixture then requests normal capture.
5. Require readiness, retained full-health passengers, closed restraints and no
   faults, then open restraints and perform native dismounting. PAX must remain
   on its original span with no observer crossing throughout.

`nativePresent` in the logs is the public controller `isDriving()` readout during
STOP/LAUNCH, not direct access to a private occupancy field. The held LAUNCH phase
also proves continued reporting: native pass logic would advance after five clear
ticks once engaged. Native LAUNCH/activity indicates a requested motor command;
Fourth Dimension's station readiness remains the dispatch authority.

Both installed profiles recorded four occupied departures at 14.51–14.91 blocks
of travel, with native observer output reaching signal strength 3. Maximum drift
during the held redstone pulse was approximately `3.04e-10` blocks. All five
families completed capture/readiness and unloading; PAX stayed captured throughout.

The existing 22-call interlock test remains in the suite. It independently checks
zero added propulsion impulse for 18 unsafe self/peer states and four positive
launch controls, guarding against reporting accidentally enabling motor power.

## Evidence and limits

`setup-attempt.log.gz` retains the initial invalid test placement outside the
1,024-block building limit. Its proposed 96-block span was also corrected to the
existing 64-block maximum before testing the behavior. It proves no presence bug.

`before.log.gz` records the corrected fixture failing on the original entry
cancellation. `motor-only-attempt.log.gz` records the insufficient one-boundary
change failing at the same captured Arrow check. Each completed its suite with
that one required failure. `before-source.json` pins the regression source used
before the fix; the final test source is identical. To reproduce the baseline,
apply the new sequence test and its two package-private reflection helpers to the
parent commit while leaving both guard mixins at that parent. Applying only the
new `EngineeredMotorGuardMixin` reproduces the insufficient motor-only attempt.

The final profile matrix loads the built JAR using `-PfdPackaged`; optional
scenarios explicitly skip when Engineered is absent. `manifest.json` records the
final artifact hash, all unit results and each completed profile. Run the profiles
sequentially because each recreates the same disposable GameTest world:

```sh
./gradlew build
./gradlew -PfdPackaged -PfdIntegrations=engineered runGameTestServer
./gradlew -PfdPackaged -PfdIntegrations=styles,engineered runGameTestServer
./gradlew -PfdPackaged runGameTestServer
./gradlew -PfdPackaged -PfdIntegrations=styles runGameTestServer
python3 scripts/verify-engineered-presence.py
```

All 26 unit tests passed. Each of the four packaged profiles passed all 43
registered native tests; Engineered-only scenarios explicitly skipped in the two
profiles without Engineered. The evidence verifier and Java formatting check
also passed. The tested JAR SHA-256 is
`9daf801509869f4791a8db70b45c1177b23a7de3278379ecac4c05014a49eb7c`.

This closes the captured-presence/native-sequence regression. It does not certify the controller GUI, generator/cable
networks, cold restart of arbitrary programs, two human clients, every curve or
train configuration, or other Engineered versions. PAX here is one stationary
captured car; existing real winch-cycle tests provide their own separate scope.
FD401 and the broader six-integration goal remain open.
