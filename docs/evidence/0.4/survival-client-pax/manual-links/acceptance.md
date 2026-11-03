# FD403 player-linked PAX demo client cycle

On 2026-11-02, the packaged Minecraft 1.21.1 / NeoForge 21.1.248 client
opened `FD403PaxManualDemoClient` in survival mode. The tested JAR's SHA-256
is `4da417a132ccb7f96d5193c39503b82c1c1005020aed51eb96af997a1075af31`.
The run used `JAVA_TOOL_OPTIONS=-Xmx4G` and ended with a normal Save and Quit,
then `BUILD SUCCESSFUL in 12m 31s`. The complete [client log](client.log.zst)
has raw SHA-256 `3bdbf8172211237d98c90ff0c401dfd2524e14069d07e3ea2b5b7949879849d2`.

The player used `/fd demo pax_manual` at x=650. That command authored the
compact, level-station PAX route, four cars six blocks apart, winch, Create
motor and station platforms. It left the cars unlinked and the winch unbound.
The player then held a normal Minecraft Chain and right-clicked each adjacent
cart pair. The native Coasters server reported [three successful player links](events.txt),
each at 6.000 blocks, between the four original chassis. Temporary stone
footing let the player reach the native cart bearings and lead seat. With an
empty hand, the player sneak-used the winch; its [BOARDING message](winch-bound.png)
confirmed binding.

After this run, the manual command's platform was extended two blocks inward
on each side to include the footing used to reach the outer seats. The
previously shipped `/fd demo pax` platform geometry is unchanged. The platform
edit passes a packaged JAR build; this client record remains tied to the JAR
hash above and does not claim a second client replay of that edit.

The player boarded an original PAX seat with an empty-hand click. The
[mounted view](lead-seat-boarded.png) shows the normal passenger prompt. The
Rotation Tool's [operator editor](ready-closed.png) then reported
`Ready | Restraints closed`. The player clicked **Dispatch**. After outbound
and return travel, the [winch query](winch-cycle.png) reported `Speed: 128`,
four `shuttle_cars`, `shuttle_cycles: 1`, phase `BOARDING`, and an empty fault.
The [post-cycle editor](postcycle-captured.png) again reported readiness with
the rider aboard. The player opened restraints and held Shift to
[dismount normally](final-dismount.png).

The [schema-6 foundation save](foundation-save.json) retains the four manual
sample cars under the same winch binding, with `stopping=true`,
`boarding_ready=true`, `restraints=false`, no fault, and zero riders on all
eight carriers. The [saved world](world.tar.zst) has SHA-256
`d3179be99ea458189daaf9c7eb8f5a997e60d465c659628a4e3da802b94cc17b`;
the foundation JSON has SHA-256
`f530d00d5e8de5e4a69863b2f57a251b146161442f50a432aab224a2a10199ef`.
The log records both the linked-cart events and the final zero-occupant status.

This proves player Chain coupling, winch binding, normal boarding, mounted
operator dispatch, powered recatch and normal unloading on a **command-authored
demo route**. The separately [survival-crafted construction](../acceptance.md)
has not yet coupled and cycled its own four cars. The other families' complete
powered teaching exercises and the FD403 gate remain open.
