# FD401 saved PAX consist: normal-client rebuild reaches a missing rear body

This is a copied continuation of the saved displaced four-car PAX world. The
original archive in `fd401-candidate-broken-consist-2e12e9d` is untouched. The
packaged NeoForge server and Dev client used Styles, Engineered and the
validation-only FE network. The client/server session used an isolated source
snapshot of `4d25b18` with the placement-obstruction diagnostic and temporary
chain-interaction logging; it is **diagnostic evidence**, not an exact release
candidate replay. The permanent placement message was committed as `bea04ec`
and `compileJava` passed in the current checkout.

After the prior normal-client Retain, rider evacuation, winch Detach and three
guarded car retirements, Dev normally placed replacement PAX items at the z79
and z91 station positions. The first item use reported smooth stone at
`(895,85,77)`, then `(895,85,78)`. In this disposable copy only, admin fixture
preparation removed the three smooth stone blocks at x895/y85/z76–78. The
next normal item use reported `Carrier capture engaged`; the z91 item placed
without a clearance change. Admin commands supplied the items, positioned Dev
for interaction and kept the route chunks loaded. All car placement, chain
clicks, editor actions, winch use and boarding were performed by the client.

The provider [server log](server.log.gz) records native chain links at exactly
6.000 blocks: `910c6fbd`–`cd90df9d` at 15:38:17,
`7b7f0154`–`910c6fbd` at 15:44:04 and
`5a4b8a72`–`7b7f0154` at 15:45:32. Dev used the normal editor to release
lead retention, sneak-used the existing winch to rebind all four cars, opened
restraints, boarded a PAX seat with an empty hand, and closed restraints from
the mounted editor. The winch NBT then listed four assigned cars in BOARDING;
the server log records Dev's accepted mount.

At 15:49:44 the provider logged `unlinked 5a4b8a72 ... 7b7f0154`. The mounted
editor Dispatch correctly refused with `Shuttle consist changed; recover and
rebind before operating`. Dev reopened restraints and Shift-dismounted
normally. In a cold reopen, the [diagnostic position query](position-query.txt)
found the first three chassis at the station and linked; the rear `5a4b8a72`
chassis was unavailable (`center=null`) although its vehicle record and winch
assignment remained. Wider world-chunk force loading did not restore it. This
is the current blocker for physical recovery, rebind and occupied dispatch;
the gate remains open. The exact removal or unload cause has not been proven.

The stopped [copied world](world.tar.gz) and compressed [client](client.log.gz)
and [server](server.log.gz) logs are retained. World SHA-256:
`71227b5ba5ad9aa4d86295619b500b6fd97d71366d351ffc7c3bae2bdf2a8e17`.
