# FD403 current-artifact teaching review

This review used the packaged standalone `0.9.0-dev.27` client on Minecraft
1.21.1 / NeoForge 21.1.248. The source code was built at
`a6e18c906a8e1f9f33880030dccd7b0be1b53b16`; the packaged JAR SHA-256 is
`34c2d6ce14fecaf9941303ff950ec9f8ef586a61456669a0d404b0be43672db1`.
The later `0aeec47` commit changed documentation only. This is the same JAR as
the [survival-built PAX cycle](../survival-client-pax/survival-cycle/acceptance.md).

The client opened a separate copy of that saved world as `FD403TeachingCurrent`.
The player switched to Creative solely to inspect teaching UI; this copy is not
used as survival acquisition or ride evidence. The review ran at 854 × 480 on
the standalone dependency profile, with no optional Track Styles or Engineered
mods. The JVM was capped at 4 GiB; the host had more than 40 GiB available.

## What was checked

The client opened the Fourth Dimension creative tab and displayed all fifteen
item tooltips. [Individual tooltip screenshots](tooltips/) and the three
[contact sheets](tooltips/contact-1.png) record the guidance. Each tooltip was
read for family matching, use and controls. All text was visible within the
854 × 480 window; no tooltip covered the screen edge.

Normal hold-W inventory access opened all nine unique Ponder scenes. The
[scene captures](ponder/) show their rendered models, captions and navigation.
For each scene, the client timeline was scrubbed through its caption sequence;
the nine caption sheets record every unique caption. We checked functional
family matching, the certified ordinary-cart exception, native-anchor building,
Arrow's `360`-degree full turn, free versus controlled carriers, magnetic masks
and the boarding lock. The PAX lesson covers four six-block-spaced Chain-linked
cars, the winch's 16–128 RPM range, boarding readiness, powered lift, release,
outbound/return motion and final catch. Captions fit in their panels, including
the longest Arrow, magnet, boarding and PAX instructions. The tutorial toast
occupies the upper right of several captures but does not obscure the caption
panel or scene controls.

The opt-in packaged validator compiled, played to completion and replayed all
53 item/scene registrations across the fifteen items and nine unique scenes.
The [compressed client log](client.log.zst) records each registration and the
summary `FD_PONDER passed items=15 registrations=53 uniqueScenes=9`.
Save and Quit to Title saved all dimensions, then Quit Game exited normally;
Gradle reported `BUILD SUCCESSFUL in 8m 51s`. Run `python3 verify.py` here to
check the log, screenshots and digests.

This closes the current-artifact rendered teaching review in the
[FD403 procedure](../../../FD403-ACCEPTANCE.md). It does not close FD403: Arrow,
Legacy, Free Spin and Revised still need their full occupied powered
survival-built client exercises. Ponder's diagram playback is teaching evidence,
not physical motion or clearance evidence. This run did not repeat the older
resource-reload exercise or test optional integrations.
