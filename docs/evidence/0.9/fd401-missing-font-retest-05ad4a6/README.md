# FD401 transient-font removal retest on `05ad4a6`

## Scope and result

This is one local, bounded NeoForge 1.21.1 client run of source commit
`05ad4a6ee466feb681e2174c434ce607686732ba`. It tested loading a
custom font from a file resource pack, using that font in chat, reloading
resources, removing the font JSON, and rendering previously styled chat,
inventory, pause, and title screens. The client saved and quit normally;
the Gradle process exited `0` with `BUILD SUCCESSFUL in 7m 10s`.

The custom font disappeared from the loaded set (`newFontSets=8` to `7`)
after its JSON was renamed. Previously styled `AAAA` chat text then displayed
missing-glyph boxes rather than crashing. No `Image is not allocated`,
`FD_FONT_IMAGE_UNALLOCATED`, or `FD_FONT_CLOSED_SET_LOOKUP` appeared in the
client log. One stale **default** font-set redirect occurred at generation 2
with `missingFallback=false`.

**Limit:** The natural run did not produce a stale lookup for the removed
custom font and did not enter the new `missingFallback=true` branch. It
therefore supports normal removal behavior and absence of this crash in this
sample, but does not directly prove that branch.

## Inputs and execution

- Built the exact source revision locally with `./gradlew build --offline --no-daemon --console=plain` (pass).
- Packaged mod JAR: `coasters-fourth-dimension-0.9.0-dev.27.jar`, SHA-256 `92a450ffbb12f9c5edf18bc8241bb8cf9f84c9b755beaff0f6e6f1a0c47c02ac`.
- Other runtime JAR SHA-256 values are in `runtime-jars.sha256`.
- Private historical schema-3 world: extracted from tracked `docs/evidence/0.9/fixtures/schema3-native-world.tar.gz`, SHA-256 `3f4f5d820deb9ffde91e576df7a582bd0234c8bfd2ddff2e3fe70e4dc0dbfac3`; copied to `FD401FontMissingHistorical`. The archive's source was not modified. The run's world file inventories are `preworld.sha256` (48 files) and `postworld.sha256` (56 files).
- `resourcepack/` is the initial file pack. It defines `fd_font_probe:transient` with a `space` provider for `A`; `resourcepack-validation.txt` records 0 warnings. `post-resourcepack/` preserves the final state with `transient.json.disabled`. `selected-packs.txt` records the run's resource-pack selection.
- Xvfb display `:101`, 1280×720×24. Local command:

```sh
timeout --signal=INT --kill-after=10s 600s \
  env DISPLAY=:101 ALSOFT_DRIVERS=null JAVA_HOME=/usr/lib/jvm/java-21-openjdk JAVA_TOOL_OPTIONS=-Xmx4G \
  ./gradlew runClient --offline --no-daemon --console=plain \
  -PfdPackaged -PfdIntegrations=styles,engineered \
  -PfdWorld=FD401FontMissingHistorical -PfdFontValidation
```

The world was survival with commands disabled. Commands were enabled through
Open to LAN in this private copy to send
`/tellraw @s {"text":"AAAA","font":"fd_font_probe:transient"}`.
The `space` provider produces blank visible glyphs while the font is present.

## Observed sequence

| Step | Font generation and count | Evidence |
| --- | --- | --- |
| Pack loaded and custom tellraw accepted | 1, 8 | `latest.log`, `f3-loaded-pack.png`, `custom-font-rendered.png` |
| Three F3+T reloads with font present; inventory and pause rendered | 2–4, 8 | `latest.log`, `inventory-three-reloads.png`, `pause-three-reloads.png` |
| Font JSON renamed, F3+T, prior styled chat reopened | 5, 7 | `latest.log`, `chat-after-removal.png` |
| Font JSON restored, F3+T, custom tellraw sent again | 6, 8 | `latest.log`, `chat-open-before-removal.png` |
| Font JSON renamed again, chat closed, F3+T, prior styled chat reopened | 7, 7 | `latest.log`, `chat-after-open-reload.png` |
| Pause, Save and Quit, title, Quit Game | — | `pause-after-removal.png`, `title-after-save.png`, `gradle-console.log`, `exit-code.txt` |

F3+T issued while the chat input was open did not start a reload, so chat was
closed before the final reload. `gradle-console.log` and `latest.log` are
unabridged run logs. `evidence.sha256` hashes every evidence file except
itself.
