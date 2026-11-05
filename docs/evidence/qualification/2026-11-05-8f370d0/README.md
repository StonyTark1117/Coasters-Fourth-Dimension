# FD401A clean source and artifact identity

This record selects committed `main` source
`8f370d09015a7e782f0bd0a050cc311ea1bd0929` as the current FD401
qualification baseline. Its tree was exported with `git archive` to
`/tmp/fd401-a-source-8f370d0` and built on the project development host with
Java 21 and Gradle 9.2.1. The exact command, input hashes and entry hashes
are in [artifact.json](artifact.json); the build output is in
[build.log.gz](build.log.gz). [qualification.jar](qualification.jar) has
SHA-256 `cf4fed9acf41873f0ba7bd3b28f49fe4507db2ae2442cfcae9bddf8cb7a77cb5`.
No client or server was launched with this clean JAR as part of this identity
record.

The previous `dd0a851` baseline differs in six exported inputs: `AGENTS.md`,
`build.gradle`, `CarrierTransformValidation.java`,
`CarrierValidationFixture.java`, `ShuttleRuntime.java` and `Boarding.java`.
Eleven packaged class entries differ. The manifest gives exact paths and
hashes. The last two production classes change PAX boarding, support hold and
reload behavior, so old PAX runtime results are not promoted automatically.
The earlier font, Styles, Engineered, geometry and recovery observations remain
attached to their original artifacts until affected rows are reviewed or
replayed. Intended profiles are base, Styles 1.2.0, Engineered 1.1.1 and
combined; the profile list is not a runtime pass.

The [two-client PAX replay](../../0.9/fd401-pax-two-client-recovery-8f370d0/README.md)
used JAR `253e6d92…` built from the shared checkout. Its only difference from
this clean JAR is an extra uncommitted
`dev/fourthdimension/gametest/ShuttleBoardingHoldGameTests.class`. Every common
entry is byte-identical. The exact tested JAR and world/log evidence remain in
that replay bundle. This comparison supports a bounded production-entry
mapping, not a claim that the runtime JAR itself had clean committed-source
provenance or that the four advertised profiles have passed.

Run `(cd docs/evidence/qualification/2026-11-05-8f370d0 && sha256sum -c SHA256SUMS)`
to verify this retained bundle. Documentation-only commits after `8f370d0`
do not alter the packaged inputs; any later source/resource change requires
another identity review.
