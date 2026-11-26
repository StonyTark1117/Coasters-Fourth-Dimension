# FD2001 native rail adapter and first production coordinator

2026-11-22, primary `main` over `f85f4a27`, uncommitted 1.5 integration source.
Java 21.0.13.1, `--no-daemon`, private persistent build and project cache under
`/mnt/NVME4/fourthdimension-runs/fd15-moving-native-20261122`.

## Accepted bounded evidence

- `native-6.log`: six required tests passed; the physical adapter case plus five
  previously accepted persistence cases sharing its namespace. Real native Wing,
  Sable bed, native rail anchors/curves and solver constraints. Eight-block lateral
  travel: bed x −319.500000 → −311.500427, y 130.499847 versus target 130.5.
  Fixed bridge edges absent during travel, including simulated stale graph rebuild;
  locked fixed edges restored with native adjacency and no edge duplication.
  Original chassis UUID `f0719401-1e1b-4420-98fb-7181813c8f6a` followed the bed and
  resumed its native track guide at the receiving dock.
- The same case stopped midway, serialized/restored the same bed/body identities,
  and resumed explicitly. Injecting a missing saved body identity denied travel
  while the surviving chassis stayed within 0.05 blocks for 20 ticks. The fixture
  removed that injected identity before continuation; production preserves missing
  identities. This is an in-process solver reconstruction, **not cold restart**.
- `production-1.log`: one required test passed. Real `MovingConstruction.build`,
  transfer controller, Creative Motor, registered original Wing and normal
  `VehicleOperations.restraints`, followed by authoritative CAPTURE/MOVE/RELEASE.
  Four-block movement ended at x −415.500427; actual locked bridge and original
  native guide remained valid 30 ticks after release. Vehicle metadata and closed
  restraints survived. `RELEASING` correctly retained receiving reservations until
  the train departs; this fixture did not prove departure or unload.

The adapter fixture deliberately suspends normal Wing metadata and directly sets
its restraint state after native allocation. The production fixture does neither;
it uses a fake operator and disclosed helper-built fixed approaches. Neither is
occupied client proof, normal player construction, rendered support inspection,
or cold-process persistence. Those remain separately assigned evidence.

## Diagnosis retained

`native-1.log` exposed an initial native COM/rotation-point mismatch (bed one block
below request). Construction now initializes the actual COM before its initial
teleport; motion itself uses finite native constraint motors. `native-2.log` and
`native-4.log` then exposed normal Wing support lookup missing an authenticated
transient dock bridge. The live route hook repaired that production lookup;
`production-1.log` proves the normal coordinator path. `native-3.log` and
`native-5.log` were compilation failures, not runtime attempts.

## Reproduction and preservation

Use `fourthdimension_fd2001_native` for the adapter holder or
`fourthdimension_fd2001_production` for the separate coordinator holder with
`-PfdValidation runGameTestServer`. Preserve worlds before another namespace run.
`native-6-source.sha256` and `production-1-source.sha256` retain source identities.
Worlds are `world-native-6` and `world-production-1`, outside the disposable build.
Both logs confirm all dimensions saved and GameTest server shutdown; their owning
Gradle command sessions exited 0. No GUI, Xvfb, service, or server remains live
for these cases. No prior accepted family/release campaign was repeated.
