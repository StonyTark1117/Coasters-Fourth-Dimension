# FD401B selected-JAR whole-section appearance transition

**Result:** bounded pass for one Arrow full-turn section in a Styles-only client. The normal Rotation Tool editor changed the entire selected section from double spine to Original, single spine, double spine, then single spine. The live appearance screen confirmed each applied choice. The final single-spine choice survived Save & Quit, reopening the world, and an F3+T resource reload.

## Runtime and input

- Source qualification: `ec2fcda`; exact runtime JAR SHA-256 `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`, identical to `docs/evidence/qualification/2026-11-05-ec2fcda/qualification.jar`.
- Java 21.0.13, private host Xvfb `:286`, direct game launch with `-Xmx1536M` inside an active systemd service with `MemoryMax=6G`. Gradle was not inside that cap. The retained `styles-classpath.txt`, `styles-vmargs.txt`, `styles-program.txt`, `run-client.sh`, and `options.txt` identify the launch. The world was copied from the earlier selected-JAR provider-lifecycle Styles world.
- A temporary stone block at `(-334,210,-149)` supplied an unambiguous, nearby solid click target beside the styled rail. The player stood at `(-334.5,211,-151.5)` for the successful UI clicks. An initial farther click pose failed to operate even a vanilla stone control; the isolated closer control succeeded with normal mouse bindings. The successful run used `rawMouseInput:false`, normal left/right mouse bindings, and ordinary Rotation Tool UI buttons. The temporary stone is a disclosed scratch-world fixture, not track construction.

## Observation

The chosen rail runs from `(-342,210,-148)` to `(-326,210,-148)`, family `arrow_controlled`, variant `arrow_four_rail_v1`. Its three-point rotation profile reaches 360 degrees at 8 blocks and returns to zero at 16 blocks. The initial saved appearance was `coasterssimulatedextratypes:arrow_4d_dbl_spine`, tint `#4A90E2`, revision 1.

Screenshots show the initial double-spine appearance, reopened Original, reopened single spine, reopened double spine, the final single-spine choice, and single spine after world reopen and F3+T. `latest.log.gz` records the cold world reopen, resource reload, clean world save, and normal client shutdown. `final-world.tar.gz` preserves the post-test world.

`before-rails.json`, `after-single-rails.json`, and `comparison.json` verify all 24 serialized rails. Only the targeted rail changed. Its appearance became `coasterssimulatedextratypes:arrow_4d` with the same tint and its revision advanced from 1 to 5, matching four UI applications. Its endpoints, family, variant, ID, geometry hash, and complete rotation profile are byte-for-byte equal in decoded data; the other 23 rail records are exactly equal.

## Boundary

This proves normal UI whole-section transitions and persistence for one full-turn Arrow span in the Styles-only profile. It does not prove a coupled moving ride through that span, swept geometry during motion, per-component render ownership, or the full FANCY/FAST/FABULOUS and shader matrix for both appearances. Earlier separate evidence covers bounded occupied ride, provider lifecycle, and graphics-profile observations; FD401B remains in progress.
