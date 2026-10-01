# Create Ponder framing and PAX boarding telemetry

All six Ponder scenes play to completion on two packaged clients connected to the
pinned combined-pack dedicated server. The baseline reveals rear-carrier clipping
in Magnetic Free Spin and Revised ZacSpin. Changing the shared scene scale from
0.65 to 0.55 keeps the sampled rotating geometry inside the 854 × 480 viewport.
Caption text, scene timing, mechanics and dispatch validation are unchanged.

The added `FD_PONDER` diagnostics are enabled only by `fourthdimension.validation`.
They read the active native Ponder scene, time, keyframes, completion state and GUI
size every ten client ticks. They never open, seek, advance or modify the scene.
The native `/ponder <item> @a` command opens each scene on Dev and Observer; playback
runs continuously, and Escape closes it after native completion. Screenshot capture
observes the logged times without controlling the animation.

## Playback evidence

Both runs use default Ponder camera settings, English, GUI scale 2 (427 × 240 GUI
coordinates), an 854 × 480 window, and owned hidden Xvfb displays. Both players stay
connected during the six scenes. The complete run retains 76 baseline images and
110 corrected images, indexed by `captures.json`. Each image has its target tick,
observed native tick, scene, phase, client and diagnostic sample. The corrected run
adds rotation samples and PAX lift samples between captions. All 110 corrected views
were visually reviewed using contact sheets of the original screenshots.

| Scene | Baseline captures per client | Corrected captures per client |
| --- | ---: | ---: |
| Arrow | 6 | 9 |
| Legacy ZacSpin | 6 | 9 |
| Magnetic Free Spin | 6 | 9 |
| Revised ZacSpin | 6 | 9 |
| PAX | 9 | 14 |
| Station operations | 5 | 5 |

The corrected captures show readable captions, the intended carrier grouping,
rotation and PAX catch movement without viewport clipping at sampled stages.
Native text/title overlays can cover parts of the schematic. This is presentation
evidence, not native ride physics, a recording of every rendered frame, or a claim
for all user camera/zoom settings, translations and window sizes.

Compare the Free Spin rotation caption:

- [Baseline Dev](baseline/family_free_spin-dev-caption_3.png) and [Observer](baseline/family_free_spin-observer-caption_3.png).
- [Corrected Dev](framed/family_free_spin-dev-caption_3.png) and [Observer](framed/family_free_spin-observer-caption_3.png).

The original startup was interrupted when the temporary runtime disappeared between
continuations. The retained cached JAR and saved world rebuilt the isolated runtime.
The first resumed Arrow captures were obscured by Minecraft's first-run movement
tutorial. Normal movement and mouse input completed that tutorial; the six-scene
baseline was restarted from the beginning. Its interrupted capture index and log
are retained separately and excluded from the accepted 76-image set.

## Live winch telemetry

After corrected Ponder playback, both unmounted players wear Create goggles and
view the existing powered PAX winch. Observer switches from spectator to creative,
which enables the normal goggle HUD. The original fixture has four coupled cars,
eight carriers, sixteen seats, cycle count four and Engineered FE 12,345.

The six sources transfer through the existing native Create Display Link to its oak
sign, using ordinary block-data configuration to select each source. Sign NBT is
read after passive refresh; no direct sign-text writes or source-method calls occur.
The existing validation fixture boards sixteen real pig entities through Create's
native seats and closes restraints through normal vehicle validation. Both clients
show the synchronized change. Normal validated unloading restores the empty/open
state. No dispatch or moving cycle is performed in this bounded telemetry check.

| Source | Empty, open restraints | Sixteen riders, closed restraints |
| --- | --- | --- |
| Status | Boarding | Ready to dispatch |
| Occupancy | 0 / 16 | 16 / 16 |
| Restraints | No | Yes |
| Dispatch | Existing shuttle readiness denial | Ready to dispatch |
| Phase | Boarding | Boarding |
| Fault | Empty | Empty |

`winch-displays.json`, native server reports and both clients' `FD_STATION` samples
agree. All four goggle captures show complete ride text. Create shifts the long
empty-state denial tooltip left and clips its leading kinetic icon; the ride text
remains readable. The sign's actual stored lines are audited, including clearing
the previously populated line when selecting the empty fault field. This does not
certify legibility of every long message on every Display Link target.

- [Empty Dev](framed/dev-goggles-empty_open.png), [empty Observer](framed/observer-goggles-empty_open.png).
- [Occupied Dev](framed/dev-goggles-occupied_closed.png), [occupied Observer](framed/observer-goggles-occupied_closed.png).

The first telemetry harness stopped before changing the display because it read the
asynchronously written report log too early. Its script and failure are retained in
`framed/winch-log-flush-race`. The completed harness polls for the emitted report;
no runtime interlock was changed or bypassed. FE, cycle count, readiness and ownership
stay unchanged while each six-field set is sampled; external PAX drive remains denied.

## Audit, versions and limits

Run from the repository root:

```sh
python3 scripts/verify-create-ponder.py docs/evidence/integrations/2026-10-01/create-ponder
```

The audit checks native start-to-finish timing, keyframes, maximum sample gaps, every
required capture on both clients, PNG dimensions, recorded diagnostic samples, all
six sign fields in both states, native rider counts, client telemetry, final unloading,
exit codes and file hashes. Visual conclusions remain explicitly manual review.
`manifest.json` records source, compiled-class and immutable artifact hashes.
`verification.json` records the successful audit output. Full logs are gzip-compressed;
events and warnings are also retained as text. Command text trims trailing whitespace;
its original bytes are retained in `commands.raw.txt.gz`. Runtime passwords are not included.

Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10-280, Ponder 1.0.82+mc1.21.1,
Sable 2.0.3, Coasters Simulated 0.1.5, Fourth Dimension 0.9.0-dev.1, Java 21.
The combined pack adds Engineered 1.1.1, Track Styles 1.2.0, Additions 0.2.0,
Magnetized 1.1.0, Immersion 0.2.3, Aeronautics 1.3.2 and Interiors 0.6.1v3.
These are the existing pinned artifacts, not claims for other versions.

The build succeeds with 19 passing unit results restored from Gradle's build cache.
Both dedicated servers and all four clients exit zero. The baseline Dev receives a
TCP peer-reset diagnostic when the server is stopped first; Observer shows ordinary
server closure. The corrected clients disconnect normally before server shutdown
and record no peer reset, queued-packet exception, null-pointer exception, missing-body
or unknown-passenger warning. Original runtime settings are restored byte-for-byte;
all owned process and display handles are gone.

This checkpoint closes the sampled six-scene two-client presentation gap and adds
PAX boarding-state Create telemetry evidence. Moving/faulted telemetry, further
station/addon combinations, other GUI settings and the broader FD901–FD904 matrix
remain open. No full integration certification or release is claimed.
