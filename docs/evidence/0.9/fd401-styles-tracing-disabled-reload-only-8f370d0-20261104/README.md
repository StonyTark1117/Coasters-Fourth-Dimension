# FD401 selected-JAR Styles-only tracing-disabled FANCY reload

This bounded client run used the clean selected qualification JAR from committed source
`8f370d0` (SHA-256
`cf4fed9acf41873f0ba7bd3b28f49fe4507db2ae2442cfcae9bddf8cb7a77cb5`) with Track Styles
1.2.0 as the only optional integration. Its runtime JAR hash is
`42535a83307f6898b33c13ca201533bb9e337418fd7c73039c867fc80bfbcb38`.

The client ran FANCY at render distance 12 on Java 21, using the generated direct launch
script with `-Xmx1536M`, an isolated game directory, private Xvfb `:229`, and an owned
client-only systemd scope capped at 6 GiB. The observed scope peak was 3,543,228,416 bytes.
The clean selected JAR was loaded from `/tmp/fd401-b-tracing-disabled-8f370d0/build/libs/`.

The player completed a normal `F3+T` reload at 22:40:46. The resource manager began a new
load and the Styles catalog inspected 55 decorative model entries at 22:40:48. The player
saved the world and exited through the title screen; the Minecraft process logged `Stopping!`.
The screenshots show the loaded FANCY test route before reload and the resource-reload
confirmation afterward. Neither validation, render-validation, nor snapshot-tracing flags
were enabled.

**Partial result only:** this run proves a tracing-disabled FANCY resource reload and Styles
catalog rebuild on the selected JAR. It does not establish a post-reload occupied ride,
station capture or unload; those remain open, as do the required repeated reload/ride runs
for supported nonshader modes. The quick-play target was `FD401StopCycle`, while the active
server log names the loaded level `FD401BarePlacement`; this naming discrepancy is preserved
as a limitation.
