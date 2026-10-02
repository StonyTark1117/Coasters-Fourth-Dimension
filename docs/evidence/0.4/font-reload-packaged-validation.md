# Packaged controlled font reload validation

**Date:** 2026-10-01

**Profile:** Track Styles 1.2.0 + Coasters: Engineered 1.1.1, packaged Fourth Dimension JAR

**Artifact:** `coasters-fourth-dimension-0.9.0-dev.27.jar`
**SHA-256:** `cddbf61bbc758f4423b1eb37b82376df122b5e309fb0ddf308e9e63f7aeec121`

## Procedure

1. Build the artifact with `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build --no-daemon`.
2. Start an isolated Xvfb client with `runClient -PfdPackaged -PfdIntegrations=styles,engineered -PfdFontValidation -PfdFontValidationReloads=16`.
3. Wait for the sixteen sequential opt-in resource reloads to complete, then inspect `packaged-client/logs/latest.log`.
4. Deliberately interrupt the isolated client. This is not a clean-exit check.

## Result

The packaged artifact recorded initial `FontManager.apply` generation 1 with
zero old font sets, then issued and completed sixteen sequential
`FD_FONT_RELOAD_REQUEST` operations. Each reload began with seven old font sets,
logged one close event for each, and completed with seven newly installed sets.
The final result was `FD_FONT_RELOAD_COMPLETE request=16 completed=16 target=16
generation=17`. The log contains 16 requests, 16 completions, 17 apply starts,
and 17 apply ends. Track Styles registered its expected 55-entry catalog after
each apply (17 catalog lines total). No
`FD_FONT_CLOSED_SET_LOOKUP`, `FD_FONT_IMAGE_UNALLOCATED`, or `Image is not
allocated` line occurred before the deliberate interruption.

## Limit

This proves sixteen controlled resource reloads of the packaged artifact under
the same Track Styles and Engineered versions present in the original dev.8
crash report. It does not reproduce the intermittent post-reload GUI failure,
validate an interactive F3+T path, or establish a production workaround. FD401,
FD902, and FD904 remain open until an actionable reproduction or broader
packaged lifecycle acceptance exists.
