# Optional integration development

FD401 is in progress. The addon remains standalone: no optional JAR is required or
bundled, and no external functional track is certified yet. FD801 owns a future
Track Styles Arrow 4D mechanical adapter; a decorative model or equal main-rail
gauge cannot satisfy that gate.

## Version and API boundaries

`/fd integrations` reports installed metadata without loading addon API classes.
The audited inspection profiles are Track Styles **1.2.0**
(`coasterssimulatedextratypes`) and Engineered **1.1.1** (`coastersengineered`).
"INSPECTED_RELEASE" means its released signatures were inspected, not that the
complete gameplay/graphics combination is certified. An absent or different
release leaves optional access disabled. The base stack pins remain unchanged.

The client-only Track Styles catalog reads the installed release's public
`MyTrackModels.STYLES` map and the `centerBeam()` / `tie()` component accessors,
after model baking. It refreshes after each resource reload, discards partial
results on API failure, and retains the original appearance. Neither its classes
nor client rendering classes are referenced by common discovery. The catalog
provides references to separately installed models; this addon includes no copied
Track Styles resources or code.

The rendering ownership contract gives every component one owner. A cosmetic
provider can reserve spine and ties only. Original running rails, position-driven
control geometry, and magnetic equipment remain ours. Appearance identifiers do
not participate in the functional track/vehicle contract, and cannot change
family eligibility or carrier profiles. The Rotation Tool's section editor now
opens **Track appearance**, with Original and the two inspected Arrow spine
choices, plus an optional `#RRGGBB` spine/tie color. Selection uses the same
permission, distance, revision, and occupied-section guards as profile editing.
Only separately installed Track Styles 1.2.0 enables its choices.

Appearance intent is saved separately in schema 5. Earlier schema 1–4 rails
default to Original; missing provider IDs and tint are retained while the original
family geometry is shown. Reinstalling the same provider restores the selection.
Network protocol 5 requires matching client and server builds. This checkpoint
does not certify all exposed development choices: the single-spine Arrow straight
has visual and provider-lifecycle evidence; double spine, curves/banking,
clearance and complete styled rides remain to be validated.

## Reproducible development profiles

Optional development dependencies are selected explicitly:

```sh
./gradlew runClient -PfdPackaged -PfdIntegrations=styles
./gradlew runClient -PfdPackaged -PfdIntegrations=engineered
./gradlew runClient -PfdPackaged -PfdIntegrations=styles,engineered
```

The same profiles apply to development dedicated-server and GameTest runs. Unknown
profile names fail configuration rather than resolving arbitrary dependencies.
The default profile installs neither addon. Do not treat a successful boot or
catalog lookup as evidence of correct propulsion, shader rendering or restraint
interlocks; those have separate acceptance scenarios.

Exact selected releases:

| Addon | Development coordinate | SHA-256 |
| --- | --- | --- |
| Track Styles 1.2.0 | `curse.maven:coasters-simulated-track-styles-1642719:9020601` | `42535a83307f6898b33c13ca201533bb9e337418fd7c73039c867fc80bfbcb38` |
| Engineered 1.1.1 | `curse.maven:coasters-engineered-1643631:8863559` | `eea19ed769ed573727b4e15184c229f66aff85f20667464650724e1b2d657f5d` |

The Track Styles [project page](https://www.curseforge.com/minecraft/mc-mods/coasters-simulated-track-styles)
still lists Arrow 4D as future work and describes Flywheel/shader requirements.
It labels the project MIT, while the inspected 1.2.0 JAR declares All Rights
Reserved. The independent optional catalog does not redistribute that JAR, code
or assets. No resolved license for copying or bundling its content is claimed.
The audit retains both observations in `research/dependency-audit.json`.

## Remaining FD401 acceptance

Complete style geometry and clearance checks and record supported styles/render
modes and styled ride behavior. Resource reload and a real removal/reinstallation
cycle now pass for one single-spine controlled Arrow section. Evaluate Engineered operating spans on real 4D
vehicles, including PAX phase ownership and boarding signals. Publish supported
combinations only after the runtime matrix; inspection profiles alone are not
advertised gameplay support.

The [partial integration evidence](evidence/0.4/README.md) retains the earlier
catalog-only checkpoint and the newer appearance transaction, model-frame and
provider-lifecycle checks. Neither checkpoint closes FD401 or the 0.4 gate.

## Released Arrow model finding

The 1.2.0 public catalog initialization includes `arrow_4d` and
`arrow_4d_dbl_spine`, and the JAR contains their spine, tie and rail resources,
despite the page's future-feature label. Both rail models include extra rails at
fixed offsets. This changes the appearance investigation: the released models
are available to inspect. It does not prove the functional profile interface or
variable control-rail movement needed by FD801.

The independent adapter consumes decorative components only and retains our
profile-driven control rails. Native model origins differ: the inspected spines
are centered on X/Z, while tie bounds include a local X offset. They require
separate transforms, not a shared guessed translation. Raw extrema and the
source JAR hash are recorded in `research/track-styles-1.2.0-model-bounds.json`;
reproduce them with `python scripts/inspect-track-style-models.py <installed-jar>`.
These aggregate measurements are not copied models and are not a clearance
certificate. Separate transforms center the spine and ties; a positive-handed
model frame preserves textured-model winding even though the native travel basis
has different handedness. Resource baking invalidates meshes by catalog
generation. Original running/control rails and mechanical supports remain ours;
external rail models are never rendered. Native curved/banked alignment and
clearance still need evidence before these development choices are advertised.
