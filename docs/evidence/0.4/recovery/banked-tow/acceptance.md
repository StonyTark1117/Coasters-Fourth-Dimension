# FD402 occupied quarter-bend and banked recovery

Dev.19 advances **FD402 in the 0.4 stage toward 0.9.0**. The native recovery
route for every family now traverses a complete 90-degree bend, a sustained
30-degree bank and a transition back to level before normal braking,
power cut, disconnection and unloading. This is one occupied car per family;
linked-train curve/bank recovery and complete rendered clearance remain open.

## Native route and operator controls

`BankedRecoveryExamples` builds five sections: 32-block level approach,
50.5266-block quarter bend with bank-up, 32-block constant 30-degree bank,
24-block bank-down and 24-block level runout. Anchor positions share the same
height; native banking slightly changes the centerline height. The exported
maximum absolute tangent Y is about 0.004186. This is not the compact inclined
PAX spike, whose occupied four-car towing limitation remains documented in the
[stall checkpoint](../chain-stall/acceptance.md).

`/fd demo recovery_bank_<family>` exposes the original fixtures for operator
inspection. Family names are `arrow`, `legacy`, `free_spin`, `revised` and `pax`.
The certified bare ordinary bearing starts 6/12/14 blocks ahead of the car,
according to family. One native chain spans the entire route; a Create motor
and clutch/vanilla lever supply or cut power. The motor starts at zero RPM;
operator selection, restraints, retention, coupling and start remain explicit.
No banked-joint motor block is added inside the vehicle corridor.

`occupiedRecoveryTraversesQuarterBendBankAndLevelRunout` uses actual native
bodies, couplers and seats with 32 passengers across the five cars. It selects
the ordinary bearing through sneak-use of the Rotation Tool and uses production
editor handlers to close restraints, retain, attach and start. Only the ordinary
bearing receives native eight-RPM chain propulsion. No vehicle receives added
drive force, commanded motion velocity or a motion teleport. The operator's
FakePlayer position changes only to satisfy nearby control requests.

Every native passenger retains its original seat entity until unloading. Each
car keeps its functional variant, every recovery pair stays natively linked and
both bodies remain supported on the route's matching family. The ordinary body
has no 4D vehicle record. Carriers remain captured/retained while Arrow's banked
track retains its full 360-degree profile; all five rails' profile records remain
unchanged. Traversal must visit all five sections and sample sustained banking
for more than 100 ticks. Each actual car samples that bank for 1324 ticks in the
recorded standalone run.

After the car reaches five blocks into the level runout, normal **Brake tow**
and vanilla lever interaction cut power. Eighty ticks later, readiness and zero
chain input are required while the motor remains installed at eight RPM. Normal
stationary **Detach recovery** preserves maintenance retention; **Open restraints**
precedes native passenger unmounting. The harness then removes only its owned
passengers, bodies and rail records. Production capacities and speed limits stay
unchanged. The PAX fixture is unassigned; physical four-car winch handover retains
its separate acceptance evidence.

## Measurements and independent geometry checks

Each physics sample compares both the chassis and ordinary bearing with their
nearest native snapped-block track anchors and expected track-frame quaternions.
The required bounds are 0.1 blocks of anchor error and five degrees of frame
error, together with 0.15 blocks of coupler-spacing error, 0.04 radians of retained
carrier angle and the existing 0.75-block/s recovery cap (0.02 sampling tolerance).
Recorded standalone maxima are much smaller: anchor error below 0.00033 blocks,
frame error below 0.048 degrees, spacing error below 0.00020 blocks, retained
angle below 0.00147 radians and chassis speed below 0.501 blocks/s. These native
pose measurements do not prove wheel tread, mesh, control-follower or rider
clearance.

The test exports `recovery-bank-native-frames.json` at four samples per block
for every section/family. `scripts/verify-recovery-bank-frames.py` independently
checks all five routes, normalized orthogonal frames, level endpoints, the
90-degree heading change, the full 32-block sustained bank, preserved nonzero
Arrow profile and joint continuity. Native joint position gaps are zero and
maximum frame-vector differences are about 0.00016211, below the 0.001 native
finite-difference tolerance. Deliberately corrupted copies with lost bank,
erased full turn or a 0.2-block joint gap are rejected; see
`geometry-negative-controls.json`. CI runs the same geometry checker and
retains frames separately from the prior `bank-native-frames.json` styling audit.

Standalone and combined Track Styles/Engineered profiles both pass all 36
required native tests and 25 unit tests on the same dev.19 artifact. Its SHA-256 is
`ea6593a4a3c5432b74c061ebb3005566ce018fd0ee896f85029600525db54af3`.
Both independently exported route geometries pass the checker. The existing
installed Arrow decoration and wheel/support audits also pass in the combined
profile; those earlier bounded audits do not certify full recovery mesh clearance.

The profile-specific JSON records pin the source manifest, JAR, native frame and
log hashes, 25 unit tests, all 36 required native tests and each family's actual
metrics. The accompanying native text includes progress and completion markers.
Reproduction uses Java 21:

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer --no-daemon
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build runGameTestServer --no-daemon -PfdIntegrations=styles,engineered
python3 scripts/verify-recovery-bank-frames.py /tmp/fourthdimension-7af457c2/build/runs/gametest/recovery-bank-native-frames.json
```

In a checkout without `:` in its path, use `build/runs/gametest/` instead of the
path-hashed build root. GameTests reset their disposable world; do not use
`clean` on run directories holding valuable GUI worlds.

## Remaining FD402 work

This proves occupied **single-car native traversal and alignment** for these
five original recovery routes. It does not certify linked Arrow/PAX trains on
these bends/banks, physical Arrow rack/follower interaction under retention,
complete chassis/carrier/rider clearance, rendered player/operator workflows,
Engineered FE recovery propulsion, a general coupler/grade rating, or all-family
multiplayer and fresh-JVM interruption recovery. Native pig unloading is not a
rendered safe player exit. The failed compact PAX spike climb remains unsupported.
FD402, FD404 and the 0.9 release gate remain open.

Dev.19 push/PR CI subsequently passes all eight jobs at `17c7cf8`; each job log
confirms all 36 required native tests, all five banked completion markers and
the independent geometry step. See `ci-dev19.json`. Dev.20 extends the separate
linked-train native scope in the [banked train checkpoint](../banked-trains/acceptance.md);
the original dev.19 measurements and hashes above remain historical.
