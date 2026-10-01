# Additions linked-consist and fin-mode acceptance

This bounded native scenario extends the [single-car Additions checks](../additions-0.2.0/README.md).
It tests Additions 0.2.0 with its native span scanner, wireless channels and powered
fin physics. The new fixture uses two physically linked Arrow cars, one native pig
passenger on each car's outer carrier seat, and eight total seats. Production
integration behavior is unchanged; this work adds direct acceptance evidence.

The scenario verifies:

- A complete two-car consist reports two cars, eight seats and two occupants. Its
  weight output is 5; a threshold of 6 cannot dispatch it.
- Adding a third uncoupled 4D car makes the span ambiguous. Replacing that car with
  an eligible ordinary native cart also rejects dispatch. In both cases the
  upstream scanner supplies the actual members; telemetry reports the binding
  error and the occupied output remains 15.
- A span containing only the rear car of the linked train cannot dispatch or retain
  a complete-consist readout. Restoring the full span begins a new arrival period.
- A five-second arrival cooldown precedes a three-second continuous readiness
  delay. Maintenance retention interrupts readiness; releasing retention requires
  the complete delay again. Both cars dispatch together, with closed restraints.
- After successful dispatch, the occupied output drops to zero and restraints stay
  at 15. The station does not recapture the train while it remains in the same span.
- One real powered fin drives both linked chassis. BOOST at strength 10 moves both;
  BRAKE at value 10 regulates roughly +3 blocks/s, reversing the span regulates
  roughly -3 blocks/s, and STOP brings both below 0.15 blocks/s while the 4D cars
  remain released, restrained and fault-free. This excludes a stop caused by our
  capture/fault interlocks instead of the fin. Coupling and both passengers survive every mode, followed by normal capture, restraint opening and
  unloading. No synthetic velocity is applied to the train in this scenario.

The first attempt used an 80-block fixture route and correctly failed the existing
placement reach check. The route was corrected to 60 blocks. The second attempt
passed the station sequence; the final fixture adds the linked fin-mode checks.
The earlier failure and station-only result are retained as `attempt1.txt` and
`attempt2.txt` rather than presented as passing final evidence.

Run with Java 21:

```sh
./gradlew build runGameTestServer -PfdIntegrations=additions
./gradlew runGameTestServer -PfdIntegrations=engineered,styles,additions,magnetized,immersion
./gradlew runGameTestServer
```

The added registered test is
`additionsRequiresOneCompleteConsistAndContinuousReadiness`. The base profile
explicitly skips optional fixtures; those skips are not compatibility evidence.
The Additions-only profile includes its required Aeronautics/Interiors dependencies.
The combined profile includes all five pinned addons. Per-run mod lists, result
excerpts, full-log hashes, artifact/source hashes and the evidence verifier specify
exactly what was executed. The retained warning excerpts include the known dedicated-server client-class
mixin messages and startup lag warnings; these runs do not establish warning-free
startup or a performance budget.

Results:

| Profile | Registered results | Actual optional evidence |
| --- | --- | --- |
| Additions only | 25 required tests passed, 2 explicit skips | Both Additions fixtures executed |
| All five addons | 25 required tests passed, no optional skips | Every optional fixture executed |
| Base | 25 required tests passed, 4 explicit skips | Optional-class isolation only |

The build also retains 19 passing unit tests. The base run preceded the final
assertion that rules out a 4D interlock/fault during STOP; that change is confined
to the optional fixture body and does not alter registration or the tested absence
path. The final Additions-only and combined runs include the stronger assertion.
`source-files.sha256` identifies those final sources; `artifact.sha256` identifies
the JAR built from them. Native Gradle tests execute compiled classes. Each full
log's hash and exit status are recorded in `runs.json`; local `/tmp` logs are
transient. Recheck the retained measurements with:

```sh
python3 scripts/verify-additions-consist.py docs/evidence/integrations/2026-09-30/additions-consist
```

This is one straight, fixed-world Arrow train with two occupied seats. It does not
certify every family/occupancy layout, curves/slopes, moving track, speed extremes,
pre-station wiring, clients, restart, chunk interruption or addon removal. Those
remain in the broader FD901–FD904 matrix.
