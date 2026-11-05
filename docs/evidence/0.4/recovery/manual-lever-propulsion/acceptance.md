# Manual no-fixture lever-propulsion checkpoint

This checkpoint continues the same production Arrow world used by the manual
ordinary-bearing selection checkpoint. It corroborates the already accepted
empty-Arrow lever workflow with a later `0.9.0-dev.27` client in which every
Fourth Dimension validation and fixture flag is false. It does not replace the
earlier moving-brake acceptance or close broader FD402 rendering and lifecycle
items.

## Operator path

The saved Arrow chassis and certified ordinary bearing restored under their
original native IDs at six-block spacing. The operator used Create's normal
motor value screen to change the installed motor from 120 RPM to exactly eight
RPM. A read-only `/data get block` query records `ScrollValue: 8` and
`Speed: -8.0f` in [motor-data-8rpm.png](motor-data-8rpm.png); no block data was
edited by command.

After a fresh client restored the same native link, the operator selected an
empty hotbar slot and physically right-clicked the installed vanilla lever.
The handle visibly changes from the power-cut position in
[lever-topdown-before-off.png](lever-topdown-before-off.png) to the supplied-power
position in [lever-topdown-after-off.png](lever-topdown-after-off.png). The
production vehicle editor then supplied **Start tow**. The first post-action
frame is [after-start-tow-lever-off.png](after-start-tow-lever-off.png); the
later fixed-camera frame [tow-motion-after-7s.png](tow-motion-after-7s.png)
shows the attached recovery group translated along the native rail.

The operator did not reopen the moving editor quickly enough in this repeat.
The guarded route-end stop therefore occurred before the editor was reopened;
[editor-for-brake3.png](editor-for-brake3.png) records the resulting braked
state and route-end diagnostic. The operator still invoked the real **Brake
tow** button afterward, producing [after-brake-tow.png](after-brake-tow.png),
but this is deliberately not counted as a second moving-brake measurement.
That part remains supported by the earlier
[switchable recovery power checkpoint](../power-control/acceptance.md).

Finally, the operator physically right-clicked the lever again. The before
frame is [lever-topdown-before-on.png](lever-topdown-before-on.png); the after
frame and a read-only block-state assertion are combined in
[lever-power-cut-confirmed.png](lever-power-cut-confirmed.png). The assertion
only emitted `FD402_LEVER_POWER_CUT_ON` when the lever at `-799 -58 524` matched
`minecraft:lever[powered=true]`.

## Persistence and isolation

Minecraft exited through **Save and Quit to Title** and **Quit Game**. The
integrated server saved all dimensions and the client stopped normally; see
[client-latest.log.gz](client-latest.log.gz) and the
[title-screen checkpoint](title-after-save.png). The final schema-6 save keeps
the Arrow vehicle stopped, captured and maintenance-retained with its original
recovery-cart identity; the decoded record is
[foundation-after-acceptance.txt](foundation-after-acceptance.txt). Its SHA-256
is `e4fd634ae1c20f5acc92247c922bb7b997c0bbc40697f06f00ebd8d5b6451fc2`.

Only task-owned private displays were used. The main interaction ran on `:212`.
After that client exited, a relaunch stalled in GLFW and shut down before world
load. The task stopped only its own idle `:212` Xvfb, reserved fresh display
`:219`, and completed the accepted continuation there. Both displays were
stopped after their clients exited. No user desktop or another agent's display,
input session, game directory, port or process was touched.

The accepted `:219` client used isolated game directory
`/tmp/fd402-physical-lever.7STV4D/game`, `-Xmx1280M`, and a 2.75 GiB systemd
hard cap. Its live-observed peak was 2,523,004,928 bytes, 429,785,088 bytes below
the 2,952,790,016-byte cap; see
[memory-scope-before-quit.txt](memory-scope-before-quit.txt). The exact VM and
program arguments are retained as [client-vm-args.txt](client-vm-args.txt) and
[client-program-args.txt](client-program-args.txt). The exercised JAR SHA-256 is
`2da14200d2e33b806dc6bb454a65fb92912ae9d87ece345adff85c8bf1e277b0`.

## Bounded conclusion

This proves that a real platform operator, with automation disabled, can set the
normal Create motor to eight RPM, physically switch the documented lever to
supply power, start the already attached native recovery group, observe native
motion, and physically restore the final power-cut state. The route-end guard,
not a timely manual brake, stopped this particular repeat. Detachment,
restraint opening, occupied operation, multiplayer and all-family rendering are
outside this checkpoint.
