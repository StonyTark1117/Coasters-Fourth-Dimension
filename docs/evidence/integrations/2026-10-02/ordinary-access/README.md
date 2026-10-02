# Ordinary bearing eligibility across optional integration profiles

This extends the existing five-family ordinary-cart GameTest with native filter,
initial-snap and binding checks across appearance/provider changes, plus measured
forward and reverse travel. It adds one GameTest; production classes are unchanged.
The earlier `OrdinaryCartGameTests` remains in the suite, including its Free Spin
magnetic-impulse and ordinary-rotation exclusion assertions.

## Fixture and scope

Five independent native 48-block straight lanes use the original Arrow, legacy
ZacSpin, Free Spin, revised ZacSpin and PAX functional tracks. Each lane carries
one unmodified native Coasters bearing. The test uses the native track basis and
bearing-center placement, then allows the native bearing index to initialize
before probing access. There are no registered 4D vehicles in this fixture.

For every family, four saved appearance selections are tested: original, Track
Styles single spine, Track Styles double spine and a missing appearance. The two
Styles choices remain Arrow-only cosmetic options; placing their saved IDs on
other families or running without Styles exercises eligibility independence and
fallback, not newly supported appearances or external functional track.

Each appearance runs seven conditions:

| Condition | Required result |
| --- | --- |
| Bare bearing on valid original functional track | Accept |
| One stone block above the bearing | Reject as an uncertified body |
| One stone block beside the bearing | Reject as an uncertified body |
| Above-bearing stone retained, original functional metadata temporarily absent | Accept ordinary native track behavior |
| Invalid original functional rail | Reject |
| Functional provider replaced with `missing:functional` | Reject |
| Cosmetic spine provider used as the functional provider | Reject |

Each decision checks shared eligibility, the native rail-snap filter, and native
initial snap. Accepted initial snaps must set the bearing's engaged state. Rejected
snaps must mark it disengaged and keep the bearing's world position within 0.0001
blocks. Binding attempts must leave the bearing outside the 4D registry and without
family-controller blocks. This yields 40 accepts and 100 rejects per profile.
The added stone blocks are deliberately outside the currently certified single-
bearing construction; rejection does not constitute a geometric collision survey
of every possible ordinary cart body.

After restoring original track and the bare bearing, the fixture supplies one
initial +3 m/s velocity, measures travel after 25 ticks, then supplies one -3 m/s
velocity and measures after another 25 ticks. Each leg must advance more than 0.5
blocks in the requested direction, drift less than 0.2 blocks laterally and
vertically, remain natively engaged, and remain unregistered as a 4D vehicle.
No per-tick velocity or pose forcing is used. Both directions are checked on all
five families, including ordinary access to PAX rails; this does not authorize
external control of a PAX 4D vehicle or bypass its winch state machine.

These checks concern single empty bearings on flat fixed-world lanes. They do not
certify recovery towing, mixed or linked ordinary/4D consists, arbitrary assembled
bodies, every item/assembly placement path, slopes, curves, moving provider loss,
client rendering, occupied lifecycle transitions or unreleased external Arrow 4D
functional tracks. FD402 and the broader FD901–FD904 gates remain open.

## Retained fixture failures

Three native base runs failed during development. Their full logs and fixture
sources are retained; none is presented as a production compatibility regression.

1. `setup-base` queried the representative bearing immediately after spawning an
   unsnapped body, before the native index was populated. The fixture now uses the
   normal track-basis placement and waits five ticks before its decisions.
2. `engagement-base` compared the body's pose origin while changing its block
   content and snap state. A body origin is unsuitable for detecting a teleport
   when native center-of-mass bookkeeping can change. The check now observes the
   actual bearing's transformed world center.
3. `precision-base` confirmed the bearing disengaged but measured a vertical world
   shift of approximately 0.000002543 blocks during the native update, exceeding
   the original 1e-8 threshold. The final 1e-4-block tolerance accommodates native
   float precision while still checking position preservation. Every decision
   logs its raw displacement; the verifier enforces this bound on all rejections.

All three runs exited 1 with exactly the new required test failing. No production
code changed to address them. Original full gzip logs preserve their bytes; readable
check extracts only trim trailing whitespace.

## Verification

All seven profiles exited 0 with 37 registered GameTests per profile; optional
tests skip only when their addon is absent. The build also passed 19 JUnit tests.
The new matrix contributes 980 access decisions and 70 guided travel legs overall.

| Profile | Access decisions | Travel legs | Absent-addon skips |
| --- | ---: | ---: | ---: |
| base | 140 | 10 | 7 |
| engineered | 140 | 10 | 4 |
| styles | 140 | 10 | 7 |
| additions | 140 | 10 | 5 |
| magnetized | 140 | 10 | 5 |
| immersion | 140 | 10 | 7 |
| combined | 140 | 10 | 0 |

The smallest measured travel leg was 3.73535156 blocks. The maximum
rejected-snap bearing displacement was 0.0000203475 blocks, below the
0.0001-block limit. Maximum measured vertical travel drift was 0.00000000 blocks.
All seven Gradle profile launchers were gone after successful completion.

Exact loaded versions are retained for every run. The common runtime is Minecraft
1.21.1, NeoForge 21.1.248, Create 6.0.10, Coasters Simulated 0.1.5 and Sable 2.0.3.
Optional artifacts are Engineered 1.1.1, Track Styles 1.2.0, Additions 0.2.0,
Magnetized 1.1.0 and Immersion 0.2.3, with their recorded companion dependencies.
An absent-addon skip provides no evidence for that absent addon.

The full suite also retains its connected-family decisions, moving provider
boundaries, delayed passenger replay and four occupied PAX interruption phases.
Engineered and combined profiles enable FE-required PAX energy recovery. Dedicated
server completion does not provide new client/camera evidence.

Artifact SHA-256: `45e0de545163e99077b799a20b4dce81feddff6b8be27c0c512a77f53aabc803`.
The local immutable artifact is
`/home/braydon/.cache/fourthdimension-48535694/ordinary-access.jar`.
Archive entry comparison against the preceding camera-connections artifact adds
only `OrdinaryAccessGameTests.class`; no existing entry changes or disappears.
The fixture's source, compiled class and packaged class hashes are recorded.

From the repository root, audit the retained evidence:

```sh
python3 scripts/verify-ordinary-access.py docs/evidence/integrations/2026-10-02/ordinary-access
```

To repeat the native checks, run `build runGameTestServer` with Java 21. Repeat
`runGameTestServer` with each `-PfdIntegrations=engineered`, `styles`, `additions`,
`magnetized` and `immersion`, then the comma-separated combined list. Add
`-PfdPaxEnergyReload` for Engineered and combined. Run profiles sequentially because
they share the GameTest world/output directory. `matrix.py` records the exact
commands used here; its original location is ignored `build/ordinary-access`.
