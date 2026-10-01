# Linked functional-provider support

A moving two-car Arrow fixture reproduced a provider-loss gap: while the cars
straddled a real connected track junction, replacing one car's saved functional
provider with `missing:functional` left the healthy partner eligible for native
snapping and both cars eligible for external propulsion. The unmodified production
code logged `eligible=true filtered=true drive=[true, true]`; the new assertion
failed exactly one required GameTest and Gradle exited 1.

`ConsistRules` now checks each linked partner's valid saved rail, matching family
and variant, and available functional provider. `ExternalDrivePolicy` also requires
the caller's loaded native chassis, its matching support and the native consist
check before evaluating the existing whole-train operating interlocks. This applies
to the policy already used by Engineered, Magnetized and Additions. Ordinary carts
and the prototype retain their existing handling; PAX remains excluded from external
propulsion. This does not change dispatch, which already resolves each car's support.

The shared helper checks the **saved functional contract**, not native geometry,
chunk readiness or physical stopping distance. Those remain separate runtime checks.

## Fixture and limits

`ControlledTrainGameTests.linkedCarsSampleTheirOwnAxlesAndRollback` uses two actual
native chassis coupled six blocks apart. Normal restraint/dispatch operations start
the train. The test harness supplies velocity along two connected 36-block curves;
each car samples its own axle position for its carrier rotation.

The support probe runs only once the leading car is bound to the second segment and
the trailing car remains bound to the first. It does not forge either binding.
For each car in turn, it:

- Confirms both cars initially allow external drive.
- Temporarily replaces that car's rail record with an identical record except for
  the unavailable functional-provider identity, preserving the appearance and magnets.
- Checks that its healthy partner fails both our eligibility hook and the native
  snap filter on the partner's still-resolvable support; checks that external drive
  is denied for **both** cars.
- Restores the original rail record in `finally`, before the next physics tick.
- Confirms drive and consist eligibility return, original bindings and native
  coupling remain, and no fault, capture, maintenance or restraint change masks the result.

The train then completes its connected transition and rollback, retains independent
carrier angles, and uses normal capture. The test explicitly fails if it never reaches
the split-support condition. This extends an existing registered test, keeping the
suite at 33 tests.

This is a bounded policy regression during native movement. It is **not** an actual
addon-removal restart, a prolonged unsupported-track run, a powered external-motor
force test or a new certification of every linked family. The helper's checks for
missing/invalid/wrong-family support are protective conditions; this probe specifically
reproduces an unavailable provider. The broader FD901–FD904 matrix stays open.

## Verification

All seven profiles exit 0 with 33 registered native GameTests per profile and 19
passing JUnit tests. The audit passes both provider-loss probes in every profile,
the existing connected-family matrix and all four occupied PAX interruption phases.
Engineered and combined use FE-required PAX checks. The pre-fix control exits 1 with
exactly the expected required failure.

| Profile | Provider-loss probes | Absent-addon skips |
| --- | ---: | ---: |
| Base | 2 | 6 |
| Engineered | 2 | 3 |
| Track Styles | 2 | 6 |
| Additions | 2 | 4 |
| Magnetized | 2 | 5 |
| Immersion | 2 | 6 |
| Combined | 2 | 0 |

Loaded versions are Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10,
Coasters Simulated 0.1.5 and Sable 2.0.3. Optional profiles select Engineered 1.1.1,
Track Styles 1.2.0, Additions 0.2.0, Magnetized 1.1.0 and Immersion 0.2.3. Complete
loaded lists are retained. Absent-addon skips do not certify an absent addon.
Warnings are preserved; after normalizing timestamps and UUIDs, no new warning/error
categories appear relative to the preceding connected-eligibility checkpoint. This
does not resolve its known upstream warnings or the separate client shutdown issue.

The final JAR SHA-256 is
`91d92faf97057e9d50a3c5eade5ef2d43ae52fc2a30e6b9cedf412b1f5a7771e`.
It differs from the preceding verified JAR in exactly three classes:
`ConsistRules`, `ExternalDrivePolicy` and `ControlledTrainGameTests`. There are no
added or removed entries. The verified local copy is
`/home/braydon/.cache/fourthdimension-48535694/linked-support.jar`.

The evidence collector preserves loaded versions, warnings, marker extracts, exit
statuses, full-log hashes, prior production source/class hashes and the final JAR
entry comparison. Full local logs remain in the ignored `build/linked-support`
directory. Run the audit from the repository root:

```sh
python3 scripts/verify-linked-support.py docs/evidence/integrations/2026-10-01/linked-support
```
