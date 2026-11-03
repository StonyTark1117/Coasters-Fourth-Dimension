# FD403 PAX shipped-demo occupied client cycle

On 2026-11-02, the packaged standalone Minecraft 1.21.1 / NeoForge 21.1.248
client opened `FD403PaxDemoClient`, a copy of the survival construction world.
The client loaded the JAR built from `4950fea30ace94e4fe87093778564d9e59566016`,
SHA-256 `2a52a641f13ef78fa3c23c2fe466ff22bd359d3af33c4fb26b0f3773379285a2`,
with `JAVA_TOOL_OPTIONS=-Xmx4G`. The player remained in survival mode and used
commands for fixture positioning, temporary upstream-stone steps, and
`/fd demo pax`. The demo command **generated** its PAX track, four cars, winch,
Create motor and platforms; this run does not count as survival acquisition or
manual Chain coupling. The separate [construction checkpoint](../acceptance.md)
records normal client crafting from upstream ingredients.

The [demo command](demo-created.png) created a clear sample route near x=550.
After its four-second assembly, the native Coasters server logged
[three original links](events.txt), each at 6.000 blocks. The player boarded an
original PAX seat on the connected client, moved to the lead car, and used the
existing Rotation Tool's normal operator editor while mounted. The
[lead-seat view](lead-seat-boarded.png) shows a mounted rider; the
[editor](ready-closed.png) reported `Ready | Restraints closed` after the
restraint action propagated to the linked consist.

The mounted player clicked **Dispatch** in that editor. The
[immediate client view](motion-f3.png) records player z changing from 473.500
at boarding to 473.737 during the start. The later in-game winch block query
reported `Speed: 128`, four `shuttle_cars`, `shuttle_cycles: 1`, phase
`BOARDING`, and an empty fault. In production, the cycle counter advances only
after the returning train is physically recaught, reaches its station distance,
and all four cars pass boarding readiness. The [saved winch block entity](winch-save.json)
retains the same counter, four chassis IDs, station distance 62.6798, and no
fault. The [post-cycle status](postcycle-status.png) and selected log
[events](events.txt) retain one rider on the original carrier after the catch.

The player then opened restraints from the [captured editor](final-captured.png)
and dismounted normally. The final `/fd status` reports all eight demo
carriers captured with zero occupants. The [final view](final-dismount.png),
[schema-6 foundation save](foundation-save.json), and normal client save/exit
record the unloaded state. The foundation save retains all four demo cars,
their original carrier IDs, `stopping=true`, `restraints=false`,
`boarding_ready=true`, and no fault. The complete
[saved world](world.tar.zst) is available for a cold-reopen check; its SHA-256
is `d9d66718bbb8f1ca648b7c65affb498afb17ee74800dc1dcf51e4048d815349a`.
The client log SHA-256 is
`481f6fbd86dfe5d46a940f564a692a6bff4c278157e542868794235a6a728dcd`;
it ends with `BUILD SUCCESSFUL in 28m 33s`. The raw foundation save SHA-256 is
`d83e212cba3ac2662203a37c5629da84c684f399f89dc0b54c4caa6836653473`.

This proves the shipped PAX sample's linked, occupied, powered client cycle
through the operator editor and normal unloading. It does not prove that the
survival-crafted four cars in the separate construction plot can be coupled by
player Chain clicks, nor the full FD403 five-family powered and tutorial gate.
