# FD401 wired, noncreative water-wheel FE and cold operator repeat

This packaged NeoForge client continuation used the [normally item-placed Arrow car and completed STOP cycle](../fd401-engineered-stop-cold/README.md) on the original route. It ran `-PfdPackaged -PfdValidation -PfdIntegrations=engineered -PfdFeNetwork` with `requireEnergy = true` on `main` at `7ac5e58`. The source dependency is validation-only Create Crafts & Additions 1.6.0; this does not add an advertised integration. Both client sessions saved every dimension and exited with Gradle `BUILD SUCCESSFUL`. The logs name the loaded packaged JAR path, but concurrent builds in the shared build directory prevent an exact runtime-JAR hash claim.

| Artifact | SHA-256 |
| --- | --- |
| [Wire-building client log](client.log) | `782a2931376803ae4aa1d8d1115830d87f9b4a5aeac78557771ac3cbb31283df` |
| [Cold-repeat client log](cold-client.log) | `1c1fd8e0d055701a49ced6bb966e7662a90e90326c186998de8f35cea90e7890` |
| [Saved world after cold repeat](world.tar.gz) | `ccfe38c978c023c0f7aff8672584854ad2498f713182eb7bc0f879968778b805` |
| [Station-captured editor](captured.png) | `9cfdb913d5d1f4b757a99a22a32be2424f1d3b2887da33f6de352ce921775594` |
| [Wire-area view](wire-view.png) | `91f638b5a381e9e4fa55233398545bdae856dccd802691277efb1abf9f6a9094` |

The fixture removed the adjacent alternator and cleared the motor anchor's stored FE. It placed a separate, noncreative three-water-wheel alternator at `(-507,142,-634)` and two Crafts & Additions connectors: source `(-506,142,-634)` and motor-side `(-499,142,-630)`. The player used the copper spool item normally on both connectors. Their block entities recorded reciprocal wire offsets `(7,0,4)` and `(-7,0,-4)`. Before linking, the motor anchor had no stored FE. After linking, its buffer rose to 5,506 FE. The source kinetic network reported `Speed=8`, `Capacity=768`, `Stress=512`, and alternator `AddedStress=64`.

For interruption, the fixture removed the middle wheel and cleared the motor buffer. The alternator network then reported `Capacity=256` against `Stress=512`; the anchor had no energy key. Replacing the wheel restored `Capacity=768`, and the anchor rose to 440 FE with the wire still present. A simulated-time `/tick sprint 10000` charged the anchor to 100,000 FE through the wired path; no command filled it. The first session saved and exited normally.

On cold reopen, native availability reported `chassis=1/1 carriers=2/2 assignments=0`. Both connectors retained reciprocal nodes; the separate alternator retained its kinetic network, the motor span and controller retained their configuration, and the anchor retained 100,000 FE. A fixture command removed only the anchor's stored FE. It rose again to 2,464 and then 2,904 FE through the saved wire, before a second simulated-time charge filled it to 100,000 FE.

The player boarded by empty-hand seat click (`assignments=1`), closed restraints with the in-ride Rotation Tool, released station rear redstone, and pulsed the linked controller. It recorded `PassReached=19.397949/20`, `PassShort=1`, `PassStarved=0`, then advanced to STOP. The occupied car completed the original circuit and held at rider `z=-617.501507` with speed about `0.014632`; a second position query several seconds later was stable. Motor FE had fallen to 82,250. Station rear redstone captured the car; the [editor](captured.png) showed `Ready | Restraints closed` before manual capture. The player opened restraints and dismounted, returning availability to `assignments=0`. The final world saved cleanly.

Fixture commands placed FE blocks, set station/controller redstone, cleared the FE buffer for transfer tests, and advanced simulated time. They did not create or mount the car, fit the motor, link or program the controller, connect the wire, close restraints, or change rider state. This demonstrates wired FE transfer, interruption/restoration, persistence through cold restart, and an occupied STOP/capture cycle from that supply. The exact 20 blocks/s target remained short.
