# Packaged controlled font reload validation

**Date:** 2026-10-01

**Profile:** Track Styles 1.2.0 + Coasters: Engineered 1.1.1, packaged Fourth Dimension JAR

**Headless artifact:** `coasters-fourth-dimension-0.9.0-dev.27.jar`

**Headless SHA-256:** `cddbf61bbc758f4423b1eb37b82376df122b5e309fb0ddf308e9e63f7aeec121`

**World/chat artifact SHA-256:** `55e82fbae402ad322a5b82e8310c148a0b4b7cfd81f7a7a1255b328851e220dd`

## Procedure

1. Build the artifact with `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build --no-daemon`.
2. Start an isolated Xvfb client with `runClient -PfdPackaged -PfdIntegrations=styles,engineered -PfdFontValidation -PfdFontValidationReloads=16`.
3. Wait for the sixteen sequential opt-in resource reloads to complete, then inspect `packaged-client/logs/latest.log`.
4. Deliberately interrupt the isolated client. This is not a clean-exit check.

For the world-loaded screen path, start the same profile with
`-PfdWorld=FD16Recovery -PfdFontValidationReloads=16 -PfdFontValidationChat`.
The opt-in chat check waits for the level to load, opens `"/"` command chat
after the final reload, and logs after twenty client ticks with that screen open.

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

The world-loaded check completed its sixteen reloads through generation 17, then
logged `FD_FONT_CHAT_OPEN generation=17` and `FD_FONT_CHAT_RENDERED generation=17
ticks=20`. It likewise had no closed-set lookup, unallocated-image trace, or
target bitmap error before deliberate interruption.

## Clean controlled shutdown

On 2026-10-02, the same packaged combined profile ran under Xvfb with the new
validation-only `-PfdFontValidationExit` switch:

```sh
xvfb-run -a timeout 240s env JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  ./gradlew runClient --no-daemon -PfdPackaged \
  -PfdIntegrations=styles,engineered -PfdFontValidation \
  -PfdFontValidationReloads=16 -PfdFontValidationExit
```

The packaged JAR SHA-256 was
`0497ff2847744a33dd890e86b95a8c05e793c73723828c75416a1aea22508713`.
It completed request 16 at generation 17, logged
`FD_FONT_VALIDATION_EXIT reason=reloads-complete completed=16 target=16
generation=17`, then Minecraft logged `Stopping!` and closed all seven current
FontSets plus `minecraft:missing`. Gradle exited successfully in 1.2 minutes;
the timeout was not reached. There was no closed-set lookup, unallocated-image
trace, or `Image is not allocated` error.

The existing `FD16Recovery` world then ran the command-chat variation under the
same clean-exit switch:

```sh
xvfb-run -a timeout 300s env JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  ./gradlew runClient --no-daemon -PfdPackaged \
  -PfdIntegrations=styles,engineered -PfdWorld=FD16Recovery \
  -PfdFontValidation -PfdFontValidationReloads=16 \
  -PfdFontValidationChat -PfdFontValidationExit
```

It logged `FD_FONT_CHAT_OPEN generation=17`, then
`FD_FONT_CHAT_RENDERED generation=17 ticks=20`, followed by
`FD_FONT_VALIDATION_EXIT reason=chat-rendered completed=16 target=16
generation=17`. Minecraft closed the client and its integrated server normally,
saved the `FD16Recovery` dimensions and all current FontSets, and Gradle exited
successfully in 1.05 minutes. The target font traces/errors were again absent.

## Limit

This proves sixteen controlled resource reloads and a normal client shutdown of
the packaged artifact under the same Track Styles and Engineered versions
present in the original dev.8 crash report, plus a sixteen-reload world-loaded
command-chat rendering sample. It does not reproduce the intermittent
post-reload GUI failure, validate an interactive F3+T path, or establish a
production workaround. FD401, FD902, and FD904 remain open until an actionable
reproduction or broader packaged lifecycle acceptance exists.
