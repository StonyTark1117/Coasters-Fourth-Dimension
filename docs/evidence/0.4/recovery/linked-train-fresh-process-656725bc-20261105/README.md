# FD402D selected-production linked-train fresh-process recovery

This checkpoint closes the current-artifact uncertainty left by the historical
five-family single-car observations. Two representative original trains were
saved while occupied, retained, linked to an ordinary bearing and braked; a new
client JVM then reopened the same world, restored the exact native graph without
automatic motion, explicitly started and braked towing, detached, opened the
restraints and unloaded the rider.

The two cases passed on 2026-11-05:

| Case | Persisted graph | Fresh-process result |
| --- | --- | --- |
| Arrow | Chassis `919b56b0-946b-4ac9-8992-7ed066b1da47` and `dde70cfa-e5a9-416f-9c18-5771fa7bb560`; bearing `ede06dcf-1961-4bd5-b461-6a1aab88b272` | Both chassis and the bearing restored linked/retained/braked. Explicit tow moved every body about 5.465 blocks; brake, detach, restraint opening and rider unload passed. |
| PAX | Chassis `bb2d5d12-9f59-4d1b-b99c-2f1c8412ac18`, `e0a82fe6-a68a-4b83-bd19-aaf510d0c896`, `8d3927da-9dd3-4abc-b5c4-0f3450a9116e` and `4ce06d52-2d10-482d-aed5-cbe2db7b35f7`; bearing `1be3f87f-dacc-4644-a2e7-01b0a32704da` | All four chassis and the bearing restored linked/retained/braked. Explicit tow moved every body about 4.975 blocks; brake, detach, restraint opening and rider unload passed. |

Both pause clients reached 40 rendered samples with the local player mounted and
the complete linked recovery train observed. Both resume clients reported
`recoveryLinkSeen=true`, `recoveryMotionSeen=true`,
`recoveryCycleSeen=true`, `recoveryTrainSeen=true` and
`recoveryTrainMotionSeen=true`, then logged `Stopping!`. The retained pause saves
contain `recovery_cart` and player `RootVehicle` tags. The final saves contain
neither tag, independently confirming that the saved assignment and recovery
link were cleared after normal release.

## Artifact boundary

The selected clean qualification JAR is
[`qualification.jar`](../../../qualification/2026-11-05-ec2fcda/qualification.jar),
SHA-256
`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`.
The clients loaded [focused-fixture-overlay.jar](focused-fixture-overlay.jar),
SHA-256
`0e31b09efb485b57a28586d71e96662c490cf3fe0fdd2215a30f56e97837538c`.
Its entry set is identical to the selected JAR. The only changed entries are the
validation-only `CarrierValidationFixture.class` and its synthetic switch helper
`CarrierValidationFixture$1.class`; all production recovery, persistence,
boarding, vehicle, native-link and rendering entries are byte-identical to the
selected artifact. [entry-diff.txt](entry-diff.txt) records the comparison.

The focused source change teaches the opt-in resume fixture to recover the
original two-car Arrow or four-car PAX graph, require one shared ordinary
bearing, require every car retained and braked, and verify the restored native
graph before allowing the existing explicit tow/release sequence. It does not
change production behavior.

## Run controls and retained material

Java 21.0.13 and Gradle 9.2.1 compiled the focused fixture once; `compileJava`
passed. Each phase then launched Java directly with a 1536 MiB heap in an owned
transient systemd cgroup with `MemoryMax=6442450944`. Gradle was outside the
client cgroup. Arrow used private Xvfb `:238`; PAX used private Xvfb `:239`.
Both displays were stopped after their clients exited. Host memory was checked
before each launch, and no two clients owned by this task overlapped. Highest
sampled client-cgroup usage was 2,644,135,936 bytes for Arrow and 3,145,932,800
bytes for PAX, below the 6 GiB limit.

The fresh game directories initially had no named world. Each world was created
once through the normal client screen after Quick Play reported it absent; the
pause/resume acceptance sequence then used that same named save without a test
retry. [run-manifest.txt](run-manifest.txt) records the build and launch boundary.
The `arrow/` and `pax/` directories retain both process logs, pause/final worlds,
classpath, VM properties and program arguments. [verify.py](verify.py) checks
their hashes, the exact identities and lifecycle markers, the selected-JAR entry
boundary and the saved-state cleanup.

This representative current-selected-production result combines with the
retained five-family single-car observations to complete FD402D under the
roadmap's cumulative validation policy. It does not close rendered moving
clearance (FD402B), multiplayer interruptions (FD402E), or manual Engineered/FE
operation (FD402F).
