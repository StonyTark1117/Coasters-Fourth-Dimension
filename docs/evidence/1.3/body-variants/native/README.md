# FD2102 native body transactions — 2026-11-22

PASS: two required tests, first run, Java 21.0.13.1 and Gradle 9.2.1; clean shutdown and build exit 0.

Source: `src/main/java/dev/fourthdimension/gametest/BodyVariantGameTests.java`; exact hashes in `inputs.sha256`. Primary checkout `main`, HEAD `b5e78f31` plus the concurrent FD2102 working changes. No commits/staging performed by this owner.

Command from primary checkout, JAVA_HOME=/usr/lib/jvm/java-21-openjdk and its bin first in PATH:

```
./gradlew --offline --no-daemon -PfdBuildDirectory=/mnt/NVME4/fourthdimension-runs/fd13-spinning-20261122/body-build -PfdGameTestNamespaces=fourthdimension_fd13 runGameTestServer
```

`run1.log` is the full raw Gradle/native log. Test world retained at `/mnt/NVME4/fourthdimension-runs/fd13-spinning-20261122/body-build/runs/gametest`. No GUI or input claims. Preflight: 47 GiB available host RAM, no swap, NVME4 XFS with 163 GiB available; command-line GameTest needs no display. Launcher and daemon both Java 21.

Both actual native Wing and Fixed Inverted were captured on finite matching straight track. Each applied alternate → base → alternate through production EditorNetwork action 82, requiring exactly one revision increment and equality of all serialized FoundationData state except body/revision. Actual FoundationData save/load retained all vehicle NBT; the real FoundationSync.Snapshot codec retained selection and complete payload. Missing body migrated to base. Unknown and known wrong-family persisted IDs retained their raw ID and resolved to BASE without changing mechanical identity/state.

Unknown new choice, wrong-family choice, stale revision, denied build permission, real native seat occupancy, and physically moving native body each rejected atomically against the entire saved FoundationData tag. Occupancy used an actual pig mounted on the native seat and authoritative carrier readback; it is not player input proof. Movement used normal close/dispatch editor actions plus disclosed native-pipeline velocity injection, then measured speed >.08 and displacement >.05 before the rejected body transaction. Both measured speed ~2.000000015 blocks/s and displacement 1.0107421875 blocks. Both subsequently captured and settled with the accepted body unchanged.

No renderer, client GUI, or cold-process reload claims. `cleanup.txt` confirms no owned Java process remains after normal save and exit. No further run required for these accepted body transaction cases absent relevant code change or defect.
