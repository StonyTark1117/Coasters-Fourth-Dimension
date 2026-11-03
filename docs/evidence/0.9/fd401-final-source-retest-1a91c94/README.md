# FD401 final-source interactive reload retest

**Result: targeted pass, 2026-11-02.** This run tested the exact source commit
`1a91c94e9a8b13beee940bb032ffda2d491adf83`, after the final stale-font
lookup redirect change. It closes the artifact mismatch in the earlier
`2be219e` retest, whose tested JAR predated the final logging/code change. It
does not complete FD401 rendering or the 0.9 release gate.

## Provenance

- The worktree was clean at `1a91c94` before the build. The unmodified
  `coasters-fourth-dimension-0.9.0-dev.27.jar` built with
  `./gradlew build --offline --no-daemon --console=plain` (`BUILD SUCCESSFUL`).
  Its SHA-256 is
  `8f09251444af16b30c5ae8092e11d07232a0b0dcc0adc6235485e6d128a45911`.
- `runtime-jars.sha256` records the exact Fourth Dimension JAR, NeoForge JAR,
  and nine other loaded dependency JAR hashes. The profile loaded Track Styles
  1.2.0 and Engineered 1.1.1 together.
- The `FD16RecoveryFontFinal` world was a private `cp -a` clone of the inactive
  `FD16Recovery` packaged-client fixture. Its `level.dat` SHA-256 before the
  run matched the source fixture:
  `47169d386bce12a41f1af7bdf68f56319fcf6d7511f0fbb1f755a38b989b872c`.
  `source-world.sha256` and `postrun-world.sha256` list all 53 files in the
  source fixture and used clone at evidence packaging time. The world payload
  is not committed.
- The client ran on isolated `Xvfb :101` with llvmpipe. No other game JVM was
  running at launch; the display was stopped after the client exited.
- Exact launch command, from this worktree:

  ```text
  timeout --signal=INT --kill-after=10s 420s env DISPLAY=:101 ALSOFT_DRIVERS=null JAVA_HOME=/usr/lib/jvm/java-21-openjdk JAVA_TOOL_OPTIONS=-Xmx4G ./gradlew runClient --offline --no-daemon --console=plain -PfdPackaged -PfdIntegrations=styles,engineered -PfdWorld=FD16RecoveryFontFinal -PfdFontValidation
  ```

## Observed sequence

1. The world loaded and F3 displayed the debug overlay
   (`fd401-final-f3.png`), proving window keyboard focus.
2. Three actual F3+T key combinations triggered FontManager generations
   2, 3 and 4 after startup generation 1. Each apply completed with seven
   live FontSets. `fd401-final-chat-help-gen2.png` shows chat text after
   generation 2.
3. After generation 3, the log recorded one
   `FD_FONT_STALE_SET_REDIRECT` for `minecraft:default`, from closed identity
   `1339706494` to live identity `261125477`. The inventory and tooltip
   rendered (`fd401-final-inventory-gen3.png`). The inventory also rendered
   after generation 4 (`fd401-final-inventory-gen4.png`).
4. The pause menu rendered after generation 4
   (`fd401-final-pause-gen4.png`), the same screen category that crashed in
   the preceding reproduction. Save and Quit stopped the integrated server
   and saved all dimensions. The title screen rendered
   (`fd401-final-title.png`); Quit Game then returned Gradle exit **0**.
   The command's 420-second timeout was not reached.

The complete `latest.log` contains no `FD_FONT_IMAGE_UNALLOCATED`,
`FD_FONT_CLOSED_SET_LOOKUP`, or `Image is not allocated` entry. The complete
Gradle console is in `fd401-final-gradle-console.log`; the wrapper's observed
exit is in `fd401-final-exit-code.txt`. `evidence.sha256` verifies those raw
files and all six screenshots.

This proves one clean replay of the previously failing interactive font path
on the exact final-source artifact. Other stale FontSet call paths, graphics
modes, styled occupied rides, repeated reliability, and broader FD401/FD902/
FD904 acceptance remain open.
