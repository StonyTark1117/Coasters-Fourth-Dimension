# FD401 Engineered normal operator loop and noncreative FE check

This continuation used the saved [bare-loop item-placement world](../fd401-bare-item-placement/README.md) on `main` at `1a0ad7c`. The Arrow car was the one placed by ordinary item right click in that world. The integrated packaged NeoForge client ran with `-PfdPackaged -PfdValidation -PfdIntegrations=engineered -PfdFeNetwork`; `requireEnergy = true`. The client saved every dimension and exited with Gradle `BUILD SUCCESSFUL`.

| Artifact | SHA-256 |
| --- | --- |
| Fourth Dimension packaged `0.9.0-dev.27` JAR | `2c336eef8c7701fbc21cedcc9ac2c204c809a2394576cd91015c8bb09bc8f04f` |
| Engineered 1.1.1 JAR | `eea19ed769ed573727b4e15184c229f66aff85f20667464650724e1b2d657f5d` |
| Validation-only Create Crafts & Additions 1.6.0 JAR | `41876c3780b70365a1848994d146a73423cc19fbe86485885795d9e7d855e7e9` |
| [Client log](client.log) | `6e69f6ed1c7f720e592092ac616fb0a8ae8d824a449b0952d52463f8569872f0` |
| [Saved world](world.tar.gz) | `8d4446c651c81425030b81f56943d4d43782d0b768573980b0977ae31ad4441f` |

## Operator cycle

1. With the real Engineered motor item, the client selected the start anchor `(-500,142,-630)` and clicked the first straight near `z=-610`. Anchor NBT recorded `CoastersEngineeredMotorSpan` with `T0=0.0` and `T1=0.5555555520669377`. The client selected that anchor with the controller item and placed a linked controller at `(-506,142,-629)`; its saved target was the motor anchor. [The green binding message](controller-bound.png) appeared. The first controller program was `Launch 20 → End`.
2. Fixture commands placed one Crafts & Additions alternator adjacent to the motor anchor at `(-500,142,-631)`, and three Create water wheels behind it on the same north-south shaft. Water flowed beside the wheels. The native kinetic network reported `Speed=8`, `Capacity=768`, and `Stress=512`; the alternator reported `AddedStress=64`. The anchor's FE rose from no stored energy to 34,155 without a direct fill or creative generator.
3. The station at `(-506,142,-618)` accepted a redstone block on its rear input at `(-506,142,-617)`, recording `powered=1`. The player boarded the original car by empty-hand right click on a seat; native availability changed to `assignments=1`. The [in-ride editor](boarded-open.png) showed `Ready | Restraints open`; the player used the Rotation Tool to close them.
4. Removing rear redstone released the station. A redstone block beside the controller at `(-506,142,-628)` pulsed the motor. The controller recorded `PassTarget=20`, `PassReached=19.385034561157227`, `PassShort=1`, and `LastCart.Speed=19.385035`. The rider moved around the original closed route while assigned; anchor FE fell to 4,306 during the ride. The controller did **not** quite attain the 20 blocks/s target.
5. With the station's rear redstone held high, the car returned into range and the [editor](station-captured.png) showed `Ready | Restraints closed` before any manual capture. The player opened restraints in the editor and Shift-dismounted; native availability returned to `assignments=0`.

## Interruption and STOP boundary

The client then programmed and saved [`Launch 20 → Stop at 18 → End`](stop-program.png). Removing the middle water wheel disconnected two wheels from the alternator, leaving `Capacity=256` against `Stress=512`. A fixture command cleared the motor anchor's stored FE to zero. A subsequent query found no energy key. The player boarded normally again, closed restraints, released station redstone, and pulsed the controller. With no FE, the controller recorded `PassStarved=1`, `Starving=1`, and `LastCart.Speed=7.6837145E-4`; the rider stayed near the station.

Replacing the wheel restored `Capacity=768` and FE transfer. The starved controller then resumed briefly with a small buffer; its sequence advanced to `Step=1` (STOP), but the pass reached only about 2.59 blocks/s and the car coasted away from station range. The player used the [in-ride editor](interruption-captured.png) to capture it, opened restraints, and dismounted. The saved world retains this safely unloaded car away from the station, the restored water-wheel supply, and the STOP program. This **does not** establish a completed STOP hold/release cycle, a cold reopen and repeat operation, or a fully charged ride after interruption.

The fixture commands built the track and FE blocks, set and removed redstone blocks, and cleared the FE buffer for the outage test. They did not allocate the car, fit the motor, link or program the controller, board the seats, or edit rider state. The raw client log and saved world retain the exact command and native-state record.
