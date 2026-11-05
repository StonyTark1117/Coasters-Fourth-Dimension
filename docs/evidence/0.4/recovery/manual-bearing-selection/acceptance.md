# Manual ordinary-bearing selection and attachment checkpoint

This checkpoint closes the manual world-side ordinary-bearing selection and
attachment item in FD402. It uses the production Arrow recovery example in a
normal development client with every Fourth Dimension validation and fixture
flag false. It does not claim physical lever propulsion or a complete manual
towing lifecycle.

## Actual operator path

The client loaded the copied `FD402BearingOperator` world on a task-owned Xvfb
display. The first production-command attempt occurred before its destination
chunks were loaded and correctly rejected the uncleared/unloaded corridor. The
operator moved to the destination, allowed the chunks to load and retried
`/execute positioned -800 -62 512 run fd demo recovery_arrow`; the registered
command then built the normal Arrow car, bare ordinary bearing, track, chain
drive, clutch and lever. [The resulting world](example-created.png) contains no
automatic recovery selection or coupling.

The operator held the real Rotation Tool, aimed at the bare cart block and held
sneak while using it. The normal client/server item path produced the visible
message [“Recovery bearing selected. Open the retained end car and choose Attach
recovery.”](bearing-selected.png). Selection was neither injected nor fixture
authored.

The same operator then opened the end car's production editor, used its normal
**Close restraints** and **Retain** buttons, reopened the editor and selected
**Attach recovery**. Before attachment the editor still showed the available
[Attach recovery action](editor-before-attach.png). After the action, reopening
the editor showed [“Recovery braked | carriers retained”](editor-after-attach.png)
and exposed the normal Start tow, Brake tow and Detach recovery actions.

The native runtime log independently records the six-block link:

```text
[cart-link] linked carts 250151b9-ea08-4567-9e77-40ef2b1b20c9 <->
0474c4e6-a085-443c-aa01-03b4c7fa2852 at distance 6.000
```

After normal **Save and Quit to Title**, the schema-6 save records the first UUID
as the Arrow chassis and the second as its `recovery_cart`, with stopping,
restraints, maintenance retention and both carrier captures set. See
[attachment-summary.json](attachment-summary.json), the [client log](client-latest.txt)
and the [clean title-screen checkpoint](title-after-save.png).

## Isolation and RAM boundary

The run used only the task-owned private `:211` Xvfb display and isolated game
directory `/tmp/fd402-bearing-operator.1jZgoe/game`. The prepared NeoForge
classpath was launched directly with `-Xmx1536M` in its own 3 GiB systemd scope;
the highest live-observed peak was 2,632,626,176 bytes, leaving 588,599,296 bytes
below the hard cap. Render distance was reduced to four. Minecraft rejected an
attempted simulation distance of four as below its supported minimum and kept
the saved value of twelve; this is recorded in the client log rather than hidden.
The client saved and exited normally, and only this task's display was stopped.

The JAR SHA-256 was
`2da14200d2e33b806dc6bb454a65fb92912ae9d87ece345adff85c8bf1e277b0`.
Later repository commits through this checkpoint changed documentation and the
separate validation completion predicate, not the production selection,
attachment or editor paths exercised here.

## Bounded conclusion

This proves one real platform operator manually selecting the certified bare
ordinary bearing, carrying that operator-owned selection into the real car
editor, and creating the normal native recovery link. It is one empty Arrow car.
Physical lever input, manual propulsion/start/brake/detach/unload, occupied
single-player operation, multiplayer and rendered mesh/rider/rack clearance are
outside this checkpoint and remain separately scoped.
