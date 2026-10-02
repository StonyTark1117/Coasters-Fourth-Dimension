# Packaged controlled font reload validation

**Date:** 2026-10-01

**Profile:** standalone dependencies, packaged Fourth Dimension JAR

**Artifact:** `coasters-fourth-dimension-0.9.0-dev.27.jar`
**SHA-256:** `1d922e3d7cddf692ad1ed674650080e30ece34e9cc2259c29918e686f48dc642`

## Procedure

1. Build the artifact with `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build --no-daemon`.
2. Start an isolated Xvfb client with `runClient -PfdPackaged -PfdFontValidation -PfdFontValidationReload`.
3. Wait for the single opt-in resource reload to complete, then inspect `packaged-client/logs/latest.log`.
4. Deliberately interrupt the isolated client. This is not a clean-exit check.

## Result

The packaged artifact recorded initial `FontManager.apply` generation 1 with
zero old font sets, issued one `FD_FONT_RELOAD_REQUEST`, and completed generation
2 with seven old font sets. The trace recorded one close event for each of those
seven sets and then `FD_FONT_RELOAD_COMPLETE generation=2`. No
`FD_FONT_CLOSED_SET_LOOKUP`, `FD_FONT_IMAGE_UNALLOCATED`, or `Image is not
allocated` line occurred before the deliberate interruption.

## Limit

This proves one controlled resource reload of the packaged artifact under the
pinned standalone dependency set. It does not reproduce the intermittent
post-reload GUI failure, validate an interactive F3+T path, or establish a
production workaround. FD401, FD902, and FD904 remain open until an actionable
reproduction or broader packaged lifecycle acceptance exists.
