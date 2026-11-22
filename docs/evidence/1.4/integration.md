# 1.4 specialty-vehicle integration

Complete 2026-11-22. Primary `main`, following accepted 1.3 baseline
`59c40d11a78c0c7166370c7c8814431ac4f6b304` and the separately accepted authority
bridge at `235255cf21845cda0e4921b5d036425c55a9a66b`. Publication is separate.

## Frozen scope and cumulative acceptance

| Package | Supported configuration and evidence |
| --- | --- |
| [FD2201 Standard / Surf](standing/README.md) | Two distinct one-car, two-place standing configurations on exact matching fixed-world track. Standard is rigid; Surf has a native child carriage with 0–.40-block travel and home capture. Native7 passed 1/1: actual displacement, rider mass, retained interruption, serialization/Resume, home latch and missing-support hold. Both completed normal occupied loops, controls and dry-platform unload. Surf additionally retained the same native parent/child/rider through shaft-loss and cold restart, denied Open while interrupted, required explicit Resume after repair, then physically returned q to home before normal Open/Shift. Both vanilla recipes and Survival paid 32-block track/car placement consumed the expected items and created the correct native configurations. |
| [FD2202 Motorbike](fd2202/README.md) | One original straddle car on matching track. Native5 passed 1/1: travel, restraint/dock/wrong-track guards, drive loss/reversal, support hold and validated saved topology. Occupied circuit, normal unload, same-identity cold restoration, distinct shaft-loss/repair/no auto-start and explicit Resume/return/unload passed. Vanilla recipes and Survival paid track/car placement passed. |
| [FD2204 Flying Bike](flying-bike/README.md) | One original two-place prone car below matching overhead track, two ordered native Chain launches at 8/12 blocks/s and maximum acceleration 3 blocks/s². Native straight-entry passed 2/2, including measured physical acceleration 3.000030517, rider mass, power hold/recovery, complete return, native-only authority and inverted evacuation guards. Normal crafting, car placement, governor linking, occupied two-launch loop and normal dock unload passed. Corrected cold restart retains car/rider with zero actual Chain speed until explicit recovery. Normal banked-coast station binding/Open/Shift unload and prone/camera views passed. |
| FD2203 | Shared integration, exact profiles and support limits reconciled below. Earlier 1.0–1.3 cases remain closed. |

Flying Bike's route, Chains, power and evacuation platforms were disclosed command
setup. Its Survival recipes, car/governor placement, wrench orientation and linking
were normal input. Paid track binding uses the unchanged shared binder proven by
Standing and Motorbike's exact item-consumption runs; no claim is made that the
entire Flying Bike example was player-built.

Each family's specification and player guide defines its exact geometry, track,
coupling and recovery limits. Specialty propulsion is native Chain only; no
Engineered/Magnetized/CTNB/external-provider specialty support is claimed. Flying
Bike's eight-block upright straight approaches precede its two launch spans;
a stranded unpowered coast car requires the documented dry-platform and second
station evacuation. No invisible recovery propulsion or automatic coast return
is provided. Standing/Motorbike recovery also requires actual native drive and
operator capture at the valid dock.

Native assertions establish mechanics; source/geometry audits establish bounds
and connected load paths; real client interaction establishes riding/rendering
and normal controls; separate clean-process runs establish restart. No record is
presented as proving another level. The declared finite four-configuration
addition makes 21 configurations including the accepted original 17.

## Shared changes and affected verification

New family/variant identities feed exact track eligibility, factory and content
registries, persistence, native rider loading, dedicated physics branches,
boarding/restraint/dispatch hooks and scoped pose mixins. Specialty cars reject
generic replacement skins, unsupported links and optional propulsion. Existing
family branches retain their prior behavior. Flying Bike normal dismount uses
its validated exact dock/evacuation exit; permanent native removal retains the
existing emergency relocation path through a scoped removal flag.

Java 21 compile and **63 unit tests** passed in
`fd14-integration-20261122/shared-hooks-test-fresh-jvm.log`. The two specialty
contract unit cases cover exact family/variant/capture and Flying overhead/drive
requirements; they do not claim radius, slope or physical riding coverage.
Native and occupied evidence above supplies the new mechanics. The subsequent
Flying Bike governor correction only reconciles held saved shaft speed after
cold load; its affected real cold restart and explicit recovery passed. New
operator text uses the actual Stop/Dispatch/Resume labels. No unaffected ride
was rerun merely for a changed JAR hash.

The first private clients exposed seven out-of-range model exports. Standing
half-coordinate/2x transforms and Flying Bike seat translation preserve actual
geometry while satisfying vanilla bounds. Corrected occupied views and geometry
audits are retained by the owners. Native shaft collisions at curve joints were
resolved by each family's documented straight drive placement; diagnostic runs
with deferred assertions do not count as acceptance.

## Selected package and reproducibility

Persistent integration root:
`/mnt/NVME4/fourthdimension-runs/fd14-integration-20261122`.
Selected unpublished development artifact `coasters-fourth-dimension-1.1.0.jar`:

`1cbcef5f501afee150c32d07e19b0947a80786c4146dabe7fe4727600f8a292d`

The existing internal version is retained, as at the 1.3 checkpoint; this does
not claim publication as version 1.4. `final-package.log` and
`final-repro-package.log` built with Java 21, offline, fresh single-use daemons,
two workers, separate persistent build directories and separate project caches.
The resulting JARs are byte-identical: 3,199,113 bytes, all **661 compiled classes**
and **729 source resources**, no missing entries or unintended resource-byte
mismatch. The NeoForge metadata template is intentionally expanded. See
[package inventory](package-inventory.json) and [input hashes](source-inputs.json).
The earlier `afb409b3...` checkpoint is retained but superseded by the governor fix.

Concurrent builds need separate project caches as well as build directories;
shared task history previously produced an incomplete JAR. A stale Gradle daemon
also failed to launch Java after an in-place JDK update; a fresh Java 21 process
resolved it without stopping other owners' daemons. Failed diagnostic records
remain under the run roots, outside disposable build output.

## Evidence, runtime ownership and integration boundary

Run roots are `/mnt/NVME4/fourthdimension-runs/fd14-{standing,motorbike,flying-bike}-20261122`.
Each has its own private host Xvfb, game/world directories and loopback ports.
Minecraft clients used 1536M heap and a 6GiB client-only scope; Gradle ran outside.
Worlds, raw logs, input/readback and selected screenshots remain preserved.
All nine Motorbike, Standing and Flying Bike client/server/Xvfb units are
independently verified inactive/dead/MainPID0. All servers saved every dimension.
Flying Bike’s corrected cold/evacuation client used the exact selected final JAR.

The concurrent 1.5 moving-track planning changes are separate work. Preserve their
files and their hunks in ROADMAP/PLAN, plus pre-existing untracked `logs/` and all
historical worktrees. Only this completed 1.4 package is selected for integration.
The separate Launch Systems consumer runtimes belong to their continuing owner;
this package neither stops them nor claims their completion.
