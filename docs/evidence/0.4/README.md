# Optional-provider infrastructure checkpoint

This is partial FD401 evidence, not the 0.4 or 0.9 release gate.

## Appearance editing and provider lifecycle checkpoint

The newer checkpoint implements independent appearance identity/color intent,
guarded editor action 5, schema/network protocol 5, and separate installed model
placement. `appearance-native.txt` records all 20 required GameTests passing;
`appearance-unit.json` records all 22 standalone unit tests passing, including
model handedness and bounded appearance parsing. The first model-frame unit run
failed because the standalone classpath lacked JOML; the explicit test dependency
fixed that failure before this checkpoint was committed.

The packaged client used the development JAR and exact hash listed in
`appearance-artifacts.sha256` as `client-observed-dev.1`. The final dev.2 build
uses the same implementation with a new version label and is listed separately;
its native/unit checks passed, but this record does not claim a separate dev.2
GUI launch. All optional JARs were separately installed, never bundled.

Normal Rotation Tool / appearance GUI actions selected Arrow single spine with
`#4A90E2` on the clear controlled straight from (-68,123,56) to (-68,123,20).
The section retains `0:0;18:360;36:0`. An earlier edit next to the parked car was
rejected by the occupied-section/approach guard. The first rendering used the
native left-handed basis and displayed inward faces; the positive-handed model
frame corrected it. Only the corrected screenshot is the rendering observation
in `appearance-arrow-straight.png`.

`appearance-resource-reloaded.png` and `appearance-client-events.txt` show a real
F3+T reload and a second successful catalog bake. Normal Save and Quit and Quit
Game completed successfully. The same world folder was then opened without the
Styles development profile: `appearance-provider-removed.png` shows original red
spine/ties. After another real save/exit, reinstalling exactly Styles 1.2.0 restored
the blue appearance automatically (`appearance-provider-restored.png`) and again
saved/exited successfully. These runs do not manufacture appearance state in NBT.

The world is a disposable copy of the previous real schema-4 five-family fixture.
The four JSON files are read-only inspections after real world saves. Run
`python scripts/verify-appearance-lifecycle.py` to reproduce
`appearance-acceptance.txt`: all 35 rail IDs/profiles/mechanisms, eight vehicle
IDs/bindings, twenty carrier definitions/capture/occupancy records, and the chosen
style/tint survive. Only that GUI edit increments one rail revision. Captured
carriers continue tiny native angle/velocity corrections while the world runs;
this is not a frozen-pose comparison or a moving styled ride. Native synthetic
schema-1–4 format tests are separate from this actual schema-4 world migration.

Temporary operator platforms at (-96,122,20), (-98,122,17), and (-66,122,23)
remain in this disposable rendering fixture. Remove them before using it for ride
acceptance. No passengers boarded or dispatch occurred during this checkpoint.
Sable's transient non-existent-sublevel movement messages recur after one reload;
the existing FD902/FD904 investigation remains open. No new crash/fix claim is made.

Double spine, curved/banked alignment, complete clearance and styled rides,
Flywheel/render/shader modes, and Engineered operating acceptance remain pending.
One straight's exterior rendering cannot certify every style/family combination.

## Earlier catalog-only checkpoint

- `provider-native.txt`: default standalone profile passed all 19 native GameTests;
  no optional API/client class broke dedicated startup or original-family checks.
- `provider-unit.txt`: all 19 JUnit tests passed, including exact-version access
  guards and rejection of cosmetic ownership of mechanical components.
- `provider-artifact.sha256`: the development JAR used for the catalog check.
- `styles-catalog-client.txt`: packaged client, separate Track Styles 1.2.0 JAR,
  `-PfdPackaged -PfdIntegrations=styles -PfdWorld=FD04StylesCatalog`, Java 21 and a
  2-GiB heap cap. The client-only baking listener successfully read 55 public
  decorative model entries. `/fd integrations` reported Styles inspected and
  Engineered absent. A copied five-family world loaded its original native links.
- `styles-installed-original-arrow.png`: original functional Arrow rails and
  original captured vehicle remain visible with Track Styles installed.

Geometry certification and style selection/rendering remain pending. No external
functional Arrow track, operating-addon combination, shader setup, or model
resource reload/removal cycle is certified by this checkpoint. Attempts at the
F3+T shortcut produced no confirmed resource reload in the log. The optional JAR
also reports an unavailable `block/track/empty` model, preserved in the log.

During the subsequent menu/exit attempt, both the isolated Xvfb and client exec
sessions returned exit code 143 and the display socket disappeared. The exact
termination source is unproven. This is not recorded as a clean application exit,
an addon crash diagnosis, or persistence acceptance. The prior separate FD305
five-family run did complete Save and Quit, reopen, and Quit Game successfully.
