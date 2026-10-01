# Occupied PAX lift eviction and explicit resume

The native PAX chunk test now evicts an entire occupied shuttle during lifting.
Four coupled chassis, eight rotating carriers and the physical winch carriage leave
the native container, then return as fresh instances with their original UUIDs.
All sixteen native pig passengers return to their original carrier attachments.
The restored winch remains faulted with `LIFTING` as its interrupted phase; it cannot
open restraints or resume automatically. Production shuttle behavior is unchanged.

## What the test proves

`occupiedLiftNativeEvictionExplicitResume` builds the existing compact, three-section
PAX example in its own GameTest batch. It uses native seats, coupling, the Create
creative motor, validated dispatch and the ordinary winch state machine. At tick
150 it verifies an active lift and real carriage, retains the original block/body
references, and releases the complete fixture's forced chunk tickets.

The test waits until all original block entities are marked removed and their
world chunks are absent from the non-loading resident-chunk lookup. All thirteen
original native bodies must also be marked removed and absent from the Sable
container. Saved occupancy must retain two riders on each of the eight carriers,
with restraints closed. None of these absent-state probes requests a chunk load.

This accelerated GameTest explicitly drains native save work at tick 400, and at
tick 1000 if eviction is still pending. It allows up to 8000 ticks for asynchronous
eviction within a 12000-tick test timeout. This is native storage eviction evidence,
not a natural eviction latency or performance measurement.

After reinstating the tickets and allowing 100 ticks:

- Every block entity and body must be a fresh instance. The winch retains the
  original ordered consist and carriage UUID.
- All sixteen original living rider UUIDs must actually be mounted on their
  original native carrier bodies, not merely listed in saved occupancy.
- The winch must remain in `FAULT`, with interrupted phase `LIFTING`. Opening the
  restraints must be rejected throughout the next 60 ticks.
- During that hold, vertical drift must remain below 0.3 blocks. An explicit call
  to the public `resume()` operation then resumes the lift.

After resume, the train must pass through lifting, outbound gravity travel,
returning, capture and final boarding. It retains all riders throughout, all four
cars remain coupled, and every car becomes boarding-ready. A further twenty ticks
of occupied boarding precede normal restraint opening and unloading; every original
pig must remain alive and cease riding normally.

## Engineered ownership through reload

With exact Engineered 1.1.1 present, the fixture installs a native motor span across
all three route sections, plus a controller and observer outside the train envelope.
The eight block-entity references then include that controller and observer as well
as the four anchors, winch and Create motor. Both additional blocks must genuinely
evict and return as fresh instances.

The existing Engineered PAX checker now has a reload-aware entry point. It reacquires
fresh block entities using resident chunk maps and queries upstream linkage only
while the complete route is ticking. This guard matters because upstream `isLinked()`
can resolve/load its anchor; the observer must not keep the test route resident.
The original occupied power-loss test retains its existing entry point and checks.

The native controller issues backward launch at 100 during lifting/capture, forward
launch at 100 during fault/boarding, STOP during outbound travel, and TRIM to 1 on
return. Each phase requires more than two ticks of native controller presence and
the observer must detect the train. These commands must remain subordinate to the
winch and catch. The fixture asserts the upstream default `requireEnergy=false`,
so empty FE cannot explain the lack of external propulsion. It does not test FE-on
PAX behavior or a user-wired automatic controller sequence.

## Evidence and reproduction

All final profiles pass all 29 registered GameTests. Explicit optional-addon skips
are five for base, three for Engineered and zero for combined. All 19 unit tests
also pass. Each final PAX run proves complete eviction at tick 901 and restores all
sixteen passengers. Measured vertical hold drift is about 3.4×10⁻⁹ blocks for base
and Engineered, and 4.1×10⁻⁹ blocks for combined, below the 0.3-block acceptance bound.

The Engineered/combined controller reports presence for respectively 338/338 lift
ticks, 159/159 fault ticks, 174/174 outbound ticks, 58/59 return ticks, 98/90 capture
ticks and 20/20 final boarding ticks. Both native observers detect the train.
The original occupied power-loss and limited-energy tests also pass in both profiles;
all profiles pass the saved-passenger regression and ten connected-route checks.

Use Java 21 and run the native profiles sequentially; they share the GameTest world:

```sh
./gradlew build runGameTestServer
./gradlew runGameTestServer -PfdIntegrations=engineered
./gradlew runGameTestServer -PfdIntegrations=engineered,styles,additions,magnetized,immersion
python3 scripts/verify-pax-occupied-chunks.py docs/evidence/integrations/2026-10-01/pax-occupied-chunks
```

Exact mod lists, filtered checks, all warning/error lines, unit XML, source/JAR hashes
and full local log hashes accompany this report. Native Gradle runs execute compiled
classes; the JAR is the accompanying build artifact. No packaged-server configuration
or user world is changed by these GameTests.

Two rejected attempts remain visible. The first incorrectly expected nine native
bodies and failed before releasing tickets; correcting the fixture to thirteen
reflects the real two-carrier PAX design. The second proved native body eviction but
timed out before block-entity removal. The subsequent exploratory run passed with
non-loading resident/ticking diagnostics and a longer wait. Final tests retain the
bounded asynchronous wait and optional second save drain. No runtime behavior was
changed to manufacture eviction. The initial final-base run preceded the last
Engineered observer guard, so a separate base repeat verifies the final source.

## Limits

This covers one compact occupied lift interruption with native pigs, followed by
explicit resume and one complete cycle. It does not establish occupied interruption
during outbound/return/capture, partly loaded occupied routes, tall or alternative
layouts, human-player clients, camera/transport recovery, FE-on PAX equipment,
corrupted saves, optional-addon removal or performance. Other addons are present
in the combined profile, but their active equipment is not added to this new fixture.
The broader FD305/FD901–FD904 matrices remain open.
