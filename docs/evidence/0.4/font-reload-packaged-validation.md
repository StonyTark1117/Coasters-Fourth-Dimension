# Packaged controlled font reload validation

**Date:** 2026-10-01

**Profile:** standalone dependencies, packaged Fourth Dimension JAR

**Artifact:** `coasters-fourth-dimension-0.9.0-dev.27.jar`
**SHA-256:** `f2d59a3e9101fce4e9cb22b4f71d38d3d262375d2ede2ed254a208d3eace29dc`

## Procedure

1. Build the artifact with `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build --no-daemon`.
2. Start an isolated Xvfb client with `runClient -PfdPackaged -PfdFontValidation -PfdFontValidationReloads=3`.
3. Wait for the three sequential opt-in resource reloads to complete, then inspect `packaged-client/logs/latest.log`.
4. Deliberately interrupt the isolated client. This is not a clean-exit check.

## Result

The packaged artifact recorded initial `FontManager.apply` generation 1 with
zero old font sets, then issued and completed three sequential
`FD_FONT_RELOAD_REQUEST` operations. Each reload began with seven old font sets,
logged one close event for each, and completed with seven newly installed sets.
The final result was `FD_FONT_RELOAD_COMPLETE request=3 completed=3 target=3
generation=4`. No
`FD_FONT_CLOSED_SET_LOOKUP`, `FD_FONT_IMAGE_UNALLOCATED`, or `Image is not
allocated` line occurred before the deliberate interruption.

## Limit

This proves three controlled resource reloads of the packaged artifact under the
pinned standalone dependency set. It does not reproduce the intermittent
post-reload GUI failure, validate an interactive F3+T path, or establish a
production workaround. FD401, FD902, and FD904 remain open until an actionable
reproduction or broader packaged lifecycle acceptance exists.
