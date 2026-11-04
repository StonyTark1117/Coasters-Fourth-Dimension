# FD401 current-artifact interactive reload checkpoint

**Result:** Three separate in-world F3+T reloads completed on the packaged
combined Track Styles and Engineered client. Chat, inventory, and pause-menu
text rendered after each reload; the world saved and the client exited normally.
The opt-in trace recorded a stale default FontSet redirect after reloads 1
and 2. This is a bounded regression of the previously reproduced freed-image
path, not complete FD401 acceptance or a frequency estimate.

## Provenance

- Source code commit `a6e18c906a8e1f9f33880030dccd7b0be1b53b16`;
  packaged `coasters-fourth-dimension-0.9.0-dev.27.jar` SHA-256
  `34c2d6ce14fecaf9941303ff950ec9f8ef586a61456669a0d404b0be43672db1`.
  Minecraft 1.21.1 / NeoForge 21.1.248 with
  `-PfdPackaged -PfdIntegrations=styles,engineered -PfdFontValidation`.
- Private survival-world copy `FD401FontCurrent` from the prior
  `FD403ArrowPoweredRetry` save, launched with `-PfdWorld=FD401FontCurrent`.
  The [saved copy](world.tar.zst) is included. The isolated display was
  Xvfb `:99`, 854×480, with a 4 GiB client heap cap. No other Minecraft
  client ran on this display.
- The original interactive [failure](../../0.9/fd401-interactive-repro-2026-11-02/README.md)
  on an older artifact logged a closed `minecraft:default` FontSet lookup,
  then `Image is not allocated` during pause-menu rendering. This artifact
  includes the closed-set lookup redirect added after that reproduction.

## Replay

The client entered the world normally ([initial](initial.png)). `xdotool`
focused the actual Minecraft window, held F3, pressed T, and released F3.
The log records FontManager generations 2, 3, and 4 as distinct completed
interactive reloads; each started with seven old FontSets and ended with seven
new ones. After each, `/` chat, survival inventory, and pause menu opened.
The selected post-generation-4 images show readable [chat](chat-after-three.png),
[inventory](inventory-after-three.png), and [pause menu](pause-after-three.png).

After generation 2, the trace logged a `minecraft:default` closed-set redirect
to the live set. Generation 3 logged another redirect from the older set and
one from the just-closed generation-2 set. No `FD_FONT_IMAGE_UNALLOCATED`,
uncaught exception or OOM appeared. The player used normal Save and Quit
([title](title-after-save.png)), then Quit Game; Gradle reported
`BUILD SUCCESSFUL in 2m 30s`.

This replay verifies the redirect on the previously failing type of GUI path.
It does not exercise an occupied styled ride or prove the intermittent bug
cannot recur. The generation-3 worker redirect occurred while the apply
window was active, so concurrency remains a follow-up investigation rather
than an inferred guarantee. The [compressed log](client.log.zst), screenshots,
and saved world are checked by [verify.py](verify.py) and `SHA256SUMS`.
