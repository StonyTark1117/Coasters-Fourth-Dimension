# FD401C selected-JAR Engineered-only PAX authority cycle

On 2026-11-05, a Java 21 client ran the selected clean `ec2fcda` JAR
(`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`)
with Engineered 1.1.1 and without Track Styles. The retained packaged
classpath, dependency hashes, VM/program arguments, launch script and logs
identify the inputs. The game JVM used `-Xmx1024M`; the full launch ran in
active `run-p420808-i396662.scope` with `MemoryMax=3G` on owned Xvfb `:258`.
The scope exited with `Result=success`, and the display was then stopped.

The stopped world was copied from
`fd401-engineered-current-pax-authority/final-world.tar.gz`, a normally built
four-car PAX train and winch route saved on the earlier clean JAR. It was
reopened here because the selected JAR changes PAX bind restoration. The
train and route were not constructed anew in this run. The world retained
`requireEnergy=true` and force-loaded route endpoints. Crafts & Additions
remained a disclosed FE fixture dependency.

At start, the winch at `(644,144,73)` was in BOARDING with `shuttle_cycles=3`,
the opposing Engineered controller at `(644,144,67)` had pass zero, and its
motor anchor at `(650,144,65)` held 100,000 FE. F3 showed the physical
`fourthdimension:pax_seat` target. After clearing hotbar slot 9 with a
disclosed fixture command, Dev made one normal empty-hand right click.
`FD_BOARDING_ASSIGN accepted=true` recorded the mount; no boarding helper
was used.

Dev closed restraints in the vehicle UI. A disclosed redstone block then
powered the opposing controller at `(644,144,68)`, and Dev clicked **Dispatch**
in the UI. The winch was sampled in LIFTING and RETURNING, then returned to
BOARDING with `shuttle_cycles=4` and no fault. At the LIFTING and RETURNING
samples, the opposing controller still had pass zero and the anchor still
held 100,000 FE. Availability at return reported four chassis, eight
carriers and one assignment. The brief OUTBOUND phase was not separately
sampled, and no per-car callback trace was captured.

Dev removed the challenge block, opened restraints in the normal UI and
pressed Shift to dismount. The final `RootVehicle` query found no matching
element. The client saved to title and quit normally. Commands, screenshots,
logs and the stopped final world are retained with SHA-256 bundle hashes.

This closes one bounded Engineered-only selected-JAR normal-board, occupied
four-car winch cycle, opposing-controller rejection and normal unload. Fresh
PAX construction, active-phase restarts, OUTBOUND interruption and the full
FD401C family/profile matrix remain open.
