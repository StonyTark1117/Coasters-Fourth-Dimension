# FD401 normal STOP and cold repeat

This packaged NeoForge client continuation began from the [ordinary item-placed Arrow car](../fd401-bare-item-placement/README.md) on the original closed route. The initial run used `main` at `e6292a9`; concurrent render-diagnostics commits landed before or during the cold run. Both runs used `-PfdPackaged -PfdValidation -PfdIntegrations=engineered -PfdFeNetwork` with `requireEnergy = true`. The retained logs and world are the behavior record; exact runtime-JAR correspondence across the concurrent build is not established here. Both runs saved all dimensions and exited with Gradle `BUILD SUCCESSFUL`.

| Artifact | SHA-256 |
| --- | --- |
| [Initial client log](client.log) | `b6c35e5cda58ad24856b8abf232e84a12efa203a8d3adb6bcde27e8d9d9162be` |
| [Cold-repeat client log](cold-client.log) | `51b3d0474ccb0f62791911ad704904b8e00dd1017863bb7df093ad2b541c8218` |
| [Saved world after cold repeat](world.tar.gz) | `5393b41d0cb4f478ddd5c51675c1be81fb2f31730308ce90615a47a26fe7c447` |
| [Controller program](program.png) | `54887b1b1386ec5fb95b5864134c3120d1da2b3e502709291221c3215b6849b8` |
| [First station capture](captured.png) | `295c4beb9ffac4f8e99d6cfd07711c601b5482f37307dc4e04e4f098372b70a5` |
| [Cold-repeat station capture](cold-captured.png) | `5cdc82edd7effb842822537dcb3f8fe97ba8c1088c4af1655676d1fb3a3dceeb` |
| [Separate unready-interlock client log](unready-client.log) | `e47c31a977cb4a0823adfa1bdca16744691211d4417511e576199345a06b81ab` |
| [Unready-interlock screenshot](unready-reject.png) | `93d01819b97530a8c1db040ef7ee621529cba856b2dc4da68f64c607e3f67bef` |

The player fitted the motor to the first straight through its item UI (`T0=0`, `T1=0.5555555520669377`), linked a normally placed controller, and programmed `Launch 20 → Stop at 12 → End` in its GUI. A fixture-built, noncreative three-water-wheel Crafts & Additions alternator supplied FE adjacent to the motor anchor: kinetic `Speed=8`, `Capacity=768`, `Stress=512`, alternator `AddedStress=64`. The player boarded the original car by empty-hand seat click and closed restraints with the in-ride Rotation Tool. Station rear redstone released it and a controller redstone pulse launched it. The controller recorded `PassReached=19.386871/20`, `PassShort=1`, and no starvation. The occupied car completed the route and held at the programmed STOP near rider `z=-617.501507`, inside station range, with speed about `0.014637`. Station rear redstone then captured it; the editor showed `Ready | Restraints closed` before manual capture. The player opened restraints and dismounted, leaving `assignments=0`.

After normal shutdown and cold reopen, native availability reported one chassis and two carriers with zero assignments. The motor span, controller link and STOP program, alternator kinetic network, and stored FE remained. The player boarded, closed restraints, released station redstone, pulsed the controller, and completed another occupied circuit. This pass recorded `PassReached=19.397947/20`, `PassShort=1`, no starvation, and the same STOP hold position. Station redstone captured it again; the [editor](cold-captured.png) showed closed restraints and ready state before opening and dismounting. Final availability returned to zero assignments.

A separate client run on an earlier checkpoint of the same item-placed car tested the unready interlock. With restraints open, the fixture raised and released the station rear redstone input, then the player clicked the station. The [raw log](unready-client.log) reports `All carriers must be captured and restraints closed before dispatch` at 03:39:24; the [screenshot](unready-reject.png) shows the rejection. This is a sibling run, not part of the saved STOP12 world.

The raw logs retain fixture redstone commands and block-state queries. This establishes the STOP hold and station capture with the normally placed car, including a cold repeat. It does not establish attainment of the exact 20 blocks/s target or a wired FE path; the [later wired run](../fd401-engineered-wired-waterwheel/README.md) addresses the latter separately.
