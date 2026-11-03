# Removed custom font: direct stale-set fallback probe

This is one local packaged-client run of isolated source
`c31d08af384d69883770634f8d267410fc08d960` on NeoForge 1.21.1, with
optional Track Styles 1.2.0 and Engineered 1.1.1 present as separate runtime
JARs. The packaged addon JAR SHA-256 is
`d911e32e0a7c55c87a39ccbfa3c12b2d04137eaf87feec4a8b5f134add43e557`.
The exact command, dependency hashes and world input are in `metadata.json`.
GitHub Actions were not used.

**Result: the opt-in direct stale-set probe passed.** The client initially
loaded `fd_font_probe:transient` from the selected file resource pack, reporting
8 font sets at generation 1. `disable-stale-font-after-load.py` renamed only
that test pack's `transient.json` to `.disabled` 50 ms after the first loaded
marker. The next client reload was requested about five seconds later, after
the world was active. The generation-2 FontManager closed the old custom
`FontSet` identity `1571774117` and published a new seven-set map without the
custom ID. The opt-in probe called `getGlyph('A')` on that actual closed object.
The FontSet mixin logged
`FD_FONT_STALE_SET_REDIRECT ... closedIdentity=1571774117 liveIdentity=124835943 missingFallback=true`;
the probe confirmed the returned glyph was the same object returned by
Minecraft's current missing-font set, identity `124835943`.

The requested reload completed, chat rendered for 20 client ticks, Minecraft
shut down normally, and Gradle exited 0 (`BUILD SUCCESSFUL in 20s`). The
raw `retry-client.log`, `retry-console.log` and `watcher.json` retain these
events. `verification.json` is the output of the included verifier, which
checks event order, both identities, the absent image/closed-set failure
markers and normal stop. `evidence.sha256` covers all retained files.

The opt-in source change in `c31d08a` is inactive without both
`-PfdFontValidation` and `-PfdStaleRemovedFontProbe=fd_font_probe:transient`.
The normal fallback behavior is unchanged. This deliberately invokes a stale
closed reference after removal; it directly exercises the
`missingFallback=true` branch. It does **not** show that ordinary GUI code
naturally retains such a reference, nor does it certify tracing-disabled
operation, graphics modes, Styles rendering, or the full FD401 release gate.
The original save was restored from the tracked
`fd401-strict-occupied-reload-1a91416/pre-world.tar.gz`; its archive hash is
in `metadata.json`. `post-world.sha256` records the stopped save's files.

An earlier client attempt loaded the same custom font but stopped at generation
1 before a reload. Its logs are retained as `first-attempt-*.log`; a separate
GameTest overlapped that run. It is inconclusive and is not used for this pass.

To reproduce, copy `pack.mcmeta` and `transient-before.json` into a private
`resourcepacks/fd-font-probe` file pack with the font at
`assets/fd_font_probe/font/transient.json`, select that pack in `options.txt`,
and restore the referenced empty test world. Start the included watcher before
the exact `retryCommand` in `metadata.json`. The watcher only renames this
test font after the eight-set generation-1 marker; the existing validation
flags request one reload after world entry, open chat and exit. Run
`verify-stale-font-probe-log.py retry-client.log` to check the result.
