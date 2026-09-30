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
family eligibility or carrier profiles. The catalog is preparation: selecting and
rendering a geometry-certified style is still unfinished.

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

Validate style geometry and clearance, wire component rendering and selection,
exercise resource reload/provider removal and original fallback, and record actual
supported styles/render modes. Evaluate Engineered operating spans on real 4D
vehicles, including PAX phase ownership and boarding signals. Publish supported
combinations only after the runtime matrix; inspection profiles alone are not
advertised gameplay support.

The [partial infrastructure evidence](evidence/0.4/README.md) records 19 native and 19 unit passing checks, public catalog discovery of 55 installed entries, and original Arrow rendering. Resource reload, style rendering and clean optional-client exit/removal acceptance remain pending.
