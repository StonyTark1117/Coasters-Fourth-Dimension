# FD401C selected-JAR live rider power fault (bounded)

2026-11-05. This is one distinct selected-JAR PAX fault run, not a repeat of the normal four-car cycle or the earlier candidate-JAR fault run. The JAR SHA-256 is `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7` (source baseline `ec2fcda`). The input was a fresh copy of the saved four-car normal-construction world from `fd401-pax-normal-construction-656725bc-20261105`. The session ran with Java 21 on private Xvfb `:279`, a direct client launched in an active 6 GiB `systemd` memory scope, and `-Xmx1024M`. `rawMouseInput` was turned off during the run to aim with Xvfb input. The graphical client and display were stopped after a normal save and exit.

1. Dev empty-hand mounted a normal PAX seat. `client.log.gz` records `FD_BOARDING_EVENT` and accepted `FD_BOARDING_ASSIGN` at 05:18:13; `boarded2.png` shows the mounted rider. The Rotation Tool displayed `pax_shuttle | Ready | Restraints open` (`rider-ui.png`).
2. Normal UI actions closed restraints and dispatched. `/data get block -856 144 83 shuttle_phase` returned `LIFTING` (`phase-query.png`).
3. During that lift, `/setblock -856 143 83 minecraft:air` removed the Create creative motor as a controlled power-fault fixture. `/data get block -856 144 83 shuttle_fault` returned `Winch power lost: retain the train; inspect and resume or recover` (`fault-query.png`).
4. `/setblock -856 143 83 create:creative_motor[facing=up]` restored the motor (`power-restored.png`). A normal Rotation Tool request to open restraints was refused with `Shuttle restraints remain closed until final station capture` (`release-at-fault.png`). Thus the live occupied fault held the rider rather than permitting an early unload.
5. The client used Save and Quit to Title, then Quit Game. `client.log.gz` records integrated-server world and player save; `title-after-save.png` and `fault-world.tar.gz` preserve the state. `SHA256SUMS` identifies the packaged evidence.

## Boundary

This run does **not** show explicit operator resume, a recovered cycle, or final station unload. The mounted rider could not directly target the winch through the four-car structure; no second operator client was started while other FD401/FD402 clients were active. Those later steps need a separate operator on the preserved world or another isolated run. Earlier candidate-JAR evidence has a power-fault resume and normal unload, but it is not evidence on this selected JAR.
