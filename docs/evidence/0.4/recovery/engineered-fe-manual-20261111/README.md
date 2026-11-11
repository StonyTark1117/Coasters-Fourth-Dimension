# FD402F1 rendered manual Engineered recovery

This record closes **FD402F1**, the Engineered-only rendered/manual Arrow
recovery case. It combines observations from one connected-client session under
the cumulative validation policy. FD402F2 (the combined Styles + Engineered
profile) remains open.

## Result

The client manually fitted the Engineered linear motor, controller, adjacent
lever, Create Crafts & Additions generator/connectors and wire. Screenshots
`053`–`080` show those provider controls, a filled 100,000 FE anchor and the
same anchor starved after physically isolating the target connector. The
occupied retained Arrow stayed braked/starved; restoring FE did not start it.
The world was also reopened in a fresh client process without automatic motion.

The final correctly spaced certified bare bearing was selected by delayed
sneak-use of the Rotation Tool (`263`), attached through the normal vehicle UI,
and shown occupied/retained/braked under zero FE (`266`). After the already
proved isolated buffer was refilled to 100,000 FE, the player cleared carried
equipment and pressed **Start tow** in the rendered recovery UI (`269`–`270`).
The log then records the same chassis:

- braked baseline: z=49.64, speed approximately 0.001 blocks/s, FE=100,000;
- first powered sample: z=49.04, speed=0.4816 blocks/s, FE=87,762; and
- second powered sample: z=47.16, speed=0.4886 blocks/s, FE=86,376.

That proves explicit occupied motion, FE consumption, and the production
0.75-block/s cap. The same uninterrupted client session had already exercised
the normal rendered **Brake tow** control (`160`–`161`) and normal stationary
Detach recovery → Open restraints → Shift dismount cleanup (`185`–`189`). The
later controller-direction and motor-span corrections affect propulsion reach,
not those shared recovery controls, so the policy permits the compatible slices
to be combined. Native rebraking/no-consumption and teardown assertions remain
covered by the isolated FD402 GameTest checkpoint.

## Run boundary and provenance

The client ran Java 21 with a 1536 MiB game heap on task-owned private Xvfb
`:241`. Only the Minecraft process and children were in a 6 GiB systemd scope;
`MemoryPeak` was 3,105,902,592 bytes. The run loaded Engineered 1.1.1 without
Track Styles, so this is Engineered-only evidence. The selected clean JAR was
`656725bc...`; the development overlay added the validation-only staging command
and temporary clearance logging. The latter was reverted after the run. Neither
change altered recovery decisions or provider force behavior.

The declared powered measurement ends at the 06:04:09 FE sample. The operator
did not brake before the short temporary straight motor span ended during later
diagnostic input, so subsequent off-track samples are excluded from acceptance.
`preserved-world-pre-boundary.tar.zst` was copied from the last complete autosave
before that diagnostic overrun. The client and owned Xvfb service were then
stopped; no other display or client was addressed.

The validation-only `/fd validation board_locked Dev` command mounted the real
native seat and closed restraints. All provider construction, bearing placement
and selection, recovery Attach/Start/Brake/Detach controls, power wiring, world
reload, restraint opening, and dismount used their normal rendered paths. The
direct FE refill after manual wire supply/starvation was disclosed and used only
to avoid handing a wrench to the retained rider; the physical producer/wire
workflow had already been observed in the same world.

Run `python verify.py` to check hashes, profile markers, measured motion/FE
samples, Java/heap launch arguments, and the retained screenshots.
