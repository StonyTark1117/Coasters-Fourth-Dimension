# FD401C clean-JAR combined Free Spin cycle

On 2026-11-05, a Java 21 client loaded the selected clean `ec2fcda` JAR
(`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`)
with Track Styles 1.2.0 and Engineered 1.1.1. The retained isolated
committed-source build and packaged client classpath were reused from the
clean-JAR combined Legacy run; the JAR hash was checked again. The direct
launch script used Java 21 and `-Xmx1024M` under the task-owned Xvfb `:244`
and an active systemd scope with `MemoryMax=3G`. The client exited with
`Result=success`, and the display was stopped afterward. The classpath and
dependency hashes identify the runtime inputs.

The world copy came from the earlier normally constructed Engineered-only
Free Spin route in `fd401-engineered-current-free-spin-fe`. It was not newly
constructed in this run. Its saved controller program was `Launch 8 → STOP 8
→ End` and `requireEnergy=true`. Crafts & Additions remained a disclosed FE
fixture dependency. At startup, the saved anchor contained 86,421 FE, and
availability reported one locally loaded chassis, four carriers and no rider
assignments. The connected player Dev boarded the local Free Spin car with
the disclosed `/fd validation board_locked Dev` helper. This does not prove
normal seat boarding.

After clearing the rear station signal and pulsing the saved linked
controller, `/fd validation motion Dev` reported 7.219 blocks/s and
`capture=false`. The controller advanced to step 1 (`STOP`), with
`PassShort=0b`, `PassStarved=0b`, and `PassReached=7.972430/8`; the literal
8 blocks/s target was not reached. Anchor FE fell to 74,833, a draw of
11,588 FE. Restoring the rear signal yielded `capture=true` with roughly
0.0015 blocks/s remaining and the assignment still present. The normal
vehicle UI showed `sns_free_spin | Ready | Restraints closed`. Dev clicked
**Open restraints**, held Shift to dismount, and the `RootVehicle` query found
none; availability returned `assignments=0`. The client saved to title and
quit normally. Commands, screenshots, logs and the stopped final world are
retained here, with bundle and classpath hashes.

This is one bounded combined-profile Free Spin launch, STOP, FE draw, capture,
UI release and unload on the selected clean JAR. Normal seat boarding, fresh
combined-profile construction, other-family operator cycles, and full profile
qualification remain open.
