# Coasters: Fourth Dimension

A Create: Coasters Simulated addon developing five original 4D families: controlled Arrow/S&S cars, legacy ZacSpin, magnetic S&S Free Spin, revised ZacSpin, and a PAX-style catch/lift/release shuttle. Every family has matching functional track. Arrow and PAX cars support family-specific native coupling; the other families operate individually.

FD201–FD204 implement the **bounded 0.2 build**. See [magnetic-family instructions and acceptance](docs/MAGNETIC.md), and the earlier [0.1 acceptance](docs/PLAYABLE.md). The current `0.9.0-dev.4` branch retains the completed **bounded 0.3 gate**, adding revised carriers and a physical PAX catch/lift/release shuttle with five-family packaged single-player and dedicated-server evidence; see [instructions, acceptance, and outstanding gates](docs/REVISED-SHUTTLE.md). Optional track appearance and Engineered operating adapters are in development under [FD401](docs/INTEGRATIONS.md), with schema/protocol 5; clients and servers must upgrade together. Engineered native tests include a coupled Arrow full turn and rollback; inspected Arrow decoration falls back to Original above +720° without changing the profile. It is not yet the 0.9 release candidate. The four-pair Free Spin and revised topologies are original game approximations; exact manufacturer coupling is not claimed.

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

Build and ride within **±1024 blocks of horizontal world origin**. The foundation exposed a native physics precision issue at large coordinates; this restriction remains in the current development build. A dimension supports up to 256 sections, 64 vehicles and 8192 total rotation-profile keys, and 512 magnetic spans (eight per section). Back up valuable worlds before trying a development build.

## Build a ride

1. Obtain matching track and car items, a Rotation Tool, and a Boarding Station from crafting or the Fourth Dimension creative tab. Place Coasters anchorpoints and use one of our track items on two anchors. A span may be at most 64 blocks; survival placement consumes one matching track item per four blocks, rounded up.
2. Place the matching car on its track. Leave room for the full wings, riders, and their rotation. Vehicle placement rejects obstructed wing envelopes. Leave at least six blocks between controlled-car axles. Use Coasters' chain coupling on two captured Arrow cars with a six-block snapped link distance. Legacy and Free Spin cars cannot couple. Leave twelve blocks between Free Spin axles and fourteen between revised axles. PAX cars link six blocks apart, up to four cars; their separate winch workflow is described in the shuttle guide.
3. For Arrow rotation, use the Rotation Tool on a section before placing cars. Keys use `distance:degrees:slope`, separated by semicolons; slope is degrees per block. Keys must cover the entire displayed section length. For a 36-block section, `0:0;18:360;36:0` makes one full turn and returns. Adjacent sections must match unwrapped angle and slope. `360` and `0` have different control-rail positions.
4. Copy/paste, mirror, reverse and explicit geometry rebinding are available in the editor. Editing occupied sections or their immediate approaches is rejected. Rebind after changing native curve geometry. Use Preview Seats to inspect cyan pose markers and yellow/red conservative clearance boxes. Free-spin previews label their starting conditions and are examples; live motion comes from native physics.
5. Place loading platforms outside the wing envelope. Wait for stopped translation, captured upright carriers and low angular speed, then right-click an empty seat. Use the Rotation Tool on the car, station, or in the air while riding to close restraints and dispatch. Coasters/Create propulsion moves the chassis; the addon controls the rotary mechanisms.
6. For either free-spin family, tune damping and declared ballast on an empty captured car. Each rider contributes a declared 80 kg at the same modeled torso location; ballast is per carrier. Gravity, chassis movement and inertia determine free motion. Free Spin also offers empty, balanced (160 kg per pair), and left-front-only ballast presets; these change ballast, not passenger count. Apply them only to an empty captured car.

A Boarding Station binds to a car within twelve blocks on first use; shift-use rebinds it. Its **rear input** requests capture when powered and dispatch on the falling edge. It acts when the bound car is within eight blocks. The **front output** is ready/aligned; the **right output** is restraints closed. The lights show the same states. Dispatch also requires captured carriers, closed restraints and valid matching track. Retain/Release Retention provides a maintenance interlock.

Locking seats never changes the track family. An Arrow car requires Arrow functional track; a legacy car requires legacy track; a Free Spin car requires magnetic Free Spin track. Ordinary access is certified only for the pinned, unmodified Coasters bearing. It receives no rotating seats or 4D behavior. Added bodies are rejected until their gear and clearance are separately certified. Rail access does not certify towing a 4D train.

## Magnetic equipment

Use a Magnetic Inducer or Magnetic Damper on matching Free Spin track to install a span and open its editor. The Rotation Tool also opens that editor. Each added span consumes one matching equipment item in survival; removing it refunds the item. Orange bars induce spin by taking translational energy; cyan bars damp rotation. Propulsion still comes from Coasters/Create.

Enter semicolon-separated spans as `start:end:induce|damp:direction:strength:falloff:targets`. For example, `3:9:induce:1:4:0.2:1;12:18:induce:-1:4:0.2:8;24:32:damp:1:5:0.2:15` fits a 36-block section. Distances follow the first-to-second anchor direction. Direction is `1` or `-1`, strength `0..20`, and falloff `0..0.5` is the fraction of each end used for a linear ramp. Target masks add left-front `1`, left-rear `2`, right-front `4`, right-rear `8`; `15` targets all four. Group names refer to the car's local frame and do not swap during rollback.

Spans must be at least 0.05 blocks long and remain inside their section. Editing occupied sections/approaches is rejected. Preview loading cycles through empty, balanced and front-loaded reference cases; **Preview seats** draws the selected case without changing the car. Reference previews use declared starting conditions and constant powered speed; live native motion can differ.

## Demonstration layouts

Operator level 2 is required. In an empty test world, run:

```text
/execute positioned -100 120 0 run fd demo arrow
/execute positioned 100 120 0 run fd demo legacy
/execute positioned -400 120 0 run fd demo free_spin
/execute positioned 400 140 0 run fd demo revised
/execute positioned 0 140 0 run fd demo pax
```

The Arrow, legacy, Free Spin, and revised circuit commands reserve a clear 47 × 17 × 69 region starting above/ahead of the command position. The original examples include track, a car, platforms, a boarding station and native Coasters chain drives powered by Create creative motors at 32 RPM. Arrow has a full-turn rotation program; both free-spin examples have hills. Free Spin also has two oppositely directed, separately targeted inducer spans and an all-group damper on the far straight. Board, close restraints and dispatch. After the car leaves, power the rear of the station so it captures the returning car; release power to dispatch again.

The PAX command builds its separate four-car two-spike layout and physical winch. `/fd demo pax_tall` creates the taller shuttle example. See [shuttle operation and restart recovery](docs/REVISED-SHUTTLE.md); these examples need larger clear corridors.

`/fd status` reports angles, occupancy and capture. `/fd clear` removes this dimension's addon vehicles and their track connections; it leaves station blocks, platforms and motors. Use it only in disposable test worlds. Other `/fd` commands are foundation diagnostics; they do not replace the normal items or station workflow.

## Development and verification

```sh
./gradlew build
./gradlew runGameTestServer
./gradlew runClient
./gradlew runServer
```

Use `JAVA_HOME=/usr/lib/jvm/java-21-openjdk` on this workstation. The generated server directory needs Minecraft EULA acceptance. Offline mode is for loopback development identities only.

The artifact is `build/libs/coasters-fourth-dimension-0.9.0-dev.4.jar`. For Unix checkout paths containing `:`, outputs move to `/tmp/fourthdimension-<path-hash>/build` to avoid Java classpath parsing errors. Run directories live under that build directory. `clean` deletes them; keep valuable worlds elsewhere. GameTests recreate their own disposable world.

After `build`, `-PfdPackaged` runs the built JAR using an isolated source set and separate `packaged-client`/`packaged-server` directories. It does not load the addon's loose compiled classes. `-PfdConnect=127.0.0.1:25581` connects to a test server; `-PfdWorld=FolderName` opens an existing single-player save. `runObserver` starts a second identity. `-PfdValidation` logs functional/native pose snapshots; `runServer -PfdFrozen` starts paused for saved-state comparisons.

Design, licenses and evidence: [PLAN.md](PLAN.md), [ROADMAP.md](ROADMAP.md), [THIRD_PARTY.md](THIRD_PARTY.md), [foundation acceptance](docs/FOUNDATION.md), [0.1 acceptance](docs/PLAYABLE.md), [0.2 acceptance](docs/MAGNETIC.md).
