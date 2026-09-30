# Optional-provider infrastructure checkpoint

This is partial FD401 evidence, not the 0.4 or 0.9 release gate.

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
