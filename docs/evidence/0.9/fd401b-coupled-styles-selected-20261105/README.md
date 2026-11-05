# FD401B selected-JAR coupled styled Arrow ride

**Result:** two bounded occupied cycles in a saved Styles-only two-car Arrow world on the selected JAR. The second cycle records current-runtime motion, rotation and render diagnostics through the banked single-spine and flat double-spine full-turn sections, followed by station capture and normal unload.

## Input and runtime

- Qualified source `ec2fcda`; runtime JAR SHA-256 `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`, equal to `docs/evidence/qualification/2026-11-05-ec2fcda/qualification.jar`.
- Input world is the archived, normally linked two-car `FD401StylesController` world from [`fd401-styles-coupled-visible-2c5d9a7`](../fd401-styles-coupled-visible-2c5d9a7/README.md). The input's styles were assigned by its fixture, not by the normal editor in this run. The separate [selected-JAR UI transition](../fd401b-style-ui-transition-selected-20261105/README.md) tests the editor on another full-turn section.
- Java 21.0.13; host-private Xvfb `:296`; direct game JVM `-Xmx1536M` under a confirmed 6 GiB systemd memory cap. `run-client.sh`, classpath, VM arguments, program arguments, and `options.txt` retain the launch. Render distance was 4, simulation distance 6. The diagnostic lap logged `graphicsMode=fancy`, `flywheelBackend=flywheel:indirect`, and `flywheelEnabled=true`.
- The saved station signal at `(-526,210,-179)` was removed to start each lap and restored while the train was away so it could capture on return. This scratch-world redstone operation is disclosed. No track or appearance edit was made in this run.

## Observations

For each lap, the player targeted a native `fourthdimension:arrow_seat`, boarded by empty-hand right click, used the Rotation Tool UI to close restraints, and remained assigned during motion. The first lap's UI returned `Ready | Restraints closed`; the player opened restraints and Shift-dismounted. The second diagnostic lap repeated normal boarding and unload. `FD_BOARDING_ASSIGN` identifies the rider on carrier `3bc96087…`, and the client logged that carrier's occupant and `riding=true`. After the second unload, `/data get entity Dev RootVehicle` returned no matching element.

The second lap's read-only `/fd validation motion Dev` queries show the assigned chassis leaving with `capture=false` at about 2 blocks/s, passing `(-487.5,211.1,-168.7)` with `capture=false`, and returning near `(-519.5,211.1,-184.2)` with near-zero velocity and `capture=true`. During the moving window, 24 five-second `FD_CLIENT` samples covered all four carriers. The occupied carrier reached 6.265 radians sampled angle and 6.235 radians native angle. The other car's carriers reached 5.149 sampled and 5.772 native radians. The 24 render samples all had nonzero styled decoration (`inRangeDecorationPieces` at least 1,411,608), and `geometryStages` reached 15,468. See `summary.json` and `secondlap.log.gz` for exact values.

The saved world has 16 rails. `input-rails.json` and `final-rails.json` are exactly equal. Its single-spine section `(-512,210,-148) → (-496,210,-148)` and double-spine section `(-488,210,-156) → (-488,210,-192)` each retain a 2π-radian midpoint rotation profile. All three vehicle records, including the two cars on the same route, finish boarding-ready with restraints open, every carrier captured, and zero occupants. The full final world, both client logs, and selected screenshots are retained and hashed.

## Boundary

This is selected-JAR evidence for an occupied two-car cycle over both styled sections with active render geometry and normal capture/unload. The client camera looked away from the track during the inversions, so the screenshots do not visually isolate both car meshes on those sections. Five-second diagnostics missed the second car's exact 2π peak; the earlier named native test measured it on an older candidate. The run reuses an inherited linked train and fixture-selected styles. It does not by itself prove a fresh normal link, component-by-component render ownership, continuous between-sample clearance, or the full graphics/shader matrix. FD401B remains in progress.
