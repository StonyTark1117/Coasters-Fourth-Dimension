# Default Arrow decoration and native wheel clearance

This dev.8 checkpoint progresses FD401. It is not the complete vehicle-clearance
gate, the 0.4 release, or the 0.9 release candidate.

## Finding and implementation

The previous banked decoration/control-rail audit explicitly excluded wheels.
Checking the separately installed, pinned Coasters 0.1.5 wheel model now exposes
an uncertified zero-distance intersection of both old Arrow tie placements with
the lower wheel's full-spin swept envelope. This is an envelope conflict, not
an exact physical wheel-phase clipping or physics-contact certificate.

The first proposed 0.4-block downward adjustment still entered the lower shaft
envelope. The implemented adapter lowers both spine and ties by **0.6 blocks in
local up**, and scales ties to **85% width** about their X=0.5/Z=0.25 model origin.
Original running-rail crossbars and diagonal mounts now remain present with
external decoration. A native lower crossbrace connects both installed spine
styles to the mounts, and an end bracket bridges the height step where a styled
section meets Original appearance. The first GUI inspection exposed that step;
`wheel-placement-gui-initial-*.png` retain those diagnostic views before adding
the end bracket. Contact rails and control mechanisms keep their original
geometry and ownership. Family eligibility, passenger behavior, save schema 5
and protocol 5 are unchanged.

## Independent geometry checks

`scripts/audit-arrow-wheel-clearance.py <base-jar> <styles-jar>` verifies both
artifact hashes and reads their resources only in memory. No provider vertices,
faces or textures are saved or distributed. The native wheel model's ten
cuboids are enclosed in full-spin cylinders; the script verifies its nonzero
model rotations preserve the audited X spin axis. It uses the inspected native
placement: path anchor +0.3125Y, renderer compensation +0.1875Y, axle +0.1Y and
lower row -0.55Y, with no world mesh scaling. Front/rear axes are covered by
continuous longitudinal extrusion.

Projecting each cylinder and each decoration triangle onto local XY yields a
conservative lower bound over **every wheel phase and longitudinal position on
a level straight**. Production adapter and support constants are read from our
Java sources; reports include their source hashes. The renderer's octagonal
support tubes are enclosed in their XY hulls. Control-support hulls include the
entire supported -4..+2-turn stroke. Synthetic intersection, containment,
degenerate-triangle and separation cases check the distance method. The same
run must also reproduce the old pinned ties' zero-distance failure.

Both new tie placements have a minimum conservative gap of **0.11752774 blocks**
to the wheel envelopes. The single spine's bound is **1.03816162 blocks**, and
the double spine's is **0.89885227 blocks**. Native support checks retain at
least **0.01065272 blocks**, above the required **0.01-block** margin.
These are lower bounds, not exact
closest-point distances.

The existing independent native-bank audit now uses the production placement
by default. Its recorded-circuit control-rail check still covers continuous
stroke and square-tube segments; it does not qualify banked wheel motion. Run
historical dev.6/dev.7 reproductions with `--placement legacy`.

## Acceptance records and remaining work

All four native profiles pass their 25 required tests; 25 unit tests pass with
no failures, errors or skips. Both installed-style profiles pass the wheel and
banked-control geometry audits. Native results, artifact hash, exported native
frames and aggregate geometry reports use the `wheel-placement-*` filenames in this
directory. CI repeats both geometry checks for Styles and combined profiles.
The packaged final artifact rendered the single spine, double spine and native
end bracket; `wheel-placement-gui-{single,double,end}.png` record those views.
The runtime then performed a real F3+T reload at 23:57:23 MST (2026-09-30) and rediscovered
55 provider entries at 23:57:25. Opening chat afterwards crashed at 23:58:12
while `BitmapProvider` tried to upload a freed native image. The selected log
and complete crash report are preserved as `wheel-placement-gui-failure-log.txt`
and `wheel-placement-gui-reload-crash.txt`. The client exited with code 255 and
Gradle failed; no post-reload ride or clean GUI exit is claimed. Earlier dev.6
screenshots show the earlier placement.

[Create issue #9422](https://github.com/Creators-of-Create/Create/issues/9422)
reports a similar freed-bitmap-provider failure after resource reloads on
Minecraft 1.21.1, with a different mod combination. That is a lead for the
investigation, not proof of this crash's cause. Reproduction without this addon,
font/resource lifecycle inspection and a fresh complete post-reload ride remain
required before GUI reload acceptance can be closed.

The wheel check excludes curves and banking, chassis, carriers, rider poses,
linked-car sweeps, custom resource meshes and graphics backends. Those checks
remain required for FD401. The +720° styling guard remains while wider profile
and whole-vehicle validation is unfinished. This checkpoint cannot be used to
claim those broader requirements complete.

Push run 36828838808 and PR run 36828844086 both completed successfully for
revision `1006d020959a2972a4843f1389a8fd711c232607`, with all four profiles green.
The compact job records are `wheel-placement-ci-{push,pr}.json`. These automated
checks do not close the packaged GUI reload failure. Subsequent
[font investigation](font-reload-investigation.md) records control and diagnostic
observations separately from production acceptance.
