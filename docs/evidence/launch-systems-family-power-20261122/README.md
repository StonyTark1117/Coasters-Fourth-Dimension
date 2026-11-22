# External family drive and accessory power contract

2026-11-22. Isolated overlay on `8cebab779da3311b39e3471bbfb41ebf1dafcd19`,
branch `codex/launch-systems-authority`, worktree
`/home/braydon/.codex/worktrees/launch-systems-authority/Coasters: Fourth Dimension`.
The primary checkout's 1.4/1.5 changes are not part of this artifact.

`EXTERNAL_API` remains 1. The additive `EXTERNAL_FAMILY_API = 1` marker lets
consumers require this family contract without claiming it on older artifacts.
External providers may report actual live accessory power through a default-false
callback. Held or suspended longitudinal authority does not itself remove a
restored physical accessory supply; fresh drive permission remains independently
required. Complete native bodies, current controller identity, whole-consist
eligibility and ownership are checked before accepting that supply.

Conventional and New-generation Flying retain their native dock/posture, yaw,
power-loss and explicit recovery logic. Their existing power predicate accepts
the selected external controller's actual accessory supply. A missing external
provider cannot fall back to native Chain power. Without external ownership the
native Chain predicate is unchanged.

Launched Inverting Spinning accepts registered external longitudinal ownership
without also commanding its native launch governor. Native 16 blocks/s travel,
4 rad/s spin, swept clearance, lift-lock Chain, carrier capture, direction and
reload guards remain. A missing selected controller fails closed. Magnetized
and ordinary native governor paths retain their existing checks.

## Focused validation

Java 21 launcher and Gradle JVM verified. Command used the existing idle NVME4
build/cache with `-PfdGameTestNamespaces=fourthdimension_ls004 jar runGameTestServer`.
[Native log](native.log): **1/1 pass**, retaining exclusive ownership, held transfer,
stale/replay rejection, missing controller, save/reload and explicit resume checks.
New assertions verify that armed authority does not invent accessory power,
actual supply is available while held, power loss is immediate, and controller
loss leaves external selection intact while denying accessory supply.

The native server saved all dimensions and exited successfully. No graphical
runtime, display or persistent service was started. [Artifact identity](artifact.json)
records the frozen JAR and changed source hashes. Actual S2/Flying family operation
is being qualified by Launch Systems' LS501/LS502 integration tests; this record
alone does not claim those end-to-end results. Previous native/API1 evidence is
retained for unchanged behavior.
