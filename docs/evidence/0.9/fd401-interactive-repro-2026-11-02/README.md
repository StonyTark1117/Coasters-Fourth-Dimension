# FD401 interactive F3+T freed-image reproduction, 2026-11-02

**Result: reproduced the release-blocking crash.** This is one packaged-client
reproduction in the combined Track Styles and Engineered profile. It is not a
fix, a frequency estimate, or acceptance of FD401/FD902/FD904.

## Provenance and launch

- Source commit: `a18e87dc80f60e1a08e51fe11d0a864ea092e152`.
- Built the unmodified `coasters-fourth-dimension-0.9.0-dev.27.jar` locally with
  `./gradlew build --offline --no-daemon --console=plain`. Its SHA-256 is
  `74ae47b70f2e95bcd8152552f255cabba6b55ed942a1f9856fa41fef39ca2467`,
  matching the 0.9 optional-profile matrix artifact. The JAR contains the
  existing opt-in diagnostic trace; no font lifecycle intervention was added.
- Runtime dependencies and exact JAR hashes are in `runtime-jars.sha256`.
  Minecraft 1.21.1 / NeoForge 21.1.248 and the loaded mod list are in the
  crash report. `-PfdIntegrations=styles,engineered` loaded both optional mods.
- World: `FD16RecoveryFont`, a private copy of the inactive `FD16Recovery`
  packaged-client fixture. Source `level.dat` SHA-256 before copying was
  `47169d386bce12a41f1af7bdf68f56319fcf6d7511f0fbb1f755a38b989b872c`.
  The copy was the only world used by this run; it was changed by normal play.
  `source-world.sha256` records every file in the inactive source fixture at
  evidence packaging time, and `postrun-world.sha256` records every file in
  the private world after the crash.
- Isolated display: `Xvfb :101 -screen 0 1280x720x24 -nolisten tcp`. The
  concurrently owned PAX client on `:99` had exited before this run began.
- Client command, from this worktree:

  ```text
  timeout --signal=INT --kill-after=10s 420s env DISPLAY=:101 ALSOFT_DRIVERS=null JAVA_HOME=/usr/lib/jvm/java-21-openjdk JAVA_TOOL_OPTIONS=-Xmx4G ./gradlew runClient --offline --no-daemon --console=plain -PfdPackaged -PfdIntegrations=styles,engineered -PfdWorld=FD16RecoveryFont -PfdFontValidation
  ```

## Observed sequence

1. The world loaded. F3 opened the debug overlay, captured in `fd401-f3.png`;
   this confirms keyboard input reached the actual game window.
2. Holding F3 while pressing T triggered three separate interactive resource
   reloads. The trace completed FontManager apply generations 2, 3, and 4,
   each closing seven old FontSets and creating seven new ones. Generation 1
   was startup.
3. Chat text rendered after a reload (`fd401-postreload-chat5.png`). The
   creative inventory and an item tooltip rendered after generations 3 and 4
   (`fd401-postreload-inventory.png` and
   `fd401-postreload-inventory-gen4.png`).
4. After closing the inventory and opening the pause menu, the render thread
   logged, at `21:13:17.002`, `FD_FONT_CLOSED_SET_LOOKUP generation=4
   font=minecraft:default codepoint=U+0047` followed immediately by
   `FD_FONT_IMAGE_UNALLOCATED generation=4 font=minecraft:default
   codepoint=U+0047 image=1885285796 size=128x128`. The original
   `IllegalStateException: Image is not allocated` followed in
   `BitmapProvider$Glyph$1.upload` while `PauseScreen.render` drew a button.
   The client exited 255 and Gradle reported `:runClient FAILED`; the
   420-second timeout was not reached.

The paired trace proves a closed FontSet lookup and a freed bitmap image at
the point of failure. It does not establish which component retained that
FontSet or why the stale lookup occurred. A code fix and a repeated post-fix
interactive styled ride still remain open.

## Files

- `latest.log`: complete raw client log from this run.
- `crash-2026-11-02_21.13.17-client.txt`: complete raw crash report.
- PNGs listed above: direct captures from the owned `:101` display.
- `runtime-jars.sha256`: source mod JAR and loaded runtime dependency hashes.
- `evidence.sha256`: hashes of the raw log, crash report, and screenshots.
- `source-world.sha256` and `postrun-world.sha256`: relative-path world file
  hashes, 53 files each. World payloads are not part of this bundle.
