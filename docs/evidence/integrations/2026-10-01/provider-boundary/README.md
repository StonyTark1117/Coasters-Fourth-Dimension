# Moving approaches to unavailable functional providers

The boundary guard previously considered any valid saved next segment with a
matching variant sufficient to continue. It did not check whether that segment's
functional provider was available. The moving regression reproduced the consequence
on Arrow cars in both directions: the chassis passed the junction without setting
a braking fault, while native eligibility refused to bind the unavailable track.
Retaining the old binding alone did not keep the moving vehicle on supported track.

The production change adds matching family and available functional-provider checks
to the next-segment predicate in `BoundaryGuard`. Existing speed-dependent approach
braking, restraint retention and fault handling perform the stop. Geometry loading
and shuttle route ownership retain their existing checks. This change does not
certify arbitrary external functional providers or introduce an alternate PAX dispatch.

## Dynamic fixture

The expanded existing GameTest runs four waves: Arrow, legacy ZacSpin, Free Spin and
revised ZacSpin. Each wave has six separate native lanes, each containing three
connected 40-block curves. The middle segment retains the car's matching functional
contract. The approaching neighbor is either another family, the matching family
with `missing:functional`, or unchanged matching track. Each condition runs forward
and reverse. Normal construction rejects wrong-family junctions, so the fixture
replaces the saved neighbor contract after constructing the real native geometry.
Arrow uses a legacy ZacSpin neighbor for its wrong-family case; each other family
uses an Arrow neighbor. This moving check is not the full Cartesian family matrix;
the separate 200-case stationary connected-family test remains in the suite.

Cars spawn eight blocks from the junction, settle under normal carrier capture,
close restraints and dispatch through the normal server operation. The test supplies
one initial 4 m/s velocity in the selected direction. It then allows native physics
and the production boundary guard to control the approach. It does not continually
force speed, manually capture the denied cars, or restore their missing providers
before the measurement.

At the measurement, each wrong-family or unavailable-provider case must have:

- A stopping flag, closed restraints and a boundary fault.
- A chassis center inside the original 300–340 block segment, with a one-block
  margin at the lower end, and speed below 0.1 m/s.
- Its original track binding, with no binding to the unavailable neighbor.

Matching controls must cross more than one block into the next segment, update their
binding, remain unheld with closed restraints and have no fault. The evidence audit
also requires positive measured speed. Before dispatch, the fixture checks rejection
through native initial-snap and eligibility paths, restores engagement on the original
segment, and confirms an unheld dispatch. Disconnected matching lanes must not steal
binding. Each wave removes its chassis, carriers, saved vehicle and rails before the
next family starts.

There are 24 moving cases per profile: eight wrong-family stops, eight unavailable-
provider stops and eight matching crossings. PAX is excluded from this free-running
fixture: its winch state machine and four occupied interruption regressions remain
in the suite. These are single, empty cars on flat fixed-world track, at the specified
initial speed. Final center/binding/speed measurements do not certify every axle's
swept clearance, higher speeds, slopes, occupied linked trains, addon-removal restarts,
chunk loss or every external material/provider.

## Preserved failures

The pre-fix run logs all six Arrow cases. Both wrong-family controls stop and both
matching controls cross. Both unavailable-provider cars pass the junction without
stopping or faulting, despite retaining their old binding. The test fails exactly
once with `Unusable boundary did not brake ARROW/MISSING_PROVIDER/FORWARD`, exit 1.
The original production source and compiled-class hashes are preserved.

The first fixed run passed the Arrow wave and the legacy rejection cases, but its
matching legacy controls were still moving toward normal rebinding at the original
210-tick measurement. It fails with `Matching boundary did not cross
LEGACY/MATCHING/FORWARD`, exit 1. The log and initial fixture source are retained as
`timing-base-*` and `initial-fixture.java.txt`. Subsequent runs use a 390-tick
measurement, 420-tick waves and a 1700-tick test timeout; no production change or
weakened crossing condition was made to address that fixture timing failure.

## Verification

All seven profiles exit 0, with 33 registered GameTests per profile and 19 passing
JUnit tests. The audit verifies every moving case, the existing linked-support and
200-case connected-family regressions, delayed passenger loading and all four
occupied PAX interruption phases. Engineered and combined enable FE-required PAX
phase recovery. The two earlier failures are retained with exit 1.

| Profile | Wrong-family stops | Missing-provider stops | Matching crossings | Absent-addon skips |
| --- | ---: | ---: | ---: | ---: |
| Base | 8 | 8 | 8 | 6 |
| Engineered | 8 | 8 | 8 | 3 |
| Track Styles | 8 | 8 | 8 | 6 |
| Additions | 8 | 8 | 8 | 4 |
| Magnetized | 8 | 8 | 8 | 5 |
| Immersion | 8 | 8 | 8 | 6 |
| Combined | 8 | 8 | 8 | 0 |

Loaded versions are Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10,
Coasters Simulated 0.1.5 and Sable 2.0.3. Optional profiles select Engineered 1.1.1,
Track Styles 1.2.0, Additions 0.2.0, Magnetized 1.1.0 and Immersion 0.2.3. Complete
loaded lists are retained. Absent-addon skips do not certify an absent addon.
After normalizing timestamps and UUIDs, no new warning/error categories appear
relative to the linked-support checkpoint. Known upstream warnings and the separate
client shutdown issue remain unresolved by this server-side change.

The verified JAR SHA-256 is
`c69fe8608bdb8ea1b866955d5d76362454e97b2d12b6a3fcdc107babc8f56437`.
The local copy is `/home/braydon/.cache/fourthdimension-48535694/provider-boundary.jar`.
An entry comparison against the preceding linked-support artifact shows exactly one
production class changed (`BoundaryGuard`), the expanded `BoundaryGameTests` and
three added test helper classes. No entries were removed. Compiled/JAR hashes match
for the production guard and all four boundary test classes.

Run the evidence audit from the repository root:

```sh
python3 scripts/verify-provider-boundary.py docs/evidence/integrations/2026-10-01/provider-boundary
```

Full local logs remain under ignored `build/provider-boundary`; committed evidence
retains per-case measurements, loaded versions, warnings, exit statuses, full-log
hashes, JUnit XML and artifact/class comparisons. The broader FD901–FD904 gates remain
open.
