# FD904C1 world-stable rider view — in progress

Owner01a1dd1d-1468-7f81-8f31-5532ad7a178d, primary main. Required occupied Wing
seat/pose handoff is integrated in cefd4fae; see ../wing/occupied/README.md.

K toggles a saved client preference, configurable in normal Controls. The camera
inherits one world-upright heading captured at activation/boarding. It preserves
ordinary mouse look and native eye position but intentionally does not follow
ride yaw, pitch or roll. It applies only to the local camera while mounted to a
known Fourth Dimension carrier. Dismount, death, world exit and a new mount clear
the cached frame. Server motion, rider/body rendering and remote players are
untouched. No camera state is persisted or sent to the server.

Pinned Sable2.0.3 inspection shows that CameraMixin composes the inherited CAMERA
quaternion once with local view rotation, updates forward/up/left and view angles,
and independently transforms eye position. The bounded client-only injection
changes only getEntityOrientation's CAMERA result for the local mounted player.
Other entity/body/remote transforms keep the original quaternion. All original
five families and Wing resolve through the same Boarding carrier body binding;
there is no family-specific camera override. Their accepted normal view/body/sync
proof remains valid; this task checks the new selected mode.

The actual shared ComfortFrame function passed16 combinations of upright,
vertical and inverted pitch/bank: finite normalized world-up frame, preserved
seat quaternion and distinct left/right mouse-look compositions. First standalone
JUnit attempt loaded a Minecraft client class absent from this project's test
runtime; extracting only the presentation math removed that test harness dependency.
Corrected targeted test and packaged build passed once (math-build.txt).

Remaining finite native cases: Wing upright/banked mode toggle and independent
look/arms/body; one original rotary inverted pose through the common body path;
reconnect plus dismount restoration. This reuses Wing's fixed-group restart and
its new two-car banked check. No original-family feature qualification is reopened.
