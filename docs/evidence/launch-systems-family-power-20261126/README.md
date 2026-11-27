# External family authority integration on current main

Integrated 2026-11-26 in the primary checkout, branch `main`, starting at
`bde3f380310f5378d12d90402546a18ae78d1452`. Source overlay:
`dc1c83a543256a5ffeab6d7958e9a3656c132f74`; its base authority bridge was already
present through `235255cf`. Only the family extension was applied, without conflicts.

Adds `EXTERNAL_FAMILY_API = 1`, default-false live accessory supply, whole-consist
external availability checks, external direction selection, Flying accessory power,
and S2 external-drive selection. Native/Magnetized paths remain unchanged when no
external owner is selected. Moving retention denial, specialty native-only guards,
MovingRouteContext routing and Flying moving-service restraint guards remain intact.
This does not add Launch Systems moving-track support or complete FD2008.

The existing focused fixture now expects current save schema 9 rather than 8.
The retained [overlay evidence](../launch-systems-family-power-20261122/README.md)
is historical and does not represent execution on this integration.

## Validation

Java launcher and Gradle JVM both 21.0.13.1 (Java 21; full Gradle output retained in
run directory). The focused namespace `fourthdimension_ls004` passed **1/1**:
[raw log](native.log). It checks exclusive ownership, held transfer, stale/replayed
requests, provider exclusion, save/load hold, explicit resume, missing controller,
schema-7 upgrade, live held accessory supply and immediate supply loss.
This is a native authority fixture, not a new occupied ride or cold-process proof.
Launch Systems retains its independently tested pinned overlay artifact; this
integration does not silently replace that consumer baseline.

Command (Java 21 selected in JAVA_HOME and PATH):

```sh
./gradlew --offline --no-daemon --max-workers=2 \
  --project-cache-dir /mnt/NVME4/fourthdimension-runs/fd-ls-family-main-20261126/project-cache \
  -PfdBuildDirectory=/mnt/NVME4/fourthdimension-runs/fd-ls-family-main-20261126/build \
  -PfdGameTestNamespaces=fourthdimension_ls004 runGameTestServer
```

Server saved all dimensions and exited successfully; no owned Java runtime
remained. No client/display was started. Earlier 1.0–1.5 acceptance carries forward
outside this changed external-provider path. No release artifact was rebuilt,
retagged or republished. Existing untracked `logs/` remains preserved.
