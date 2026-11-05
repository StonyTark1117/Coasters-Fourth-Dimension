# Revised and PAX survival-client run preparation

Preparation only, inspected 2026-11-02 against release source `a18e87d` and the
equivalent mechanics in evidence worktree `c5a713c`. No game process was launched
for this report. This supplements [FD403 acceptance](FD403-ACCEPTANCE.md), not a
passing record. Obtain the coordinator's runtime handoff and exact candidate
revision before executing it. Keep the new integration expansion paused.

A later [survival-built PAX client cycle](evidence/0.4/survival-client-pax/survival-cycle/acceptance.md)
records the PAX construction, normal Chain links, occupied powered recatch,
unloading and reopen on source `a6e18c9` with a disclosed motor fixture. This
preparation remains a historical plan for the broader FD403 gate.

## What can be driven and observed today

| Operation | Available normal path and observation | Remaining proof |
| --- | --- | --- |
| Recipe discovery and crafting | Purple/green dye unlocks Revised/PAX track; matching track unlocks car and PAX winch. Normal recipe-book/result-slot UI, inventory counts and screenshots. Native tests cover recipes. | Connected survival crafting of these two families; do not infer it from creative demos. |
| Matching track construction | Place native anchors, select both with crafted family track, shape with native tools and rebind through Rotation Tool. Record each cost and curve. | A complete usable route constructed by the player, with exact survival consumption. |
| Car placement | `VehicleItem.useOn` resolves the nearest graph hit within four blocks of the clicked position; it checks interaction permissions and eight-block player reach before factory validation. It consumes one item only after successful creation. | Ordinary clicks on temporary supports near six-block-spaced station positions are a source-supported option, not yet evidence of reproducible four-car UI placement. Verify actual centers and no unwanted curve selection. |
| Wrong-family rejection | Same item path delegates to the family-validating factory. Count cars and items before and after. | Capture the actual rejection and unchanged inventory in the connected client. |
| Revised propulsion | Native chain lift accepts Create rotational power. Coasters 0.1.5's own Ponder text describes Chain selection of span start/end and the shaft on the starting end. | Normal chain installation, powered travel and carrier motion on a complete route. Demo builders call `commitChainLift` directly and do not prove this construction path. |
| PAX assembly | Minecraft Chain selects the first cart, then its neighbor; repeat three times. Normal UI reports `Carts coupled`. | Preserve evidence of all four initially separated cars, each link, then the retained consist. Captured seat carriers alone do not establish that the chassis remain six blocks apart during assembly. |
| Station/winch operation | Normal Rotation Tool controls, empty-hand station binding, sneak-use winch binding, platform operator, Create shaft power. Existing creative PAX evidence proves a bounded occupied cycle and unload. | The same outcome using the survival-crafted and normally assembled train; a successful bind alone is insufficient. |
| Motion and occupancy | `/fd validation motion Dev` reads assigned native chassis position, velocity and all-carrier capture. `/fd validation availability`, `/fd status`, goggles, UI and saved identities supply complementary observations. | Sample multiple live positions while mounted, pair with carrier observations; a saved reservation is not a live mount. Motion query does not work after the player is unassigned. |
| Reopen | Normal save/stop, copy whole stopped world, cold start on identical JAR, reconnect and inspect. | Original identities and usable retained state on the survival fixture, followed by normal exit. |
| Teaching | Fifteen tooltips and nine unique Ponder scenes already exist, with 53 registrations. | Normal UI review on the candidate, including readable controls and correct instructions. Registration counts do not prove this. |

The separate 0.4 acceptance stream owns the currently scheduled PAX runtime.
Reconcile its final record before reserving another run; do not duplicate it or
label its result until its source, crafting scope and shutdown are known.

## Dependencies and outstanding procedure questions

Use the existing standalone pins: Minecraft 1.21.1, Java 21, NeoForge 21.1.248,
Create 6.0.10 and Coasters Simulated 0.1.5 with Sable 2.0.3. Engineered, Track Styles,
CTNB and other optional addons are not required for this FD403 exercise. Do not
activate staged dependencies to supply propulsion or work around a building gap.

No missing production dependency has been identified by this inspection. The
uncertainties are normal route shaping/chain installation and stable manual
four-car placement/coupling. If these fail, retain the exact failure and propose
a narrow mechanics or teaching correction; fixture-authored routes or injected
velocities cannot substitute for the advertised survival workflow.

The prior GUI run used an isolated Xvfb display and `ALSOFT_DRIVERS=null` after an
earlier OpenAL crash. Allocate a free owned display only after runtime handoff;
keep it alive until both clients exit. Never reuse another agent's display.
Use current screenshots/window identity before sending inputs, not coordinates
from another world. Workspace desktop tooling availability must be checked then.

NBT inspection does **not** require `nbtlib`: the existing standard-library
[`inspect-foundation-save.py`](../scripts/inspect-foundation-save.py) reads the
compressed foundation record without editing it. Older `/tmp` observation helpers
contain fixture-specific paths and must not be run unchanged.

## Crafting ledger and route setup

The following ledger is derived from the checked-in recipes, not measured GUI
consumption. It supplies 64 track items per family before intermediate crafting.
It excludes native anchors, ordinary power equipment, scaffolding and tools.
Craft additional batches normally if actual sampled curve costs require them.

| Ingredients supplied | Revised: 64 track, one car, one station, one tool | PAX: 64 track, four cars, one winch, one tool |
| --- | --- | --- |
| Iron ingots | 30 | 44 |
| Copper ingots | 8 | 10 |
| Dye | 4 purple | 4 green |
| Create red seats / native coaster carts / iron bars | 2 / 1 / 1 | 8 / 4 / 4 |
| Redstone / comparator / copper block | 2 / 1 / 1 | 1 / 0 / 0 |
| Create shaft | 0 | 1 |
| Compass / Create brass ingot / stick | 1 / 1 / 1 | 1 / 1 / 1 |
| Track left for route after car/winch crafting | 63 | 59 |

Supply upstream ingredients only, record every grant, then perform discovery and
crafting in survival through normal result slots. A supplied Create creative motor
is a disclosed power fixture, not proof of survival acquisition; use a normally
available source if claiming a fully survival-obtainable power setup. Record RPM,
direction, shafts and any span endpoints whichever source is used.

Build Revised as a complete powered route with sufficient carrier/rider sweep
and an accessible boarding station. Place and shape its track before the car;
rebind after native curve edits. Record measured occupied travel, independent
carrier behavior, capture, restraint opening and ordinary dismount.

For PAX, follow the [route constraints](BUILDING.md#family-walkthroughs): an
unbranched route with two open ends, nearly level station, both spike samples
three blocks inward at least ten blocks above station, at least 25 route blocks
before the lead and at least 38 after it for four cars. The example builder's
compact geometry is a reference only; its direct curve/vehicle creation does not
count as survival construction. Record actual manually shaped curves and costs.
Place four matching cars six blocks apart, verify stationary spacing, and make
the three ordinary Chain links. Keep drive power off during assembly. Bind the
crafted winch within twelve blocks, supply Create shaft rotation, board normally
and close restraints. A second connected player operates the platform winch.
Observe lift, release, outbound, return, capture and final boarding readiness;
then open restraints and dismount normally. Preserve any rolling/coupling failure.

## Launch and evidence procedure

These are commands for the future reserved run, not commands executed by this
preparation. Run them in the selected clean candidate checkout. Record its exact
commit, tree, any diff, and command outputs. Build without optional profiles:

```sh
git rev-parse HEAD
git status --short
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build --no-daemon --console=plain
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew properties --no-daemon --console=plain -q
```

Read `buildDir` from the properties output; colon-containing checkout names use
a relocated `/tmp/fourthdimension-…/build`, not the checkout's `build/`. Hash and
copy the resulting `libs/coasters-fourth-dimension-<version>.jar` before launch.
Record resolved dependency files/hashes and corroborate them with the runtime
mod list. A Gradle dependency report alone is not live-process evidence.

Use a fresh named world and reserved port in that build directory's
`runs/packaged-server`. Preserve any prior world first; never overwrite it.
Record the nonsecret world settings, seed and survival mode, and document every
setup command. Keep credentials out of evidence. Start the server, then prepare
the two client launch scripts outside their 3 GiB scopes after it reports ready:

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runServer -PfdPackaged -PfdValidation --no-daemon --console=plain
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew createClientLaunchScript -PfdPackaged -PfdValidation -PfdConnect=127.0.0.1:<reserved-port> -PfdClientMaxHeap=1536M -PfdClientGameDirectory=/absolute/client-directory --no-daemon --console=plain
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew createObserverLaunchScript -PfdPackaged -PfdValidation -PfdConnect=127.0.0.1:<reserved-port> -PfdClientMaxHeap=1536M -PfdObserverGameDirectory=/absolute/observer-directory --no-daemon --console=plain
```

Use the generated direct Java scripts in separate agent-owned Xvfb sessions,
each with its own 3 GiB Minecraft-only scope as required by `AGENTS.md`.
Verify their Java 21 executable, 1536M maximum heap, game directory, and
classpath before launch. Keep the Gradle launcher and daemon outside those
scopes, and check host/cgroup headroom for both preparation and clients.
Replace the port and directory placeholders before execution. Confirm the logs load the
packaged addon archive. Do not enable automatic carrier fixtures or frozen-start
flags. Validation availability/motion queries are observational; validation
board/dispatch/style commands, `/fd demo`, direct vehicle creation, curve/NBT
writes and velocity injection would change the acceptance scope.

At discovery, crafting, placement, linking, boarding, moving, final capture and
unload checkpoints, capture timestamp/game tick, inventory and UI images, full
logs, live motion/occupancy queries and saved identity snapshots. Use
`save-all flush` for on-disk checkpoints; label them as disk state. Decode the
foundation record with:

```sh
python3 scripts/inspect-foundation-save.py /absolute/world/data/fourthdimension_foundation.dat > /absolute/evidence/foundation.json
```

For the restart checkpoint, save/flush, issue `stop`, and wait on the actual
server process handle until exit. Copy the **entire** stopped world, hash every
file, then restart the same candidate on the working copy and reconnect. Retain
the pre-restart copy unchanged. Repeat normal shutdown and full-world capture
after final inspection; quit both clients normally before ending their display.

The final evidence bundle must include source/JAR identity, dependency inventory,
fixture/grant ledger, ordered observations (including failures), screenshots,
full compressed logs and actual process exit results, pre/post-restart raw save
records plus decoded comparisons, whole-world hashes and retained local paths.
Hash bundle payloads separately from the manifest. A verifier must check the
specific crafting/placement/operation/identity claims, not just checksums. Report
unfinished family operations explicitly; this preparation closes no release gate.
