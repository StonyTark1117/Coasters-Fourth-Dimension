# Coasters: Fourth Dimension

A Create: Coasters Simulated addon with original controlled Arrow/S&S-style four-rail tracks and four-seat cars, plus legacy ZacSpin-style tracks and eight-seat free-rotating cars. Each car has two independent native Sable carrier groups. Controlled Arrow cars can form trains; legacy cars operate individually.

FD101–FD109 are **implemented and validated for the bounded 0.1 build**. See [acceptance evidence and limits](docs/PLAYABLE.md). S&S Free Spin, revised ZacSpin, PAX shuttles, and external Track Styles integration follow the [roadmap to 1.0](ROADMAP.md).

## Install

Install the addon and its dependencies on both client and server:

| Component | Tested version |
| --- | --- |
| Minecraft / Java | 1.21.1 / Java 21 |
| NeoForge | 21.1.248 |
| Create | 6.0.10 |
| Create: Coasters Simulated | 0.1.5 |
| Sable | 2.0.3 |

Use the dependencies supplied by those mods, including Flywheel 1.0.6, Sable Companion 1.6.0 and Veil 4.1.4. Exact development artifact pins are in [build.gradle](build.gradle). No optional coaster addon is required or bundled.

Build and ride within **±1024 blocks of horizontal world origin**. The foundation exposed a native physics precision issue at large coordinates; this restriction remains in 0.1. A dimension supports up to 256 sections, 64 vehicles and 8192 total rotation-profile keys. Back up valuable worlds before trying a development build.

## Build a ride

1. Obtain matching track and car items, a Rotation Tool, and a Boarding Station from crafting or the Fourth Dimension creative tab. Place Coasters anchorpoints and use one of our track items on two anchors. A span may be at most 64 blocks; survival placement consumes one matching track item per four blocks, rounded up.
2. Place the matching car on its track. Leave room for the full wings, riders, and their rotation. Vehicle placement rejects obstructed wing envelopes. Leave at least six blocks between controlled-car axles. Use Coasters' chain coupling on two captured Arrow cars with a six-block snapped link distance. Legacy cars cannot couple.
3. For Arrow rotation, use the Rotation Tool on a section before placing cars. Keys use `distance:degrees:slope`, separated by semicolons; slope is degrees per block. Keys must cover the entire displayed section length. For a 36-block section, `0:0;18:360;36:0` makes one full turn and returns. Adjacent sections must match unwrapped angle and slope. `360` and `0` have different control-rail positions.
4. Copy/paste, mirror, reverse and explicit geometry rebinding are available in the editor. Editing occupied sections or their immediate approaches is rejected. Rebind after changing native curve geometry. Use Preview Seats to inspect cyan pose markers and yellow/red conservative clearance boxes. Free-spin previews label their starting conditions and are examples; live motion comes from native physics.
5. Place loading platforms outside the wing envelope. Wait for stopped translation, captured upright carriers and low angular speed, then right-click an empty seat. Use the Rotation Tool on the car, station, or in the air while riding to close restraints and dispatch. Coasters/Create propulsion moves the chassis; the addon controls the rotary mechanisms.
6. For legacy cars, tune damping and declared ballast on an empty captured car. Each rider contributes a declared 80 kg at the same modeled torso location; ballast is per carrier. Gravity, chassis movement and inertia determine free motion.

A Boarding Station binds to a car within twelve blocks on first use; shift-use rebinds it. Its **rear input** requests capture when powered and dispatch on the falling edge. It acts when the bound car is within eight blocks. The **front output** is ready/aligned; the **right output** is restraints closed. The lights show the same states. Dispatch also requires captured carriers, closed restraints and valid matching track. Retain/Release Retention provides a maintenance interlock.

Locking seats never changes the track family. An Arrow car requires Arrow functional track; a legacy car requires legacy track. Ordinary access is certified only for the pinned, unmodified Coasters bearing. It receives no rotating seats or 4D behavior. Added bodies are rejected until their gear and clearance are separately certified. Rail access does not certify towing a 4D train.

## Demonstration layouts

Operator level 2 is required. In an empty test world, run:

```text
/execute positioned -100 120 0 run fd demo arrow
/execute positioned 100 120 0 run fd demo legacy
```

Each command reserves a clear 47 × 17 × 69 region starting above/ahead of the command position. The original examples include track, a car, platforms, a boarding station and native Coasters chain drives powered by Create creative motors at 32 RPM. Arrow has a full-turn rotation program; legacy has hills. Board, close restraints and dispatch. After the car leaves, power the rear of the station so it captures the returning car; release power to dispatch again.

`/fd status` reports angles, occupancy and capture. `/fd clear` removes this dimension's addon vehicles and their track connections; it leaves station blocks, platforms and motors. Use it only in disposable test worlds. Other `/fd` commands are foundation diagnostics; they do not replace the normal items or station workflow.

## Development and verification

```sh
./gradlew build
./gradlew runGameTestServer
./gradlew runClient
./gradlew runServer
```

Use `JAVA_HOME=/usr/lib/jvm/java-21-openjdk` on this workstation. The generated server directory needs Minecraft EULA acceptance. Offline mode is for loopback development identities only.

The artifact is `build/libs/coasters-fourth-dimension-0.1.0.jar`. For Unix checkout paths containing `:`, outputs move to `/tmp/fourthdimension-<path-hash>/build` to avoid Java classpath parsing errors. Run directories live under that build directory. `clean` deletes them; keep valuable worlds elsewhere. GameTests recreate their own disposable world.

After `build`, `-PfdPackaged` runs the built JAR using an isolated source set and separate `packaged-client`/`packaged-server` directories. It does not load the addon's loose compiled classes. `-PfdConnect=127.0.0.1:25581` connects to a test server; `-PfdWorld=FolderName` opens an existing single-player save. `runObserver` starts a second identity. `-PfdValidation` logs functional/native pose snapshots; `runServer -PfdFrozen` starts paused for saved-state comparisons.

Design, licenses and evidence: [PLAN.md](PLAN.md), [ROADMAP.md](ROADMAP.md), [THIRD_PARTY.md](THIRD_PARTY.md), [foundation acceptance](docs/FOUNDATION.md), [0.1 acceptance](docs/PLAYABLE.md).
