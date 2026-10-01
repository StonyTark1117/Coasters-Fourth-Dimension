# Packaged banked styling and occupied ride checkpoint

This is bounded FD401 evidence for **0.9.0-dev.6**, not the 0.4 or 0.9 gate.
The exact packaged hash is in `bank-gui-artifact.sha256`; the later dev.7 PAX
regression test is a separate artifact. Track Styles 1.2.0 and Engineered 1.1.1
were separately installed, with the base stack pinned in `docs/INTEGRATIONS.md`.
No external meshes, textures or code are included in this evidence.

## Actual operator workflow

The source was the closed `FD04StyledRide` world, copied to the disposable
`FD04BankedRide` folder. Its internal level name remains `FD02 Acceptance`.
The initial baseline contains 35 sections, eight chassis and twenty carriers.
Normal `/execute positioned -350 207 -212 run fd demo arrow_bank` creates eight
more functional Arrow sections, one chassis and two carriers. Its native banked
anchors are (-342,210,-148) and (-326,210,-148). The 16-block upper straight retains
`0:0;8:360;16:0`; the level right straight retains `0:0;18:360;36:0`.

Using the Rotation Tool's normal Track appearance screen selected double spine
with `#4A90E2` on the upper banked straight, and single spine with `#9B59B6` on its
curved exit toward (-318,210,-156). Both styles have revision 1 and preserve their
original profiles, functional provider, family and geometry fingerprints.
Attempting to style the entry curve next to the parked vehicle was correctly
rejected as occupied; that section remains Original, revision 0.

Temporary standing blocks for the editor were removed through normal `/setblock`
commands before boarding. The first fast editor attempt failed the eight-block
endpoint-distance check; a later creative-flight/near-endpoint attempt succeeded.
No addon NBT, angles, velocities or passenger state were manufactured.

An actual F3+T resource reload at 22:35:47 was followed by the catalog's second
55-entry bake at 22:35:48. `bank-gui-resource-reloaded.png` shows both styles after
that reload. The earlier F3+R attempt did not reload resources and is excluded.
`bank-gui-render-environment.png` records Java 21.0.12, NeoForge 21.1.248, Mesa
26.3.3 llvmpipe, Flywheel 1.0.6 with the indirect backend and limitUpdates enabled.
This is one observed rendering configuration; no shader pack was installed.

## Cold reopen, ride and normal exit

The first client was paused after a real integrated-server autosave. Its display
connection subsequently broke and that client terminated unsuccessfully; this is
not a clean exit certificate and its cause remains unproven. The world was not
recreated. The second client opened the same folder with the same dev.6 artifact;
`bank-gui-cold-reopen.png` shows both saved styles restored.

The player teleported to the existing station platform, selected an empty hand,
and right-clicked the carrier seat. Create's actual SeatEntity mounted the player.
The mounted Rotation Tool screen closed restraints and dispatched normally.
After departure a normal command supplied the station's rear redstone input at
(-356,210,-179), requesting capture on return. The player stayed aboard through
the complete native circuit, including both controlled turns and the banked exit.
The occupied body is `ccf0437f-f43a-4c2c-8844-7b02184e6c42`; only its actual occupant
records are used for ride claims.

The diagnostic interval is five seconds, so it does not locate the exact peak of
the brief banked turn. At 22:46:29 the occupied body's snapshot/native angles are
5.8414751366 / 5.4638870900 radians. At 22:46:49, on the longer level full turn,
they are 6.2766148039 / 6.2778102400. These are actual samples, not a fabricated
GUI full-bank peak or a camera synchronization tolerance certificate. The separate
native `bank-acceptance.md` test measures both turn peaks at finer resolution.

The returning mounted screen reports Ready with restraints closed. Normal Open
restraints followed by a held Shift dismounted the player onto the platform.
Actual Save and Quit completed all-dimension saves; Quit Game ended the client
with exit code 0 before stopping its owned hidden display. The saved player has
no RootVehicle or NoGravity tag, no gravity-restore marker, and no mounted riders
remain in any of the 22 carriers. All nine vehicles are fault-free, captured,
ready and have open restraints. Existing IDs/mechanisms/profiles and the two
selected styles survive unchanged; the new car has four operator revisions.

## Reproduction and limits

Run `python3 scripts/verify-banked-styled-ride.py` to verify the committed baseline,
styled autosave, final save, player observations and selected client logs. This
checks saved observations; rendering requires reviewing the PNGs. The first
client failure is retained in `bank-gui-client.txt`, alongside the separate clean
second client lifecycle.

This closes the banked default-style GUI/reload/occupied-circuit observation for
this layout and artifact. Complete wheel/chassis/rider/linked-car swept clearance,
other Flywheel modes, a shader stack, measured performance, full Engineered
controller/FE-network operator workflows and the remaining FD401 requirements
are still open. It does not certify arbitrary user track geometry or resource
pack replacements, and it does not close FD902's broader lifecycle work.
