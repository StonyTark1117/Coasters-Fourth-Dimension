# FD401 isolated artifact identity at `81ee2e6`

The authoritative `main` commit `81ee2e6` was exported with `git archive` to
`/tmp/fd401-candidate-81ee2e6/source`, outside the checkout. The source tar's
SHA-256 was
`6b5e074a067327a4eca8a848aad22f849f4ea68d3a1fb15bbdb076266da278ef`.
The tar is not bundled here because it includes the repository's prior large
evidence files; the commit itself fixes its contents. An offline, isolated
`jar` task used Java 21, `-PfdPackaged`,
`-PfdIntegrations=styles,engineered`, `-PfdFeNetwork`, and
`-PfdBuildDirectory=/tmp/fd401-candidate-81ee2e6/build`. The [build log](jar-build.log)
ends `BUILD SUCCESSFUL`. The resulting [JAR](coasters-fourth-dimension-0.9.0-dev.27.jar)
hash is
`de64a6833fd0a4e5f7bdbc36ad40b89ff396d48941c7e6ae0ae2e52ca6ca3a70`.

That byte-for-byte hash equals the JAR in the combined Create telemetry run
and the Styles render-mode evidence. The Create server log names the shared
output path from which the mod was loaded, and that file had this hash before
and after the run. Concurrent builds mean this is a strong artifact match,
not live-process byte attestation. New candidate runs should use this isolated
output tree to remove that ambiguity.

The [classpath probe](runtime-classpath.init.gradle) resolved the packaged
runtime configuration in the same isolated source tree and hashed its 111
files. The [JSON](runtime-classpath.json) gives names, sizes, and SHA-256 values;
the [probe log](classpath-build.log) ends `BUILD SUCCESSFUL`. This is Gradle's
resolved packaged configuration, not a live JVM classpath dump. Relevant
addon hashes are:

| Dependency | SHA-256 |
| --- | --- |
| Coasters Simulated | `f80b4c3cdf9fa6188804967147761b0373e150a6eadbdc53626e5ac9700b2049` |
| Create | `192c7b0be13f523dcec9a7a2393bbc2614c3c9bd3edb1f7720a97bcd4f0b87a7` |
| Styles | `42535a83307f6898b33c13ca201533bb9e337418fd7c73039c867fc80bfbcb38` |
| Engineered | `eea19ed769ed573727b4e15184c229f66aff85f20667464650724e1b2d657f5d` |
| Crafts & Additions validation dependency | `41876c3780b70365a1848994d146a73423cc19fbe86485885795d9e7d855e7e9` |

The [SHA-256 inventory](SHA256SUMS) covers every retained file except this
README. This checkpoint freezes an audit baseline; it does not transfer older
runtime observations to the new candidate by version string or replace the
remaining client acceptance and release review.
