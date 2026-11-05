# FD401 evidence review at source `54495a5`

This read-only review checked candidate identity and the PAX long-hold blocker
against retained artifacts. It is a scoped release-evidence checkpoint, not
runtime acceptance or a final FD401 sign-off.

## Artifact identity

- The frozen `2c5d9a7` candidate's `verify.py` passed: SHA-256
  `4b23cf0e1766617eb106a7ffcf293f73639d2bad119872c218b7cb2bb52b7781`,
  111 recorded classpath files and 343 JAR entries. All four files in its
  `SHA256SUMS` passed when checked from that evidence directory.
- The later `54495a5` JAR has SHA-256
  `e78651e98c79ba1eeb50d50f2e21ded9970318d3d2e69455c0234f66e112cd6e`.
  All 351 files in its isolated class and resource output appear in the JAR
  byte for byte; the JAR has 353 file entries including two generated entries.
- Relative to the frozen JAR, the later JAR changes 34 production classes,
  6 resources and 9 GameTest classes. The frozen candidate's exact-JAR
  client evidence therefore cannot be promoted to exact evidence for the
  later package. Its bounded historical claims remain tied to `2c5d9a7`.
- At review time, `main` was `73a792d`. Since `54495a5`, three production
  source files (`RecoveryClearancePolicy`, `RecoveryExamples`,
  `RecoveryOperations`) and two fixture files changed. The `54495a5` JAR
  does not represent those later committed changes. Source inspection scopes
  them to FD402 recovery towing and its rider-equipment interlock:
  `RecoveryOperations.tick` iterates only vehicles with a non-null
  `recoveryCart`. `ShuttleRuntime`, `CarrierRuntime`, `VehicleOperations`,
  and main resources have no diff from `54495a5` to `73a792d`. This supports
  reuse of the earlier package for a focused PAX winch hold attempt, but is
  not whole-JAR parity for a later release artifact.

## PAX blocker cross-check

The failed `1f13a0e` normal-client package retains SHA-256
`c63ea2824cd007dea1859e2dd5d78d785517fffaccc22272f4f4f94502ced703`.
Its stopped world archive hashes to
`fbcbc88b90821cda998f1a9f5b018afe112ecf568e1261d0519db9198f011ce4`.
The server log records both native links to middle car `ebd70587` unlinking at
17:03:33. The decoded stopped world says `shuttle_interrupted=OUTBOUND` and
game time 191424; the post-removal query was 189028, only 2,396 ticks earlier.
That record proves an outbound blocker on the older package. It does not
establish a 4,800-tick LIFTING result on the later package.

## Remaining decision and evidence

The current PAX mechanics changes need a completed 4,800-tick LIFTING hold,
packaged normal-client recovery, and cold-restart acceptance if that claim is
retained. Before calling these exact-current-source evidence, select and
identify the source/JAR revision that includes the later committed changes.
The final support statement must keep older exact-candidate results scoped to
their frozen artifact or obtain acceptance for the selected later artifact.
